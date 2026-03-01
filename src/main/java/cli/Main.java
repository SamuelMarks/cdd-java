package cli;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import openapi.OpenAPI;

/**
 * CLI Entrypoint.
 */
public class Main {
    /** Default constructor. */
    public Main() {}

    /**
     * Entrypoint.
     * @param args arguments
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            printHelp();
            return;
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            System.out.println("cdd-java 1.0.0");
            return;
        }

        String command = args[0];

        if (command.equals("from_openapi")) {
            String inputFile = getArg(args, "-i");
            if (inputFile == null) {
                System.err.println("Missing -i <spec.json>");
                throw new Exception("Exit 1");
            }
            OpenAPI api = openapi.Parse.fromFile(new File(inputFile));
            
            if (api.info != null) {
                System.out.println("// Generated code for " + api.info.title + "\n");
            } else {
                System.out.println("// Generated code\n");
            }
            
            System.out.println(classes.Emit.emit(api, null));
            System.out.println(routes.Emit.emit(api, null));
            System.out.println(mocks.Emit.emit(api, null));
            System.out.println(tests.Emit.emit(api, null));
            System.out.println(functions.Emit.emit(api, null));
            
        } else if (command.equals("to_openapi")) {
            String filePath = getArg(args, "-f");
            if (filePath == null) {
                System.err.println("Missing -f <path/to/code>");
                throw new Exception("Exit 1");
            }
            OpenAPI fullApi = extractOpenAPI(new File(filePath));
            System.out.println(openapi.Emit.toString(fullApi));
            
        } else if (command.equals("sync")) {
            String dirPath = getArg(args, "-d");
            if (dirPath == null) {
                System.err.println("Missing -d <dir>");
                throw new Exception("Exit 1");
            }
            
            File dir = new File(dirPath);
            OpenAPI fullApi = extractOpenAPI(dir);
            
            // Re-emit using lexical preservation
            List<File> javaFiles = new ArrayList<>();
            findJavaFiles(dir, javaFiles);
            
            for (File jf : javaFiles) {
                String source = new String(Files.readAllBytes(jf.toPath()));
                
                String newSource = source;
                if (jf.getAbsolutePath().contains("/classes/")) {
                     newSource = classes.Emit.emit(fullApi, source);
                } else if (jf.getAbsolutePath().contains("/routes/")) {
                     newSource = routes.Emit.emit(fullApi, source);
                } else if (jf.getAbsolutePath().contains("/mocks/")) {
                     newSource = mocks.Emit.emit(fullApi, source);
                } else if (jf.getAbsolutePath().contains("/tests/")) {
                     newSource = tests.Emit.emit(fullApi, source);
                } else if (jf.getAbsolutePath().contains("/functions/")) {
                     newSource = functions.Emit.emit(fullApi, source);
                }
                
                if (!newSource.equals(source)) {
                    Files.write(jf.toPath(), newSource.getBytes());
                    System.out.println("Updated: " + jf.getAbsolutePath());
                }
            }
            System.out.println("Sync complete. All files updated to match the consolidated OpenAPI model.");
        } else if (command.equals("to_docs_json")) {
            String inputFile = getArg(args, "-i");
            boolean noImports = hasFlag(args, "--no-imports");
            boolean noWrapping = hasFlag(args, "--no-wrapping");
            
            if (inputFile == null) {
                System.err.println("Missing -i <spec.json>");
                throw new Exception("Exit 1");
            }
            OpenAPI api = openapi.Parse.fromFile(new File(inputFile));
            
            System.out.println(docstrings.Emit.emitDocsJson(api, noImports, noWrapping));
        } else {
            System.err.println("Unknown command: " + command);
            printHelp();
            throw new Exception("Exit 1");
        }
    }

    /**
     * Generated JavaDoc.
     * @param dir param doc
     * @throws java.lang.Exception exception doc
     * @return return doc
     */
    private static OpenAPI extractOpenAPI(File dir) throws Exception {
        List<File> javaFiles = new ArrayList<>();
        findJavaFiles(dir, javaFiles);
        
        OpenAPI fullApi = new OpenAPI();
        fullApi.openapi = "3.2.0";
        fullApi.info = new openapi.Info();
        fullApi.info.title = "Extracted API";
        fullApi.info.version = "1.0.0";
        fullApi.paths = new openapi.Paths();
        fullApi.paths.pathItems = new java.util.HashMap<>();
        fullApi.components = new openapi.Components();
        fullApi.components.schemas = new java.util.HashMap<>();
        
        for (File jf : javaFiles) {
            String source = new String(Files.readAllBytes(jf.toPath()));
            
            OpenAPI apiPaths = routes.Parse.parse(source);
            if (apiPaths.paths != null && apiPaths.paths.pathItems != null) fullApi.paths.pathItems.putAll(apiPaths.paths.pathItems);
            
            OpenAPI mockPaths = mocks.Parse.parse(source);
            if (mockPaths.paths != null && mockPaths.paths.pathItems != null) {
                for (java.util.Map.Entry<String, openapi.PathItem> entry : mockPaths.paths.pathItems.entrySet()) {
                    fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
            
            OpenAPI apiClasses = classes.Parse.parse(source);
            if (apiClasses.components != null && apiClasses.components.schemas != null) {
                fullApi.components.schemas.putAll(apiClasses.components.schemas);
            }
            
            OpenAPI testsPaths = tests.Parse.parse(source);
            if (testsPaths.paths != null && testsPaths.paths.pathItems != null) {
                for (java.util.Map.Entry<String, openapi.PathItem> entry : testsPaths.paths.pathItems.entrySet()) {
                    fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
        return fullApi;
    }

    /**
     * Generated JavaDoc.
     * @param args param doc
     * @param flag param doc
     * @return return doc
     */
    private static String getArg(String[] args, String flag) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(flag)) {
                return args[i+1];
            }
        }
        return null;
    }
    
    /**
     * Generated JavaDoc.
     * @param args param doc
     * @param flag param doc
     * @return return doc
     */
    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        return false;
    }

    /**
     * Generated JavaDoc.
     * @param dir param doc
     * @param result param doc
     */
    private static void findJavaFiles(File dir, List<File> result) {
        if (dir.isFile() && dir.getName().endsWith(".java")) {
            result.add(dir);
        } else if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) findJavaFiles(f, result);
            }
        }
    }

    /**
     * Generated JavaDoc.
     */
    private static void printHelp() {
        System.out.println("cdd-java CLI");
        System.out.println("Usage:");
        System.out.println("  cdd-java --help");
        System.out.println("  cdd-java --version");
        System.out.println("  cdd-java sync -d <dir>");
        System.out.println("  cdd-java from_openapi -i <spec.json>");
        System.out.println("  cdd-java to_openapi -f <path/to/code>");
        System.out.println("  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json>");
    }
}
