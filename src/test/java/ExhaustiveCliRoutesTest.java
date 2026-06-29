import org.junit.Test;
import static org.junit.Assert.*;
import openapi.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class ExhaustiveCliRoutesTest {

	@Test
	public void testCliEmitEmptyObjects() {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.servers = new ArrayList<>();
		api.servers.add(new Server());
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.schemas.put("EmptySchema", new Schema());
		api.components.responses = new HashMap<>();
		api.components.responses.put("EmptyResponse", new openapi.Response());
		api.components.parameters = new HashMap<>();
		api.components.parameters.put("EmptyParam", new openapi.Parameter());
		api.components.requestBodies = new HashMap<>();
		api.components.requestBodies.put("EmptyRequestBody", new RequestBody());
		api.components.headers = new HashMap<>();
		api.components.headers.put("EmptyHeader", new Header());
		api.components.securitySchemes = new HashMap<>();
		api.components.securitySchemes.put("EmptySecurityScheme", new SecurityScheme());
		api.components.links = new HashMap<>();
		api.components.links.put("EmptyLink", new Link());
		api.components.callbacks = new HashMap<>();
		api.components.callbacks.put("EmptyCallback", new Callback());
		api.components.pathItems = new HashMap<>();
		api.components.pathItems.put("EmptyPathItem", new PathItem());

		api.paths = new openapi.Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.get.parameters = new ArrayList<>();
		pi.get.parameters.add(new openapi.Parameter());
		pi.get.requestBody = new RequestBody();
		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		pi.get.responses.statusCodes.put("200", new openapi.Response());
		pi.get.responses.defaultResponse = new openapi.Response();
		pi.get.callbacks = new HashMap<>();
		pi.get.callbacks.put("EmptyCallback", new Callback());
		api.paths.pathItems.put("/empty", pi);

		String out = cli.Emit.emitCli(api);
		assertNotNull(out);
	}

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

	@Test
	public void testCliMainReflection() throws Exception {
		System.setProperty("cdd.test", "true");
		Method processInMemory = cli.CddCli.class.getDeclaredMethod("processInMemory", String.class);
		processInMemory.setAccessible(true);
		try {
			processInMemory.invoke(null, "{");
		} catch (Exception e) {
		}
		try {
			processInMemory.invoke(null, "{\"command\": \"to_openapi\"}");
		} catch (Exception e) {
		}
		Method startStdioJsonRpcServer = cli.CddCli.class.getDeclaredMethod("startStdioJsonRpcServer");
		startStdioJsonRpcServer.setAccessible(true);
		InputStream oldIn = System.in;
		System.setIn(new ByteArrayInputStream("".getBytes()));
		try {
			startStdioJsonRpcServer.invoke(null);
		} catch (Exception e) {
		}
		System.setIn(oldIn);
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
		Method hasFlag = cli.CddCli.class.getDeclaredMethod("hasFlag", String[].class, String.class, String.class,
				String.class);
		hasFlag.setAccessible(true);
		assertEquals(true, (boolean) hasFlag.invoke(null, new Object[]{new String[]{"-a"}, "-a", "--all", null}));
		assertEquals(true, (boolean) hasFlag.invoke(null, new Object[]{new String[]{"--all"}, "-a", "--all", null}));
		assertEquals(false, (boolean) hasFlag.invoke(null, new Object[]{new String[]{"-b"}, "-a", "--all", null}));
		Method getArg = cli.CddCli.class.getDeclaredMethod("getArg", String[].class, String.class, String.class,
				String.class);
		getArg.setAccessible(true);
		assertEquals("val", getArg.invoke(null, new Object[]{new String[]{"-a", "val"}, "-a", "--all", ""}));
		assertNull(getArg.invoke(null, new Object[]{new String[]{"-a"}, "-a", "--all", ""}));
		assertNull(getArg.invoke(null, new Object[]{new String[]{"-b", "val"}, "-a", "--all", ""}));
		Method extractOpenAPI = cli.CddCli.class.getDeclaredMethod("extractOpenAPI", java.io.File.class);
		extractOpenAPI.setAccessible(true);
		try {
			extractOpenAPI.invoke(null, new java.io.File("non_existent_file.json"));
		} catch (Exception e) {
		}
		Method resolveFile = cli.CddCli.class.getDeclaredMethod("resolveFile", String.class);
		resolveFile.setAccessible(true);
		assertNotNull(resolveFile.invoke(null, "/absolute/path"));
	}

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
