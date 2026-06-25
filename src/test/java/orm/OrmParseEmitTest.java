package orm;

import org.junit.Test;
import static org.junit.Assert.*;

import openapi.OpenAPI;
import openapi.Schema;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class OrmParseEmitTest {

	@Test
	public void testParseConstructor() {
		new Parse();
	}

	@Test
	public void testEmitConstructor() {
		new Emit();
	}

	@Test
	public void testParseValid() {
		String javaSource = "package com.example;\n" + "import jakarta.persistence.*;\n" + "import java.util.*;\n"
				+ "import java.time.*;\n" + "\n" + "public interface MyInterface {}\n" + "\n"
				+ "public class NotAnEntity {}\n" + "\n" + "@Entity\n"
				+ "@Table(name = \"users_table\", other = \"test\")\n" + "public class User {\n" + "    @Id\n"
				+ "    @Column(name = \"user_id\")\n" + "    public String id;\n" + "\n"
				+ "    @Column(other = \"val\")\n" + "    public Integer age;\n" + "    public Long longAge;\n"
				+ "    public Double doubleVal;\n" + "    public Float floatVal;\n" + "    public Boolean active;\n"
				+ "    public UUID uuid;\n" + "    public LocalDate ld;\n" + "    public OffsetDateTime odt;\n"
				+ "    public ZonedDateTime zdt;\n" + "    public List<String> stringList;\n"
				+ "    public ArrayList<Integer> intList;\n" + "    public Set noGenSet;\n"
				+ "    public Map<String, Integer> mapVal;\n" + "    public HashMap singleGenMap;\n"
				+ "    public Map myMap;\n" + "    public List myList;\n" + "    public Map<String> badMap;\n"
				+ "    public List<> emptyDiamondList;\n" + "    public Map<> emptyDiamondMap;\n"
				+ "    public SomeCustomType custom;\n" + "    public byte[] bytes;\n"
				+ "    public String[] stringArray;\n" + "    public int pInt;\n" + "    public long pLong;\n"
				+ "    public double pDouble;\n" + "    public float pFloat;\n" + "    public boolean pBoolean;\n"
				+ "    public short pShort;\n" + "    public Map<String, ?> wildcardMap;\n" + "}\n" + "@Entity\n"
				+ "@Table\n" + "public class MissingNameTable {\n" + "    @Column\n"
				+ "    public String missingNameCol;\n" + "}\n";

		OpenAPI api = Parse.parse(javaSource);
		assertNotNull(api);
		assertTrue(api.components.schemas.containsKey("User"));
		assertFalse(api.components.schemas.containsKey("MyInterface"));
		assertFalse(api.components.schemas.containsKey("NotAnEntity"));

		Schema user = api.components.schemas.get("User");
		assertEquals("users_table", user.extensions.get("x-table-name"));

		Map<String, Object> props = user.properties;
		assertNotNull(props);

		Schema idSchema = (Schema) props.get("user_id");
		assertNotNull(idSchema);
		assertEquals("string", idSchema.type);
		assertEquals(true, idSchema.extensions.get("x-primary-key"));

		assertEquals("integer", ((Schema) props.get("age")).type);
		assertEquals("integer", ((Schema) props.get("longAge")).type);
		assertEquals("int64", ((Schema) props.get("longAge")).format);
		assertEquals("number", ((Schema) props.get("doubleVal")).type);
		assertEquals("number", ((Schema) props.get("floatVal")).type);
		assertEquals("float", ((Schema) props.get("floatVal")).format);
		assertEquals("boolean", ((Schema) props.get("active")).type);
		assertEquals("string", ((Schema) props.get("uuid")).type);
		assertEquals("uuid", ((Schema) props.get("uuid")).format);
		assertEquals("string", ((Schema) props.get("ld")).type);
		assertEquals("date", ((Schema) props.get("ld")).format);
		assertEquals("string", ((Schema) props.get("odt")).type);
		assertEquals("date-time", ((Schema) props.get("odt")).format);
		assertEquals("string", ((Schema) props.get("zdt")).type);
		assertEquals("date-time", ((Schema) props.get("zdt")).format);

		assertEquals("array", ((Schema) props.get("stringList")).type);
		assertEquals("string", ((Schema) ((Schema) props.get("stringList")).items).type);
		assertEquals("array", ((Schema) props.get("intList")).type);
		assertEquals("integer", ((Schema) ((Schema) props.get("intList")).items).type);
		assertEquals("array", ((Schema) props.get("noGenSet")).type);
		assertEquals("string", ((Schema) ((Schema) props.get("noGenSet")).items).type);

		assertEquals("object", ((Schema) props.get("mapVal")).type);
		assertEquals("integer", ((Schema) ((Schema) props.get("mapVal")).additionalProperties).type);
		assertEquals("object", ((Schema) props.get("singleGenMap")).type);
		assertNull(((Schema) props.get("singleGenMap")).additionalProperties);

		assertEquals("#/components/schemas/SomeCustomType", ((Schema) props.get("custom")).$ref);

		assertEquals("string", ((Schema) props.get("bytes")).type);
		assertEquals("binary", ((Schema) props.get("bytes")).format);

		assertEquals("array", ((Schema) props.get("stringArray")).type);
		assertEquals("string", ((Schema) ((Schema) props.get("stringArray")).items).type);

		assertEquals("integer", ((Schema) props.get("pInt")).type);
		assertEquals("integer", ((Schema) props.get("pLong")).type);
		assertEquals("int64", ((Schema) props.get("pLong")).format);
		assertEquals("number", ((Schema) props.get("pDouble")).type);
		assertEquals("number", ((Schema) props.get("pFloat")).type);
		assertEquals("boolean", ((Schema) props.get("pBoolean")).type);
		assertNull(((Schema) props.get("pShort")).type);

		assertEquals("object", ((Schema) props.get("wildcardMap")).type);
		assertEquals("object", ((Schema) ((Schema) props.get("wildcardMap")).additionalProperties).type);

		Schema missingNameTable = api.components.schemas.get("MissingNameTable");
		assertNotNull(missingNameTable);
		assertNull(missingNameTable.extensions.get("x-table-name"));
		assertNotNull(missingNameTable.properties.get("missingNameCol"));
	}

	@Test
	public void testParseError() {
		OpenAPI api = Parse.parse("this is not valid java {{");
		assertNotNull(api);
		assertEquals("3.2.0", api.openapi);
	}

	@Test
	public void testEmitNullChecks() {
		OpenAPI api = new OpenAPI();
		assertEquals("", Emit.emit(api, null));
		assertEquals("test", Emit.emit(api, "test"));
		api.components = new openapi.Components();
		assertEquals("", Emit.emit(api, null));
		assertEquals("test2", Emit.emit(api, "test2")); // Hits the components != null && schemas == null case
	}

	@Test
	public void testEmitNew() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		user.properties = new LinkedHashMap<>();
		user.properties.put("id", new Schema() {
			{
				type = "string";
				format = "uuid";
			}
		});
		user.properties.put("active", new Schema() {
			{
				type = "boolean";
			}
		});
		user.properties.put("1invalid", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("enum", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("const", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("default", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("class", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("age", new Schema() {
			{
				type = "integer";
			}
		});
		user.properties.put("longAge", new Schema() {
			{
				type = "integer";
				format = "int64";
			}
		});
		user.properties.put("dVal", new Schema() {
			{
				type = "number";
			}
		});
		user.properties.put("fVal", new Schema() {
			{
				type = "number";
				format = "float";
			}
		});
		user.properties.put("bytes", new Schema() {
			{
				type = "string";
				format = "binary";
			}
		});
		user.properties.put("ld", new Schema() {
			{
				type = "string";
				format = "date";
			}
		});
		user.properties.put("odt", new Schema() {
			{
				type = "string";
				format = "date-time";
			}
		});
		user.properties.put("str", new Schema() {
			{
				type = "string";
			}
		});

		Schema child = new Schema();
		child.type = "object";
		user.properties.put("childMap", child); // map<string, object>

		Schema childMapSchema = new Schema();
		childMapSchema.type = "object";
		childMapSchema.additionalProperties = new Schema() {
			{
				type = "string";
			}
		};
		user.properties.put("childSpecMap", childMapSchema); // map<string, string>

		Schema arraySchema = new Schema();
		arraySchema.type = "array";
		arraySchema.items = new Schema() {
			{
				type = "string";
			}
		};
		user.properties.put("tags", arraySchema); // List<String>

		Schema refSchema = new Schema();
		refSchema.$ref = "#/components/schemas/Role";
		user.properties.put("role", refSchema); // Role

		Schema unknown = new Schema();
		unknown.type = "unknown";
		user.properties.put("unk", unknown);

		api.components.schemas.put("User", user);

		// Test filtering Parse and Emit
		api.components.schemas.put("Emit", new Schema());
		api.components.schemas.put("Parse", new Schema());

		// Test enumValues (should be skipped)
		Schema enumSchema = new Schema();
		enumSchema.enumValues = new ArrayList<>(Arrays.asList("A"));
		api.components.schemas.put("EnumObj", enumSchema);

		String emitted = Emit.emit(api, null);
		assertTrue(emitted.contains("class User"));
		assertTrue(emitted.contains("@Entity"));
		assertTrue(emitted.contains("@Table(name = \"users\")"));
		assertTrue(emitted.contains("public java.util.UUID id;"));
		assertTrue(emitted.contains("@Id"));
		assertTrue(emitted.contains("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
		assertTrue(emitted.contains("public String _1invalid;"));
		assertTrue(emitted.contains("public String enumValue;"));
		assertTrue(emitted.contains("public Boolean active;"));
		assertTrue(emitted.contains("public Integer age;"));
		assertTrue(emitted.contains("public Long longAge;"));
		assertTrue(emitted.contains("public Double dVal;"));
		assertTrue(emitted.contains("public Float fVal;"));
		assertTrue(emitted.contains("public byte[] bytes;"));
		assertTrue(emitted.contains("public java.time.LocalDate ld;"));
		assertTrue(emitted.contains("public java.time.OffsetDateTime odt;"));
		assertTrue(emitted.contains("public String str;"));
		assertTrue(emitted.contains("public Map<String, Object> childMap;"));
		assertTrue(emitted.contains("public Map<String, String> childSpecMap;"));
		assertTrue(emitted.contains("public List<String> tags;"));
		assertTrue(emitted.contains("public Role role;"));
		assertTrue(emitted.contains("public Object unk;"));

		assertFalse(emitted.contains("class Emit"));
		assertFalse(emitted.contains("class Parse"));
		assertFalse(emitted.contains("class EnumObj"));
	}

	@Test
	public void testEmitExisting() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		user.properties = new HashMap<>();
		user.properties.put("newField", new Schema() {
			{
				type = "string";
			}
		});
		user.properties.put("existingField", new Schema() {
			{
				type = "string";
			}
		});
		api.components.schemas.put("User", user);

		String existing = "package com.example;\n" + "import jakarta.persistence.*;\n" + "@Entity\n"
				+ "@Table(name = \"custom_table\")\n" + "public class User {\n" + "    public String existingField;\n"
				+ "}\n";

		String emitted = Emit.emit(api, existing);
		assertTrue(emitted.contains("public String newField;"));
		assertTrue(emitted.contains("public String existingField;"));
		assertTrue(emitted.contains("@Table(name = \"custom_table\")")); // existing shouldn't be overwritten
	}

	@Test
	public void testEmitExistingWithoutAnnotations() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		api.components.schemas.put("User", user);

		String existing = "public class User {}";
		String emitted = Emit.emit(api, existing);
		assertTrue(emitted.contains("@Entity"));
		assertTrue(emitted.contains("@Table(name = \"users\")"));

		// empty existing source
		String emittedEmpty = Emit.emit(api, "   ");
		assertTrue(emittedEmpty.contains("@Entity"));
	}
	@Test
	public void testEmitNotObjectWithAdditionalProps() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema parent = new Schema();
		parent.type = "object";
		parent.properties = new HashMap<>();

		Schema prop1 = new Schema();
		// null type, not object, but has additionalProperties
		prop1.type = null;
		prop1.additionalProperties = new Schema();
		parent.properties.put("prop1", prop1);

		Schema prop2 = new Schema();
		// null type, no additionalProperties
		prop2.type = null;
		parent.properties.put("prop2", prop2);

		api.components.schemas.put("ParentModel", parent);
		String emitted = Emit.emit(api, null);
		assertNotNull(emitted);
	}
	@Test
	public void testResolveTypeNotSchema() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();
		Schema user = new Schema();
		user.properties = new HashMap<>();
		user.properties.put("notSchema", "JustAString");
		user.properties.put("addPropNotSchema", new Schema() {
			{
				type = "object";
				additionalProperties = "StringVal";
			}
		});

		api.components.schemas.put("User", user);
		String emitted = Emit.emit(api, null);
		assertTrue(emitted.contains("public Object notSchema;"));
		assertTrue(emitted.contains("public Map<String, Object> addPropNotSchema;"));
	}
}
