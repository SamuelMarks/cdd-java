import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.*;

public class OpenapiPojoTest {
    @Test
    public void testPojos() throws Exception {
        String[] pojos = {
            "openapi.Callback", "openapi.Components", "openapi.Contact", "openapi.Discriminator",
            "openapi.Encoding", "openapi.Example", "openapi.ExternalDocumentation", "openapi.Header",
            "openapi.Info", "openapi.Items", "openapi.License", "openapi.Link", "openapi.MediaType",
            "openapi.OAuthFlow", "openapi.OAuthFlows", "openapi.OpenAPI", "openapi.Operation",
            "openapi.Parameter", "openapi.PathItem", "openapi.Paths", "openapi.Reference",
            "openapi.RequestBody", "openapi.Response", "openapi.Responses", "openapi.Schema",
            "openapi.SecurityRequirement", "openapi.SecurityScheme", "openapi.Server", "openapi.ServerVariable",
            "openapi.Tag", "openapi.XML"
        };
        
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
    }
}
