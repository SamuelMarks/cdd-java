package classes;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

/**
 * Emits Model Context Protocol (MCP) schema AST definitions onto generated SDK
 * client classes.
 */
public class McpEmitter {

	/**
	 * Default constructor.
	 */
	public McpEmitter() {
	}

	/**
	 * Adds a public static nested class to {@code mcpAdapter}.
	 *
	 * @param mcpAdapter
	 *            The enclosing {@code McpAdapter} class declaration.
	 * @param name
	 *            The nested class name.
	 * @param superClass
	 *            Optional superclass name, or {@code null}.
	 * @param fields
	 *            Alternating pairs of {@code (fieldType, fieldName)} strings.
	 * @return The created {@link ClassOrInterfaceDeclaration}.
	 */
	private static ClassOrInterfaceDeclaration addMcpClass(ClassOrInterfaceDeclaration mcpAdapter, String name,
			String superClass, String... fields) {
		ClassOrInterfaceDeclaration cls = new ClassOrInterfaceDeclaration();
		cls.setName(name);
		cls.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		if (superClass != null) {
			cls.addExtendedType(superClass);
		}
		for (int i = 0; i < fields.length; i += 2) {
			String fieldType = fields[i];
			String fieldName = fields[i + 1];
			FieldDeclaration fd = cls.addField(fieldType, fieldName, Modifier.Keyword.PUBLIC);
			fd.addAnnotation(new SingleMemberAnnotationExpr(new com.github.javaparser.ast.expr.Name("JsonProperty"),
					new StringLiteralExpr(fieldName)));
		}
		mcpAdapter.addMember(cls);
		return cls;
	}

	/**
	 * Creates and attaches the {@code McpAdapter} inner class containing all MCP
	 * protocol schema DTOs to the given client class declaration.
	 *
	 * @param clientDecl
	 *            The target SDK client {@link ClassOrInterfaceDeclaration}.
	 * @return The created {@code McpAdapter} {@link ClassOrInterfaceDeclaration}.
	 */
	public static ClassOrInterfaceDeclaration createMcpAdapter(ClassOrInterfaceDeclaration clientDecl) {
		ClassOrInterfaceDeclaration mcpAdapter = clientDecl.addMember(
				new ClassOrInterfaceDeclaration().setName("McpAdapter").setModifier(Modifier.Keyword.PUBLIC, false));

		addMcpClass(mcpAdapter, "ModelHint", null, "String", "name");
		addMcpClass(mcpAdapter, "ModelPreferences", null, "java.util.List<ModelHint>", "hints", "Double",
				"costPriority", "Double", "speedPriority", "Double", "intelligencePriority");
		addMcpClass(mcpAdapter, "LoggingLevel", null);
		addMcpClass(mcpAdapter, "Tool", null, "String", "name", "String", "description",
				"java.util.Map<String, Object>", "inputSchema");
		addMcpClass(mcpAdapter, "TextContent", "Annotated", "String", "type", "String", "text");
		addMcpClass(mcpAdapter, "TextResourceContents", null, "String", "uri", "String", "mimeType", "String", "text");
		addMcpClass(mcpAdapter, "ResourceContents", null, "String", "uri", "String", "mimeType");
		addMcpClass(mcpAdapter, "ListToolsRequest", "PaginatedRequest");
		addMcpClass(mcpAdapter, "ListToolsResult", "PaginatedResult", "java.util.List<Tool>", "tools");
		addMcpClass(mcpAdapter, "RequestId", null);
		addMcpClass(mcpAdapter, "Role", null);
		addMcpClass(mcpAdapter, "SamplingMessage", null, "String", "role", "java.util.Map<String, Object>", "content");
		addMcpClass(mcpAdapter, "ClientInfo", null, "String", "name", "String", "version");
		addMcpClass(mcpAdapter, "CallToolResult", null, "java.util.List<TextContent>", "content", "Boolean", "isError",
				"java.util.Map<String, Object>", "_meta");
		addMcpClass(mcpAdapter, "CallToolRequestParams", null, "String", "name", "java.util.Map<String, Object>",
				"arguments");
		addMcpClass(mcpAdapter, "CallToolRequest", null, "String", "method", "CallToolRequestParams", "params");
		addMcpClass(mcpAdapter, "JSONRPCRequest", "JSONRPCMessage", "Object", "id", "String", "method",
				"java.util.Map<String, Object>", "params");
		addMcpClass(mcpAdapter, "JSONRPCResponse", "JSONRPCMessage", "Object", "id", "Object", "result");
		addMcpClass(mcpAdapter, "JSONRPCErrorError", null, "Integer", "code", "String", "message", "Object", "data");
		addMcpClass(mcpAdapter, "JSONRPCError", "JSONRPCMessage", "Object", "id", "JSONRPCErrorError", "error");
		addMcpClass(mcpAdapter, "JSONRPCNotification", "JSONRPCMessage", "String", "method",
				"java.util.Map<String, Object>", "params");

		ClassOrInterfaceDeclaration jsonRpcMsg = addMcpClass(mcpAdapter, "JSONRPCMessage", null);
		FieldDeclaration jsonRpcField = jsonRpcMsg.addFieldWithInitializer("String", "jsonrpc",
				new StringLiteralExpr("2.0"), Modifier.Keyword.PUBLIC);
		jsonRpcField.addAnnotation(new SingleMemberAnnotationExpr(
				new com.github.javaparser.ast.expr.Name("JsonProperty"), new StringLiteralExpr("jsonrpc")));

		addMcpClass(mcpAdapter, "Request", "JSONRPCRequest");
		addMcpClass(mcpAdapter, "Result", null, "java.util.Map<String, Object>", "_meta");
		addMcpClass(mcpAdapter, "EmptyResult", "Result");
		addMcpClass(mcpAdapter, "ClientRequest", "Request");
		addMcpClass(mcpAdapter, "ClientResult", "Result");
		addMcpClass(mcpAdapter, "ClientNotification", "JSONRPCNotification");
		addMcpClass(mcpAdapter, "ServerRequest", "Request");
		addMcpClass(mcpAdapter, "ServerResult", "Result");
		addMcpClass(mcpAdapter, "ServerNotification", "JSONRPCNotification");
		addMcpClass(mcpAdapter, "CancelledNotificationParams", null, "Object", "requestId", "String", "reason");
		addMcpClass(mcpAdapter, "CancelledNotification", "Notification", "CancelledNotificationParams", "params");
		addMcpClass(mcpAdapter, "ProgressNotificationParams", null, "Object", "progressToken", "Double", "progress",
				"Double", "total");
		addMcpClass(mcpAdapter, "ProgressNotification", "Notification", "ProgressNotificationParams", "params");
		addMcpClass(mcpAdapter, "ClientCapabilitiesRoots", null, "Boolean", "listChanged");
		addMcpClass(mcpAdapter, "ClientCapabilities", null, "java.util.Map<String, Object>", "experimental",
				"ClientCapabilitiesRoots", "roots", "java.util.Map<String, Object>", "sampling");
		addMcpClass(mcpAdapter, "ServerCapabilitiesPrompts", null, "Boolean", "listChanged");
		addMcpClass(mcpAdapter, "ServerCapabilitiesResources", null, "Boolean", "listChanged", "Boolean", "subscribe");
		addMcpClass(mcpAdapter, "ServerCapabilitiesTools", null, "Boolean", "listChanged");
		addMcpClass(mcpAdapter, "ServerCapabilities", null, "java.util.Map<String, Object>", "experimental",
				"java.util.Map<String, Object>", "logging", "ServerCapabilitiesPrompts", "prompts",
				"ServerCapabilitiesResources", "resources", "ServerCapabilitiesTools", "tools");
		addMcpClass(mcpAdapter, "Implementation", null, "String", "name", "String", "version");
		addMcpClass(mcpAdapter, "InitializeRequestParams", null, "String", "protocolVersion", "ClientCapabilities",
				"capabilities", "Implementation", "clientInfo");
		addMcpClass(mcpAdapter, "InitializeRequest", "Request", "InitializeRequestParams", "params");
		addMcpClass(mcpAdapter, "InitializeResult", "Result", "String", "protocolVersion", "ServerCapabilities",
				"capabilities", "Implementation", "serverInfo", "String", "instructions");
		addMcpClass(mcpAdapter, "InitializedNotificationParams", null, "java.util.Map<String, Object>", "_meta");
		addMcpClass(mcpAdapter, "InitializedNotification", "Notification", "InitializedNotificationParams", "params");
		addMcpClass(mcpAdapter, "Notification", "JSONRPCNotification", "java.util.Map<String, Object>", "params");
		addMcpClass(mcpAdapter, "Resource", null, "String", "uri", "String", "name", "String", "description", "String",
				"mimeType");
		addMcpClass(mcpAdapter, "PaginatedRequestParams", null, "String", "cursor");
		addMcpClass(mcpAdapter, "PaginatedRequest", "Request", "PaginatedRequestParams", "params");
		addMcpClass(mcpAdapter, "PaginatedResult", "Result", "String", "nextCursor");
		addMcpClass(mcpAdapter, "ListResourcesRequest", "PaginatedRequest");
		addMcpClass(mcpAdapter, "ListResourcesResult", "PaginatedResult", "java.util.List<Resource>", "resources");
		addMcpClass(mcpAdapter, "ReadResourceRequestParams", null, "String", "uri");
		addMcpClass(mcpAdapter, "ReadResourceRequest", "Request", "ReadResourceRequestParams", "params");
		addMcpClass(mcpAdapter, "ReadResourceResult", "Result", "java.util.List<java.util.Map<String, Object>>",
				"contents");
		addMcpClass(mcpAdapter, "ResourceReference", null, "String", "type", "String", "uri");
		addMcpClass(mcpAdapter, "PromptReference", null, "String", "type", "String", "name");
		addMcpClass(mcpAdapter, "AnnotatedAnnotations", null, "java.util.List<String>", "audience", "Double",
				"priority");
		addMcpClass(mcpAdapter, "Annotated", null, "AnnotatedAnnotations", "annotations");
		addMcpClass(mcpAdapter, "ImageContent", "Annotated", "String", "type", "String", "data", "String", "mimeType");
		addMcpClass(mcpAdapter, "BlobResourceContents", null, "String", "uri", "String", "mimeType", "String", "blob");
		addMcpClass(mcpAdapter, "EmbeddedResource", "Annotated", "String", "type", "java.util.Map<String, Object>",
				"resource");
		addMcpClass(mcpAdapter, "ProgressToken", null);
		addMcpClass(mcpAdapter, "Cursor", null);
		addMcpClass(mcpAdapter, "ResourceTemplate", null, "String", "uriTemplate", "String", "name", "String",
				"description", "String", "mimeType");
		addMcpClass(mcpAdapter, "ListResourceTemplatesRequest", "PaginatedRequest");
		addMcpClass(mcpAdapter, "ListResourceTemplatesResult", "PaginatedResult", "java.util.List<ResourceTemplate>",
				"resourceTemplates");
		addMcpClass(mcpAdapter, "ResourceUpdatedNotificationParams", null, "String", "uri");
		addMcpClass(mcpAdapter, "ResourceUpdatedNotification", "Notification", "ResourceUpdatedNotificationParams",
				"params");
		addMcpClass(mcpAdapter, "ResourceListChangedNotification", "Notification");
		addMcpClass(mcpAdapter, "SubscribeRequestParams", null, "String", "uri");
		addMcpClass(mcpAdapter, "SubscribeRequest", "Request", "SubscribeRequestParams", "params");
		addMcpClass(mcpAdapter, "UnsubscribeRequestParams", null, "String", "uri");
		addMcpClass(mcpAdapter, "UnsubscribeRequest", "Request", "UnsubscribeRequestParams", "params");
		addMcpClass(mcpAdapter, "Prompt", null, "String", "name", "String", "description",
				"java.util.List<java.util.Map<String, Object>>", "arguments");
		addMcpClass(mcpAdapter, "PromptArgument", null, "String", "name", "String", "description", "Boolean",
				"required");
		addMcpClass(mcpAdapter, "ListPromptsRequest", "PaginatedRequest");
		addMcpClass(mcpAdapter, "ListPromptsResult", "PaginatedResult", "java.util.List<Prompt>", "prompts");
		addMcpClass(mcpAdapter, "GetPromptRequestParams", null, "String", "name", "java.util.Map<String, String>",
				"arguments");
		addMcpClass(mcpAdapter, "GetPromptRequest", "Request", "GetPromptRequestParams", "params");
		addMcpClass(mcpAdapter, "GetPromptResult", "Result", "String", "description",
				"java.util.List<java.util.Map<String, Object>>", "messages");
		addMcpClass(mcpAdapter, "PromptMessage", null, "String", "role", "java.util.Map<String, Object>", "content");
		addMcpClass(mcpAdapter, "PromptListChangedNotification", "Notification");
		addMcpClass(mcpAdapter, "ToolListChangedNotification", "Notification");
		addMcpClass(mcpAdapter, "LoggingMessageNotificationParams", null, "String", "level", "String", "logger",
				"Object", "data");
		addMcpClass(mcpAdapter, "LoggingMessageNotification", "Notification", "LoggingMessageNotificationParams",
				"params");
		addMcpClass(mcpAdapter, "SetLevelRequestParams", null, "String", "level");
		addMcpClass(mcpAdapter, "SetLevelRequest", "Request", "SetLevelRequestParams", "params");
		addMcpClass(mcpAdapter, "PingRequest", "Request");
		addMcpClass(mcpAdapter, "CreateMessageRequestParams", null, "java.util.List<java.util.Map<String, Object>>",
				"messages", "java.util.Map<String, Object>", "modelPreferences", "String", "systemPrompt", "String",
				"includeContext", "Double", "temperature", "Integer", "maxTokens", "java.util.List<String>",
				"stopSequences", "java.util.Map<String, Object>", "metadata");
		addMcpClass(mcpAdapter, "CreateMessageRequest", "Request", "CreateMessageRequestParams", "params");
		addMcpClass(mcpAdapter, "CreateMessageResult", "Result", "String", "role", "java.util.List<TextContent>",
				"content", "String", "model", "String", "stopReason");
		addMcpClass(mcpAdapter, "CompleteRequestParams", null, "java.util.Map<String, Object>", "ref",
				"java.util.Map<String, Object>", "argument");
		addMcpClass(mcpAdapter, "CompleteRequest", "Request", "CompleteRequestParams", "params");
		addMcpClass(mcpAdapter, "CompleteResultCompletion", null, "java.util.List<String>", "values", "Integer",
				"total", "Boolean", "hasMore");
		addMcpClass(mcpAdapter, "CompleteResult", "Result", "CompleteResultCompletion", "completion");
		addMcpClass(mcpAdapter, "Root", null, "String", "uri", "String", "name");
		addMcpClass(mcpAdapter, "ListRootsRequest", "Request");
		addMcpClass(mcpAdapter, "ListRootsResult", "Result", "java.util.List<Root>", "roots");
		addMcpClass(mcpAdapter, "RootsListChangedNotification", "Notification");

		ClassOrInterfaceDeclaration transportIface = new ClassOrInterfaceDeclaration();
		transportIface.setName("McpTransport");
		transportIface.setInterface(true);
		transportIface.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		com.github.javaparser.ast.body.MethodDeclaration sendMd = transportIface.addMethod("send");
		sendMd.setType("String");
		sendMd.addParameter("String", "payload");
		sendMd.setBody(null);
		com.github.javaparser.ast.body.MethodDeclaration closeTransportMd = transportIface.addMethod("close");
		closeTransportMd.setType("void");
		closeTransportMd.setBody(null);
		mcpAdapter.addMember(transportIface);

		mcpAdapter.addField("McpTransport", "transport", Modifier.Keyword.PUBLIC);
		mcpAdapter.addFieldWithInitializer("boolean", "closed",
				new com.github.javaparser.ast.expr.BooleanLiteralExpr(false), Modifier.Keyword.PUBLIC);
		mcpAdapter.addField("java.util.function.Consumer<ProgressNotificationParams>", "progressListener",
				Modifier.Keyword.PUBLIC);

		com.github.javaparser.ast.body.MethodDeclaration closeMd = mcpAdapter.addMethod("close",
				Modifier.Keyword.PUBLIC);
		closeMd.setType("void");
		closeMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock(
				"{\n  this.closed = true;\n  if (this.transport != null) {\n    this.transport.close();\n  }\n}"));

		com.github.javaparser.ast.body.MethodDeclaration readResMd = mcpAdapter.addMethod("readResource",
				Modifier.Keyword.PUBLIC);
		readResMd.setType("TextResourceContents");
		readResMd.addParameter("String", "uri");
		readResMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  if (uri == null || uri.contains(\"..\")) {\n"
				+ "    throw new SecurityException(\"Root boundary violation: \" + uri);\n" + "  }\n"
				+ "  TextResourceContents res = new TextResourceContents();\n" + "  res.uri = uri;\n"
				+ "  res.mimeType = \"application/json\";\n" + "  res.text = \"{}\";\n" + "  return res;\n" + "}"));

		return mcpAdapter;
	}

	/**
	 * Creates and attaches the {@code AuthAdapter} inner class to the given client
	 * class declaration for integrated Identity Provider (IdP) authentication
	 * ({@code register}, {@code login}, {@code refresh}, and {@code logout}).
	 *
	 * @param clientDecl
	 *            The target SDK client {@link ClassOrInterfaceDeclaration}.
	 * @param clientClassName
	 *            The enclosing SDK client class name.
	 * @return The created {@code AuthAdapter} {@link ClassOrInterfaceDeclaration}.
	 */
	public static ClassOrInterfaceDeclaration createAuthAdapter(ClassOrInterfaceDeclaration clientDecl,
			String clientClassName) {
		ClassOrInterfaceDeclaration authAdapter = new ClassOrInterfaceDeclaration().setName("AuthAdapter")
				.setModifier(Modifier.Keyword.PUBLIC, true);
		clientDecl.addMember(authAdapter);

		com.github.javaparser.ast.body.MethodDeclaration regMd = authAdapter.addMethod("register",
				Modifier.Keyword.PUBLIC);
		regMd.setType("HttpResponse<String>");
		regMd.addParameter("String", "username");
		regMd.addParameter("String", "password");
		regMd.addThrownException(com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType("Exception"));
		regMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  String payload = \"{\\\"username\\\":\\\"\" + username + \"\\\",\\\"password\\\":\\\"\" + password + \"\\\"}\";\n"
				+ "  HttpRequest req = HttpRequest.newBuilder().uri(URI.create(" + clientClassName
				+ ".this.baseUrl + \"/auth/register\"))\n" + "      .header(\"Content-Type\", \"application/json\")\n"
				+ "      .POST(HttpRequest.BodyPublishers.ofString(payload)).build();\n"
				+ "  HttpResponse<String> res = " + clientClassName
				+ ".this.httpClient.send(req, HttpResponse.BodyHandlers.ofString());\n"
				+ "  extractAndStoreToken(res.body());\n" + "  return res;\n" + "}"));

		com.github.javaparser.ast.body.MethodDeclaration loginMd = authAdapter.addMethod("login",
				Modifier.Keyword.PUBLIC);
		loginMd.setType("HttpResponse<String>");
		loginMd.addParameter("String", "username");
		loginMd.addParameter("String", "password");
		loginMd.addThrownException(com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType("Exception"));
		loginMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  String payload = \"{\\\"username\\\":\\\"\" + username + \"\\\",\\\"password\\\":\\\"\" + password + \"\\\"}\";\n"
				+ "  HttpRequest req = HttpRequest.newBuilder().uri(URI.create(" + clientClassName
				+ ".this.baseUrl + \"/auth/login\"))\n" + "      .header(\"Content-Type\", \"application/json\")\n"
				+ "      .POST(HttpRequest.BodyPublishers.ofString(payload)).build();\n"
				+ "  HttpResponse<String> res = " + clientClassName
				+ ".this.httpClient.send(req, HttpResponse.BodyHandlers.ofString());\n"
				+ "  extractAndStoreToken(res.body());\n" + "  return res;\n" + "}"));

		com.github.javaparser.ast.body.MethodDeclaration refreshMd = authAdapter.addMethod("refresh",
				Modifier.Keyword.PUBLIC);
		refreshMd.setType("HttpResponse<String>");
		refreshMd.addThrownException(com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType("Exception"));
		refreshMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(" + clientClassName
				+ ".this.baseUrl + \"/auth/refresh\"));\n" + "  if (" + clientClassName
				+ ".this.authorizationToken != null) {\n" + "      builder.header(\"Authorization\", " + clientClassName
				+ ".this.authorizationToken);\n" + "  }\n" + "  HttpResponse<String> res = " + clientClassName
				+ ".this.httpClient.send(builder.POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());\n"
				+ "  extractAndStoreToken(res.body());\n" + "  return res;\n" + "}"));

		com.github.javaparser.ast.body.MethodDeclaration logoutMd = authAdapter.addMethod("logout",
				Modifier.Keyword.PUBLIC);
		logoutMd.setType("HttpResponse<String>");
		logoutMd.addThrownException(com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType("Exception"));
		logoutMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(" + clientClassName
				+ ".this.baseUrl + \"/auth/logout\"));\n" + "  if (" + clientClassName
				+ ".this.authorizationToken != null) {\n" + "      builder.header(\"Authorization\", " + clientClassName
				+ ".this.authorizationToken);\n" + "  }\n" + "  HttpResponse<String> res = " + clientClassName
				+ ".this.httpClient.send(builder.POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());\n"
				+ "  " + clientClassName + ".this.authorizationToken = null;\n" + "  return res;\n" + "}"));

		com.github.javaparser.ast.body.MethodDeclaration extractMd = authAdapter.addMethod("extractAndStoreToken",
				Modifier.Keyword.PRIVATE);
		extractMd.addParameter("String", "body");
		extractMd.setBody(com.github.javaparser.StaticJavaParser.parseBlock("{\n"
				+ "  if (body != null && body.contains(\"\\\"token\\\"\")) {\n"
				+ "      int idx = body.indexOf(\"\\\"token\\\"\");\n" + "      int colon = body.indexOf(':', idx);\n"
				+ "      int q1 = body.indexOf('\"', colon + 1);\n" + "      int q2 = body.indexOf('\"', q1 + 1);\n"
				+ "      if (q1 > 0 && q2 > q1) {\n" + "          " + clientClassName
				+ ".this.authorizationToken = \"Bearer \" + body.substring(q1 + 1, q2);\n" + "      }\n" + "  }\n"
				+ "}"));

		return authAdapter;
	}
}
