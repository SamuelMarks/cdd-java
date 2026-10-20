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

/**
 * Unit and round-trip tests for {@link orm.Parse} and {@link orm.Emit}.
 */
public class OrmParseEmitTest {

	/**
	 * Verifies default constructor of {@link Parse}.
	 */
	@Test
	public void testParseConstructor() {
		assertNotNull(new Parse());
	}

	/**
	 * Verifies default constructor of {@link Emit}.
	 */
	@Test
	public void testEmitConstructor() {
		assertNotNull(new Emit());
	}

	/**
	 * Verifies JPA entity parsing across primitive, boxed, temporal, collection,
	 * and map field types.
	 */
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

		Schema user = api.components.schemas.get("User").value;
		assertEquals(common.PayloadValue.ofString("users_table"), user.extensions.get("x-table-name"));

		Map<String, openapi.ReferenceOr<Schema>> props = user.properties;
		assertNotNull(props);

		Schema idSchema = props.get("user_id").value;
		assertNotNull(idSchema);
		assertEquals(openapi.SchemaType.ofSingle("string"), idSchema.type);
		assertEquals(common.PayloadValue.ofBoolean(true), idSchema.extensions.get("x-primary-key"));

		assertEquals(openapi.SchemaType.ofSingle("integer"), props.get("age").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), props.get("longAge").value.type);
		assertEquals("int64", props.get("longAge").value.format);
		assertEquals(openapi.SchemaType.ofSingle("number"), props.get("doubleVal").value.type);
		assertEquals(openapi.SchemaType.ofSingle("number"), props.get("floatVal").value.type);
		assertEquals("float", props.get("floatVal").value.format);
		assertEquals(openapi.SchemaType.ofSingle("boolean"), props.get("active").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("uuid").value.type);
		assertEquals("uuid", props.get("uuid").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("ld").value.type);
		assertEquals("date", props.get("ld").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("odt").value.type);
		assertEquals("date-time", props.get("odt").value.format);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("zdt").value.type);
		assertEquals("date-time", props.get("zdt").value.format);

		assertEquals(openapi.SchemaType.ofSingle("array"), props.get("stringList").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("stringList").value.items.value.type);
		assertEquals(openapi.SchemaType.ofSingle("array"), props.get("intList").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), props.get("intList").value.items.value.type);
		assertEquals(openapi.SchemaType.ofSingle("array"), props.get("noGenSet").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("noGenSet").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("object"), props.get("mapVal").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"),
				props.get("mapVal").value.additionalProperties.schema.value.type);
		assertEquals(openapi.SchemaType.ofSingle("object"), props.get("singleGenMap").value.type);
		assertNull(props.get("singleGenMap").value.additionalProperties);

		assertEquals("#/components/schemas/SomeCustomType", props.get("custom").value.$ref);

		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("bytes").value.type);
		assertEquals("binary", props.get("bytes").value.format);

		assertEquals(openapi.SchemaType.ofSingle("array"), props.get("stringArray").value.type);
		assertEquals(openapi.SchemaType.ofSingle("string"), props.get("stringArray").value.items.value.type);

		assertEquals(openapi.SchemaType.ofSingle("integer"), props.get("pInt").value.type);
		assertEquals(openapi.SchemaType.ofSingle("integer"), props.get("pLong").value.type);
		assertEquals("int64", props.get("pLong").value.format);
		assertEquals(openapi.SchemaType.ofSingle("number"), props.get("pDouble").value.type);
		assertEquals(openapi.SchemaType.ofSingle("number"), props.get("pFloat").value.type);
		assertEquals(openapi.SchemaType.ofSingle("boolean"), props.get("pBoolean").value.type);
		assertNull(props.get("pShort").value.type);

		assertEquals(openapi.SchemaType.ofSingle("object"), props.get("wildcardMap").value.type);
		assertEquals(openapi.SchemaType.ofSingle("object"),
				props.get("wildcardMap").value.additionalProperties.schema.value.type);

		Schema missingNameTable = api.components.schemas.get("MissingNameTable").value;
		assertNotNull(missingNameTable);
		assertNull(missingNameTable.extensions.get("x-table-name"));
		assertNotNull(missingNameTable.properties.get("missingNameCol"));
	}

	/**
	 * Verifies graceful handling of malformed Java source in {@link Parse#parse}.
	 */
	@Test
	public void testParseError() {
		OpenAPI api = Parse.parse("this is not valid java {{");
		assertNotNull(api);
		assertEquals("3.2.0", api.openapi);
	}

	/**
	 * Verifies null/empty component handling in {@link Emit#emit}.
	 */
	@Test
	public void testEmitNullChecks() {
		OpenAPI api = new OpenAPI();
		assertEquals("", Emit.emit(api, null));
		assertEquals("test", Emit.emit(api, "test"));
		api.components = new openapi.Components();
		assertEquals("", Emit.emit(api, null));
		assertEquals("test2", Emit.emit(api, "test2")); // Hits the components != null && schemas == null case
	}

	/**
	 * Verifies JPA entity emission and round-trip parsing of annotations and
	 * relationships.
	 */
	@Test
	public void testEmitNew() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		user.properties = new LinkedHashMap<>();
		user.properties.put("id", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
				format = "uuid";
			}
		}));
		user.properties.put("active", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("boolean");
			}
		}));
		user.properties.put("1invalid", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("enum", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("const", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("default", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("class", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("age", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("integer");
			}
		}));
		user.properties.put("longAge", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("integer");
				format = "int64";
			}
		}));
		user.properties.put("dVal", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("number");
			}
		}));
		user.properties.put("fVal", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("number");
				format = "float";
			}
		}));
		user.properties.put("bytes", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
				format = "binary";
			}
		}));
		user.properties.put("ld", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
				format = "date";
			}
		}));
		user.properties.put("odt", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
				format = "date-time";
			}
		}));
		user.properties.put("str", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));

		Schema child = new Schema();
		child.type = openapi.SchemaType.ofSingle("object");
		user.properties.put("childMap", openapi.ReferenceOr.ofValue(child)); // map<string, object>

		Schema childMapSchema = new Schema();
		childMapSchema.type = openapi.SchemaType.ofSingle("object");
		childMapSchema.additionalProperties = openapi.AdditionalProperties.ofSchema(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		});
		user.properties.put("childSpecMap", openapi.ReferenceOr.ofValue(childMapSchema)); // map<string, string>

		Schema arraySchema = new Schema();
		arraySchema.type = openapi.SchemaType.ofSingle("array");
		arraySchema.items = openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		});
		user.properties.put("tags", openapi.ReferenceOr.ofValue(arraySchema)); // List<String>

		Schema refSchema = new Schema();
		refSchema.$ref = "#/components/schemas/Role";
		user.properties.put("role", openapi.ReferenceOr.ofValue(refSchema)); // Role

		Schema reqRefSchema = new Schema();
		reqRefSchema.$ref = "#/components/schemas/Department";
		user.properties.put("dept", openapi.ReferenceOr.ofValue(reqRefSchema));

		user.required = new ArrayList<>(Arrays.asList("active", "dept"));

		Schema unknown = new Schema();
		unknown.type = openapi.SchemaType.ofSingle("unknown");
		user.properties.put("unk", openapi.ReferenceOr.ofValue(unknown));

		api.components.schemas.put("User", openapi.ReferenceOr.ofValue(user));

		// Test filtering Parse and Emit
		api.components.schemas.put("Emit", openapi.ReferenceOr.ofValue(new Schema()));
		api.components.schemas.put("Parse", openapi.ReferenceOr.ofValue(new Schema()));

		// Test enumValues (should be skipped)
		Schema enumSchema = new Schema();
		enumSchema.enumValues = new ArrayList<>(Arrays.asList(common.PayloadValue.ofString("A")));
		api.components.schemas.put("EnumObj", openapi.ReferenceOr.ofValue(enumSchema));

		String emitted = Emit.emit(api, null);
		assertTrue(emitted.contains("class User"));
		assertTrue(emitted.contains("@Entity"));
		assertTrue(emitted.contains("@Table(name = \"users\")"));
		assertTrue(emitted.contains("public java.util.UUID id;"));
		assertTrue(emitted.contains("@Id"));
		assertTrue(emitted.contains("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
		assertTrue(emitted.contains("@ManyToOne"));
		assertTrue(emitted.contains("@JoinColumn(name = \"role_id\")"));
		assertTrue(emitted.contains("@JoinColumn(name = \"dept_id\", nullable = false)"));
		assertTrue(emitted.contains("@Column(name = \"active\", nullable = false)"));
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

		OpenAPI roundTrip = Parse.parse(emitted
				+ "\n@Entity public class Extra { @JoinColumn(name = \"custom_col\", nullable = true) @JoinColumn public Role r; }");
		Schema parsedUser = roundTrip.components.schemas.get("User").value;
		assertNotNull(parsedUser);
		assertNotNull(parsedUser.required);
		assertTrue(parsedUser.required.contains("active"));
		assertTrue(parsedUser.required.contains("dept"));
		assertEquals(common.PayloadValue.ofBoolean(true),
				parsedUser.properties.get("id").value.extensions.get("x-primary-key"));
		assertEquals(common.PayloadValue.ofBoolean(true),
				parsedUser.properties.get("id").value.extensions.get("x-generated-value"));
		assertEquals(common.PayloadValue.ofString("many-to-one"),
				parsedUser.properties.get("role").value.extensions.get("x-relationship"));
		assertEquals(common.PayloadValue.ofString("one-to-many"),
				parsedUser.properties.get("tags").value.extensions.get("x-relationship"));
	}

	/**
	 * Verifies lexical-preserving JPA entity emission into existing source.
	 */
	@Test
	public void testEmitExisting() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		user.properties = new HashMap<>();
		user.properties.put("newField", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		user.properties.put("existingField", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("string");
			}
		}));
		api.components.schemas.put("User", openapi.ReferenceOr.ofValue(user));

		String existing = "package com.example;\n" + "import jakarta.persistence.*;\n" + "@Entity\n"
				+ "@Table(name = \"custom_table\")\n" + "public class User {\n" + "    public String existingField;\n"
				+ "}\n";

		String emitted = Emit.emit(api, existing);
		assertTrue(emitted.contains("public String newField;"));
		assertTrue(emitted.contains("public String existingField;"));
		assertTrue(emitted.contains("@Table(name = \"custom_table\")")); // existing shouldn't be overwritten
	}

	/**
	 * Verifies JPA entity emission adds {@code @Entity} and {@code @Table} when
	 * absent on an existing class.
	 */
	@Test
	public void testEmitExistingWithoutAnnotations() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema user = new Schema();
		api.components.schemas.put("User", openapi.ReferenceOr.ofValue(user));

		String existing = "public class User {}";
		String emitted = Emit.emit(api, existing);
		assertTrue(emitted.contains("@Entity"));
		assertTrue(emitted.contains("@Table(name = \"users\")"));

		// empty existing source
		String emittedEmpty = Emit.emit(api, "   ");
		assertTrue(emittedEmpty.contains("@Entity"));
	}

	/**
	 * Verifies emission of untyped properties with and without
	 * {@code additionalProperties}.
	 */
	@Test
	public void testEmitNotObjectWithAdditionalProps() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		Schema parent = new Schema();
		parent.type = openapi.SchemaType.ofSingle("object");
		parent.properties = new HashMap<>();

		Schema prop1 = new Schema();
		// null type, not object, but has additionalProperties
		prop1.type = null;
		prop1.additionalProperties = openapi.AdditionalProperties.ofSchema(new Schema());
		parent.properties.put("prop1", openapi.ReferenceOr.ofValue(prop1));

		Schema prop2 = new Schema();
		// null type, no additionalProperties
		prop2.type = null;
		parent.properties.put("prop2", openapi.ReferenceOr.ofValue(prop2));

		api.components.schemas.put("ParentModel", openapi.ReferenceOr.ofValue(parent));
		String emitted = Emit.emit(api, null);
		assertTrue(emitted.contains("public Map<String, Object> prop1;"));
		assertTrue(emitted.contains("public Object prop2;"));
	}

	/**
	 * Verifies fallback type resolution when property or additionalProperties is
	 * not a {@link Schema}.
	 */
	@Test
	public void testResolveTypeNotSchema() {
		OpenAPI api = new OpenAPI();
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();
		Schema user = new Schema();
		user.properties = new HashMap<>();
		user.properties.put("notSchema", null);
		user.properties.put("addPropNotSchema", openapi.ReferenceOr.ofValue(new Schema() {
			{
				type = openapi.SchemaType.ofSingle("object");
				additionalProperties = openapi.AdditionalProperties.ofBoolean(true);
			}
		}));

		api.components.schemas.put("User", openapi.ReferenceOr.ofValue(user));
		String emitted = Emit.emit(api, null);
		assertTrue(emitted.contains("public Object notSchema;"));
		assertTrue(emitted.contains("public Map<String, Object> addPropNotSchema;"));
	}
}
