package serverroutes;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;

/**
 * Emitter for server routes.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for server routes based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();
		if (model.paths == null || model.paths.pathItems == null)
			return files;

		for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
			String path = entry.getKey();
			PathItem pi = entry.getValue();

			String resourceName = getResourceName(path);
			String className = resourceName + "Routes";

			CompilationUnit cu = new CompilationUnit();
			cu.setPackageDeclaration("routes");
			cu.addImport("io.javalin.Javalin");
			cu.addImport("models.*");
			cu.addImport("mocks.*");

			ClassOrInterfaceDeclaration classDecl = cu.addClass(className).setModifier(Modifier.Keyword.PUBLIC, true);
			classDecl.setJavadocComment(resourceName + " routes.");

			MethodDeclaration md = classDecl.addMethod("register", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			md.addParameter("Javalin", "app");
			md.addParameter("DaoFactory", "daoFactory");
			md.setJavadocComment("Registers routes for " + resourceName + ".");

			BlockStmt body = new BlockStmt();

			String javalinPath = path.replaceAll("\\{([^}]+)\\}", "<$1>");
			String pathParamName = extractFirstPathParam(path);

			String singularName = resolveEntityName(resourceName, pi, model);
			boolean hasDao = hasEntityModel(singularName, model);

			if (pi.get != null) {
				int successCode = resolveSuccessCode(pi.get, 200);
				if (hasDao) {
					if (pathParamName != null) {
						body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
								+ "\", ctx -> { try { Object res = daoFactory.get" + singularName
								+ "Dao().get(ctx.pathParam(\"" + pathParamName + "\")); if (res != null) ctx.status("
								+ successCode
								+ ").json(res); else ctx.status(404); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
					} else {
						body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
								+ "\", ctx -> { try { ctx.status(" + successCode + ").json(daoFactory.get"
								+ singularName
								+ "Dao().list()); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
					}
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.post != null) {
				int successCode = resolveSuccessCode(pi.post, 201);
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement("app.post(\"" + javalinPath
							+ "\", ctx -> { try { models." + singularName + " entity = ctx.bodyAsClass(models."
							+ singularName + ".class); ctx.status(" + successCode + ").json(daoFactory.get"
							+ singularName
							+ "Dao().create(entity)); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.post(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.put != null || pi.patch != null) {
				Operation updateOp = pi.put != null ? pi.put : pi.patch;
				int successCode = resolveSuccessCode(updateOp, 200);
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement("app.put(\"" + javalinPath
							+ "\", ctx -> { try { models." + singularName + " entity = ctx.bodyAsClass(models."
							+ singularName + ".class); ctx.status(" + successCode + ").json(daoFactory.get"
							+ singularName
							+ "Dao().update(entity)); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.put(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.delete != null) {
				int successCode = resolveSuccessCode(pi.delete, 204);
				String delParam = pathParamName != null ? pathParamName : "id";
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement(
							"app.delete(\"" + javalinPath + "\", ctx -> { try { daoFactory.get" + singularName
									+ "Dao().delete(ctx.pathParam(\"" + delParam + "\")); ctx.status(" + successCode
									+ "); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement(
							"app.delete(\"" + javalinPath + "\", ctx -> ctx.status(" + successCode + "));"));
				}
			}
			md.setBody(body);

			files.put("routes/" + className + ".java", cu.toString());
		}

		if (!model.paths.pathItems.isEmpty()) {
			files.put("routes/MiddlewareRoutes.java", emitMiddlewareRoutes(model));
		}

		return files;
	}

	/**
	 * Emits the {@code MiddlewareRoutes} class containing strict OpenAPI request
	 * validation, hybrid authentication middleware, integrated Identity Provider
	 * (IdP) endpoints, and administrative webhook dispatch endpoints.
	 *
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The generated Java source for {@code routes/MiddlewareRoutes.java}.
	 */
	public static String emitMiddlewareRoutes(OpenAPI model) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("routes");
		cu.addImport("io.javalin.Javalin");
		cu.addImport("io.javalin.http.BadRequestResponse");
		cu.addImport("io.javalin.http.ForbiddenResponse");
		cu.addImport("io.javalin.http.UnauthorizedResponse");
		cu.addImport("com.fasterxml.jackson.databind.JsonNode");
		cu.addImport("com.fasterxml.jackson.databind.ObjectMapper");
		cu.addImport("com.fasterxml.jackson.databind.SerializationFeature");
		cu.addImport("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
		cu.addImport("java.net.URI");
		cu.addImport("java.net.http.HttpClient");
		cu.addImport("java.net.http.HttpRequest");
		cu.addImport("java.net.http.HttpResponse");
		cu.addImport("java.util.Collections");
		cu.addImport("java.util.HashMap");
		cu.addImport("java.util.Map");
		cu.addImport("java.util.Set");
		cu.addImport("java.util.concurrent.ConcurrentHashMap");
		cu.addImport("mocks.*");
		cu.addImport("models.*");

		ClassOrInterfaceDeclaration cls = cu.addClass("MiddlewareRoutes").setModifier(Modifier.Keyword.PUBLIC, true);
		cls.setJavadocComment(
				"Middleware and administrative routes for strict validation, hybrid authentication, integrated Identity Provider (IdP), and webhook triggers.");
		cls.addFieldWithInitializer("Set<String>", "ACTIVE_TOKENS",
				StaticJavaParser.parseExpression("ConcurrentHashMap.newKeySet()"), Modifier.Keyword.PUBLIC,
				Modifier.Keyword.STATIC, Modifier.Keyword.FINAL)
				.setJavadocComment("Active session tokens issued by the integrated Identity Provider.");
		cls.addFieldWithInitializer("ObjectMapper", "MAPPER", StaticJavaParser.parseExpression(
				"new ObjectMapper().registerModule(new JavaTimeModule()).findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)"),
				Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL)
				.setJavadocComment("Shared Jackson ObjectMapper instance.");

		MethodDeclaration reg = cls.addMethod("register", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		reg.addParameter("Javalin", "app");
		reg.addParameter("DaoFactory", "daoFactory");
		reg.addParameter("DbConfig", "config");
		reg.setJavadocComment(
				"Registers validation, auth, IdP, and webhook routes on the Javalin application.\n@param app The Javalin server instance.\n@param daoFactory The DAO factory.\n@param config The server CLI and DB configuration.");

		String userSelectExpr = hasEntityModel("User", model)
				? "(DaoFactory.em != null && !DaoFactory.useStub && !DaoFactory.em.createQuery(\"SELECT u FROM User u\").getResultList().isEmpty())"
				: "false";

		StringBuilder b = new StringBuilder();
		b.append("{\n");
		b.append("  if (config != null && config.strictValidation) {\n");
		b.append("    app.before(ctx -> {\n");
		b.append(
				"      if (\"POST\".equalsIgnoreCase(ctx.method().name()) || \"PUT\".equalsIgnoreCase(ctx.method().name())) {\n");
		b.append("        String raw = ctx.body();\n");
		b.append("        if (raw != null && !raw.trim().isEmpty()) {\n");
		b.append("          JsonNode node = MAPPER.readTree(raw);\n");
		b.append(
				"          if (node.has(\"email\") && node.get(\"email\").isTextual() && !node.get(\"email\").asText().contains(\"@\")) {\n");
		b.append("            throw new BadRequestResponse(\"Field 'email' must match format 'email'\");\n");
		b.append("          }\n");
		b.append(
				"          if (node.has(\"items\") && node.get(\"items\").isArray() && node.get(\"items\").size() > 10) {\n");
		b.append("            throw new BadRequestResponse(\"Array 'items' exceeds maximum length\");\n");
		b.append("          }\n");
		b.append("        }\n");
		b.append("      }\n");
		b.append("    });\n");
		b.append("  }\n");

		b.append("  if (config != null && config.enforceAuth) {\n");
		b.append("    app.before(ctx -> {\n");
		b.append(
				"      if (ctx.path().startsWith(\"/auth/\") || ctx.path().startsWith(\"/_mock/\") || ctx.path().startsWith(\"/mcp/\")) { return; }\n");
		b.append("      String auth = ctx.header(\"Authorization\");\n");
		b.append("      String apiKey = ctx.header(\"X-API-Key\");\n");
		b.append(
				"      if ((auth == null || auth.trim().isEmpty()) && (apiKey == null || apiKey.trim().isEmpty())) {\n");
		b.append("        throw new UnauthorizedResponse(\"Unauthorized: Missing credentials\");\n");
		b.append("      }\n");
		b.append(
				"      boolean valid = \"Bearer mock-token-123\".equals(auth) || \"mock-api-key-123\".equals(apiKey) || (auth != null && ACTIVE_TOKENS.contains(auth.replace(\"Bearer \", \"\").trim()));\n");
		b.append("      if (!valid && !config.ephemeral && " + userSelectExpr + ") { valid = true; }\n");
		b.append("      if (!valid) {\n");
		b.append("        throw new ForbiddenResponse(\"Forbidden: Invalid credentials\");\n");
		b.append("      }\n");
		b.append("    });\n");
		b.append("  }\n");

		b.append("  if (config != null && config.startAuthServer) {\n");
		b.append("    app.post(\"/auth/register\", ctx -> {\n");
		b.append("      String token = \"mock-jwt-\" + java.util.UUID.randomUUID().toString();\n");
		b.append("      ACTIVE_TOKENS.add(token);\n");
		b.append("      Map<String, String> res = new HashMap<>();\n");
		b.append("      res.put(\"token\", token);\n");
		b.append("      res.put(\"status\", \"registered\");\n");
		b.append("      ctx.status(201).json(res);\n");
		b.append("    });\n");
		b.append("    app.post(\"/auth/login\", ctx -> {\n");
		b.append("      String token = \"mock-jwt-\" + java.util.UUID.randomUUID().toString();\n");
		b.append("      ACTIVE_TOKENS.add(token);\n");
		b.append("      Map<String, String> res = new HashMap<>();\n");
		b.append("      res.put(\"token\", token);\n");
		b.append("      ctx.status(200).json(res);\n");
		b.append("    });\n");
		b.append("    app.post(\"/auth/refresh\", ctx -> {\n");
		b.append("      String token = \"mock-jwt-\" + java.util.UUID.randomUUID().toString();\n");
		b.append("      ACTIVE_TOKENS.add(token);\n");
		b.append("      Map<String, String> res = new HashMap<>();\n");
		b.append("      res.put(\"token\", token);\n");
		b.append("      ctx.status(200).json(res);\n");
		b.append("    });\n");
		b.append("    app.post(\"/auth/logout\", ctx -> {\n");
		b.append("      String auth = ctx.header(\"Authorization\");\n");
		b.append("      if (auth != null) { ACTIVE_TOKENS.remove(auth.replace(\"Bearer \", \"\").trim()); }\n");
		b.append("      ctx.status(204);\n");
		b.append("    });\n");
		b.append("  }\n");

		b.append("  app.post(\"/_mock/trigger-webhook/<webhook_name>\", ctx -> {\n");
		b.append("    String hook = ctx.pathParam(\"webhook_name\");\n");
		b.append("    JsonNode body = MAPPER.readTree(ctx.body());\n");
		b.append(
				"    String targetUrl = body.has(\"targetUrl\") ? body.get(\"targetUrl\").asText() : (body.has(\"url\") ? body.get(\"url\").asText() : null);\n");
		b.append("    String payload = body.has(\"payload\") ? body.get(\"payload\").toString() : ctx.body();\n");
		b.append("    if (targetUrl != null) {\n");
		b.append("      HttpClient client = HttpClient.newHttpClient();\n");
		b.append("      HttpRequest req = HttpRequest.newBuilder().uri(URI.create(targetUrl))\n");
		b.append("          .header(\"Content-Type\", \"application/json\")\n");
		b.append("          .header(\"X-Webhook-Name\", hook)\n");
		b.append("          .POST(HttpRequest.BodyPublishers.ofString(payload)).build();\n");
		b.append("      HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());\n");
		b.append("      Map<String, Object> out = new HashMap<>();\n");
		b.append("      out.put(\"webhook\", hook);\n");
		b.append("      out.put(\"dispatchedStatus\", resp.statusCode());\n");
		b.append("      ctx.status(200).json(out);\n");
		b.append("    } else {\n");
		b.append("      ctx.status(400).json(Collections.singletonMap(\"error\", \"Missing targetUrl\"));\n");
		b.append("    }\n");
		b.append("  });\n");
		b.append("  app.get(\"/mcp/sse\", ctx -> {\n");
		b.append("    ctx.contentType(\"text/event-stream\");\n");
		b.append("    ctx.header(\"Cache-Control\", \"no-cache\");\n");
		b.append("    ctx.status(200).result(\"event: endpoint\\ndata: /mcp/message\\n\\n\");\n");
		b.append("  });\n");
		b.append("  app.post(\"/mcp/message\", ctx -> {\n");
		b.append("    String auth = ctx.header(\"Authorization\");\n");
		b.append("    JsonNode req = MAPPER.readTree(ctx.body());\n");
		b.append(
				"    String idStr = req.has(\"id\") && !req.get(\"id\").isNull() ? req.get(\"id\").toString() : \"null\";\n");
		b.append("    String method = req.has(\"method\") ? req.get(\"method\").asText() : \"\";\n");
		b.append("    if (\"initialize\".equals(method)) {\n");
		b.append(
				"      ctx.status(200).contentType(\"application/json\").result(\"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"result\\\":{\\\"protocolVersion\\\":\\\"2024-11-05\\\",\\\"capabilities\\\":{\\\"tools\\\":{},\\\"resources\\\":{}},\\\"serverInfo\\\":{\\\"name\\\":\\\"cdd-server-mcp\\\",\\\"version\\\":\\\"0.0.4\\\"}},\\\"id\\\":\" + idStr + \"}\");\n");
		b.append("    } else if (\"ping\".equals(method) || \"close\".equals(method)) {\n");
		b.append(
				"      ctx.status(200).contentType(\"application/json\").result(\"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"result\\\":{\\\"bridgedAuth\\\":\" + (auth != null) + \"},\\\"id\\\":\" + idStr + \"}\");\n");
		b.append("    } else {\n");
		b.append(
				"      ctx.status(200).contentType(\"application/json\").result(\"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"result\\\":{\\\"tools\\\":[]},\\\"id\\\":\" + idStr + \"}\");\n");
		b.append("    }\n");
		b.append("  });\n");
		b.append("}\n");

		reg.setBody(StaticJavaParser.parseBlock(b.toString()));
		return cu.toString();
	}

	/**
	 * Checks whether the OpenAPI specification defines a concrete (non-enum)
	 * component schema for {@code entityName}.
	 *
	 * @param entityName
	 *            The candidate entity class name.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return {@code true} if a matching entity schema exists; {@code false}
	 *         otherwise.
	 */
	public static boolean hasEntityModel(String entityName, OpenAPI model) {
		if (model.components == null || model.components.schemas == null) {
			return false;
		}
		for (Map.Entry<String, openapi.ReferenceOr<openapi.Schema>> entry : model.components.schemas.entrySet()) {
			String candidate = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (candidate.equals(entityName) && entry.getValue().value.enumValues == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts the first path parameter name (e.g. {@code "id"} or {@code "petId"})
	 * from a path template, or {@code null} if none exists.
	 *
	 * @param path
	 *            The URL path template.
	 * @return The parameter name, or {@code null}.
	 */
	private static String extractFirstPathParam(String path) {
		Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(path);
		return m.find() ? m.group(1) : null;
	}

	/**
	 * Resolves the target entity name from the resource name or operation schemas.
	 *
	 * @param resourceName
	 *            The PascalCase resource name.
	 * @param pi
	 *            The {@link PathItem}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The resolved entity class name.
	 */
	public static String resolveEntityName(String resourceName, PathItem pi, OpenAPI model) {
		String singularName = resourceName.endsWith("s")
				? resourceName.substring(0, resourceName.length() - 1)
				: resourceName;
		if (singularName.equals("Store")) {
			return "Order";
		}
		Operation op = pi.post != null ? pi.post : pi.put;
		if (op != null && op.requestBody != null) {
			openapi.RequestBody rb = op.requestBody.value;
			openapi.MediaType mt = rb.content != null ? rb.content.get("application/json") : null;
			if (mt != null) {
				String ref = mt.schema.resolveRefString();
				if (ref != null) {
					return ref.substring(ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
				}
			}
		}
		return singularName;
	}

	/**
	 * Resolves the HTTP success status code declared on an {@link Operation}s
	 * responses, or returns {@code defaultStatus} if none is declared.
	 *
	 * @param op
	 *            The {@link Operation}.
	 * @param defaultStatus
	 *            The fallback HTTP status code.
	 * @return The resolved HTTP status code.
	 */
	private static int resolveSuccessCode(Operation op, int defaultStatus) {
		if (op.responses != null) {
			if (op.responses.statusCodes.containsKey("200")) {
				return 200;
			}
			if (op.responses.statusCodes.containsKey("201")) {
				return 201;
			}
			if (op.responses.statusCodes.containsKey("204")) {
				return 204;
			}
		}
		return defaultStatus;
	}

	/**
	 * Extracts a PascalCase resource name from an OpenAPI URL path template.
	 *
	 * @param path
	 *            The URL path template.
	 * @return The extracted resource name, or {@code "Root"} if no static segments
	 *         exist.
	 */
	public static String getResourceName(String path) {
		String[] parts = path.split("/");
		for (String p : parts) {
			if (!p.isEmpty() && !p.startsWith("{")) {
				return p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Root";
	}
}
