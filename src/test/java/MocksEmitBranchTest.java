import org.junit.Test;
import openapi.OpenAPI;
import openapi.Paths;

public class MocksEmitBranchTest {
	@Test
	public void testMocksEmitBranches() {
		OpenAPI api = new OpenAPI();
		mocks.Emit.emitModular(api); // paths == null

		api.paths = new Paths();
		api.paths.pathItems = null;
		mocks.Emit.emitModular(api); // paths != null, pathItems == null

		api.paths.pathItems = new java.util.HashMap<>();
		mocks.Emit.emitModular(api); // paths != null, pathItems != null
	}
}
