import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import openapi.*;
import cli.Emit;

/**
 * Branch coverage unit tests for {@link cli.Emit}.
 */
public class EmitBranchTest {
	/**
	 * Verifies null, empty, and non-matching branches in {@link cli.Emit#emitCli}.
	 */
	@Test
	public void testEmitBranches() {
		// 1. api.paths == null
		OpenAPI api = new OpenAPI();
		api.paths = null;
		assertNotNull(Emit.emitCli(api));

		// 2. api.paths != null, pathItems == null
		api.paths = new Paths();
		api.paths.pathItems = null;
		assertNotNull(Emit.emitCli(api));

		// 3. api.paths != null, pathItems != null
		api.paths.pathItems = new HashMap<>();
		assertNotNull(Emit.emitCli(api));

		// op.requestBody != null && op.requestBody instanceof RequestBody
		Operation op = new Operation();
		op.requestBody = null;
		assertNotNull(Emit.emitCli(wrap(op)));

		op.requestBody = new Object(); // not RequestBody
		assertNotNull(Emit.emitCli(wrap(op)));

		op.requestBody = new RequestBody(); // is RequestBody
		assertTrue(Emit.emitCli(wrap(op)).contains("--requestBody"));

		// mt.itemSchema != null && mt.itemSchema instanceof Map
		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		MediaType mt = new MediaType();
		rb.content.put("json", mt);
		op.requestBody = rb;

		mt.itemSchema = null;
		assertNotNull(Emit.emitCli(wrap(op)));

		mt.itemSchema = new Object(); // not map
		assertNotNull(Emit.emitCli(wrap(op)));

		mt.itemSchema = new HashMap<>(); // is map
		assertNotNull(Emit.emitCli(wrap(op)));

		// op.responses != null && op.responses.statusCodes != null
		op.responses = null;
		assertNotNull(Emit.emitCli(wrap(op)));

		op.responses = new Responses();
		op.responses.statusCodes = null;
		assertNotNull(Emit.emitCli(wrap(op)));

		op.responses.statusCodes = new HashMap<>();
		assertNotNull(Emit.emitCli(wrap(op)));

		// op.responses != null && op.responses.defaultResponse != null &&
		// op.responses.defaultResponse instanceof Response
		op.responses.defaultResponse = null;
		assertNotNull(Emit.emitCli(wrap(op)));

		op.responses.defaultResponse = new Object();
		assertNotNull(Emit.emitCli(wrap(op)));

		op.responses.defaultResponse = new Response();
		assertTrue(Emit.emitCli(wrap(op)).contains("Returns default:"));

		// h.required != null && h.required, h.deprecated != null &&
		// h.deprecated
		Response r = new Response();
		r.headers = new HashMap<>();
		Header h = new Header();
		r.headers.put("h1", h);
		op.responses.defaultResponse = r;

		h.required = null;
		h.deprecated = null;
		assertTrue(Emit.emitCli(wrap(op)).contains("Header h1:"));

		h.required = false;
		h.deprecated = false;
		assertTrue(Emit.emitCli(wrap(op)).contains("Header h1:"));

		h.required = true;
		h.deprecated = true;
		assertTrue(Emit.emitCli(wrap(op)).contains("Header h1 (required) [DEPRECATED]:"));
	}

	/**
	 * Wraps an {@link Operation} inside a single-path {@link OpenAPI} model.
	 *
	 * @param op
	 *            The operation to wrap.
	 * @return The wrapping {@link OpenAPI} model.
	 */
	private OpenAPI wrap(Operation op) {
		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		pi.get = op;
		api.paths.pathItems.put("/test", pi);
		return api;
	}
}
