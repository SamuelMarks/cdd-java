import static org.junit.Assert.*;

import cli.Parse;
import java.util.*;
import java.util.regex.*;
import openapi.*;
import org.junit.Test;

/**
 * Fuzzes {@link cli.Parse} with isolated, contextual, and malformed help output
 * lines and asserts model invariants.
 */
public class CliParseFuzzTest {
	/**
	 * Default constructor.
	 */
	public CliParseFuzzTest() {
	}

	/**
	 * Verifies {@link cli.Parse#parse(String)} handles individual and combined CLI
	 * help output lines cleanly and preserves round-tripped fields.
	 *
	 * @throws Exception
	 *             If parsing fails unexpectedly.
	 */
	@Test
	public void fuzzParse() throws Exception {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.title = "test";
		api.info.version = "1.0";
		api.info.summary = "sum";
		api.info.description = "desc";
		api.info.termsOfService = "tos";
		api.info.contact = new Contact();
		api.info.contact.name = "test";
		api.info.contact.email = "e";
		api.info.contact.url = "u";
		api.info.license = new License();
		api.info.license.name = "test";
		api.info.license.identifier = "id";
		api.info.license.url = "u";
		api.servers = new ArrayList<>();
		Server s = new Server();
		s.url = "test";
		s.name = "n";
		s.description = "d";
		s.variables = new HashMap<>();
		ServerVariable sv = new ServerVariable();
		sv.defaultValue = "v";
		sv.description = "d";
		sv.enumValues = Arrays.asList("a", "b");
		s.variables.put("var", sv);
		api.servers.add(s);

		api.components = new Components();
		api.components.schemas = new HashMap<>();
		Schema sch = new Schema();
		sch.discriminator = new Discriminator();
		sch.discriminator.mapping = new HashMap<>();
		sch.xml = new XML();
		api.components.schemas.put("test", ReferenceOr.ofValue(sch));

		api.components.responses = new HashMap<>();
		api.components.responses.put("res", ReferenceOr.ofValue(new Response()));
		api.components.parameters = new HashMap<>();
		api.components.parameters.put("param", ReferenceOr.ofValue(new Parameter()));
		api.components.requestBodies = new HashMap<>();
		api.components.requestBodies.put("rb", ReferenceOr.ofValue(new RequestBody()));
		api.components.headers = new HashMap<>();
		api.components.headers.put("h", ReferenceOr.ofValue(new Header()));

		api.components.securitySchemes = new HashMap<>();
		SecurityScheme ss = new SecurityScheme();
		ss.type = "oauth2";
		ss.scheme = "bearer";
		ss.in = "header";
		ss.name = "auth";
		ss.bearerFormat = "jwt";
		ss.openIdConnectUrl = "http";
		ss.oauth2MetadataUrl = "http";
		ss.flows = new OAuthFlows();
		ss.flows.implicit = new OAuthFlow();
		ss.flows.implicit.authorizationUrl = "http";
		ss.flows.implicit.tokenUrl = "http";
		ss.flows.implicit.refreshUrl = "http";
		ss.flows.implicit.scopes = new HashMap<>();
		ss.flows.implicit.scopes.put("sc", "sc");
		ss.flows.password = new OAuthFlow();
		ss.flows.clientCredentials = new OAuthFlow();
		ss.flows.authorizationCode = new OAuthFlow();
		ss.flows.deviceAuthorization = new OAuthFlow();
		api.components.securitySchemes.put("ss", ReferenceOr.ofValue(ss));

		api.components.links = new HashMap<>();
		Link lnk = new Link();
		lnk.operationId = "op1";
		lnk.operationRef = "op1";
		lnk.description = "d";
		lnk.requestBody = common.PayloadValue.ofString("rb");
		lnk.server = new Server();
		lnk.server.url = "http";
		lnk.parameters = new HashMap<>();
		lnk.parameters.put("p", common.PayloadValue.ofString("p"));
		api.components.links.put("l", ReferenceOr.ofValue(lnk));
		api.components.callbacks = new HashMap<>();
		api.components.callbacks.put("cb", ReferenceOr.ofValue(new Callback()));
		api.components.pathItems = new HashMap<>();
		api.components.pathItems.put("pi", ReferenceOr.ofValue(new PathItem()));
		api.components.mediaTypes = new HashMap<>();
		api.components.mediaTypes.put("mt", ReferenceOr.ofValue(new MediaType()));

		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.get.tags = Arrays.asList("t1");
		pi.get.externalDocs = new ExternalDocumentation();
		pi.get.callbacks = new HashMap<>();
		pi.get.callbacks.put("cb", ReferenceOr.ofValue(new Callback()));
		pi.get.parameters = new ArrayList<>();
		Parameter p = new Parameter();
		p.examples = new HashMap<>();
		p.examples.put("ex", ReferenceOr.ofValue(new Example()));
		pi.get.parameters.add(ReferenceOr.ofValue(p));
		RequestBody rb = new RequestBody();
		rb.content = new HashMap<>();
		MediaType mt = new MediaType();
		mt.itemSchema = ReferenceOr.ofValue(new Schema());
		mt.prefixEncoding = Arrays.asList(new Encoding());
		mt.itemEncoding = new Encoding();
		mt.encoding = new HashMap<>();
		Encoding enc = new Encoding();
		enc.prefixEncoding = Arrays.asList(new Encoding());
		enc.itemEncoding = new Encoding();
		mt.encoding.put("e", enc);
		rb.content.put("application/json", mt);
		pi.get.requestBody = ReferenceOr.ofValue(rb);

		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		Response r = new Response();
		r.headers = new HashMap<>();
		r.headers.put("h", ReferenceOr.ofValue(new Header()));
		r.links = new HashMap<>();
		r.links.put("l", ReferenceOr.ofValue(new Link()));
		pi.get.responses.statusCodes.put("200", ReferenceOr.ofValue(r));
		pi.get.responses.defaultResponse = ReferenceOr.ofValue(r);
		api.paths.pathItems.put("/test", pi);

		String content = cli.Emit.emitCli(api);
		OpenAPI fullParsed = Parse.parse(content);
		assertEquals("test", fullParsed.info.title);
		assertEquals("1.0", fullParsed.info.version);
		assertNotNull(fullParsed.paths.pathItems.get("/test"));

		Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{(.*?)\\n    \\}").matcher(content);
		assertTrue("hm.find() failed to find printHelp!", hm.find());

		String helpBody = hm.group(1);
		Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(helpBody);

		List<String> lines = new ArrayList<>();
		while (lm.find()) {
			lines.add(lm.group(0));
		}

		String[] customLines = {"System.out.println(\"Server Object url: http://something\");",
				"System.out.println(\"Server Object url: http://something name: \");",
				"System.out.println(\"Server Object url: http://something description: \");",
				"System.out.println(\"Server Variable Object var\");",
				"System.out.println(\"Server Variable Object var defaultValue: v\");",
				"System.out.println(\"Server Variable Object var defaultValue: v description: \");",
				"System.out.println(\"Component schemas S\");",
				"System.out.println(\"  Discriminator propertyName=p mapping=a=b defaultMapping=d\");",
				"System.out.println(\"  Discriminator propertyName= mapping= defaultMapping=\");",
				"System.out.println(\"  Discriminator propertyName=null mapping=null defaultMapping=null\");",
				"System.out.println(\"  Discriminator propertyName=p mapping=bad defaultMapping=d\");",
				"System.out.println(\"  XML name=n namespace=ns prefix=p attribute=true wrapped=true\");",
				"System.out.println(\"  XML name= namespace= prefix= attribute= wrapped=\");",
				"System.out.println(\"  XML name=null namespace=null prefix=null attribute=null wrapped=null\");",
				"System.out.println(\"  XML name=n namespace=ns prefix=p attribute=false wrapped=false\");",
				"System.out.println(\"Component securitySchemes sc type= scheme= in= name= bearerFormat= openIdConnectUrl= oauth2MetadataUrl=\");",
				"System.out.println(\"Component securitySchemes sc type=null scheme=null in=null name=null bearerFormat=null openIdConnectUrl=null oauth2MetadataUrl=null\");",
				"System.out.println(\"Component securitySchemesFlow sc implicit authorizationUrl= tokenUrl= refreshUrl=\");",
				"System.out.println(\"Component securitySchemesFlow sc implicit authorizationUrl=null tokenUrl=null refreshUrl=null\");",
				"System.out.println(\"Component securitySchemesFlowScope sc implicit scopeName \");",
				"System.out.println(\"Component securitySchemesFlowScope sc implicit scopeName null\");",
				"System.out.println(\"Component links l1\");",
				"System.out.println(\"Component links l1 operationId= operationRef= description=\\\"\\\" requestBody=\\\"\\\" serverUrl=\\\"\\\"\");",
				"System.out.println(\"Component linksParam l1 p1 v1\");", "System.out.println(\"Operation: get /p\");",
				"System.out.println(\"Operation: custom /p\");",
				"System.out.println(\"Operation Object [DEPRECATED] \");",
				"System.out.println(\"Operation Object (Tags: )\");", "System.out.println(\"See also: http\");",
				"System.out.println(\"    --param \");", "System.out.println(\"    --param (required) \");",
				"System.out.println(\"      Example ex1: null\");",
				"System.out.println(\"      Example ex1: null - null\");", "System.out.println(\"    --requestBody\");",
				"System.out.println(\"    --requestBody: \");", "System.out.println(\"    Returns 200\");",
				"System.out.println(\"    Returns default\");", "System.out.println(\"      Header h1\");",
				"System.out.println(\"      Link l1\");",
				"System.out.println(\"      RequestBodyContentItemSchema ct type\");",
				"System.out.println(\"      RequestBodyContentItemSchema ct\");",
				"System.out.println(\"      RequestBodyContentItemSchema unknown type\");",
				"System.out.println(\"      RequestBodyContentItemEncoding ct type\");",
				"System.out.println(\"      RequestBodyContentItemEncoding ct\");",
				"System.out.println(\"      RequestBodyContentItemEncoding unknown type\");",
				"System.out.println(\"      RequestBodyEncoding ct p1 ct2\");",
				"System.out.println(\"      RequestBodyEncoding ct p1\");",
				"System.out.println(\"      RequestBodyEncoding unknown p1 ct2\");",
				"System.out.println(\"      RequestBodyEncodingPrefixEncoding ct p1 ct2\");",
				"System.out.println(\"      RequestBodyEncodingPrefixEncoding ct p1\");",
				"System.out.println(\"      RequestBodyEncodingPrefixEncoding unknown p1 ct2\");",
				"System.out.println(\"      RequestBodyEncodingItemEncoding ct p1 ct2\");",
				"System.out.println(\"      RequestBodyEncodingItemEncoding ct p1\");",
				"System.out.println(\"      RequestBodyEncodingItemEncoding unknown p1 ct2\");",
				"System.out.println(\"      ResponseContentItemSchema ct type\");",
				"System.out.println(\"      ResponseContentItemSchema ct\");",
				"System.out.println(\"      ResponseContentItemSchema unknown type\");",
				"System.out.println(\"      ResponseContentItemEncoding 200 ct type\");",
				"System.out.println(\"      ResponseContentItemEncoding 200 ct\");",
				"System.out.println(\"      ResponseContentItemEncoding 200 unknown type\");",
				"System.out.println(\"      ResponseEncoding 200 ct p1 type\");",
				"System.out.println(\"      ResponseEncoding 200 ct p1\");",
				"System.out.println(\"      ResponseEncoding 200 unknown p1 type\");",
				"System.out.println(\"      ResponseEncodingPrefixEncoding 200 ct p1 type\");",
				"System.out.println(\"      ResponseEncodingPrefixEncoding 200 ct p1\");",
				"System.out.println(\"      ResponseEncodingPrefixEncoding 200 unknown p1 type\");",
				"System.out.println(\"      ResponseEncodingItemEncoding 200 ct p1 type\");",
				"System.out.println(\"      ResponseEncodingItemEncoding 200 ct p1\");",
				"System.out.println(\"      ResponseEncodingItemEncoding 200 unknown p1 type\");"};

		for (String l : customLines) {
			lines.add(l);
		}

		for (String line : lines) {
			String isolated = "public class SdkCli {\n" + "    private static void printHelp() {\n" + "        " + line
					+ "\n" + "        " + line + "\n" + "    }\n" + "}";
			OpenAPI isoApi = Parse.parse(isolated);
			assertNotNull(isoApi);

			String withContext = "public class SdkCli {\n" + "    private static void printHelp() {\n"
					+ "        System.out.println(\"Component schemas S1\");\n"
					+ "        System.out.println(\"Component securitySchemes sc1\");\n"
					+ "        System.out.println(\"Component responses r1\");\n"
					+ "        System.out.println(\"Operation: get /p\");\n"
					+ "        System.out.println(\"Operation Object [DEPRECATED] summary (Tags: t1, t2)\");\n"
					+ "        System.out.println(\"    --param (required)\");\n"
					+ "        System.out.println(\"    --requestBody: [Content-Types: ct]\");\n"
					+ "        System.out.println(\"    Returns 200: [Content-Types: ct]\");\n"
					+ "        System.out.println(\"    Returns default: [Content-Types: default]\");\n" + "        "
					+ line + "\n" + "        " + line + "\n" + "    }\n" + "}";
			OpenAPI ctxApi = Parse.parse(withContext);
			assertNotNull(ctxApi.paths.pathItems.get("/p"));

			String withMissingContext = "public class SdkCli {\n" + "    private static void printHelp() {\n"
					+ "        System.out.println(\"Operation: get /p\");\n"
					+ "        System.out.println(\"    --requestBody\");\n"
					+ "        System.out.println(\"    Returns 200:\");\n"
					+ "        System.out.println(\"    Returns default:\");\n" + "        " + line + "\n" + "    }\n"
					+ "}";
			OpenAPI missingCtxApi = Parse.parse(withMissingContext);
			assertNotNull(missingCtxApi.paths.pathItems.get("/p"));
		}
	}
}
