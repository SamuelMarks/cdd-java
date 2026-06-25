package mocks;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

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
			/**
			 * Method.
			 */
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
				/**
				 * Method.
				 */
				sb.append("public void start(int port) throws IOException {\n");
				sb.append("    server = HttpServer.create(new InetSocketAddress(port), 0);\n");

				if (model.paths != null && model.paths.pathItems != null) {
					for (String path : model.paths.pathItems.keySet()) {
						String handlerPath = path.replaceAll("\\{[^}]+\\}", "");
						if (handlerPath.endsWith("/") && handlerPath.length() > 1) {
							handlerPath = handlerPath.substring(0, handlerPath.length() - 1);
						}

						sb.append("    server.createContext(\"").append(handlerPath)
								.append("\", (HttpExchange exchange) -> {\n");
						sb.append("        String response = \"{\\\"mock\\\": \\\"true\\\"}\";\n");
						sb.append("        exchange.sendResponseHeaders(200, response.length());\n");
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
			startMethod.setJavadocComment(
					"Starts the mock server.\n@param port The port to listen on.\n@throws IOException If an I/O error occurs.");

			StringBuilder startBody = new StringBuilder();
			startBody.append("{\n");
			startBody.append("    server = HttpServer.create(new InetSocketAddress(port), 0);\n");

			String handlerPath = path.replaceAll("\\{[^}]+\\}", "");
			if (handlerPath.endsWith("/") && handlerPath.length() > 1) {
				handlerPath = handlerPath.substring(0, handlerPath.length() - 1);
			}

			startBody.append("    server.createContext(\"").append(handlerPath)
					.append("\", (HttpExchange exchange) -> {\n");
			startBody.append("        String response = \"{\\\"mock\\\": \\\"true\\\"}\";\n");
			startBody.append("        exchange.sendResponseHeaders(200, response.length());\n");
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
	 * Generated JavaDoc.
	 */
	/**
	 * Generated JavaDoc.
	 *
	 * @param classDecl
	 *            param doc
	 * @param name
	 *            param doc
	 * @return return doc
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
