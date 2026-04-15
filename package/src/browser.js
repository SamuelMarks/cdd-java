export class CddJavaBrowser {
    constructor(wasmPath = './cdd-java.js.wasm') {
        this.wasmPath = wasmPath;
    }

    async run(args, files = {}) {
        const GraalVM = globalThis.GraalVM || (typeof window !== 'undefined' ? window.GraalVM : null);
        
        if (!GraalVM) {
            throw new Error("GraalVM is not defined. Please include cdd-java.js via a <script> tag before using CddJavaBrowser.");
        }

        const payload = {
            command: args,
            files: files
        };

        const outputLogs = [];
        const originalLog = console.log;
        
        // Temporarily intercept console.log to capture WASM stdout
        console.log = (...args) => {
            outputLogs.push(args.join(' '));
        };

        try {
            const config = new GraalVM.Config();
            config.wasm_path = this.wasmPath;
            await GraalVM.run(["process_in_memory", JSON.stringify(payload)], config);
        } finally {
            console.log = originalLog;
        }

        const fullOut = outputLogs.join('\n');
        const startIdx = fullOut.indexOf('CDD_IN_MEMORY_START');
        const endIdx = fullOut.indexOf('CDD_IN_MEMORY_END');

        if (startIdx !== -1 && endIdx !== -1) {
            const jsonStr = fullOut.substring(startIdx + 19, endIdx).trim();
            const result = JSON.parse(jsonStr);
            if (!result.success) {
                throw new Error("WASM execution failed: " + result.error);
            }
            return {
                output: fullOut,
                files: result.files || {}
            };
        } else {
            throw new Error("Failed to parse WASM output. Raw logs: " + fullOut);
        }
    }

    async generateDocsJson(specJsonStr, noImports = false, noWrapping = false) {
        const args = ["to_docs_json", "-i", "spec.json"];
        if (noImports) args.push("--no-imports");
        if (noWrapping) args.push("--no-wrapping");
        
        const res = await this.run(args, { "spec.json": specJsonStr });
        return res.files["docs.json"];
    }

    async generateSdkCli(specJsonStr, noGithubActions = false, noInstallablePackage = false) {
        const args = ["from_openapi", "to_sdk_cli", "-i", "spec.json"];
        if (noGithubActions) args.push("--no-github-actions");
        if (noInstallablePackage) args.push("--no-installable-package");
        return this.run(args, { "spec.json": specJsonStr });
    }

    async generateSdk(specJsonStr) {
        return this.run(["from_openapi", "to_sdk", "-i", "spec.json"], { "spec.json": specJsonStr });
    }

    async generateServer(specJsonStr) {
        return this.run(["from_openapi", "to_server", "-i", "spec.json"], { "spec.json": specJsonStr });
    }

    async generateOrm(specJsonStr) {
        return this.run(["from_openapi", "to_orm", "-i", "spec.json"], { "spec.json": specJsonStr });
    }
}
