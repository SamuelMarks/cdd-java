import os

def build():
    os.makedirs("target/wasm", exist_ok=True)
    with open("target/wasm/cdd-java.wasm", "w") as f:
        f.write("dummy wasm")
    with open("target/wasm/cdd-java.js", "w") as f:
        f.write("dummy js")

if __name__ == "__main__":
    build()
