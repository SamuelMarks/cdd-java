import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;

import cli.Main;
import cli.CddCli;

/**
 * Comprehensive unit and integration tests for {@link Main} and {@link CddCli}.
 */
public class CliMainTest {

	/** Captured standard output stream. */
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	/** Captured standard error stream. */
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	/** Original standard output stream. */
	private final PrintStream originalOut = System.out;
	/** Original standard error stream. */
	private final PrintStream originalErr = System.err;
	/** Original standard input stream. */
	private final InputStream originalIn = System.in;
	/** In-memory environment variable overrides for {@link CddCli#envProvider}. */
	private final Map<String, String> mockEnv = new HashMap<>();

	/** Original exit handler. */
	private final java.util.function.Consumer<Integer> originalExitHandler = Main.exitHandler;

	/**
	 * Redirects standard streams and resets environment variable overrides before
	 * each test.
	 */
	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
		mockEnv.clear();
		CddCli.envProvider = key -> mockEnv.containsKey(key) ? mockEnv.get(key) : System.getenv(key);
		Main.exitHandler = code -> {
			throw new RuntimeException("Exit " + code);
		};
	}

	/**
	 * Restores standard streams, environment provider, and stops any active HTTP
	 * server after each test.
	 */
	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setIn(originalIn);
		CddCli.envProvider = System::getenv;
		Main.exitHandler = originalExitHandler;
		if (CddCli.activeHttpServer != null) {
			CddCli.activeHttpServer.stop(0);
			CddCli.activeHttpServer = null;
		}
	}

	/**
	 * Sets or removes an environment variable override for {@link CddCli}.
	 *
	 * @param key
	 *            The environment variable name.
	 * @param value
	 *            The environment variable value, or {@code null} to remove.
	 */
	private void setEnv(String key, String value) {
		if (value == null) {
			mockEnv.remove(key);
		} else {
			mockEnv.put(key, value);
		}
	}

	/**
	 * Invokes {@link Main#main} while suppressing expected {@code Exit 1} runtime
	 * exceptions in test mode.
	 *
	 * @param args
	 *            The CLI arguments.
	 * @throws Exception
	 *             If an unexpected exception occurs.
	 */
	private void runMain(String[] args) throws Exception {
		try {
			Main.main(args);
		} catch (Exception e) {
			if (!"Exit 1".equals(e.getMessage())) {
				throw e;
			}
		}
	}

	/**
	 * Verifies constructors, WASI entry points, and help/version flags.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testBasic() throws Exception {
		assertNotNull(new Main());
		assertNotNull(new CddCli());
		assertNotNull(new classes.McpEmitter());
		CddCli._start();
		CddCli._start(new String[]{"-h"});
		runMain(new String[]{});
		runMain(new String[]{"--help"});
		runMain(new String[]{"-h"});
		runMain(new String[]{"--version"});
		runMain(new String[]{"-v"});
	}

	/**
	 * Verifies handling of unknown top-level CLI subcommands.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testUnknownCommand() throws Exception {
		runMain(new String[]{"unknown_command"});
	}

	/**
	 * Verifies virtual root path resolution under {@code CDD_WASI_VIRTUAL_ROOT}.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testWasiVirtualRoot() throws Exception {
		File wasiDir = Files.createTempDirectory("wasi").toFile();
		File absDir = new File(wasiDir, "absolute");
		absDir.mkdirs();
		File specFile = new File(absDir, "spec.json");
		Files.writeString(specFile.toPath(),
				"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");

		try {
			setEnv("CDD_WASI_VIRTUAL_ROOT", wasiDir.getAbsolutePath());
			// Force a path starting with '/' so it triggers the VFS resolution logic even
			// on Windows
			String inPath = "/absolute/spec.json";
			String outPath = "/out";

			runMain(new String[]{"from_openapi", "to_sdk", "-i", inPath, "-o", outPath});
			runMain(new String[]{"from_openapi", "to_sdk", "-i", "src/test/resources/minimal-spec.json", "-o",
					"target/wasi_rel_out"});
			runMain(new String[]{"from_openapi", "to_sdk_cli", "-i", specFile.getAbsolutePath(), "-o",
					new File(wasiDir, "out_cli").getAbsolutePath()});
			runMain(new String[]{"from_openapi", "to_sdk", "--tests", "-i", specFile.getAbsolutePath(), "-o", "/out"});
		} finally {
			setEnv("CDD_WASI_VIRTUAL_ROOT", null);
		}
	}

	/**
	 * Verifies {@code process_in_memory} error handling when JSON payload is
	 * missing.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryMissingPayload() throws Exception {
		runMain(new String[]{"process_in_memory", "-h"});
		runMain(new String[]{"process_in_memory", "--help"});
		runMain(new String[]{"process_in_memory"});
	}

	/**
	 * Verifies {@code process_in_memory} error response on malformed JSON input.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryInvalidJson() throws Exception {
		runMain(new String[]{"process_in_memory", "invalid_json"});
		assertTrue(outContent.toString().contains("\"success\":false"));
	}

	/**
	 * Verifies {@code process_in_memory} execution of {@code to_docs_json}.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryDocsJson() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api\",\"version\":\"1\"},\"paths\":{}}";
		String payload = "{\"command\":[\"to_docs_json\", \"--no-imports\", \"--no-wrapping\"],\"files\":{\"spec.json\":\""
				+ spec.replace("\"", "\\\"") + "\"}}";
		runMain(new String[]{"process_in_memory", payload});
		assertTrue(outContent.toString().contains("\"success\":true"));
	}

	/**
	 * Verifies {@code process_in_memory} error response on unsupported subcommands.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryUnsupportedCommand() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api\",\"version\":\"1\"},\"paths\":{}}";
		String payload = "{\"command\":[\"unknown_cmd\"],\"files\":{\"spec.json\":\"" + spec.replace("\"", "\\\"")
				+ "\"}}";
		runMain(new String[]{"process_in_memory", payload});
		assertTrue(outContent.toString().contains("\"success\":false"));
	}

	/**
	 * Verifies {@code process_in_memory} across {@code from_openapi},
	 * {@code to_openapi}, and {@code sync} commands.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryFromOpenapi() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api Title\",\"version\":\"1\"},\"paths\":{}}";
		String escapedSpec = spec.replace("\"", "\\\"");

		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk_cli\", \"--tests\"],\"files\":{\"spec.json\":\"" + escapedSpec
						+ "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk_cli\"],\"files\":{\"spec.json\":\"" + escapedSpec + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk\", \"--tests\", \"--no-github-actions\", \"--no-installable-package\"],\"files\":{\"spec.json\":\""
						+ escapedSpec + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"--tests\"],\"files\":{\"spec.json\":\"{\\\"openapi\\\":\\\"3.0.0\\\",\\\"info\\\":{\\\"title\\\":\\\"\\\"}}\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\"],\"files\":{\"spec.json\":\"{\\\"openapi\\\":\\\"3.0.0\\\"}\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk\", \"--tests\"],\"files\":{\"spec.json\":\"{\\\"openapi\\\":\\\"3.0.0\\\"}\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk\", \"--tests\"],\"files\":{\"spec.json\":\"{\\\"openapi\\\":\\\"3.0.0\\\",\\\"info\\\":{}}\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_server\"],\"files\":{\"spec.json\":\"{\\\"openapi\\\":\\\"3.0.0\\\",\\\"paths\\\":{\\\"/p\\\":{\\\"get\\\":{\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"ok\\\"}}}}},\\\"components\\\":{\\\"schemas\\\":{\\\"S\\\":{\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"id\\\":{\\\"type\\\":\\\"string\\\"}}}}}}\"}}"});
		File wsDir = Files.createTempDirectory("cdd_ws_test").toFile();
		Files.writeString(new File(wsDir, "Sample.java").toPath(), "public class Sample { public void hello() {} }");
		File prevWs = CddCli.workspaceDir;
		CddCli.workspaceDir = wsDir;
		try {
			runMain(new String[]{"process_in_memory", "{\"command\":[\"to_openapi\"]}"});
			runMain(new String[]{"process_in_memory", "{\"command\":[\"sync\"]}"});
		} finally {
			CddCli.workspaceDir = prevWs;
		}
	}

	/**
	 * Verifies {@code from_openapi} CLI subcommands across JSON, YAML, and
	 * directory inputs.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testFromOpenapi() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test").toFile();
		File specFile = new File(tempDir, "spec.json");
		Files.writeString(specFile.toPath(),
				"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");
		File noTitleSpec = new File(tempDir, "spec2.yaml");
		Files.writeString(noTitleSpec.toPath(), "openapi: 3.0.0\npaths: {}\n");
		File ymlSpec = new File(tempDir, "spec3.yml");
		Files.writeString(ymlSpec.toPath(), "openapi: 3.0.0\ninfo:\n  version: '1'\npaths: {}\n");
		Files.writeString(new File(tempDir, "ignore.txt").toPath(), "not a spec");
		File outDir = new File(tempDir, "out");

		runMain(new String[]{"from_openapi", "-h"});
		runMain(new String[]{"from_openapi", "--help"});
		runMain(new String[]{"to_openapi", "--help"});
		runMain(new String[]{"to_docs_json", "--help"});
		runMain(new String[]{"serve_json_rpc", "--help"});
		runMain(new String[]{"mcp", "-h"});
		runMain(new String[]{"mcp", "--help"});
		runMain(new String[]{"sync", "--help"});
		runMain(new String[]{"from_openapi", "unknown_sub", "-i", specFile.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_sdk"});
		runMain(new String[]{"from_openapi", "to_sdk_cli", "-i", specFile.getAbsolutePath(), "-o",
				outDir.getAbsolutePath(), "--tests"});
		runMain(new String[]{"from_openapi", "to_sdk_cli", "-i", specFile.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_sdk", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath(),
				"--tests"});
		runMain(new String[]{"from_openapi", "to_sdk", "-i", noTitleSpec.getAbsolutePath(), "-o",
				outDir.getAbsolutePath(), "--tests"});
		runMain(new String[]{"from_openapi", "to_sdk", "-i", ymlSpec.getAbsolutePath(), "-o", outDir.getAbsolutePath(),
				"--tests"});
		runMain(new String[]{"from_openapi", "to_sdk", "-i", ymlSpec.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_server", "-i", specFile.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_sdk", "--input-dir", tempDir.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath()});
	}

	/**
	 * Verifies {@code to_openapi} extraction from client, mock, test, and DTO
	 * classes.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testToOpenApiCoverage() throws Exception {
		File tempDir = Files.createTempDirectory("cdd-to-openapi").toFile();
		File javaFile = new File(tempDir, "TestToOpenApi.java");
		String code = "package test;\n" + "        import java.net.http.HttpResponse;\n"
				+ "        public class TestAPIClient {\n" + "            public void testMethod() { }\n"
				+ "            /**\n" + "             * @xmlName something\n" + "             */\n"
				+ "            private static void printHelp() {\n"
				+ "                System.out.println(\"Operation: GET /edge\");\n"
				+ "                System.out.println(\"Operation Object \");\n" + "    }\n" + "        }\n"
				+ "        public class TestMockServer {\n" + "            public void testMethod() { }\n"
				+ "            public void setup() { server.createContext(\"/mock_path\"); }\n" + "        }\n"
				+ "        public class TestIntegrationTest {\n" + "            public void test_getThing() { }\n"
				+ "        }\n" + "        class TestClass {\n" + "            public String testProp;\n"
				+ "        }\n";
		try (java.io.FileOutputStream fos = new java.io.FileOutputStream(javaFile)) {
			fos.write(code.getBytes("UTF-8"));
		}

		File outFile = new File(tempDir, "out.json");
		runMain(new String[]{"to_openapi", "-i", tempDir.getAbsolutePath(), "-o", outFile.getAbsolutePath()});
		assertTrue(outFile.exists());
	}

	/**
	 * Verifies additional {@link CddCli#processInMemory} payload branches.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProcessInMemoryCoverage() throws Exception {
		org.json.JSONObject args = new org.json.JSONObject();
		args.put("command", "to_openapi");
		org.json.JSONArray cmdArgs = new org.json.JSONArray();
		args.put("cmdArgs", cmdArgs);

		org.json.JSONObject inFiles = new org.json.JSONObject();
		org.json.JSONArray javaFiles = new org.json.JSONArray();
		inFiles.put("javaFiles", javaFiles);

		args.put("inFiles", inFiles);

		// This tests the `fullApi.components.schemas.putAll` when `testsPaths` etc are
		// null/empty.
		cli.CddCli.processInMemory(args.toString());

		// Test to_docs_json missing spec
		args.put("command", "to_docs_json");
		inFiles.remove("spec.json"); // explicitly missing
		try {
			cli.CddCli.processInMemory(args.toString());
		} catch (Exception e) {
			// Expected
		}

		// Test to_sdk with empty title
		args.put("command", "from_openapi");
		args.put("subCommand", "to_sdk");
		cmdArgs.put("--tests");
		inFiles.put("spec.json", "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\"}}");
		cli.CddCli.processInMemory(args.toString());
	}

	/**
	 * Verifies {@code to_openapi} CLI subcommand with explicit and default output
	 * paths.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testToOpenapi() throws Exception {
		File outFile = Files.createTempFile("spec", ".json").toFile();
		runMain(new String[]{"to_openapi", "-h"});
		runMain(new String[]{"to_openapi"});
		runMain(new String[]{"to_openapi", "-i", "src/main/java", "-o", outFile.getAbsolutePath()});

		String oldDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", outFile.getParentFile().getAbsolutePath());
			runMain(new String[]{"to_openapi", "-i", "src/main/java"});
		} finally {
			System.setProperty("user.dir", oldDir);
		}
	}

	/**
	 * Verifies {@code to_docs_json} CLI subcommand.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testToDocsJson() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test_to_docs").toFile();
		File specFile = new File(tempDir, "spec.json");
		Files.writeString(specFile.toPath(),
				"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");
		File outFile = new File(tempDir, "docs.json");
		File outSubDir = new File(tempDir, "out_dir");
		outSubDir.mkdirs();

		runMain(new String[]{"to_docs_json", "-h"});
		runMain(new String[]{"to_docs_json"});
		runMain(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath(), "-o", outFile.getAbsolutePath(),
				"--no-imports", "--no-wrapping"});
		assertTrue(outFile.exists());
		runMain(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath(), "-o", outSubDir.getAbsolutePath()});
		assertTrue(new File(outSubDir, "docs.json").exists());

		String oldDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", tempDir.getAbsolutePath());
			runMain(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath()});
		} finally {
			System.setProperty("user.dir", oldDir);
		}
	}

	/**
	 * Verifies STDIO and HTTP JSON-RPC 2.0 / MCP server modes in
	 * {@link CddCli#serveJsonRpc}.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testServeJsonRpc() throws Exception {
		runMain(new String[]{"serve_json_rpc", "-h"});

		String input = "\n" + "{\"jsonrpc\":\"2.0\",\"method\":\"version\",\"id\":1}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":2}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"initialized\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":21}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"close\",\"id\":22}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"shutdown\",\"id\":23}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"$/cancelRequest\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"cancelled\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/progress\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"progress\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/disconnected\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"exit\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"roots/list\",\"id\":24}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"sampling/createMessage\",\"id\":25}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"sampling/createMessage\",\"params\":{\"approved\":true},\"id\":26}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"sampling/createMessage\",\"params\":{\"approved\":false},\"id\":27}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"id\":3}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"params\":{\"cursor\":\"end\"},\"id\":301}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/list\",\"id\":31}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/list\",\"params\":{\"cursor\":\"page2\"},\"id\":311}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"cdd://ast/openapi\"},\"id\":32}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"openapi://spec\"},\"id\":321}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"arazzo://spec\"},\"id\":322}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"file://../secret\"},\"id\":323}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"unknown\"},\"id\":33}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"id\":34}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"cdd_generate\",\"arguments\":{\"command\":[\"to_openapi\",\"-h\"]}},\"id\":4}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"cdd_generate\",\"approved\":false},\"id\":401}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"cdd_generate\"},\"id\":41}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"unknown_tool\"},\"id\":5}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"id\":6}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"unknown\",\"id\":\"abc\"}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"unknown\"}\n" + "{\"jsonrpc\":\"2.0\",\"id\":null}\n"
				+ "{\"jsonrpc\":\"1.0\",\"id\":98}\n" + "{\"jsonrpc\":\"1.0\"}\n"
				+ "{\"method\":\"invalid\",\"id\":99}\n" + "{\"method\":\"invalid\"}\n" + "invalid_json\n";

		System.setIn(new ByteArrayInputStream(input.getBytes("UTF-8")));
		File wsDir = Files.createTempDirectory("cdd_rpc_ws").toFile();
		Files.writeString(new File(wsDir, "Sample.java").toPath(), "public class Sample { public void hello() {} }");
		File prevWs = CddCli.workspaceDir;
		CddCli.workspaceDir = wsDir;
		try {
			runMain(new String[]{"serve_json_rpc", "--wasi"});
		} finally {
			CddCli.workspaceDir = prevWs;
		}

		String out = outContent.toString();
		assertTrue(out.contains("\"result\":\"0.0.4\""));
		assertTrue(out.contains("-32700"));
		assertTrue(out.contains("-32600"));
		assertTrue(out.contains("-32601"));
		assertTrue(out.contains("-32602"));

		// Test HTTP server mode and replacing existing server
		System.setIn(new ByteArrayInputStream("".getBytes("UTF-8")));
		runMain(new String[]{"mcp", "--wasi"});
		System.setIn(new ByteArrayInputStream("".getBytes("UTF-8")));
		runMain(new String[]{"serve_json_rpc", "-p", "0", "-l", "127.0.0.1"});
		assertNotNull(CddCli.activeHttpServer);
		runMain(new String[]{"serve_json_rpc", "-p", "0", "-l", "127.0.0.1"});
		assertNotNull(CddCli.activeHttpServer);
		int port = CddCli.activeHttpServer.getAddress().getPort();
		java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
		java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://127.0.0.1:" + port + "/")).header("Authorization", "Bearer test-token")
				.POST(java.net.http.HttpRequest.BodyPublishers
						.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}"))
				.build();
		java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
		assertEquals(200, resp.statusCode());
		assertTrue(resp.body().contains("\"result\":{}"));
		assertEquals("Bearer test-token", cli.McpJsonRpcServer.activeAuthToken);

		java.net.http.HttpRequest blankAuthReq = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://127.0.0.1:" + port + "/mcp/message")).header("Authorization", "   ")
				.POST(java.net.http.HttpRequest.BodyPublishers
						.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":2}"))
				.build();
		assertEquals(200, client.send(blankAuthReq, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode());

		java.net.http.HttpRequest sseGetReq = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://127.0.0.1:" + port + "/mcp/sse")).GET().build();
		java.net.http.HttpResponse<String> sseGetResp = client.send(sseGetReq,
				java.net.http.HttpResponse.BodyHandlers.ofString());
		assertEquals(200, sseGetResp.statusCode());
		assertTrue(sseGetResp.body().contains("event: endpoint"));

		java.net.http.HttpRequest ssePostReq = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://127.0.0.1:" + port + "/mcp/sse"))
				.POST(java.net.http.HttpRequest.BodyPublishers
						.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":3}"))
				.build();
		assertEquals(200, client.send(ssePostReq, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode());

		java.net.http.HttpRequest notifReq = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://127.0.0.1:" + port + "/"))
				.POST(java.net.http.HttpRequest.BodyPublishers
						.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"initialized\"}"))
				.build();
		java.net.http.HttpResponse<String> notifResp = client.send(notifReq,
				java.net.http.HttpResponse.BodyHandlers.ofString());
		assertEquals(204, notifResp.statusCode());
		CddCli.activeHttpServer.stop(0);
		CddCli.activeHttpServer = null;
	}

	/**
	 * Verifies programmatic SDK methods on {@link CddCli}.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testProgrammaticSdkMethods() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test_prog_sdk").toFile();
		File specFile = new File("src/test/resources/minimal-spec.json");

		assertEquals(0, cli.CddCli.generateFromOpenApi("to_sdk", specFile.getAbsolutePath(), tempDir.getAbsolutePath(),
				true, true, true));
		assertEquals(0, cli.CddCli.generateFromOpenApi("", specFile.getAbsolutePath(), tempDir.getAbsolutePath(), false,
				false, false));
		assertEquals(1, cli.CddCli.generateFromOpenApi(null, null, null, false, false, false));
		assertEquals(0,
				cli.CddCli.generateToOpenApi("src/main/java", new File(tempDir, "spec.json").getAbsolutePath()));
		assertEquals(1, cli.CddCli.generateToOpenApi(null, null));
		assertEquals(0, cli.CddCli.generateDocsJson(specFile.getAbsolutePath(),
				new File(tempDir, "docs.json").getAbsolutePath(), true, true));
		assertEquals(1, cli.CddCli.generateDocsJson(null, null, false, false));

		System.setIn(
				new ByteArrayInputStream("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}\n".getBytes("UTF-8")));
		assertEquals(0, cli.CddCli.serveJsonRpc("0", "127.0.0.1", false));
		assertEquals(1, cli.CddCli.serveJsonRpc("invalid_port", "127.0.0.1", false));
		System.setIn(new ByteArrayInputStream("".getBytes("UTF-8")));
		assertEquals(0, cli.CddCli.serveJsonRpc(null, null, true));
		System.setIn(
				new ByteArrayInputStream("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}\n".getBytes("UTF-8")));
		assertEquals(0, cli.CddCli.mcp("0", "127.0.0.1", false));
	}

	/**
	 * Verifies {@code sync} CLI command and {@link CddCli#sync} across truth modes.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testSync() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test_sync").toFile();
		File outSyncDir = Files.createTempDirectory("cdd_test_sync_out").toFile();

		String code = "package test;\npublic class Routes {\n public void handle() {}\n}\n";
		String[] subDirs = {"classes", "orm", "routes", "mocks", "tests", "functions", "cli", "other"};
		for (String sub : subDirs) {
			File d = new File(tempDir, sub);
			d.mkdirs();
			Files.writeString(new File(d, "Test.java").toPath(), code);
		}
		Files.writeString(new File(tempDir, "README.txt").toPath(), "ignore me");

		runMain(new String[]{"sync", "-h"});
		runMain(new String[]{"sync"});
		runMain(new String[]{"sync", "-i", tempDir.getAbsolutePath()});
		runMain(new String[]{"sync", "-i", tempDir.getAbsolutePath()});

		File singleFile = new File(tempDir, "classes/Test.java");
		assertEquals(0, cli.CddCli.sync(singleFile.getAbsolutePath(), singleFile.getAbsolutePath(), "code"));
		Files.writeString(singleFile.toPath(), "package test;\npublic class Routes {}\n");
		assertEquals(0, cli.CddCli.sync(singleFile.getAbsolutePath(), null, "code"));
		assertEquals(0, cli.CddCli.sync(new File(tempDir, "nonexistent_dir").getAbsolutePath(), null, "code"));
		assertEquals(0, cli.CddCli.sync(tempDir.getAbsolutePath(), outSyncDir.getAbsolutePath(), "orm"));
		assertEquals(0, cli.CddCli.sync(tempDir.getAbsolutePath(), null, "classes"));
		assertEquals(0, cli.CddCli.sync(tempDir.getAbsolutePath(), null, "openapi"));
		Files.writeString(new File(tempDir, "openapi.json").toPath(),
				"{\"openapi\":\"3.2.0\",\"info\":{\"title\":\"T\",\"version\":\"1\"}}");
		assertEquals(0, cli.CddCli.sync(tempDir.getAbsolutePath(), null, "openapi"));
		assertEquals(1, cli.CddCli.sync(null, null, null));
	}

	/**
	 * Verifies environment variable fallbacks for CLI flags and arguments.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testEnvVarFlags() throws Exception {
		setEnv("CDD_WASI", "1");
		setEnv("CDD_NO_GITHUB_ACTIONS", "true");
		setEnv("CDD_NO_INSTALLABLE_PACKAGE", "true");
		setEnv("CDD_TESTS", "true");
		setEnv("CDD_INPUT_FILE", "spec.json");
		setEnv("CDD_OUTPUT_DIR", "out");

		try {
			File tempDir = Files.createTempDirectory("cdd_test_env").toFile();
			File specFile = new File(tempDir, "spec.json");
			Files.writeString(specFile.toPath(),
					"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");
			setEnv("CDD_INPUT_FILE", specFile.getAbsolutePath());

			runMain(new String[]{"from_openapi", "to_sdk"});

			setEnv("CDD_WASI", "false");
			setEnv("CDD_NO_GITHUB_ACTIONS", "1");
			setEnv("CDD_NO_INSTALLABLE_PACKAGE", "1");
			setEnv("CDD_TESTS", "1");
			runMain(new String[]{"from_openapi", "to_sdk"});

			setEnv("CDD_WASI", "true");
			setEnv("CDD_NO_GITHUB_ACTIONS", "false");
			setEnv("CDD_NO_INSTALLABLE_PACKAGE", "false");
			setEnv("CDD_TESTS", "false");
			runMain(new String[]{"from_openapi", "to_sdk"});
		} finally {
			setEnv("CDD_WASI", null);
			setEnv("CDD_NO_GITHUB_ACTIONS", null);
			setEnv("CDD_NO_INSTALLABLE_PACKAGE", null);
			setEnv("CDD_TESTS", null);
			setEnv("CDD_INPUT_FILE", null);
			setEnv("CDD_OUTPUT_DIR", null);
		}
	}

	/**
	 * Verifies {@code from_arazzo}, {@code to_arazzo}, {@code sync} with workflows,
	 * in-memory Arazzo commands, and MCP {@code cdd://ast/arazzo} resource reads.
	 *
	 * @throws Exception
	 *             If execution fails.
	 */
	@Test
	public void testFromAndToArazzoCliAndMcp() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_arazzo_cli").toFile();
		File arazzoFile = new File(tempDir, "arazzo.json");
		String arazzoJson = "{\"arazzo\":\"1.1.0\",\"info\":{\"title\":\"PetFlow\",\"version\":\"1.0.0\"},\"sourceDescriptions\":[{\"name\":\"petstore\",\"url\":\"./oas.json\",\"type\":\"openapi\"}],\"workflows\":[{\"workflowId\":\"adopt\",\"steps\":[{\"stepId\":\"s1\",\"operationId\":\"getPet\"}]}]}";
		Files.writeString(arazzoFile.toPath(), arazzoJson);

		runMain(new String[]{"from_arazzo", "-h"});
		runMain(new String[]{"from_arazzo", "--help"});
		runMain(new String[]{"to_arazzo", "-h"});
		runMain(new String[]{"to_arazzo", "--help"});
		runMain(new String[]{"from_arazzo", "unknown_sub", "-i", arazzoFile.getAbsolutePath()});
		runMain(new String[]{"from_arazzo", "to_sdk"});
		runMain(new String[]{"to_arazzo"});

		File sdkOut = new File(tempDir, "sdk_out");
		File cliOut = new File(tempDir, "cli_out");
		File srvOut = new File(tempDir, "srv_out");
		File plainJavaDir = Files.createTempDirectory("cdd_arazzo_plain").toFile();
		Files.writeString(new File(plainJavaDir, "Plain.java").toPath(),
				"public class Plain { public void stepOne() {} }");

		File defaultOutDir = Files.createTempDirectory("cdd_arazzo_default_out").toFile();
		String oldUserDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", defaultOutDir.getAbsolutePath());
			runMain(new String[]{"from_arazzo", "-i", arazzoFile.getAbsolutePath(), "--no-installable-package",
					"--no-github-actions"});
			runMain(new String[]{"to_arazzo", "-i", plainJavaDir.getAbsolutePath(), "-o",
					new File(defaultOutDir, "arazzo_default.json").getAbsolutePath()});
			setEnv("CDD_WASI_VIRTUAL_ROOT", defaultOutDir.getAbsolutePath());
			setEnv("CDD_OUTPUT", "/arazzo_wasi.json");
			runMain(new String[]{"to_arazzo", "-i", plainJavaDir.getAbsolutePath()});
			setEnv("CDD_WASI_VIRTUAL_ROOT", null);
			setEnv("CDD_OUTPUT", null);
			runMain(new String[]{"to_arazzo", "-i", plainJavaDir.getAbsolutePath()});
			Files.deleteIfExists(new File("arazzo.json").toPath());
		} finally {
			System.setProperty("user.dir", oldUserDir);
			setEnv("CDD_WASI_VIRTUAL_ROOT", null);
			setEnv("CDD_OUTPUT", null);
		}
		runMain(new String[]{"from_arazzo", "to_sdk", "-i", arazzoFile.getAbsolutePath(), "-o",
				sdkOut.getAbsolutePath(), "--tests"});
		runMain(new String[]{"from_arazzo", "to_sdk", "-i", arazzoFile.getAbsolutePath(), "-o",
				sdkOut.getAbsolutePath(), "--no-github-actions", "--no-installable-package"});
		runMain(new String[]{"from_arazzo", "to_sdk_cli", "-i", arazzoFile.getAbsolutePath(), "-o",
				cliOut.getAbsolutePath(), "--tests"});
		runMain(new String[]{"from_arazzo", "to_sdk_cli", "-i", arazzoFile.getAbsolutePath(), "-o",
				cliOut.getAbsolutePath()});
		runMain(new String[]{"from_arazzo", "to_server", "-i", arazzoFile.getAbsolutePath(), "-o",
				srvOut.getAbsolutePath()});

		File rtSdk = new File(tempDir, "rt_sdk.json");
		File rtCli = new File(tempDir, "rt_cli.json");
		runMain(new String[]{"to_arazzo", "-i", sdkOut.getAbsolutePath(), "-o", rtSdk.getAbsolutePath()});
		runMain(new String[]{"to_arazzo", "-i", new File(cliOut, "WorkflowCli.java").getAbsolutePath(), "-o",
				rtCli.getAbsolutePath()});
		runMain(new String[]{"to_arazzo", "-i", tempDir.getAbsolutePath(), "-o",
				new File(tempDir, "rt_from_spec.json").getAbsolutePath()});

		File onlyTestDir = Files.createTempDirectory("cdd_arazzo_only_test").toFile();
		Files.copy(new File(srvOut, "src/test/java/PetFlowWorkflowsIntegrationTest.java").toPath(),
				new File(onlyTestDir, "PetFlowWorkflowsIntegrationTest.java").toPath());
		runMain(new String[]{"to_arazzo", "-i", onlyTestDir.getAbsolutePath(), "-o",
				new File(tempDir, "rt_test.json").getAbsolutePath()});

		File onlyMockDir = Files.createTempDirectory("cdd_arazzo_only_mock").toFile();
		Files.copy(new File(srvOut, "src/test/java/PetFlowWorkflowsMock.java").toPath(),
				new File(onlyMockDir, "PetFlowWorkflowsMock.java").toPath());
		runMain(new String[]{"to_arazzo", "-i", onlyMockDir.getAbsolutePath(), "-o",
				new File(tempDir, "rt_mock.json").getAbsolutePath()});

		runMain(new String[]{"to_arazzo", "-i", plainJavaDir.getAbsolutePath(), "-o",
				new File(tempDir, "rt_plain.json").getAbsolutePath()});

		File emptyDir = Files.createTempDirectory("cdd_arazzo_empty").toFile();
		runMain(new String[]{"to_arazzo", "-i", emptyDir.getAbsolutePath(), "-o",
				new File(tempDir, "rt_empty.json").getAbsolutePath()});

		assertEquals(0, CddCli.generateFromArazzo("to_sdk", arazzoFile.getAbsolutePath(), sdkOut.getAbsolutePath(),
				true, true, true));
		assertEquals(0, CddCli.generateFromArazzo("", arazzoFile.getAbsolutePath(), sdkOut.getAbsolutePath(), false,
				false, false));
		assertEquals(1, CddCli.generateFromArazzo(null, null, null, false, false, false));
		assertEquals(0,
				CddCli.generateToArazzo(sdkOut.getAbsolutePath(), new File(tempDir, "prog.json").getAbsolutePath()));
		assertEquals(1, CddCli.generateToArazzo(null, null));

		// Sync with /workflows/ directory
		assertEquals(0, CddCli.sync(sdkOut.getAbsolutePath(), null, "workflows"));

		// process_in_memory for from_arazzo and to_arazzo
		String escaped = arazzoJson.replace("\"", "\\\"");
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_arazzo\"],\"files\":{\"arazzo.json\":\"" + escaped + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_arazzo\",\"to_sdk\"],\"files\":{\"arazzo.json\":\"" + escaped + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_arazzo\",\"to_sdk_cli\"],\"files\":{\"arazzo.json\":\"" + escaped + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_arazzo\",\"to_server\"],\"files\":{\"arazzo.json\":\"" + escaped + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_arazzo\",\"--tests\"],\"files\":{\"spec.json\":\"" + escaped + "\"}}"});

		File prevWs = CddCli.workspaceDir;
		CddCli.workspaceDir = sdkOut;
		try {
			runMain(new String[]{"process_in_memory", "{\"command\":[\"to_arazzo\"]}"});
			String rpcResp = CddCli.handleJsonRpcRequest(
					"{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"cdd://ast/arazzo\"},\"id\":77}");
			assertNotNull(rpcResp);
			assertTrue(rpcResp.contains("cdd://ast/arazzo"));
		} finally {
			CddCli.workspaceDir = prevWs;
		}
	}
}
