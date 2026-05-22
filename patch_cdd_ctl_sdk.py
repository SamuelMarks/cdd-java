import sys
import re

file_path = '/Users/samuel/repos/cdd-ctl/cdd-ctl-wasm-sdk/src/index.ts'
with open(file_path, 'r') as f:
    content = f.read()

java_block = """
    if (options.ecosystem === "cdd-java") {
      let CddJavaBrowser;
      try {
        // @ts-ignore
        if (typeof importScripts === 'function') {
            try {
                // @ts-ignore
                importScripts(location.origin + '/assets/wasm/cdd-java.js');
                if ((self as any).GraalVM) {
                    (globalThis as any).GraalVM = (self as any).GraalVM;
                }
            } catch(e) {
                console.warn("Failed to importScripts cdd-java.js", e);
            }
        }

        // Wait, if it's running in a WebWorker, 'window' is not defined. We need to check 'self'.
        if (!(globalThis as any).GraalVM && typeof self !== 'undefined' && (self as any).GraalVM) {
          (globalThis as any).GraalVM = (self as any).GraalVM;
        }

        // If it's still missing, provide a mock GraalVM that throws a clearer error inside the wrapper
        if (!(globalThis as any).GraalVM) {
           (globalThis as any).GraalVM = null;
        }

        // @ts-ignore
        const mod = await import("cdd-java-cli");
        CddJavaBrowser = mod.CddJavaBrowser || mod.default?.CddJavaBrowser || Object.values(mod)[0];
        if (!CddJavaBrowser) {
          throw new Error("Could not find CddJavaBrowser in imported cdd-java-cli module: " + Object.keys(mod).join(","));
        }
      } catch (e: any) {
        throw new Error("cdd-java-cli is not installed or available for WasmGC execution. Error: " + (e.message || String(e)));
      }

      // Also apply a mock to globalThis for the tests so it avoids ReferenceError if it uses globalThis.GraalVM before we inject it
      if (typeof globalThis !== 'undefined' && !(globalThis as any).GraalVM) {
          (globalThis as any).GraalVM = null;
      }

      let engine;
      try {
          const wasmUrl = (typeof location !== 'undefined' ? location.origin : '') + '/assets/wasm/cdd-java.wasm';
          engine = new CddJavaBrowser(wasmUrl);
      } catch (e) {
         throw new Error("Failed to instantiate CddJavaBrowser: " + e);
      }

      // HACK: manually inject GraalVM to bypass wrapper check if it somehow loaded but wrapper failed
      if (typeof self !== 'undefined' && (self as any).GraalVM) {
          (globalThis as any).GraalVM = (self as any).GraalVM;
      }

      // Restore standard console logs inside GraalVM intercept block because Playwright swallows it otherwise during failure
      const originalLog = console.log;
      console.log = (...args) => {
         originalLog("[GraalVM Log]", ...args);
      };

      const specContentStr =
          typeof options.specContent === "string"
            ? options.specContent
            : new TextDecoder().decode(options.specContent);
      let res;
      try {
          if (options.target === "to_server") {
              res = await engine.generateServer(specContentStr);
          } else if (options.target === "to_sdk_cli") {
              res = await engine.generateSdkCli(specContentStr, !!options.additionalArgs?.includes('--no-github-actions'), !!options.additionalArgs?.includes('--no-installable-package'), !!options.additionalArgs?.includes('--tests'));
          } else {
              // @ts-ignore
              if (options.target === "to_orm") { res = await engine.generateOrm(specContentStr); }
              else { res = await engine.generateSdk(specContentStr, !!options.additionalArgs?.includes('--no-github-actions'), !!options.additionalArgs?.includes('--no-installable-package'), !!options.additionalArgs?.includes('--tests')); }
          }
      } finally {
          console.log = originalLog;
      }

      const results: GeneratedFile[] = [];
      for (const [path, content] of Object.entries(res.files)) {
          results.push({
              path: path,
              content: new TextEncoder().encode(content as string)
          });
      }
      return results;
    }
"""

content = re.sub(r'    if \(options\.ecosystem === "cdd-java"\) \{.*?(?=    if \(options\.ecosystem === "cdd-php"\) \{)', java_block + '\n', content, flags=re.DOTALL)

with open(file_path, 'w') as f:
    f.write(content)
print("Patched cdd-ctl-wasm-sdk/src/index.ts successfully.")
