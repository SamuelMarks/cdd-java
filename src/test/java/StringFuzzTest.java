import org.junit.Test;
import static org.junit.Assert.*;
import openapi.OpenAPI;

/**
 * Fuzz and boundary tests for {@link routes.Parse} and {@link cli.Parse} on
 * valid and malformed input strings.
 */
public class StringFuzzTest {

	/**
	 * Verifies that {@link routes.Parse#parse(String)} cleanly handles both valid
	 * HTTP client route snippets and arbitrary/malformed Java snippets without
	 * throwing exceptions, always returning a valid {@link OpenAPI} model.
	 */
	@Test
	public void fuzzRoutes() {
		String nl = String.valueOf((char) 10);
		String[] inputs = new String[]{"/** " + nl + "* @HEAD test " + nl + "*/ class T { void m() { HEAD(); } }",
				"/** " + nl + "* @HEAD test " + nl + "*/ class T { void m() { obj.HEAD(); } }",
				"/** " + nl + "* @method test " + nl + "*/ class T { void m() { method(); } }",
				"/** " + nl + "* @method test " + nl + "*/ class T { void m() { obj.method(); } }",
				"/** " + nl + "* @header test " + nl + "*/ class T { void m() { header(); } }",
				"/** " + nl + "* @header test " + nl + "*/ class T { void m() { obj.header(); } }",
				"/** " + nl + "* @query test " + nl + "*/ class T { void m() { query(); } }",
				"/** " + nl + "* @query test " + nl + "*/ class T { void m() { obj.query(); } }",
				"/** " + nl + "* @QUERY test " + nl + "*/ class T { void m() { QUERY(); } }",
				"/** " + nl + "* @QUERY test " + nl + "*/ class T { void m() { obj.QUERY(); } }",
				"/** " + nl + "* @DELETE test " + nl + "*/ class T { void m() { DELETE(); } }",
				"/** " + nl + "* @DELETE test " + nl + "*/ class T { void m() { obj.DELETE(); } }",
				"/** " + nl + "* @application/json test " + nl + "*/ class T { void m() { application/json(); } }",
				"/** " + nl + "* @Authorization test " + nl + "*/ class T { void m() { obj.Authorization(); } }",
				"/** " + nl + "* @description test " + nl + "*/ class T { void m() { obj.description(); } }",
				"/** " + nl + "* @licenseName MIT " + nl + "* @licenseUrl https://opensource.org/licenses/MIT " + nl
						+ "* @licenseIdentifier MIT " + nl + "*/ public class ApiClient {}",
				"/** " + nl + "* @PUT test " + nl + "*/ class T { void m() { obj.PUT(); } }",
				"/** " + nl + "* @jsonSchemaDialect https://spec.openapis.org/oas/3.1/dialect/base " + nl
						+ "* @openapiVersion 3.2.0 " + nl + "* @openapiSelf https://example.com/openapi.json " + nl
						+ "*/ public class ApiClient {}",
				"/** " + nl + "* @OPTIONS test " + nl + "*/ class T { void m() { obj.OPTIONS(); } }",
				"/** " + nl + "* @POST test " + nl + "*/ class T { void m() { obj.POST(); } }",
				"/** " + nl + "* @TRACE test " + nl + "*/ class T { void m() { obj.TRACE(); } }",
				"/** " + nl + "* @GET test " + nl + "*/ class T { void m() { obj.GET(); } }",
				"/** " + nl + "* @PATCH test " + nl + "*/ class T { void m() { obj.PATCH(); } }",
				"/** " + nl + "* @contactName Support " + nl + "* @contactUrl https://example.com " + nl
						+ "* @contactEmail support@example.com " + nl + "* @termsOfService https://example.com/tos "
						+ nl + "*/ public class ApiClient {}"};

		for (String input : inputs) {
			OpenAPI api = routes.Parse.parse(input);
			assertNotNull("Expected non-null OpenAPI for input: " + input, api);
			assertNotNull("Expected non-null paths for input: " + input, api.paths);
		}
	}

	/**
	 * Verifies that {@link cli.Parse#parse(String)} cleanly handles both valid and
	 * arbitrary/malformed CLI help snippets without throwing exceptions, always
	 * returning a valid {@link OpenAPI} model.
	 */
	@Test
	public void fuzzCli() {
		String nl = String.valueOf((char) 10);
		String[] inputs = new String[]{"/** " + nl + "* @true test " + nl + "*/ class T { void m() { true(); } }",
				"/** " + nl + "* @options test " + nl + "*/ class T { void m() { obj.options(); } }",
				"/** " + nl + "* @Component test " + nl + "*/ class T { void m() { obj.Component(); } }",
				"/** " + nl + "* @query test " + nl + "*/ class T { void m() { obj.query(); } }",
				"/** " + nl + "* @callbacks test " + nl + "*/ class T { void m() { obj.callbacks(); } }",
				"/** " + nl + "* @responses test " + nl + "*/ class T { void m() { obj.responses(); } }",
				"/** " + nl + "* @mediaTypes test " + nl + "*/ class T { void m() { obj.mediaTypes(); } }",
				"/** " + nl + "* @head test " + nl + "*/ class T { void m() { obj.head(); } }",
				"/** " + nl + "* @patch test " + nl + "*/ class T { void m() { obj.patch(); } }",
				"/** " + nl + "* @headers test " + nl + "*/ class T { void m() { obj.headers(); } }",
				"/** " + nl + "* @get test " + nl + "*/ class T { void m() { obj.get(); } }",
				"/** " + nl + "* @parameters test " + nl + "*/ class T { void m() { obj.parameters(); } }",
				"/** " + nl + "* @put test " + nl + "*/ class T { void m() { obj.put(); } }",
				"/** " + nl + "* @requestBodies test " + nl + "*/ class T { void m() { obj.requestBodies(); } }",
				"/** " + nl + "* @post test " + nl + "*/ class T { void m() { obj.post(); } }",
				"/** " + nl + "* @pathItems test " + nl + "*/ class T { void m() { obj.pathItems(); } }",
				"/** " + nl + "* @links test " + nl + "*/ class T { void m() { obj.links(); } }",
				"/** " + nl + "* @trace test " + nl + "*/ class T { void m() { obj.trace(); } }",
				"/** " + nl + "* @delete test " + nl + "*/ class T { void m() { obj.delete(); } }"};

		for (String input : inputs) {
			OpenAPI api = cli.Parse.parse(input);
			assertNotNull("Expected non-null OpenAPI for input: " + input, api);
			assertNotNull("Expected non-null components for input: " + input, api.components);
		}
	}
}
