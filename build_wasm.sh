#!/bin/bash
set -e

if [ -z "$GRAALVM_HOME" ]; then
  # Prefer the new GraalVM 25 EA download if present
  GRAALVM_DIR=$(find . -maxdepth 1 -type d -name "graalvm-jdk-25*" | head -n 1)
  if [ -n "$GRAALVM_DIR" ]; then
    if [ -d "$GRAALVM_DIR/Contents/Home" ]; then
      export GRAALVM_HOME="$(pwd)/${GRAALVM_DIR#./}/Contents/Home"
    else
      export GRAALVM_HOME="$(pwd)/${GRAALVM_DIR#./}"
    fi
  else
    echo "Error: GRAALVM_HOME is not set."
    exit 1
  fi
fi

echo "Starting GraalVM 25 SVM-WASM compilation..."
mkdir -p target/wasm

"$GRAALVM_HOME/bin/native-image" \
  --tool:svm-wasm \
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

echo "Compilation successful!"
