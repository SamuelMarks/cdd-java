package cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Map;
import java.util.HashMap;

import openapi.OpenAPI;

/**
 * CLI Entrypoint.
 */
public class Main {

	/** Default constructor. */
	public Main() {
	}

	private static File resolveFile(String path) {
		String vfsRoot = System.getenv("CDD_WASI_VIRTUAL_ROOT");
		if (vfsRoot != null && path.startsWith("/")) {
			return new File(vfsRoot, path.substring(1));
		}
		return new File(path);
	}

	/**
	 * Entrypoint.
	 * 
	 * @param args
	 *            arguments
	 * @throws Exception
	 *             on error
	 */
	public static void _start(String[] args) throws Exception {
		main(args);
	}

	/**
	 * Start.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */

	public static void _start() throws Exception {
		main(new String[0]);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            The command line arguments.
	 * @throws Exception
	 *             if an error occurs
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
		boolean wasi = hasFlag(args, "--wasi", "CDD_WASI");

		if (command.equals("process_in_memory")) {
			if (args.length < 2) {
				System.err.println("Missing JSON payload argument");
				throw new Exception("Exit 1");
			}
			processInMemory(args[1]);
			return;
		}

		if (command.equals("from_openapi")) {
			if (hasFlag(args, "--help", null) || hasFlag(args, "-h", null)) {
				System.out.println("cdd-java from_openapi");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println(
						"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
				System.out.println("  cdd-java from_openapi to_orm -i <spec.json> [-o <target_directory>]");
				return;
			}

			String subCommand = "to_sdk"; // Default for backward compatibility
			int subCmdIdx = 1;
			if (args.length > 1 && !args[1].startsWith("-")) {
				subCommand = args[1];
				subCmdIdx = 2;
			}

			if (!subCommand.equals("to_sdk_cli") && !subCommand.equals("to_sdk") && !subCommand.equals("to_server")
					&& !subCommand.equals("to_orm")) {
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
			boolean generateTests = hasFlag(args, "--tests", "CDD_TESTS");

			List<File> specFiles = new ArrayList<>();
			if (inputFile != null) {
				specFiles.add(resolveFile(inputFile));
			} else if (inputDir != null) {
				File dir = resolveFile(inputDir);
				if (dir.exists() && dir.isDirectory()) {
					File[] files = dir.listFiles(
							(d, name) -> name.endsWith(".json") || name.endsWith(".yaml") || name.endsWith(".yml"));
					if (files != null) {
						for (File f : files)
							specFiles.add(f);
					}
				}
			}

			for (File specFile : specFiles) {
				OpenAPI api = openapi.Parse.fromFile(specFile);
				File outDir = resolveFile(outputDir);
				outDir.mkdirs();

				if (!noInstallablePackage) {
					generateScaffolding(outDir);
				}
				if (!noGithubActions) {
					generateGithubActions(outDir);
				}

				if (subCommand.equals("to_sdk_cli")) {
					String code = cli.Emit.emitCli(api);
					writeFile(new File(outDir, "SdkCli.java"), code);
					System.out.println("Generated SDK CLI in " + outDir.getAbsolutePath());
					if (generateTests) {
						String testCode = tests.Emit.emit(api, null);
						writeFile(new File(outDir, "SdkCliIntegrationTest.java"), testCode);
						String mockCode = mocks.Emit.emit(api, null);
						writeFile(new File(outDir, "SdkCliMockServer.java"), mockCode);
						System.out.println("Generated Composable Tests & Mocks in " + outDir.getAbsolutePath());
					}
				} else if (subCommand.equals("to_sdk")) {
					String code = classes.Emit.emit(api, null);
					File srcMainJava = new File(outDir, "src/main/java");
					srcMainJava.mkdirs();
					File srcTestJava = new File(outDir, "src/test/java");
					srcTestJava.mkdirs();
					writeFile(new File(srcMainJava, "Sdk.java"), code);
					System.out.println("Generated SDK in " + outDir.getAbsolutePath());
					if (generateTests) {
						String testCode = tests.Emit.emit(api, null);
						String title1 = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title1.isEmpty())
							title1 = "Api";
						writeFile(new File(srcTestJava, title1 + "IntegrationTest.java"), testCode);
						String mockCode = mocks.Emit.emit(api, null);
						String title2 = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title2.isEmpty())
							title2 = "Api";
						writeFile(new File(srcTestJava, title2 + "MockServer.java"), mockCode);
						System.out.println("Generated Composable Tests & Mocks in " + outDir.getAbsolutePath());
					}
				} else if (subCommand.equals("to_server")) {
					String code = routes.Emit.emit(api, null);
					writeFile(new File(outDir, "ServerRoutes.java"), code);
					System.out.println("Generated Server Routes in " + outDir.getAbsolutePath());
				} else if (subCommand.equals("to_orm")) {
					String code = orm.Emit.emit(api, null);
					writeFile(new File(outDir, "OrmEntities.java"), code);
					System.out.println("Generated ORM Entities in " + outDir.getAbsolutePath());
				}
			}

		} else if (command.equals("to_openapi")) {
			if (hasFlag(args, "--help", null) || hasFlag(args, "-h", null)) {
				System.out.println("cdd-java to_openapi");
				System.out.println("Usage:");
				System.out.println("  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]");
				return;
			}

			String filePath = getArg(args, "-i", "CDD_INPUT_FILE");
			String outputFile = getArg(args, "-o", "CDD_OUTPUT_FILE");
			if (filePath == null) {
				System.err.println("Missing -i <path/to/code>");
				throw new Exception("Exit 1");
			}
			if (outputFile == null) {
				outputFile = "spec.json";
			}
			OpenAPI fullApi = extractOpenAPI(resolveFile(filePath));
			String spec = openapi.Emit.toString(fullApi);
			writeFile(resolveFile(outputFile), spec);
			System.out.println("Emitted OpenAPI to " + outputFile);

		} else if (command.equals("to_docs_json")) {
			if (hasFlag(args, "--help", null) || hasFlag(args, "-h", null)) {
				System.out.println("cdd-java to_docs_json");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
				return;
			}

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
			OpenAPI api = openapi.Parse.fromFile(resolveFile(inputFile));

			String docsJson = docstrings.Emit.emitDocsJson(api, noImports, noWrapping);
			writeFile(resolveFile(outputFile), docsJson);
			System.out.println("Emitted docs JSON to " + outputFile);

		} else if (command.equals("serve_json_rpc")) {
			if (hasFlag(args, "--help", null) || hasFlag(args, "-h", null)) {
				System.out.println("cdd-java serve_json_rpc");
				System.out.println("Usage:");
				System.out.println("  cdd-java serve_json_rpc [--wasi]");
				return;
			}
			startStdioJsonRpcServer();
		} else if (command.equals("sync")) {
			if (hasFlag(args, "--help", null) || hasFlag(args, "-h", null)) {
				System.out.println("cdd-java sync");
				System.out.println("Usage:");
				System.out.println("  cdd-java sync -d <dir>");
				return;
			}
			String dirPath = getArg(args, "-d", "CDD_DIR");
			if (dirPath == null) {
				System.err.println("Missing -d <dir>");
				throw new Exception("Exit 1");
			}
			File dir = resolveFile(dirPath);
			OpenAPI fullApi = extractOpenAPI(dir);
			List<File> javaFiles = new ArrayList<>();
			findJavaFiles(dir, javaFiles);
			for (File jf : javaFiles) {
				String source = readFile(jf);
				String newSource = source;
				String absPath = jf.getAbsolutePath().replace('\\', '/');
				if (absPath.contains("/classes/")) {
					newSource = classes.Emit.emit(fullApi, source);
				} else if (absPath.contains("/orm/")) {
					newSource = orm.Emit.emit(fullApi, source);
				} else if (absPath.contains("/routes/")) {
					newSource = routes.Emit.emit(fullApi, source);
				} else if (absPath.contains("/mocks/")) {
					newSource = mocks.Emit.emit(fullApi, source);
				} else if (absPath.contains("/tests/")) {
					newSource = tests.Emit.emit(fullApi, source);
				} else if (absPath.contains("/functions/")) {
				} else if (absPath.contains("/cli/")) {
					newSource = cli.Emit.emitCli(fullApi);
					newSource = functions.Emit.emit(fullApi, source);
				}
				if (!newSource.equals(source)) {
					writeFile(jf, newSource);
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

	/**
	 * Processes payload in memory.
	 * 
	 * @param payload
	 *            json payload
	 */
	public static void processInMemory(String payload) {
		try {
			JSONObject req = new JSONObject(payload);
			JSONArray cmdArr = req.getJSONArray("command");
			String[] cmdArgs = new String[cmdArr.length()];
			for (int i = 0; i < cmdArr.length(); i++) {
				cmdArgs[i] = cmdArr.getString(i);
			}

			JSONObject inFiles = req.has("files") ? req.getJSONObject("files") : new JSONObject();
			JSONObject outFiles = new JSONObject();

			String command = cmdArgs[0];

			if (command.equals("from_openapi")) {
				String subCommand = "to_sdk";
				if (cmdArgs.length > 1 && !cmdArgs[1].startsWith("-")) {
					subCommand = cmdArgs[1];
				}

				boolean noGithubActions = hasFlag(cmdArgs, "--no-github-actions", null);
				boolean noInstallablePackage = hasFlag(cmdArgs, "--no-installable-package", null);
				boolean generateTests = hasFlag(cmdArgs, "--tests", null);

				String specContent = inFiles.optString("spec.json", null);
				if (specContent == null || specContent.isEmpty()) {
					throw new Exception("Missing spec.json in files");
				}

				OpenAPI api = openapi.Parse.fromString(specContent);

				if (!noInstallablePackage) {
					outFiles.put("pom.xml", getScaffoldingPom());
				}
				if (!noGithubActions) {
					outFiles.put(".github/workflows/ci.yml", getGithubActionsCi());
				}

				if (subCommand.equals("to_sdk_cli")) {
					outFiles.put("SdkCli.java", cli.Emit.emitCli(api));
					if (generateTests) {
						outFiles.put("SdkCliIntegrationTest.java", tests.Emit.emit(api, null));
						outFiles.put("SdkCliMockServer.java", mocks.Emit.emit(api, null));
					}
				} else if (subCommand.equals("to_sdk")) {
					outFiles.put("src/main/java/Sdk.java", classes.Emit.emit(api, null));
					if (generateTests) {
						String title3 = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title3.isEmpty())
							title3 = "Api";
						outFiles.put("src/test/java/" + title3 + "IntegrationTest.java", tests.Emit.emit(api, null));
						String title4 = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title4.isEmpty())
							title4 = "Api";
						outFiles.put("src/test/java/" + title4 + "MockServer.java", mocks.Emit.emit(api, null));
					}
				} else if (subCommand.equals("to_server")) {
					outFiles.put("ServerRoutes.java", routes.Emit.emit(api, null));
				} else if (subCommand.equals("to_orm")) {
					outFiles.put("OrmEntities.java", orm.Emit.emit(api, null));
				}

			} else if (command.equals("to_docs_json")) {
				boolean noImports = hasFlag(cmdArgs, "--no-imports", null);
				boolean noWrapping = hasFlag(cmdArgs, "--no-wrapping", null);
				String specContent = inFiles.optString("spec.json", null);
				if (specContent == null || specContent.isEmpty()) {
					throw new Exception("Missing spec.json in files");
				}
				OpenAPI api = openapi.Parse.fromString(specContent);
				String docsJson = docstrings.Emit.emitDocsJson(api, noImports, noWrapping);
				outFiles.put("docs.json", docsJson);
			} else {
				throw new Exception("Unsupported in-memory command: " + command);
			}

			JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("files", outFiles);
			System.out.println("CDD_IN_MEMORY_START");
			System.out.println(result.toString());
			System.out.println("CDD_IN_MEMORY_END");
		} catch (Exception e) {
			JSONObject err = new JSONObject();
			err.put("success", false);
			err.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
			System.out.println("CDD_IN_MEMORY_START");
			System.out.println(err.toString());
			System.out.println("CDD_IN_MEMORY_END");
		}
	}

	private static void writeFile(File file, String content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes("UTF-8"));
		}
	}

	private static String readFile(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			return new String(data, "UTF-8");
		}
	}

	private static String getScaffoldingPom() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
				+ "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + "    <modelVersion>4.0.0</modelVersion>\n"
				+ "    <groupId>com.example</groupId>\n" + "    <artifactId>generated-sdk</artifactId>\n"
				+ "    <version>0.0.1</version>\n" + "    <properties>\n"
				+ "        <maven.compiler.source>11</maven.compiler.source>\n"
				+ "        <maven.compiler.target>11</maven.compiler.target>\n" + "    </properties>\n"
				+ "    <dependencies>\n" + "        <dependency>\n" + "            <groupId>io.javalin</groupId>\n"
				+ "            <artifactId>javalin</artifactId>\n" + "            <version>5.6.3</version>\n"
				+ "        </dependency>\n" + "        <dependency>\n"
				+ "            <groupId>org.hibernate.orm</groupId>\n"
				+ "            <artifactId>hibernate-core</artifactId>\n"
				+ "            <version>6.4.4.Final</version>\n" + "        </dependency>\n" + "        <dependency>\n"
				+ "            <groupId>org.postgresql</groupId>\n"
				+ "            <artifactId>postgresql</artifactId>\n" + "            <version>42.7.2</version>\n"
				+ "        </dependency>\n" + "        <dependency>\n"
				+ "            <groupId>com.fasterxml.jackson.core</groupId>\n"
				+ "            <artifactId>jackson-databind</artifactId>\n" + "            <version>2.15.2</version>\n"
				+ "        </dependency>\n" + "        <dependency>\n" + "            <groupId>junit</groupId>\n"
				+ "            <artifactId>junit</artifactId>\n" + "            <version>4.13.2</version>\n"
				+ "            <scope>test</scope>\n" + "        </dependency>\n" + "    </dependencies>\n"
				+ "</project>";
	}

	private static void generateScaffolding(File dir) throws IOException {
		writeFile(new File(dir, "pom.xml"), getScaffoldingPom());
	}

	private static String getGithubActionsCi() {
		return "name: CI\non: [push, pull_request]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n    - uses: actions/checkout@v3\n    - name: Set up JDK\n      uses: actions/setup-java@v3\n      with:\n        java-version: '11'\n        distribution: 'temurin'\n    - name: Build with Maven\n      run: mvn clean install\n";
	}

	private static void generateGithubActions(File dir) throws IOException {
		File ghDir = new File(dir, ".github/workflows");
		ghDir.mkdirs();
		writeFile(new File(ghDir, "ci.yml"), getGithubActionsCi());
	}

	private static void startStdioJsonRpcServer() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;

			String response;
			try {
				JSONObject req = new JSONObject(line);
				Object idObj = req.has("id") && !req.isNull("id") ? req.get("id") : null;
				String idStr = idObj != null ? idObj.toString() : "null";

				if (req.has("jsonrpc") && "2.0".equals(req.getString("jsonrpc"))) {
					String method = req.has("method") ? req.getString("method") : "";
					if ("version".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":\"0.0.1\",\"id\":" + idStr + "}";
					} else {
						response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":"
								+ idStr + "}";
					}
				} else {
					response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32600,\"message\":\"Invalid Request\"},\"id\":null}";
				}
			} catch (Exception e) {
				response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32700,\"message\":\"Parse error\"},\"id\":null}";
			}

			System.out.println(response);
			System.out.flush();
		}
	}

	/**
	 * Generated JavaDoc.
	 * 
	 * @param dir
	 *            param doc
	 * @throws java.lang.Exception
	 *             exception doc
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
			String source = readFile(jf);

			OpenAPI apiPaths = routes.Parse.parse(source);
			if (apiPaths.paths != null && apiPaths.paths.pathItems != null)
				fullApi.paths.pathItems.putAll(apiPaths.paths.pathItems);

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

			OpenAPI ormClasses = orm.Parse.parse(source);
			if (ormClasses.components != null && ormClasses.components.schemas != null) {
				fullApi.components.schemas.putAll(ormClasses.components.schemas);
			}

			OpenAPI testsPaths = tests.Parse.parse(source);
			OpenAPI cliPaths = cli.Parse.parse(source);

			if (cliPaths.components != null && cliPaths.components.schemas != null) {
				fullApi.components.schemas.putAll(cliPaths.components.schemas);
			}
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
				return args[i + 1];
			}
		}
		if (envVar == null || envVar.isEmpty())
			return null;
		String env = System.getenv(envVar);
		if (env != null && !env.trim().isEmpty()) {
			return env;
		}
		return null;
	}

	private static boolean hasFlag(String[] args, String flag, String envVar) {
		for (String arg : args) {
			if (arg.equals(flag))
				return true;
		}
		if (envVar == null || envVar.isEmpty())
			return false;
		String env = System.getenv(envVar);
		return env != null && (env.equalsIgnoreCase("true") || env.equals("1"));
	}

	private static void findJavaFiles(File dir, List<File> result) {
		if (dir.isFile() && dir.getName().endsWith(".java")) {
			result.add(dir);
		} else if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files)
					findJavaFiles(f, result);
			}
		}
	}

	private static void printHelp() {
		System.out.println("cdd-java CLI");
		System.out.println("Usage:");
		System.out.println("  cdd-java --help");
		System.out.println("  cdd-java --version");
		System.out.println("  cdd-java serve_json_rpc [--wasi]");
		System.out.println(
				"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println(
				"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
		System.out.println("  cdd-java from_openapi to_orm -i <spec.json> [-o <target_directory>]");
		System.out.println("  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]");
		System.out.println("  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
		System.out.println("  cdd-java sync -d <dir>");
	}
}
