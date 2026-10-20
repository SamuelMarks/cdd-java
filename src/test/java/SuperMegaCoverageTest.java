import static org.junit.Assert.*;

import cli.CddCli;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import openapi.OpenAPI;
import openapi.Parse;
import org.junit.Test;

/**
 * End-to-end CLI verification tests over complex and minimal OpenAPI specs.
 */
public class SuperMegaCoverageTest {
	/**
	 * Default constructor.
	 */
	public SuperMegaCoverageTest() {
	}

	/**
	 * Verifies {@code from_openapi} and {@code to_openapi} round-trip CLI commands
	 * exit with code 0 and generate valid Java and OpenAPI artifacts.
	 *
	 * @throws Exception
	 *             If CLI execution fails.
	 */
	@Test
	public void testCoverageFuzz() throws Exception {
		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_sdk", "-i",
				"src/test/resources/super-mega-spec.json", "-o", "target/super-mega-sdk"}));
		File sdkJava = new File("target/super-mega-sdk/src/main/java/Sdk.java");
		assertTrue(sdkJava.exists());
		String sdkContent = Files.readString(sdkJava.toPath(), StandardCharsets.UTF_8);
		assertTrue(sdkContent.contains("class "));

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_server", "-i",
				"src/test/resources/super-mega-spec.json", "-o", "target/super-mega-server"}));
		File serverMain = new File("target/super-mega-server/src/main/java/servermain/Main.java");
		assertTrue(serverMain.exists());
		String serverMainContent = Files.readString(serverMain.toPath(), StandardCharsets.UTF_8);
		assertTrue(serverMainContent.contains("class Main"));

		assertEquals(0, CddCli.run(new String[]{"to_openapi", "-i", "target/super-mega-sdk/src/main/java", "-o",
				"target/super-mega-parsed-sdk.json"}));
		File parsedSdkJson = new File("target/super-mega-parsed-sdk.json");
		assertTrue(parsedSdkJson.exists());
		OpenAPI parsedSdkApi = Parse.fromFile(parsedSdkJson);
		assertNotNull(parsedSdkApi.info);
		assertNotNull(parsedSdkApi.paths);
		assertFalse(parsedSdkApi.paths.pathItems.isEmpty());

		assertEquals(0, CddCli.run(new String[]{"to_openapi", "-i", "target/super-mega-server/src/main/java", "-o",
				"target/super-mega-parsed-server.json"}));
		File parsedServerJson = new File("target/super-mega-parsed-server.json");
		assertTrue(parsedServerJson.exists());
		OpenAPI parsedServerApi = Parse.fromFile(parsedServerJson);
		assertNotNull(parsedServerApi.paths);

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_sdk", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/minimal-sdk"}));
		File minSdk = new File("target/minimal-sdk/src/main/java/Sdk.java");
		assertTrue(minSdk.exists());
		assertTrue(Files.readString(minSdk.toPath(), StandardCharsets.UTF_8).contains("class "));

		assertEquals(0, CddCli.run(new String[]{"from_openapi", "to_server", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/minimal-server"}));
		File minServer = new File("target/minimal-server/src/main/java/servermain/Main.java");
		assertTrue(minServer.exists());
		assertTrue(Files.readString(minServer.toPath(), StandardCharsets.UTF_8).contains("class Main"));
	}
}
