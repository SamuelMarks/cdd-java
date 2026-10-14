package org.cdd;

import org.junit.Test;
import static org.junit.Assert.*;
import cli.CddCli;
import java.io.File;

/**
 * End-to-end CLI verification tests over complex and minimal OpenAPI specs.
 */
public class SuperMegaCoverageTest {
	/**
	 * Verifies {@code from_openapi} and {@code to_openapi} round-trip CLI commands
	 * exit with code 0 and generate expected artifacts.
	 *
	 * @throws Exception
	 *             If CLI execution fails.
	 */
	@Test
	public void testCoverageFuzz() throws Exception {
		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_sdk", "-i",
				"src/test/resources/super-mega-spec.json", "-o", "target/super-mega-sdk"}));
		assertTrue(new File("target/super-mega-sdk/src/main/java/Sdk.java").exists());

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_server", "-i",
				"src/test/resources/super-mega-spec.json", "-o", "target/super-mega-server"}));
		assertTrue(new File("target/super-mega-server/src/main/java/servermain/Main.java").exists());

		assertEquals(0, CddCli.run(new String[]{"to_openapi", "-i", "target/super-mega-sdk/src/main/java", "-o",
				"target/super-mega-parsed-sdk.json"}));
		assertTrue(new File("target/super-mega-parsed-sdk.json").exists());

		assertEquals(0, CddCli.run(new String[]{"to_openapi", "-i", "target/super-mega-server/src/main/java", "-o",
				"target/super-mega-parsed-server.json"}));
		assertTrue(new File("target/super-mega-parsed-server.json").exists());

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_sdk", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/minimal-sdk"}));
		assertTrue(new File("target/minimal-sdk/src/main/java/Sdk.java").exists());

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_server", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/minimal-server"}));
		assertTrue(new File("target/minimal-server/src/main/java/servermain/Main.java").exists());
	}
}
