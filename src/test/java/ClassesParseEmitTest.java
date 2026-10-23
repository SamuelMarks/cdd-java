import org.junit.Test;
import static org.junit.Assert.*;

import classes.Parse;
import classes.Emit;
import openapi.OpenAPI;
import openapi.Schema;
import openapi.Components;
import openapi.Paths;
import openapi.PathItem;
import openapi.Operation;
import openapi.Info;
import openapi.Parameter;
import openapi.XML;
import openapi.Discriminator;
import openapi.ExternalDocumentation;
import openapi.RequestBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link classes.Parse} and {@link classes.Emit}.
 */
public class ClassesParseEmitTest {

	/**
	 * Verifies enum parsing into OpenAPI string schemas with enum values.
	 */
	@Test
	public void testParseEnum() {
		String source = "/** My Enum */\n" + "public enum MyEnum {\n" + "    @JsonProperty(\"val_a\")\n"
				+ "    VAL_A,\n" + "    @Deprecated\n" + "    @JsonProperty(value = \"val_b\")\n" + "    VAL_B\n"
				+ "}\n" + "/**\n * @author x\n */\npublic enum EmptyDocEnum { A }\n" + "public enum NoDocEnum { X }\n";
		OpenAPI api = Parse.parse(source);
		assertNotNull(api.components.schemas.get("MyEnum"));
		Schema schema = api.components.schemas.get("MyEnum").value;
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.type);
		assertEquals("My Enum", schema.description);
		assertEquals(Arrays.asList(common.PayloadValue.ofString("val_a"), common.PayloadValue.ofString("VAL_B")),
				schema.enumValues);
	}

	/**
	 * Verifies DTO class parsing including XML, discriminator, inheritance, and
	 * property metadata.
	 */
	@Test
	public void testParseClass() {
		String source = "/**\n" + " * Class doc\n" + " * @xmlName xname\n" + " * @xmlNamespace xns\n"
				+ " * @xmlPrefix xpr\n" + " * @xmlAttribute true\n" + " * @xmlWrapped false\n"
				+ " * @discriminatorProperty type\n" + " * @discriminatorMapping foo FooType\n"
				+ " * @discriminatorMapping nospace\n" + " * @unknownTag ignored\n"
				+ " * @discriminatorDefault defaultMapping\n" + " * @schemaExample {ex}\n"
				+ " * @schemaExternalDocs http://doc doc_desc\n" + " */\n" + "@Deprecated\n"
				+ "@JsonTypeInfo(\"simple\")\n" + "@JsonTypeInfo(use=Id.NAME, property=\"type\")\n"
				+ "public class MyClass extends BaseClass {\n" + "    /**\n" + "     * prop doc\n"
				+ "     * @xmlName xpname\n" + "     * @xmlNamespace xpns\n" + "     * @xmlPrefix xppr\n"
				+ "     * @xmlAttribute false\n" + "     * @xmlWrapped true\n" + "     * @schemaExample exprop\n"
				+ "     * @schemaExternalDocs http://pdoc pdesc\n" + "     * @unknownPropTag ignored\n" + "     */\n"
				+ "    @Deprecated\n" + "    @JsonProperty(value = \"ignored\")\n" + "    @JsonProperty(\"prop_a\")\n"
				+ "    public String propA;\n" + "    private int propB;\n" + "    String pkgProp;\n" + "}\n" + "/**\n"
				+ " * @schemaExternalDocs http://nospace\n" + " */\n"
				+ "@JsonTypeInfo(use=Id.NAME, property=\"type\")\n" + "public class NoSpaceClass {}\n"
				+ "public interface MyInterface {}\n" + "public class MyClient {}\n" + "class NospaceServer { \n"
				+ "    /**\n" + "     * @schemaExternalDocs http://nospace\n" + "     */\n"
				+ "    public String myProp;\n" + "}\n" + "public class MyMockServer {}\n"
				+ "public class MyIntegrationTest {}\n";

		OpenAPI api = Parse.parse(source);
		Schema schema = api.components.schemas.get("MyClass").value;
		assertNotNull(schema);
		assertEquals("Class doc", schema.description);

		assertNotNull(schema.xml);
		assertEquals("xname", schema.xml.name);
		assertEquals("xns", schema.xml.namespace);
		assertEquals("xpr", schema.xml.prefix);
		assertTrue(schema.xml.attribute);
		assertFalse(schema.xml.wrapped);

		assertNotNull(schema.discriminator);
		assertEquals("type", schema.discriminator.propertyName);
		assertEquals("FooType", schema.discriminator.mapping.get("foo"));
		// Check extensions key
		if (schema.discriminator.extensions != null && !schema.discriminator.extensions.isEmpty()) {
			assertEquals("defaultMapping", schema.discriminator.extensions.values().iterator().next());
		}

		assertEquals(common.PayloadValue.ofString("{ex}"), schema.example);
		assertNotNull(schema.externalDocs);
		assertEquals("http://doc", schema.externalDocs.url);
		assertEquals("doc_desc", schema.externalDocs.description);

		assertNotNull(schema.allOf);
		assertEquals("#/components/schemas/BaseClass", schema.allOf.get(0).value.$ref);

		assertNotNull(schema.properties.get("prop_a"));
		Schema propSchema = schema.properties.get("prop_a").value;
		assertEquals(openapi.SchemaType.ofSingle("string"), propSchema.type);
		assertEquals("prop doc", propSchema.description);
		assertEquals("xpname", propSchema.xml.name);
		assertEquals("xpns", propSchema.xml.namespace);
		assertEquals("xppr", propSchema.xml.prefix);
		assertFalse(propSchema.xml.attribute);
		assertTrue(propSchema.xml.wrapped);
		assertEquals(common.PayloadValue.ofString("exprop"), propSchema.example);
		assertEquals("http://pdoc", propSchema.externalDocs.url);
		assertEquals("pdesc", propSchema.externalDocs.description);
	}

	/**
	 * Verifies primitive, boxed, collection, map, and temporal type mapping in
	 * {@link classes.Parse}.
	 */
	@Test
	public void testParseTypes() {
		String source = "public class Types {\n" + "    public Integer vInt;\n" + "    public Long vLong;\n"
				+ "    public Double vDouble;\n" + "    public Float vFloat;\n" + "    public Boolean vBool;\n"
				+ "    public java.util.UUID vUuid;\n" + "    public java.time.LocalDate vDate;\n"
				+ "    public java.time.OffsetDateTime vOffset;\n" + "    public java.time.ZonedDateTime vZoned;\n"
				+ "    public java.util.List<String> vList;\n" + "    public java.util.ArrayList vRawList;\n"
				+ "    public java.util.Set<Integer> vSet;\n" + "    public java.util.Map<String, String> vMap;\n"
				+ "    public java.util.HashMap vRawMap;\n" + "    public CustomType vCustom;\n"
				+ "    public byte[] vByteArr;\n" + "    public int[] vIntArr;\n" + "    public int pInt;\n"
				+ "    public long pLong;\n" + "    public double pDouble;\n" + "    public float pFloat;\n"
				+ "    public boolean pBool;\n" + "    public short pShort;\n" + // to hit "other" primitive branch or
																					// error
				"}\n";
		OpenAPI api = Parse.parse(source);
		Schema schema = api.components.schemas.get("Types").value;

		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("vInt").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("vLong").value.type);
		assertEquals("int64", schema.properties.get("vLong").value.format);
		assertEquals(openapi.SchemaType.ofSingle("number"), schema.properties.get("vDouble").value.type);
		assertEquals(openapi.SchemaType.ofSingle("number"), schema.properties.get("vFloat").value.type);
		assertEquals("float", schema.properties.get("vFloat").value.format);
		assertEquals(openapi.SchemaType.ofSingle("boolean"), schema.properties.get("vBool").value.type);

		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vUuid").value.type);
		assertEquals("uuid", schema.properties.get("vUuid").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vDate").value.type);
		assertEquals("date", schema.properties.get("vDate").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vOffset").value.type);
		assertEquals("date-time", schema.properties.get("vOffset").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vZoned").value.type);
		assertEquals("date-time", schema.properties.get("vZoned").value.format);

		assertEquals(openapi.SchemaType.ofSingle("array"), schema.properties.get("vList").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vList").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("array"), schema.properties.get("vRawList").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vRawList").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("array"), schema.properties.get("vSet").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("vSet").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("object"), schema.properties.get("vMap").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"),
				schema.properties.get("vMap").value.additionalProperties.schema.value.type);

		assertEquals(openapi.SchemaType.ofSingle("object"), schema.properties.get("vRawMap").value.type);

		assertEquals("#/components/schemas/CustomType", schema.properties.get("vCustom").value.$ref);

		assertEquals(openapi.SchemaType.ofSingle("string"), schema.properties.get("vByteArr").value.type);
		assertEquals("binary", schema.properties.get("vByteArr").value.format);

		assertEquals(openapi.SchemaType.ofSingle("array"), schema.properties.get("vIntArr").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("vIntArr").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("pInt").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), schema.properties.get("pLong").value.type);
		assertEquals("int64", schema.properties.get("pLong").value.format);
		assertEquals(openapi.SchemaType.ofSingle("number"), schema.properties.get("pDouble").value.type);
		assertEquals(openapi.SchemaType.ofSingle("number"), schema.properties.get("pFloat").value.type);
		assertEquals(openapi.SchemaType.ofSingle("boolean"), schema.properties.get("pBool").value.type);
	}

	/**
	 * Verifies graceful handling of malformed Java source input.
	 */
	@Test
	public void testParseException() {
		// Syntax error causes ParseException, which is caught and returns empty api.
		OpenAPI api = Parse.parse("invalid java code {");
		assertNotNull(api);
		assertNotNull(api.components.schemas);
	}

	/**
	 * Verifies full SDK and DTO emission from a comprehensive OpenAPI model.
	 */
	@Test
	public void testEmitNew() {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.title = "Test API!";
		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();

		PathItem pi = new PathItem();

		Operation getOp = new Operation();
		getOp.operationId = "getThing";
		getOp.summary = "Get Thing Summary";
		Parameter p1 = new Parameter();
		p1.name = "id";
		p1.in = "path";
		p1.description = "ID desc";
		p1.required = true;
		Parameter p2 = new Parameter();
		p2.name = "query_p";
		p2.in = "query";
		Parameter p2b = new Parameter();
		p2b.name = "query_p2";
		p2b.in = "query";
		getOp.parameters = Arrays.asList(openapi.ReferenceOr.ofValue(p1), openapi.ReferenceOr.ofValue(p2),
				openapi.ReferenceOr.ofValue(p2b));

		pi.get = getOp;

		Operation postOp = new Operation();
		postOp.description = "Post Thing Desc";
		postOp.requestBody = openapi.ReferenceOr.ofValue(new RequestBody());
		pi.post = postOp;

		Operation putOp = new Operation();
		Parameter p3 = new Parameter();
		p3.name = "qp2";
		p3.in = "query";
		Parameter p4 = new Parameter();
		p4.name = null;
		p4.in = "query";
		putOp.parameters = Arrays.asList(openapi.ReferenceOr.ofValue(p3), openapi.ReferenceOr.ofValue(p4));

		pi.put = putOp;

		pi.delete = new Operation();

		api.paths.pathItems.put("/thing/{id}", pi);

		api.components = new Components();
		api.components.schemas = new HashMap<>();

		// Enum schema
		Schema enumSchema = new Schema();
		enumSchema.enumValues = Arrays.asList(common.PayloadValue.ofString("A"), common.PayloadValue.ofString("b-c"),
				common.PayloadValue.ofString("1D"), common.PayloadValue.ofString(""));
		api.components.schemas.put("MyEnum", openapi.ReferenceOr.ofValue(enumSchema));

		// Object schema
		Schema objSchema = new Schema();
		objSchema.description = "obj desc";
		objSchema.example = common.PayloadValue.ofString("ex");
		objSchema.xml = new XML();
		objSchema.xml.name = "xn";
		objSchema.xml.namespace = "xns";
		objSchema.xml.prefix = "xpr";
		objSchema.xml.attribute = true;
		objSchema.xml.wrapped = false;
		objSchema.externalDocs = new ExternalDocumentation();
		objSchema.externalDocs.url = "http://doc";
		objSchema.externalDocs.description = "doc";
		objSchema.discriminator = new Discriminator();
		objSchema.discriminator.propertyName = "type";
		objSchema.discriminator.mapping = new HashMap<>();
		objSchema.discriminator.mapping.put("a", "A");
		objSchema.discriminator.extensions.put("defaultMapping", common.PayloadValue.ofString("A"));

		objSchema.properties = new HashMap<>();

		Schema strProp = new Schema();
		strProp.type = openapi.SchemaType.ofSingle("string");
		strProp.description = "str prop";
		strProp.example = common.PayloadValue.ofString("ex prop");
		strProp.xml = new XML();
		strProp.xml.name = "pxn";
		strProp.xml.namespace = "pxns";
		strProp.xml.prefix = "pxpr";
		strProp.xml.attribute = true;
		strProp.xml.wrapped = false;
		strProp.externalDocs = new ExternalDocumentation();
		strProp.externalDocs.url = "http://pdoc";
		strProp.externalDocs.description = "pdoc";
		objSchema.properties.put("str_prop", openapi.ReferenceOr.ofValue(strProp));

		Schema refProp = new Schema();
		refProp.$ref = "#/components/schemas/MyEnum";
		objSchema.properties.put("refProp", openapi.ReferenceOr.ofValue(refProp));

		Schema dtProp = new Schema();
		dtProp.type = openapi.SchemaType.ofSingle("string");
		dtProp.format = "date-time";
		objSchema.properties.put("dt", openapi.ReferenceOr.ofValue(dtProp));

		Schema dProp = new Schema();
		dProp.type = openapi.SchemaType.ofSingle("string");
		dProp.format = "date";
		objSchema.properties.put("d", openapi.ReferenceOr.ofValue(dProp));

		Schema uProp = new Schema();
		uProp.type = openapi.SchemaType.ofSingle("string");
		uProp.format = "uuid";
		objSchema.properties.put("u", openapi.ReferenceOr.ofValue(uProp));

		Schema bProp = new Schema();
		bProp.type = openapi.SchemaType.ofSingle("string");
		bProp.format = "binary";
		objSchema.properties.put("b", openapi.ReferenceOr.ofValue(bProp));

		Schema int64Prop = new Schema();
		int64Prop.type = openapi.SchemaType.ofSingle("integer");
		int64Prop.format = "int64";
		objSchema.properties.put("i64", openapi.ReferenceOr.ofValue(int64Prop));

		Schema int32Prop = new Schema();
		int32Prop.type = openapi.SchemaType.ofSingle("integer");
		objSchema.properties.put("i32", openapi.ReferenceOr.ofValue(int32Prop));

		Schema fProp = new Schema();
		fProp.type = openapi.SchemaType.ofSingle("number");
		fProp.format = "float";
		objSchema.properties.put("f", openapi.ReferenceOr.ofValue(fProp));

		Schema dbProp = new Schema();
		dbProp.type = openapi.SchemaType.ofSingle("number");
		objSchema.properties.put("db", openapi.ReferenceOr.ofValue(dbProp));

		Schema boolProp = new Schema();
		boolProp.type = openapi.SchemaType.ofSingle("boolean");
		objSchema.properties.put("bool", openapi.ReferenceOr.ofValue(boolProp));

		Schema arrProp = new Schema();
		arrProp.type = openapi.SchemaType.ofSingle("array");
		Schema arrItems = new Schema();
		arrItems.type = openapi.SchemaType.ofSingle("string");
		arrProp.items = openapi.ReferenceOr.ofValue(arrItems);
		objSchema.properties.put("arr", openapi.ReferenceOr.ofValue(arrProp));

		Schema mapProp = new Schema();
		mapProp.type = openapi.SchemaType.ofSingle("object");
		Schema mapAddProps = new Schema();
		mapAddProps.type = openapi.SchemaType.ofSingle("integer");
		mapProp.additionalProperties = openapi.AdditionalProperties.ofSchema(mapAddProps);
		objSchema.properties.put("map", openapi.ReferenceOr.ofValue(mapProp));

		Schema mapObjProp = new Schema();
		mapObjProp.type = openapi.SchemaType.ofSingle("object");
		objSchema.properties.put("mapObj", openapi.ReferenceOr.ofValue(mapObjProp));

		Schema otherProp = new Schema();
		otherProp.type = openapi.SchemaType.ofSingle("unknown");
		objSchema.properties.put("other", openapi.ReferenceOr.ofValue(otherProp));

		Schema keyProp = new Schema();
		keyProp.type = openapi.SchemaType.ofSingle("string");
		objSchema.properties.put("enum", openapi.ReferenceOr.ofValue(keyProp));
		objSchema.properties.put("notSchema", null); // tests "enumValue" renaming
		objSchema.properties.put("1digit", openapi.ReferenceOr.ofValue(keyProp)); // tests "_1digit" renaming

		api.components.schemas.put("MyObj", openapi.ReferenceOr.ofValue(objSchema));

		String result = Emit.emit(api, null);
		System.out.println("EMIT_RESULT:\n" + result);
		assertNotNull(result);
		assertTrue(result.contains("class TestAPIClient"));
		assertTrue(result.contains("getThing(String id, String query_p, String query_p2)"));
		assertTrue(result.contains("class McpAdapter"));
		assertTrue(result.contains("public java.util.List<Tool> getTools()"));
		assertTrue(result.contains("public java.util.List<Resource> getResources()"));
		assertTrue(result.contains(
				"public CallToolResult callTool(String name, java.util.Map<String, Object> arguments) throws Exception"));
		assertTrue(result.contains("if (\"getThing\".equals(name))"));
		assertTrue(result.contains("if (\"POST__thing__id_\".equals(name))"));
		assertTrue(result.contains("enum MyEnum"));
		assertTrue(result.contains("B_C"));
		assertTrue(result.contains("_1D"));
		assertTrue(result.contains("class MyObj"));
		assertTrue(result.contains("enumValue"));
		assertTrue(result.contains("_1digit"));

		// Also test Emit constructor
		assertNotNull(new Emit());
		assertNotNull(new Parse());
	}

	/**
	 * Verifies lexical-preserving emission into an existing Java source string.
	 */
	@Test
	public void testEmitExisting() {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		Schema emptyEnum = new Schema();
		emptyEnum.enumValues = Arrays.asList(common.PayloadValue.ofString("V"));
		api.components.schemas.put("MyEnum", openapi.ReferenceOr.ofValue(emptyEnum));

		String existing = "public enum MyEnum { OLD_V }\n";
		String result = Emit.emit(api, existing);
		assertTrue(result.contains("V"));
		assertFalse(result.contains("OLD_V"));
	}

	/**
	 * Verifies default client class naming when Info or title is absent.
	 */
	@Test
	public void testEmitApiDefaultTitle() {
		OpenAPI api = new OpenAPI();
		String result = Emit.emit(api, null);
		assertTrue(result.contains("class ApiClient"));

		api.info = new Info();
		api.info.title = "";
		result = Emit.emit(api, null);
		assertTrue(result.contains("class ApiClient"));
	}

	/**
	 * Verifies reserved class names (Parse, Emit) are skipped during DTO emission.
	 */
	@Test
	public void testEmitEmptySchema() {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.schemas.put("Parse", openapi.ReferenceOr.ofValue(new Schema()));
		api.components.schemas.put("Emit", openapi.ReferenceOr.ofValue(new Schema()));

		Schema sparseObj = new Schema();
		sparseObj.description = "";
		sparseObj.xml = new XML();
		sparseObj.externalDocs = new ExternalDocumentation();
		ExternalDocumentation urlOnlyEd = new ExternalDocumentation();
		urlOnlyEd.url = "https://example.com";
		sparseObj.discriminator = new Discriminator();
		sparseObj.discriminator.extensions = null;
		sparseObj.properties = new HashMap<>();

		Schema propSparse = new Schema();
		propSparse.description = "";
		propSparse.xml = new XML();
		propSparse.externalDocs = new ExternalDocumentation();
		Schema propUrlOnlyEd = new Schema();
		propUrlOnlyEd.externalDocs = urlOnlyEd;
		sparseObj.properties.put("default", openapi.ReferenceOr.ofValue(propSparse));
		sparseObj.properties.put("const", openapi.ReferenceOr.ofValue(propUrlOnlyEd));
		sparseObj.properties.put("class", openapi.ReferenceOr.ofValue(new Schema()));
		api.components.schemas.put("SparseObj", openapi.ReferenceOr.ofValue(sparseObj));

		Schema discNullExt = new Schema();
		discNullExt.discriminator = new Discriminator();
		discNullExt.externalDocs = urlOnlyEd;
		api.components.schemas.put("DiscNullExt", openapi.ReferenceOr.ofValue(discNullExt));

		String result = Emit.emit(api, "   ");
		assertFalse(result.contains("class Parse"));
		assertFalse(result.contains("class Emit"));
		// Re-emit into existing result so existing fields hit
		// !getFieldByName(...).isPresent() false branch
		String reEmitted = Emit.emit(api, result);
		assertTrue(reEmitted.contains("defaultValue"));
		assertTrue(reEmitted.contains("constValue"));
		assertTrue(reEmitted.contains("classValue"));

		Map<String, String> modular = Emit.emitModular(api);
		assertFalse(modular.containsKey("models/Parse.java"));
		assertFalse(modular.containsKey("models/Emit.java"));
		assertTrue(modular.containsKey("models/SparseObj.java"));
	}

	/**
	 * Verifies that all emitted Model Context Protocol (MCP) 1.0.0 schema DTOs
	 * inside {@code McpAdapter} round-trip cleanly through {@link classes.Parse}
	 * into {@code api.components.schemas}.
	 */
	@Test
	public void testMcpSchemasRoundTrip() {
		OpenAPI api = new OpenAPI();
		String sdkSource = Emit.emit(api, null);
		OpenAPI extracted = Parse.parse(sdkSource);
		assertNotNull(extracted.components);
		assertNotNull(extracted.components.schemas);
		String[] mcpSchemas = {"Annotated", "BlobResourceContents", "CallToolRequest", "CallToolResult",
				"CancelledNotification", "ClientCapabilities", "ClientNotification", "ClientRequest", "ClientResult",
				"CompleteRequest", "CompleteResult", "CreateMessageRequest", "CreateMessageResult", "Cursor",
				"EmbeddedResource", "EmptyResult", "GetPromptRequest", "GetPromptResult", "ImageContent",
				"Implementation", "InitializeRequest", "InitializeResult", "InitializedNotification", "JSONRPCError",
				"JSONRPCMessage", "JSONRPCNotification", "JSONRPCRequest", "JSONRPCResponse", "ListPromptsRequest",
				"ListPromptsResult", "ListResourceTemplatesRequest", "ListResourceTemplatesResult",
				"ListResourcesRequest", "ListResourcesResult", "ListRootsRequest", "ListRootsResult",
				"ListToolsRequest", "ListToolsResult", "LoggingLevel", "LoggingMessageNotification", "ModelHint",
				"ModelPreferences", "Notification", "PaginatedRequest", "PaginatedResult", "PingRequest", "Prompt",
				"PromptArgument", "PromptListChangedNotification", "PromptMessage", "PromptReference",
				"ProgressNotification", "ProgressToken", "ReadResourceRequest", "ReadResourceResult", "Request",
				"RequestId", "Resource", "ResourceContents", "ResourceListChangedNotification", "ResourceReference",
				"ResourceTemplate", "ResourceUpdatedNotification", "Result", "Role", "Root",
				"RootsListChangedNotification", "SamplingMessage", "ServerCapabilities", "ServerNotification",
				"ServerRequest", "ServerResult", "SetLevelRequest", "SubscribeRequest", "TextContent",
				"TextResourceContents", "Tool", "ToolListChangedNotification", "UnsubscribeRequest"};
		for (String name : mcpSchemas) {
			assertTrue("Missing MCP schema: " + name, extracted.components.schemas.containsKey(name));
		}
	}
}
