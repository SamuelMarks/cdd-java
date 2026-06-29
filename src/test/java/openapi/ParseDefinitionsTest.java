package openapi;
import org.junit.Test;
import org.json.JSONObject;
import static org.junit.Assert.*;

public class ParseDefinitionsTest {
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
