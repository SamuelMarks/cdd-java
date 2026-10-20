import org.junit.Test;
import static org.junit.Assert.*;
import openapi.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.*;

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
}
