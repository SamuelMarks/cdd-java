import org.junit.Test;
import java.util.*;
import openapi.*;
import cli.Emit;

public class EmitBranchTest {
	@Test
	public void testEmitBranches() {
		// 1. api.paths == null
		OpenAPI api = new OpenAPI();
		api.paths = null;
		Emit.emitCli(api);

		// 2. api.paths != null, pathItems == null
		api.paths = new Paths();
		api.paths.pathItems = null;
		Emit.emitCli(api);

		// 3. api.paths != null, pathItems != null
		api.paths.pathItems = new HashMap<>();
		Emit.emitCli(api);

		// Let's do the same for op.requestBody (Line 447)
		// op.requestBody != null && op.requestBody instanceof RequestBody
		Operation op = new Operation();
		op.requestBody = null;
		Emit.emitCli(wrap(op));

		op.requestBody = new Object(); // not RequestBody
		Emit.emitCli(wrap(op));

		op.requestBody = new RequestBody(); // is RequestBody
		Emit.emitCli(wrap(op));

		// Line 463: mt.itemSchema != null && mt.itemSchema instanceof Map
		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		MediaType mt = new MediaType();
		rb.content.put("json", mt);
		op.requestBody = rb;

		mt.itemSchema = null;
		Emit.emitCli(wrap(op));

		mt.itemSchema = new Object(); // not map
		Emit.emitCli(wrap(op));

		mt.itemSchema = new HashMap<>(); // is map
		Emit.emitCli(wrap(op));

		// Line 507: op.responses != null && op.responses.statusCodes != null
		op.responses = null;
		Emit.emitCli(wrap(op));

		op.responses = new Responses();
		op.responses.statusCodes = null;
		Emit.emitCli(wrap(op));

		op.responses.statusCodes = new HashMap<>();
		Emit.emitCli(wrap(op));

		// Line 559: op.responses != null && op.responses.defaultResponse != null &&
		// op.responses.defaultResponse instanceof Response
		op.responses.defaultResponse = null;
		Emit.emitCli(wrap(op));

		op.responses.defaultResponse = new Object();
		Emit.emitCli(wrap(op));

		op.responses.defaultResponse = new Response();
		Emit.emitCli(wrap(op));

		// Line 570, 571: h.required != null && h.required, h.deprecated != null &&
		// h.deprecated
		Response r = new Response();
		r.headers = new HashMap<>();
		Header h = new Header();
		r.headers.put("h1", h);
		op.responses.defaultResponse = r;

		h.required = null;
		h.deprecated = null;
		Emit.emitCli(wrap(op));

		h.required = false;
		h.deprecated = false;
		Emit.emitCli(wrap(op));

		h.required = true;
		h.deprecated = true;
		Emit.emitCli(wrap(op));
	}

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
