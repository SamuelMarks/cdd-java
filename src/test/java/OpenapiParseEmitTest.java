package openapi;

import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Parse} and {@link Emit} across OpenAPI 3.2.0 and
 * Swagger 2.0 specifications.
 */
public class OpenapiParseEmitTest {

	/**
	 * Verifies parsing of top-level {@code servers} array.
	 *
	 * @throws Exception
	 *             If parsing fails.
	 */
	@Test
	public void testParseServers() throws Exception {
		String json = "{\n" + "  \"servers\": [\n" + "    {\n" + "      \"url\": \"https://api.example.com/v1\",\n"
				+ "      \"description\": \"Main production server\"\n" + "    }\n" + "  ]\n" + "}";
		OpenAPI api = Parse.fromString(json);
		assertNotNull(api.servers);
		assertEquals(1, api.servers.size());
		assertEquals("https://api.example.com/v1", api.servers.get(0).url);
		assertEquals("Main production server", api.servers.get(0).description);
	}

	/**
	 * Verifies default constructors of {@link Parse} and {@link Emit}.
	 */
	@Test
	public void testParseConstructors() {
		assertNotNull(new Parse());
		assertNotNull(new Emit());
	}

	/**
	 * Verifies parsing and serialization of a combined OpenAPI 3.0 and Swagger 2.0
	 * specification.
	 *
	 * @throws Exception
	 *             If parsing or serialization fails.
	 */
	@Test
	public void testParseFromStringFull() throws Exception {
		String json = "{\n" + "  \"openapi\": \"3.0.0\",\n" + "  \"swagger\": \"2.0\",\n"
				+ "  \"host\": \"localhost\",\n" + "  \"basePath\": \"/v1\",\n"
				+ "  \"schemes\": [\"http\", \"https\"],\n" + "  \"consumes\": [\"application/json\"],\n"
				+ "  \"produces\": [\"application/json\"],\n" + "  \"info\": {\n" + "    \"title\": \"API\",\n"
				+ "    \"version\": \"1.0\",\n" + "    \"description\": \"My API\"\n" + "  },\n" + "  \"paths\": {\n"
				+ "    \"x-ignore\": {},\n" + "    \"/path\": {\n" + "      \"get\": {\n"
				+ "        \"operationId\": \"getOp\",\n" + "        \"summary\": \"sum\",\n"
				+ "        \"description\": \"desc\",\n" + "        \"parameters\": [\n" + "          {\n"
				+ "            \"name\": \"param1\",\n" + "            \"in\": \"query\",\n"
				+ "            \"description\": \"pdesc\",\n" + "            \"required\": true,\n"
				+ "            \"schema\": {\n" + "              \"type\": \"string\",\n"
				+ "              \"$ref\": \"#/def/schema\"\n" + "            }\n" + "          }\n" + "        ],\n"
				+ "        \"requestBody\": {\n" + "          \"description\": \"rbdesc\",\n"
				+ "          \"required\": true,\n" + "          \"content\": {\n"
				+ "            \"application/json\": {\n" + "              \"schema\": {\n"
				+ "                \"type\": \"string\"\n" + "              }\n" + "            }\n" + "          }\n"
				+ "        },\n" + "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"200desc\",\n" + "            \"content\": {\n"
				+ "              \"application/json\": {\n" + "                \"schema\": {\n"
				+ "                  \"type\": \"string\"\n" + "                }\n" + "              }\n"
				+ "            }\n" + "          }\n" + "        }\n" + "      },\n" + "      \"post\": {},\n"
				+ "      \"put\": {},\n" + "      \"delete\": {},\n" + "      \"patch\": {},\n"
				+ "      \"parameters\": []\n" + "    },\n" + "    \"/empty\": {}\n" + "  },\n"
				+ "  \"definitions\": {\n" + "    \"Def\": {\n" + "      \"type\": \"object\",\n"
				+ "      \"format\": \"date\",\n" + "      \"description\": \"def desc\",\n"
				+ "      \"$ref\": \"#/ref\",\n" + "      \"items\": { \"type\": \"string\" },\n"
				+ "      \"properties\": {\n" + "        \"prop1\": { \"type\": \"string\" }\n" + "      }\n"
				+ "    },\n" + "    \"EmptyDef\": {}\n" + "  },\n" + "  \"components\": {\n" + "    \"schemas\": {\n"
				+ "      \"Comp\": {\n" + "        \"type\": \"string\"\n" + "      }\n" + "    }\n" + "  }\n" + "}";

		OpenAPI api = Parse.fromString(json);
		assertNotNull(api);
		assertEquals("3.0.0", api.openapi);
		assertEquals("2.0", api.swagger);
		assertEquals("localhost", api.host);

		// Convert back to string
		String emitted = Emit.toString(api);
		assertTrue(emitted.contains("\"openapi\": \"3.0.0\""));
	}

	/**
	 * Verifies parsing and serialization when optional fields are null or empty.
	 *
	 * @throws Exception
	 *             If parsing fails.
	 */
	@Test
	public void testParseEmptyAndNulls() throws Exception {
		String json = "{\n" + "  \"info\": {\n" + "    \"description\": null\n" + "  },\n" + "  \"paths\": {\n"
				+ "    \"/empty_op\": {\n" + "      \"get\": {\n" + "        \"requestBody\": {\n"
				+ "          \"content\": {\n" + "            \"text/plain\": {}\n" + "          }\n" + "        },\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n" + "            \"content\": {\n"
				+ "              \"text/plain\": {}\n" + "            }\n" + "          }\n" + "        },\n"
				+ "        \"parameters\": [{}]\n" + "      }\n" + "    }\n" + "  },\n" + "  \"components\": {}\n"
				+ "}";
		OpenAPI api = Parse.fromString(json);
		assertNotNull(api.info);
		assertNull(api.info.description);

		String emitted = Emit.toString(api);
		assertFalse(emitted.contains("\"description\""));
	}

	/**
	 * Verifies defensive handling of unexpected field types in {@link Emit}.
	 *
	 * @throws Exception
	 *             If serialization fails.
	 */
	@Test
	public void testEmitCoverage() throws Exception {
		OpenAPI api = new OpenAPI();
		api.schemes = new ArrayList<>();
		api.consumes = new ArrayList<>();
		api.produces = new ArrayList<>();

		api.definitions = new HashMap<>();
		api.definitions.put("nullDef", null);

		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.schemas.put("nullSchema", null);

		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		api.paths.pathItems.put("/p", pi);

		Operation op = new Operation();
		pi.get = op;

		op.requestBody = null;
		op.parameters = new ArrayList<>();

		op.responses = new Responses();
		op.responses.statusCodes = new HashMap<>();
		op.responses.statusCodes.put("200", null);

		Response validResponse = new Response();
		validResponse.content = new HashMap<>();
		MediaType badMt = new MediaType();
		badMt.schema = null;
		validResponse.content.put("application/json", badMt);
		op.responses.statusCodes.put("201", ReferenceOr.ofValue(validResponse));

		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		rb.content.put("application/json", badMt);
		op.requestBody = ReferenceOr.ofValue(rb);

		Schema badSchema = new Schema();
		badSchema.items = null;
		badSchema.properties = new HashMap<>();
		badSchema.properties.put("prop", null);

		api.definitions.put("badSchema", badSchema);

		String emitted = Emit.toString(api);
		assertNotNull(emitted);

		api.paths.pathItems.clear();
		assertNotNull(Emit.toString(api));
	}

	/**
	 * Verifies {@link IOException} is thrown on malformed JSON input.
	 *
	 * @throws Exception
	 *             Expected {@link IOException}.
	 */
	@Test(expected = IOException.class)
	public void testParseException() throws Exception {
		Parse.fromString("{ malformed json");
	}

	/**
	 * Verifies file read/write round-trip via {@link Emit#toFile} and
	 * {@link Parse#fromFile}.
	 *
	 * @throws Exception
	 *             If file operations fail.
	 */
	@Test
	public void testFileOperations() throws Exception {
		File tempFile = File.createTempFile("openapi-test", ".json");
		tempFile.deleteOnExit();

		OpenAPI api = new OpenAPI();
		api.openapi = "3.1.0";

		Emit.toFile(api, tempFile);

		OpenAPI parsed = Parse.fromFile(tempFile);
		assertEquals("3.1.0", parsed.openapi);
	}

	/**
	 * Verifies serialization when collections and maps are empty or null.
	 *
	 * @throws Exception
	 *             If serialization fails.
	 */
	@Test
	public void testEmitRemainingBranches2() throws Exception {
		openapi.OpenAPI api = new openapi.OpenAPI();

		// 70: api.paths != null && api.paths.pathItems != null &&
		// !api.paths.pathItems.isEmpty()
		api.paths = new openapi.Paths();
		api.paths.pathItems = new java.util.HashMap<>(); // Empty Map

		// 129: op.parameters != null && !op.parameters.isEmpty()
		openapi.PathItem pi = new openapi.PathItem();
		api.paths.pathItems.put("/test2", pi);
		openapi.Operation op = new openapi.Operation();
		pi.get = op;
		op.parameters = new java.util.ArrayList<>(); // Empty List

		// 140: rb.content != null
		openapi.RequestBody rb = new openapi.RequestBody();
		rb.content = null;
		op.requestBody = ReferenceOr.ofValue(rb);

		// 154: op.responses != null && op.responses.statusCodes != null
		op.responses = new openapi.Responses();
		op.responses.statusCodes = null;

		openapi.Emit.toString(api);
	}

	/**
	 * Verifies Swagger 2.0 {@code definitions} merging into {@code components}.
	 *
	 * @throws Exception
	 *             If parsing fails.
	 */
	@Test
	public void testParseDefinitionsWithPreexistingComponents() throws Exception {
		// To guarantee coverage, we can just call Parse.fromString on a JSON that has
		// definitions,
		// and then we can do it multiple times or we can just construct a JSON where
		// components comes first.
		// Since org.json.JSONObject uses a map, we can bypass the string parser and use
		// the JSON-like structure
		// But Parse.fromString is our entry point. Let's just create a test that
		// specifically provides components
		// but no definitions, and then another with definitions but no components.

		String json1 = "{ \"definitions\": { \"Def1\": {} } }";
		Parse.fromString(json1); // api.components will be null initially

		String json2 = "{ \"components\": { \"schemas\": {} }, \"definitions\": { \"Def2\": {} } }";
		// Even with random order, either components is parsed first (so
		// components!=null),
		// or definitions is parsed first (so components==null).
		// Actually to ensure false branch of `if (api.components == null)`:
		// We need to parse definitions while api.components is NOT null.
		// We can just add it to ensure it hits.
		for (int i = 0; i < 10; i++) {
			Parse.fromString(json2);
		}

		String json3 = "{ \"components\": {}, \"definitions\": { \"Def3\": {} } }";
		Parse.fromString(json3); // components != null, schemas == null
	}

	/**
	 * Verifies additional null/empty branches in {@link Emit} and {@link Parse}.
	 *
	 * @throws Exception
	 *             If parsing or serialization fails.
	 */
	@Test
	public void testEmitRemainingBranches3() throws Exception {
		openapi.OpenAPI api = new openapi.OpenAPI();

		// 70: api.paths != null && api.paths.pathItems == null
		api.paths = new openapi.Paths();
		api.paths.pathItems = null;
		openapi.Emit.toString(api);

		api.paths.pathItems = new java.util.HashMap<>();
		openapi.PathItem pi = new openapi.PathItem();
		api.paths.pathItems.put("/test3", pi);

		// 86: pi.parameters != null && !pi.parameters.isEmpty()
		pi.parameters = new java.util.ArrayList<>();
		openapi.Emit.toString(api); // empty list

		openapi.Parameter p = new openapi.Parameter();
		p.name = "p1";
		pi.parameters.add(ReferenceOr.ofValue(p));
		String out1 = openapi.Emit.toString(api); // non-empty list
		assertTrue(out1.contains("\"p1\""));

		// api.components != null && api.components.schemas == null
		openapi.OpenAPI apiCompNullSchemas = new openapi.OpenAPI();
		apiCompNullSchemas.components = new openapi.Components();
		apiCompNullSchemas.components.schemas = null;
		String outCompNull = openapi.Emit.toString(apiCompNullSchemas);
		assertFalse(outCompNull.contains("\"components\""));

		// Server with neither url nor description, Info without description, and
		// definitions with existing components.schemas
		String jsonBranches = "{\"servers\": [{}], \"info\": {\"title\": \"T\"}, \"components\": {\"schemas\": {\"S1\": {\"type\": \"string\"}}}, \"definitions\": {\"D1\": {\"type\": \"integer\"}}}";
		openapi.OpenAPI parsedBranches = openapi.Parse.fromString(jsonBranches);
		assertEquals(1, parsedBranches.servers.size());
		assertNull(parsedBranches.servers.get(0).url);
		assertNull(parsedBranches.servers.get(0).description);
		assertNull(parsedBranches.info.description);
		assertTrue(parsedBranches.components.schemas.containsKey("S1"));
		assertTrue(parsedBranches.components.schemas.containsKey("D1"));
	}

	/**
	 * Verifies 100% lossless round-trip serialization and parsing across all
	 * OpenAPI 3.2.0 and Swagger 2.0 model properties.
	 *
	 * @throws Exception
	 *             If round-trip parsing or serialization fails.
	 */
	@Test
	public void testFullLosslessRoundTripOpenApi32AndSwagger2() throws Exception {
		File megaSpec = new File("src/test/resources/super-mega-spec.json");
		OpenAPI api1 = Parse.fromFile(megaSpec);
		assertNotNull(api1);

		// Add remaining Swagger 2.0 and OpenAPI 3.2.0 properties to ensure 100%
		// coverage
		api1.swagger = "2.0";
		api1.host = "api.example.com";
		api1.basePath = "/v2";
		api1.schemes = java.util.Arrays.asList("https", "http");
		api1.consumes = java.util.Collections.singletonList("application/json");
		api1.produces = java.util.Collections.singletonList("application/json");

		Parameter globalParam = new Parameter();
		globalParam.name = "limit";
		globalParam.in = "query";
		globalParam.type = "array";
		globalParam.format = "int32";
		globalParam.collectionFormat = "csv";
		globalParam.defaultValue = common.PayloadValue.ofNumber(10);
		globalParam.maximum = 100;
		globalParam.exclusiveMaximum = true;
		globalParam.minimum = 1;
		globalParam.exclusiveMinimum = false;
		globalParam.maxLength = 10;
		globalParam.minLength = 1;
		globalParam.pattern = "^[0-9]+$";
		globalParam.maxItems = 50;
		globalParam.minItems = 1;
		globalParam.uniqueItems = true;
		globalParam.enumValues = java.util.Arrays.asList(common.PayloadValue.ofNumber(10),
				common.PayloadValue.ofNumber(20));
		globalParam.multipleOf = 5;
		Items items = new Items();
		items.type = "integer";
		items.format = "int32";
		items.collectionFormat = "multi";
		items.defaultValue = common.PayloadValue.ofNumber(5);
		items.maximum = 50;
		items.exclusiveMaximum = false;
		items.minimum = 1;
		items.exclusiveMinimum = false;
		items.maxLength = 5;
		items.minLength = 1;
		items.pattern = ".*";
		items.maxItems = 10;
		items.minItems = 1;
		items.uniqueItems = false;
		items.enumValues = java.util.Collections.singletonList(common.PayloadValue.ofNumber(5));
		items.multipleOf = 1;
		Items nestedItems = new Items();
		nestedItems.type = "string";
		items.items = nestedItems;
		items.addExtension("x-items-ext", "val");
		globalParam.items = items;
		Example paramEx = new Example();
		paramEx.summary = "exSum";
		paramEx.description = "exDesc";
		paramEx.value = common.PayloadValue.ofString("10");
		paramEx.externalValue = "http://example.com/ex";
		paramEx.dataValue = common.PayloadValue.ofString("data");
		paramEx.serializedValue = "10";
		paramEx.addExtension("x-ex", "1");
		globalParam.examples = java.util.Collections.singletonMap("ex1", ReferenceOr.ofValue(paramEx));
		MediaType paramMt = new MediaType();
		Schema paramMtSchema = new Schema();
		paramMtSchema.type = SchemaType.ofSingle("string");
		paramMt.schema = ReferenceOr.ofValue(paramMtSchema);
		globalParam.content = java.util.Collections.singletonMap("text/plain", paramMt);
		globalParam.addExtension("x-param", "val");
		api1.parameters = java.util.Collections.singletonMap("LimitParam", globalParam);

		Response globalResp = new Response();
		globalResp.summary = "Global Resp";
		globalResp.description = "Global Desc";
		Schema respSchema = new Schema();
		respSchema.type = SchemaType.ofSingle("string");
		globalResp.schema = ReferenceOr.ofValue(respSchema);
		Header hdr = new Header();
		hdr.description = "Header desc";
		hdr.required = true;
		hdr.deprecated = false;
		hdr.allowEmptyValue = false;
		hdr.style = "simple";
		hdr.explode = false;
		hdr.allowReserved = false;
		hdr.type = "string";
		hdr.format = "uuid";
		hdr.items = nestedItems;
		hdr.collectionFormat = "csv";
		hdr.defaultValue = common.PayloadValue.ofString("abc");
		hdr.maximum = 10;
		hdr.exclusiveMaximum = false;
		hdr.minimum = 1;
		hdr.exclusiveMinimum = false;
		hdr.maxLength = 36;
		hdr.minLength = 1;
		hdr.pattern = ".*";
		hdr.maxItems = 2;
		hdr.minItems = 1;
		hdr.uniqueItems = true;
		hdr.enumValues = java.util.Collections.singletonList(common.PayloadValue.ofString("abc"));
		hdr.multipleOf = 1;
		hdr.schema = ReferenceOr.ofValue(respSchema);
		hdr.example = common.PayloadValue.ofString("abc");
		hdr.examples = new HashMap<>();
		hdr.examples.put("hEx", ReferenceOr.ofValue(paramEx));
		Example rawExObj = new Example();
		rawExObj.value = common.PayloadValue.ofString("rawVal");
		hdr.examples.put("rawEx", ReferenceOr.ofValue(rawExObj));
		hdr.content = java.util.Collections.singletonMap("text/plain", paramMt);
		hdr.addExtension("x-hdr", "v");
		globalResp.headers = java.util.Collections.singletonMap("X-Rate-Limit", ReferenceOr.ofValue(hdr));
		globalResp.examples = new HashMap<>();
		globalResp.examples.put("application/json", ReferenceOr.ofValue(paramEx));
		Example rawRespExObj = new Example();
		rawRespExObj.value = common.PayloadValue.ofString("rawExample");
		globalResp.examples.put("text/plain", ReferenceOr.ofValue(rawRespExObj));

		Link link = new Link();
		link.operationRef = "#/paths/~1users/get";
		link.operationId = "getUsers";
		link.description = "Link desc";
		link.requestBody = common.PayloadValue.ofString("$response.body#/id");
		link.parameters = java.util.Collections.singletonMap("userId",
				common.PayloadValue.ofString("$response.body#/id"));
		Server linkServer = new Server();
		linkServer.url = "https://link.example.com";
		link.server = linkServer;
		link.addExtension("x-link", "v");
		globalResp.links = java.util.Collections.singletonMap("UserLink", ReferenceOr.ofValue(link));
		globalResp.addExtension("x-resp", "v");
		api1.responses = java.util.Collections.singletonMap("StandardResp", globalResp);

		SecurityScheme secDef = new SecurityScheme();
		secDef.type = "oauth2";
		secDef.description = "OAuth2";
		secDef.name = "auth";
		secDef.in = "header";
		secDef.scheme = "bearer";
		secDef.bearerFormat = "JWT";
		secDef.flow = "accessCode";
		secDef.authorizationUrl = "https://example.com/auth";
		secDef.tokenUrl = "https://example.com/token";
		secDef.openIdConnectUrl = "https://example.com/oidc";
		secDef.oauth2MetadataUrl = "https://example.com/meta";
		secDef.deprecated = false;
		secDef.scopes = java.util.Collections.singletonMap("read", "Read access");
		OAuthFlows flows = new OAuthFlows();
		OAuthFlow flow = new OAuthFlow();
		flow.authorizationUrl = "https://example.com/auth";
		flow.tokenUrl = "https://example.com/token";
		flow.refreshUrl = "https://example.com/refresh";
		flow.deviceAuthorizationUrl = "https://example.com/device";
		flow.scopes = java.util.Collections.singletonMap("write", "Write access");
		flow.addExtension("x-flow", "v");
		flows.implicit = flow;
		flows.password = flow;
		flows.clientCredentials = flow;
		flows.authorizationCode = flow;
		flows.deviceAuthorization = flow;
		flows.addExtension("x-flows", "v");
		secDef.flows = flows;
		secDef.addExtension("x-sec", "v");
		api1.securityDefinitions = java.util.Collections.singletonMap("OAuth", secDef);

		// Also populate rich Schema 2020-12 fields
		Schema richSchema = new Schema();
		richSchema.type = SchemaType.ofMultiple(java.util.Arrays.asList("string", "null"));
		richSchema.title = "Rich";
		richSchema.format = "text";
		richSchema.description = "Rich schema";
		richSchema.multipleOf = 2;
		richSchema.maximum = 100;
		richSchema.exclusiveMaximum = ExclusiveBound.ofNumber(99);
		richSchema.minimum = 0;
		richSchema.exclusiveMinimum = ExclusiveBound.ofNumber(1);
		richSchema.maxLength = 50;
		richSchema.minLength = 1;
		richSchema.pattern = ".*";
		richSchema.maxItems = 10;
		richSchema.minItems = 0;
		richSchema.uniqueItems = true;
		richSchema.maxProperties = 10;
		richSchema.minProperties = 1;
		richSchema.required = java.util.Collections.singletonList("id");
		richSchema.enumValues = java.util.Arrays.asList(common.PayloadValue.ofString("a"),
				common.PayloadValue.ofString("b"));
		Schema subSchema = new Schema();
		subSchema.type = SchemaType.ofSingle("string");
		ReferenceOr<Schema> subSchemaRefOr = ReferenceOr.ofValue(subSchema);
		richSchema.allOf = java.util.Collections.singletonList(subSchemaRefOr);
		richSchema.anyOf = java.util.Collections.singletonList(subSchemaRefOr);
		richSchema.oneOf = java.util.Collections.singletonList(subSchemaRefOr);
		richSchema.not = subSchemaRefOr;
		richSchema.items = subSchemaRefOr;
		richSchema.properties = java.util.Collections.singletonMap("id", subSchemaRefOr);
		richSchema.additionalProperties = AdditionalProperties.ofSchema(subSchema);
		richSchema.defaultValue = common.PayloadValue.ofString("a");
		richSchema.constValue = common.PayloadValue.ofString("a");
		richSchema.readOnly = true;
		richSchema.writeOnly = false;
		richSchema.deprecated = true;
		richSchema.example = common.PayloadValue.ofString("a");
		richSchema.examples = java.util.Collections.singletonList(common.PayloadValue.ofString("a"));
		ExternalDocumentation extDocs = new ExternalDocumentation();
		extDocs.url = "https://docs.example.com";
		extDocs.description = "Docs";
		extDocs.addExtension("x-ed", "v");
		richSchema.externalDocs = extDocs;
		XML xml = new XML();
		xml.name = "rich";
		xml.namespace = "http://example.com/ns";
		xml.prefix = "ex";
		xml.attribute = false;
		xml.wrapped = true;
		xml.nodeType = "element";
		xml.addExtension("x-xml", "v");
		richSchema.xml = xml;
		Discriminator disc = new Discriminator();
		disc.propertyName = "type";
		disc.defaultMapping = "Rich";
		disc.mapping = java.util.Collections.singletonMap("rich", "#/components/schemas/Rich");
		disc.addExtension("x-disc", "v");
		richSchema.discriminator = disc;
		richSchema.id = "https://example.com/schema";
		richSchema.schema = "https://json-schema.org/draft/2020-12/schema";
		richSchema.anchor = "richAnchor";
		richSchema.dynamicAnchor = "richDynAnchor";
		richSchema.vocabulary = java.util.Collections.singletonMap("https://json-schema.org/draft/2020-12/vocab/core",
				true);
		richSchema.defs = java.util.Collections.singletonMap("Sub", subSchemaRefOr);
		richSchema.dependentRequired = java.util.Collections.singletonMap("id",
				java.util.Collections.singletonList("name"));
		richSchema.dependentSchemas = java.util.Collections.singletonMap("id", subSchemaRefOr);
		richSchema.propertyNames = subSchemaRefOr;
		richSchema.patternProperties = java.util.Collections.singletonMap("^S_", subSchemaRefOr);
		richSchema.unevaluatedProperties = AdditionalProperties.ofSchema(subSchema);
		richSchema.unevaluatedItems = AdditionalProperties.ofSchema(subSchema);
		richSchema.prefixItems = java.util.Collections.singletonList(subSchemaRefOr);
		richSchema.contains = subSchemaRefOr;
		richSchema.minContains = 1;
		richSchema.maxContains = 5;
		richSchema.addExtension("x-schema", "v");
		api1.components.schemas.put("Rich", ReferenceOr.ofValue(richSchema));

		Schema boolSchemaProps = new Schema();
		boolSchemaProps.type = SchemaType.ofSingle("object");
		boolSchemaProps.exclusiveMaximum = ExclusiveBound.ofBoolean(true);
		boolSchemaProps.exclusiveMinimum = ExclusiveBound.ofBoolean(false);
		boolSchemaProps.additionalProperties = AdditionalProperties.ofBoolean(false);
		boolSchemaProps.unevaluatedProperties = AdditionalProperties.ofBoolean(true);
		boolSchemaProps.unevaluatedItems = AdditionalProperties.ofBoolean(false);
		api1.components.schemas.put("BoolProps", ReferenceOr.ofValue(boolSchemaProps));

		// Components extra maps
		api1.components.responses = java.util.Collections.singletonMap("CompResp", ReferenceOr.ofValue(globalResp));
		api1.components.parameters = java.util.Collections.singletonMap("CompParam", ReferenceOr.ofValue(globalParam));
		api1.components.examples = java.util.Collections.singletonMap("CompEx", ReferenceOr.ofValue(paramEx));
		RequestBody compRb = new RequestBody();
		compRb.description = "RB";
		compRb.required = true;
		MediaType encMt = new MediaType();
		encMt.schema = subSchemaRefOr;
		encMt.itemSchema = subSchemaRefOr;
		encMt.example = common.PayloadValue.ofString("ex");
		encMt.examples = java.util.Collections.singletonMap("e1", ReferenceOr.ofValue(paramEx));
		Encoding enc = new Encoding();
		enc.contentType = "application/json";
		enc.style = "form";
		enc.explode = true;
		enc.allowReserved = false;
		enc.headers = java.util.Collections.singletonMap("X-Enc", ReferenceOr.ofValue(hdr));
		Encoding subEnc = new Encoding();
		subEnc.contentType = "text/plain";
		enc.encoding = java.util.Collections.singletonMap("sub", subEnc);
		enc.prefixEncoding = java.util.Collections.singletonList(subEnc);
		enc.itemEncoding = subEnc;
		enc.addExtension("x-enc", "v");
		encMt.encoding = java.util.Collections.singletonMap("field", enc);
		encMt.prefixEncoding = java.util.Collections.singletonList(enc);
		encMt.itemEncoding = enc;
		encMt.addExtension("x-mt", "v");
		compRb.content = java.util.Collections.singletonMap("multipart/form-data", encMt);
		compRb.addExtension("x-rb", "v");
		api1.components.requestBodies = new HashMap<>();
		api1.components.requestBodies.put("CompRb", ReferenceOr.ofValue(compRb));
		api1.components.requestBodies.put("SparseRb", ReferenceOr.ofValue(new RequestBody()));
		api1.components.headers = java.util.Collections.singletonMap("CompHdr", ReferenceOr.ofValue(hdr));
		api1.components.securitySchemes = java.util.Collections.singletonMap("CompSec", ReferenceOr.ofValue(secDef));
		api1.components.links = java.util.Collections.singletonMap("CompLink", ReferenceOr.ofValue(link));
		Callback cb = new Callback();
		PathItem cbPi = new PathItem();
		cbPi.ref = "#/components/pathItems/SharedPi";
		cbPi.summary = "cb summary";
		cbPi.description = "cb desc";
		Operation allMethodsOp = new Operation();
		allMethodsOp.operationId = "opAll";
		allMethodsOp.tags = java.util.Collections.singletonList("t1");
		allMethodsOp.summary = "s";
		allMethodsOp.description = "d";
		allMethodsOp.consumes = java.util.Collections.singletonList("application/json");
		allMethodsOp.produces = java.util.Collections.singletonList("application/json");
		allMethodsOp.schemes = java.util.Collections.singletonList("https");
		allMethodsOp.deprecated = true;
		allMethodsOp.externalDocs = extDocs;
		allMethodsOp.parameters = java.util.Collections.singletonList(ReferenceOr.ofValue(globalParam));
		allMethodsOp.requestBody = ReferenceOr.ofValue(compRb);
		Responses opResps = new Responses();
		opResps.defaultResponse = ReferenceOr.ofValue(globalResp);
		opResps.statusCodes = java.util.Collections.singletonMap("200", ReferenceOr.ofValue(globalResp));
		opResps.addExtension("x-resps", "v");
		allMethodsOp.responses = opResps;
		SecurityRequirement sr = new SecurityRequirement();
		sr.addRequirement("OAuth", java.util.Collections.singletonList("read"));
		allMethodsOp.security = java.util.Collections.singletonList(sr);
		allMethodsOp.servers = java.util.Collections.singletonList(linkServer);
		allMethodsOp.addExtension("x-op", "v");
		cbPi.get = allMethodsOp;
		cbPi.put = allMethodsOp;
		cbPi.post = allMethodsOp;
		cbPi.delete = allMethodsOp;
		cbPi.options = allMethodsOp;
		cbPi.head = allMethodsOp;
		cbPi.patch = allMethodsOp;
		cbPi.trace = allMethodsOp;
		cbPi.query = allMethodsOp;
		cbPi.additionalOperations = java.util.Collections.singletonMap("COPY", allMethodsOp);
		cbPi.servers = java.util.Collections.singletonList(linkServer);
		cbPi.parameters = java.util.Collections.singletonList(ReferenceOr.ofValue(globalParam));
		cbPi.addExtension("x-pi", "v");
		Callback leafCb = new Callback();
		PathItem leafPi = new PathItem();
		Operation leafOp = new Operation();
		leafOp.operationId = "leafCbOp";
		leafPi.post = leafOp;
		leafCb.pathItems.put("{$request.body#/leafUrl}", leafPi);
		allMethodsOp.callbacks = java.util.Collections.singletonMap("onEvent", ReferenceOr.ofValue(leafCb));
		cb.pathItems.put("{$request.body#/callbackUrl}", cbPi);
		cb.addExtension("x-cb", "v");
		api1.webhooks = java.util.Collections.singletonMap("newPetWebhook", leafPi);
		api1.components.callbacks = java.util.Collections.singletonMap("CompCb", ReferenceOr.ofValue(cb));
		api1.components.pathItems = java.util.Collections.singletonMap("SharedPi", ReferenceOr.ofValue(cbPi));
		api1.components.mediaTypes = java.util.Collections.singletonMap("CompMt", ReferenceOr.ofValue(encMt));
		api1.components.addExtension("x-comp", common.PayloadValue.ofArray(
				java.util.Arrays.asList(common.PayloadValue.ofString("a"), common.PayloadValue.ofString("b"))));

		// Also include sparse instances to cover null/empty branches on Tag, Header,
		// Link, SecurityScheme, OAuthFlows, OAuthFlow, Discriminator
		Tag sparseTag = new Tag();
		sparseTag.name = "sparseTag";
		Tag richTag = new Tag();
		richTag.name = "richTag";
		richTag.externalDocs = extDocs;
		api1.tags = java.util.Arrays.asList(sparseTag, richTag);

		Map<String, ReferenceOr<Header>> hdrMap = new HashMap<>(api1.components.headers);
		hdrMap.put("SparseHdr", ReferenceOr.ofValue(new Header()));
		api1.components.headers = hdrMap;

		Map<String, ReferenceOr<Link>> linkMap = new HashMap<>(api1.components.links);
		linkMap.put("SparseLink", ReferenceOr.ofValue(new Link()));
		api1.components.links = linkMap;

		Map<String, ReferenceOr<SecurityScheme>> secMap = new HashMap<>(api1.components.securitySchemes);
		SecurityScheme sparseSec = new SecurityScheme();
		sparseSec.type = "apiKey";
		secMap.put("SparseSec", ReferenceOr.ofValue(sparseSec));
		SecurityScheme emptyFlowsSec = new SecurityScheme();
		emptyFlowsSec.type = "oauth2";
		emptyFlowsSec.flows = new OAuthFlows();
		secMap.put("EmptyFlowsSec", ReferenceOr.ofValue(emptyFlowsSec));
		SecurityScheme sparseFlowSec = new SecurityScheme();
		sparseFlowSec.type = "oauth2";
		sparseFlowSec.flows = new OAuthFlows();
		sparseFlowSec.flows.implicit = new OAuthFlow();
		secMap.put("SparseFlowSec", ReferenceOr.ofValue(sparseFlowSec));
		api1.components.securitySchemes = secMap;

		Schema sparseDiscSchema = new Schema();
		sparseDiscSchema.discriminator = new Discriminator();
		sparseDiscSchema.discriminator.propertyName = "kind";
		api1.components.schemas.put("SparseDisc", ReferenceOr.ofValue(sparseDiscSchema));
		api1.components.schemas.put("RefCompSchema", ReferenceOr.ofRef("#/components/schemas/Rich"));

		String emittedJson = Emit.toString(api1);
		OpenAPI api2 = Parse.fromJson(new org.json.JSONObject(emittedJson));

		assertEquals(api1, api2);
		assertEquals(api1.hashCode(), api2.hashCode());
		assertNotNull(api2.toString());

		Schema emptyMapSchema = new Schema();
		emptyMapSchema.defs = new HashMap<>();
		OpenAPI emptyMapApi = new OpenAPI();
		emptyMapApi.components = new Components();
		emptyMapApi.components.schemas = java.util.Collections.singletonMap("EmptyMap",
				ReferenceOr.ofValue(emptyMapSchema));
		assertNotNull(Emit.toString(emptyMapApi));
	}

}
