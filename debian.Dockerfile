# Stage 1: Build
FROM debian:12-slim AS builder
RUN apt-get update && apt-get install -y --no-install-recommends openjdk-17-jdk-headless make && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN make install_deps && make build

# Stage 2: Runtime
FROM debian:12-slim
RUN apt-get update && apt-get install -y --no-install-recommends openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib

ENTRYPOINT ["java", "-cp", "lib/*:bin", "cli.Main", "serve_json_rpc"]
