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
}
