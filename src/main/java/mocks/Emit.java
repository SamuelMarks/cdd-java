package mocks;

import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;
import openapi.Response;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Emits mock servers to language source preserving lexical layout.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Java code for mock servers using com.sun.net.httpserver.HttpServer.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		String title = (model.info != null && model.info.title != null)
				? model.info.title.replaceAll("[^a-zA-Z0-9]", "")
				: "ApiMock";
		if (title.isEmpty())
			title = "ApiMock";

		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = StaticJavaParser.parse(existingSource);
			LexicalPreservingPrinter.setup(cu);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("import com.sun.net.httpserver.HttpServer;\n");
			sb.append("import com.sun.net.httpserver.HttpExchange;\n");
			sb.append("import java.net.InetSocketAddress;\n");
			sb.append("import java.io.IOException;\n");
			sb.append("import java.io.OutputStream;\n\n");
			sb.append("/**\n * Auto-generated mock server for ").append(title).append(".\n */\n");
			sb.append("public class ").append(title).append("MockServer {\n");
			sb.append("    private HttpServer server;\n\n");
			sb.append("    public void stop() {\n");
			sb.append("        if (server != null) {\n");
			sb.append("            server.stop(0);\n");
			sb.append("        }\n");
			sb.append("    }\n");
			sb.append("}\n");
			cu = StaticJavaParser.parse(sb.toString());
			isNew = true;
		}
		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(title + "MockServer").orElse(null);
		if (classDecl == null) {
			java.util.List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
			if (!classes.isEmpty()) {
				classDecl = classes.get(0);
			}
		}
		if (classDecl != null) {
			if (!hasMember(classDecl, "start")) {
				StringBuilder sb = new StringBuilder();
				sb.append("/**\n");
				if (model.paths != null && model.paths.pathItems != null) {
					for (java.util.Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
						appendPathJavadoc(sb, entry.getKey(), entry.getValue(), model);
					}
				}
				sb.append(" */\n");
				sb.append("public void start(int port) throws IOException {\n");
				sb.append("    server = HttpServer.create(new InetSocketAddress(port), 0);\n");

				if (model.paths != null && model.paths.pathItems != null) {
					for (java.util.Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
						String path = entry.getKey();
						PathItem pi = entry.getValue();
						String handlerPath = path.replaceAll("\\{[^}]+\\}", "");
						if (handlerPath.endsWith("/") && handlerPath.length() > 1) {
							handlerPath = handlerPath.substring(0, handlerPath.length() - 1);
						}

						Operation primaryOp = firstOperation(pi);
						int status = resolveStatusCode(primaryOp);
						String responsePayload = synthesizeMockPayload(primaryOp, model);
						String escapedResponse = responsePayload.replace("\\", "\\\\").replace("\"", "\\\"");

						sb.append("    server.createContext(\"").append(handlerPath)
								.append("\", (HttpExchange exchange) -> {\n");
						sb.append("        String response = \"").append(escapedResponse).append("\";\n");
						sb.append("        exchange.sendResponseHeaders(").append(status)
								.append(", response.length());\n");
						sb.append("        try (OutputStream os = exchange.getResponseBody()) {\n");
						sb.append("            os.write(response.getBytes());\n");
						sb.append("        }\n");
						sb.append("    });\n");
					}
				}

				sb.append("    server.setExecutor(null);\n");
				sb.append("    server.start();\n");
				sb.append("    System.out.println(\"Mock server started on port \" + port);\n");
				sb.append("}\n");

				classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
			}
		}

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Emits modular Java code for mock servers.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return A map of file paths to generated Java source code.
	 */
	public static java.util.Map<String, String> emitModular(OpenAPI model) {
		java.util.Map<String, String> files = new java.util.HashMap<>();
		if (model.paths == null)
			return files;
		if (model.paths.pathItems == null)
			return files;

		for (java.util.Map.Entry<String, openapi.PathItem> entry : model.paths.pathItems.entrySet()) {
			String path = entry.getKey();
			PathItem pi = entry.getValue();
			String resourceName = getResourceName(path);
			String className = resourceName + "MockServer";

			CompilationUnit cu = new CompilationUnit();
			cu.setPackageDeclaration("mocks");
			cu.addImport("com.sun.net.httpserver.HttpServer");
			cu.addImport("com.sun.net.httpserver.HttpExchange");
			cu.addImport("java.net.InetSocketAddress");
			cu.addImport("java.io.IOException");
			cu.addImport("java.io.OutputStream");

			ClassOrInterfaceDeclaration classDecl = cu.addClass(className)
					.setModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC, true);
			classDecl.setJavadocComment("Mock server for " + resourceName + ".");

			classDecl.addField("HttpServer", "server", com.github.javaparser.ast.Modifier.Keyword.PRIVATE);

			MethodDeclaration startMethod = classDecl.addMethod("start",
					com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			startMethod.addParameter("int", "port");
			startMethod.addThrownException(com.github.javaparser.ast.type.ClassOrInterfaceType.class
					.cast(StaticJavaParser.parseType("IOException")));
			StringBuilder docSb = new StringBuilder();
			docSb.append(
					"Starts the mock server.\n@param port The port to listen on.\n@throws IOException If an I/O error occurs.\n");
			appendPathJavadoc(docSb, path, pi, model);
			startMethod.setJavadocComment(docSb.toString());

			StringBuilder startBody = new StringBuilder();
			startBody.append("{\n");
			startBody.append("    server = HttpServer.create(new InetSocketAddress(port), 0);\n");

			String handlerPath = path.replaceAll("\\{[^}]+\\}", "");
			if (handlerPath.endsWith("/") && handlerPath.length() > 1) {
				handlerPath = handlerPath.substring(0, handlerPath.length() - 1);
			}

			Operation primaryOp = firstOperation(pi);
			int status = resolveStatusCode(primaryOp);
			String responsePayload = synthesizeMockPayload(primaryOp, model);
			String escapedResponse = responsePayload.replace("\\", "\\\\").replace("\"", "\\\"");

			startBody.append("    server.createContext(\"").append(handlerPath)
					.append("\", (HttpExchange exchange) -> {\n");
			startBody.append("        String response = \"").append(escapedResponse).append("\";\n");
			startBody.append("        exchange.sendResponseHeaders(").append(status).append(", response.length());\n");
			startBody.append("        try (OutputStream os = exchange.getResponseBody()) {\n");
			startBody.append("            os.write(response.getBytes());\n");
			startBody.append("        }\n");
			startBody.append("    });\n");
			startBody.append("    server.setExecutor(null);\n");
			startBody.append("    server.start();\n");
			startBody.append("    System.out.println(\"").append(className).append(" started on port \" + port);\n");
			startBody.append("}\n");
			startMethod.setBody(StaticJavaParser.parseBlock(startBody.toString()));

			MethodDeclaration stopMethod = classDecl.addMethod("stop",
					com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			stopMethod.setJavadocComment("Stops the mock server.");
			StringBuilder stopBody = new StringBuilder();
			stopBody.append("{\n");
			stopBody.append("    if (server != null) {\n");
			stopBody.append("        server.stop(0);\n");
			stopBody.append("    }\n");
			stopBody.append("}\n");
			stopMethod.setBody(StaticJavaParser.parseBlock(stopBody.toString()));

			files.put("mocks/" + className + ".java", cu.toString());
		}

		return files;
	}

	/**
	 * Appends {@code @mockRoute} Javadoc annotations for each operation on a
	 * {@link PathItem}.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param path
	 *            The URL path template.
	 * @param pi
	 *            The {@link PathItem}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void appendPathJavadoc(StringBuilder sb, String path, PathItem pi, OpenAPI model) {
		appendOpJavadoc(sb, "GET", path, pi.get);
		appendOpJavadoc(sb, "POST", path, pi.post);
		appendOpJavadoc(sb, "PUT", path, pi.put);
		appendOpJavadoc(sb, "DELETE", path, pi.delete);
		appendOpJavadoc(sb, "PATCH", path, pi.patch);
	}

	/**
	 * Appends a single {@code @mockRoute} Javadoc tag for an operation if non-null.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param verb
	 *            The HTTP verb.
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation}, or {@code null}.
	 */
	private static void appendOpJavadoc(StringBuilder sb, String verb, String path, Operation op) {
		if (op == null) {
			return;
		}
		String opId = (op.operationId != null && !op.operationId.isEmpty()) ? op.operationId : "-";
		int status = resolveStatusCode(op);
		String desc = (op.description != null && !op.description.isEmpty())
				? op.description.replace('\n', ' ')
				: "Generated from mock";
		sb.append(" * @mockRoute ").append(verb).append(" ").append(path).append(" ").append(opId).append(" ")
				.append(status).append(" ").append(desc).append("\n");
	}

	/**
	 * Returns the first available operation on a {@link PathItem}.
	 *
	 * @param pi
	 *            The {@link PathItem}.
	 * @return The first {@link Operation}, or {@code null}.
	 */
	private static Operation firstOperation(PathItem pi) {
		if (pi.get != null)
			return pi.get;
		if (pi.post != null)
			return pi.post;
		if (pi.put != null)
			return pi.put;
		if (pi.delete != null)
			return pi.delete;
		return pi.patch;
	}

	/**
	 * Resolves the primary HTTP status code for a mock operation response.
	 *
	 * @param op
	 *            The {@link Operation}.
	 * @return The HTTP status code (defaulting to {@code 200}).
	 */
	public static int resolveStatusCode(Operation op) {
		if (op != null && op.responses != null) {
			if (op.responses.statusCodes.containsKey("200"))
				return 200;
			if (op.responses.statusCodes.containsKey("201"))
				return 201;
			if (op.responses.statusCodes.containsKey("204"))
				return 204;
		}
		return 200;
	}

	/**
	 * Synthesizes a JSON mock payload from an operation's {@code 200}, {@code 201},
	 * or {@code default} response schema or example.
	 *
	 * @param op
	 *            The {@link Operation} to synthesize a payload for.
	 * @param model
	 *            The root {@link OpenAPI} model used to resolve {@code $ref}
	 *            schemas.
	 * @return A serialized JSON string representing the mock response body.
	 */
	public static String synthesizeMockPayload(Operation op, OpenAPI model) {
		if (op != null && op.responses != null) {
			openapi.ReferenceOr<Response> respObj = op.responses.defaultResponse;
			if (op.responses.statusCodes.containsKey("200")) {
				respObj = op.responses.statusCodes.get("200");
			} else if (op.responses.statusCodes.containsKey("201")) {
				respObj = op.responses.statusCodes.get("201");
			}
			if (respObj != null && respObj.value != null) {
				Response resp = respObj.value;
				if (resp.content != null) {
					openapi.MediaType mt = resp.content.get("application/json");
					if (mt.example != null) {
						return valueToJsonString(mt.example);
					}
					if (mt.schema != null) {
						return synthesizeSchemaValue(mt.schema.value, model).toString();
					}
				}
				if (resp.schema != null) {
					return synthesizeSchemaValue(resp.schema.value, model).toString();
				}
			}
		}
		return "{\"mock\": \"true\"}";
	}

	/**
	 * Synthesizes a mock JSON value ({@link JSONObject}, {@link JSONArray}, or
	 * scalar) from a {@link Schema}.
	 *
	 * @param schema
	 *            The {@link Schema} definition.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The synthesized value object.
	 */
	public static Object synthesizeSchemaValue(Schema schema, OpenAPI model) {
		Schema resolved = dereferenceSchema(schema, model);
		if (resolved.example != null) {
			return arazzo.Emit.serializePayloadValue(resolved.example);
		}
		if (resolved.enumValues != null) {
			return arazzo.Emit.serializePayloadValue(resolved.enumValues.get(0));
		}
		String typeStr = resolved.type != null ? resolved.type.toString() : "object";
		if ("array".equals(typeStr)) {
			JSONArray arr = new JSONArray();
			if (resolved.items != null) {
				arr.put(synthesizeSchemaValue(resolved.items.value, model));
			}
			return arr;
		}
		if ("string".equals(typeStr)) {
			return "string";
		}
		if ("integer".equals(typeStr)) {
			return 1;
		}
		if ("number".equals(typeStr)) {
			return 1.0;
		}
		if ("boolean".equals(typeStr)) {
			return true;
		}
		JSONObject obj = new JSONObject();
		if (resolved.properties != null) {
			for (java.util.Map.Entry<String, openapi.ReferenceOr<Schema>> propEntry : resolved.properties.entrySet()) {
				if (propEntry.getValue() != null) {
					obj.put(propEntry.getKey(), synthesizeSchemaValue(propEntry.getValue().value, model));
				}
			}
			return obj;
		}
		obj.put("mock", "true");
		return obj;
	}

	/**
	 * Dereferences a {@code $ref} schema against {@code model.components.schemas}
	 * if applicable.
	 *
	 * @param schema
	 *            The input {@link Schema}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The dereferenced {@link Schema}, or the original if not a reference.
	 */
	private static Schema dereferenceSchema(Schema schema, OpenAPI model) {
		if (schema.$ref != null && model != null && model.components != null) {
			String name = schema.$ref.substring("#/components/schemas/".length());
			openapi.ReferenceOr<Schema> target = model.components.schemas.get(name);
			if (target != null) {
				return target.value;
			}
		}
		return schema;
	}

	/**
	 * Converts an example value into a serialized JSON string.
	 *
	 * @param val
	 *            The example value.
	 * @return A JSON string.
	 */
	private static String valueToJsonString(Object val) {
		String s = String.valueOf(val).trim();
		if (s.startsWith("{")) {
			return s;
		}
		JSONObject obj = new JSONObject();
		obj.put("value", val);
		return obj.toString();
	}

	/**
	 * Extracts a PascalCase resource name from an OpenAPI URL path template.
	 *
	 * @param path
	 *            The URL path template.
	 * @return The extracted resource name, or {@code "Root"} if no static segments
	 *         exist.
	 */
	private static String getResourceName(String path) {
		String[] parts = path.split("/");
		for (String p : parts) {
			if (!p.isEmpty() && !p.startsWith("{")) {
				return p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Root";
	}

	/**
	 * Checks whether the given class declaration already contains a method with the
	 * specified name.
	 *
	 * @param classDecl
	 *            The {@link ClassOrInterfaceDeclaration} to inspect.
	 * @param name
	 *            The method name to look for.
	 * @return {@code true} if a method with the given name exists; {@code false}
	 *         otherwise.
	 */
	private static boolean hasMember(ClassOrInterfaceDeclaration classDecl, String name) {
		for (BodyDeclaration<?> member : classDecl.getMembers()) {
			if (member instanceof MethodDeclaration) {
				if (((MethodDeclaration) member).getNameAsString().equals(name))
					return true;
			}
		}
		return false;
	}
}
