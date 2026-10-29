package docstrings;

import static org.junit.Assert.*;

import arazzo.Arazzo;
import arazzo.ArazzoParameter;
import arazzo.ReusableOr;
import arazzo.Workflow;
import cli.CddCli;
import common.PayloadValue;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import openapi.Components;
import openapi.MediaType;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.Parameter;
import openapi.PathItem;
import openapi.Paths;
import openapi.ReferenceOr;
import openapi.RequestBody;
import openapi.Schema;
import openapi.SchemaType;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Exhaustive unit tests for {@link Emit} verifying structured code objects,
 * parameterized Java SDK invocations, OpenAPI webhooks, Arazzo workflows, and
 * CLI directory output handling.
 */
public class EmitTest {

	/** Line feed character constant. */
	private static final String NL = String.valueOf((char) 10);

	/** Double-quote character constant. */
	private static final String Q = String.valueOf((char) 34);

	/**
	 * Default constructor.
	 */
	public EmitTest() {
	}

	/**
	 * Verifies structured {@code imports}, {@code wrapper_start}, {@code snippet},
	 * {@code wrapper_end}, and {@code code} output with and without
	 * {@code noImports} and {@code noWrapping} flags.
	 *
	 * @throws Exception
	 *             If generation fails.
	 */
	@Test
	public void testStructuredComponentsAndFlags() throws Exception {
		assertNotNull(new Emit());

		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		api.paths.pathItems = new LinkedHashMap<>();
		PathItem pi = new PathItem();
		Operation op = new Operation();
		op.operationId = "listPets";
		pi.get = op;
		api.paths.pathItems.put("/pets", pi);

		JSONObject full = new JSONObject(Emit.emitDocsJson(api, false, false));
		assertEquals(Emit.DEFAULT_IMPORTS, full.getString("imports"));
		assertEquals(Emit.DEFAULT_WRAPPER_START, full.getString("wrapper_start"));
		assertEquals(Emit.DEFAULT_WRAPPER_END, full.getString("wrapper_end"));

		JSONObject getObj = full.getJSONObject("endpoints").getJSONObject("/pets").getJSONObject("get");
		assertEquals(Emit.DEFAULT_IMPORTS, getObj.getString("imports"));
		assertEquals(Emit.DEFAULT_WRAPPER_START, getObj.getString("wrapper_start"));
		assertEquals(Emit.DEFAULT_WRAPPER_END, getObj.getString("wrapper_end"));
		assertEquals("var response = client.listPets();" + NL + "System.out.println(response);",
				getObj.getString("snippet"));
		assertTrue(getObj.getString("code").contains(Emit.DEFAULT_IMPORTS));
		assertTrue(getObj.getString("code").contains(Emit.DEFAULT_WRAPPER_START));
		assertTrue(getObj.getString("code").contains("            var response = client.listPets();"));
		assertTrue(getObj.getString("code").contains(Emit.DEFAULT_WRAPPER_END));

		JSONObject stripped = new JSONObject(Emit.emitDocsJson(api, true, true));
		assertEquals("", stripped.getString("imports"));
		assertEquals("", stripped.getString("wrapper_start"));
		assertEquals("", stripped.getString("wrapper_end"));

		JSONObject strippedGet = stripped.getJSONObject("endpoints").getJSONObject("/pets").getJSONObject("get");
		assertEquals("", strippedGet.getString("imports"));
		assertEquals("", strippedGet.getString("wrapper_start"));
		assertEquals("", strippedGet.getString("wrapper_end"));
		assertEquals("var response = client.listPets();" + NL + "System.out.println(response);",
				strippedGet.getString("code"));
	}

	/**
	 * Verifies parameterized Java SDK method invocations across inline parameters,
	 * referenced parameters, examples, defaults, enums, primitive types/formats,
	 * and request bodies.
	 *
	 * @throws Exception
	 *             If generation fails.
	 */
	@Test
	public void testParameterizedJavaSdkInvocations() throws Exception {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.parameters = new LinkedHashMap<>();
		api.components.schemas = new LinkedHashMap<>();
		api.components.requestBodies = new LinkedHashMap<>();

		Parameter compParam = new Parameter();
		compParam.name = "limit";
		compParam.in = "query";
		Schema intSchema = new Schema();
		intSchema.type = SchemaType.ofSingle("integer");
		compParam.schema = ReferenceOr.ofValue(intSchema);
		api.components.parameters.put("LimitParam", ReferenceOr.ofValue(compParam));

		Schema tagSchema = new Schema();
		tagSchema.type = SchemaType.ofSingle("string");
		tagSchema.example = PayloadValue.ofString("dog");
		api.components.schemas.put("TagSchema", ReferenceOr.ofValue(tagSchema));

		Schema nestedRefSchema = new Schema();
		nestedRefSchema.$ref = "#/components/schemas/TagSchema";
		api.components.schemas.put("NestedRefSchema", ReferenceOr.ofValue(nestedRefSchema));

		Schema unresolvedRefSchema = new Schema();
		unresolvedRefSchema.$ref = "#/components/schemas/MissingDto";
		api.components.schemas.put("UnresolvedRefSchema", ReferenceOr.ofValue(unresolvedRefSchema));

		RequestBody compRb = new RequestBody();
		compRb.content = new LinkedHashMap<>();
		MediaType compMt = new MediaType();
		compMt.schema = ReferenceOr.ofRef("#/components/schemas/Pet");
		compRb.content.put("application/json", compMt);
		api.components.requestBodies.put("PetBody", ReferenceOr.ofValue(compRb));

		api.paths = new Paths();
		api.paths.pathItems = new LinkedHashMap<>();

		PathItem pi = new PathItem();
		pi.parameters = new ArrayList<>();
		pi.parameters.add(ReferenceOr.ofRef("#/components/parameters/LimitParam"));
		pi.parameters.add(ReferenceOr.ofRef("#/components/parameters/Missing"));

		Operation op = new Operation();
		op.operationId = "createPet";
		op.parameters = new ArrayList<>();

		// Duplicate of path parameter (same name and in) -> skipped
		Parameter dupLimit = new Parameter();
		dupLimit.name = "limit";
		dupLimit.in = "query";
		op.parameters.add(ReferenceOr.ofValue(dupLimit));

		// Same name, different 'in' -> kept
		Parameter headerLimit = new Parameter();
		headerLimit.name = "limit";
		headerLimit.in = "header";
		headerLimit.example = PayloadValue.ofNumber(42);
		op.parameters.add(ReferenceOr.ofValue(headerLimit));

		// Null example, non-null default boolean
		Parameter boolParam = new Parameter();
		boolParam.name = "active";
		boolParam.in = "query";
		boolParam.example = PayloadValue.ofNull();
		boolParam.defaultValue = PayloadValue.ofBoolean(true);
		op.parameters.add(ReferenceOr.ofValue(boolParam));

		// Null default, non-empty enumValues
		Parameter enumParam = new Parameter();
		enumParam.name = "status";
		enumParam.in = "query";
		enumParam.defaultValue = PayloadValue.ofNull();
		enumParam.enumValues = Collections.singletonList(PayloadValue.ofString("available"));
		op.parameters.add(ReferenceOr.ofValue(enumParam));

		// Empty enumValues, schema ref to TagSchema
		Parameter schemaRefParam = new Parameter();
		schemaRefParam.name = "tag";
		schemaRefParam.in = "query";
		schemaRefParam.enumValues = Collections.emptyList();
		schemaRefParam.schema = ReferenceOr.ofRef("#/components/schemas/TagSchema");
		op.parameters.add(ReferenceOr.ofValue(schemaRefParam));

		// Schema with nested $ref resolved via api.components.schemas
		Parameter nestedSchemaParam = new Parameter();
		nestedSchemaParam.name = "nested";
		nestedSchemaParam.in = "query";
		nestedSchemaParam.schema = ReferenceOr.ofValue(nestedRefSchema);
		op.parameters.add(ReferenceOr.ofValue(nestedSchemaParam));

		// Schema with unresolved $ref
		Parameter unresolvedSchemaParam = new Parameter();
		unresolvedSchemaParam.name = "unresolved";
		unresolvedSchemaParam.in = "query";
		unresolvedSchemaParam.schema = ReferenceOr.ofValue(unresolvedRefSchema);
		op.parameters.add(ReferenceOr.ofValue(unresolvedSchemaParam));

		// Schema with int64, float, double, boolean, array, object
		Parameter longParam = new Parameter();
		longParam.name = "id";
		longParam.in = "path";
		Schema longSchema = new Schema();
		longSchema.type = SchemaType.ofSingle("integer");
		longSchema.format = "int64";
		longParam.schema = ReferenceOr.ofValue(longSchema);
		op.parameters.add(ReferenceOr.ofValue(longParam));

		Parameter floatParam = new Parameter();
		floatParam.name = "score";
		floatParam.in = "query";
		Schema floatSchema = new Schema();
		floatSchema.type = SchemaType.ofSingle("number");
		floatSchema.format = "float";
		floatParam.schema = ReferenceOr.ofValue(floatSchema);
		op.parameters.add(ReferenceOr.ofValue(floatParam));

		Parameter doubleParam = new Parameter();
		doubleParam.name = "ratio";
		doubleParam.in = "query";
		Schema doubleSchema = new Schema();
		doubleSchema.type = SchemaType.ofSingle("number");
		doubleParam.schema = ReferenceOr.ofValue(doubleSchema);
		op.parameters.add(ReferenceOr.ofValue(doubleParam));

		Parameter boolSchemaParam = new Parameter();
		boolSchemaParam.name = "flag";
		boolSchemaParam.in = "query";
		Schema bSchema = new Schema();
		bSchema.type = SchemaType.ofSingle("boolean");
		boolSchemaParam.schema = ReferenceOr.ofValue(bSchema);
		op.parameters.add(ReferenceOr.ofValue(boolSchemaParam));

		Parameter arrSchemaParam = new Parameter();
		arrSchemaParam.name = "items";
		arrSchemaParam.in = "query";
		Schema aSchema = new Schema();
		aSchema.type = SchemaType.ofSingle("array");
		arrSchemaParam.schema = ReferenceOr.ofValue(aSchema);
		op.parameters.add(ReferenceOr.ofValue(arrSchemaParam));

		Parameter objSchemaParam = new Parameter();
		objSchemaParam.name = "filter";
		objSchemaParam.in = "query";
		Schema oSchema = new Schema();
		oSchema.type = SchemaType.ofSingle("object");
		objSchemaParam.schema = ReferenceOr.ofValue(oSchema);
		op.parameters.add(ReferenceOr.ofValue(objSchemaParam));

		// Swagger 2.0 parameter with p.type
		Parameter sw2Param = new Parameter();
		sw2Param.name = "sw2";
		sw2Param.in = "query";
		sw2Param.type = "integer";
		op.parameters.add(ReferenceOr.ofValue(sw2Param));

		// Parameter with no schema and no type (named and unnamed)
		Parameter bareNamed = new Parameter();
		bareNamed.name = "bare";
		bareNamed.in = "query";
		op.parameters.add(ReferenceOr.ofValue(bareNamed));

		Parameter bareUnnamed = new Parameter();
		bareUnnamed.name = "";
		bareUnnamed.in = "query";
		op.parameters.add(ReferenceOr.ofValue(bareUnnamed));

		Parameter bareNullName = new Parameter();
		bareNullName.name = null;
		bareNullName.in = "query";
		op.parameters.add(ReferenceOr.ofValue(bareNullName));

		// Unresolved param ref in op.parameters
		op.parameters.add(ReferenceOr.ofRef("#/components/parameters/Missing"));

		// RequestBody ref to PetBody
		op.requestBody = ReferenceOr.ofRef("#/components/requestBodies/PetBody");

		pi.post = op;
		api.paths.pathItems.put("/pets", pi);

		JSONObject out = new JSONObject(Emit.emitDocsJson(api, false, false));
		String snippet = out.getJSONObject("endpoints").getJSONObject("/pets").getJSONObject("post")
				.getString("snippet");
		assertTrue(snippet.contains(
				"client.createPet(1, 42, true, " + Q + "available" + Q + ", " + Q + "dog" + Q + ", " + Q + "dog" + Q
						+ ", new MissingDto(), 1L, 1.0f, 1.0, true, java.util.List.of(), " + Q + "{}" + Q + ", 1, " + Q
						+ "bare_example" + Q + ", " + Q + "example" + Q + ", " + Q + "example" + Q + ", new Pet())"));
	}

	/**
	 * Verifies edge cases in schema and requestBody resolution (examples, defaults,
	 * enums, titles, inline $ref, non-JSON media types, and missing components).
	 *
	 * @throws Exception
	 *             If generation fails.
	 */
	@Test
	public void testSchemaAndRequestBodyBranches() throws Exception {
		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		api.paths.pathItems = new LinkedHashMap<>();

		PathItem pi = new PathItem();
		Operation op1 = new Operation();
		op1.operationId = "";
		op1.parameters = new ArrayList<>();

		// Parameter with non-ref empty ReferenceOr and paramRef when api.components is
		// null
		op1.parameters.add(new ReferenceOr<>(null, null));
		op1.parameters.add(ReferenceOr.ofRef("#/components/parameters/MissingNoComp"));

		// Parameter with schema ref when api.components is null
		Parameter pSchemaRefNoComp = new Parameter();
		pSchemaRefNoComp.name = "p1";
		pSchemaRefNoComp.schema = ReferenceOr.ofRef("#/components/schemas/S1");
		op1.parameters.add(ReferenceOr.ofValue(pSchemaRefNoComp));

		// Parameter with schema having defaultValue, enumValues, array/object
		// PayloadValues
		Parameter pDef = new Parameter();
		pDef.name = "pDef";
		Schema sDef = new Schema();
		sDef.$ref = "";
		sDef.example = PayloadValue.ofNull();
		sDef.defaultValue = PayloadValue.ofArray(Collections.emptyList());
		pDef.schema = ReferenceOr.ofValue(sDef);
		op1.parameters.add(ReferenceOr.ofValue(pDef));

		Parameter pEnum = new Parameter();
		pEnum.name = "pEnum";
		Schema sEnum = new Schema();
		sEnum.defaultValue = PayloadValue.ofNull();
		sEnum.enumValues = Collections.singletonList(PayloadValue.ofObject(Collections.emptyMap()));
		pEnum.schema = ReferenceOr.ofValue(sEnum);
		op1.parameters.add(ReferenceOr.ofValue(pEnum));

		Parameter pEmptyEnum = new Parameter();
		pEmptyEnum.name = "pEmptyEnum";
		Schema sEmptyEnum = new Schema();
		sEmptyEnum.enumValues = Collections.emptyList();
		sEmptyEnum.type = new SchemaType((String) null);
		pEmptyEnum.schema = ReferenceOr.ofValue(sEmptyEnum);
		op1.parameters.add(ReferenceOr.ofValue(pEmptyEnum));

		// RequestBody with inline s.$ref
		RequestBody rbInlineRef = new RequestBody();
		rbInlineRef.content = new LinkedHashMap<>();
		MediaType mtInlineRef = new MediaType();
		Schema sInlineRef = new Schema();
		sInlineRef.$ref = "#/components/schemas/Order";
		mtInlineRef.schema = ReferenceOr.ofValue(sInlineRef);
		rbInlineRef.content.put("text/plain", mtInlineRef);
		op1.requestBody = ReferenceOr.ofValue(rbInlineRef);
		pi.get = op1;

		// Operation 2: RequestBody with s.title
		Operation op2 = new Operation();
		op2.operationId = null;
		RequestBody rbTitle = new RequestBody();
		rbTitle.content = new LinkedHashMap<>();
		MediaType mtTitle = new MediaType();
		mtTitle.example = PayloadValue.ofNull();
		Schema sTitle = new Schema();
		sTitle.$ref = "";
		sTitle.title = "CustomPayload";
		mtTitle.schema = ReferenceOr.ofValue(sTitle);
		rbTitle.content.put("application/json", mtTitle);
		op2.requestBody = ReferenceOr.ofValue(rbTitle);
		pi.post = op2;

		// Operation 3: RequestBody with mt.example
		Operation op3 = new Operation();
		op3.operationId = "op3";
		RequestBody rbEx = new RequestBody();
		rbEx.content = new LinkedHashMap<>();
		MediaType mtEx = new MediaType();
		mtEx.example = PayloadValue.ofString("rawBody");
		rbEx.content.put("application/json", mtEx);
		op3.requestBody = ReferenceOr.ofValue(rbEx);
		pi.put = op3;

		// Operation 4: RequestBody with primitive string schema
		Operation op4 = new Operation();
		op4.operationId = "op4";
		RequestBody rbPrim = new RequestBody();
		rbPrim.content = new LinkedHashMap<>();
		MediaType mtPrim = new MediaType();
		Schema sPrim = new Schema();
		sPrim.title = "";
		sPrim.type = SchemaType.ofSingle("integer");
		mtPrim.schema = ReferenceOr.ofValue(sPrim);
		rbPrim.content.put("application/json", mtPrim);
		op4.requestBody = ReferenceOr.ofValue(rbPrim);
		pi.delete = op4;

		// Operation 5: RequestBody with object schema (no title)
		Operation op5 = new Operation();
		op5.operationId = "op5";
		RequestBody rbObj = new RequestBody();
		rbObj.content = new LinkedHashMap<>();
		MediaType mtObj = new MediaType();
		Schema sObj = new Schema();
		sObj.type = SchemaType.ofSingle("object");
		mtObj.schema = ReferenceOr.ofValue(sObj);
		rbObj.content.put("application/json", mtObj);
		op5.requestBody = ReferenceOr.ofValue(rbObj);
		pi.patch = op5;

		// Operation 6: RequestBody with null content, empty content, null mt.schema,
		// empty schema ReferenceOr, and unresolved ref
		Operation op6 = new Operation();
		op6.operationId = "op6";
		op6.requestBody = new ReferenceOr<>(null, null);
		pi.head = op6;

		Operation op7 = new Operation();
		op7.operationId = "op7";
		RequestBody rbEmptyContent = new RequestBody();
		rbEmptyContent.content = new LinkedHashMap<>();
		op7.requestBody = ReferenceOr.ofValue(rbEmptyContent);
		pi.options = op7;

		Operation op8 = new Operation();
		op8.operationId = "op8";
		RequestBody rbNullSchema = new RequestBody();
		rbNullSchema.content = new LinkedHashMap<>();
		MediaType mtNullSchema = new MediaType();
		rbNullSchema.content.put("application/json", mtNullSchema);
		op8.requestBody = ReferenceOr.ofValue(rbNullSchema);
		pi.trace = op8;

		Operation op9 = new Operation();
		op9.operationId = "op9";
		RequestBody rbEmptyRefOrSchema = new RequestBody();
		rbEmptyRefOrSchema.content = new LinkedHashMap<>();
		MediaType mtEmptyRefOr = new MediaType();
		mtEmptyRefOr.schema = new ReferenceOr<>(null, null);
		rbEmptyRefOrSchema.content.put("application/json", mtEmptyRefOr);
		op9.requestBody = ReferenceOr.ofValue(rbEmptyRefOrSchema);
		pi.query = op9;

		api.paths.pathItems.put("/edge", pi);
		api.paths.pathItems.put("/emptyPathItem", new PathItem());

		JSONObject res = new JSONObject(Emit.emitDocsJson(api, false, false));
		JSONObject edge = res.getJSONObject("endpoints").getJSONObject("/edge");
		assertTrue(edge.getJSONObject("get").getString("snippet").contains("new Order()"));
		assertTrue(edge.getJSONObject("post").getString("snippet").contains("new CustomPayload()"));
		assertTrue(edge.getJSONObject("put").getString("snippet").contains(Q + "rawBody" + Q));
		assertTrue(edge.getJSONObject("delete").getString("snippet").contains("client.op4(1)"));
		assertTrue(edge.getJSONObject("patch").getString("snippet").contains("client.op5(" + Q + "{}" + Q + ")"));
		assertFalse(res.getJSONObject("endpoints").has("/emptyPathItem"));

		// Test components with null maps and unresolved refs
		PathItem piNoComp = new PathItem();
		Operation opRbRefNoComp = new Operation();
		opRbRefNoComp.requestBody = ReferenceOr.ofRef("#/components/requestBodies/MissingNoComp");
		opRbRefNoComp.parameters = Collections.singletonList(ReferenceOr.ofRef("#/components/parameters/PNoComp"));
		piNoComp.get = opRbRefNoComp;

		Operation opRbSchemaNullType = new Operation();
		RequestBody rbSchemaNullType = new RequestBody();
		rbSchemaNullType.content = new LinkedHashMap<>();
		MediaType mtSchemaNullType = new MediaType();
		mtSchemaNullType.schema = ReferenceOr.ofValue(new Schema());
		rbSchemaNullType.content.put("application/json", mtSchemaNullType);
		opRbSchemaNullType.requestBody = ReferenceOr.ofValue(rbSchemaNullType);
		piNoComp.post = opRbSchemaNullType;

		api.paths.pathItems.put("/nocomp", piNoComp);
		Emit.emitDocsJson(api, false, false);

		api.components = new Components();
		Emit.emitDocsJson(api, false, false);

		api.components.parameters = new LinkedHashMap<>();
		api.components.schemas = new LinkedHashMap<>();
		api.components.schemas.put("S1", new ReferenceOr<>(null, null));
		api.components.requestBodies = new LinkedHashMap<>();

		PathItem pi2 = new PathItem();
		Operation opUnresRb = new Operation();
		opUnresRb.requestBody = ReferenceOr.ofRef("#/components/requestBodies/MissingRb");

		Parameter pNestedUnres = new Parameter();
		pNestedUnres.name = "pnu";
		Schema sNestedUnres = new Schema();
		sNestedUnres.$ref = "#/components/schemas/S1";
		pNestedUnres.schema = ReferenceOr.ofValue(sNestedUnres);
		opUnresRb.parameters = Collections.singletonList(ReferenceOr.ofValue(pNestedUnres));

		pi2.get = opUnresRb;
		api.paths.pathItems.put("/edge2", pi2);
		Emit.emitDocsJson(api, false, false);

		Operation opNullRbContent = new Operation();
		opNullRbContent.requestBody = ReferenceOr.ofValue(new RequestBody());
		Parameter pNoTypeSchema = new Parameter();
		pNoTypeSchema.name = "pnt";
		pNoTypeSchema.schema = ReferenceOr.ofValue(new Schema());
		opNullRbContent.parameters = Collections.singletonList(ReferenceOr.ofValue(pNoTypeSchema));
		pi2.post = opNullRbContent;
		Emit.emitDocsJson(api, false, false);
	}

	/**
	 * Verifies OpenAPI {@code webhooks} and Arazzo {@code workflows} emission in
	 * {@link Emit#emitDocsJson}.
	 *
	 * @throws Exception
	 *             If generation fails.
	 */
	@Test
	public void testWebhooksAndArazzoWorkflows() throws Exception {
		OpenAPI api = new OpenAPI();
		api.webhooks = new LinkedHashMap<>();
		Emit.emitDocsJson(api, false, false);

		// Empty webhook PathItem -> webhooks object not added when empty
		api.webhooks.put("emptyHook", new PathItem());
		JSONObject emptyHooksRes = new JSONObject(Emit.emitDocsJson(api, false, false));
		assertFalse(emptyHooksRes.has("webhooks"));

		PathItem hookPi = new PathItem();
		Operation hookOp = new Operation();
		hookOp.operationId = "onNewPet";
		hookPi.post = hookOp;
		api.webhooks.put("newPet", hookPi);

		Arazzo arazzo = new Arazzo();
		Emit.emitDocsJson(null, arazzo, false, false);
		arazzo.workflows = new ArrayList<>();
		Emit.emitDocsJson(null, arazzo, false, false);

		// Workflow 1: with inputs schema (including null property schema)
		Workflow wf1 = new Workflow();
		wf1.workflowId = "placeOrderWorkflow";
		Schema inSchema = new Schema();
		inSchema.properties = new LinkedHashMap<>();
		Schema prop1 = new Schema();
		prop1.type = SchemaType.ofSingle("string");
		inSchema.properties.put("petId", ReferenceOr.ofValue(prop1));
		inSchema.properties.put("nullProp", new ReferenceOr<>(null, null));
		wf1.inputs = ReferenceOr.ofValue(inSchema);
		arazzo.workflows.add(wf1);

		// Workflow 2: with inputs schema having null properties and workflow parameters
		Workflow wf2 = new Workflow();
		wf2.workflowId = "";
		wf2.inputs = ReferenceOr.ofValue(new Schema());
		arazzo.workflows.add(wf2);

		// Workflow 3: with null inputs and workflow parameters
		Workflow wf3 = new Workflow();
		wf3.workflowId = null;
		wf3.parameters = new ArrayList<>();
		ArazzoParameter ap1 = new ArazzoParameter();
		ap1.name = "token";
		ap1.value = PayloadValue.ofString("secret");
		wf3.parameters.add(ReusableOr.ofItem(ap1));

		ArazzoParameter apNullVal = new ArazzoParameter();
		apNullVal.name = "empty";
		apNullVal.value = null;
		wf3.parameters.add(ReusableOr.ofItem(apNullVal));

		wf3.parameters.add(ReusableOr.ofReference("$components.parameters.missing"));
		arazzo.workflows.add(wf3);

		// Workflow 4: with null inputs and null parameters
		Workflow wf4 = new Workflow();
		wf4.workflowId = "bareWorkflow";
		arazzo.workflows.add(wf4);

		JSONObject combined = new JSONObject(Emit.emitDocsJson(api, arazzo, false, false));
		Emit.emitDocsJson(null, arazzo, false, false);
		assertTrue(combined.has("webhooks"));
		assertEquals("var response = client.onNewPet();" + NL + "System.out.println(response);",
				combined.getJSONObject("webhooks").getJSONObject("newPet").getJSONObject("post").getString("snippet"));

		assertTrue(combined.has("workflows"));
		JSONObject wfs = combined.getJSONObject("workflows");
		assertTrue(wfs.getJSONObject("placeOrderWorkflow").getString("snippet").contains(
				"client.placeOrderWorkflow(" + Q + "petId_example" + Q + ", " + Q + "nullProp_example" + Q + ")"));
		assertTrue(wfs.getJSONObject("runWorkflow").getString("snippet")
				.contains("client.runWorkflow(" + Q + "secret" + Q + ")"));
		assertTrue(wfs.getJSONObject("bareWorkflow").getString("snippet").contains("client.bareWorkflow()"));
	}

	/**
	 * Verifies CLI {@code to_docs_json} writes to {@code
	 *
	<dir>
	 * /docs.json} when {@code -o} points to an existing directory.
	 *
	 * @throws Exception
	 *             If CLI execution fails.
	 */
	@Test
	public void testCliDirectoryOutputHandling() throws Exception {
		File tempDir = Files.createTempDirectory("cdd_docs_json_dir_test").toFile();
		File specFile = new File(tempDir, "openapi.json");
		Files.writeString(specFile.toPath(),
				"{" + Q + "openapi" + Q + ":" + Q + "3.2.0" + Q + "," + Q + "info" + Q + ":{" + Q + "title" + Q + ":"
						+ Q + "Test" + Q + "," + Q + "version" + Q + ":" + Q + "1.0" + Q + "}," + Q + "paths" + Q + ":{"
						+ Q + "/items" + Q + ":{" + Q + "get" + Q + ":{" + Q + "operationId" + Q + ":" + Q + "getItems"
						+ Q + "}}}}");

		File outDir = new File(tempDir, "target_dir");
		assertTrue(outDir.mkdirs());

		int exitCode = CddCli
				.run(new String[]{"to_docs_json", "-i", specFile.getAbsolutePath(), "-o", outDir.getAbsolutePath()});
		assertEquals(0, exitCode);

		File expectedFile = new File(outDir, "docs.json");
		assertTrue(expectedFile.exists());
		JSONObject parsed = new JSONObject(Files.readString(expectedFile.toPath()));
		assertEquals("var response = client.getItems();" + NL + "System.out.println(response);",
				parsed.getJSONObject("endpoints").getJSONObject("/items").getJSONObject("get").getString("snippet"));
	}
}
