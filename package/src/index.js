const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

class CddJava {
    constructor() {
        this.wasmDir = path.join(__dirname, '..', 'dist');
        this.wasmPath = path.join(this.wasmDir, 'cdd-java.js.wasm');
    }

    run(args, files = {}) {
        const payload = {
            command: args,
            files: files
        };

        const tempDir = os.tmpdir() + '/cdd_java_' + Date.now() + Math.random().toString(36).substring(7);
        fs.mkdirSync(tempDir, { recursive: true });

        const runnerScript = path.join(tempDir, 'runner.js');
        const cddJsPath = path.join(this.wasmDir, 'cdd-java.js').replace(/\\/g, '/');
        const wasmFilePath = this.wasmPath.replace(/\\/g, '/');

        const scriptContent = `
const { GraalVM } = require('${cddJsPath}');

async function main() {
    try {
        const config = new GraalVM.Config();
        config.wasm_path = '${wasmFilePath}';
        await GraalVM.run(["process_in_memory", ${JSON.stringify(JSON.stringify(payload))}], config);
    } catch (e) {
        console.error(e);
        process.exit(1);
    }
}
main();
`;
        fs.writeFileSync(runnerScript, scriptContent);

        try {
            const output = execSync(`node ${runnerScript}`, {
                stdio: 'pipe',
                maxBuffer: 50 * 1024 * 1024
            });

            const fullOut = output.toString();
            const startIdx = fullOut.indexOf('CDD_IN_MEMORY_START');
            const endIdx = fullOut.indexOf('CDD_IN_MEMORY_END');

            if (startIdx !== -1 && endIdx !== -1) {
                const jsonStr = fullOut.substring(startIdx + 19, endIdx).trim();
                const result = JSON.parse(jsonStr);
                if (!result.success) {
                    throw new Error("WASM execution failed: " + result.error);
                }
                return {
                    output: "Success",
                    files: result.files || {}
                };
            } else {
                throw new Error("Failed to parse WASM output. Raw logs: " + fullOut);
            }
        } finally {
            fs.rmSync(tempDir, { recursive: true, force: true });
        }
    }

    async extractOpenAPI(codePath) {
        throw new Error("extractOpenAPI not yet implemented for SVM Wasm in this wrapper. Requires local file paths.");
    }

    async generateDocsJson(specJsonStr, noImports = false, noWrapping = false) {
        const args = ["to_docs_json", "-i", "spec.json"];
        if (noImports) args.push("--no-imports");
        if (noWrapping) args.push("--no-wrapping");

        const res = this.run(args, { "spec.json": specJsonStr });
        return res.files["docs.json"];
    }

    async generateSdkCli(specJsonStr, noGithubActions = false, noInstallablePackage = false, generateTests = false) {
        const args = ["from_openapi", "to_sdk_cli", "-i", "spec.json"];
        if (noGithubActions) args.push("--no-github-actions");
        if (noInstallablePackage) args.push("--no-installable-package");
        if (generateTests) args.push("--tests");
        return this.run(args, { "spec.json": specJsonStr });
    }

    async generateSdk(specJsonStr, noGithubActions = false, noInstallablePackage = false, generateTests = false) {
        const args = ["from_openapi", "to_sdk", "-i", "spec.json"];
        if (noGithubActions) args.push("--no-github-actions");
        if (noInstallablePackage) args.push("--no-installable-package");
        if (generateTests) args.push("--tests");
        return this.run(args, { "spec.json": specJsonStr });
    }

    async generateServer(specJsonStr) {
        return this.run(["from_openapi", "to_server", "-i", "spec.json"], { "spec.json": specJsonStr });
    }

    async generateOrm(specJsonStr) {
        return this.run(["from_openapi", "to_orm", "-i", "spec.json"], { "spec.json": specJsonStr });
    }
}

module.exports = { CddJava };
