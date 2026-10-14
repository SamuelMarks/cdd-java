import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import openapi.OpenAPI;
import openapi.Paths;

/**
 * Branch coverage unit tests for {@link mocks.Emit#emitModular}.
 */
public class MocksEmitBranchTest {
	/**
	 * Verifies {@link mocks.Emit#emitModular} with null and empty path maps.
	 */
	@Test
	public void testMocksEmitBranches() {
		OpenAPI api = new OpenAPI();
		Map<String, String> out1 = mocks.Emit.emitModular(api); // paths == null
		assertTrue(out1.isEmpty());

		api.paths = new Paths();
		api.paths.pathItems = null;
		Map<String, String> out2 = mocks.Emit.emitModular(api); // paths != null, pathItems == null
		assertTrue(out2.isEmpty());

		api.paths.pathItems = new java.util.HashMap<>();
		Map<String, String> out3 = mocks.Emit.emitModular(api); // paths != null, pathItems != null
		assertTrue(out3.isEmpty());
	}
}
