import static org.junit.Assert.*;

import cli.Emit;
import java.util.*;
import openapi.*;
import org.junit.Test;

/**
 * Branch coverage and output verification unit tests for {@link cli.Emit}.
 */
public class EmitBranchTest {
	/**
	 * Default constructor.
	 */
	public EmitBranchTest() {
	}

	/**
	 * Verifies null, empty, and non-matching branches in {@link cli.Emit#emitCli}
	 * with assertions on generated CLI code.
	 */
	@Test
	public void testEmitBranches() {
		OpenAPI api = new OpenAPI();
		api.paths = null;
		String c1 = Emit.emitCli(api);
		assertTrue(c1.contains("public class SdkCli"));

		api.paths = new Paths();
		api.paths.pathItems = null;
		String c2 = Emit.emitCli(api);
		assertTrue(c2.contains("printHelp()"));

		api.paths.pathItems = new HashMap<>();
		String c3 = Emit.emitCli(api);
		assertFalse(c3.contains("Operation:"));

		Operation op = new Operation();
		op.requestBody = null;
		assertFalse(Emit.emitCli(wrap(op)).contains("--requestBody"));

		op.requestBody = ReferenceOr.ofRef("#/components/requestBodies/Ref");
		assertFalse(Emit.emitCli(wrap(op)).contains("--requestBody"));

		op.requestBody = ReferenceOr.ofValue(new RequestBody());
		assertTrue(Emit.emitCli(wrap(op)).contains("--requestBody"));

		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		MediaType mt = new MediaType();
		rb.content.put("json", mt);
		op.requestBody = ReferenceOr.ofValue(rb);

		mt.itemSchema = null;
		assertFalse(Emit.emitCli(wrap(op)).contains("RequestBodyContentItemSchema"));

		mt.itemSchema = ReferenceOr.ofRef("#/components/schemas/Ref");
		assertFalse(Emit.emitCli(wrap(op)).contains("RequestBodyContentItemSchema"));

		mt.itemSchema = ReferenceOr.ofValue(new Schema());
		assertFalse(Emit.emitCli(wrap(op)).contains("RequestBodyContentItemSchema json"));

		op.responses = null;
		assertFalse(Emit.emitCli(wrap(op)).contains("Returns "));

		op.responses = new Responses();
		op.responses.statusCodes = null;
		assertFalse(Emit.emitCli(wrap(op)).contains("Returns 200"));

		op.responses.statusCodes = new HashMap<>();
		assertFalse(Emit.emitCli(wrap(op)).contains("Returns 200"));

		op.responses.defaultResponse = null;
		assertFalse(Emit.emitCli(wrap(op)).contains("Returns default:"));

		op.responses.defaultResponse = ReferenceOr.ofRef("#/components/responses/Ref");
		assertFalse(Emit.emitCli(wrap(op)).contains("Returns default:"));

		op.responses.defaultResponse = ReferenceOr.ofValue(new Response());
		assertTrue(Emit.emitCli(wrap(op)).contains("Returns default:"));

		Response r = new Response();
		r.headers = new HashMap<>();
		Header h = new Header();
		r.headers.put("h1", ReferenceOr.ofValue(h));
		op.responses.defaultResponse = ReferenceOr.ofValue(r);

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
