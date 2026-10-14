package openapi;
import org.junit.Test;
import org.json.JSONObject;
import static org.junit.Assert.*;

/**
 * Unit test verifying Swagger 2.0 {@code definitions} parsing when
 * {@code components} is absent.
 */
public class ParseDefinitionsTest {
	/**
	 * Verifies {@code definitions} are populated into {@code components.schemas}.
	 *
	 * @throws Exception
	 *             If parsing fails.
	 */
	@Test
	public void testDefinitionsWithoutComponents() throws Exception {
		JSONObject obj = new JSONObject("{\"definitions\": {\"Pet\": {\"type\": \"object\"}}}");
		OpenAPI api = Parse.fromJson(obj);
		assertNotNull(api.components);
		assertNotNull(api.components.schemas.get("Pet"));

		try {
			Parse.fromJson(null);
			fail();
		} catch (Exception e) {
		}
	}
}
