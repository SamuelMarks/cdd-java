import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import cli.Main;

/**
 * Branch coverage unit tests for {@link Main} and {@link cli.CddCli}.
 */
public class MainBranchTest {
	/**
	 * Verifies CLI command variations and exit code handling on {@link Main#main}.
	 *
	 * @throws Exception
	 *             If CLI execution fails.
	 */
	@Test
	public void testMainBranches() throws Exception {
		// hasFlag true through argument
		Main.main(new String[]{"from_openapi", "to_sdk", "--no-github-actions", "--no-installable-package", "--tests",
				"-i", "src/test/resources/minimal-spec.json", "-o", "target/tmp_1"});
		assertTrue(new File("target/tmp_1/src/main/java/Sdk.java").exists());

		// from_openapi missing subCommand
		Main.main(new String[]{"from_openapi", "--no-github-actions", "-i", "src/test/resources/minimal-spec.json",
				"-o", "target/tmp_3"});
		assertTrue(new File("target/tmp_3/src/main/java/Sdk.java").exists());

		// --input-dir test
		Main.main(new String[]{"from_openapi", "to_sdk", "--input-dir", "src/test/resources", "-o", "target/tmp_4"});
		assertTrue(new File("target/tmp_4/src/main/java/Sdk.java").exists());

		// to_sdk --tests with minimal-empty-spec.json
		Main.main(new String[]{"from_openapi", "to_sdk", "--tests", "-i", "src/test/resources/minimal-empty-spec.json",
				"-o", "target/tmp_5"});
		assertTrue(new File("target/tmp_5/src/test/java/ApiIntegrationTest.java").exists());

		Main.main(new String[]{"from_openapi", "to_sdk_cli", "--tests", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/tmp_6"});
		assertTrue(new File("target/tmp_6/SdkCli.java").exists());

		Main.main(new String[]{"from_openapi", "to_sdk", "--tests", "-i", "src/test/resources/empty-title-spec.json",
				"-o", "target/tmp_7"});
		assertTrue(new File("target/tmp_7/src/test/java/ApiIntegrationTest.java").exists());

		// CDD_WASI_VIRTUAL_ROOT
		Main.main(new String[]{"from_openapi", "to_sdk", "-i", "src/test/resources/minimal-spec.json", "-o",
				"target/tmp_out_2"});
		assertTrue(new File("target/tmp_out_2/src/main/java/Sdk.java").exists());

		// Test Main exitHandler and CliContext
		java.util.function.Consumer<Integer> prevExit = Main.exitHandler;
		final int[] exitCode = new int[]{-1};
		try {
			Main.exitHandler = code -> exitCode[0] = code;
			Main.main(new String[]{"unknown_command"});
			assertEquals(1, exitCode[0]);
			cli.CliContext defaultCtx = cli.CliContext.createDefault();
			assertNotNull(defaultCtx.envProvider);
			assertNotNull(defaultCtx.workspaceDir);
			assertNotNull(defaultCtx.exitHandler);
		} finally {
			Main.exitHandler = prevExit;
		}
	}

	/**
	 * Verifies CLI smoke tests for {@code from_openapi to_sdk --tests} and
	 * {@code to_openapi} roundtrip execution via the generated launcher script.
	 *
	 * @throws Exception
	 *             If building or executing the launcher script fails.
	 */
	@Test
	public void testLauncherScriptSmokeRoundtrip() throws Exception {
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		File launcher = new File(isWindows ? "bin/cdd-java.bat" : "bin/cdd-java");
		if (!launcher.exists() || !new File("bin/cli/Main.class").exists()) {
			ProcessBuilder buildPb = isWindows
					? new ProcessBuilder("cmd.exe", "/c", "make.bat", "build")
					: new ProcessBuilder("make", "build");
			buildPb.inheritIO();
			assertEquals(0, buildPb.start().waitFor());
		}
		assertTrue("Launcher script must exist in bin/", launcher.exists());
		File smokeSpec = new File("target/launcher_smoke_input.json");
		smokeSpec.getParentFile().mkdirs();
		java.nio.file.Files.writeString(smokeSpec.toPath(),
				"{\"openapi\":\"3.2.0\",\"info\":{\"title\":\"SmokeApi\",\"version\":\"1.0.0\"},"
						+ "\"paths\":{\"/pets\":{\"get\":{\"operationId\":\"listPets\",\"responses\":{\"200\":{\"description\":\"ok\","
						+ "\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"}}}}}}}},"
						+ "\"components\":{\"schemas\":{\"Pet\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"}}}}}}",
				java.nio.charset.StandardCharsets.UTF_8);
		File sdkDir = new File("target/launcher_smoke_sdk");
		File roundtripSpec = new File("target/launcher_smoke_openapi.json");
		if (roundtripSpec.exists()) {
			roundtripSpec.delete();
		}

		ProcessBuilder sdkPb = isWindows
				? new ProcessBuilder("cmd.exe", "/c", launcher.getAbsolutePath(), "from_openapi", "to_sdk", "--tests",
						"-i", smokeSpec.getPath(), "-o", sdkDir.getPath())
				: new ProcessBuilder(launcher.getAbsolutePath(), "from_openapi", "to_sdk", "--tests", "-i",
						smokeSpec.getPath(), "-o", sdkDir.getPath());
		sdkPb.inheritIO();
		assertEquals(0, sdkPb.start().waitFor());
		assertTrue(new File(sdkDir, "src/main/java/Sdk.java").exists());
		assertTrue(new File(sdkDir, "src/test/java/SmokeApiIntegrationTest.java").exists());
		assertTrue(new File(sdkDir, "src/test/java/SmokeApiMockServer.java").exists());

		ProcessBuilder toOpenApiPb = isWindows
				? new ProcessBuilder("cmd.exe", "/c", launcher.getAbsolutePath(), "to_openapi", "-i", sdkDir.getPath(),
						"-o", roundtripSpec.getPath())
				: new ProcessBuilder(launcher.getAbsolutePath(), "to_openapi", "-i", sdkDir.getPath(), "-o",
						roundtripSpec.getPath());
		toOpenApiPb.inheritIO();
		assertEquals(0, toOpenApiPb.start().waitFor());
		assertTrue(roundtripSpec.exists());
		openapi.OpenAPI parsedRoundtrip = openapi.Parse.fromFile(roundtripSpec);
		assertNotNull(parsedRoundtrip);
		assertNotNull(parsedRoundtrip.paths);
		assertTrue(parsedRoundtrip.paths.pathItems.containsKey("/pets"));
		assertNotNull(parsedRoundtrip.components);
		assertTrue(parsedRoundtrip.components.schemas.containsKey("Pet"));
	}
}
