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

		return mcpAdapter;
	}
}
