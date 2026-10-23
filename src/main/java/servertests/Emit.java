package servertests;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Map;
import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Schema;

/**
 * Emitter for server tests.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for server tests based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();

		// Model Tests
		if (model.components != null && model.components.schemas != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse"))
					continue;
				if (entry.getValue().value.enumValues != null)
					continue;

				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("models");
				cu.addImport("org.junit.Test");
				cu.addImport("static org.junit.Assert.*");

				ClassOrInterfaceDeclaration testClass = cu.addClass(className + "Test")
						.setModifier(Modifier.Keyword.PUBLIC, true);
				testClass.setJavadocComment("Tests for " + className + " model.");

				MethodDeclaration md = testClass.addMethod("testInstantiation", Modifier.Keyword.PUBLIC);
				md.addAnnotation("Test");
				md.setJavadocComment("Test instantiation of " + className + ".");
				BlockStmt body = new BlockStmt();
				body.addStatement(StaticJavaParser.parseStatement(className + " obj = new " + className + "();"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(obj);"));
				md.setBody(body);

				files.put("models/" + className + "Test.java", cu.toString());
			}
		}

		// Route Tests using Stub DAOs
		if (model.paths != null && model.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				PathItem pi = entry.getValue();
				String resourceName = serverroutes.Emit.getResourceName(path);
				String className = resourceName + "Routes";
				String singularName = serverroutes.Emit.resolveEntityName(resourceName, pi, model);
				boolean hasDao = serverroutes.Emit.hasEntityModel(singularName, model);

				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("routes");
				cu.addImport("org.junit.Test");
				cu.addImport("static org.junit.Assert.*");
				cu.addImport("io.javalin.Javalin");
				cu.addImport("mocks.*");
				cu.addImport("models.*");

				ClassOrInterfaceDeclaration testClass = cu.addClass(className + "Test")
						.setModifier(Modifier.Keyword.PUBLIC, true);
				testClass.setJavadocComment("Tests for " + className + ".");

				MethodDeclaration md = testClass.addMethod("testWiring", Modifier.Keyword.PUBLIC);
				md.addAnnotation("Test");
				md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
				md.setJavadocComment("Test DAO factory wiring, stub DAO CRUD, and Javalin route registration.");
				BlockStmt body = new BlockStmt();
				body.addStatement(StaticJavaParser.parseStatement("DaoFactory.useStub = true;"));
				body.addStatement(StaticJavaParser.parseStatement("DaoFactory factory = new DaoFactory();"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(factory);"));
				if (hasDao) {
					body.addStatement(
							StaticJavaParser.parseStatement("assertNotNull(factory.get" + singularName + "Dao());"));
					body.addStatement(StaticJavaParser
							.parseStatement("assertNotNull(factory.get" + singularName + "Dao().list());"));
				}
				body.addStatement(StaticJavaParser.parseStatement("Javalin app = Javalin.create();"));
				body.addStatement(StaticJavaParser.parseStatement(className + ".register(app, factory);"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(app);"));
				md.setBody(body);

				files.put("routes/" + className + "Test.java", cu.toString());
			}
			if (!model.paths.pathItems.isEmpty()) {
				files.put("routes/MiddlewareRoutesTest.java", emitMiddlewareRoutesTest());
			}
		}

		return files;
	}

	/**
	 * Emits the {@code MiddlewareRoutesTest} class verifying CLI flag parsing,
	 * strict request validation, hybrid authentication, integrated Identity
	 * Provider (IdP) lifecycle, and administrative webhook dispatch.
	 *
	 * @return The generated Java source for
	 *         {@code routes/MiddlewareRoutesTest.java}.
	 */
	public static String emitMiddlewareRoutesTest() {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("routes");
		cu.addImport("org.junit.Test");
		cu.addImport("static org.junit.Assert.*");
		cu.addImport("io.javalin.Javalin");
		cu.addImport("java.net.InetSocketAddress");
		cu.addImport("java.net.URI");
		cu.addImport("java.net.http.HttpClient");
		cu.addImport("java.net.http.HttpRequest");
		cu.addImport("java.net.http.HttpResponse");
		cu.addImport("com.sun.net.httpserver.HttpServer");
		cu.addImport("mocks.*");

		ClassOrInterfaceDeclaration testClass = cu.addClass("MiddlewareRoutesTest").setModifier(Modifier.Keyword.PUBLIC,
				true);
		testClass.setJavadocComment(
				"Integration tests for CLI flags, strict validation, authentication, IdP, and webhook triggers.");

		MethodDeclaration md = testClass.addMethod("testMiddlewareAndCapabilities", Modifier.Keyword.PUBLIC);
		md.addAnnotation("Test");
		md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
		md.setJavadocComment(
				"Tests CLI flag matrix, strict validation errors, auth enforcement, IdP flow, and webhook dispatch.");

		StringBuilder b = new StringBuilder();
		b.append("{\n");
		b.append("  DbConfig defaultCfg = DbConfig.load();\n");
		b.append("  assertNotNull(defaultCfg);\n");
		b.append(
				"  DbConfig cfg = DbConfig.fromArgs(new String[]{\"--ephemeral\", \"--seed\", \"--strict-validation\", \"--enforce-auth\", \"--start-auth-server\"});\n");
		b.append("  assertTrue(cfg.ephemeral);\n");
		b.append("  assertTrue(cfg.seed);\n");
		b.append("  assertTrue(cfg.strictValidation);\n");
		b.append("  assertTrue(cfg.enforceAuth);\n");
		b.append("  assertTrue(cfg.startAuthServer);\n");
		b.append("  DaoFactory.useStub = true;\n");
		b.append("  DaoFactory factory = new DaoFactory();\n");
		b.append(
				"  Javalin app = Javalin.create(c -> { c.plugins.enableCors(cors -> { cors.add(it -> { it.anyHost(); }); }); });\n");
		b.append("  MiddlewareRoutes.register(app, factory, cfg);\n");
		b.append("  app.get(\"/_protected_probe\", ctx -> ctx.status(200).result(\"ok\"));\n");
		b.append("  app.post(\"/_protected_probe\", ctx -> ctx.status(200).result(\"created\"));\n");
		b.append("  app.start(0);\n");
		b.append("  int port = app.port();\n");
		b.append("  String baseUrl = \"http://localhost:\" + port;\n");
		b.append("  HttpClient client = HttpClient.newHttpClient();\n");
		b.append("  HttpServer receiver = HttpServer.create(new InetSocketAddress(0), 0);\n");
		b.append("  final StringBuilder receivedPayload = new StringBuilder();\n");
		b.append("  receiver.createContext(\"/hook\", ex -> {\n");
		b.append("    byte[] bytes = ex.getRequestBody().readAllBytes();\n");
		b.append("    receivedPayload.append(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));\n");
		b.append("    ex.sendResponseHeaders(200, 2);\n");
		b.append("    ex.getResponseBody().write(\"ok\".getBytes(java.nio.charset.StandardCharsets.UTF_8));\n");
		b.append("    ex.close();\n");
		b.append("  });\n");
		b.append("  receiver.start();\n");
		b.append("  int receiverPort = receiver.getAddress().getPort();\n");
		b.append("  try {\n");
		b.append(
				"    HttpResponse<String> corsPreflight = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/auth/login\")).header(\"Origin\", \"http://example.com\").header(\"Access-Control-Request-Method\", \"POST\").method(\"OPTIONS\", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertTrue(corsPreflight.statusCode() >= 200 && corsPreflight.statusCode() < 300);\n");
		b.append(
				"    HttpResponse<String> unauth = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).GET().build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(401, unauth.statusCode());\n");
		b.append(
				"    HttpResponse<String> forbidden = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).header(\"Authorization\", \"Bearer wrong\").GET().build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(403, forbidden.statusCode());\n");
		b.append(
				"    HttpResponse<String> mockTokenOk = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).header(\"Authorization\", \"Bearer mock-token-123\").GET().build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(200, mockTokenOk.statusCode());\n");
		b.append(
				"    HttpResponse<String> badEmail = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).header(\"Authorization\", \"Bearer mock-token-123\").POST(HttpRequest.BodyPublishers.ofString(\"{\\\"email\\\":\\\"invalid-email\\\"}\")).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(400, badEmail.statusCode());\n");
		b.append("    assertTrue(badEmail.body().contains(\"Field 'email' must match format 'email'\"));\n");
		b.append(
				"    HttpResponse<String> badItems = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).header(\"Authorization\", \"Bearer mock-token-123\").POST(HttpRequest.BodyPublishers.ofString(\"{\\\"items\\\":[1,2,3,4,5,6,7,8,9,10,11]}\")).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(400, badItems.statusCode());\n");
		b.append("    assertTrue(badItems.body().contains(\"Array 'items' exceeds maximum length\"));\n");
		b.append(
				"    HttpResponse<String> regRes = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/auth/register\")).POST(HttpRequest.BodyPublishers.ofString(\"{\\\"username\\\":\\\"u\\\",\\\"password\\\":\\\"p\\\"}\")).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(201, regRes.statusCode());\n");
		b.append(
				"    HttpResponse<String> loginRes = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/auth/login\")).POST(HttpRequest.BodyPublishers.ofString(\"{\\\"username\\\":\\\"u\\\",\\\"password\\\":\\\"p\\\"}\")).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(200, loginRes.statusCode());\n");
		b.append("    String body = loginRes.body();\n");
		b.append("    int q1 = body.indexOf('\"', body.indexOf(\"\\\"token\\\"\") + 8);\n");
		b.append("    int q2 = body.indexOf('\"', q1 + 1);\n");
		b.append("    String jwt = body.substring(q1 + 1, q2);\n");
		b.append(
				"    HttpResponse<String> idpOk = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_protected_probe\")).header(\"Authorization\", \"Bearer \" + jwt).GET().build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(200, idpOk.statusCode());\n");
		b.append(
				"    HttpResponse<String> logoutRes = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/auth/logout\")).header(\"Authorization\", \"Bearer \" + jwt).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(204, logoutRes.statusCode());\n");
		b.append(
				"    String hookBody = \"{\\\"targetUrl\\\":\\\"http://localhost:\" + receiverPort + \"/hook\\\",\\\"payload\\\":{\\\"event\\\":\\\"pet.created\\\"}}\";\n");
		b.append(
				"    HttpResponse<String> hookRes = client.send(HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/_mock/trigger-webhook/newPet\")).POST(HttpRequest.BodyPublishers.ofString(hookBody)).build(), HttpResponse.BodyHandlers.ofString());\n");
		b.append("    assertEquals(200, hookRes.statusCode());\n");
		b.append("    assertTrue(receivedPayload.toString().contains(\"pet.created\"));\n");
		b.append("  } finally {\n");
		b.append("    receiver.stop(0);\n");
		b.append("    app.stop();\n");
		b.append("  }\n");
		b.append("}\n");

		md.setBody(StaticJavaParser.parseBlock(b.toString()));
		return cu.toString();
	}
}
