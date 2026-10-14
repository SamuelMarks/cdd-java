import org.junit.Test;
import java.util.*;
import openapi.*;
import routes.Emit;

/**
 * Branch coverage unit tests for {@link routes.Emit}.
 */
public class RoutesEmitBranchTest {
	/**
	 * Verifies {@link routes.Emit#emit} across sparse, null, and multi-verb path
	 * items and webhooks.
	 */
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
		wh2.parameters = Arrays.asList(new Parameter(), wp);
		api.webhooks.put("wh2", wh2);

		String result = Emit.emit(api, null);
		org.junit.Assert.assertTrue(result.contains(
				"public void mcpSse(String toolName, java.util.Map<String, Object> arguments, java.util.Map<String, String> headers)"));
		// Re-emit into existing result to cover !hasMember(classDecl, "mcpSse") and
		// !hasMember(classDecl, interfaceName) false branches
		String reResult = Emit.emit(api, result);
		org.junit.Assert.assertNotNull(reResult);

		// Cover query-only and options-only callback items, empty
		// ref/summary/description, duplicate parameter in/name, and security scheme
		// edge cases
		OpenAPI edgeApi = new OpenAPI();
		edgeApi.paths = new Paths();
		edgeApi.paths.pathItems = new HashMap<>();
		PathItem optionsOnlyPi = new PathItem();
		Operation optOp = new Operation();
		Callback optCb = new Callback();
		optCb.pathItems = Collections.singletonMap("/cb", new PathItem());
		optOp.callbacks = Collections.singletonMap("optCb", optCb);
		optionsOnlyPi.options = optOp;
		edgeApi.paths.pathItems.put("/optonly", optionsOnlyPi);
		PathItem queryOnlyPi = new PathItem();
		queryOnlyPi.ref = "";
		queryOnlyPi.summary = "";
		queryOnlyPi.description = "";
		Parameter sharedP = new Parameter();
		sharedP.name = "shared";
		sharedP.in = "query";
		queryOnlyPi.parameters = Collections.singletonList(sharedP);
		Operation qOp = new Operation();
		Parameter opP1 = new Parameter();
		opP1.name = "shared";
		opP1.in = "query";
		Parameter opP2 = new Parameter();
		opP2.name = "shared";
		opP2.in = "header";
		Parameter opP3 = new Parameter();
		opP3.name = "other";
		opP3.in = "query";
		qOp.parameters = Arrays.asList(opP1, opP2, opP3);
		Callback qCb = new Callback();
		qCb.pathItems = Collections.singletonMap("/cb", new PathItem());
		qOp.callbacks = Collections.singletonMap("qCb", qCb);

		SecurityRequirement srNullReqs = new SecurityRequirement();
		srNullReqs.requirements = null;
		SecurityRequirement srEdge = new SecurityRequirement();
		srEdge.requirements = new HashMap<>();
		srEdge.requirements.put("apiKeyNoName", Collections.emptyList());
		srEdge.requirements.put("apiKeyNoIn", Collections.emptyList());
		srEdge.requirements.put("httpOther", Collections.emptyList());
		srEdge.requirements.put("otherType", Collections.emptyList());
		qOp.security = Arrays.asList(srNullReqs, srEdge);

		// Also test secReqs != null when model.components == null, and when
		// components.securitySchemes == null
		queryOnlyPi.query = qOp;
		edgeApi.paths.pathItems.put("/qonly", queryOnlyPi);
		Emit.emit(edgeApi, null);
		edgeApi.components = new Components();
		edgeApi.components.securitySchemes = null;
		Emit.emit(edgeApi, null);

		edgeApi.components.securitySchemes = new HashMap<>();
		SecurityScheme sNoName = new SecurityScheme();
		sNoName.type = "apiKey";
		sNoName.in = "header";
		edgeApi.components.securitySchemes.put("apiKeyNoName", sNoName);
		SecurityScheme sNoIn = new SecurityScheme();
		sNoIn.type = "apiKey";
		sNoIn.name = "X-Key";
		edgeApi.components.securitySchemes.put("apiKeyNoIn", sNoIn);
		SecurityScheme sHttpOther = new SecurityScheme();
		sHttpOther.type = "http";
		sHttpOther.scheme = "digest";
		edgeApi.components.securitySchemes.put("httpOther", sHttpOther);
		SecurityScheme sOtherType = new SecurityScheme();
		sOtherType.type = "oauth2";
		edgeApi.components.securitySchemes.put("otherType", sOtherType);

		RequestBody edgeRb = new RequestBody();
		edgeRb.content = new HashMap<>();
		MediaType edgeMt = new MediaType();
		edgeMt.schema = new HashMap<String, Object>();
		edgeMt.itemSchema = new HashMap<String, Object>();
		Encoding nullCtEnc = new Encoding();
		edgeMt.prefixEncoding = Collections.singletonList(nullCtEnc);
		edgeMt.itemEncoding = new Encoding();
		Encoding mapEnc = new Encoding();
		mapEnc.prefixEncoding = Collections.singletonList(new Encoding());
		mapEnc.itemEncoding = new Encoding();
		Map<String, Encoding> encMap = new HashMap<>();
		encMap.put("e", mapEnc);
		encMap.put("eSparse", new Encoding());
		edgeMt.encoding = encMap;
		edgeRb.content.put("application/json", edgeMt);
		qOp.requestBody = edgeRb;

		Emit.emit(edgeApi, null);
	}
}
