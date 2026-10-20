package routes;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;
import java.util.List;
import openapi.PathItem;
import openapi.Operation;
import openapi.Parameter;

/**
 * Emits routes to language source (HttpClient) preserving lexical layout.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Java code using java.net.http.HttpClient.
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
				: "ApiClient";
		if (title.isEmpty())
			title = "ApiClient";

		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = common.AstMergeUtils.parseLexicalPreserving(existingSource);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("import java.net.http.HttpClient;\n");
			sb.append("import java.net.http.HttpRequest;\n");
			sb.append("import java.net.http.HttpResponse;\n");
			sb.append("import java.net.URI;\n");
			sb.append("import java.io.IOException;\n");
			sb.append("import java.net.URLEncoder;\n");
			sb.append("import java.nio.charset.StandardCharsets;\n\n");
			sb.append(RouteJavadocEmitter.buildClassJavadoc(title, model));
			sb.append("public class ").append(title).append("Client {\n");
			sb.append("    private final HttpClient httpClient;\n");
			sb.append("    private final String baseUrl;\n\n");
			sb.append("    /**\n     * Constructor.\n     * @param baseUrl Base URL\n     */\n");
			sb.append("    public ").append(title).append("Client(String baseUrl) {\n");
			sb.append("        this.httpClient = HttpClient.newHttpClient();\n");
			sb.append("        this.baseUrl = baseUrl;\n");
			sb.append("    }\n\n");
			sb.append("}\n");
			cu = StaticJavaParser.parse(sb.toString());
			isNew = true;
		}
		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(title + "Client").orElse(null);

		if (classDecl != null) {
			if (!hasMember(classDecl, "mcpSse")) {
				StringBuilder sb = new StringBuilder();
				sb.append("/**\n * SSE Endpoint Generation\n */\n");
				sb.append(
						"public void mcpSse(String toolName, java.util.Map<String, Object> arguments, java.util.Map<String, String> headers) {\n");
				sb.append("    // Auto-generated SSE adapter for remote HTTP consumption.\n");
				sb.append("    if (headers != null && headers.containsKey(\"authorization\")) {\n");
				sb.append("        this.authorizationToken = headers.get(\"authorization\");\n");
				sb.append("    }\n");
				sb.append("    if (headers != null && headers.containsKey(\"Authorization\")) {\n");
				sb.append("        this.authorizationToken = headers.get(\"Authorization\");\n");
				sb.append("    }\n");
				if (model.paths != null && model.paths.pathItems != null) {
					for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
						String path = entry.getKey();
						PathItem item = entry.getValue();
						for (Map.Entry<openapi.HttpVerb, Operation> opEntry : item.getOperationsMap().entrySet()) {
							appendMcpSseCall(sb, opEntry.getKey().name(), path, opEntry.getValue(), item, model);
						}
					}
				}
				sb.append("}\n");
				classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
			}

			if (model.paths != null && model.paths.pathItems != null) {
				for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
					String path = entry.getKey();
					PathItem item = entry.getValue();
					Map<openapi.HttpVerb, Operation> opsMap = item.getOperationsMap();
					for (Map.Entry<openapi.HttpVerb, Operation> opEntry : opsMap.entrySet()) {
						emitMethodToAST(classDecl, opEntry.getKey().name(), path, opEntry.getValue(), item, model);
					}
					for (Operation itemOp : opsMap.values()) {
						if (itemOp.callbacks != null) {
							emitCallbacksToAST(classDecl, itemOp.callbacks);
						}
					}
				}
			}

			if (model.webhooks != null) {
				for (Map.Entry<String, PathItem> entry : model.webhooks.entrySet()) {
					String name = entry.getKey();
					PathItem item = entry.getValue();
					String safeName = name.replaceAll("[^a-zA-Z0-9_]", "");
					String interfaceName = safeName + "WebhookHandler";

					if (!hasMember(classDecl, interfaceName)) {
						StringBuilder sb = new StringBuilder();
						sb.append("public interface ").append(interfaceName).append(" {\n");
						appendOnPostMethod(sb, item);
						sb.append("}\n");
						classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
					}
				}
			}
		}

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Appends an {@code onPost(...)} handler method signature to a webhook or
	 * callback interface definition if {@code item.post} is present.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param item
	 *            The {@link PathItem} containing {@code post} and shared
	 *            {@code parameters}.
	 */
	private static void appendOnPostMethod(StringBuilder sb, PathItem item) {
		if (item.post != null) {
			sb.append("    void onPost(");
			boolean first = true;
			if (item.post.requestBody != null) {
				sb.append("String body");
				first = false;
			}
			if (item.parameters != null) {
				for (openapi.ReferenceOr<Parameter> o : item.parameters) {
					if (!first)
						sb.append(", ");
					String pName = o.value.name;
					if (!isNonEmpty(pName))
						pName = "param";
					sb.append("String ").append(pName.replaceAll("[^a-zA-Z0-9_]", ""));
					first = false;
				}
			}
			sb.append(");\n");
		}
	}

	/**
	 * Checks whether a string is non-null and non-empty.
	 *
	 * @param s
	 *            The string to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	private static boolean isNonEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	/**
	 * Appends a tool dispatch branch inside {@code mcpSse} for the given HTTP
	 * operation, forwarding all operation parameters and request body.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param httpMethod
	 *            The HTTP method verb (uppercase, e.g. {@code GET}, {@code PUT}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 * @param item
	 *            The parent {@link PathItem}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void appendMcpSseCall(StringBuilder sb, String httpMethod, String path, Operation op, PathItem item,
			OpenAPI model) {
		String tName = isNonEmpty(op.operationId)
				? op.operationId
				: httpMethod + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
		String methodName = isNonEmpty(op.operationId)
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		sb.append("    if (\"").append(tName).append("\".equals(toolName)) { ").append(methodName).append("(");
		boolean first = true;
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> pObj : op.parameters) {
				Parameter p = pObj.value;
				if (p.name == null || p.name.replaceAll("[^a-zA-Z0-9_]", "").isEmpty())
					continue;
				if (!first)
					sb.append(", ");
				sb.append("arguments.get(\"").append(p.name).append("\") != null ? arguments.get(\"").append(p.name)
						.append("\").toString() : null");
				first = false;
			}
		}
		if (op.requestBody != null) {
			if (!first)
				sb.append(", ");
			sb.append("arguments.get(\"requestBody\") != null ? arguments.get(\"requestBody\").toString() : null");
		}
		sb.append("); }\n");
	}

	/**
	 * Checks whether the given class declaration already contains a member (method
	 * or inner type) with the specified name.
	 *
	 * @param classDecl
	 *            The {@link ClassOrInterfaceDeclaration} to inspect.
	 * @param name
	 *            The member name to look for.
	 * @return {@code true} if a member with the given name exists; {@code false}
	 *         otherwise.
	 */
	private static boolean hasMember(ClassOrInterfaceDeclaration classDecl, String name) {
		for (BodyDeclaration<?> member : classDecl.getMembers()) {
			if (member instanceof ClassOrInterfaceDeclaration) {
				if (((ClassOrInterfaceDeclaration) member).getNameAsString().equals(name))
					return true;
			} else if (member instanceof MethodDeclaration) {
				if (((MethodDeclaration) member).getNameAsString().equals(name))
					return true;
			}
		}
		return false;
	}

	/**
	 * Emits callback handler interfaces into the client class declaration for all
	 * callbacks in an operation.
	 *
	 * @param classDecl
	 *            The target {@link ClassOrInterfaceDeclaration}.
	 * @param callbacks
	 *            The map of callback names to {@link openapi.Callback} definitions.
	 */
	private static void emitCallbacksToAST(ClassOrInterfaceDeclaration classDecl,
			Map<String, openapi.ReferenceOr<openapi.Callback>> callbacks) {
		for (Map.Entry<String, openapi.ReferenceOr<openapi.Callback>> cbEntry : callbacks.entrySet()) {
			openapi.Callback cb = cbEntry.getValue().value;
			String safeName = cbEntry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			String interfaceName = safeName + "CallbackHandler";

			if (!hasMember(classDecl, interfaceName)) {
				StringBuilder sb = new StringBuilder();
				sb.append("public interface ").append(interfaceName).append(" {\n");
				for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
					appendOnPostMethod(sb, piEntry.getValue());
				}
				sb.append("}\n");
				classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
			}
		}
	}

	/**
	 * Emits an HTTP route method with full OpenAPI Javadoc metadata into the client
	 * class AST.
	 *
	 * @param classDecl
	 *            The target {@link ClassOrInterfaceDeclaration}.
	 * @param httpMethod
	 *            The HTTP method verb (uppercase, e.g. {@code GET}, {@code POST}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 * @param item
	 *            The parent {@link openapi.PathItem} containing shared parameters
	 *            and metadata.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void emitMethodToAST(ClassOrInterfaceDeclaration classDecl, String httpMethod, String path,
			Operation op, openapi.PathItem item, OpenAPI model) {
		String methodName = op.operationId;
		if (!isNonEmpty(methodName)) {
			methodName = httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", "");
		} else {
			methodName = methodName.replaceAll("[^a-zA-Z0-9_]", "");
		}

		if (hasMember(classDecl, methodName)) {
			return; // Preserve existing custom implementation
		}

		String processedPath = path.replaceAll("\\{([^}]+)\\}", "\"+ $1 +\"");
		if (processedPath.endsWith("+\"\"")) {
			processedPath = processedPath.substring(0, processedPath.length() - 3);
		}

		StringBuilder argsBuilder = new StringBuilder();
		StringBuilder docBuilder = new StringBuilder();

		docBuilder.append("/**\n");
		if (isNonEmpty(op.summary)) {
			docBuilder.append(" * ").append(op.summary.replace("\n", "\n * ")).append("\n");
		}
		if (isNonEmpty(op.description)) {
			docBuilder.append(" * ").append(op.description.replace("\n", "\n * ")).append("\n");
		}

		java.util.List<Parameter> allParams = new java.util.ArrayList<>();
		if (item.parameters != null) {
			for (openapi.ReferenceOr<Parameter> po : item.parameters) {
				allParams.add(po.value);
			}
		}
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> po : op.parameters) {
				Parameter p = po.value;
				boolean exists = false;
				for (Parameter e : allParams) {
					if (java.util.Objects.equals(e.name, p.name) && java.util.Objects.equals(e.in, p.in)) {
						exists = true;
						break;
					}
				}
				if (!exists)
					allParams.add(p);
			}
		}

		java.util.List<openapi.SecurityRequirement> secReqs = op.security != null ? op.security : model.security;
		if (secReqs != null && model.components != null && model.components.securitySchemes != null) {
			for (openapi.SecurityRequirement req : secReqs) {
				if (req.requirements != null) {
					for (String secName : req.requirements.keySet()) {
						openapi.ReferenceOr<openapi.SecurityScheme> schemeRefOr = model.components.securitySchemes
								.get(secName);
						if (schemeRefOr != null) {
							openapi.SecurityScheme scheme = schemeRefOr.value;
							if ("apiKey".equals(scheme.type) && scheme.name != null && scheme.in != null) {
								addSecurityParamIfAbsent(allParams, scheme.name, scheme.in,
										"Security credential for " + secName);
							} else if ("http".equals(scheme.type) && "bearer".equals(scheme.scheme)) {
								addSecurityParamIfAbsent(allParams, "Authorization", "header",
										"Bearer token for " + secName + " (prefix with 'Bearer ')");
							} else if ("http".equals(scheme.type) && "basic".equals(scheme.scheme)) {
								addSecurityParamIfAbsent(allParams, "Authorization", "header",
										"Basic auth for " + secName + " (prefix with 'Basic ')");
							}
						}
					}
				}
			}
		}

		boolean hasQuery = false;
		boolean hasHeader = false;

		for (Parameter p : allParams) {
			if (p.name == null)
				continue;
			String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
			if (safeName.isEmpty())
				continue;

			if (argsBuilder.length() > 0)
				argsBuilder.append(", ");
			argsBuilder.append("String ").append(safeName);
			docBuilder.append(" * @param ").append(safeName).append(" ")
					.append(p.description != null ? p.description : p.name).append("\n");

			if ("query".equals(p.in))
				hasQuery = true;
			if ("header".equals(p.in))
				hasHeader = true;
		}

		boolean hasBody = false;
		if (op.requestBody != null) {
			hasBody = true;
			if (argsBuilder.length() > 0)
				argsBuilder.append(", ");
			argsBuilder.append("String requestBody");
			docBuilder.append(" * @param requestBody The request body payload\n");
		}

		RouteJavadocEmitter.appendOperationJavadocTags(docBuilder, path, op, item);

		StringBuilder sb = new StringBuilder();
		sb.append(docBuilder.toString());
		sb.append("public HttpResponse<String> ").append(methodName).append("(").append(argsBuilder.toString())
				.append(") throws IOException, InterruptedException {\n");
		sb.append("    StringBuilder uriBuilder = new StringBuilder(baseUrl + \"").append(processedPath)
				.append("\");\n");

		if (hasQuery) {
			sb.append("    boolean firstQuery = true;\n");
			for (Parameter p : allParams) {
				if ("query".equals(p.in)) {
					String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
					sb.append("    if (").append(safeName).append(" != null) {\n");
					sb.append("        uriBuilder.append(firstQuery ? \"?\" : \"&\");\n");
					sb.append("        uriBuilder.append(\"").append(p.name).append("=\").append(URLEncoder.encode(")
							.append(safeName).append(", StandardCharsets.UTF_8));\n");
					sb.append("        firstQuery = false;\n");
					sb.append("    }\n");
				}
			}
		}

		sb.append("    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()\n");
		sb.append("        .uri(URI.create(uriBuilder.toString()));\n");

		if (hasHeader) {
			for (Parameter p : allParams) {
				if ("header".equals(p.in)) {
					String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
					sb.append("    if (").append(safeName).append(" != null) {\n");
					sb.append("        requestBuilder.header(\"").append(p.name).append("\", ").append(safeName)
							.append(");\n");
					sb.append("    }\n");
				}
			}
		}

		String bodyPublisher = hasBody
				? "HttpRequest.BodyPublishers.ofString(requestBody)"
				: "HttpRequest.BodyPublishers.noBody()";

		if (httpMethod.equals("GET") || httpMethod.equals("DELETE")) {
			sb.append("    requestBuilder.").append(httpMethod).append("();\n");
		} else if (httpMethod.equals("QUERY") || httpMethod.equals("PATCH") || httpMethod.equals("OPTIONS")
				|| httpMethod.equals("HEAD") || httpMethod.equals("TRACE")) {
			sb.append("    requestBuilder.method(\"").append(httpMethod).append("\", ").append(bodyPublisher)
					.append(");\n");
		} else {
			sb.append("    requestBuilder.").append(httpMethod).append("(").append(bodyPublisher).append(");\n");
		}

		sb.append("    return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());\n");
		sb.append("}\n");

		classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
	}

	/**
	 * Emits the SDK HTTP client class (including MCP adapter methods and tools)
	 * into the provided compilation unit.
	 *
	 * @param cu
	 *            The target {@link CompilationUnit}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	public static void emitSdkClientClass(CompilationUnit cu, OpenAPI model) {
		String title = (model.info != null && model.info.title != null)
				? model.info.title.replaceAll("[^a-zA-Z0-9]", "")
				: "Api";
		if (title.isEmpty())
			title = "Api";
		String clientClass = title + "Client";
		ClassOrInterfaceDeclaration clientDecl = cu.getClassByName(clientClass).orElse(null);
		if (clientDecl == null) {
			clientDecl = cu.addClass(clientClass);
			clientDecl.setModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC, false);
			clientDecl.addField("String", "baseUrl", com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
			clientDecl.addField("HttpClient", "httpClient", com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
			clientDecl.addField("String", "authorizationToken", com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			clientDecl.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
					.addParameter("String", "baseUrl").setBody(StaticJavaParser.parseBlock(
							"{ this.baseUrl = baseUrl; this.httpClient = HttpClient.newHttpClient(); this.mcp = new McpAdapter(); }"));
			ClassOrInterfaceDeclaration mcpAdapter = classes.McpEmitter.createMcpAdapter(clientDecl);
			com.github.javaparser.ast.body.MethodDeclaration getTools = mcpAdapter.addMethod("getTools",
					com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			getTools.setType("java.util.List<Tool>");
			StringBuilder toolsBody = new StringBuilder();
			toolsBody.append("{\n");
			toolsBody.append("  java.util.List<Tool> tools = new java.util.ArrayList<>();\n");
			com.github.javaparser.ast.body.MethodDeclaration callTool = mcpAdapter.addMethod("callTool",
					com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			callTool.setType("CallToolResult");
			callTool.addParameter("String", "name");
			callTool.addParameter("java.util.Map<String, Object>", "arguments");
			callTool.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
			StringBuilder mcpBody = new StringBuilder();
			mcpBody.append("{\n");
			mcpBody.append("  CallToolResult result = new CallToolResult();\n");
			mcpBody.append("  java.util.List<TextContent> contentArr = new java.util.ArrayList<>();\n");
			mcpBody.append("  TextContent content = new TextContent();\n");
			mcpBody.append("  content.type = \"text\";\n");
			if (model.paths != null && model.paths.pathItems != null) {
				for (Map.Entry<String, openapi.PathItem> entry : model.paths.pathItems.entrySet()) {
					String path = entry.getKey();
					openapi.PathItem pi = entry.getValue();
					if (pi.get != null) {
						emitSdkClientMethod(clientDecl, "GET", path, pi.get);
						appendMcpToolCall(mcpBody, "GET", path, pi.get, clientClass);
						appendMcpToolSchema(toolsBody, "GET", path, pi.get);
					}
					if (pi.post != null) {
						emitSdkClientMethod(clientDecl, "POST", path, pi.post);
						appendMcpToolCall(mcpBody, "POST", path, pi.post, clientClass);
						appendMcpToolSchema(toolsBody, "POST", path, pi.post);
					}
					if (pi.put != null) {
						emitSdkClientMethod(clientDecl, "PUT", path, pi.put);
						appendMcpToolCall(mcpBody, "PUT", path, pi.put, clientClass);
						appendMcpToolSchema(toolsBody, "PUT", path, pi.put);
					}
					if (pi.delete != null) {
						emitSdkClientMethod(clientDecl, "DELETE", path, pi.delete);
						appendMcpToolCall(mcpBody, "DELETE", path, pi.delete, clientClass);
						appendMcpToolSchema(toolsBody, "DELETE", path, pi.delete);
					}
				}
			}
			mcpBody.append("  throw new IllegalArgumentException(\"Unknown tool: \" + name);\n");
			mcpBody.append("}\n");
			callTool.setBody(StaticJavaParser.parseBlock(mcpBody.toString()));
			toolsBody.append("  return tools;\n");
			toolsBody.append("}\n");
			getTools.setBody(StaticJavaParser.parseBlock(toolsBody.toString()));
			com.github.javaparser.ast.body.MethodDeclaration getResources = mcpAdapter.addMethod("getResources",
					com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
			getResources.setType("java.util.List<Resource>");
			StringBuilder resBody = new StringBuilder();
			resBody.append("{\n");
			resBody.append("  java.util.List<Resource> resources = new java.util.ArrayList<>();\n");
			resBody.append("  Resource res = new Resource();\n");
			resBody.append("  res.uri = \"cdd://openapi/spec\";\n");
			resBody.append("  res.name = \"OpenAPI Specification\";\n");
			resBody.append("  res.mimeType = \"application/json\";\n");
			resBody.append("  resources.add(res);\n");
			resBody.append("  return resources;\n");
			resBody.append("}\n");
			getResources.setBody(StaticJavaParser.parseBlock(resBody.toString()));
			clientDecl.addField("McpAdapter", "mcp", com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
		}
	}

	/**
	 * Appends an MCP tool schema definition to the generated {@code getTools}
	 * method body.
	 *
	 * @param body
	 *            The target {@link StringBuilder}.
	 * @param method
	 *            The HTTP method (uppercase, e.g. {@code GET}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link openapi.Operation} definition.
	 */
	private static void appendMcpToolSchema(StringBuilder body, String method, String path, openapi.Operation op) {
		String toolName = op.operationId != null ? op.operationId : method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
		String desc = op.summary != null ? op.summary : (op.description != null ? op.description : "");
		body.append("  {\n");
		body.append("    Tool tool = new Tool();\n");
		body.append("    tool.name = \"").append(escape(toolName)).append("\";\n");
		body.append("    tool.description = \"").append(escape(desc)).append("\";\n");
		body.append("    java.util.Map<String, Object> inputSchema = new java.util.HashMap<>();\n");
		body.append("    inputSchema.put(\"type\", \"object\");\n");
		body.append("    java.util.Map<String, Object> properties = new java.util.HashMap<>();\n");
		body.append("    java.util.List<String> required = new java.util.ArrayList<>();\n");
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> pObj : op.parameters) {
				openapi.Parameter p = pObj.value;
				appendMcpProperty(body, p.name, p.description, p.required);
			}
		}
		if (op.requestBody != null) {
			openapi.RequestBody rb = op.requestBody.value;
			appendMcpProperty(body, "requestBody", rb != null ? rb.description : null, rb != null ? rb.required : null);
		}
		body.append("    inputSchema.put(\"properties\", properties);\n");
		body.append("    inputSchema.put(\"required\", required);\n");
		body.append("    tool.inputSchema = inputSchema;\n");
		body.append("    tools.add(tool);\n");
		body.append("  }\n");
	}

	/**
	 * Appends a single property entry to an MCP tool's {@code inputSchema}.
	 *
	 * @param body
	 *            The target {@link StringBuilder}.
	 * @param name
	 *            The property name.
	 * @param description
	 *            The optional property description.
	 * @param required
	 *            Whether the property is required.
	 */
	private static void appendMcpProperty(StringBuilder body, String name, String description, Boolean required) {
		body.append("    {\n");
		body.append("      java.util.Map<String, Object> prop = new java.util.HashMap<>();\n");
		body.append("      prop.put(\"type\", \"string\");\n");
		if (description != null) {
			body.append("      prop.put(\"description\", \"").append(escape(description)).append("\");\n");
		}
		body.append("      properties.put(\"").append(escape(name)).append("\", prop);\n");
		if (Boolean.TRUE.equals(required)) {
			body.append("      required.add(\"").append(escape(name)).append("\");\n");
		}
		body.append("    }\n");
	}

	/**
	 * Appends an MCP {@code callTool} dispatch branch for an OpenAPI operation.
	 *
	 * @param body
	 *            The target {@link StringBuilder}.
	 * @param method
	 *            The HTTP method (uppercase, e.g. {@code GET}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link openapi.Operation} definition.
	 * @param clientClassName
	 *            The outer SDK client class name.
	 */
	private static void appendMcpToolCall(StringBuilder body, String method, String path, openapi.Operation op,
			String clientClassName) {
		String toolName = op.operationId != null ? op.operationId : method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
		String methodName = op.operationId != null
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (method.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		if (methodName.isEmpty())
			methodName = "method";
		body.append("  if (\"").append(toolName).append("\".equals(name)) {\n");
		body.append("    HttpResponse<String> res = ").append(clientClassName).append(".this.").append(methodName)
				.append("(");
		boolean first = true;
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> po : op.parameters) {
				openapi.Parameter p = po.value;
				if (!first)
					body.append(", ");
				body.append("arguments.get(\"").append(p.name).append("\") != null ? arguments.get(\"").append(p.name)
						.append("\").toString() : null");
				first = false;
			}
		}
		boolean hasBody = (op.requestBody != null);
		if (hasBody) {
			if (!first)
				body.append(", ");
			body.append("arguments.get(\"requestBody\") != null ? arguments.get(\"requestBody\").toString() : null");
		}
		body.append(");\n");
		body.append("    content.text = res.body();\n");
		body.append("    contentArr.add(content);\n");
		body.append("    result.content = contentArr;\n");
		body.append("    return result;\n");
		body.append("  }\n");
	}

	/**
	 * Emits an HTTP client method on the generated SDK client class for an OpenAPI
	 * operation.
	 *
	 * @param classDecl
	 *            The target {@link ClassOrInterfaceDeclaration}.
	 * @param method
	 *            The HTTP method (uppercase, e.g. {@code GET}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link openapi.Operation} definition.
	 */
	private static void emitSdkClientMethod(ClassOrInterfaceDeclaration classDecl, String method, String path,
			openapi.Operation op) {
		String methodName = op.operationId != null
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (method.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		if (methodName.isEmpty())
			methodName = "method";
		com.github.javaparser.ast.body.MethodDeclaration md = classDecl.addMethod(methodName,
				com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
		md.setType("HttpResponse<String>");
		md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
		String resolvedPath = path;
		if (op.parameters != null) {
			int pIdx = 0;
			for (openapi.ReferenceOr<Parameter> po : op.parameters) {
				openapi.Parameter p = po.value;
				String pName = p.name != null ? p.name.replaceAll("[^a-zA-Z0-9_]", "") : ("p" + pIdx);
				md.addParameter("String", pName);
				if ("path".equals(p.in)) {
					resolvedPath = resolvedPath.replace("{" + p.name + "}", "\" + " + pName + " + \"");
				} else if ("query".equals(p.in)) {
					if (!resolvedPath.contains("?")) {
						resolvedPath += "?" + p.name + "=\" + " + pName + " + \"";
					} else {
						resolvedPath += "&" + p.name + "=\" + " + pName + " + \"";
					}
				}
				pIdx++;
			}
		}
		StringBuilder body = new StringBuilder();
		body.append("{\n");
		body.append("  HttpRequest.Builder builder = HttpRequest.newBuilder()\n");
		body.append("      .uri(URI.create(this.baseUrl + \"").append(resolvedPath).append("\"));\n");
		body.append("  builder.header(\"Accept\", \"application/json\");\n");
		body.append("  if (this.authorizationToken != null) {\n");
		body.append("      builder.header(\"Authorization\", this.authorizationToken);\n");
		body.append("  }\n");
		boolean hasBody = (op.requestBody != null);
		if (hasBody) {
			md.addParameter("String", "requestBody");
			body.append("  builder.header(\"Content-Type\", \"application/json\");\n");
			body.append("  builder.method(\"").append(method)
					.append("\", HttpRequest.BodyPublishers.ofString(requestBody));\n");
		} else {
			body.append("  builder.method(\"").append(method).append("\", HttpRequest.BodyPublishers.noBody());\n");
		}
		body.append("  return this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());\n");
		body.append("}\n");
		md.setBody(StaticJavaParser.parseBlock(body.toString()));
	}

	/**
	 * Adds a synthetic security credential parameter to {@code allParams} if no
	 * parameter with the same name is already present.
	 *
	 * @param allParams
	 *            The target parameter list.
	 * @param name
	 *            The parameter name.
	 * @param in
	 *            The parameter location ({@code "header"}, {@code "query"}, etc.).
	 * @param description
	 *            The parameter description.
	 */
	private static void addSecurityParamIfAbsent(java.util.List<Parameter> allParams, String name, String in,
			String description) {
		for (Parameter e : allParams) {
			if (name.equals(e.name)) {
				return;
			}
		}
		Parameter sp = new Parameter();
		sp.name = name;
		sp.in = in;
		sp.description = description;
		allParams.add(sp);
	}

	/**
	 * Escapes backslashes, double quotes, and newlines for Java string literals.
	 *
	 * @param s
	 *            The input string to escape.
	 * @return The escaped string (or empty string if {@code s} is {@code null}).
	 */
	private static String escape(String s) {
		return openapi.TypeMapper.escape(s);
	}
}
