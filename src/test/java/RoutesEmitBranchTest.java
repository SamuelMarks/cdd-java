import static org.junit.Assert.*;

import java.util.*;
import openapi.*;
import org.junit.Test;
import routes.Emit;

/**
 * Branch coverage and output verification unit tests for {@link routes.Emit}.
 */
public class RoutesEmitBranchTest {
	/**
	 * Default constructor.
	 */
	public RoutesEmitBranchTest() {
	}

	/**
	 * Verifies {@link routes.Emit#emit} across sparse, null, and multi-verb path
	 * items and webhooks with concrete assertions on emitted source code.
	 */
	@Test
	public void testRoutesEmitBranches() {
		OpenAPI api = new OpenAPI();
		api.paths = null;
		String outNullPaths = Emit.emit(api, null);
		assertTrue(outNullPaths.contains("class ApiClient"));

		api.paths = new Paths();
		api.paths.pathItems = null;
		String outNullPathItems = Emit.emit(api, null);
		assertTrue(outNullPathItems.contains("class ApiClient"));

		api.paths.pathItems = new HashMap<>();

		PathItem pi = new PathItem();
		api.paths.pathItems.put("/test", pi);
		String outEmptyPi = Emit.emit(api, null);
		assertTrue(outEmptyPi.contains("class ApiClient"));

		pi.get = new Operation();
		pi.get.operationId = "testOp";
		pi.get.parameters = new ArrayList<>();

		Parameter pNull = new Parameter();
		pi.get.parameters.add(ReferenceOr.ofValue(pNull));

		Parameter pQuery = new Parameter();
		pQuery.in = "query";
		pQuery.name = "qp";
		Schema pQuerySchema = new Schema();
		pQuerySchema.type = SchemaType.ofSingle("array");
		Schema it = new Schema();
		it.type = SchemaType.ofSingle("string");
		pQuerySchema.items = ReferenceOr.ofValue(it);
		pQuery.schema = ReferenceOr.ofValue(pQuerySchema);
		pi.get.parameters.add(ReferenceOr.ofValue(pQuery));

		Parameter pPath = new Parameter();
		pPath.in = "path";
		pPath.name = "pp";
		Schema pPathSchema = new Schema();
		pPathSchema.type = SchemaType.ofSingle("integer");
		pPath.schema = ReferenceOr.ofValue(pPathSchema);
		pi.get.parameters.add(ReferenceOr.ofValue(pPath));

		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		MediaType mt = new MediaType();
		Schema mtItemSchema = new Schema();
		mtItemSchema.type = SchemaType.ofSingle("string");
		mt.itemSchema = ReferenceOr.ofValue(mtItemSchema);
		rb.content.put("application/json", mt);
		pi.get.requestBody = ReferenceOr.ofValue(rb);

		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		Response resp = new Response();
		pi.get.responses.statusCodes.put("200", ReferenceOr.ofValue(resp));
		pi.get.responses.statusCodes.put("400", ReferenceOr.ofRef("#/components/responses/BadReq"));

		String outSingleGet = Emit.emit(api, null);
		assertTrue(outSingleGet.contains("public HttpResponse<String> testOp("));
		assertTrue(outSingleGet.contains("@requestBodyContentItemSchema application/json string"));

		pi.post = new Operation();
		pi.post.operationId = "postOp";
		pi.post.requestBody = ReferenceOr.ofRef("#/components/requestBodies/PostBody");
		pi.put = new Operation();
		pi.delete = new Operation();
		pi.patch = new Operation();
		pi.options = new Operation();
		pi.head = new Operation();
		pi.trace = new Operation();
		pi.query = new Operation();

		pi.query.callbacks = new HashMap<>();
		pi.query.callbacks.put("cb", ReferenceOr.ofValue(new Callback()));

		api.webhooks = new HashMap<>();
		PathItem wh = new PathItem();
		wh.post = new Operation();
		wh.post.requestBody = ReferenceOr.ofValue(new RequestBody());
		wh.parameters = new ArrayList<>();
		wh.parameters.add(ReferenceOr.ofValue(new Parameter()));
		Parameter wp = new Parameter();
		wp.name = "wp";
		wh.parameters.add(ReferenceOr.ofValue(wp));
		api.webhooks.put("wh", wh);

		PathItem wh2 = new PathItem();
		wh2.post = new Operation();
		wh2.parameters = Arrays.asList(ReferenceOr.ofValue(new Parameter()), ReferenceOr.ofValue(wp));
		api.webhooks.put("wh2", wh2);

		String result = Emit.emit(api, null);
		assertTrue(result.contains(
				"public void mcpSse(String toolName, java.util.Map<String, Object> arguments, java.util.Map<String, String> headers)"));
		assertTrue(result.contains("Webhook"));
		String reResult = Emit.emit(api, result);
		assertTrue(reResult.contains("Webhook"));

		OpenAPI edgeApi = new OpenAPI();
		edgeApi.paths = new Paths();
		edgeApi.paths.pathItems = new HashMap<>();
		PathItem optionsOnlyPi = new PathItem();
		Operation optOp = new Operation();
		Callback optCb = new Callback();
		optCb.pathItems = Collections.singletonMap("/cb", new PathItem());
		optOp.callbacks = Collections.singletonMap("optCb", ReferenceOr.ofValue(optCb));
		optionsOnlyPi.options = optOp;
		edgeApi.paths.pathItems.put("/optonly", optionsOnlyPi);
		PathItem queryOnlyPi = new PathItem();
		queryOnlyPi.ref = "";
		queryOnlyPi.summary = "";
		queryOnlyPi.description = "";
		Parameter sharedP = new Parameter();
		sharedP.name = "shared";
		sharedP.in = "query";
		queryOnlyPi.parameters = Collections.singletonList(ReferenceOr.ofValue(sharedP));
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
		qOp.parameters = Arrays.asList(ReferenceOr.ofValue(opP1), ReferenceOr.ofValue(opP2), ReferenceOr.ofValue(opP3));
		Callback qCb = new Callback();
		qCb.pathItems = Collections.singletonMap("/cb", new PathItem());
		qOp.callbacks = Collections.singletonMap("qCb", ReferenceOr.ofValue(qCb));

		SecurityRequirement srNullReqs = new SecurityRequirement();
		srNullReqs.requirements = null;
		SecurityRequirement srEdge = new SecurityRequirement();
		srEdge.requirements = new HashMap<>();
		srEdge.requirements.put("apiKeyNoName", Collections.emptyList());
		srEdge.requirements.put("apiKeyNoIn", Collections.emptyList());
		srEdge.requirements.put("httpOther", Collections.emptyList());
		srEdge.requirements.put("otherType", Collections.emptyList());
		qOp.security = Arrays.asList(srNullReqs, srEdge);

		queryOnlyPi.query = qOp;
		edgeApi.paths.pathItems.put("/qonly", queryOnlyPi);
		String edgeNoComp = Emit.emit(edgeApi, null);
		assertTrue(edgeNoComp.contains("queryqonly"));
		edgeApi.components = new Components();
		edgeApi.components.securitySchemes = null;
		String edgeNullSec = Emit.emit(edgeApi, null);
		assertTrue(edgeNullSec.contains("optionsoptonly"));

		edgeApi.components.securitySchemes = new HashMap<>();
		SecurityScheme sNoName = new SecurityScheme();
		sNoName.type = "apiKey";
		sNoName.in = "header";
		edgeApi.components.securitySchemes.put("apiKeyNoName", ReferenceOr.ofValue(sNoName));
		SecurityScheme sNoIn = new SecurityScheme();
		sNoIn.type = "apiKey";
		sNoIn.name = "X-Key";
		edgeApi.components.securitySchemes.put("apiKeyNoIn", ReferenceOr.ofValue(sNoIn));
		SecurityScheme sHttpOther = new SecurityScheme();
		sHttpOther.type = "http";
		sHttpOther.scheme = "digest";
		edgeApi.components.securitySchemes.put("httpOther", ReferenceOr.ofValue(sHttpOther));
		SecurityScheme sOtherType = new SecurityScheme();
		sOtherType.type = "oauth2";
		edgeApi.components.securitySchemes.put("otherType", ReferenceOr.ofValue(sOtherType));

		RequestBody edgeRb = new RequestBody();
		edgeRb.content = new HashMap<>();
		MediaType edgeMt = new MediaType();
		edgeMt.schema = ReferenceOr.ofValue(new Schema());
		edgeMt.itemSchema = ReferenceOr.ofValue(new Schema());
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
		MediaType refMt = new MediaType();
		refMt.schema = ReferenceOr.ofRef("#/components/schemas/RefSchema");
		refMt.itemSchema = ReferenceOr.ofRef("#/components/schemas/RefItemSchema");
		edgeRb.content.put("application/xml", refMt);
		qOp.requestBody = ReferenceOr.ofValue(edgeRb);

		String edgeFull = Emit.emit(edgeApi, null);
		assertTrue(edgeFull.contains("@requestBodyContent application/json"));
		assertTrue(edgeFull.contains("@callback qCb /cb QUERY"));
	}
}
