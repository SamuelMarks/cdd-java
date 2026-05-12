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
  --initialize-at-build-time=cli.Main,cli.GraalEntryPoint \
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
echo "GraalVM SVM-WASM binary keeps 'main' export (not renamed to '_start')."
echo "The cdd-ctl-wasm-sdk handles GraalVM binaries via the 'main' export path."
