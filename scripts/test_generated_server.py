#!/usr/bin/env python3
import sys
import glob
import os
import shutil
import subprocess


def run_cmd(cmd, **kwargs):
    if os.name == "nt" and not kwargs.get("shell") and isinstance(cmd, list):
        cmd[0] = shutil.which(cmd[0]) or cmd[0]
    return subprocess.run(cmd, **kwargs)


if len(sys.argv) < 3:
    print("Usage: test_generated_server.py <version> <json_file>")
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
                "petstore_raw.yaml",
            ],
            check=True,
        )
        run_cmd(
            [
                "npx",
                "--yes",
                "swagger-cli",
                "bundle",
                "petstore_raw.yaml",
                "-t",
                "json",
            ],
            stdout=open(json_file, "w"),
            check=True,
        )

server_dir = f"../cdd-java-server-{version}"

if os.path.exists(server_dir):
    shutil.rmtree(server_dir)

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
            "to_server",
            "-i",
            json_file,
            "-o",
            server_dir,
        ],
        check=True,
    )
except subprocess.CalledProcessError:
    sys.exit(1)

try:
    run_cmd(
        ["mvn", "clean", "test"], cwd=server_dir, shell=(os.name == "nt"), check=True
    )
except subprocess.CalledProcessError:
    print("Generated server tests failed!")
    sys.exit(1)

print("Generated server tests passed successfully!")
