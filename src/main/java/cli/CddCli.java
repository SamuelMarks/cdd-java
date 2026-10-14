package cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Map;
import java.util.function.Function;
import openapi.OpenAPI;

/**
 * CLI Entrypoint.
 */
public class CddCli {

	/**
	 * Environment variable lookup function, defaulting to
	 * {@link System#getenv(String)}.
	 */
	public static Function<String, String> envProvider = System::getenv;

	/**
	 * Active HTTP JSON-RPC server instance when running in non-WASI HTTP mode.
	 */
	public static com.sun.net.httpserver.HttpServer activeHttpServer = null;

	/**
	 * Default directory used when scanning the current workspace in memory or via
	 * MCP.
	 */
	public static File workspaceDir = new File("src/main/java");

	/**
	 * Default constructor.
	 */
	public CddCli() {
	}

	/**
	 * Resolves a file path against the WASI virtual root if configured.
	 *
	 * @param path
	 *            The file path to resolve.
	 * @return The resolved {@link File} instance.
	 */
	private static File resolveFile(String path) {
		String vfsRoot = envProvider.apply("CDD_WASI_VIRTUAL_ROOT");
		if (vfsRoot != null && path.startsWith("/")) {
			return new File(vfsRoot, path.substring(1));
		}
		return new File(path);
	}

	/**
	 * WASI and CLI entrypoint accepting arguments.
	 *
	 * @param args
	 *            The command-line arguments.
	 * @throws Exception
	 *             If command execution fails.
	 */
	public static void _start(String[] args) throws Exception {
		run(args);
	}

	/**
	 * Default WASI entrypoint taking no arguments.
	 *
	 * @throws Exception
	 *             If command execution fails.
	 */
	public static void _start() throws Exception {
		run(new String[0]);
	}

	/**
	 * Generates code from an OpenAPI specification.
	 *
	 * @param subCommand
	 *            Subcommand (e.g. to_sdk, to_sdk_cli, to_server).
	 * @param input
	 *            Input OpenAPI specification file or directory path.
	 * @param outputDir
	 *            Target output directory path.
	 * @param noGithubActions
	 *            Whether to disable GitHub Actions workflow generation.
	 * @param noInstallablePackage
	 *            Whether to disable installable package (pom.xml) generation.
	 * @param generateTests
	 *            Whether to generate integration tests and mock servers.
	 * @return Exit code (0 for success, 1 for error).
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
	 * Synchronizes database schema, models, and OpenAPI specifications in a
	 * directory.
	 *
	 * @param inputDir
	 *            Input source directory path.
	 * @param outputDir
	 *            Output directory path (or null to update in place).
	 * @param truth
	 *            Source of truth type.
	 * @return Exit code (0 for success, 1 for error).
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
	 * Starts the Model Context Protocol (MCP) JSON-RPC server.
	 *
	 * @param port
	 *            Port number to listen on.
	 * @param listen
	 *            Host address to bind to.
	 * @param wasi
	 *            Whether to run over STDIO in WASI mode.
	 * @return Exit code (0 for success, 1 for error).
	 */
	public static int mcp(String port, String listen, boolean wasi) {
		return serveJsonRpc(port, listen, wasi);
	}

	/**
	 * Parses and executes CLI subcommands from raw command-line arguments.
	 *
	 * @param args
	 *            Command-line arguments.
	 * @return Exit code (0 for success).
	 * @throws Exception
	 *             If an invalid command or execution error occurs.
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
				for (File f : files)
					specFiles.add(f);
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
						String title = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title.isEmpty())
							title = "Api";
						String testCode = tests.Emit.emit(api, null);
						writeFile(new File(srcTestJava, title + "IntegrationTest.java"), testCode);
						String mockCode = mocks.Emit.emit(api, null);
						writeFile(new File(srcTestJava, title + "MockServer.java"), mockCode);
						System.out.println("Generated Composable Tests & Mocks in " + outDir.getAbsolutePath());
					}
				} else {
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
						writeFile(new File(outDir, "src/test/java/" + e.getKey()), e.getValue());
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
			if (wasi) {
				startStdioJsonRpcServer();
			} else {
				startHttpJsonRpcServer(listen, Integer.parseInt(portStr));
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
			OpenAPI fullApi = extractOpenAPI(dir, truth);
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
					newSource = functions.Emit.emit(fullApi, source);
				} else if (absPath.contains("/cli/")) {
					newSource = cli.Emit.emitCli(fullApi);
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
						String title = (api.info != null && api.info.title != null)
								? api.info.title.replaceAll("[^a-zA-Z0-9]", "")
								: "Api";
						if (title.isEmpty())
							title = "Api";
						outFiles.put("src/test/java/" + title + "IntegrationTest.java", tests.Emit.emit(api, null));
						outFiles.put("src/test/java/" + title + "MockServer.java", mocks.Emit.emit(api, null));
					}
				} else {
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
			} else if (command.equals("to_openapi") || command.equals("sync")) {
				OpenAPI api = extractOpenAPI(workspaceDir);
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
			err.put("error", String.valueOf(e.getMessage()));
			System.out.println("CDD_IN_MEMORY_START");
			System.out.println(err.toString());
			System.out.println("CDD_IN_MEMORY_END");
		}
	}

	/**
	 * Writes string content to a UTF-8 file, creating parent directories if
	 * necessary.
	 *
	 * @param file
	 *            The target file to write.
	 * @param content
	 *            The UTF-8 string content to write.
	 * @throws IOException
	 *             If writing fails.
	 */
	private static void writeFile(File file, String content) throws IOException {
		File absParent = file.getAbsoluteFile().getParentFile();
		absParent.mkdirs();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes("UTF-8"));
		}
	}

	/**
	 * Reads the entire contents of a file as a UTF-8 string.
	 *
	 * @param file
	 *            The file to read.
	 * @return The file contents as a UTF-8 string.
	 * @throws IOException
	 *             If reading fails.
	 */
	private static String readFile(File file) throws IOException {
		return java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
	}

	/**
	 * Generates the default Maven {@code pom.xml} scaffolding content for generated
	 * projects.
	 *
	 * @return The XML content for {@code pom.xml}.
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
	 * Writes the Maven {@code pom.xml} scaffolding file into the specified
	 * directory.
	 *
	 * @param dir
	 *            The target project directory.
	 * @throws IOException
	 *             If writing {@code pom.xml} fails.
	 */
	private static void generateScaffolding(File dir) throws IOException {
		writeFile(new File(dir, "pom.xml"), getScaffoldingPom());
	}

	/**
	 * Generates the default GitHub Actions CI workflow YAML content.
	 *
	 * @return The YAML content for {@code .github/workflows/ci.yml}.
	 */
	private static String getGithubActionsCi() {
		return "name: CI\non: [push, pull_request]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n    - uses: actions/checkout@v6\n    - name: Set up JDK\n      uses: actions/setup-java@v3\n      with:\n        java-version: '11'\n        distribution: 'temurin'\n    - name: Build with Maven\n      run: mvn clean install\n";
	}

	/**
	 * Writes the GitHub Actions CI workflow into {@code .github/workflows/ci.yml}
	 * inside the target directory.
	 *
	 * @param dir
	 *            The target project directory.
	 * @throws IOException
	 *             If writing the workflow file fails.
	 */
	private static void generateGithubActions(File dir) throws IOException {
		File ghDir = new File(dir, ".github/workflows");
		ghDir.mkdirs();
		writeFile(new File(ghDir, "ci.yml"), getGithubActionsCi());
	}

	/**
	 * Starts an HTTP JSON-RPC 2.0 / MCP server on the specified host and port, also
	 * processing any lines buffered on standard input.
	 *
	 * @param listen
	 *            The host address to bind to.
	 * @param port
	 *            The TCP port to listen on (0 for an ephemeral port).
	 * @throws Exception
	 *             If starting or running the server fails.
	 */
	private static void startHttpJsonRpcServer(String listen, int port) throws Exception {
		if (activeHttpServer != null) {
			activeHttpServer.stop(0);
			activeHttpServer = null;
		}
		com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
				.create(new java.net.InetSocketAddress(listen, port), 0);
		server.createContext("/", exchange -> {
			byte[] reqBytes = exchange.getRequestBody().readAllBytes();
			String reqBody = new String(reqBytes, java.nio.charset.StandardCharsets.UTF_8).trim();
			String resp = handleJsonRpcRequest(reqBody);
			if (resp != null) {
				byte[] respBytes = resp.getBytes(java.nio.charset.StandardCharsets.UTF_8);
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, respBytes.length);
				try (java.io.OutputStream os = exchange.getResponseBody()) {
					os.write(respBytes);
				}
			} else {
				exchange.sendResponseHeaders(204, -1);
			}
		});
		server.start();
		activeHttpServer = server;
		startStdioJsonRpcServer();
	}

	/**
	 * Starts the JSON-RPC 2.0 / MCP server loop over standard input and standard
	 * output.
	 *
	 * @throws Exception
	 *             If reading from standard input fails.
	 */
	private static void startStdioJsonRpcServer() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			String response = handleJsonRpcRequest(line);
			if (response != null) {
				System.out.println(response);
				System.out.flush();
			}
		}
	}

	/**
	 * Processes a single JSON-RPC 2.0 request line and returns the serialized
	 * JSON-RPC response string, or {@code null} for notifications that require no
	 * response.
	 *
	 * @param line
	 *            The raw JSON-RPC 2.0 request string.
	 * @return The JSON-RPC 2.0 response string, or {@code null} for notifications.
	 */
	public static String handleJsonRpcRequest(String line) {
		try {
			JSONObject req = new JSONObject(line);
			Object idObj = req.has("id") && !req.isNull("id") ? req.get("id") : null;
			String idStr = idObj != null ? JSONObject.valueToString(idObj) : "null";
			if (req.has("jsonrpc") && "2.0".equals(req.getString("jsonrpc"))) {
				String method = req.optString("method", "");
				if ("initialize".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{\"tools\":{},\"resources\":{}},\"serverInfo\":{\"name\":\"cdd-java\",\"version\":\"0.0.3\"}},\"id\":"
							+ idStr + "}";
				} else if ("notifications/initialized".equals(method) || "initialized".equals(method)
						|| "$/cancelRequest".equals(method) || "cancelled".equals(method)
						|| "notifications/progress".equals(method) || "progress".equals(method)) {
					return null;
				} else if ("ping".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":" + idStr + "}";
				} else if ("tools/list".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{\"tools\":[{\"name\":\"cdd_generate\",\"description\":\"Generate code from OpenAPI or sync code\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"command\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"files\":{\"type\":\"object\"}},\"required\":[\"command\"]}}]},\"id\":"
							+ idStr + "}";
				} else if ("resources/list".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{\"resources\":[{\"uri\":\"cdd://ast/openapi\",\"name\":\"OpenAPI AST\",\"mimeType\":\"application/json\"}]},\"id\":"
							+ idStr + "}";
				} else if ("resources/read".equals(method)) {
					JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
					String uri = params.optString("uri", "");
					if ("cdd://ast/openapi".equals(uri)) {
						OpenAPI api = extractOpenAPI(workspaceDir);
						String astText = openapi.Emit.toString(api);
						return "{\"jsonrpc\":\"2.0\",\"result\":{\"contents\":[{\"uri\":\"cdd://ast/openapi\",\"mimeType\":\"application/json\",\"text\":"
								+ JSONObject.quote(astText) + "}]},\"id\":" + idStr + "}";
					} else {
						return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32602,\"message\":\"Resource not found\"},\"id\":"
								+ idStr + "}";
					}
				} else if ("tools/call".equals(method)) {
					JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
					String toolName = params.optString("name", "");
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
						return "{\"jsonrpc\":\"2.0\",\"result\":" + resultObj.toString() + ",\"id\":" + idStr + "}";
					} else {
						return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Tool not found\"},\"id\":"
								+ idStr + "}";
					}
				} else if ("version".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":\"0.0.3\",\"id\":" + idStr + "}";
				} else {
					if (idObj == null)
						return null;
					return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":"
							+ idStr + "}";
				}
			} else {
				if (idObj == null)
					return null;
				return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32600,\"message\":\"Invalid Request\"},\"id\":null}";
			}
		} catch (Exception e) {
			return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32700,\"message\":\"Parse error\"},\"id\":null}";
		}
	}

	/**
	 * Extracts an aggregated OpenAPI specification by parsing all Java files in a
	 * directory or file path.
	 *
	 * @param dir
	 *            The root directory or single Java file to scan.
	 * @return The extracted {@link OpenAPI} model.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	private static OpenAPI extractOpenAPI(File dir) throws Exception {
		return extractOpenAPI(dir, null);
	}

	/**
	 * Extracts an aggregated OpenAPI specification by parsing source files in a
	 * directory or file path, optionally prioritizing a source of truth.
	 *
	 * @param dir
	 *            The root directory or single file to scan.
	 * @param truth
	 *            Optional source of truth filter (e.g. {@code "openapi"},
	 *            {@code "orm"}, {@code "classes"}, {@code "routes"}, or
	 *            {@code null} for all).
	 * @return The extracted {@link OpenAPI} model.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	private static OpenAPI extractOpenAPI(File dir, String truth) throws Exception {
		if ("openapi".equalsIgnoreCase(truth)) {
			File specFile = new File(dir, "openapi.json");
			if (specFile.exists()) {
				return openapi.Parse.fromFile(specFile);
			}
		}
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
			if (!"orm".equalsIgnoreCase(truth) && !"classes".equalsIgnoreCase(truth)) {
				OpenAPI apiPaths = routes.Parse.parse(source);
				fullApi.paths.pathItems.putAll(apiPaths.paths.pathItems);
				OpenAPI mockPaths = mocks.Parse.parse(source);
				for (java.util.Map.Entry<String, openapi.PathItem> entry : mockPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
				OpenAPI cliPaths = cli.Parse.parse(source);
				if (cliPaths.paths != null) {
					for (java.util.Map.Entry<String, openapi.PathItem> entry : cliPaths.paths.pathItems.entrySet()) {
						fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
				OpenAPI testsPaths = tests.Parse.parse(source);
				for (java.util.Map.Entry<String, openapi.PathItem> entry : testsPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
				OpenAPI fnPaths = functions.Parse.parse(source);
				for (java.util.Map.Entry<String, openapi.PathItem> entry : fnPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
			}
			if (!"orm".equalsIgnoreCase(truth)) {
				OpenAPI apiClasses = classes.Parse.parse(source);
				fullApi.components.schemas.putAll(apiClasses.components.schemas);
			}
			if (!"classes".equalsIgnoreCase(truth)) {
				OpenAPI ormClasses = orm.Parse.parse(source);
				fullApi.components.schemas.putAll(ormClasses.components.schemas);
			}
		}
		return fullApi;
	}

	/**
	 * Retrieves a command-line option value by short flag, long flag, or fallback
	 * environment variable.
	 *
	 * @param args
	 *            The command-line arguments array.
	 * @param shortFlag
	 *            The short flag (e.g. {@code -i}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --input}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @return The resolved argument value, or {@code null} if not present.
	 */
	private static String getArg(String[] args, String shortFlag, String longFlag, String envVar) {
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(shortFlag) || args[i].equals(longFlag)) {
				return args[i + 1];
			}
		}
		if (envVar != null) {
			return envProvider.apply(envVar);
		}
		return null;
	}

	/**
	 * Checks whether a boolean flag is enabled via command-line arguments or
	 * environment variable.
	 *
	 * @param args
	 *            The command-line arguments array.
	 * @param shortFlag
	 *            The short flag (e.g. {@code -h}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --help}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @return {@code true} if the flag is set; {@code false} otherwise.
	 */
	private static boolean hasFlag(String[] args, String shortFlag, String longFlag, String envVar) {
		for (String arg : args) {
			if (arg.equals(shortFlag) || arg.equals(longFlag))
				return true;
		}
		if (envVar != null) {
			String env = envProvider.apply(envVar);
			return "true".equalsIgnoreCase(env) || "1".equals(env);
		}
		return false;
	}

	/**
	 * Recursively collects all {@code .java} files within a directory or single
	 * file.
	 *
	 * @param dir
	 *            The root directory or file to inspect.
	 * @param result
	 *            The list to populate with discovered Java files.
	 */
	private static void findJavaFiles(File dir, List<File> result) {
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java")) {
				result.add(dir);
			}
		} else {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files)
					findJavaFiles(f, result);
			}
		}
	}

	/**
	 * Prints the CLI usage and help text to standard output.
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
