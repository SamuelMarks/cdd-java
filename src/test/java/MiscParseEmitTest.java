import org.junit.Test;
import static org.junit.Assert.*;

import openapi.*;

/**
 * Unit tests for {@code tests}, {@code docstrings}, {@code mocks},
 * {@code functions}, and {@link openapi.TypeMapper}.
 */
public class MiscParseEmitTest {

	/**
	 * Verifies {@link tests.Parse#parse}.
	 */
	@Test
	public void testTestsParse() {
		tests.Parse parser = new tests.Parse();
		String source = "class MyIntegrationTest { void test_myOp() {} void test_myOp() {} }";
		OpenAPI api = tests.Parse.parse(source);
		assertNotNull(api);
		assertEquals(1, api.paths.pathItems.size());
		assertTrue(api.paths.pathItems.containsKey("/myOp"));
		assertEquals("myOp", api.paths.pathItems.get("/myOp").get.operationId);

		// Exception branch
		OpenAPI apiErr = tests.Parse.parse("invalid java");
		assertNotNull(apiErr);

		// Non-matching class name
		OpenAPI api2 = tests.Parse.parse("class MyClass { void test_myOp() {} }");
		assertEquals(0, api2.paths.pathItems.size());

		// Non-matching method name
		OpenAPI api3 = tests.Parse.parse("class MyIntegrationTest { void myOp() {} }");
		assertEquals(0, api3.paths.pathItems.size());
	}

	/**
	 * Verifies {@link tests.Emit#emit} across all HTTP verbs and parameter
	 * configurations.
	 *
	 * @throws Exception
	 *             If emission or reflection fails.
	 */
	@Test
	public void testTestsEmit() throws Exception {
		tests.Emit emitter = new tests.Emit();
		OpenAPI api = new OpenAPI();
		api.servers = new java.util.ArrayList<>();
		openapi.Server s = new openapi.Server();
		s.url = "http://x/api/v3";
		api.servers.add(s);
		api.info = new Info();
		api.info.title = "Test";
		api.paths = new Paths();
		PathItem pi = new PathItem();

		Operation opGet = new Operation();
		opGet.operationId = "getOp";
		pi.get = opGet;

		Operation opPost = new Operation();
		opPost.operationId = "postOp";
		opPost.requestBody = ReferenceOr.ofValue(new RequestBody());
		pi.post = opPost;

		Operation opPut = new Operation();
		// no operation ID
		opPut.requestBody = ReferenceOr.ofValue(new RequestBody());
		pi.put = opPut;

		Operation opDelete = new Operation();
		opDelete.operationId = "deleteOp";
		pi.delete = opDelete;

		Operation opPatch = new Operation();
		opPatch.operationId = "patchOp";
		pi.patch = opPatch;

		api.paths.pathItems.put("/test", pi);

		// Tests Emit parameter logic
		PathItem pi2 = new PathItem();
		pi2.parameters = new java.util.ArrayList<>();
		Parameter pathParam = new Parameter();
		pathParam.name = "id";
		pathParam.in = "path";
		pi2.parameters.add(ReferenceOr.ofValue(pathParam));

		Operation opGet2 = new Operation();
		opGet2.parameters = new java.util.ArrayList<>();
		Parameter opParam = new Parameter();
		opParam.name = "id";
		opParam.in = "path";
		opGet2.parameters.add(ReferenceOr.ofValue(opParam));

		Parameter opParam2 = new Parameter();
		opParam2.name = "query";
		opParam2.in = "query";
		opGet2.parameters.add(ReferenceOr.ofValue(opParam2));

		// null param handling
		opGet2.parameters.add(ReferenceOr.ofValue(new Parameter()));
		opGet2.parameters.add(null);
		pi2.parameters.add(null);

		pi2.get = opGet2;
		api.paths.pathItems.put("/test2", pi2);

		// post edge cases
		PathItem pi3 = new PathItem();
		Operation postArray = new Operation();
		postArray.requestBody = ReferenceOr.ofValue(new RequestBody());
		postArray.parameters = new java.util.ArrayList<>();
		Parameter postParam = new Parameter();
		postParam.name = "id";
		postParam.in = "body";
		postArray.parameters.add(ReferenceOr.ofValue(postParam));
		pi3.post = postArray;
		api.paths.pathItems.put("/createWithArray", pi3);

		PathItem pi4 = new PathItem();
		Operation postList = new Operation();
		postList.requestBody = ReferenceOr.ofValue(new RequestBody());
		postList.parameters = new java.util.ArrayList<>();
		Parameter postListParam = new Parameter();
		postListParam.name = "id";
		postListParam.in = "body";
		postList.parameters.add(ReferenceOr.ofValue(postListParam));
		pi4.post = postList;
		api.paths.pathItems.put("/createWithList", pi4);

		PathItem pi5_test = new PathItem();
		Operation postNormal = new Operation();
		postNormal.parameters = new java.util.ArrayList<>();
		Parameter postNormalParam = new Parameter();
		postNormalParam.name = "id";
		postNormalParam.in = "body";
		postNormal.parameters.add(ReferenceOr.ofValue(postNormalParam));
		pi5_test.post = postNormal;
		api.paths.pathItems.put("/createNormal", pi5_test);

		String res = tests.Emit.emit(api, null);
		assertNotNull(res);
		assertTrue(res.contains("testGetOp"));
		assertTrue(res.contains("testPostOp"));
		assertTrue(res.contains("testPuttest"));
		assertTrue(res.contains("testDeleteOp"));
		assertTrue(res.contains("testPatchOp"));

		OpenAPI roundTripTests = tests.Parse.parse(res);
		assertNotNull(roundTripTests.paths.pathItems.get("/test"));
		assertNotNull(roundTripTests.paths.pathItems.get("/test").get);
		assertNotNull(roundTripTests.paths.pathItems.get("/test").post);
		assertNotNull(roundTripTests.paths.pathItems.get("/test").put);
		assertNotNull(roundTripTests.paths.pathItems.get("/test").delete);
		assertNotNull(roundTripTests.paths.pathItems.get("/test").patch);
		OpenAPI rootPathTest = tests.Parse.parse(
				"public class RootIntegrationTest { public void t() { String s = \"GET /v2 HTTP/1.1\"; } public void t2(); }");
		assertNotNull(rootPathTest.paths.pathItems.get("/"));
		assertEquals("t", rootPathTest.paths.pathItems.get("/").get.operationId);

		// empty info title
		OpenAPI api2 = new OpenAPI();
		api2.info = new Info();
		api2.info.title = "";
		tests.Emit.emit(api2, null);

		// null info title
		OpenAPI api3 = new OpenAPI();
		api3.info = new Info();
		api3.paths = new Paths();
		api3.paths.pathItems = null;
		tests.Emit.emit(api3, null);
		// empty opId, non-alphanumeric
		OpenAPI api4 = new OpenAPI();
		api4.paths = new Paths();
		PathItem pi4_1 = new PathItem();
		Operation op4 = new Operation();
		op4.operationId = "";
		pi4_1.get = op4;
		api4.paths.pathItems.put("/abc", pi4_1);
		tests.Emit.emit(api4, null);

		// basePath present
		OpenAPI apiBasePath = new OpenAPI();
		apiBasePath.info = new Info();
		apiBasePath.info.title = "BasePath API";
		apiBasePath.basePath = "/v1";
		apiBasePath.paths = new Paths();
		apiBasePath.paths.pathItems = new java.util.HashMap<>();
		PathItem piBasePath = new PathItem();
		Operation opBasePath = new Operation();
		opBasePath.operationId = "basePathOp";
		piBasePath.get = opBasePath;
		apiBasePath.paths.pathItems.put("/base", piBasePath);
		String baseOut = tests.Emit.emit(apiBasePath, null);
		assertTrue(baseOut.contains("http://localhost:8080/v1"));

		// basePath empty string
		apiBasePath.basePath = "";
		String emptyBaseOut = tests.Emit.emit(apiBasePath, null);
		assertTrue(emptyBaseOut.contains("http://localhost:8080/v2"));

		// PUT with body parameter and requestBody on /createWithArray
		PathItem piPutBody = new PathItem();
		Operation putBodyOp = new Operation();
		putBodyOp.operationId = "putWithBody";
		putBodyOp.requestBody = ReferenceOr.ofValue(new RequestBody());
		putBodyOp.parameters = new java.util.ArrayList<>();
		Parameter putBodyParam = new Parameter();
		putBodyParam.name = "bodyParam";
		putBodyParam.in = "body";
		putBodyOp.parameters.add(ReferenceOr.ofValue(putBodyParam));
		piPutBody.put = putBodyOp;
		apiBasePath.paths.pathItems.put("/createWithArray", piPutBody);
		String putBodyOut = tests.Emit.emit(apiBasePath, null);
		assertTrue(putBodyOut.contains("testPutWithBody"));

		// servers present but URL missing "/api"
		OpenAPI apiNoApiUrl = new OpenAPI();
		apiNoApiUrl.info = new Info();
		apiNoApiUrl.info.title = "No Api Url API";
		apiNoApiUrl.servers = new java.util.ArrayList<>();
		openapi.Server sNoApi = new openapi.Server();
		sNoApi.url = "http://x/v3";
		apiNoApiUrl.servers.add(sNoApi);
		apiNoApiUrl.paths = new Paths();
		apiNoApiUrl.paths.pathItems = new java.util.HashMap<>();
		PathItem piNoApi = new PathItem();
		Operation opNoApi = new Operation();
		opNoApi.operationId = "noApiOp";
		piNoApi.get = opNoApi;
		apiNoApiUrl.paths.pathItems.put("/noapi", piNoApi);
		tests.Emit.emit(apiNoApiUrl, null);

		// parameter with empty safeName and parameter/body examples/schemas
		OpenAPI api5 = new OpenAPI();
		api5.paths = new Paths();
		PathItem pi5 = new PathItem();
		Operation op5 = new Operation();
		op5.operationId = "testSafeName";
		op5.parameters = new java.util.ArrayList<>();
		Parameter p_invalid = new Parameter();
		p_invalid.name = "$"; // non-alphanumeric will become empty
		p_invalid.in = "query";
		op5.parameters.add(ReferenceOr.ofValue(p_invalid));

		Parameter pEx = new Parameter();
		pEx.name = "exParam";
		pEx.in = "query";
		pEx.example = "exVal";
		op5.parameters.add(ReferenceOr.ofValue(pEx));

		Parameter pDef = new Parameter();
		pDef.name = "defParam";
		pDef.in = "query";
		Schema defSchema = new Schema();
		defSchema.defaultValue = arazzo.PayloadValue.ofString("defVal");
		pDef.schema = ReferenceOr.ofValue(defSchema);
		op5.parameters.add(ReferenceOr.ofValue(pDef));

		Parameter pSchEx = new Parameter();
		pSchEx.name = "schExParam";
		pSchEx.in = "query";
		Schema schEx = new Schema();
		schEx.example = arazzo.PayloadValue.ofString("schExVal");
		pSchEx.schema = ReferenceOr.ofValue(schEx);
		op5.parameters.add(ReferenceOr.ofValue(pSchEx));

		Parameter pBodyArr = new Parameter();
		pBodyArr.name = "bodyArr";
		pBodyArr.in = "body";
		Schema arrSchema = new Schema();
		arrSchema.type = SchemaType.ofSingle("array");
		pBodyArr.schema = ReferenceOr.ofValue(arrSchema);
		op5.parameters.add(ReferenceOr.ofValue(pBodyArr));

		Parameter pBodyEx = new Parameter();
		pBodyEx.name = "bodyEx";
		pBodyEx.in = "body";
		Schema bodyExSchema = new Schema();
		bodyExSchema.type = SchemaType.ofSingle("object");
		bodyExSchema.example = arazzo.PayloadValue.ofString("{\"a\":1}");
		pBodyEx.schema = ReferenceOr.ofValue(bodyExSchema);
		op5.parameters.add(ReferenceOr.ofValue(pBodyEx));

		Parameter pBodyPlain = new Parameter();
		pBodyPlain.name = "bodyPlain";
		pBodyPlain.in = "body";
		Schema bodyPlainSchema = new Schema();
		bodyPlainSchema.type = SchemaType.ofSingle("object");
		pBodyPlain.schema = ReferenceOr.ofValue(bodyPlainSchema);
		op5.parameters.add(ReferenceOr.ofValue(pBodyPlain));

		RequestBody rbJson = new RequestBody();
		rbJson.content = new java.util.HashMap<>();
		MediaType mtJson = new MediaType();
		mtJson.schema = ReferenceOr.ofValue(arrSchema);
		rbJson.content.put("application/json", mtJson);
		op5.requestBody = ReferenceOr.ofValue(rbJson);

		pi5.get = op5;
		api5.paths.pathItems.put("/testSafeNamePath", pi5);
		String out5 = tests.Emit.emit(api5, null);
		assertTrue(out5.contains("\"exVal\""));
		assertTrue(out5.contains("\"defVal\""));
		assertTrue(out5.contains("\"schExVal\""));
	}

	/**
	 * Verifies {@link docstrings.Parse#parse}.
	 */
	@Test
	public void testDocstringsParse() {
		docstrings.Parse p = new docstrings.Parse();
		assertNotNull(docstrings.Parse.parse("any"));
	}

	/**
	 * Verifies {@link docstrings.Emit#emitDocsJson} with and without imports and
	 * wrapping.
	 *
	 * @throws Exception
	 *             If JSON serialization fails.
	 */
	@Test
	public void testDocstringsEmit() throws Exception {
		docstrings.Emit e = new docstrings.Emit();
		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.post = new Operation();
		pi.put = new Operation();
		pi.delete = new Operation();
		pi.patch = new Operation();
		pi.options = new Operation();
		pi.head = new Operation();
		pi.trace = new Operation();
		api.paths.pathItems.put("/path", pi);

		String json1 = docstrings.Emit.emitDocsJson(api, false, false);
		assertTrue(json1.contains("import my.api.ApiClient"));
		assertTrue(json1.contains("public class Example"));

		String json2 = docstrings.Emit.emitDocsJson(api, true, true);
		assertFalse(json2.contains("import my.api.ApiClient"));
		assertFalse(json2.contains("public class Example"));

		Operation op = new Operation();
		op.operationId = "myOp";
		pi.get = op;
		api.paths.pathItems.put("/path2", pi);
		docstrings.Emit.emitDocsJson(api, false, false);

		// Test nulls
		docstrings.Emit.emitDocsJson(null, false, false);
		docstrings.Emit.emitDocsJson(new OpenAPI(), false, false);
		OpenAPI apiNullPaths = new OpenAPI();
		apiNullPaths.paths = new Paths();
		apiNullPaths.paths.pathItems = null;
		docstrings.Emit.emitDocsJson(apiNullPaths, false, false);
		// Test missing get and empty path map
		OpenAPI emptyApi = new OpenAPI();
		emptyApi.paths = new Paths();
		emptyApi.paths.pathItems.put("/empty", new PathItem());
		PathItem noGet = new PathItem();
		noGet.post = new Operation();
		emptyApi.paths.pathItems.put("/noget", noGet);
		docstrings.Emit.emitDocsJson(emptyApi, false, false);

		// Empty operationId
		Operation emptyOpId = new Operation();
		emptyOpId.operationId = "";
		noGet.post = emptyOpId;
		docstrings.Emit.emitDocsJson(emptyApi, false, false);
	}

	/**
	 * Verifies {@link mocks.Parse#parse}.
	 */
	@Test
	public void testMocksParse() {
		mocks.Parse p = new mocks.Parse();
		String src = "server.createContext(\"/test\", handler);";
		OpenAPI api = mocks.Parse.parse(src);
		assertNotNull(api.paths.pathItems.get("/test"));

		// with class
		String src2 = "class Mock { void test() { server.createContext(\"/test2\"); } }";
		OpenAPI api2 = mocks.Parse.parse(src2);
		assertNotNull(api2.paths.pathItems.get("/test2"));

		// error
		mocks.Parse.parse("invalid");

		// empty args
		mocks.Parse.parse("class Mock { void test() { server.createContext(); } }");

		// multiple for same path
		mocks.Parse.parse(
				"class Mock { void test() { server.createContext(\"/test3\"); server.createContext(\"/test3\"); } }");

		// Different method scope or no scope
		mocks.Parse
				.parse("class Mock { void test() { createContext(\"/test\"); something.createContext(\"/test\"); } }");

		// @mockRoute tags covering all HTTP verbs and arities
		String mockWithRoutes = "public class PetMockServer {\n" + "  /**\n"
				+ "   * @mockRoute GET /pets listPets 200 List all pets\n" + "   * @mockRoute POST /pets - 201\n"
				+ "   * @mockRoute PUT /pets/{id} updatePet 200 Update pet\n"
				+ "   * @mockRoute DELETE /pets/{id} deletePet 204 Delete pet\n"
				+ "   * @mockRoute PATCH /pets/{id} patchPet 200 Patch pet\n" + "   * @mockRoute SHORT /only3\n"
				+ "   * @otherTag ignored\n" + "   */\n" + "  public void start(int port) {}\n"
				+ "  public void noDoc() {}\n" + "}\n";
		OpenAPI parsedMockRoutes = mocks.Parse.parse(mockWithRoutes);
		assertNotNull(parsedMockRoutes.paths.pathItems.get("/pets"));
		assertEquals("listPets", parsedMockRoutes.paths.pathItems.get("/pets").get.operationId);
		assertNull(parsedMockRoutes.paths.pathItems.get("/pets").post.operationId);
		assertEquals("updatePet", parsedMockRoutes.paths.pathItems.get("/pets/{id}").put.operationId);
		assertEquals("deletePet", parsedMockRoutes.paths.pathItems.get("/pets/{id}").delete.operationId);
		assertEquals("patchPet", parsedMockRoutes.paths.pathItems.get("/pets/{id}").patch.operationId);
	}

	/**
	 * Verifies {@link mocks.Emit#emit}.
	 */
	@Test
	public void testMocksEmit() {
		mocks.Emit e = new mocks.Emit();
		assertNotNull(e);
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.title = "TestMock";
		api.components = new Components();
		api.components.schemas = new java.util.HashMap<>();
		Schema itemSchema = new Schema();
		itemSchema.type = SchemaType.ofSingle("object");
		itemSchema.properties = new java.util.HashMap<>();
		Schema strProp = new Schema();
		strProp.type = SchemaType.ofSingle("string");
		Schema intProp = new Schema();
		intProp.type = SchemaType.ofSingle("integer");
		Schema numProp = new Schema();
		numProp.type = SchemaType.ofSingle("number");
		Schema boolProp = new Schema();
		boolProp.type = SchemaType.ofSingle("boolean");
		Schema enumProp = new Schema();
		enumProp.type = SchemaType.ofSingle("string");
		enumProp.enumValues = java.util.Arrays.asList(arazzo.PayloadValue.ofString("ACTIVE"));
		Schema exProp = new Schema();
		exProp.type = SchemaType.ofSingle("string");
		exProp.example = arazzo.PayloadValue.ofString("customEx");
		itemSchema.properties.put("s", ReferenceOr.ofValue(strProp));
		itemSchema.properties.put("i", ReferenceOr.ofValue(intProp));
		itemSchema.properties.put("n", ReferenceOr.ofValue(numProp));
		itemSchema.properties.put("b", ReferenceOr.ofValue(boolProp));
		itemSchema.properties.put("e", ReferenceOr.ofValue(enumProp));
		itemSchema.properties.put("x", ReferenceOr.ofValue(exProp));
		itemSchema.properties.put("nonSchema", null);
		api.components.schemas.put("Item", ReferenceOr.ofValue(itemSchema));

		api.paths = new Paths();
		PathItem pi1 = new PathItem();
		Operation getOp = new Operation();
		getOp.operationId = "getItem";
		getOp.description = "Get item\ndesc";
		getOp.responses = new Responses();
		getOp.responses.statusCodes = new java.util.HashMap<>();
		Response r200 = new Response();
		r200.content = new java.util.HashMap<>();
		MediaType mt200 = new MediaType();
		Schema refSchema = new Schema();
		refSchema.$ref = "#/components/schemas/Item";
		mt200.schema = ReferenceOr.ofValue(refSchema);
		r200.content.put("application/json", mt200);
		getOp.responses.statusCodes.put("200", ReferenceOr.ofValue(r200));
		pi1.get = getOp;
		api.paths.pathItems.put("/test/{id}", pi1);

		PathItem pi2 = new PathItem();
		Operation postOp = new Operation();
		postOp.operationId = "";
		postOp.description = "";
		postOp.responses = new Responses();
		postOp.responses.statusCodes = new java.util.HashMap<>();
		Response r201 = new Response();
		r201.content = new java.util.HashMap<>();
		MediaType mt201 = new MediaType();
		mt201.example = "{\"created\":true}";
		r201.content.put("application/json", mt201);
		postOp.responses.statusCodes.put("201", ReferenceOr.ofValue(r201));
		pi2.post = postOp;
		api.paths.pathItems.put("/test2/", pi2);

		PathItem pi3 = new PathItem();
		Operation putOp = new Operation();
		putOp.responses = new Responses();
		Response rDef = new Response();
		Schema arrSchema = new Schema();
		arrSchema.type = SchemaType.ofSingle("array");
		arrSchema.items = ReferenceOr.ofValue(strProp);
		rDef.schema = ReferenceOr.ofValue(arrSchema);
		putOp.responses.defaultResponse = ReferenceOr.ofValue(rDef);
		pi3.put = putOp;
		api.paths.pathItems.put("/test3", pi3);

		PathItem pi4 = new PathItem();
		Operation delOp = new Operation();
		delOp.responses = new Responses();
		delOp.responses.statusCodes = new java.util.HashMap<>();
		Response r204 = new Response();
		r204.content = new java.util.HashMap<>();
		MediaType mtScalarEx = new MediaType();
		mtScalarEx.example = "scalarVal";
		r204.content.put("application/json", mtScalarEx);
		delOp.responses.statusCodes.put("200", ReferenceOr.ofValue(r204));
		delOp.responses.statusCodes.put("204", ReferenceOr.ofValue(r204));
		pi4.delete = delOp;
		api.paths.pathItems.put("/test4", pi4);

		PathItem pi5 = new PathItem();
		Operation patchOp = new Operation();
		patchOp.responses = new Responses();
		patchOp.responses.statusCodes = new java.util.HashMap<>();
		Response rEmptyMt = new Response();
		rEmptyMt.content = new java.util.HashMap<>();
		rEmptyMt.content.put("application/json", new MediaType());
		patchOp.responses.statusCodes.put("200", ReferenceOr.ofValue(rEmptyMt));
		patchOp.responses.statusCodes.put("204", ReferenceOr.ofValue(new Response()));
		pi5.patch = patchOp;
		api.paths.pathItems.put("/test5", pi5);

		PathItem pi6 = new PathItem();
		Operation op204Only = new Operation();
		op204Only.responses = new Responses();
		op204Only.responses.statusCodes = new java.util.HashMap<>();
		op204Only.responses.statusCodes.put("204", ReferenceOr.ofValue(new Response()));
		pi6.patch = op204Only;
		api.paths.pathItems.put("/test6", pi6);

		Schema missingRef = new Schema();
		missingRef.$ref = "#/components/schemas/DoesNotExist";
		assertNotNull(mocks.Emit.synthesizeSchemaValue(missingRef, api));
		assertNotNull(mocks.Emit.synthesizeSchemaValue(missingRef, new OpenAPI()));
		Schema externalRef = new Schema();
		externalRef.$ref = "http://example.com/schema";
		assertNotNull(mocks.Emit.synthesizeSchemaValue(externalRef, null));
		Schema emptyArr = new Schema();
		emptyArr.type = SchemaType.ofSingle("array");
		assertNotNull(mocks.Emit.synthesizeSchemaValue(emptyArr, null));
		Schema nullTypeSchema = new Schema();
		nullTypeSchema.type = null;
		assertNotNull(mocks.Emit.synthesizeSchemaValue(nullTypeSchema, null));

		String newSrc = mocks.Emit.emit(api, null);
		assertTrue(newSrc.contains("MockServer"));
		assertNotNull(mocks.Emit.emitModular(api));

		// Re-emit to same src
		String resrc = mocks.Emit.emit(api, newSrc);
		assertTrue(resrc.contains("MockServer"));

		// Re-emit to existing source with different class name to trigger fallback
		String otherSrc = "class OtherClass {}";
		String otherResrc = mocks.Emit.emit(api, otherSrc);
		assertTrue(otherResrc.contains("start"));

		// existing source is empty
		String emptyResrc = mocks.Emit.emit(api, "   ");
		assertTrue(emptyResrc.contains("MockServer"));

		// Try with missing model paths
		mocks.Emit.emit(new OpenAPI(), null);
		OpenAPI mockNullItems = new OpenAPI();
		mockNullItems.paths = new Paths();
		mockNullItems.paths.pathItems = null;
		mocks.Emit.emit(mockNullItems, null);
		// Empty title
		OpenAPI api2 = new OpenAPI();
		api2.info = new Info();
		api2.info.title = "";
		mocks.Emit.emit(api2, null);

		// null info
		OpenAPI api3 = new OpenAPI();
		api3.paths = new Paths();
		api3.paths.pathItems.put("/", new PathItem()); // test handlerPath.length() <= 1
		mocks.Emit.emit(api3, null);
	}

	/**
	 * Verifies {@link functions.Parse#parse}, {@link docstrings.Parse#parse}, and
	 * {@link openapi.TypeMapper} utilities.
	 */
	@Test
	public void testFunctionsParse() {
		functions.Parse p = new functions.Parse();
		assertNotNull(p);
		assertNotNull(functions.Parse.parse(null));
		assertNotNull(functions.Parse.parse("test"));
		assertNotNull(functions.Parse.parse("class Broken {"));
		OpenAPI parsed = functions.Parse.parse("public class Helper { public void doWork() {} }");
		assertNotNull(parsed.paths);
		assertNotNull(parsed.paths.pathItems.get("/doWork"));
		assertEquals("doWork", parsed.paths.pathItems.get("/doWork").get.operationId);

		assertEquals("", openapi.TypeMapper.escape(null));
		assertEquals("a\\\"b\\nc\\rd\\\\e", openapi.TypeMapper.escape("a\"b\nc\rd\\e"));
		assertEquals("", openapi.TypeMapper.sanitizeIdentifier(null));
		assertEquals("abc_123", openapi.TypeMapper.sanitizeIdentifier("a-b$c_123!"));
		assertEquals("", openapi.TypeMapper.sanitizeAlphanumeric(null));
		assertEquals("abc123", openapi.TypeMapper.sanitizeAlphanumeric("a-b_c 123!"));
		Schema wildcardSchema = new Schema();
		openapi.TypeMapper.toSchema(new com.github.javaparser.ast.type.WildcardType(), wildcardSchema, false);
		assertNull(wildcardSchema.type);

		assertNull(docstrings.Parse.parse(null).paths);
		assertNull(docstrings.Parse.parse("not json").paths);
		assertNull(docstrings.Parse.parse("{\"other\": 1}").paths);
		String docsJson = "{\"endpoints\": [" + "{\"path\": \"/p1\", \"method\": \"GET\", \"operationId\": \"opGet\"},"
				+ "{\"path\": \"/p1\", \"method\": \"POST\"}," + "{\"path\": \"/p1\", \"method\": \"PUT\"},"
				+ "{\"path\": \"/p1\", \"method\": \"DELETE\"}," + "{\"path\": \"/p1\", \"method\": \"PATCH\"}" + "]}";
		OpenAPI parsedDocs = docstrings.Parse.parse(docsJson);
		assertNotNull(parsedDocs.paths);
		PathItem docPi = parsedDocs.paths.pathItems.get("/p1");
		assertNotNull(docPi);
		assertEquals("opGet", docPi.get.operationId);
		assertNotNull(docPi.post);
		assertNotNull(docPi.put);
		assertNotNull(docPi.delete);
		assertNotNull(docPi.patch);
	}

	/**
	 * Verifies {@link functions.Emit#emit}.
	 */
	@Test
	public void testFunctionsEmit() {
		functions.Emit e = new functions.Emit();
		assertNotNull(e);
		String src = "class Test {}";
		String out1 = functions.Emit.emit(null, src);
		assertTrue(out1.contains("class Test {}"));

		String out2 = functions.Emit.emit(null, null);
		assertTrue(out2.contains("Helper Functions Generated"));

		String out3 = functions.Emit.emit(null, "   ");
		assertTrue(out3.contains("Helper Functions Generated"));

		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		api.paths.pathItems = new java.util.HashMap<>();
		PathItem pi = new PathItem();
		Operation getOp = new Operation();
		getOp.operationId = "getFunc";
		getOp.description = "Get func\ndesc";
		getOp.parameters = new java.util.ArrayList<>();
		Parameter p1 = new Parameter();
		p1.name = "id";
		p1.in = "path";
		getOp.parameters.add(ReferenceOr.ofValue(p1));
		Parameter p2 = new Parameter();
		p2.name = "q";
		getOp.parameters.add(ReferenceOr.ofValue(p2));
		Parameter p3 = new Parameter();
		p3.name = "$";
		getOp.parameters.add(ReferenceOr.ofValue(p3));
		Parameter p4 = new Parameter();
		getOp.parameters.add(ReferenceOr.ofValue(p4));
		getOp.parameters.add(null);
		pi.get = getOp;

		Operation postOp = new Operation();
		postOp.operationId = "";
		postOp.description = "";
		postOp.requestBody = ReferenceOr.ofValue(new RequestBody());
		pi.post = postOp;

		pi.put = new Operation();
		pi.delete = new Operation();
		pi.patch = new Operation();
		api.paths.pathItems.put("/items/{id}", pi);

		String emittedFuncs = functions.Emit.emit(api, null);
		assertTrue(emittedFuncs.contains("getFunc"));
		String reEmitted = functions.Emit.emit(api, emittedFuncs);
		assertTrue(reEmitted.contains("getFunc"));
		functions.Emit.emit(api, "enum E {}");
		functions.Emit.emit(new OpenAPI(), null);
		OpenAPI emptyPaths = new OpenAPI();
		emptyPaths.paths = new Paths();
		emptyPaths.paths.pathItems = new java.util.HashMap<>();
		functions.Emit.emit(emptyPaths, null);
		emptyPaths.paths.pathItems = null;
		functions.Emit.emit(emptyPaths, src);

		OpenAPI roundTripFuncs = functions.Parse.parse(emittedFuncs);
		assertNotNull(roundTripFuncs.paths.pathItems.get("/items/{id}"));
		assertEquals("getFunc", roundTripFuncs.paths.pathItems.get("/items/{id}").get.operationId);
		assertNotNull(roundTripFuncs.paths.pathItems.get("/items/{id}").post.requestBody);
		assertNotNull(roundTripFuncs.paths.pathItems.get("/items/{id}").put);
		assertNotNull(roundTripFuncs.paths.pathItems.get("/items/{id}").delete);
		assertNotNull(roundTripFuncs.paths.pathItems.get("/items/{id}").patch);

		// Edge tags in functions.Parse
		functions.Parse.parse(
				"class Edge { /** @httpMethod \n @httpPath \n @httpParam onlyOne \n @httpRequestBody false \n @other tag */ void m() {} }");
	}

	/**
	 * Verifies {@link tests.Emit#emit} when parameter name or location fields are
	 * missing.
	 *
	 * @throws Exception
	 *             If emission fails.
	 */
	@Test
	public void testMissingParamFields() throws Exception {
		openapi.OpenAPI api = new openapi.OpenAPI();
		api.paths = new openapi.Paths();
		openapi.PathItem pi = new openapi.PathItem();
		api.paths.pathItems.put("/test", pi);

		openapi.Operation op = new openapi.Operation();
		pi.get = op;

		op.parameters = new java.util.ArrayList<>();
		openapi.Parameter p1 = new openapi.Parameter();
		op.parameters.add(ReferenceOr.ofValue(p1));

		openapi.Parameter p2 = new openapi.Parameter();
		p2.name = "id";
		op.parameters.add(ReferenceOr.ofValue(p2));

		openapi.Parameter p3 = new openapi.Parameter();
		p3.in = "query";
		op.parameters.add(ReferenceOr.ofValue(p3));

		pi.parameters = new java.util.ArrayList<>();
		openapi.Parameter pp1 = new openapi.Parameter();
		pi.parameters.add(ReferenceOr.ofValue(pp1));

		openapi.Parameter pp2 = new openapi.Parameter();
		pp2.name = "id";
		pi.parameters.add(ReferenceOr.ofValue(pp2));

		openapi.Parameter pp3 = new openapi.Parameter();
		pp3.in = "query";
		pi.parameters.add(ReferenceOr.ofValue(pp3));

		openapi.Parameter p4 = new openapi.Parameter();
		p4.name = "match";
		p4.in = "query";
		op.parameters.add(ReferenceOr.ofValue(p4));

		openapi.Parameter pp4 = new openapi.Parameter();
		pp4.name = "match";
		pp4.in = "query";
		pi.parameters.add(ReferenceOr.ofValue(pp4));

		openapi.Parameter p5 = new openapi.Parameter();
		p5.name = "diffIn";
		p5.in = "query";
		op.parameters.add(ReferenceOr.ofValue(p5));

		openapi.Parameter pp5 = new openapi.Parameter();
		pp5.name = "diffIn";
		pp5.in = "path";
		pi.parameters.add(ReferenceOr.ofValue(pp5));

		tests.Emit.emit(api, null);
	}

	/**
	 * Verifies additional class resolution branches in {@link mocks.Emit#emit}.
	 *
	 * @throws Exception
	 *             If emission fails.
	 */
	@Test
	public void testMocksEmitBranches() throws Exception {
		openapi.OpenAPI api = new openapi.OpenAPI();
		// Test missing info or title
		mocks.Emit.emit(api, null);

		api.info = new openapi.Info();
		mocks.Emit.emit(api, null);

		api.info.title = "MyApi";
		mocks.Emit.emit(api, null);

		// classDecl != null requires existing code with a class
		mocks.Emit.emit(api, "class Mocks {\n}");

		// test where classDecl is null
		mocks.Emit.emit(api, "interface Something {}");

		// test where classDecl remains null (no class or interface)
		mocks.Emit.emit(api, "enum SomeEnum {}");
	}
}
