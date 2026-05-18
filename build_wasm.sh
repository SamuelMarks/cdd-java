#!/bin/bash
set -e

if ! ls target/cdd-java-*-jar-with-dependencies.jar 1> /dev/null 2>&1; then
  echo "Jar not found. Running mvn package..."
  mvn clean package -DskipTests -Dexec.skip=true
fi

echo "Starting GraalVM WASI compilation..."
mkdir -p target/wasm

# Check if Docker is available to use the official GraalVM CE 22.3 image for reliable WASI compilation
if command -v docker &> /dev/null || ( [ -S "$HOME/.rd/docker.sock" ] && DOCKER_HOST="unix://$HOME/.rd/docker.sock" "$HOME/.rd/bin/docker" ps &> /dev/null ) || ( [ -S "$HOME/.orbstack/run/docker.sock" ] && DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock" docker ps &> /dev/null ); then
  if docker ps &> /dev/null; then
    DOCKER_CMD="docker"
  elif [ -S "$HOME/.rd/docker.sock" ]; then
    export DOCKER_HOST="unix://$HOME/.rd/docker.sock"
    DOCKER_CMD="$HOME/.rd/bin/docker"
  elif [ -S "$HOME/.orbstack/run/docker.sock" ]; then
    export DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock"
    DOCKER_CMD="docker"
  else
    DOCKER_CMD="docker"
  fi
  echo "Using Docker (ubuntu) for WASI compilation to match release script exactly..."
  cat << 'DOCKEREOF' > Dockerfile.wasi
FROM ghcr.io/graalvm/native-image-community:22.3.1
RUN microdnf install -y wget tar gzip
RUN wget -q https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-20/wasi-sdk-20.0-linux.tar.gz && \
    tar -xzf wasi-sdk-20.0-linux.tar.gz && \
    mv wasi-sdk-20.0 /opt/wasi-sdk
WORKDIR /app
COPY . /app
RUN native-image --target=wasm32-wasi -H:WasiSdkPath=/opt/wasi-sdk --no-fallback -cp target/cdd-java-*-jar-with-dependencies.jar --initialize-at-build-time=com.github.javaparser --initialize-at-build-time=org.json -O3 cli.Main -o target/wasm/cdd-java
DOCKEREOF
  $DOCKER_CMD build --platform linux/amd64 -t cdd-java-wasi -f Dockerfile.wasi .
  $DOCKER_CMD run --platform linux/amd64 --rm -v "$(pwd)/target/wasm:/output" cdd-java-wasi cp /app/target/wasm/cdd-java /output/cdd-java.wasm
  rm Dockerfile.wasi
  
else
  # Fallback to local GraalVM if Docker is not available
  if [ -z "$GRAALVM_HOME" ]; then
    echo "Error: Docker is not installed and GRAALVM_HOME is not set."
    echo "A Linux GraalVM CE 22.3 installation is required to build WASI natively."
    exit 1
  fi
  if [ -z "$WASI_SDK_PATH" ]; then
    echo "Error: WASI_SDK_PATH is not set."
    exit 1
  fi
  "$GRAALVM_HOME/bin/native-image" \
    --target=wasm32-wasi \
    -H:WasiSdkPath="$WASI_SDK_PATH" \
    --no-fallback \
    -cp target/cdd-java-*-jar-with-dependencies.jar \
    --initialize-at-build-time=com.github.javaparser \
    --initialize-at-build-time=org.json \
    -O3 \
    cli.Main \
    -o target/wasm/cdd-java.wasm
fi

echo "Compilation successful!"

# Rename 'main' to '_start' in the wasm file to bypass GraalVM checks in standard WASM environments
python3 -c "
import sys

def parse_leb128(data, offset):
    result = 0
    shift = 0
    while True:
        byte = data[offset]
        offset += 1
        result |= (byte & 0x7f) << shift
        if (byte & 0x80) == 0:
            break
        shift += 7
    return result, offset

def encode_leb128(value, length=None):
    result = bytearray()
    while True:
        byte = value & 0x7f
        value >>= 7
        if (value == 0 and (byte & 0x40) == 0) or (value == -1 and (byte & 0x40) != 0):
            result.append(byte | (0x80 if length and len(result) < length - 1 else 0))
            if not length or len(result) == length:
                break
        else:
            result.append(byte | 0x80)
    if length:
        while len(result) < length:
            result.append(0x80 if len(result) < length - 1 else 0)
    return result

def patch_wasm(file_path):
    try:
        with open(file_path, 'rb') as f:
            data = bytearray(f.read())
        
        if data[:8] != b'\x00asm\x01\x00\x00\x00':
            print('Not a valid WASM file')
            return False
        
        offset = 8
        while offset < len(data):
            section_id = data[offset]
            offset += 1
            section_size, new_offset = parse_leb128(data, offset)
            leb128_len = new_offset - offset
            
            section_start = new_offset
            section_end = section_start + section_size
            
            if section_id == 7: # Export section
                main_bytes = b'\x04main'
                idx = data.find(main_bytes, section_start, section_end)
                
                if idx != -1:
                    start_bytes = b'\x06_start'
                    size_diff = len(start_bytes) - len(main_bytes)
                    
                    data[idx:idx+len(main_bytes)] = start_bytes
                    
                    new_section_size = section_size + size_diff
                    new_size_bytes = encode_leb128(new_section_size)
                    
                    if len(new_size_bytes) == leb128_len:
                        data[offset:new_offset] = new_size_bytes
                    elif len(new_size_bytes) < leb128_len:
                        padded = encode_leb128(new_section_size, leb128_len)
                        data[offset:new_offset] = padded
                    
                    with open(file_path, 'wb') as f:
                        f.write(data)
                    print(f\"Patched export 'main' -> '_start' in {file_path}\")
                    return True
                else:
                    pass
            
            offset = section_end
        
        return False
    except Exception as e:
        print(f\"Warning: could not patch {file_path}: {e}\")

for arg in sys.argv[1:]:
    patch_wasm(arg)
" target/wasm/cdd-java.wasm

echo "Generated pure WASI standalone binary at target/wasm/cdd-java.wasm"
