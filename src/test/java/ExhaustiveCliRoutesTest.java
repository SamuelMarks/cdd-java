import org.junit.Test;
import static org.junit.Assert.*;
import openapi.*;
import java.util.*;
import java.io.*;

/**
 * Exhaustive unit tests for CLI and Routes parse/emit edge cases and CLI entry
 * points.
 */
public class ExhaustiveCliRoutesTest {

	/**
	 * Verifies {@link cli.Emit#emitCli} with empty OpenAPI component and operation
	 * objects.
	 */
	@Test
	public void testCliEmitEmptyObjects() {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.servers = new ArrayList<>();
		api.servers.add(new Server());
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.schemas.put("EmptySchema", ReferenceOr.ofValue(new Schema()));
		api.components.responses = new HashMap<>();
		api.components.responses.put("EmptyResponse", ReferenceOr.ofValue(new openapi.Response()));
		api.components.parameters = new HashMap<>();
		api.components.parameters.put("EmptyParam", ReferenceOr.ofValue(new openapi.Parameter()));
		api.components.requestBodies = new HashMap<>();
		api.components.requestBodies.put("EmptyRequestBody", ReferenceOr.ofValue(new RequestBody()));
		api.components.headers = new HashMap<>();
		api.components.headers.put("EmptyHeader", ReferenceOr.ofValue(new Header()));
		api.components.securitySchemes = new HashMap<>();
		api.components.securitySchemes.put("EmptySecurityScheme", ReferenceOr.ofValue(new SecurityScheme()));
		api.components.links = new HashMap<>();
		api.components.links.put("EmptyLink", ReferenceOr.ofValue(new Link()));
		api.components.callbacks = new HashMap<>();
		api.components.callbacks.put("EmptyCallback", ReferenceOr.ofValue(new Callback()));
		api.components.pathItems = new HashMap<>();
		api.components.pathItems.put("EmptyPathItem", ReferenceOr.ofValue(new PathItem()));

		api.paths = new openapi.Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.get.parameters = new ArrayList<>();
		pi.get.parameters.add(ReferenceOr.ofValue(new openapi.Parameter()));
		pi.get.requestBody = ReferenceOr.ofValue(new RequestBody());
		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		pi.get.responses.statusCodes.put("200", ReferenceOr.ofValue(new openapi.Response()));
		pi.get.responses.defaultResponse = ReferenceOr.ofValue(new openapi.Response());
		pi.get.callbacks = new HashMap<>();
		pi.get.callbacks.put("EmptyCallback", ReferenceOr.ofValue(new Callback()));
		api.paths.pathItems.put("/empty", pi);

		String out = cli.Emit.emitCli(api);
		assertNotNull(com.github.javaparser.StaticJavaParser.parse(out));
		assertTrue(out.contains("Component schemas EmptySchema"));
		assertTrue(out.contains("Component responses EmptyResponse"));
		assertTrue(out.contains("Component parameters EmptyParam"));
		assertTrue(out.contains("Component requestBodies EmptyRequestBody"));
		assertTrue(out.contains("Component headers EmptyHeader"));
		assertTrue(out.contains("Component securitySchemes EmptySecurityScheme"));
		assertTrue(out.contains("Component links EmptyLink"));
		assertTrue(out.contains("Component callbacks EmptyCallback"));
		assertTrue(out.contains("Component pathItems EmptyPathItem"));
		assertTrue(out.contains("Operation: get /empty"));
	}

	/**
	 * Verifies {@link cli.Parse#parse} when encoding keys must be initialized on
	 * the fly.
	 */
	@Test
	public void testCliParseMissingKeys() {
		String cliOutput = "private static void printHelp() {\n"
				+ "        System.out.println(\"Operation: get /p1\");\n"
				+ "        System.out.println(\"Operation Object \");\n"
				+ "        System.out.println(\"    --requestBody: rb [Content-Types: application/json]\");\n"
				+ "        System.out.println(\"      RequestBodyEncoding application/json enc1 text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingPrefixEncoding application/json enc2 text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingItemEncoding application/json enc3 text/plain\");\n"
				+ "        System.out.println(\"    Returns 200: rdesc [Content-Types: application/json]\");\n"
				+ "        System.out.println(\"      ResponseEncoding 200 application/json enc4 text/plain\");\n"
				+ "        System.out.println(\"      ResponseEncodingPrefixEncoding 200 application/json enc5 text/plain\");\n"
				+ "        System.out.println(\"      ResponseEncodingItemEncoding 200 application/json enc6 text/plain\");\n"
				+ "        System.out.println(\"    Returns default: rdesc [Content-Types: application/xml]\");\n"
				+ "        System.out.println(\"      ResponseEncoding default application/xml enc7 text/plain\");\n"
				+ "        System.out.println(\"      ResponseEncodingPrefixEncoding default application/xml enc8 text/plain\");\n"
				+ "        System.out.println(\"      ResponseEncodingItemEncoding default application/xml enc9 text/plain\");\n"
				+ "    }\n";
		OpenAPI api = cli.Parse.parse(cliOutput);
		Operation op = api.paths.pathItems.get("/p1").get;
		MediaType rbMt = op.requestBody.value.content.get("application/json");
		assertEquals("text/plain", rbMt.encoding.get("enc1").contentType);
		assertEquals("text/plain", rbMt.encoding.get("enc2").prefixEncoding.get(0).contentType);
		assertEquals("text/plain", rbMt.encoding.get("enc3").itemEncoding.contentType);

		MediaType r200Mt = op.responses.statusCodes.get("200").value.content.get("application/json");
		assertEquals("text/plain", r200Mt.encoding.get("enc4").contentType);
		assertEquals("text/plain", r200Mt.encoding.get("enc5").prefixEncoding.get(0).contentType);
		assertEquals("text/plain", r200Mt.encoding.get("enc6").itemEncoding.contentType);

		MediaType rDefMt = op.responses.defaultResponse.value.content.get("application/xml");
		assertEquals("text/plain", rDefMt.encoding.get("enc7").contentType);
		assertEquals("text/plain", rDefMt.encoding.get("enc8").prefixEncoding.get(0).contentType);
		assertEquals("text/plain", rDefMt.encoding.get("enc9").itemEncoding.contentType);
	}

	/**
	 * Verifies helper methods and CLI flags on {@link cli.CddCli},
	 * {@link cli.McpJsonRpcServer}, {@link cli.SyncCommand},
	 * {@link cli.ProjectScaffolder}, and {@link cli.Main}.
	 *
	 * @throws Exception
	 *             If invocation fails.
	 */
	@Test
	public void testCliMainReflection() throws Exception {
		java.util.function.Consumer<Integer> prevExit = cli.Main.exitHandler;
		final List<Integer> exitCodes = new ArrayList<>();
		cli.Main.exitHandler = exitCodes::add;
		try {
			cli.CddCli.processInMemory("{");
			cli.CddCli.processInMemory("{\"command\": \"to_openapi\"}");
			InputStream oldIn = System.in;
			System.setIn(new ByteArrayInputStream("".getBytes()));
			try {
				cli.McpJsonRpcServer.serveJsonRpcStdio();
			} finally {
				System.setIn(oldIn);
			}
			cli.Main.main(new String[]{});
			cli.Main.main(new String[]{"-v"});
			cli.Main.main(new String[]{"--version"});
			cli.Main.main(new String[]{"process_in_memory"});
			cli.Main.main(new String[]{"invalid_cmd"});
			assertEquals(Arrays.asList(1, 1), exitCodes);
		} finally {
			cli.Main.exitHandler = prevExit;
		}
		assertTrue(cli.CddCli.hasFlag(new String[]{"-a"}, "-a", "--all", null));
		assertTrue(cli.CddCli.hasFlag(new String[]{"--all"}, "-a", "--all", null));
		assertFalse(cli.CddCli.hasFlag(new String[]{"-b"}, "-a", "--all", null));
		assertEquals("val", cli.CddCli.getArg(new String[]{"-a", "val"}, "-a", "--all", ""));
		assertNull(cli.CddCli.getArg(new String[]{"-a"}, "-a", "--all", ""));
		assertNull(cli.CddCli.getArg(new String[]{"-b", "val"}, "-a", "--all", ""));
		assertNotNull(cli.SyncCommand.extractOpenAPI(new java.io.File("non_existent_file.json")));
		assertEquals(new java.io.File("/absolute/path"),
				cli.ProjectScaffolder.resolveWasiPath("/absolute/path", cli.CddCli.envProvider));
	}

	/**
	 * Verifies {@link routes.Parse#parse} and {@link routes.Emit#emit} on response
	 * headers, content, and links.
	 */
	@Test
	public void testRoutesParseEmit() {
		String code = "class Routes {\n" + "  /**\n" + "   * @responseHeader 404 x-err desc\n"
				+ "   * @responseContent 404 application/json ErrSchema\n" + "   * @responseLink 404 myLink desc\n"
				+ "   * @responseHeader default x-def desc\n" + "   * @responseContent default text/plain DefSchema\n"
				+ "   * @responseLink default myDefLink desc\n" + "   */\n"
				+ "  @Custom(method = \"OPTIONS\", path = \"/opts\")\n" + "  public void opts() {}\n" + "}\n";
		OpenAPI api = routes.Parse.parse(code);
		assertEquals("3.2.0", api.openapi);
		assertNotNull(api.paths);

		String em = routes.Emit.emit(api, code);
		assertNotNull(com.github.javaparser.StaticJavaParser.parse(em));
		assertTrue(em.contains("@responseHeader 404 x-err desc"));
		assertTrue(em.contains("@responseContent default text/plain DefSchema"));
	}

	/**
	 * Verifies exhaustive round-trip and edge-case branches of
	 * {@link routes.RouteJavadocEmitter} and {@link routes.Parse}.
	 */
	@Test
	public void testExhaustiveRouteJavadocEmitterRoundTrip() {
		OpenAPI full = new OpenAPI();
		full.openapi = "3.2.0";
		full.$self = "https://example.com/openapi.json";
		full.jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base";
		full.info = new Info();
		full.info.title = "RichApi";
		full.info.version = "2.0.0";
		full.info.summary = "Rich Summary";
		full.info.description = "Rich Description";
		full.info.termsOfService = "https://example.com/tos";
		full.info.contact = new Contact();
		full.info.contact.name = "Dev";
		full.info.contact.email = "dev@example.com";
		full.info.contact.url = "https://example.com/contact";
		full.info.license = new License();
		full.info.license.name = "Apache-2.0";
		full.info.license.identifier = "Apache-2.0";
		full.info.license.url = "https://example.com/license";

		Tag tFull = new Tag();
		tFull.name = "users";
		tFull.summary = "User ops";
		tFull.description = "User desc";
		tFull.externalDocs = new ExternalDocumentation();
		tFull.externalDocs.url = "https://example.com/docs";
		tFull.parent = "root";
		tFull.kind = "nav";
		Tag tEmpty = new Tag();
		Tag tNoExtUrl = new Tag();
		tNoExtUrl.name = "noext";
		tNoExtUrl.externalDocs = new ExternalDocumentation();
		full.tags = Arrays.asList(tFull, tEmpty, tNoExtUrl);

		Server sFull = new Server();
		sFull.url = "https://api.example.com/{env}";
		sFull.description = "Prod server";
		sFull.name = "prod";
		ServerVariable svFull = new ServerVariable();
		svFull.defaultValue = "v1";
		svFull.description = "Env var";
		svFull.enumValues = Arrays.asList("v1", "v2");
		ServerVariable svSparse = new ServerVariable();
		svSparse.enumValues = Collections.emptyList();
		sFull.variables = new LinkedHashMap<>();
		sFull.variables.put("env", svFull);
		sFull.variables.put("sparse", svSparse);

		Server sUnnamedWithVars = new Server();
		sUnnamedWithVars.url = "https://unnamed.example.com";
		sUnnamedWithVars.name = "namedNoVars";
		Server sUnnamed = new Server();
		sUnnamed.url = "https://noname.example.com";
		Server sNoUrl = new Server();
		full.servers = Arrays.asList(sFull, sUnnamedWithVars, sUnnamed, sNoUrl);

		full.components = new Components();
		full.components.securitySchemes = new LinkedHashMap<>();
		SecurityScheme scFull = new SecurityScheme();
		scFull.type = "oauth2";
		scFull.scheme = "bearer";
		scFull.in = "header";
		scFull.name = "Authorization";
		scFull.bearerFormat = "JWT";
		scFull.openIdConnectUrl = "https://example.com/.well-known/openid";
		scFull.flows = new OAuthFlows();
		OAuthFlow fImp = new OAuthFlow();
		fImp.authorizationUrl = "https://example.com/auth";
		fImp.refreshUrl = "https://example.com/refresh";
		fImp.scopes = new LinkedHashMap<>();
		fImp.scopes.put("read", "Read access");
		fImp.scopes.put("empty", "");
		scFull.flows.implicit = fImp;
		OAuthFlow fPass = new OAuthFlow();
		fPass.tokenUrl = "https://example.com/token";
		fPass.refreshUrl = "https://example.com/refresh";
		scFull.flows.password = fPass;
		OAuthFlow fCc = new OAuthFlow();
		scFull.flows.clientCredentials = fCc;
		OAuthFlow fAc = new OAuthFlow();
		fAc.authorizationUrl = "https://example.com/auth";
		fAc.tokenUrl = "https://example.com/token";
		scFull.flows.authorizationCode = fAc;
		OAuthFlow fAcSparse = new OAuthFlow();
		OAuthFlow fDev = new OAuthFlow();
		fDev.deviceAuthorizationUrl = "https://example.com/device";
		fDev.tokenUrl = "https://example.com/token";
		scFull.flows.deviceAuthorization = fDev;
		SecurityScheme scSparseFlows = new SecurityScheme();
		scSparseFlows.flows = new OAuthFlows();
		scSparseFlows.flows.implicit = new OAuthFlow();
		scSparseFlows.flows.authorizationCode = fAcSparse;
		scSparseFlows.flows.deviceAuthorization = new OAuthFlow();
		full.components.securitySchemes.put("oauth", ReferenceOr.ofValue(scFull));
		full.components.securitySchemes.put("sparse", ReferenceOr.ofValue(scSparseFlows));
		full.components.securitySchemes.put("refSec", ReferenceOr.ofRef("#/components/securitySchemes/oauth"));

		full.components.responses = new LinkedHashMap<>();
		Response cResp = new Response();
		cResp.description = "Resp desc";
		full.components.responses.put("R1", ReferenceOr.ofValue(cResp));
		full.components.responses.put("R2", ReferenceOr.ofValue(new Response()));
		full.components.responses.put("RRef", ReferenceOr.ofRef("#/components/responses/R1"));

		full.components.parameters = new LinkedHashMap<>();
		Parameter cParam = new Parameter();
		cParam.name = "cp";
		cParam.in = "query";
		cParam.description = "Param desc";
		full.components.parameters.put("P1", ReferenceOr.ofValue(cParam));
		full.components.parameters.put("P2", ReferenceOr.ofValue(new Parameter()));
		full.components.parameters.put("PRef", ReferenceOr.ofRef("#/components/parameters/P1"));

		full.components.requestBodies = new LinkedHashMap<>();
		RequestBody cRb = new RequestBody();
		cRb.description = "RB desc";
		full.components.requestBodies.put("RB1", ReferenceOr.ofValue(cRb));
		full.components.requestBodies.put("RB2", ReferenceOr.ofValue(new RequestBody()));
		full.components.requestBodies.put("RBRef", ReferenceOr.ofRef("#/components/requestBodies/RB1"));

		full.components.headers = new LinkedHashMap<>();
		Header cHdr = new Header();
		cHdr.description = "Hdr desc";
		full.components.headers.put("H1", ReferenceOr.ofValue(cHdr));
		full.components.headers.put("H2", ReferenceOr.ofValue(new Header()));
		full.components.headers.put("HRef", ReferenceOr.ofRef("#/components/headers/H1"));

		full.components.links = new LinkedHashMap<>();
		Link cLink = new Link();
		cLink.operationId = "op1";
		cLink.operationRef = "#/paths/~1items/get";
		cLink.description = "Link desc";
		cLink.server = new Server();
		cLink.server.url = "https://link.example.com";
		cLink.requestBody = common.PayloadValue.ofString("$request.body");
		cLink.parameters = Collections.singletonMap("id", common.PayloadValue.ofString("$response.body#/id"));
		Link cLinkServerNoUrl = new Link();
		cLinkServerNoUrl.server = new Server();
		full.components.links.put("L1", ReferenceOr.ofValue(cLink));
		full.components.links.put("L2", ReferenceOr.ofValue(cLinkServerNoUrl));
		full.components.links.put("L3", ReferenceOr.ofValue(new Link()));
		full.components.links.put("LRef", ReferenceOr.ofRef("#/components/links/L1"));

		full.components.callbacks = new LinkedHashMap<>();
		Callback cCb = new Callback();
		PathItem cCbPi = new PathItem();
		cCbPi.post = new Operation();
		cCb.pathItems = Collections.singletonMap("{$request.query.cb}", cCbPi);
		Callback cCbNull = new Callback();
		cCbNull.pathItems = null;
		full.components.callbacks.put("CB1", ReferenceOr.ofValue(cCb));
		full.components.callbacks.put("CB2", ReferenceOr.ofValue(cCbNull));
		full.components.callbacks.put("CBRef", ReferenceOr.ofRef("#/components/callbacks/CB1"));

		full.components.pathItems = new LinkedHashMap<>();
		PathItem cPi = new PathItem();
		cPi.description = "PI desc";
		full.components.pathItems.put("PI1", ReferenceOr.ofValue(cPi));
		full.components.pathItems.put("PI2", ReferenceOr.ofValue(new PathItem()));
		full.components.pathItems.put("PIRef", ReferenceOr.ofRef("#/components/pathItems/PI1"));

		full.components.mediaTypes = new LinkedHashMap<>();
		MediaType cMt = new MediaType();
		Schema cMtSchema = new Schema();
		cMtSchema.type = SchemaType.ofSingle("object");
		cMt.schema = ReferenceOr.ofValue(cMtSchema);
		MediaType cMtNoType = new MediaType();
		cMtNoType.schema = ReferenceOr.ofValue(new Schema());
		MediaType cMtRefSchema = new MediaType();
		cMtRefSchema.schema = ReferenceOr.ofRef("#/components/schemas/S");
		full.components.mediaTypes.put("MT1", ReferenceOr.ofValue(cMt));
		full.components.mediaTypes.put("MT2", ReferenceOr.ofValue(new MediaType()));
		full.components.mediaTypes.put("MT3", ReferenceOr.ofValue(cMtNoType));
		full.components.mediaTypes.put("MT4", ReferenceOr.ofValue(cMtRefSchema));
		full.components.mediaTypes.put("MTRef", ReferenceOr.ofRef("#/components/mediaTypes/MT1"));

		full.paths = new Paths();
		full.paths.pathItems = new LinkedHashMap<>();
		PathItem pi = new PathItem();
		Server ps1 = new Server();
		ps1.url = "https://path.example.com";
		ps1.description = "Path server";
		Server ps2 = new Server();
		ps2.url = "https://path2.example.com";
		Server psNoUrl = new Server();
		pi.servers = Arrays.asList(ps1, ps2, psNoUrl);

		Parameter pp1 = new Parameter();
		pp1.name = "sharedId";
		pp1.in = "query";
		pp1.description = "Shared desc";
		Parameter pp2 = new Parameter();
		pi.parameters = Arrays.asList(ReferenceOr.ofValue(pp1), ReferenceOr.ofValue(pp2));

		Operation op = new Operation();
		op.operationId = "doRich";
		op.summary = "Do rich";
		op.description = "Detailed description";
		op.tags = Arrays.asList("users", "");
		op.externalDocs = new ExternalDocumentation();
		op.externalDocs.url = "https://example.com/opdocs";
		op.externalDocs.description = "Op docs";
		op.deprecated = true;
		op.servers = Arrays.asList(ps1, ps2, psNoUrl);

		SecurityRequirement sr1 = new SecurityRequirement();
		sr1.requirements = new LinkedHashMap<>();
		sr1.requirements.put("oauth", Arrays.asList("read", "write"));
		sr1.requirements.put("apiKey", Collections.emptyList());
		sr1.requirements.put("nullScopes", null);
		op.security = Collections.singletonList(sr1);

		Parameter opParamFull = new Parameter();
		opParamFull.name = "qParam";
		opParamFull.in = "query";
		opParamFull.description = "Query param";
		opParamFull.required = true;
		opParamFull.deprecated = true;
		opParamFull.allowEmptyValue = true;
		opParamFull.style = "form";
		opParamFull.explode = true;
		opParamFull.allowReserved = true;
		Schema pSch = new Schema();
		pSch.type = SchemaType.ofSingle("string");
		opParamFull.schema = ReferenceOr.ofValue(pSch);
		opParamFull.content = Collections.singletonMap("application/json", new MediaType());
		opParamFull.example = common.PayloadValue.ofString("ex1");
		Example pEx1 = new Example();
		pEx1.summary = "Sum";
		pEx1.description = "Desc";
		pEx1.value = common.PayloadValue.ofString("val");
		Example pEx2 = new Example();
		opParamFull.examples = new LinkedHashMap<>();
		opParamFull.examples.put("e1", ReferenceOr.ofValue(pEx1));
		opParamFull.examples.put("e2", ReferenceOr.ofValue(pEx2));
		opParamFull.examples.put("eRef", ReferenceOr.ofRef("#/components/examples/E1"));

		Parameter opParamNoSchemaType = new Parameter();
		opParamNoSchemaType.name = "pNoType";
		opParamNoSchemaType.in = "query";
		opParamNoSchemaType.schema = ReferenceOr.ofValue(new Schema());

		Parameter opParamRefSchema = new Parameter();
		opParamRefSchema.name = "pRefSch";
		opParamRefSchema.in = "query";
		opParamRefSchema.schema = ReferenceOr.ofRef("#/components/schemas/S");

		Parameter opParamSymbolsOnly = new Parameter();
		opParamSymbolsOnly.name = "---";
		opParamSymbolsOnly.in = "path";

		op.parameters = Arrays.asList(ReferenceOr.ofValue(opParamFull), ReferenceOr.ofValue(opParamNoSchemaType),
				ReferenceOr.ofValue(opParamRefSchema), ReferenceOr.ofValue(opParamSymbolsOnly),
				ReferenceOr.ofValue(new Parameter()));

		RequestBody opRb = new RequestBody();
		opRb.description = "Body payload";
		opRb.required = true;
		op.requestBody = ReferenceOr.ofValue(opRb);

		op.responses = new Responses();
		op.responses.statusCodes = new LinkedHashMap<>();
		Response r200 = new Response();
		r200.description = "OK resp";
		r200.headers = new LinkedHashMap<>();
		Header hFull = new Header();
		hFull.description = "Rate limit";
		hFull.required = true;
		hFull.deprecated = true;
		hFull.style = "simple";
		hFull.explode = true;
		hFull.schema = ReferenceOr.ofValue(pSch);
		hFull.example = common.PayloadValue.ofString("100");
		hFull.content = Collections.singletonMap("text/plain", new MediaType());
		hFull.examples = opParamFull.examples;
		Header hNoSchType = new Header();
		hNoSchType.schema = ReferenceOr.ofValue(new Schema());
		Header hRefSch = new Header();
		hRefSch.schema = ReferenceOr.ofRef("#/components/schemas/S");
		r200.headers.put("X-Rate", ReferenceOr.ofValue(hFull));
		r200.headers.put("X-NoType", ReferenceOr.ofValue(hNoSchType));
		r200.headers.put("X-RefSch", ReferenceOr.ofValue(hRefSch));
		r200.headers.put("X-NullSch", ReferenceOr.ofValue(new Header()));
		r200.headers.put("X-Ref", ReferenceOr.ofRef("#/components/headers/H1"));

		r200.links = new LinkedHashMap<>();
		r200.links.put("next", ReferenceOr.ofValue(cLink));
		r200.links.put("sparseLink", ReferenceOr.ofValue(cLinkServerNoUrl));
		r200.links.put("nullServerLink", ReferenceOr.ofValue(new Link()));
		r200.links.put("refLink", ReferenceOr.ofRef("#/components/links/L1"));

		op.responses.statusCodes.put("200", ReferenceOr.ofValue(r200));
		op.responses.defaultResponse = ReferenceOr.ofValue(r200);
		pi.get = op;

		Operation opSparse = new Operation();
		opSparse.operationId = "doSparse";
		opSparse.externalDocs = new ExternalDocumentation();
		opSparse.externalDocs.url = "https://example.com/sparse";
		opSparse.responses = new Responses();
		opSparse.responses.defaultResponse = ReferenceOr.ofRef("#/components/responses/R1");
		pi.post = opSparse;

		Operation opEmptyExt = new Operation();
		opEmptyExt.operationId = "doEmptyExt";
		opEmptyExt.externalDocs = new ExternalDocumentation();
		opEmptyExt.responses = new Responses();
		opEmptyExt.responses.defaultResponse = ReferenceOr.ofValue(new Response());
		pi.put = opEmptyExt;

		full.paths.pathItems.put("/rich", pi);

		String emitted = routes.Emit.emit(full, null);
		OpenAPI parsed = routes.Parse.parse(emitted);
		assertEquals("RichApi", parsed.info.title);
		assertEquals("2.0.0", parsed.info.version);
		assertEquals("Rich Summary", parsed.info.summary);
		assertEquals("Rich Description", parsed.info.description);
		assertEquals("https://example.com/tos", parsed.info.termsOfService);
		assertEquals("Dev", parsed.info.contact.name);
		assertEquals("Apache-2.0", parsed.info.license.name);
		assertEquals(2, parsed.tags.size());
		assertEquals("users", parsed.tags.get(0).name);
		assertEquals("prod", parsed.servers.get(0).name);
		assertTrue(parsed.components.securitySchemes.containsKey("oauth"));
		assertTrue(parsed.components.responses.containsKey("R1"));
		assertTrue(parsed.components.parameters.containsKey("P1"));
		assertTrue(parsed.components.requestBodies.containsKey("RB1"));
		assertTrue(parsed.components.headers.containsKey("H1"));
		assertTrue(parsed.components.links.containsKey("L1"));
		assertTrue(parsed.components.callbacks.containsKey("CB1"));
		assertTrue(parsed.components.pathItems.containsKey("PI1"));
		assertTrue(parsed.components.mediaTypes.containsKey("MT1"));

		Operation parsedOp = parsed.paths.pathItems.get("/rich").get;
		assertEquals("Do rich", parsedOp.summary);
		assertEquals("Detailed description", parsedOp.description);
		assertTrue(Boolean.TRUE.equals(parsedOp.deprecated));
		assertEquals("https://example.com/opdocs", parsedOp.externalDocs.url);
		assertEquals("Body payload", parsedOp.requestBody.value.description);
		assertTrue(Boolean.TRUE.equals(parsedOp.requestBody.value.required));
		assertEquals("OK resp", parsedOp.responses.statusCodes.get("200").value.description);
		assertEquals("Rate limit",
				parsedOp.responses.statusCodes.get("200").value.headers.get("X-Rate").value.description);
		assertEquals("op1", parsedOp.responses.statusCodes.get("200").value.links.get("next").value.operationId);

		// Also cover sparse Info/Contact/License and ref parameters in
		// RouteJavadocEmitter
		OpenAPI sparseInfoApi = new OpenAPI();
		sparseInfoApi.info = new Info();
		sparseInfoApi.info.contact = new Contact();
		sparseInfoApi.info.license = new License();
		sparseInfoApi.paths = new Paths();
		PathItem refPi = new PathItem();
		refPi.parameters = Collections.singletonList(ReferenceOr.ofRef("#/components/parameters/P1"));
		Operation refOp = new Operation();
		refOp.parameters = Collections.singletonList(ReferenceOr.ofRef("#/components/parameters/P1"));
		sparseInfoApi.paths.pathItems = Collections.singletonMap("/ref", refPi);
		assertNotNull(routes.RouteJavadocEmitter.buildClassJavadoc("Sparse", sparseInfoApi));
		StringBuilder refOpDoc = new StringBuilder();
		routes.RouteJavadocEmitter.appendOperationJavadocTags(refOpDoc, "/ref", refOp, refPi);
		assertTrue(refOpDoc.toString().contains("@return HttpResponse"));
	}
}
