package cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import openapi.OpenAPI;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

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
            System.out.println("0.0.1");
            return;
        }

        String command = args[0];

        if (command.equals("from_openapi")) {
            String subCommand = "to_sdk"; // Default for backward compatibility
            int subCmdIdx = 1;
            if (args.length > 1 && !args[1].startsWith("-")) {
                subCommand = args[1];
                subCmdIdx = 2;
            }

            if (!subCommand.equals("to_sdk_cli") && !subCommand.equals("to_sdk") && !subCommand.equals("to_server")) {
                System.err.println("Unknown from_openapi subcommand: " + subCommand);
                throw new Exception("Exit 1");
            }

            String inputFile = getArg(args, "-i", "CDD_INPUT_FILE");
            String inputDir = getArg(args, "--input-dir", "CDD_INPUT_DIR");
            String outputDir = getArg(args, "-o", "CDD_OUTPUT_DIR");
            
            if (outputDir == null) {
                outputDir = System.getProperty("user.dir");
            }

            if (inputFile == null && inputDir == null) {
                System.err.println("Missing -i <spec.json> or --input-dir <dir>");
                throw new Exception("Exit 1");
            }

            boolean noGithubActions = hasFlag(args, "--no-github-actions", "CDD_NO_GITHUB_ACTIONS");
            boolean noInstallablePackage = hasFlag(args, "--no-installable-package", "CDD_NO_INSTALLABLE_PACKAGE");

            List<File> specFiles = new ArrayList<>();
            if (inputFile != null) {
                specFiles.add(new File(inputFile));
            } else if (inputDir != null) {
                File dir = new File(inputDir);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".json") || name.endsWith(".yaml") || name.endsWith(".yml"));
                    if (files != null) {
                        for (File f : files) specFiles.add(f);
                    }
                }
            }

            for (File specFile : specFiles) {
                OpenAPI api = openapi.Parse.fromFile(specFile);
                File outDir = new File(outputDir);
                outDir.mkdirs();

                if (!noInstallablePackage) {
                    generateScaffolding(outDir);
                }
                if (!noGithubActions) {
                    generateGithubActions(outDir);
                }

                if (subCommand.equals("to_sdk_cli")) {
                    String code = cli.Emit.emitCli(api);
                    Files.write(new File(outDir, "SdkCli.java").toPath(), code.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Generated SDK CLI in " + outDir.getAbsolutePath());
                } else if (subCommand.equals("to_sdk")) {
                    String code = classes.Emit.emit(api, null);
                    Files.write(new File(outDir, "Sdk.java").toPath(), code.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Generated SDK in " + outDir.getAbsolutePath());
                } else if (subCommand.equals("to_server")) {
                    String code = routes.Emit.emit(api, null);
                    Files.write(new File(outDir, "ServerRoutes.java").toPath(), code.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Generated Server Routes in " + outDir.getAbsolutePath());
                }
            }

        } else if (command.equals("to_openapi")) {
            String filePath = getArg(args, "-f", "CDD_FILE_PATH");
            String outputFile = getArg(args, "-o", "CDD_OUTPUT_FILE");
            if (filePath == null) {
                System.err.println("Missing -f <path/to/code>");
                throw new Exception("Exit 1");
            }
            if (outputFile == null) {
                outputFile = "spec.json";
            }
            OpenAPI fullApi = extractOpenAPI(new File(filePath));
            String spec = openapi.Emit.toString(fullApi);
            Files.write(new File(outputFile).toPath(), spec.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Emitted OpenAPI to " + outputFile);

        } else if (command.equals("to_docs_json")) {
            String inputFile = getArg(args, "-i", "CDD_INPUT_FILE");
            String outputFile = getArg(args, "-o", "CDD_OUTPUT_FILE");
            boolean noImports = hasFlag(args, "--no-imports", "CDD_NO_IMPORTS");
            boolean noWrapping = hasFlag(args, "--no-wrapping", "CDD_NO_WRAPPING");
            
            if (inputFile == null) {
                System.err.println("Missing -i <spec.json>");
                throw new Exception("Exit 1");
            }
            if (outputFile == null) {
                outputFile = "docs.json";
            }
            OpenAPI api = openapi.Parse.fromFile(new File(inputFile));
            
            String docsJson = docstrings.Emit.emitDocsJson(api, noImports, noWrapping);
            Files.write(new File(outputFile).toPath(), docsJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Emitted docs JSON to " + outputFile);
            
        } else if (command.equals("serve_json_rpc")) {
            String portStr = getArg(args, "--port", "CDD_PORT");
            String listenStr = getArg(args, "--listen", "CDD_LISTEN");
            int port = portStr != null ? Integer.parseInt(portStr) : 8080;
            String listen = listenStr != null ? listenStr : "0.0.0.0";
            startJsonRpcServer(listen, port);
        } else if (command.equals("sync")) {
            // Unchanged for now, keeping compatibility
            String dirPath = getArg(args, "-d", "CDD_DIR");
            if (dirPath == null) {
                System.err.println("Missing -d <dir>");
                throw new Exception("Exit 1");
            }
            File dir = new File(dirPath);
            OpenAPI fullApi = extractOpenAPI(dir);
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
                } else if (jf.getAbsolutePath().contains("/cli/")) {
                     newSource = cli.Emit.emitCli(fullApi);
                     newSource = functions.Emit.emit(fullApi, source);
                }
                if (!newSource.equals(source)) {
                    Files.write(jf.toPath(), newSource.getBytes());
                    System.out.println("Updated: " + jf.getAbsolutePath());
                }
            }
            System.out.println("Sync complete.");
        } else {
            System.err.println("Unknown command: " + command);
            printHelp();
            throw new Exception("Exit 1");
        }
    }

    private static void generateScaffolding(File dir) throws IOException {
        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>generated-sdk</artifactId>\n" +
                "    <version>0.0.1</version>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>11</maven.compiler.source>\n" +
                "        <maven.compiler.target>11</maven.compiler.target>\n" +
                "    </properties>\n" +
                "</project>";
        Files.write(new File(dir, "pom.xml").toPath(), pom.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void generateGithubActions(File dir) throws IOException {
        File ghDir = new File(dir, ".github/workflows");
        ghDir.mkdirs();
        String ci = "name: CI\non: [push, pull_request]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n    - uses: actions/checkout@v3\n    - name: Set up JDK\n      uses: actions/setup-java@v3\n      with:\n        java-version: '11'\n        distribution: 'temurin'\n    - name: Build with Maven\n      run: mvn clean install\n";
        Files.write(new File(ghDir, "ci.yml").toPath(), ci.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void startJsonRpcServer(String listen, int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(listen, port), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    String reqBody = new String(exchange.getRequestBody().readAllBytes());
                    String response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":null}";
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(reqBody);
                        if (node.has("method") && "version".equals(node.get("method").asText())) {
                            JsonNode idNode = node.get("id");
                            String idVal = idNode != null && !idNode.isNull() ? idNode.toString() : "null";
                            response = "{\"jsonrpc\":\"2.0\",\"result\":\"0.0.1\",\"id\":" + idVal + "}";
                        }
                    } catch (Exception e) {
                        response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32700,\"message\":\"Parse error\"},\"id\":null}";
                    }
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] respBytes = response.getBytes();
                    exchange.sendResponseHeaders(200, respBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(respBytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("JSON-RPC server started at http://" + listen + ":" + port);
        // keep it running
        Thread.currentThread().join();
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
        fullApi.info.version = "0.0.1";
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
            OpenAPI cliPaths = cli.Parse.parse(source);
            if (cliPaths.paths != null && cliPaths.paths.pathItems != null) {
                for (java.util.Map.Entry<String, openapi.PathItem> entry : cliPaths.paths.pathItems.entrySet()) {
                    fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
            if (testsPaths.paths != null && testsPaths.paths.pathItems != null) {
                for (java.util.Map.Entry<String, openapi.PathItem> entry : testsPaths.paths.pathItems.entrySet()) {
                    fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
        return fullApi;
    }

    private static String getArg(String[] args, String flag, String envVar) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(flag)) {
                return args[i+1];
            }
        }
        String env = System.getenv(envVar);
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        return null;
    }
    
    private static boolean hasFlag(String[] args, String flag, String envVar) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        String env = System.getenv(envVar);
        return env != null && (env.equalsIgnoreCase("true") || env.equals("1"));
    }

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

    private static void printHelp() {
        System.out.println("cdd-java CLI");
        System.out.println("Usage:");
        System.out.println("  cdd-java --help");
        System.out.println("  cdd-java --version");
        System.out.println("  cdd-java serve_json_rpc [--port <port>] [--listen <ip>]");
        System.out.println("  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package]");
        System.out.println("  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>]");
        System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
        System.out.println("  cdd-java to_openapi -f <path/to/code> [-o <spec.json>]");
        System.out.println("  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
    }
}