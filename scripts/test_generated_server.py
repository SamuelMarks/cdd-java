#!/usr/bin/env python3
import sys
import glob
import os
import shutil
import subprocess

if len(sys.argv) < 3:
    print("Usage: test_generated_server.py <version> <json_file>")
    sys.exit(1)

version = sys.argv[1]
json_file = sys.argv[2]

server_dir = f"../cdd-java-server-{version}"

if os.path.exists(server_dir):
    shutil.rmtree(server_dir)

jar_files = glob.glob("target/*-jar-with-dependencies.jar")
if not jar_files:
    print("Error: Could not find jar-with-dependencies in target/")
    sys.exit(1)
jar_file = jar_files[0]

try:
    subprocess.run(["java", "-jar", jar_file, "from_openapi", "to_server", "-i", json_file, "-o", server_dir], check=True)
except subprocess.CalledProcessError:
    sys.exit(1)

try:
    subprocess.run(["mvn", "clean", "test"], cwd=server_dir, shell=(os.name == 'nt'), check=True)
except subprocess.CalledProcessError:
    print("Generated server tests failed!")
    sys.exit(1)

print("Generated server tests passed successfully!")
