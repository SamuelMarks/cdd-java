#!/usr/bin/env python3
import sys
import glob
import os
import shutil
import subprocess
import time
import socket


def run_cmd(cmd, **kwargs):
    if os.name == "nt" and not kwargs.get("shell") and isinstance(cmd, list):
        cmd[0] = shutil.which(cmd[0]) or cmd[0]
    return subprocess.run(cmd, **kwargs)


if len(sys.argv) < 3:
    print("Usage: test_petstore.py <version> <json_file>")
    sys.exit(1)

version = sys.argv[1]
json_file = sys.argv[2]

if not os.path.exists(json_file):
    print(f"{json_file} not found. Attempting to download...")
    if version == "v2":
        run_cmd(
            [
                "curl",
                "-sL",
                "https://petstore.swagger.io/v2/swagger.json",
                "-o",
                json_file,
            ],
            check=True,
        )
    elif version == "v3":
        run_cmd(
            [
                "curl",
                "-sL",
                "https://raw.githubusercontent.com/swagger-api/swagger-petstore/master/src/main/resources/openapi.yaml",
                "-o",
                json_file,
            ],
            check=True,
        )

client_dir = f"../cdd-java-client-{version}"

if os.path.exists(client_dir):
    shutil.rmtree(client_dir)

print("test_petstore cwd:", os.getcwd())
if os.path.exists("target"):
    print("test_petstore target contents:", os.listdir("target"))
else:
    print("test_petstore target NOT FOUND")

jar_files = glob.glob("target/*-jar-with-dependencies.jar")
if not jar_files:
    print("Error: Could not find jar-with-dependencies in target/")
    sys.exit(1)
jar_file = jar_files[0]

try:
    run_cmd(
        [
            "java",
            "-jar",
            jar_file,
            "from_openapi",
            "to_sdk",
            "-i",
            json_file,
            "--tests",
            "-o",
            client_dir,
        ],
        check=True,
    )
except subprocess.CalledProcessError:
    sys.exit(1)

server_started_by_me = 0


def check_port(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(("localhost", port)) == 0


server_started_by_me = 0

if check_port(8080):
    print("Mock server is already running on port 8080, reusing it...")
else:
    print("Mock server not running. Starting swaggerapi/petstore via Docker...")
    if not shutil.which("docker"):
        print("Docker not found and mock server not running. Failing.")
        sys.exit(1)

    if (
        subprocess.run(
            ["docker", "info"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
        ).returncode
        != 0
    ):
        print("Docker daemon not running, skipping test.")
        sys.exit(0)

    # Try to clean up any leftover container
    try:
        run_cmd(
            ["docker", "rm", "-f", "petstore-server"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    except Exception:
        pass

    run_cmd(
        [
            "docker",
            "run",
            "--rm",
            "-d",
            "-p",
            "8080:8080",
            "--name",
            "petstore-server",
            "swaggerapi/petstore",
        ],
        check=True,
    )
    server_started_by_me = 1

    # Wait for it to become available
    for _ in range(30):
        if check_port(8080):
            break
        time.sleep(1)

test_status = run_cmd(
    ["mvn", "clean", "test"], cwd=client_dir, shell=(os.name == "nt")
).returncode

if test_status != 0:
    print("Client tests failed!")
    if server_started_by_me == 1:
        run_cmd(
            ["docker", "rm", "-f", "petstore-server"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    sys.exit(1)

if server_started_by_me == 1:
    run_cmd(
        ["docker", "rm", "-f", "petstore-server"],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
