#!/bin/bash
set -e
# Even with -target 11 and -source 11, OpenJDK 23's standard library classes (like java.lang.String) are compiled to class version 67/69 in the system modules/JIMAGE.
# TeaVM reads the system classpath (the JDK) to parse classes, and hits version 69, which it doesn't support!
# To fix this, we need to download JDK 11 or JDK 17 to use as the toolchain for maven/TeaVM.
if [ ! -d "jdk-17" ]; then
    echo "Downloading JDK 17..."
    curl -L -o jdk-17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.10_7.tar.gz" || \
    curl -L -o jdk-17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_mac_hotspot_17.0.10_7.tar.gz"
    mkdir -p jdk-17
    tar xzf jdk-17.tar.gz -C jdk-17 --strip-components=1
    rm jdk-17.tar.gz
fi
export JAVA_HOME="$PWD/jdk-17/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java --version
mvn clean package
