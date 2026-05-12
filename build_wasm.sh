#!/bin/bash
set -e

if [ -z "$GRAALVM_HOME" ]; then
  # Prefer the new GraalVM 25 EA download if present
  GRAALVM_DIR=$(find . -maxdepth 1 -type d -name "graalvm-jdk-25*" | head -n 1)

  if [ -z "$GRAALVM_DIR" ]; then
    echo "GraalVM 25 not found locally and GRAALVM_HOME not set."
    echo "Downloading GraalVM 25..."
    OS_RAW=$(uname -s)
    if [ "$OS_RAW" = "Darwin" ]; then
      OS="macos"
    else
      OS="linux"
    fi
    ARCH_RAW=$(uname -m)
    if [ "$ARCH_RAW" = "x86_64" ]; then
      ARCH="x64"
    elif [ "$ARCH_RAW" = "arm64" ] || [ "$ARCH_RAW" = "aarch64" ]; then
      ARCH="aarch64"
    else
      ARCH="$ARCH_RAW"
    fi
    
    URL="https://download.oracle.com/graalvm/25/latest/graalvm-jdk-25_${OS}-${ARCH}_bin.tar.gz"
    echo "Fetching $URL..."
    curl -L "$URL" -o graalvm.tar.gz
    tar -xzf graalvm.tar.gz
    rm graalvm.tar.gz
    GRAALVM_DIR=$(find . -maxdepth 1 -type d -name "graalvm-jdk-25*" | head -n 1)
  fi

  if [ -n "$GRAALVM_DIR" ]; then
    if [ -d "$GRAALVM_DIR/Contents/Home" ]; then
      export GRAALVM_HOME="$(pwd)/${GRAALVM_DIR#./}/Contents/Home"
    else
      export GRAALVM_HOME="$(pwd)/${GRAALVM_DIR#./}"
    fi
  else
    echo "Error: GRAALVM_HOME is not set and automatic download failed."
    exit 1
  fi
fi

if ! ls target/cdd-java-*-jar-with-dependencies.jar 1> /dev/null 2>&1; then
  echo "Jar not found. Running mvn package..."
  mvn clean package -DskipTests -Dexec.skip=true
fi

echo "Starting GraalVM 25 SVM-WASM compilation..."
mkdir -p target/wasm

"$GRAALVM_HOME/bin/native-image" \
  --tool:svm-wasm \
  --shared \
  --initialize-at-build-time=cli.Main \
  --shared \
  --initialize-at-build-time=cli.Main \
   \
  -cp target/cdd-java-*-jar-with-dependencies.jar \
  cli.Main \
  -o target/wasm/cdd-java

echo "Patching JS Wrapper for universal usage..."
# Disable auto-run
sed -i.bak 's/GraalVM.run(load_cmd_args(),config).catch(console.error);/\/\/ Auto-run disabled/g' target/wasm/cdd-java.js
# Export GraalVM object universally
cat << 'JS_EOF' >> target/wasm/cdd-java.js

if (typeof exports !== 'undefined') {
    exports.GraalVM = GraalVM;
}
if (typeof window !== 'undefined') {
    window.GraalVM = GraalVM;
}
JS_EOF

rm -f target/wasm/cdd-java.js.bak

# Create a copy of the .wasm file to ensure local cdd-web-ui scripts can find it
cp target/wasm/cdd-java.js.wasm target/wasm/cdd-java.wasm

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

for arg in sys.argv[1:]:
    patch_wasm(arg)
" target/wasm/cdd-java.wasm
