import org.junit.Test;
import java.util.*;
import cli.Main;

public class MainBranchTest {
	@Test
	public void testMainBranches() throws Exception {
		// hasFlag true through argument
		Main.main(new String[]{"from_openapi", "to_sdk", "--no-github-actions", "--no-installable-package", "--tests",
				"-i", "src/test/resources/minimal-spec.json", "-o", "target/tmp_1"});

		// from_openapi missing subCommand
		Main.main(new String[]{"from_openapi", "--no-github-actions", "-i", "src/test/resources/minimal-spec.json",
				"-o", "target/tmp_3"});

		// --input-dir test
		Main.main(new String[]{"from_openapi", "to_sdk", "--input-dir", "src/test/resources", "-o", "target/tmp_4"});

		// to_sdk --tests with minimal-empty-spec.json
		Main.main(new String[]{"from_openapi", "to_sdk", "--tests", "-i", "src/test/resources/minimal-empty-spec.json",
				"-o", "target/tmp_5"});
		Main.main(new String[]{"from_openapi", "to_sdk_cli", "--tests", "-i",
				"src/test/resources/minimal-empty-spec.json", "-o", "target/tmp_6"});
		Main.main(new String[]{"from_openapi", "to_sdk", "--tests", "-i", "src/test/resources/empty-title-spec.json",
				"-o", "target/tmp_7"});

		// CDD_WASI_VIRTUAL_ROOT
		Main.main(new String[]{"from_openapi", "to_sdk", "-i", "src/test/resources/minimal-spec.json", "-o",
				"target/tmp_out_2"}); // does not start with /
	}

	private void setEnv(String key, String value) {
		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			java.lang.reflect.Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			if (value == null)
				writableEnv.remove(key);
			else
				writableEnv.put(key, value);
		} catch (Exception e) {
		}
	}
}
