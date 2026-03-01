# Stage 1: Build
FROM alpine:3.21 AS builder
RUN apk add --no-cache openjdk17 make bash
WORKDIR /app
COPY . .
RUN make install_deps && make build

# Stage 2: Runtime
FROM alpine:3.21
RUN apk add --no-cache openjdk17-jre-headless
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib

ENTRYPOINT ["java", "-cp", "lib/*:bin", "cli.Main", "serve_json_rpc"]
