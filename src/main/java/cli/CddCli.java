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
public @Generated class CddCli {

	/**
	 * Default constructor.
	 */
	public CddCli() {
	}

	/**
	 * resolveFile doc
	 */
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
		run(args);
	}

	/**
	 * Start.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	/**
	 * Method.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	public static void _start() throws Exception {
		run(new String[0]);
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *            The command line arguments.
	 * @throws Exception
	 *             if an error occurs
	 */
	/**
	 * Generates code from an OpenAPI specification.
	 *
	 * @param subCommand
	 *            Subcommand (e.g. to_sdk)
	 * @param input
	 *            Input file
	 * @param outputDir
	 *            Output directory
	 * @param noGithubActions
	 *            Disable Github Actions
	 * @param noInstallablePackage
	 *            Disable installable package
	 * @param generateTests
	 *            Generate tests
	 * @return Exit code (0 for success).
	 */
	public static int generateFromOpenApi(String subCommand, String input, String outputDir, boolean noGithubActions,
			boolean noInstallablePackage, boolean generateTests) {
		List<String> argsList = new ArrayList<>();
		argsList.add("from_openapi");
		if (subCommand != null && !subCommand.isEmpty())
			argsList.add(subCommand);
		if (input != null) {
			argsList.add("-i");
			argsList.add(input);
		}
		if (outputDir != null) {
			argsList.add("-o");
			argsList.add(outputDir);
		}
		if (noGithubActions)
			argsList.add("--no-github-actions");
		if (noInstallablePackage)
			argsList.add("--no-installable-package");
		if (generateTests)
			argsList.add("--tests");
		try {
			return run(argsList.toArray(new String[0]));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		}
	}

	/**
	 * Generates an OpenAPI specification from source code.
	 *
	 * @param input
	 *            Input code path
	 * @param output
	 *            Output spec path
	 * @return Exit code (0 for success).
	 */
	public static int generateToOpenApi(String input, String output) {
		List<String> argsList = new ArrayList<>();
		argsList.add("to_openapi");
		if (input != null) {
			argsList.add("-i");
			argsList.add(input);
		}
		if (output != null) {
			argsList.add("-o");
			argsList.add(output);
		}
		try {
			return run(argsList.toArray(new String[0]));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		}
	}

	/**
	 * Generates JSON documentation with code snippets for an OpenAPI specification.
	 *
	 * @param input
	 *            Input spec path
	 * @param output
	 *            Output docs path
	 * @param noImports
	 *            Disable imports
	 * @param noWrapping
	 *            Disable wrapping
	 * @return Exit code (0 for success).
	 */
	public static int generateDocsJson(String input, String output, boolean noImports, boolean noWrapping) {
		List<String> argsList = new ArrayList<>();
		argsList.add("to_docs_json");
		if (input != null) {
			argsList.add("-i");
			argsList.add(input);
		}
		if (output != null) {
			argsList.add("-o");
			argsList.add(output);
		}
		if (noImports)
			argsList.add("--no-imports");
		if (noWrapping)
			argsList.add("--no-wrapping");
		try {
			return run(argsList.toArray(new String[0]));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		}
	}

	/**
	 * Exposes CLI interface as a JSON-RPC server.
	 *
	 * @param port
	 *            Port
	 * @param listen
	 *            Listen address
	 * @param wasi
	 *            WASI flag
	 * @return Exit code (0 for success).
	 */
	public static int serveJsonRpc(String port, String listen, boolean wasi) {
		List<String> argsList = new ArrayList<>();
		argsList.add("serve_json_rpc");
		if (port != null) {
			argsList.add("-p");
			argsList.add(port);
		}
		if (listen != null) {
			argsList.add("-l");
			argsList.add(listen);
		}
		if (wasi)
			argsList.add("--wasi");
		try {
			return run(argsList.toArray(new String[0]));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		}
	}

	/**
	 * Synchronize database schema to models and OpenAPI specifications.
	 *
	 * @param inputDir
	 *            Input directory
	 * @param truth
	 *            Truth type
	 * @return Exit code (0 for success).
	 */
	public static int sync(String inputDir, String outputDir, String truth) {
		List<String> argsList = new ArrayList<>();
		argsList.add("sync");
		if (inputDir != null) {
			argsList.add("-i");
			argsList.add(inputDir);
		}
		if (outputDir != null) {
			argsList.add("-o");
			argsList.add(outputDir);
		}
		if (truth != null) {
			argsList.add("--truth");
			argsList.add(truth);
		}
		try {
			return run(argsList.toArray(new String[0]));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		}
	}

	/**
	 * Start MCP server.
	 *
	 * @param port
	 *            Port
	 * @param listen
	 *            Listen address
	 * @param wasi
	 *            WASI flag
	 * @return Exit code (0 for success).
	 */
	public static int mcp(String port, String listen, boolean wasi) {
		return serveJsonRpc(port, listen, wasi);
	}

	/**
	 * run doc
	 *
	 * @param args
	 *            Command-line arguments.
	 * @return Exit code (0 for success).
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static int run(String[] args) throws Exception {
		if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
			printHelp();
			return 0;
		}
		if (args[0].equals("--version") || args[0].equals("-v")) {
			System.out.println("0.0.3");
			return 0;
		}
		String command = args[0];
		boolean wasi = hasFlag(args, "--wasi", "--wasi", "CDD_WASI");
		if (command.equals("process_in_memory")) {
			if (args.length < 2) {
				System.err.println("Missing JSON payload argument");
				throw new Exception("Exit 1");
			}
			processInMemory(args[1]);
			return 0;
		}
		if (command.equals("from_openapi")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java from_openapi");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println(
						"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
				return 0;
			}
			// Default for backward compatibility
			String subCommand = "to_sdk";
			int subCmdIdx = 1;
			if (args.length > 1 && !args[1].startsWith("-")) {
				subCommand = args[1];
				subCmdIdx = 2;
			}
			if (!subCommand.equals("to_sdk_cli") && !subCommand.equals("to_sdk") && !subCommand.equals("to_server")) {
				System.err.println("Unknown from_openapi subcommand: " + subCommand);
				throw new Exception("Exit 1");
			}
			String inputFile = getArg(args, "-i", "--input", "CDD_INPUT");
			String inputDir = getArg(args, "--input-dir", "--input-dir", "CDD_INPUT_DIR");
			if (inputFile == null && inputDir != null) {
				inputFile = inputDir;
			}
			String outputDir = getArg(args, "-o", "--output", "CDD_OUTPUT");
			if (outputDir == null) {
				outputDir = System.getProperty("user.dir");
			}
			if (inputFile == null) {
				System.err.println("Missing -i <spec.json> or --input-dir <dir>");
				throw new Exception("Exit 1");
			}
			boolean noGithubActions = hasFlag(args, "--no-github-actions", "--no-github-actions",
					"CDD_NO_GITHUB_ACTIONS");
			boolean noInstallablePackage = hasFlag(args, "--no-installable-package", "--no-installable-package",
					"CDD_NO_INSTALLABLE_PACKAGE");
			boolean generateTests = hasFlag(args, "--tests", "--tests", "CDD_TESTS");
			List<File> specFiles = new ArrayList<>();
			File targetFile = resolveFile(inputFile);
			if (targetFile.isDirectory()) {
				File[] files = targetFile.listFiles(
						(d, name) -> name.endsWith(".json") || name.endsWith(".yaml") || name.endsWith(".yml"));
				if (files != null) {
					for (File f : files)
						specFiles.add(f);
				}
			} else {
				specFiles.add(targetFile);
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
					java.util.Map<String, String> modelsCode = classes.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : modelsCode.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> ormCode = orm.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : ormCode.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> daoCode = dao.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : daoCode.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> sRoutes = serverroutes.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : sRoutes.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> seederCode = seeder.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : seederCode.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> sMain = servermain.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : sMain.entrySet()) {
						writeFile(new File(outDir, "src/main/java/" + e.getKey()), e.getValue());
					}
					java.util.Map<String, String> sTests = servertests.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : sTests.entrySet()) {
						if (e.getKey().startsWith("../main/")) {
							writeFile(new File(outDir, "src/" + e.getKey().substring(3)), e.getValue());
						} else {
							writeFile(new File(outDir, "src/test/java/" + e.getKey()), e.getValue());
						}
					}
					java.util.Map<String, String> sMocks = mocks.Emit.emitModular(api);
					for (java.util.Map.Entry<String, String> e : sMocks.entrySet()) {
						writeFile(new File(outDir, "src/test/java/" + e.getKey()), e.getValue());
					}
					System.out.println("Generated Modular Server in " + outDir.getAbsolutePath());
				}
			}
		} else if (command.equals("to_openapi")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java to_openapi");
				System.out.println("Usage:");
				System.out.println("  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]");
				return 0;
			}
			String filePath = getArg(args, "-i", "--input", "CDD_INPUT");
			String outputFile = getArg(args, "-o", "--output", "CDD_OUTPUT");
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
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java to_docs_json");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
				return 0;
			}
			String inputFile = getArg(args, "-i", "--input", "CDD_INPUT");
			String outputFile = getArg(args, "-o", "--output", "CDD_OUTPUT");
			boolean noImports = hasFlag(args, "--no-imports", "--no-imports", "CDD_NO_IMPORTS");
			boolean noWrapping = hasFlag(args, "--no-wrapping", "--no-wrapping", "CDD_NO_WRAPPING");
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
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java serve_json_rpc");
				System.out.println("Usage:");
				System.out.println("  cdd-java serve_json_rpc [-p|--port <port>] [-l|--listen <address>] [--wasi]");
				return 0;
			}
			String portStr = getArg(args, "-p", "--port", "CDD_PORT");
			if (portStr == null)
				portStr = "8080";
			String listen = getArg(args, "-l", "--listen", "CDD_LISTEN");
			if (listen == null)
				listen = "127.0.0.1";
			// If --wasi is present, continue with STDIO
			if (wasi) {
				startStdioJsonRpcServer();
			} else {
				// For now, standard server behaves same as WASI due to Java's lack of built-in
				// lightweight HTTP server,
				// but we parse the flags for consistency.
				startStdioJsonRpcServer();
			}
		} else if (command.equals("sync")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java sync");
				System.out.println("Usage:");
				System.out.println("  cdd-java sync -i <dir> [-o <dir>] [--truth <type>]");
				return 0;
			}
			String dirPath = getArg(args, "-i", "--input", "CDD_INPUT");
			if (dirPath == null) {
				System.err.println("Missing -i <dir>");
				throw new Exception("Exit 1");
			}
			String outputDir = getArg(args, "-o", "--output", "CDD_OUTPUT");
			String truth = getArg(args, "--truth", "--truth", "CDD_TRUTH");
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
				File targetFile = jf;
				if (outputDir != null) {
					String relativePath = jf.getAbsolutePath().substring(dir.getAbsolutePath().length());
					if (relativePath.startsWith(File.separator)) {
						relativePath = relativePath.substring(1);
					}
					targetFile = new File(resolveFile(outputDir), relativePath);
				}
				if (!newSource.equals(source) || outputDir != null) {
					writeFile(targetFile, newSource);
					System.out.println("Updated: " + targetFile.getAbsolutePath());
				}
			}
			System.out.println("Sync complete.");
		} else {
			System.err.println("Error: Unknown or incomplete command: " + command);
			printHelp();
			throw new Exception("Exit 1");
		}
		return 0;
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
				boolean noGithubActions = hasFlag(cmdArgs, "--no-github-actions", "--no-github-actions", null);
				boolean noInstallablePackage = hasFlag(cmdArgs, "--no-installable-package", "--no-installable-package",
						null);
				boolean generateTests = hasFlag(cmdArgs, "--tests", "--tests", null);
				String specContent = inFiles.optString("spec.json", null);
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
					for (java.util.Map.Entry<String, String> e : classes.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : orm.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : dao.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : serverroutes.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : seeder.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : servermain.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : servertests.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
					for (java.util.Map.Entry<String, String> e : mocks.Emit.emitModular(api).entrySet()) {
						outFiles.put(e.getKey(), e.getValue());
					}
				}
			} else if (command.equals("to_openapi")) {
				OpenAPI api = extractOpenAPI(new File("."));
				String spec = openapi.Emit.toString(api);
				outFiles.put("spec.json", spec);
			} else if (command.equals("sync")) {
				OpenAPI api = extractOpenAPI(new File("."));
				String spec = openapi.Emit.toString(api);
				outFiles.put("spec.json", spec);
			} else if (command.equals("to_docs_json")) {
				boolean noImports = hasFlag(cmdArgs, "--no-imports", "--no-imports", null);
				boolean noWrapping = hasFlag(cmdArgs, "--no-wrapping", "--no-wrapping", null);
				String specContent = inFiles.optString("spec.json", null);
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

	/**
	 * writeFile doc
	 */
	private static void writeFile(File file, String content) throws IOException {
		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes("UTF-8"));
		}
	}

	/**
	 * readFile doc
	 */
	private static String readFile(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			return new String(data, "UTF-8");
		}
	}

	/**
	 * getScaffoldingPom doc
	 */
	private static String getScaffoldingPom() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
				+ "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + "    <modelVersion>4.0.0</modelVersion>\n"
				+ "    <groupId>com.example</groupId>\n" + "    <artifactId>generated-sdk</artifactId>\n"
				+ "    <version>0.0.3</version>\n" + "    <properties>\n"
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
				+ "        </dependency>\n" + "        <dependency>\n"
				+ "            <groupId>net.datafaker</groupId>\n" + "            <artifactId>datafaker</artifactId>\n"
				+ "            <version>2.0.2</version>\n" + "        </dependency>\n" + "        <dependency>\n"
				+ "            <groupId>junit</groupId>\n" + "            <artifactId>junit</artifactId>\n"
				+ "            <version>4.13.2</version>\n" + "            <scope>test</scope>\n"
				+ "        </dependency>\n" + "    </dependencies>\n" + "</project>";
	}

	/**
	 * generateScaffolding doc
	 */
	private static void generateScaffolding(File dir) throws IOException {
		writeFile(new File(dir, "pom.xml"), getScaffoldingPom());
	}

	/**
	 * getGithubActionsCi doc
	 */
	private static String getGithubActionsCi() {
		return "name: CI\non: [push, pull_request]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n    - uses: actions/checkout@v6\n    - name: Set up JDK\n      uses: actions/setup-java@v3\n      with:\n        java-version: '11'\n        distribution: 'temurin'\n    - name: Build with Maven\n      run: mvn clean install\n";
	}

	/**
	 * generateGithubActions doc
	 */
	private static void generateGithubActions(File dir) throws IOException {
		File ghDir = new File(dir, ".github/workflows");
		ghDir.mkdirs();
		writeFile(new File(ghDir, "ci.yml"), getGithubActionsCi());
	}

	/**
	 * startStdioJsonRpcServer doc
	 */
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
					if ("initialize".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{\"tools\":{},\"resources\":{}},\"serverInfo\":{\"name\":\"cdd-java\",\"version\":\"0.0.3\"}},\"id\":"
								+ idStr + "}";
					} else if ("notifications/initialized".equals(method) || "initialized".equals(method)) {
						continue;
					} else if ("ping".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":" + idStr + "}";
					} else if ("$/cancelRequest".equals(method) || "cancelled".equals(method)) {
						continue;
					} else if ("tools/list".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":{\"tools\":[{\"name\":\"cdd_generate\",\"description\":\"Generate code from OpenAPI or sync code\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"command\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"files\":{\"type\":\"object\"}},\"required\":[\"command\"]}}]},\"id\":"
								+ idStr + "}";
					} else if ("resources/list".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":{\"resources\":[{\"uri\":\"cdd://ast/openapi\",\"name\":\"OpenAPI AST\",\"mimeType\":\"application/json\"}]},\"id\":"
								+ idStr + "}";
					} else if ("resources/read".equals(method)) {
						JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
						String uri = params.has("uri") ? params.getString("uri") : "";
						if ("cdd://ast/openapi".equals(uri)) {
							String astText = "{}";
							try {
								OpenAPI api = extractOpenAPI(new File("."));
								astText = openapi.Emit.toString(api);
							} catch (Exception e) {
								astText = "{}";
							}
							response = "{\"jsonrpc\":\"2.0\",\"result\":{\"contents\":[{\"uri\":\"cdd://ast/openapi\",\"mimeType\":\"application/json\",\"text\":"
									+ JSONObject.quote(astText) + "}]},\"id\":" + idStr + "}";
						} else {
							response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32602,\"message\":\"Resource not found\"},\"id\":"
									+ idStr + "}";
						}
					} else if ("tools/call".equals(method)) {
						JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
						String toolName = params.has("name") ? params.getString("name") : "";
						if ("cdd_generate".equals(toolName)) {
							JSONObject args = params.has("arguments")
									? params.getJSONObject("arguments")
									: new JSONObject();
							java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
							java.io.PrintStream ps = new java.io.PrintStream(baos);
							java.io.PrintStream oldOut = System.out;
							System.setOut(ps);
							try {
								processInMemory(args.toString());
							} finally {
								System.setOut(oldOut);
							}
							String output = baos.toString("UTF-8");
							JSONObject content = new JSONObject();
							content.put("type", "text");
							content.put("text", output);
							JSONArray contentArr = new JSONArray();
							contentArr.put(content);
							JSONObject resultObj = new JSONObject();
							resultObj.put("content", contentArr);
							response = "{\"jsonrpc\":\"2.0\",\"result\":" + resultObj.toString() + ",\"id\":" + idStr
									+ "}";
						} else {
							response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Tool not found\"},\"id\":"
									+ idStr + "}";
						}
					} else if ("version".equals(method)) {
						response = "{\"jsonrpc\":\"2.0\",\"result\":\"0.0.3\",\"id\":" + idStr + "}";
					} else if ("notifications/progress".equals(method) || "progress".equals(method)) {
						continue;
					} else {
						if (idObj == null)
							continue;
						response = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":"
								+ idStr + "}";
					}
				} else {
					if (idObj == null)
						continue;
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
		fullApi.info.version = "0.0.3";
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

	/**
	 * getArg doc
	 */
	private static String getArg(String[] args, String shortFlag, String longFlag, String envVar) {
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(shortFlag) || args[i].equals(longFlag)) {
				return args[i + 1];
			}
		}
		if (envVar != null) {
			String env = System.getenv(envVar);
			return env;
		}
		return null;
	}

	/**
	 * hasFlag doc
	 */
	private static boolean hasFlag(String[] args, String shortFlag, String longFlag, String envVar) {
		for (String arg : args) {
			if (arg.equals(shortFlag) || arg.equals(longFlag))
				return true;
		}
		if (envVar != null) {
			String env = System.getenv(envVar);
			return "true".equalsIgnoreCase(env) || "1".equals(env);
		}
		return false;
	}

	/**
	 * findJavaFiles doc
	 */
	private static void findJavaFiles(File dir, List<File> result) {
		if (dir.isFile() && dir.getName().endsWith(".java")) {
			result.add(dir);
		} else if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			{
				for (File f : files)
					findJavaFiles(f, result);
			}
		}
	}

	/**
	 * printHelp doc
	 */
	private static void printHelp() {
		System.out.println("cdd-java CLI");
		System.out.println("Usage:");
		System.out.println("  cdd-java [subcommand] [options]");
		System.out.println("\nSubcommands:");
		System.out.println("  from_openapi    Generate code from an OpenAPI specification.");
		System.out.println("  to_openapi      Generate an OpenAPI specification from source code.");
		System.out.println(
				"  to_docs_json    Generate JSON documentation with code snippets for an OpenAPI specification.");
		System.out.println("  serve_json_rpc  Expose CLI interface as a JSON-RPC server.");
		System.out.println("  sync            Synchronize database schema to models and OpenAPI specifications.");
		System.out.println("\nOptions:");
		System.out.println("  --help, -h      Show this help message.");
		System.out.println("  --version, -v   Show version information.");
		System.out.println("\nExamples:");
		System.out.println("  cdd-java serve_json_rpc [--port 8080] [--listen 127.0.0.1] [--wasi]");
		System.out.println(
				"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println(
				"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
		System.out.println("  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]");
		System.out.println("  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
		System.out.println("  cdd-java sync -i <dir> [-o <dir>] [--truth <type>]");
	}
}
