#!/usr/bin/env python3
"""
Builds the WasmGC binary and JavaScript wrapper for cdd-java using GraalVM 25 SVM-WASM.
Raises a RuntimeError if compilation fails or if valid WASM/JS artifacts are not generated.
"""

import glob
import os
import platform
import shutil
import subprocess
import tarfile
import urllib.request

WASM_MAGIC = b"\x00asm\x01\x00\x00\x00"


def find_or_download_graalvm(project_root: str) -> str:
    """Locates an existing GraalVM 25+ installation or downloads Oracle GraalVM 25."""
    env_home = os.environ.get("GRAALVM_HOME")
    if env_home and os.path.isdir(env_home):
        native_img = os.path.join(
            env_home, "bin", "native-image.cmd" if os.name == "nt" else "native-image"
        )
        if os.path.isfile(native_img):
            return env_home

    search_roots = [
        project_root,
        os.path.expanduser("~/.tools"),
        os.path.expanduser("~/Library/Java/JavaVirtualMachines"),
        "/Library/Java/JavaVirtualMachines",
    ]
    for root in search_roots:
        if not os.path.isdir(root):
            continue
        for candidate in sorted(
            glob.glob(os.path.join(root, "graalvm-jdk-25*")), reverse=True
        ):
            if not os.path.isdir(candidate):
                continue
            home = (
                os.path.join(candidate, "Contents", "Home")
                if os.path.isdir(os.path.join(candidate, "Contents", "Home"))
                else candidate
            )
            native_img = os.path.join(
                home, "bin", "native-image.cmd" if os.name == "nt" else "native-image"
            )
            if os.path.isfile(native_img):
                return home

    sys_name = platform.system().lower()
    if sys_name == "darwin":
        os_tag = "macos"
        ext = "tar.gz"
    elif sys_name == "linux":
        os_tag = "linux"
        ext = "tar.gz"
    elif sys_name == "windows":
        os_tag = "windows"
        ext = "zip"
    else:
        raise RuntimeError(
            f"Unsupported operating system for GraalVM 25 download: {sys_name}"
        )

    machine = platform.machine().lower()
    if machine in ("x86_64", "amd64"):
        arch_tag = "x64"
    elif machine in ("arm64", "aarch64"):
        arch_tag = "aarch64"
    else:
        raise RuntimeError(
            f"Unsupported architecture for GraalVM 25 download: {machine}"
        )

    url = f"https://download.oracle.com/graalvm/25/latest/graalvm-jdk-25_{os_tag}-{arch_tag}_bin.{ext}"
    archive_path = os.path.join(project_root, f"graalvm-jdk-25.{ext}")
    print(f"GraalVM 25 not found locally. Downloading from {url}...")
    try:
        urllib.request.urlretrieve(url, archive_path)
        print("Extracting GraalVM 25 archive...")
        if ext == "tar.gz":
            with tarfile.open(archive_path, "r:gz") as tar:
                tar.extractall(path=project_root)
        else:
            shutil.unpack_archive(archive_path, project_root)
    finally:
        if os.path.exists(archive_path):
            os.remove(archive_path)

    for candidate in sorted(
        glob.glob(os.path.join(project_root, "graalvm-jdk-25*")), reverse=True
    ):
        if not os.path.isdir(candidate):
            continue
        home = (
            os.path.join(candidate, "Contents", "Home")
            if os.path.isdir(os.path.join(candidate, "Contents", "Home"))
            else candidate
        )
        native_img = os.path.join(
            home, "bin", "native-image.cmd" if os.name == "nt" else "native-image"
        )
        if os.path.isfile(native_img):
            return home

    raise RuntimeError("Failed to locate GraalVM 25 native-image after download.")


def ensure_jar_built(project_root: str) -> str:
    """Ensures the shaded JAR with dependencies is built and returns its path."""
    jar_pattern = os.path.join(
        project_root, "target", "cdd-java-*-jar-with-dependencies.jar"
    )
    jars = sorted(glob.glob(jar_pattern))
    if not jars:
        print("Shaded JAR not found. Running mvn package -DskipTests...")
        mvn_cmd = shutil.which("mvn.cmd" if os.name == "nt" else "mvn") or "mvn"
        subprocess.run(
            [
                mvn_cmd,
                "package",
                "-DskipTests",
                "-Dcheckstyle.skip=true",
                "-Dspotless.check.skip=true",
            ],
            cwd=project_root,
            check=True,
        )
        jars = sorted(glob.glob(jar_pattern))
    if not jars:
        raise RuntimeError(
            f"Expected shaded JAR matching {jar_pattern}, but none was found."
        )
    return jars[-1]


def patch_js_wrapper(js_path: str) -> None:
    """Disables auto-run on load and exports the GraalVM object universally."""
    if not os.path.isfile(js_path):
        raise RuntimeError(
            f"Expected generated JS wrapper at {js_path}, but it does not exist."
        )

    with open(js_path, "r", encoding="utf-8") as f:
        content = f.read()

    if len(content) < 100 or "GraalVM" not in content:
        raise RuntimeError(
            f"Generated JS wrapper at {js_path} is invalid or truncated ({len(content)} bytes)."
        )

    content = content.replace(
        "GraalVM.run(load_cmd_args(),config).catch(console.error);",
        "// Auto-run disabled",
    )
    content = content.replace(
        "var stdoutWriter = new ConsoleWriter(console.log);",
        "var stdoutWriter = new ConsoleWriter((...args) => console.log(...args));",
    )
    content = content.replace(
        "var stderrWriter = new ConsoleWriter(console.error);",
        "var stderrWriter = new ConsoleWriter((...args) => console.error(...args));",
    )

    universal_export = """
if (typeof exports !== 'undefined') {
    exports.GraalVM = GraalVM;
}
if (typeof window !== 'undefined') {
    window.GraalVM = GraalVM;
}
if (typeof globalThis !== 'undefined') {
    globalThis.GraalVM = GraalVM;
}
if (typeof self !== 'undefined') {
    self.GraalVM = GraalVM;
}
"""
    if "globalThis.GraalVM = GraalVM;" not in content:
        content = content + universal_export

    with open(js_path, "w", encoding="utf-8") as f:
        f.write(content)


def validate_wasm_binary(wasm_path: str) -> None:
    """Validates that the compiled WASM file exists, has the WASM magic header, and is non-trivial."""
    if not os.path.isfile(wasm_path):
        raise RuntimeError(f"WASM binary was not generated at {wasm_path}.")
    size = os.path.getsize(wasm_path)
    if size < 1024:
        raise RuntimeError(
            f"WASM binary at {wasm_path} is invalid or too small ({size} bytes)."
        )
    with open(wasm_path, "rb") as f:
        header = f.read(8)
    if header != WASM_MAGIC:
        raise RuntimeError(
            f"WASM binary at {wasm_path} is missing the standard WebAssembly magic header (got {header!r})."
        )


def build() -> None:
    """Executes the full GraalVM 25 SVM-WASM build and validates all outputs."""
    project_root = os.path.dirname(os.path.abspath(__file__))
    jar_path = ensure_jar_built(project_root)
    graalvm_home = find_or_download_graalvm(project_root)
    native_image_bin = os.path.join(
        graalvm_home, "bin", "native-image.cmd" if os.name == "nt" else "native-image"
    )

    wasm_dir = os.path.join(project_root, "target", "wasm")
    os.makedirs(wasm_dir, exist_ok=True)
    output_prefix = os.path.join(wasm_dir, "cdd-java")

    js_path = os.path.join(wasm_dir, "cdd-java.js")
    js_wasm_path = os.path.join(wasm_dir, "cdd-java.js.wasm")
    wasm_path = os.path.join(wasm_dir, "cdd-java.wasm")

    force_rebuild = os.environ.get("FORCE_REBUILD_WASM", "").lower() in ("1", "true", "yes")
    if (
        not force_rebuild
        and os.path.isfile(js_wasm_path)
        and os.path.isfile(js_path)
        and os.path.getmtime(js_wasm_path) >= os.path.getmtime(jar_path)
        and os.path.getmtime(js_wasm_path) >= os.path.getmtime(__file__)
    ):
        try:
            patch_js_wrapper(js_path)
            validate_wasm_binary(js_wasm_path)
            shutil.copyfile(js_wasm_path, wasm_path)
            validate_wasm_binary(wasm_path)
            print("Up-to-date valid WasmGC binary found in target/wasm/, skipping native-image re-run.")
            return
        except RuntimeError:
            pass

    print(f"Starting GraalVM 25 SVM-WASM compilation using {native_image_bin}...")
    env = os.environ.copy()
    env["GRAALVM_HOME"] = graalvm_home
    env["JAVA_HOME"] = graalvm_home

    cmd = [
        native_image_bin,
        "--tool:svm-wasm",
        "-O3",
        "-H:+UnlockExperimentalVMOptions",
        "-H:-VMContinuations",
    ]
    if platform.system().lower() == "darwin":
        cmd.append("--initialize-at-build-time=apple.security")
    cmd.extend(
        [
            "-cp",
            jar_path,
            "cli.Main",
            "-o",
            output_prefix,
        ]
    )

    subprocess.run(
        cmd,
        cwd=project_root,
        env=env,
        check=True,
    )

    wat_path = os.path.join(wasm_dir, "cdd-java.js.wat")
    if os.path.exists(wat_path):
        os.remove(wat_path)

    js_path = os.path.join(wasm_dir, "cdd-java.js")
    js_wasm_path = os.path.join(wasm_dir, "cdd-java.js.wasm")
    wasm_path = os.path.join(wasm_dir, "cdd-java.wasm")

    patch_js_wrapper(js_path)
    validate_wasm_binary(js_wasm_path)
    shutil.copyfile(js_wasm_path, wasm_path)
    validate_wasm_binary(wasm_path)
    print(f"Successfully compiled {js_wasm_path}, {wasm_path}, and {js_path}.")


if __name__ == "__main__":
    build()
