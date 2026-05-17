import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.Collections;
import openapi.*;

public class OpenapiPojoTest {
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

			// Look for addExtension
			try {
				Method addExt = clazz.getMethod("addExtension", String.class, Object.class);
				addExt.invoke(inst, "x-test", "val");
				addExt.invoke(inst, "invalid", "val"); // branch coverage for startswith x-
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
		assertEquals(1, sr.requirements.size());
	}
}
