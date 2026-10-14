import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.Collections;
import openapi.*;

/**
 * Unit tests for all OpenAPI AST POJO classes and union wrappers.
 */
public class OpenapiPojoTest {
	/**
	 * Verifies constructors, extensions, structural equality, hash codes, and
	 * string representations of all OpenAPI model classes.
	 *
	 * @throws Exception
	 *             If reflective instantiation fails.
	 */
	@Test
	public void testPojos() throws Exception {
		String[] pojos = {"openapi.Callback", "openapi.Components", "openapi.Contact", "openapi.Discriminator",
				"openapi.Encoding", "openapi.Example", "openapi.ExternalDocumentation", "openapi.Header",
				"openapi.Info", "openapi.Items", "openapi.License", "openapi.Link", "openapi.MediaType",
				"openapi.OAuthFlow", "openapi.OAuthFlows", "openapi.OpenAPI", "openapi.Operation", "openapi.Parameter",
				"openapi.PathItem", "openapi.Paths", "openapi.Reference", "openapi.RequestBody", "openapi.Response",
				"openapi.Responses", "openapi.Schema", "openapi.SecurityRequirement", "openapi.SecurityScheme",
				"openapi.Server", "openapi.ServerVariable", "openapi.Tag", "openapi.XML"};

		for (String p : pojos) {
			Class<?> clazz = Class.forName(p);
			Object inst = clazz.getDeclaredConstructor().newInstance();
			Object inst2 = clazz.getDeclaredConstructor().newInstance();
			assertEquals(inst, inst);
			assertEquals(inst, inst2);
			assertNotEquals(inst, null);
			assertNotEquals(inst, "otherType");
			assertEquals(inst.hashCode(), inst2.hashCode());
			assertNotNull(inst.toString());

			// Look for addExtension and getExtensions
			try {
				Method addExt = clazz.getMethod("addExtension", String.class, Object.class);
				addExt.invoke(inst, "x-test", "val");
				addExt.invoke(inst, "invalid", "val"); // branch coverage for startswith x-
				Method getExt = clazz.getMethod("getExtensions");
				assertNotNull(getExt.invoke(inst));
				assertNotEquals(inst, inst2);
			} catch (NoSuchMethodException e) {
				// Ignore
			}
		}

		// Paths
		Paths paths = new Paths();
		paths.addProperty("x-ext", "extVal");
		paths.addProperty("/path", new PathItem());
		paths.addProperty("invalid", "val");
		paths.addProperty("/invalid_type", "val");
		assertEquals(2, paths.getProperties().size());

		// Responses
		Responses responses = new Responses();
		responses.addProperty("default", new Response());
		responses.addProperty("x-ext", "extVal");
		responses.addProperty("200", new Response());
		responses.addProperty("invalid", "val");
		responses.addProperty("201", "val"); // not a Response
		assertEquals(4, responses.getProperties().size());
		assertNotNull(responses.defaultResponse);
		// Callback
		Callback cb = new Callback();
		cb.addProperty("x-ext", "extVal");
		cb.addProperty("http://expr", new PathItem());
		cb.addProperty("invalid", "val");
		assertEquals(2, cb.getProperties().size());
		// SecurityRequirement
		SecurityRequirement sr = new SecurityRequirement();
		sr.addRequirement("req", Collections.singletonList("scope"));
		assertEquals(1, sr.getRequirements().size());

		// ReferenceOr
		ReferenceOr<String> valWrapper = ReferenceOr.ofValue("hello");
		ReferenceOr<String> valWrapper2 = ReferenceOr.ofValue("hello");
		ReferenceOr<String> refWrapper = ReferenceOr.ofRef("#/components/schemas/Foo");
		Reference refObj = new Reference();
		refObj.ref = "#/components/schemas/Foo";
		ReferenceOr<String> refWrapper2 = ReferenceOr.ofRef(refObj);
		ReferenceOr<String> refWrapperDiff = ReferenceOr.ofRef("#/components/schemas/Bar");
		assertTrue(valWrapper.isValue());
		assertFalse(valWrapper.isRef());
		assertTrue(refWrapper.isRef());
		assertFalse(refWrapper.isValue());
		assertEquals(valWrapper, valWrapper);
		assertEquals(valWrapper, valWrapper2);
		assertNotEquals(valWrapper, refWrapper);
		assertNotEquals(refWrapper, refWrapperDiff);
		assertNotEquals(valWrapper, "notReferenceOr");
		assertEquals(valWrapper.hashCode(), valWrapper2.hashCode());
		assertEquals("Value(hello)", valWrapper.toString());
		assertEquals("Ref(#/components/schemas/Foo)", refWrapper.toString());
		assertNotNull(refWrapper2);

		// SchemaType
		SchemaType singleType = new SchemaType("string");
		SchemaType singleType2 = new SchemaType("string");
		SchemaType nullSingle = new SchemaType((String) null);
		SchemaType multiType = new SchemaType(java.util.Arrays.asList("string", "null"));
		SchemaType emptyMulti = new SchemaType((java.util.List<String>) null);
		assertFalse(singleType.isMultiType());
		assertTrue(multiType.isMultiType());
		assertNull(nullSingle.single);
		assertNull(emptyMulti.single);
		assertEquals(singleType, singleType);
		assertEquals(singleType, singleType2);
		assertNotEquals(singleType, multiType);
		assertNotEquals(singleType, nullSingle);
		assertNotEquals(singleType, "notSchemaType");
		assertEquals(singleType.hashCode(), singleType2.hashCode());
		assertEquals("string", singleType.toString());
		assertEquals("[string, null]", multiType.toString());

		// AdditionalProperties
		AdditionalProperties boolAp = AdditionalProperties.ofBoolean(true);
		AdditionalProperties boolAp2 = AdditionalProperties.ofBoolean(true);
		Schema apSchema = new Schema();
		AdditionalProperties schemaAp = AdditionalProperties.ofSchema(apSchema);
		AdditionalProperties refAp = AdditionalProperties.ofRefOrSchema(ReferenceOr.ofRef("#/components/schemas/Bar"));
		assertTrue(boolAp.isBoolean());
		assertFalse(boolAp.isSchema());
		assertTrue(schemaAp.isSchema());
		assertFalse(schemaAp.isBoolean());
		assertEquals(boolAp, boolAp);
		assertEquals(boolAp, boolAp2);
		assertNotEquals(boolAp, schemaAp);
		assertNotEquals(schemaAp, refAp);
		assertNotEquals(boolAp, "notAp");
		assertEquals(boolAp.hashCode(), boolAp2.hashCode());
		assertEquals("true", boolAp.toString());
		assertTrue(refAp.toString().contains("Ref("));
		Field infoTitleField = Info.class.getField("title");
		assertNull(TypeMapper.getFieldValue(infoTitleField, "wrongTargetType"));
	}
}
