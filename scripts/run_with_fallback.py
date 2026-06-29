#!/usr/bin/env python3
import sys
import os
import shutil
import subprocess


def has_tools(tools):
    for tool in tools:
        if shutil.which(tool) is None:
            return False
    return True


def main():
    if len(sys.argv) < 2:
        sys.exit(0)

    args = sys.argv[1:]
    cmd = args[0]

    is_slow_check = False
    if cmd == "make" and len(args) > 1 and args[1] in ["build", "test", "build_wasm"]:
        is_slow_check = True
    elif (
        cmd in ["python3", "python"]
        and len(args) > 1
        and ("test_petstore.py" in args[1] or "test_generated_server.py" in args[1])
    ):
        is_slow_check = True

    if is_slow_check and os.environ.get("RUN_SLOW_TESTS") != "1":
        print(f"Skipping slow check {' '.join(args)} (set RUN_SLOW_TESTS=1 to run).")
        sys.exit(0)

    required_tools = []
    if cmd == "make":
        required_tools = ["make", "javac", "java"]
        if len(args) > 1 and args[1] in ["install_deps", "test", "check_deps"]:
            required_tools.append("mvn")
    elif cmd == "mvn":
        required_tools = ["mvn", "java"]
    elif cmd == "python3" or cmd == "python":
        required_tools = [cmd]
        if len(args) > 1 and (
            "test_petstore.py" in args[1] or "test_generated_server.py" in args[1]
        ):
            required_tools.extend(["java", "mvn", "curl"])

    if has_tools(required_tools):
        # Run natively
        try:
            resolved_cmd = shutil.which(cmd) or cmd
            exec_args = [resolved_cmd] + args[1:]
            result = subprocess.run(exec_args)
            sys.exit(result.returncode)
        except Exception as e:
            print(f"Error running natively: {e}")
            sys.exit(1)
    else:
        print(f"Missing one of: {', '.join(required_tools)}. Falling back to Docker...")
        image_name = "cdd-java-fallback-env"

        docker_cmd = shutil.which("docker") or "docker"
        inspect_result = subprocess.run(
            [docker_cmd, "image", "inspect", image_name], capture_output=True
        )
        if inspect_result.returncode != 0:
            print("Building fallback Docker image...")
            dockerfile = """FROM maven:3.9-eclipse-temurin-17
RUN apt-get update && apt-get install -y python3 python3-pip curl nodejs npm make && rm -rf /var/lib/apt/lists/*
"""
            build_process = subprocess.Popen(
                [docker_cmd, "build", "-t", image_name, "-"], stdin=subprocess.PIPE
            )
            build_process.communicate(input=dockerfile.encode("utf-8"))
            if build_process.returncode != 0:
                print("Failed to build Docker image.")
                sys.exit(build_process.returncode)

        pwd = os.getcwd()
        docker_args = [
            docker_cmd,
            "run",
            "--rm",
            "-v",
            f"{pwd}:/app",
            "-w",
            "/app",
            image_name,
        ] + args
        result = subprocess.run(docker_args)
        sys.exit(result.returncode)


if __name__ == "__main__":
    main()
