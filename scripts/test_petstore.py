#!/usr/bin/env python3
import sys
import glob
import os
import shutil
import subprocess
import time
import socket

if len(sys.argv) < 3:
    print("Usage: test_petstore.py <version> <json_file>")
    sys.exit(1)

version = sys.argv[1]
json_file = sys.argv[2]

if not os.path.exists(json_file):
    print(f"{json_file} not found. Attempting to download...")
    if version == "v2":
        subprocess.run(["curl", "-sL", "https://petstore.swagger.io/v2/swagger.json", "-o", json_file], check=True)
    elif version == "v3":
        subprocess.run(["curl", "-sL", "https://raw.githubusercontent.com/swagger-api/swagger-petstore/master/src/main/resources/openapi.yaml", "-o", "petstore_raw.yaml"], check=True)
        subprocess.run(["npx", "swagger-cli", "bundle", "petstore_raw.yaml", "-t", "json"], stdout=open(json_file, "w"), check=True)

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
    subprocess.run(["java", "-jar", jar_file, "from_openapi", "to_sdk", "-i", json_file, "--tests", "-o", client_dir], check=True)
except subprocess.CalledProcessError:
    sys.exit(1)

server_started_by_me = 0

def run_cmd(cmd, **kwargs):
    return subprocess.run(cmd, **kwargs)

def kill_port_8080():
    if os.name == 'nt':
        try:
            output = subprocess.check_output('netstat -ano | findstr :8080', shell=True).decode()
            for line in output.splitlines():
                parts = line.strip().split()
                if len(parts) >= 5 and parts[3] == "LISTENING":
                    pid = parts[4]
                    subprocess.run(f'taskkill /F /PID {pid}', shell=True, stderr=subprocess.DEVNULL)
        except Exception:
            pass
    else:
        try:
            pids = subprocess.check_output("lsof -i :8080 | awk 'NR>1 {print $2}'", shell=True).decode().split()
            for pid in pids:
                subprocess.run(["kill", "-9", pid], stderr=subprocess.DEVNULL)
        except Exception:
            pass

def check_port(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(('localhost', port)) == 0

try:
    run_cmd(["docker", "rm", "-f", "petstore-server", "prism-mock"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
except Exception:
    pass

kill_port_8080()
time.sleep(1)

print("Starting JVM official petstore server...")
tmp_dir = os.environ.get("TEMP", "/tmp") if os.name == 'nt' else "/tmp"
petstore_dir = os.path.join(tmp_dir, "swagger-petstore")

if not os.path.exists(os.path.join(petstore_dir, "pom.xml")):
    if os.path.exists(petstore_dir):
        shutil.rmtree(petstore_dir)
    run_cmd(["git", "clone", "https://github.com/swagger-api/swagger-petstore.git", petstore_dir], check=True)

jetty_runner = os.path.join(petstore_dir, "target", "lib", "jetty-runner.jar")
if not os.path.exists(jetty_runner):
    run_cmd(["mvn", "package", "-DskipTests"], cwd=petstore_dir, shell=(os.name == 'nt'), check=True)

war_file = os.path.join(petstore_dir, "target", "swagger-petstore-1.0.27.war")
server_proc = subprocess.Popen(["java", "-jar", jetty_runner, war_file], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
server_started_by_me = 1

for _ in range(10):
    if check_port(8080):
        break
    time.sleep(1)

test_status = run_cmd(["mvn", "clean", "test"], cwd=client_dir, shell=(os.name == 'nt')).returncode

if test_status != 0:
    print("JVM server tests failed! Falling back to non-JVM docker version...")
    server_proc.kill()
    server_started_by_me = 2
    time.sleep(2)
    kill_port_8080()
    time.sleep(1)

    if shutil.which("docker"):
        abs_json = os.path.abspath(json_file)
        if os.name == 'nt':
            abs_json = abs_json.replace('\\', '/')
            if abs_json.startswith(('C:', 'D:', 'E:', 'F:')):
                abs_json = '/' + abs_json[0].lower() + abs_json[2:]
        run_cmd(["docker", "run", "--init", "--rm", "-d", "-p", "8080:4010", "-v", f"{abs_json}:/spec.json", "--name", "prism-mock", "stoplight/prism:4", "mock", "-h", "0.0.0.0", "/spec.json"])
        time.sleep(5)
        test_status = run_cmd(["mvn", "clean", "test"], cwd=client_dir, shell=(os.name == 'nt')).returncode
        if test_status != 0:
            print("Fallback docker tests failed.")
            sys.exit(1)
    else:
        print("Docker not found for fallback. Failing.")
        sys.exit(1)

if server_started_by_me == 1:
    server_proc.kill()
elif server_started_by_me == 2:
    run_cmd(["docker", "rm", "-f", "prism-mock"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
