import { WASI, File, Directory, OpenFile, PreopenDirectory } from "@bjorn3/browser_wasi_shim";

export class CddJavaWasm {
    /**
     * Create the Java WASM Engine
     * @param {ArrayBuffer | WebAssembly.Module} wasmBuffer The loaded WASM binary
     */
    constructor(wasmBuffer) {
        if (!wasmBuffer) {
            throw new Error("WASM buffer or module is required.");
        }
        if (wasmBuffer instanceof WebAssembly.Module) {
            this.wasmModule = wasmBuffer;
        } else {
            this.wasmBuffer = wasmBuffer;
        }
    }

    _extractFiles(dirObj, basePath = "", result = {}) {
        if (!dirObj || !dirObj.contents) return result;
        for (const [name, obj] of dirObj.contents.entries()) {
            const currentPath = basePath ? `${basePath}/${name}` : name;
            if (obj instanceof File) {
                result[currentPath] = new TextDecoder().decode(obj.data);
            } else if (obj instanceof Directory) {
                this._extractFiles(obj, currentPath, result);
            }
        }
        return result;
    }

    async run(args, files = {}) {
        const rootContents = [];
        for (const [name, content] of Object.entries(files)) {
            rootContents.push([name, new File(new TextEncoder().encode(content))]);
        }
        const outDirName = "out";
        rootContents.push([outDirName, new Directory(new Map())]);

        const fds = [
            new OpenFile(new File([])), // fd 0 (stdin)
            new OpenFile(new File([])), // fd 1 (stdout)
            new OpenFile(new File([])), // fd 2 (stderr)
            new PreopenDirectory(".", new Map(rootContents)) // fd 3
        ];

        const wasi = new WASI(args, [], fds);

        let instance;
        if (this.wasmModule) {
            instance = await WebAssembly.instantiate(this.wasmModule, {
                "wasi_snapshot_preview1": wasi.wasiImport
            });
        } else {
            const result = await WebAssembly.instantiate(this.wasmBuffer, {
                "wasi_snapshot_preview1": wasi.wasiImport
            });
            instance = result.instance;
        }

        try {
            wasi.start(instance);
        } catch (e) {
            // WASI typically throws an exception on normal exit, which we can safely ignore
        }

        const stdout = new TextDecoder().decode(fds[1].file.data);
        const stderr = new TextDecoder().decode(fds[2].file.data);

        // Extract generated files
        const outDir = fds[3].dir.contents.get(outDirName);
        const generatedFiles = this._extractFiles(outDir);

        return { stdout, stderr, generatedFiles };
    }

    /**
     * Generates a standard SDK for cdd-java
     */
    async generateSdk(specJsonStr, noGithubActions = false, noInstallablePackage = false) {
        return this.run(
            ["cdd-java", "from_openapi", "to_sdk", "-i", "spec.json", "-o", "out", ...(noGithubActions ? ["--no-github-actions"] : []), ...(noInstallablePackage ? ["--no-installable-package"] : [])],
            { "spec.json": specJsonStr }
        );
    }
    
    /**
     * Generates a CLI-enabled SDK for cdd-java
     */
    async generateSdkCli(specJsonStr, noGithubActions = false, noInstallablePackage = false) {
        return this.run(
            ["cdd-java", "from_openapi", "to_sdk_cli", "-i", "spec.json", "-o", "out", ...(noGithubActions ? ["--no-github-actions"] : []), ...(noInstallablePackage ? ["--no-installable-package"] : [])],
            { "spec.json": specJsonStr }
        );
    }

    /**
     * Generates a Server Implementation
     */
    async generateServer(specJsonStr) {
        return this.run(
            ["cdd-java", "from_openapi", "to_server", "-i", "spec.json", "-o", "out"],
            { "spec.json": specJsonStr }
        );
    }

    /**
     * Generates ORM Entities
     */
    async generateOrm(specJsonStr) {
        return this.run(
            ["cdd-java", "from_openapi", "to_orm", "-i", "spec.json", "-o", "out"],
            { "spec.json": specJsonStr }
        );
    }
}
