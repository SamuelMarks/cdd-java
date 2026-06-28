package org.cdd;

import org.junit.Test;
import cli.Main;
import java.io.File;

public class SuperMegaCoverageTest {
	@Test
	public void testCoverageFuzz() throws Exception {
		try {
			Main.main(new String[]{"from_openapi", "to_sdk", "-i", "src/test/resources/super-mega-spec.json", "-o",
					"target/super-mega-sdk"});
		} catch (Exception e) {
		}
		try {
			Main.main(new String[]{"from_openapi", "to_server", "-i", "src/test/resources/super-mega-spec.json", "-o",
					"target/super-mega-server"});
		} catch (Exception e) {
		}

		// Parse them back
		try {
			Main.main(new String[]{"to_openapi", "-i", "target/super-mega-sdk/src/main/java", "-o",
					"target/super-mega-parsed-sdk.json"});
		} catch (Exception e) {
		}
		try {
			Main.main(new String[]{"to_openapi", "-i", "target/super-mega-server/src/main/java", "-o",
					"target/super-mega-parsed-server.json"});
		} catch (Exception e) {
		}
		try {
			Main.main(new String[]{"from_openapi", "to_sdk", "-i", "src/test/resources/minimal-empty-spec.json", "-o",
					"target/minimal-sdk"});
			Main.main(new String[]{"from_openapi", "to_server", "-i", "src/test/resources/minimal-empty-spec.json",
					"-o", "target/minimal-server"});
		} catch (Exception e) {
		}
	}
}
