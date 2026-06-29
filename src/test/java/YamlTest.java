import openapi.OpenAPI;
import openapi.Parse;
import org.junit.Test;
import static org.junit.Assert.*;

public class YamlTest {
	@Test
	public void testYamlParse() throws Exception {
		String yaml = "openapi: 3.0.0\ninfo:\n  title: test\n  version: '1.0'\npaths:\n  /test:\n    get:\n      responses:\n        '200':\n          description: ok";
		OpenAPI api = Parse.fromString(yaml);
		assertEquals("3.0.0", api.openapi);
		assertEquals("test", api.info.title);
		assertNotNull(api.paths.pathItems.get("/test").get);
	}
}
