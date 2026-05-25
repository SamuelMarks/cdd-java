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

public class ClassesParseEmitTest {

	@Test
	public void testParseEnum() {
		String source = "/** My Enum */\n" + "public enum MyEnum {\n" + "    @JsonProperty(\"val_a\")\n"
				+ "    VAL_A,\n" + "    VAL_B\n" + "}\n";
		OpenAPI api = Parse.parse(source);
		assertNotNull(api.components.schemas.get("MyEnum"));
		Schema schema = api.components.schemas.get("MyEnum");
		assertEquals("string", schema.type);
		assertEquals("My Enum", schema.description);
		assertEquals(Arrays.asList("val_a", "VAL_B"), schema.enumValues);
	}

	@Test
	public void testParseClass() {
		String source = "/**\n" + " * Class doc\n" + " * @xmlName xname\n" + " * @xmlNamespace xns\n"
				+ " * @xmlPrefix xpr\n" + " * @xmlAttribute true\n" + " * @xmlWrapped false\n"
				+ " * @discriminatorProperty type\n" + " * @discriminatorMapping foo FooType\n"
				+ " * @discriminatorDefault defaultMapping\n" + " * @schemaExample {ex}\n"
				+ " * @schemaExternalDocs http://doc doc_desc\n" + " */\n"
				+ "@JsonTypeInfo(use=Id.NAME, property=\"type\")\n" + "public class MyClass extends BaseClass {\n"
				+ "    /**\n" + "     * prop doc\n" + "     * @xmlName xpname\n" + "     * @xmlNamespace xpns\n"
				+ "     * @xmlPrefix xppr\n" + "     * @xmlAttribute false\n" + "     * @xmlWrapped true\n"
				+ "     * @schemaExample exprop\n" + "     * @schemaExternalDocs http://pdoc pdesc\n" + "     */\n"
				+ "    @JsonProperty(\"prop_a\")\n" + "    public String propA;\n" + "    private int propB;\n" + // should
																													// be
																													// ignored
				"}\n" + "/**\n" + " * @schemaExternalDocs http://nospace\n" + " */\n"
				+ "@JsonTypeInfo(use=Id.NAME, property=\"type\")\n" + "public class NoSpaceClass {}\n"
				+ "public interface MyInterface {}\n" + "public class MyClient {}\n" + "class NospaceServer { \n"
				+ "    /**\n" + "     * @schemaExternalDocs http://nospace\n" + "     */\n"
				+ "    public String myProp;\n" + "}\n" + "public class MyMockServer {}\n"
				+ "public class MyIntegrationTest {}\n";

		OpenAPI api = Parse.parse(source);
		Schema schema = api.components.schemas.get("MyClass");
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

		assertEquals("{ex}", schema.example);
		assertNotNull(schema.externalDocs);
		assertEquals("http://doc", schema.externalDocs.url);
		assertEquals("doc_desc", schema.externalDocs.description);

		assertNotNull(schema.allOf);
		assertEquals("#/components/schemas/BaseClass", ((Schema) schema.allOf.get(0)).$ref);

		assertNotNull(schema.properties.get("prop_a"));
		Schema propSchema = (Schema) schema.properties.get("prop_a");
		assertEquals("string", propSchema.type);
		assertEquals("prop doc", propSchema.description);
		assertEquals("xpname", propSchema.xml.name);
		assertEquals("xpns", propSchema.xml.namespace);
		assertEquals("xppr", propSchema.xml.prefix);
		assertFalse(propSchema.xml.attribute);
		assertTrue(propSchema.xml.wrapped);
		assertEquals("exprop", propSchema.example);
		assertEquals("http://pdoc", propSchema.externalDocs.url);
		assertEquals("pdesc", propSchema.externalDocs.description);
	}

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
		Schema schema = api.components.schemas.get("Types");

		assertEquals("integer", ((Schema) schema.properties.get("vInt")).type);
		assertEquals("integer", ((Schema) schema.properties.get("vLong")).type);
		assertEquals("int64", ((Schema) schema.properties.get("vLong")).format);
		assertEquals("number", ((Schema) schema.properties.get("vDouble")).type);
		assertEquals("number", ((Schema) schema.properties.get("vFloat")).type);
		assertEquals("float", ((Schema) schema.properties.get("vFloat")).format);
		assertEquals("boolean", ((Schema) schema.properties.get("vBool")).type);

		assertEquals("string", ((Schema) schema.properties.get("vUuid")).type);
		assertEquals("uuid", ((Schema) schema.properties.get("vUuid")).format);
		assertEquals("string", ((Schema) schema.properties.get("vDate")).type);
		assertEquals("date", ((Schema) schema.properties.get("vDate")).format);
		assertEquals("string", ((Schema) schema.properties.get("vOffset")).type);
		assertEquals("date-time", ((Schema) schema.properties.get("vOffset")).format);
		assertEquals("string", ((Schema) schema.properties.get("vZoned")).type);
		assertEquals("date-time", ((Schema) schema.properties.get("vZoned")).format);

		assertEquals("array", ((Schema) schema.properties.get("vList")).type);
		assertEquals("string", ((Schema) ((Schema) schema.properties.get("vList")).items).type);

		assertEquals("array", ((Schema) schema.properties.get("vRawList")).type);
		assertEquals("string", ((Schema) ((Schema) schema.properties.get("vRawList")).items).type);

		assertEquals("array", ((Schema) schema.properties.get("vSet")).type);
		assertEquals("integer", ((Schema) ((Schema) schema.properties.get("vSet")).items).type);

		assertEquals("object", ((Schema) schema.properties.get("vMap")).type);
		assertEquals("string", ((Schema) ((Schema) schema.properties.get("vMap")).additionalProperties).type);

		assertEquals("object", ((Schema) schema.properties.get("vRawMap")).type);

		assertEquals("#/components/schemas/CustomType", ((Schema) schema.properties.get("vCustom")).$ref);

		assertEquals("string", ((Schema) schema.properties.get("vByteArr")).type);
		assertEquals("binary", ((Schema) schema.properties.get("vByteArr")).format);

		assertEquals("array", ((Schema) schema.properties.get("vIntArr")).type);
		assertEquals("integer", ((Schema) ((Schema) schema.properties.get("vIntArr")).items).type);

		assertEquals("integer", ((Schema) schema.properties.get("pInt")).type);
		assertEquals("integer", ((Schema) schema.properties.get("pLong")).type);
		assertEquals("int64", ((Schema) schema.properties.get("pLong")).format);
		assertEquals("number", ((Schema) schema.properties.get("pDouble")).type);
		assertEquals("number", ((Schema) schema.properties.get("pFloat")).type);
		assertEquals("boolean", ((Schema) schema.properties.get("pBool")).type);
	}

	@Test
	public void testParseException() {
		// Syntax error causes ParseException, which is caught and returns empty api.
		OpenAPI api = Parse.parse("invalid java code {");
		assertNotNull(api);
		assertNotNull(api.components.schemas);
	}

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
		Parameter p1 = new Parameter();
		p1.name = "id";
		p1.in = "path";
		Parameter p2 = new Parameter();
		p2.name = "query_p";
		p2.in = "query";
		Parameter p2b = new Parameter();
		p2b.name = "query_p2";
		p2b.in = "query";
		getOp.parameters = Arrays.asList(p1, p2, p2b);

		pi.get = getOp;

		Operation postOp = new Operation();
		postOp.requestBody = new RequestBody();
		pi.post = postOp;

		Operation putOp = new Operation();
		Parameter p3 = new Parameter();
		p3.name = "qp2";
		p3.in = "query";
		Parameter p4 = new Parameter();
		p4.name = null;
		p4.in = "query";
		putOp.parameters = Arrays.asList(p3, p4);

		pi.put = putOp;

		pi.delete = new Operation();

		api.paths.pathItems.put("/thing/{id}", pi);

		api.components = new Components();
		api.components.schemas = new HashMap<>();

		// Enum schema
		Schema enumSchema = new Schema();
		enumSchema.enumValues = Arrays.asList("A", "b-c", "1D", "");
		api.components.schemas.put("MyEnum", enumSchema);

		// Object schema
		Schema objSchema = new Schema();
		objSchema.description = "obj desc";
		objSchema.example = "ex";
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
		objSchema.discriminator.extensions.put("defaultMapping", "A");

		objSchema.properties = new HashMap<>();

		Schema strProp = new Schema();
		strProp.type = "string";
		strProp.description = "str prop";
		strProp.example = "ex prop";
		strProp.xml = new XML();
		strProp.xml.name = "pxn";
		strProp.xml.namespace = "pxns";
		strProp.xml.prefix = "pxpr";
		strProp.xml.attribute = true;
		strProp.xml.wrapped = false;
		strProp.externalDocs = new ExternalDocumentation();
		strProp.externalDocs.url = "http://pdoc";
		strProp.externalDocs.description = "pdoc";
		objSchema.properties.put("str_prop", strProp);

		Schema refProp = new Schema();
		refProp.$ref = "#/components/schemas/MyEnum";
		objSchema.properties.put("refProp", refProp);

		Schema dtProp = new Schema();
		dtProp.type = "string";
		dtProp.format = "date-time";
		objSchema.properties.put("dt", dtProp);

		Schema dProp = new Schema();
		dProp.type = "string";
		dProp.format = "date";
		objSchema.properties.put("d", dProp);

		Schema uProp = new Schema();
		uProp.type = "string";
		uProp.format = "uuid";
		objSchema.properties.put("u", uProp);

		Schema bProp = new Schema();
		bProp.type = "string";
		bProp.format = "binary";
		objSchema.properties.put("b", bProp);

		Schema int64Prop = new Schema();
		int64Prop.type = "integer";
		int64Prop.format = "int64";
		objSchema.properties.put("i64", int64Prop);

		Schema int32Prop = new Schema();
		int32Prop.type = "integer";
		objSchema.properties.put("i32", int32Prop);

		Schema fProp = new Schema();
		fProp.type = "number";
		fProp.format = "float";
		objSchema.properties.put("f", fProp);

		Schema dbProp = new Schema();
		dbProp.type = "number";
		objSchema.properties.put("db", dbProp);

		Schema boolProp = new Schema();
		boolProp.type = "boolean";
		objSchema.properties.put("bool", boolProp);

		Schema arrProp = new Schema();
		arrProp.type = "array";
		Schema arrItems = new Schema();
		arrItems.type = "string";
		arrProp.items = arrItems;
		objSchema.properties.put("arr", arrProp);

		Schema mapProp = new Schema();
		mapProp.type = "object";
		Schema mapAddProps = new Schema();
		mapAddProps.type = "integer";
		mapProp.additionalProperties = mapAddProps;
		objSchema.properties.put("map", mapProp);

		Schema mapObjProp = new Schema();
		mapObjProp.type = "object";
		objSchema.properties.put("mapObj", mapObjProp);

		Schema otherProp = new Schema();
		otherProp.type = "unknown";
		objSchema.properties.put("other", otherProp);

		Schema keyProp = new Schema();
		keyProp.type = "string";
		objSchema.properties.put("enum", keyProp);
		objSchema.properties.put("notSchema", new Object()); // tests "enumValue" renaming
		objSchema.properties.put("1digit", keyProp); // tests "_1digit" renaming

		api.components.schemas.put("MyObj", objSchema);

		String result = Emit.emit(api, null);
		System.out.println("EMIT_RESULT:\n" + result);
		assertNotNull(result);
		assertTrue(result.contains("class TestAPIClient"));
		assertTrue(result.contains("getThing(String id, String query_p, String query_p2)"));
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

	@Test
	public void testEmitExisting() {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		Schema emptyEnum = new Schema();
		emptyEnum.enumValues = Arrays.asList("V");
		api.components.schemas.put("MyEnum", emptyEnum);

		String existing = "public enum MyEnum { OLD_V }\n";
		String result = Emit.emit(api, existing);
		assertTrue(result.contains("V"));
		assertFalse(result.contains("OLD_V"));
	}

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

	@Test
	public void testEmitEmptySchema() {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.schemas.put("Parse", new Schema());
		api.components.schemas.put("Emit", new Schema());
		String result = Emit.emit(api, null);
		assertFalse(result.contains("class Parse"));
		assertFalse(result.contains("class Emit"));
	}
}
