import org.junit.Test;
import java.util.*;
import openapi.*;
import routes.Emit;

public class RoutesEmitBranchTest {
	@Test
	public void testRoutesEmitBranches() {
		OpenAPI api = new OpenAPI();
		// Base edge case: null paths
		api.paths = null;
		Emit.emit(api, null);

		api.paths = new Paths();
		api.paths.pathItems = null;
		Emit.emit(api, null);

		api.paths.pathItems = new HashMap<>();

		// Single path item with null operations
		PathItem pi = new PathItem();
		api.paths.pathItems.put("/test", pi);
		Emit.emit(api, null);

		// Let's add parameters
		pi.get = new Operation();
		pi.get.operationId = "testOp";
		pi.get.parameters = new ArrayList<>();

		Parameter pNull = new Parameter(); // no schema, no name, etc.
		pi.get.parameters.add(pNull);

		Parameter pQuery = new Parameter();
		pQuery.in = "query";
		pQuery.name = "qp";
		pQuery.schema = new Schema();
		pQuery.schema.type = "array";
		Items it = new Items();
		it.type = "string";
		pQuery.schema.items = it;
		pi.get.parameters.add(pQuery);

		Parameter pPath = new Parameter();
		pPath.in = "path";
		pPath.name = "pp";
		pPath.schema = new Schema();
		pPath.schema.type = "integer";
		pi.get.parameters.add(pPath);

		pi.get.requestBody = new RequestBody();
		((RequestBody) pi.get.requestBody).content = new HashMap<>();
		MediaType mt = new MediaType();
		mt.itemSchema = new HashMap<String, Object>();
		((Map<String, Object>) mt.itemSchema).put("type", "string");
		((RequestBody) pi.get.requestBody).content.put("application/json", mt);

		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		Response resp = new Response();
		pi.get.responses.statusCodes.put("200", resp);

		Emit.emit(api, null);

		// Add multiple operations
		pi.post = new Operation();
		pi.post.operationId = "postOp";
		pi.put = new Operation();
		pi.delete = new Operation();
		pi.patch = new Operation();
		pi.options = new Operation();
		pi.head = new Operation();
		pi.trace = new Operation();
		pi.query = new Operation();

		pi.query.callbacks = new HashMap<>();
		pi.query.callbacks.put("cb", new Callback());

		api.webhooks = new HashMap<>();
		PathItem wh = new PathItem();
		wh.post = new Operation();
		wh.post.requestBody = new RequestBody();
		wh.parameters = new ArrayList<>();
		wh.parameters.add(new Parameter());
		Parameter wp = new Parameter();
		wp.name = "wp";
		wh.parameters.add(wp);
		api.webhooks.put("wh", wh);

		PathItem wh2 = new PathItem();
		wh2.post = new Operation();
		api.webhooks.put("wh2", wh2);

		Emit.emit(api, null);
	}
}
