export class CddJavaBrowser {
    constructor(wasmPath = './cdd-java.js.wasm') {
        this.wasmPath = wasmPath;
    }

    async run(args, files = {}) {
        let GraalVM = null;
        if (typeof globalThis !== 'undefined' && globalThis.GraalVM) GraalVM = globalThis.GraalVM;
        else if (typeof self !== 'undefined' && self.GraalVM) GraalVM = self.GraalVM;
        else if (typeof window !== 'undefined' && window.GraalVM) GraalVM = window.GraalVM;

        if (!GraalVM) {
            throw new Error("GraalVM is completely missing from the browser context.");
        }

        const payload = {
            command: args,
            files: files
        };

        const outputLogs = [];
        
        try {
            const config = new GraalVM.Config();
            config.env = config.env || {};
            config.wasm_path = this.wasmPath;
            
            let tempLog = function() {
                outputLogs.push(Array.prototype.slice.call(arguments).join(' '));
            };
            
            config.print = tempLog;
            config.printErr = tempLog;

            let originalPostMessage = null;
            if (typeof self !== 'undefined' && self.postMessage) {
                originalPostMessage = self.postMessage;
                self.postMessage = function(data) {
                    if (data && data.status === 'log' && data.message) {
                        outputLogs.push(data.message);
                    }
                    if (originalPostMessage) {
                        originalPostMessage.apply(self, arguments);
                    }
                };
            }

            let resultPromise = GraalVM.run(["process_in_memory", JSON.stringify(payload)], config);
            await Promise.race([
                resultPromise,
                new Promise((_, reject) => setTimeout(() => reject(new Error("GraalVM execution timed out after 30 seconds")), 30000))
            ]);
            
            if (typeof self !== 'undefined' && originalPostMessage) {
                self.postMessage = originalPostMessage;
            }
        } catch(e) {
            outputLogs.push("FATAL EXCEPTION: " + String(e));
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
            throw new Error("Failed to parse WASM output. Expected CDD_IN_MEMORY_START, got:\n" + fullOut);
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
