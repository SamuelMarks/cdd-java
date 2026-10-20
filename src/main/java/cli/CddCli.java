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
		return ProjectScaffolder.resolveWasiPath(path, envProvider);
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
	 * Generates Java workflow SDK, CLI, or Server test/mock code from an Arazzo
	 * 1.1.0 specification.
	 *
	 * @param subCommand
	 *            Subcommand ({@code to_sdk}, {@code to_sdk_cli}, or
	 *            {@code to_server}).
	 * @param input
	 *            Input Arazzo 1.1.0 specification file or directory path.
	 * @param outputDir
	 *            Target output directory path.
	 * @param noGithubActions
	 *            Whether to disable GitHub Actions workflow generation.
	 * @param noInstallablePackage
	 *            Whether to disable installable package ({@code pom.xml})
	 *            generation.
	 * @param generateTests
	 *            Whether to generate E2E workflow tests and stateful mocks.
	 * @return Exit code (0 for success, 1 for error).
	 */
	public static int generateFromArazzo(String subCommand, String input, String outputDir, boolean noGithubActions,
			boolean noInstallablePackage, boolean generateTests) {
		List<String> argsList = new ArrayList<>();
		argsList.add("from_arazzo");
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
	 * Generates an Arazzo 1.1.0 specification from Java workflow, CLI, test, or
	 * mock source code.
	 *
	 * @param input
	 *            Input Java source file or directory path.
	 * @param output
	 *            Output Arazzo specification file path ({@code .json} or
	 *            {@code .yaml}).
	 * @return Exit code (0 for success, 1 for error).
	 */
	public static int generateToArazzo(String input, String output) {
		List<String> argsList = new ArrayList<>();
		argsList.add("to_arazzo");
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
		CliArguments cliArgs = CliArguments.parse(args);
		if (args.length == 0 || "--help".equals(cliArgs.command()) || "-h".equals(cliArgs.command())) {
			printHelp();
			return 0;
		}
		if ("--version".equals(cliArgs.command()) || "-v".equals(cliArgs.command())) {
			System.out.println("0.0.4");
			return 0;
		}
		String command = cliArgs.command();
		boolean wasi = cliArgs.hasFlag("--wasi", "--wasi", "CDD_WASI");
		if (command.equals("process_in_memory")) {
			if (cliArgs.hasFlag("-h", "--help", null)) {
				System.out.println("cdd-java process_in_memory");
				System.out.println("Usage:");
				System.out.println("  cdd-java process_in_memory <json_payload>");
				return 0;
			}
			if (args.length < 2) {
				System.err.println("Missing JSON payload argument");
				throw new Exception("Exit 1");
			}
			processInMemory(args[1]);
			return 0;
		}
		if (command.equals("from_openapi")) {
			if (cliArgs.hasFlag("-h", "--help", null)) {
				System.out.println("cdd-java from_openapi");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println(
						"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
				return 0;
			}
			String subCommand = cliArgs.subCommand("to_sdk");
			if (!subCommand.equals("to_sdk_cli") && !subCommand.equals("to_sdk") && !subCommand.equals("to_server")) {
				System.err.println("Unknown from_openapi subcommand: " + subCommand);
				throw new Exception("Exit 1");
			}
			String inputFile = cliArgs.getArg("-i", "--input", "CDD_INPUT");
			String inputDir = cliArgs.getArg("--input-dir", "--input-dir", "CDD_INPUT_DIR");
			if (inputFile == null && inputDir != null) {
				inputFile = inputDir;
			}
			String outputDir = cliArgs.getArg("-o", "--output", "CDD_OUTPUT");
			if (outputDir == null) {
				outputDir = System.getProperty("user.dir");
			}
			if (inputFile == null) {
				System.err.println("Missing -i <spec.json> or --input-dir <dir>");
				throw new Exception("Exit 1");
			}
			boolean noGithubActions = cliArgs.hasFlag("--no-github-actions", "--no-github-actions",
					"CDD_NO_GITHUB_ACTIONS");
			boolean noInstallablePackage = cliArgs.hasFlag("--no-installable-package", "--no-installable-package",
					"CDD_NO_INSTALLABLE_PACKAGE");
			boolean generateTests = cliArgs.hasFlag("--tests", "--tests", "CDD_TESTS");
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
		} else if (command.equals("from_arazzo")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java from_arazzo");
				System.out.println("Usage:");
				System.out.println(
						"  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println(
						"  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
				System.out.println("  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]");
				return 0;
			}
			String subCommand = "to_sdk";
			if (args.length > 1 && !args[1].startsWith("-")) {
				subCommand = args[1];
			}
			if (!subCommand.equals("to_sdk_cli") && !subCommand.equals("to_sdk") && !subCommand.equals("to_server")) {
				System.err.println("Unknown from_arazzo subcommand: " + subCommand);
				throw new Exception("Exit 1");
			}
			String inputFile = getArg(args, "-i", "--input", "CDD_INPUT");
			String outputDir = getArg(args, "-o", "--output", "CDD_OUTPUT");
			if (outputDir == null) {
				outputDir = System.getProperty("user.dir");
			}
			if (inputFile == null) {
				System.err.println("Missing -i <arazzo.json>");
				throw new Exception("Exit 1");
			}
			boolean noGithubActions = hasFlag(args, "--no-github-actions", "--no-github-actions",
					"CDD_NO_GITHUB_ACTIONS");
			boolean noInstallablePackage = hasFlag(args, "--no-installable-package", "--no-installable-package",
					"CDD_NO_INSTALLABLE_PACKAGE");
			boolean generateTests = hasFlag(args, "--tests", "--tests", "CDD_TESTS");
			File specFile = resolveFile(inputFile);
			arazzo.Arazzo spec = arazzo.Parse.fromFile(specFile);
			Map<String, OpenAPI> openApiSources = arazzo.ArazzoResolver.loadOpenApiSources(spec,
					specFile.getParentFile());
			File outDir = resolveFile(outputDir);
			outDir.mkdirs();
			if (!noInstallablePackage) {
				generateScaffolding(outDir);
			}
			if (!noGithubActions) {
				generateGithubActions(outDir);
			}
			if (subCommand.equals("to_sdk_cli")) {
				writeFile(new File(outDir, "WorkflowCli.java"), cli.ArazzoCliEmitter.emitWorkflowCli(spec));
				if (generateTests) {
					writeFile(new File(outDir, "WorkflowCliIntegrationTest.java"), tests.ArazzoTestEmitter.emit(spec));
					writeFile(new File(outDir, "WorkflowCliMock.java"), mocks.ArazzoMockEmitter.emit(spec));
				}
				System.out.println("Generated Arazzo SDK CLI in " + outDir.getAbsolutePath());
			} else if (subCommand.equals("to_sdk")) {
				String wfClassName = workflows.Emit.deriveClassName(spec);
				writeFile(new File(outDir, "src/main/java/workflows/" + wfClassName + ".java"),
						workflows.Emit.emit(spec, openApiSources, null));
				if (generateTests) {
					writeFile(new File(outDir, "src/test/java/" + wfClassName + "IntegrationTest.java"),
							tests.ArazzoTestEmitter.emit(spec));
					writeFile(new File(outDir, "src/test/java/" + wfClassName + "Mock.java"),
							mocks.ArazzoMockEmitter.emit(spec));
				}
				System.out.println("Generated Arazzo SDK in " + outDir.getAbsolutePath());
			} else {
				String wfClassName = workflows.Emit.deriveClassName(spec);
				writeFile(new File(outDir, "src/main/java/workflows/" + wfClassName + ".java"),
						workflows.Emit.emit(spec, openApiSources, null));
				writeFile(new File(outDir, "src/test/java/" + wfClassName + "IntegrationTest.java"),
						tests.ArazzoTestEmitter.emit(spec));
				writeFile(new File(outDir, "src/test/java/" + wfClassName + "Mock.java"),
						mocks.ArazzoMockEmitter.emit(spec));
				System.out.println("Generated Arazzo Server E2E Tests & Mocks in " + outDir.getAbsolutePath());
			}
		} else if (command.equals("to_arazzo")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java to_arazzo");
				System.out.println("Usage:");
				System.out.println("  cdd-java to_arazzo -i <path/to/code> [-o <arazzo.json>]");
				return 0;
			}
			String filePath = getArg(args, "-i", "--input", "CDD_INPUT");
			String outputFile = getArg(args, "-o", "--output", "CDD_OUTPUT");
			if (filePath == null) {
				System.err.println("Missing -i <path/to/code>");
				throw new Exception("Exit 1");
			}
			if (outputFile == null) {
				outputFile = "arazzo.json";
			}
			arazzo.Arazzo spec = extractArazzo(resolveFile(filePath));
			arazzo.Emit.toFile(spec, resolveFile(outputFile));
			System.out.println("Emitted Arazzo to " + outputFile);
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
		} else if (command.equals("serve_json_rpc") || command.equals("mcp")) {
			if (hasFlag(args, "-h", "--help", null)) {
				System.out.println("cdd-java " + command);
				System.out.println("Usage:");
				System.out.println("  cdd-java " + command + " [-p|--port <port>] [-l|--listen <address>] [--wasi]");
				return 0;
			}
			String portStr = getArg(args, "-p", "--port", "CDD_PORT");
			if (portStr == null)
				portStr = "8080";
			String listen = getArg(args, "-l", "--listen", "CDD_LISTEN");
			if (listen == null)
				listen = "127.0.0.1";
			if (wasi) {
				McpJsonRpcServer.serveJsonRpcStdio();
			} else {
				McpJsonRpcServer.serveJsonRpcHttp(listen, Integer.parseInt(portStr));
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
			SyncCommand.syncDirectory(dir, outputDir, truth);
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
					outFiles.put("pom.xml", ProjectScaffolder.generatePomXml());
				}
				if (!noGithubActions) {
					outFiles.put(".github/workflows/ci.yml", ProjectScaffolder.generateGithubActionsCi());
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
			} else if (command.equals("from_arazzo")) {
				String subCommand = "to_sdk";
				if (cmdArgs.length > 1 && !cmdArgs[1].startsWith("-")) {
					subCommand = cmdArgs[1];
				}
				String specContent = inFiles.optString("arazzo.json", inFiles.optString("spec.json", null));
				arazzo.Arazzo spec = arazzo.Parse.fromString(specContent);
				if (subCommand.equals("to_sdk_cli")) {
					outFiles.put("WorkflowCli.java", cli.ArazzoCliEmitter.emitWorkflowCli(spec));
				} else if (subCommand.equals("to_sdk")) {
					String wfClassName = workflows.Emit.deriveClassName(spec);
					outFiles.put("src/main/java/workflows/" + wfClassName + ".java", workflows.Emit.emit(spec));
				} else {
					String wfClassName = workflows.Emit.deriveClassName(spec);
					outFiles.put("src/test/java/" + wfClassName + "IntegrationTest.java",
							tests.ArazzoTestEmitter.emit(spec));
					outFiles.put("src/test/java/" + wfClassName + "Mock.java", mocks.ArazzoMockEmitter.emit(spec));
				}
			} else if (command.equals("to_arazzo")) {
				arazzo.Arazzo spec = SyncCommand.extractArazzo(workspaceDir);
				outFiles.put("arazzo.json", arazzo.Emit.toString(spec));
			} else if (command.equals("to_openapi") || command.equals("sync")) {
				OpenAPI api = SyncCommand.extractOpenAPI(workspaceDir);
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
		ProjectScaffolder.writeOutput(file, content);
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
		ProjectScaffolder.generateScaffolding(dir);
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
		ProjectScaffolder.generateGithubActions(dir);
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
		return McpJsonRpcServer.handleJsonRpc(line);
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
		return SyncCommand.extractOpenAPI(dir);
	}

	/**
	 * Extracts an aggregated Arazzo 1.1.0 specification from a Java file or
	 * directory containing workflow orchestrators, CLI runners, E2E tests, or
	 * mocks.
	 *
	 * @param dir
	 *            The root directory or single file to scan.
	 * @return The extracted {@link arazzo.Arazzo} specification.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	private static arazzo.Arazzo extractArazzo(File dir) throws Exception {
		return SyncCommand.extractArazzo(dir);
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
	public static String getArg(String[] args, String shortFlag, String longFlag, String envVar) {
		return CliArguments.getArg(args, shortFlag, longFlag, envVar, envProvider);
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
	public static boolean hasFlag(String[] args, String shortFlag, String longFlag, String envVar) {
		return CliArguments.hasFlag(args, shortFlag, longFlag, envVar, envProvider);
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
	public static void findJavaFiles(File dir, List<File> result) {
		ProjectScaffolder.findJavaFiles(dir, result);
	}

	/**
	 * Prints the CLI usage and help text to standard output.
	 */
	private static void printHelp() {
		System.out.println("cdd-java CLI");
		System.out.println("Usage:");
		System.out.println("  cdd-java [subcommand] [options]");
		System.out.println("\nSubcommands:");
		System.out.println("  from_openapi       Generate code from an OpenAPI specification.");
		System.out.println("  to_openapi         Generate an OpenAPI specification from source code.");
		System.out.println("  from_arazzo        Generate workflow code from an Arazzo 1.1.0 specification.");
		System.out.println("  to_arazzo          Generate an Arazzo 1.1.0 specification from workflow source code.");
		System.out.println(
				"  to_docs_json       Generate JSON documentation with code snippets for an OpenAPI specification.");
		System.out.println("  serve_json_rpc     Expose CLI interface as a JSON-RPC server.");
		System.out.println("  mcp                Expose CLI interface as a Model Context Protocol (MCP) server.");
		System.out.println("  sync               Synchronize database schema to models and OpenAPI specifications.");
		System.out.println("  process_in_memory  Execute compilation in memory from a JSON payload.");
		System.out.println("\nOptions:");
		System.out.println("  --help, -h      Show this help message.");
		System.out.println("  --version, -v   Show version information.");
		System.out.println("\nExamples:");
		System.out.println("  cdd-java serve_json_rpc [--port 8080] [--listen 127.0.0.1] [--wasi]");
		System.out.println("  cdd-java mcp [--port 8080] [--listen 127.0.0.1] [--wasi]");
		System.out.println(
				"  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println(
				"  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println("  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]");
		System.out.println(
				"  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println(
				"  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]");
		System.out.println("  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]");
		System.out.println("  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]");
		System.out.println("  cdd-java to_arazzo -i <path/to/code> [-o <arazzo.json>]");
		System.out.println("  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]");
		System.out.println("  cdd-java sync -i <dir> [-o <dir>] [--truth <type>]");
	}
}
