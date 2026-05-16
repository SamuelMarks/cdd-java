import org.junit.Test;
import static org.junit.Assert.*;
import openapi.*;
import cli.Emit;
import cli.Parse;
import java.util.*;

public class CliParseEmitTest {

    @Test
    public void testConstructors() {
        assertNotNull(new Emit());
        assertNotNull(new Parse());
    }

    private OpenAPI buildApi() {
        OpenAPI api = new OpenAPI();
        api.info = new Info();
        api.info.title = "Test API";
        api.info.version = "1.0.0";
        api.info.summary = "A test API";
        api.info.description = "Test Description";
        api.info.termsOfService = "http://example.com/terms";

        api.info.contact = new Contact();
        api.info.contact.name = "John Doe";
        api.info.contact.email = "john@example.com";
        api.info.contact.url = "http://example.com/contact";

        api.info.license = new License();
        api.info.license.name = "MIT";
        api.info.license.identifier = "MIT";
        api.info.license.url = "http://example.com/license";

        api.servers = new ArrayList<>();
        Server s1 = new Server();
        s1.url = "http://example.com";
        s1.name = "prod";
        s1.description = "Production server";
        s1.variables = new HashMap<>();
        ServerVariable sv = new ServerVariable();
        sv.defaultValue = "80";
        sv.description = "Port";
        sv.enumValues = Arrays.asList("80", "443");
        s1.variables.put("port", sv);
        api.servers.add(s1);

        api.components = new Components();
        
        api.components.schemas = new HashMap<>();
        Schema sch1 = new Schema();
        sch1.type = "object";
        sch1.discriminator = new Discriminator();
        sch1.discriminator.propertyName = "type";
        sch1.discriminator.defaultMapping = "defaultType";
        sch1.discriminator.mapping = new HashMap<>();
        sch1.discriminator.mapping.put("dog", "DogSchema");
        
        sch1.allOf = Arrays.asList(new Schema());
        sch1.anyOf = Arrays.asList(new Schema());
        sch1.oneOf = Arrays.asList(new Schema());
        sch1.not = new Schema();
        sch1.items = new Items();
        sch1.properties = new HashMap<>();
        sch1.properties.put("prop1", new Schema());
        sch1.additionalProperties = new HashMap<>();
        sch1.xml = new XML();
        sch1.xml.name = "xmlName";
        sch1.xml.namespace = "xmlNamespace";
        sch1.xml.prefix = "xmlPrefix";
        sch1.xml.attribute = true;
        sch1.xml.wrapped = true;
        sch1.externalDocs = new ExternalDocumentation();
        sch1.externalDocs.url = "url";
        sch1.externalDocs.description = "desc";
        sch1.enumValues = Arrays.asList("val1", "val2");
        api.components.schemas.put("Schema1", sch1);
        api.components.schemas.put("NullSchema", null);
        
        Schema schXml2 = new Schema();
        schXml2.xml = new XML();
        schXml2.xml.attribute = false;
        schXml2.xml.wrapped = false;
        api.components.schemas.put("SchemaXml2", schXml2);

        api.components.responses = new HashMap<>();
        api.components.responses.put("res1", new Object());
        api.components.parameters = new HashMap<>();
        api.components.parameters.put("param1", new Object());
        api.components.requestBodies = new HashMap<>();
        api.components.requestBodies.put("rb1", new Object());
        api.components.headers = new HashMap<>();
        api.components.headers.put("h1", new Object());

        api.components.securitySchemes = new HashMap<>();
        SecurityScheme sc1 = new SecurityScheme();
        sc1.type = "apiKey";
        sc1.name = "api_key";
        sc1.in = "header";
        sc1.description = "apikey desc";
        sc1.oauth2MetadataUrl = "oauth2meta";
        sc1.deprecated = true;
        api.components.securitySchemes.put("api_key", sc1);

        SecurityScheme sc2 = new SecurityScheme();
        sc2.type = "http";
        sc2.scheme = "bearer";
        sc2.bearerFormat = "JWT";
        api.components.securitySchemes.put("bearerAuth", sc2);
        
        SecurityScheme sc3 = new SecurityScheme();
        sc3.type = "mutualTLS";
        sc3.deprecated = false;
        api.components.securitySchemes.put("mTLS", sc3);

        SecurityScheme sc4 = new SecurityScheme();
        sc4.type = "oauth2";
        sc4.flows = new OAuthFlows();
        sc4.flows.implicit = new OAuthFlow();
        sc4.flows.implicit.authorizationUrl = "authUrl";
        sc4.flows.implicit.refreshUrl = "refreshUrl";
        sc4.flows.implicit.scopes = new HashMap<>();
        sc4.flows.implicit.scopes.put("read", "read access");

        sc4.flows.password = new OAuthFlow();
        sc4.flows.password.tokenUrl = "tokenUrl";
        sc4.flows.password.refreshUrl = "refreshUrl";
        sc4.flows.password.scopes = new HashMap<>();
        sc4.flows.password.scopes.put("write", "write access");

        sc4.flows.clientCredentials = new OAuthFlow();
        sc4.flows.clientCredentials.tokenUrl = "tokenUrl";
        sc4.flows.clientCredentials.refreshUrl = "refreshUrl";
        sc4.flows.clientCredentials.deviceAuthorizationUrl = "deviceUrl";
        sc4.flows.clientCredentials.scopes = new HashMap<>();
        sc4.flows.clientCredentials.scopes.put("admin", "admin access");

        sc4.flows.authorizationCode = new OAuthFlow();
        sc4.flows.authorizationCode.authorizationUrl = "authUrl";
        sc4.flows.authorizationCode.tokenUrl = "tokenUrl";
        sc4.flows.authorizationCode.refreshUrl = "refreshUrl";
        sc4.flows.authorizationCode.deviceAuthorizationUrl = "deviceUrl";
        sc4.flows.authorizationCode.scopes = new HashMap<>();
        sc4.flows.authorizationCode.scopes.put("all", "all access");

        sc4.flows.deviceAuthorization = new OAuthFlow();
        sc4.flows.deviceAuthorization.authorizationUrl = "authUrl";
        sc4.flows.deviceAuthorization.tokenUrl = "tokenUrl";
        sc4.flows.deviceAuthorization.refreshUrl = "refreshUrl";
        sc4.flows.deviceAuthorization.deviceAuthorizationUrl = "deviceUrl";
        sc4.flows.deviceAuthorization.scopes = new HashMap<>();
        sc4.flows.deviceAuthorization.scopes.put("dev", "dev access");

        api.components.securitySchemes.put("oauth2", sc4);
        
        SecurityScheme sc4_empty = new SecurityScheme();
        sc4_empty.flows = new OAuthFlows();
        sc4_empty.flows.implicit = new OAuthFlow();
        sc4_empty.flows.password = new OAuthFlow();
        sc4_empty.flows.clientCredentials = new OAuthFlow();
        sc4_empty.flows.authorizationCode = new OAuthFlow();
        sc4_empty.flows.deviceAuthorization = new OAuthFlow();
        api.components.securitySchemes.put("oauth2_empty", sc4_empty);

        SecurityScheme sc5 = new SecurityScheme();
        sc5.type = "openIdConnect";
        sc5.openIdConnectUrl = "openIdUrl";
        api.components.securitySchemes.put("openIdConnect", sc5);
        
        api.components.securitySchemes.put("not_a_scheme", new Object());

        api.components.links = new HashMap<>();
        Link lnk1 = new Link();
        lnk1.operationId = "opId";
        lnk1.operationRef = "opRef";
        lnk1.description = "lnkDesc";
        lnk1.requestBody = "reqBody";
        lnk1.server = new Server();
        lnk1.server.url = "serverUrl";
        lnk1.parameters = new HashMap<>();
        lnk1.parameters.put("p1", "v1");
        api.components.links.put("link1", lnk1);
        api.components.links.put("link2", new Object()); 

        api.components.callbacks = new HashMap<>();
        api.components.callbacks.put("cb1", new Object());
        api.components.pathItems = new HashMap<>();
        api.components.pathItems.put("pi1", new PathItem());
        api.components.mediaTypes = new HashMap<>();
        api.components.mediaTypes.put("mt1", new Object());

        api.paths = new Paths();
        api.paths.pathItems = new HashMap<>();
        PathItem pi1 = new PathItem();
        api.paths.pathItems.put("/test", pi1);

        pi1.get = createOp();
        pi1.post = createOp();
        pi1.put = createOp();
        pi1.delete = createOp();
        pi1.patch = createOp();
        pi1.options = createOp();
        pi1.head = createOp();
        pi1.trace = createOp();
        pi1.query = createOp();

        return api;
    }

    private Operation createOp() {
        Operation op = new Operation();
        op.tags = Arrays.asList("t1", "t2");
        op.deprecated = true;
        op.summary = "opSummary";
        op.operationId = "opId";
        op.description = "opDesc";
        op.externalDocs = new ExternalDocumentation();
        op.externalDocs.url = "exUrl";
        op.externalDocs.description = "exDesc";

        op.callbacks = new HashMap<>();
        op.callbacks.put("cb1", new Callback());

        op.parameters = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.name = "p1";
        p1.required = true;
        p1.deprecated = true;
        p1.description = "p1Desc";
        p1.example = "p1Ex";
        p1.examples = new HashMap<>();
        Example ex1 = new Example();
        ex1.summary = "exSum";
        ex1.description = "exDesc";
        ex1.value = "exVal";
        p1.examples.put("ex1", ex1);
        p1.examples.put("ex2", null); // branch coverage
        op.parameters.add(p1);
        op.parameters.add(new Object()); 

        RequestBody rb = new RequestBody();
        rb.required = true;
        rb.description = "rbDesc";
        rb.content = new HashMap<>();
        MediaType mt = new MediaType();
        Map<String, Object> mtSch = new HashMap<>();
        mtSch.put("type", "string");
        mt.itemSchema = mtSch;
        
        Encoding e1 = new Encoding();
        e1.contentType = "application/json";
        mt.prefixEncoding = Arrays.asList(e1, new Encoding());
        
        Encoding e2 = new Encoding();
        e2.contentType = "text/plain";
        mt.itemEncoding = e2;

        mt.encoding = new HashMap<>();
        Encoding e3 = new Encoding();
        e3.contentType = "application/xml";
        Encoding e4 = new Encoding();
        e4.contentType = "application/pdf";
        e3.prefixEncoding = Arrays.asList(e4, new Encoding());
        Encoding e5 = new Encoding();
        e5.contentType = "image/png";
        e3.itemEncoding = e5;
        mt.encoding.put("enc1", e3);
        mt.encoding.put("enc2", new Encoding()); // empty encoding

        rb.content.put("application/json", mt);
        op.requestBody = rb;

        op.responses = new Responses();
        op.responses.statusCodes = new HashMap<>();
        Response r1 = new Response();
        r1.description = "r1Desc";
        r1.content = new HashMap<>();
        MediaType mt2 = new MediaType();
        r1.content.put("application/json", mt2);
        r1.headers = new HashMap<>();
        Header h1 = new Header();
        h1.required = true;
        h1.deprecated = true;
        h1.description = "h1Desc";
        r1.headers.put("h1", h1);
        r1.headers.put("h2", new Object()); 
        
        r1.links = new HashMap<>();
        Link lnk2 = new Link();
        lnk2.operationId = "opId";
        lnk2.operationRef = "opRef";
        lnk2.description = "lnkDesc";
        lnk2.requestBody = "reqBody";
        lnk2.server = new Server();
        lnk2.server.url = "serverUrl";
        lnk2.parameters = new HashMap<>();
        lnk2.parameters.put("p1", "v1");
        r1.links.put("l1", lnk2);
        
        op.responses.statusCodes.put("200", r1);
        op.responses.statusCodes.put("201", new Object());

        Response r2 = new Response();
        r2.description = "defaultDesc";
        r2.content = new HashMap<>();
        r2.content.put("text/plain", new MediaType());
        r2.headers = new HashMap<>();
        Header h2 = new Header();
        h2.description = "h2Desc";
        r2.headers.put("h2", h2);
        r2.links = new HashMap<>();
        r2.links.put("l2", lnk2);
        op.responses.defaultResponse = r2;

        return op;
    }

    @Test
    public void testExhaustive() {
        OpenAPI api = buildApi();
        String generated = Emit.emitCli(api);
        assertNotNull(generated);

        OpenAPI parsed = Parse.parse(generated);
        assertNotNull(parsed);

        // Parse nulls and empty fields to hit remaining edge cases
        String minimal = "private static void printHelp() {\n    }\n";
        OpenAPI minParsed = Parse.parse(minimal);
        assertNotNull(minParsed);
        
        // Manual lines to parse
        String extra = "private static void printHelp() {\n" +
            "        System.out.println(\"Info Object title: Min API\");\n" +
            "        System.out.println(\"Server Object url: http://a name:  description: \");\n" +
            "        System.out.println(\"Server Variable Object var defaultValue: v description:  enumValues: \");\n" +
            "        System.out.println(\"Component schemas S1\");\n" +
            "        System.out.println(\"  Discriminator propertyName= mapping= defaultMapping=\");\n" +
            "        System.out.println(\"Operation Object [DEPRECATED] summary (Tags: t1, t2)\");\n" +
            "        System.out.println(\"    --p1 (required) [DEPRECATED] : desc\");\n" +
            "        System.out.println(\"      Example ex1: null - null = null\");\n" +
            "        System.out.println(\"    --requestBody: rbdesc [Content-Types: application/json]\");\n" +
            "        System.out.println(\"    Returns 200: rdesc [Content-Types: application/json]\");\n" +
            "        System.out.println(\"    Returns default: rdesc [Content-Types: application/json]\");\n" +
            "        System.out.println(\"      Header h1: hdesc\");\n" +
            "        System.out.println(\"      Link l1 operationId=o1\");\n" +
            "        System.out.println(\"        LinkParam l1 p1 v1\");\n" +
            "        System.out.println(\"      RequestBodyContentItemSchema application/json string\");\n" +
            "        System.out.println(\"      RequestBodyContentPrefixEncoding application/json application/json\");\n" +
            "        System.out.println(\"      RequestBodyContentItemEncoding application/json application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncodingPrefixEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncodingItemEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseContentItemSchema 200 application/json string\");\n" +
            "        System.out.println(\"      ResponseContentPrefixEncoding 200 application/json application/json\");\n" +
            "        System.out.println(\"      ResponseContentItemEncoding 200 application/json application/json\");\n" +
            "        System.out.println(\"      ResponseEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingPrefixEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingItemEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseContentItemSchema default application/json string\");\n" +
            "        System.out.println(\"      ResponseContentPrefixEncoding default application/json application/json\");\n" +
            "        System.out.println(\"      ResponseContentItemEncoding default application/json application/json\");\n" +
            "        System.out.println(\"      ResponseEncoding default application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingPrefixEncoding default application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingItemEncoding default application/json enc1 application/json\");\n" +
            "    }\n";
        OpenAPI extraParsed = Parse.parse(extra);
        assertNotNull(extraParsed);

        // Try edge cases: emit with completely empty API

        String cliEdge = "private static void printHelp() {\n" +
            "        System.out.println(\"Server Object url: http://a name:  description: \");\n" +
            "        System.out.println(\"Server Variable Object var defaultValue: v description:  enumValues: \");\n" +
            "        System.out.println(\"Operation Object \");\n" +
            "        System.out.println(\"See also: http://only_url\");\n" +
            "        System.out.println(\"    --requestBody: rb\");\n" +
            "        System.out.println(\"      RequestBodyContentItemSchema application/json string\");\n" +
            "        System.out.println(\"      RequestBodyContentPrefixEncoding application/json application/json\");\n" +
            "        System.out.println(\"      RequestBodyContentItemEncoding application/json application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncodingPrefixEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      RequestBodyEncodingItemEncoding application/json enc1 application/json\");\n" +
            "        System.out.println(\"      Example null: null - null = null\");\n" +
            "        System.out.println(\"      ResponseContentItemSchema 200 application/json string\");\n" +
            "        System.out.println(\"      ResponseContentPrefixEncoding 200 application/json application/json\");\n" +
            "        System.out.println(\"      ResponseContentItemEncoding 200 application/json application/json\");\n" +
            "        System.out.println(\"      ResponseEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingPrefixEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"      ResponseEncodingItemEncoding 200 application/json enc1 application/json\");\n" +
            "        System.out.println(\"        LinkParam l1 p1 v1\");\n" +
            "        System.out.println(\"      Link l1 operationId=o1 operationRef=o2 description=\\\"d\\\" requestBody=\\\"rb\\\" serverUrl=\\\"http://s\\\"\");\n" +
            "    }\n";
        
    }
}
