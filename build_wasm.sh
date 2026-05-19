#!/bin/bash
set -e

if ! ls target/cdd-java-*-jar-with-dependencies.jar 1> /dev/null 2>&1; then
  echo "Jar not found. Running mvn package..."
  mvn clean package -DskipTests -Dexec.skip=true
fi

echo "Starting GraalVM WASI compilation..."
mkdir -p target/wasm

# Check if Docker or a Docker-compatible alternative (like nerdctl) is available to use the official GraalVM CE 22.3 image for reliable WASI compilation
DOCKER_CMD=""

if command -v docker &> /dev/null && docker ps &> /dev/null; then
  DOCKER_CMD="docker"
elif [ -S "$HOME/.rd/docker.sock" ] && DOCKER_HOST="unix://$HOME/.rd/docker.sock" "$HOME/.rd/bin/docker" ps &> /dev/null; then
  export DOCKER_HOST="unix://$HOME/.rd/docker.sock"
  DOCKER_CMD="$HOME/.rd/bin/docker"
elif [ -S "$HOME/.orbstack/run/docker.sock" ] && DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock" docker ps &> /dev/null; then
  export DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock"
  DOCKER_CMD="docker"
elif command -v nerdctl &> /dev/null && nerdctl ps &> /dev/null; then
  DOCKER_CMD="nerdctl"
elif command -v lima &> /dev/null && lima nerdctl ps &> /dev/null; then
  DOCKER_CMD="lima nerdctl"
fi

if [ -n "$DOCKER_CMD" ]; then
  echo "Using container runtime ($DOCKER_CMD) for WASI compilation to match release script exactly..."
  cat << 'DOCKEREOF' > Dockerfile.wasi
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y wget tar gzip ca-certificates curl build-essential maven
RUN cd /opt && \
    curl -sL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/graalvm-ce-java17-linux-amd64-22.3.1.tar.gz | tar -xz && \
    ./graalvm-ce-java17-22.3.1/bin/gu install native-image && curl -sL -o wasm32-wasi-libs.tar.gz https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-20/wasi-sysroot-20.0.tar.gz && mkdir -p /opt/graalvm-ce-java17-22.3.1/lib/svm/clibraries/wasm32-wasi && tar -xzf wasm32-wasi-libs.tar.gz -C /opt && mv /opt/wasi-sysroot*/* /opt/graalvm-ce-java17-22.3.1/lib/svm/clibraries/wasm32-wasi/ && \
    curl -sL -o wasi-sdk.tar.gz https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-20/wasi-sdk-20.0-linux.tar.gz && \
    tar -xzf wasi-sdk.tar.gz && mv wasi-sdk-20.0* wasi-sdk-20.0 || true
ENV GRAALVM_HOME=/opt/graalvm-ce-java17-22.3.1
ENV WASI_SDK_PATH=/opt/wasi-sdk-20.0
ENV JAVA_HOME=/opt/graalvm-ce-java17-22.3.1
ENV PATH=$JAVA_HOME/bin:$PATH
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests
RUN native-image --target=wasm32-wasi -H:WasiSdkPath=$WASI_SDK_PATH --no-fallback -cp target/cdd-java-*-jar-with-dependencies.jar --initialize-at-build-time=com.github.javaparser --initialize-at-build-time=org.json -O3 cli.Main -o target/wasm/cdd-java
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
