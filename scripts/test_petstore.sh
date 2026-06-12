#!/bin/bash
set -e

VERSION=$1
JSON_FILE=$2

CLIENT_DIR="../cdd-java-client-$VERSION"
rm -rf "$CLIENT_DIR"
java -cp "lib/*:bin" cli.Main from_openapi to_sdk -i "$JSON_FILE" --tests -o "$CLIENT_DIR"

SERVER_STARTED_BY_ME=0

if command -v docker >/dev/null 2>&1; then
    docker rm -f petstore-server prism-mock >/dev/null 2>&1 || true
fi
lsof -i :8080 | awk 'NR>1 {print $2}' | xargs kill -9 >/dev/null 2>&1 || true
sleep 1

echo "Starting JVM official petstore server..."
if [ ! -d "/tmp/swagger-petstore" ]; then
    git clone https://github.com/swagger-api/swagger-petstore.git /tmp/swagger-petstore
fi
if [ ! -f "/tmp/swagger-petstore/target/lib/jetty-runner.jar" ]; then
    (cd /tmp/swagger-petstore && mvn package -DskipTests)
fi
java -jar /tmp/swagger-petstore/target/lib/jetty-runner.jar /tmp/swagger-petstore/target/swagger-petstore-1.0.27.war > /dev/null 2>&1 &
PID=$!
SERVER_STARTED_BY_ME=1

for i in {1..10}; do
    if nc -z localhost 8080 2>/dev/null; then
        break
    fi
    sleep 1
done

set +e
(cd "$CLIENT_DIR" && mvn clean test)
TEST_STATUS=$?
set -e

if [ $TEST_STATUS -ne 0 ]; then
    echo "JVM server tests failed! Falling back to non-JVM docker version..."
    kill -9 $PID || true
    SERVER_STARTED_BY_ME=2
    sleep 2
    lsof -i :8080 | awk 'NR>1 {print $2}' | xargs kill -9 >/dev/null 2>&1 || true
    sleep 1
    if command -v docker >/dev/null 2>&1; then
        ABS_JSON="$PWD/$JSON_FILE"
        # Since swaggerapi/petstore on ARM mac via qemu is broken and crashes HTTP parsing,
        # we fallback to Prism which is a non-JVM docker mock server that perfectly parses OpenAPI
        docker run --init --rm -d -p 8080:4010 -v "$ABS_JSON:/spec.json" --name prism-mock stoplight/prism:4 mock -h 0.0.0.0 /spec.json
        sleep 5
        (cd "$CLIENT_DIR" && mvn clean test)
    else
        echo "Docker not found for fallback. Failing."
        exit 1
    fi
fi

if [ "$SERVER_STARTED_BY_ME" = "1" ]; then
    kill -9 $PID || true
elif [ "$SERVER_STARTED_BY_ME" = "2" ]; then
    docker rm -f prism-mock || true
fi
