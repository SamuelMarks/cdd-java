import openapi.RefResolver;
import org.junit.Test;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;

public class RefResolverLogicTest {

	@Test
	public void testJsonPointer() throws Exception {
		RefResolver resolver = new RefResolver();
		JSONObject obj = new JSONObject("{\"a\": {\"b\": {\"c\": 42}}, \"arr\": [1, 2, 3]}");

		assertEquals(obj, resolver.resolvePointer(obj, null));
		assertEquals(obj, resolver.resolvePointer(obj, "/"));

		Object val = resolver.resolvePointer(obj, "/a/b/c");
		assertEquals(42, val);

		Object arrVal = resolver.resolvePointer(obj, "/arr/1");
		assertEquals(2, arrVal);

		assertNull(resolver.resolvePointer(obj, "/a/missing"));
	}

	@Test
	public void testBundle() throws Exception {
		File temp = File.createTempFile("test_ext", ".yaml");
		Files.writeString(temp.toPath(), "Pet:\n  type: object\n  properties:\n    name:\n      type: string");

		RefResolver resolver = new RefResolver();
		JSONObject root = new JSONObject(
				"{\"openapi\": \"3.0.0\", \"paths\": {\"/pet\": {\"get\": {\"responses\": {\"200\": {\"schema\": {\"$ref\": \""
						+ temp.getName() + "#/Pet\"}}}}}}}");

		resolver.bundle(root, temp.getParentFile().toURI().toString());

		JSONObject schema = root.getJSONObject("components").getJSONObject("schemas").getJSONObject("Pet");
		assertNotNull(schema);
		assertEquals("object", schema.getString("type"));

		String rewrittenRef = root.getJSONObject("paths").getJSONObject("/pet").getJSONObject("get")
				.getJSONObject("responses").getJSONObject("200").getJSONObject("schema").getString("$ref");
		assertEquals("#/components/schemas/Pet", rewrittenRef);
	}

	@Test
	public void testBundleArray() throws Exception {
		File temp = File.createTempFile("test_arr", ".json");
		Files.writeString(temp.toPath(), "{\"type\": \"string\"}");

		RefResolver resolver = new RefResolver();
		JSONObject root = new JSONObject(
				"{\"components\": {\"schemas\": {\"MyArr\": {\"type\": \"array\", \"items\": {\"$ref\": \""
						+ temp.getName() + "\"}}}}}");

		resolver.bundle(root, temp.getParentFile().toURI().toString());

		String name = temp.getName().substring(0, temp.getName().lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.-]", "_");
		String rewrittenRef = root.getJSONObject("components").getJSONObject("schemas").getJSONObject("MyArr")
				.getJSONObject("items").getString("$ref");
		assertEquals("#/components/schemas/" + name, rewrittenRef);
	}

	@Test
	public void testResolvePointerEdgeCases() throws Exception {
		RefResolver resolver = new RefResolver();
		JSONObject obj = new JSONObject("{\"a\": [1]}");
		assertNull(resolver.resolvePointer(obj, "/a/b")); // non-int on array
		assertNull(resolver.resolvePointer(obj, "/b/c")); // missing
		assertNull(resolver.resolvePointer(obj, "/a/2")); // out of bounds array
	}

	@Test
	public void testCircularDependency() throws Exception {
		File temp1 = File.createTempFile("circ1", ".yaml");
		File temp2 = File.createTempFile("circ2", ".yaml");
		Files.writeString(temp1.toPath(), "ref: \n  $ref: '" + temp2.getName() + "'\n");
		Files.writeString(temp2.toPath(), "ref: \n  $ref: '" + temp1.getName() + "'\n");

		RefResolver resolver = new RefResolver();
		JSONObject root = new JSONObject(
				"{\"paths\": {\"/pet\": {\"get\": {\"responses\": {\"200\": {\"schema\": {\"$ref\": \""
						+ temp1.getName() + "\"}}}}}}}");
		resolver.bundle(root, temp1.getParentFile().toURI().toString());
		assertNotNull(root.getJSONObject("components").getJSONObject("schemas"));
	}

	@Test
	public void testNameCollision() throws Exception {
		File dir1 = Files.createTempDirectory("d1").toFile();
		File dir2 = Files.createTempDirectory("d2").toFile();
		File temp1 = new File(dir1, "col.yaml");
		File temp2 = new File(dir2, "col.yaml");
		Files.writeString(temp1.toPath(), "type: string");
		Files.writeString(temp2.toPath(), "type: integer");

		RefResolver resolver = new RefResolver();
		JSONObject root = new JSONObject("{\"paths\": {\"/1\": {\"schema\": {\"$ref\": \"" + temp1.toURI()
				+ "\"}}, \"/2\": {\"schema\": {\"$ref\": \"" + temp2.toURI() + "\"}}}}");
		resolver.bundle(root, new File(".").toURI().toString());

		JSONObject schemas = root.getJSONObject("components").getJSONObject("schemas");
		assertEquals(2, schemas.keySet().size()); // should have 'col' and 'col_1'
	}

	@Test
	public void testBundleError() {
		RefResolver resolver = new RefResolver();
		JSONObject root = new JSONObject(
				"{\"paths\": {\"/1\": {\"schema\": {\"$ref\": \"http://localhost:0/nonexistent\"}}}}");
		try {
			resolver.bundle(root, "http://localhost:0/");
			fail("Expected exception");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("Failed to bundle reference"));
		}
	}
}
