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
		assertNotNull(out);
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
		assertNotNull(api);
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
		System.setProperty("cdd.test", "true");
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
		try {
			cli.Main.main(new String[]{"process_in_memory"});
		} catch (Exception e) {
		}
		try {
			cli.Main.main(new String[]{"invalid_cmd"});
		} catch (Exception e) {
		}
		assertTrue(cli.CddCli.hasFlag(new String[]{"-a"}, "-a", "--all", null));
		assertTrue(cli.CddCli.hasFlag(new String[]{"--all"}, "-a", "--all", null));
		assertFalse(cli.CddCli.hasFlag(new String[]{"-b"}, "-a", "--all", null));
		assertEquals("val", cli.CddCli.getArg(new String[]{"-a", "val"}, "-a", "--all", ""));
		assertNull(cli.CddCli.getArg(new String[]{"-a"}, "-a", "--all", ""));
		assertNull(cli.CddCli.getArg(new String[]{"-b", "val"}, "-a", "--all", ""));
		assertNotNull(cli.SyncCommand.extractOpenAPI(new java.io.File("non_existent_file.json")));
		assertNotNull(cli.ProjectScaffolder.resolveWasiPath("/absolute/path", cli.CddCli.envProvider));
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
		assertNotNull(api);
		String em = routes.Emit.emit(api, code);
		assertNotNull(em);
	}
}
