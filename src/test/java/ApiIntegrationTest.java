import org.junit.Test;

/**
 * Integration test suite verifying end-to-end parse and emit execution via
 * {@link TestRunner}.
 */
public class ApiIntegrationTest {
	/**
	 * Executes the full integration verification suite.
	 *
	 * @throws Exception
	 *             If any integration check fails.
	 */
	@Test
	public void runAll() throws Exception {
		TestRunner.main(new String[0]);
	}
}
