import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.lang.reflect.Field;
import org.json.JSONObject;

import cli.Main;
import cli.CddCli;

public class CliMainTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;
	private final InputStream originalIn = System.in;

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setIn(originalIn);
	}

	@SuppressWarnings("unchecked")
	private void setEnv(String key, String value) {
		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			if (value == null) {
				writableEnv.remove(key);
			} else {
				writableEnv.put(key, value);
			}
		} catch (Exception e) {
			try {
				Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
				Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
				theEnvironmentField.setAccessible(true);
				Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
				if (value == null) {
					env.remove(key);
				} else {
					env.put(key, value);
				}
				Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
						.getDeclaredField("theCaseInsensitiveEnvironment");
				theCaseInsensitiveEnvironmentField.setAccessible(true);
				Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
				if (value == null) {
					cienv.remove(key);
				} else {
					cienv.put(key, value);
				}
			} catch (Exception e2) {
				// Ignore
			}
		}
	}

	private void runMain(String[] args) throws Exception {
		try {
			Main.main(args);
		} catch (Exception e) {
			if (!"Exit 1".equals(e.getMessage())) {
				throw e;
			}
		}
	}

	@Test
	public void testBasic() throws Exception {
		new Main();
		CddCli._start();
		CddCli._start(new String[]{"-h"});
		runMain(new String[]{});
		runMain(new String[]{"--help"});
		runMain(new String[]{"-h"});
		runMain(new String[]{"--version"});
		runMain(new String[]{"-v"});
	}

	@Test
	public void testUnknownCommand() throws Exception {
		runMain(new String[]{"unknown_command"});
	}

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
		} finally {
			setEnv("CDD_WASI_VIRTUAL_ROOT", null);
		}
	}

	@Test
	public void testProcessInMemoryMissingPayload() throws Exception {
		runMain(new String[]{"process_in_memory"});
	}

	@Test
	public void testProcessInMemoryInvalidJson() throws Exception {
		runMain(new String[]{"process_in_memory", "invalid_json"});
		assertTrue(outContent.toString().contains("\"success\":false"));
	}

	@Test
	public void testProcessInMemoryDocsJson() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api\",\"version\":\"1\"},\"paths\":{}}";
		String payload = "{\"command\":[\"to_docs_json\", \"--no-imports\", \"--no-wrapping\"],\"files\":{\"spec.json\":\""
				+ spec.replace("\"", "\\\"") + "\"}}";
		runMain(new String[]{"process_in_memory", payload});
		assertTrue(outContent.toString().contains("\"success\":true"));
	}

	@Test
	public void testProcessInMemoryUnsupportedCommand() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api\",\"version\":\"1\"},\"paths\":{}}";
		String payload = "{\"command\":[\"unknown_cmd\"],\"files\":{\"spec.json\":\"" + spec.replace("\"", "\\\"")
				+ "\"}}";
		runMain(new String[]{"process_in_memory", payload});
		assertTrue(outContent.toString().contains("\"success\":false"));
	}

	@Test
	public void testProcessInMemoryFromOpenapi() throws Exception {
		String spec = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Api Title\",\"version\":\"1\"},\"paths\":{}}";
		String escapedSpec = spec.replace("\"", "\\\"");

		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk_cli\", \"--tests\"],\"files\":{\"spec.json\":\"" + escapedSpec
						+ "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_sdk\", \"--tests\"],\"files\":{\"spec.json\":\"" + escapedSpec
						+ "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_server\"],\"files\":{\"spec.json\":\"" + escapedSpec + "\"}}"});
		runMain(new String[]{"process_in_memory",
				"{\"command\":[\"from_openapi\", \"to_orm\", \"--no-github-actions\", \"--no-installable-package\"],\"files\":{\"spec.json\":\""
						+ escapedSpec + "\"}}"});
	}

	@Test
	public void testFromOpenapi() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test").toFile();
		File specFile = new File(tempDir, "spec.json");
		Files.writeString(specFile.toPath(),
				"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");
		File outDir = new File(tempDir, "out");

		runMain(new String[]{"from_openapi", "-h"});
		runMain(new String[]{"from_openapi", "--help"});
		runMain(new String[]{"to_openapi", "--help"});
		runMain(new String[]{"to_docs_json", "--help"});
		runMain(new String[]{"serve_json_rpc", "--help"});
		runMain(new String[]{"sync", "--help"});
		runMain(new String[]{"from_openapi", "unknown_sub", "-i", specFile.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_sdk"});
		runMain(new String[]{"from_openapi", "to_sdk_cli", "-i", specFile.getAbsolutePath(), "-o",
				outDir.getAbsolutePath(), "--tests"});
		runMain(new String[]{"from_openapi", "to_sdk", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath(),
				"--tests"});
		runMain(new String[]{"from_openapi", "to_server", "-i", specFile.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "to_orm", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath(),
				"--no-github-actions", "--no-installable-package"});
		runMain(new String[]{"from_openapi", "to_sdk", "--input-dir", tempDir.getAbsolutePath(), "-o",
				outDir.getAbsolutePath()});
		runMain(new String[]{"from_openapi", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath()});
	}

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

	@Test
	public void testToDocsJson() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test_to_docs").toFile();
		File specFile = new File(tempDir, "spec.json");
		Files.writeString(specFile.toPath(),
				"{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"\",\"version\":\"1\"},\"paths\":{}}");
		File outFile = new File(tempDir, "docs.json");

		runMain(new String[]{"to_docs_json", "-h"});
		runMain(new String[]{"to_docs_json"});
		runMain(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath(), "-o", outFile.getAbsolutePath(),
				"--no-imports", "--no-wrapping"});

		String oldDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", tempDir.getAbsolutePath());
			runMain(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath()});
		} finally {
			System.setProperty("user.dir", oldDir);
		}
	}

	@Test
	public void testServeJsonRpc() throws Exception {
		runMain(new String[]{"serve_json_rpc", "-h"});

		String input = "\n" + "{\"jsonrpc\":\"2.0\",\"method\":\"version\",\"id\":1}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":2}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"id\":3}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/list\",\"id\":31}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"cdd://ast/openapi\"},\"id\":32}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"resources/read\",\"params\":{\"uri\":\"unknown\"},\"id\":33}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"cdd_generate\",\"arguments\":{\"command\":[\"to_openapi\",\"-h\"]}},\"id\":4}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"unknown_tool\"},\"id\":5}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"id\":6}\n"
				+ "{\"jsonrpc\":\"2.0\",\"method\":\"unknown\",\"id\":\"abc\"}\n" + "{\"method\":\"invalid\"}\n"
				+ "invalid_json\n";

		System.setIn(new ByteArrayInputStream(input.getBytes("UTF-8")));
		runMain(new String[]{"serve_json_rpc"});

		String out = outContent.toString();
		assertTrue(out.contains("\"result\":\"0.0.2\""));
	}

	@Test
	public void testSync() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_test_sync").toFile();

		// Copy a single source file to a mocked sync folder structure
		String code = "package test;\npublic class Routes {\n public void handle() {}\n}\n";
		String[] subDirs = {"classes", "orm", "routes", "mocks", "tests", "functions", "cli"};
		for (String sub : subDirs) {
			File d = new File(tempDir, sub);
			d.mkdirs();
			Files.writeString(new File(d, "Test.java").toPath(), code);
		}

		runMain(new String[]{"sync", "-h"});
		runMain(new String[]{"sync"});
		runMain(new String[]{"sync", "-d", tempDir.getAbsolutePath()});
	}

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

			setEnv("CDD_NO_GITHUB_ACTIONS", "1");
			setEnv("CDD_NO_INSTALLABLE_PACKAGE", "1");
			setEnv("CDD_TESTS", "1");
			runMain(new String[]{"from_openapi", "to_sdk"});

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
}
