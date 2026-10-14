package classes;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

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
		String[] mcpDeclarations = new String[]{
				"public static class ModelHint { @JsonProperty('name') public String name; }",
				"public static class ModelPreferences { @JsonProperty('hints') public java.util.List<ModelHint> hints; @JsonProperty('costPriority') public Double costPriority; @JsonProperty('speedPriority') public Double speedPriority; @JsonProperty('intelligencePriority') public Double intelligencePriority; }",
				"public static class LoggingLevel { }",
				"public static class Tool { @JsonProperty('name') public String name; @JsonProperty('description') public String description; @JsonProperty('inputSchema') public java.util.Map<String, Object> inputSchema; }",
				"public static class TextContent extends Annotated { @JsonProperty('type') public String type; @JsonProperty('text') public String text; }",
				"public static class TextResourceContents { @JsonProperty('uri') public String uri; @JsonProperty('mimeType') public String mimeType; @JsonProperty('text') public String text; }",
				"public static class ResourceContents { @JsonProperty('uri') public String uri; @JsonProperty('mimeType') public String mimeType; }",
				"public static class ListToolsRequest extends PaginatedRequest {}",
				"public static class ListToolsResult extends PaginatedResult { @JsonProperty('tools') public java.util.List<Tool> tools; }",
				"public static class RequestId {}", "public static class Role {}",
				"public static class SamplingMessage { @JsonProperty('role') public String role; @JsonProperty('content') public java.util.Map<String, Object> content; }",
				"public static class ClientInfo { @JsonProperty('name') public String name; @JsonProperty('version') public String version; }",
				"public static class CallToolResult { @JsonProperty('content') public java.util.List<TextContent> content; @JsonProperty('isError') public Boolean isError; @JsonProperty('_meta') public java.util.Map<String, Object> _meta; }",
				"public static class CallToolRequestParams { @JsonProperty('name') public String name; @JsonProperty('arguments') public java.util.Map<String, Object> arguments; }",
				"public static class CallToolRequest { @JsonProperty('method') public String method; @JsonProperty('params') public CallToolRequestParams params; }",
				"public static class JSONRPCRequest extends JSONRPCMessage { @JsonProperty('id') public Object id; @JsonProperty('method') public String method; @JsonProperty('params') public java.util.Map<String, Object> params; }",
				"public static class JSONRPCResponse extends JSONRPCMessage { @JsonProperty('id') public Object id; @JsonProperty('result') public Object result; }",
				"public static class JSONRPCErrorError { @JsonProperty('code') public Integer code; @JsonProperty('message') public String message; @JsonProperty('data') public Object data; }",
				"public static class JSONRPCError extends JSONRPCMessage { @JsonProperty('id') public Object id; @JsonProperty('error') public JSONRPCErrorError error; }",
				"public static class JSONRPCNotification extends JSONRPCMessage { @JsonProperty('method') public String method; @JsonProperty('params') public java.util.Map<String, Object> params; }",
				"public static class JSONRPCMessage { @JsonProperty('jsonrpc') public String jsonrpc = '2.0'; }",
				"public static class Request extends JSONRPCRequest {}",
				"public static class Result { @JsonProperty('_meta') public java.util.Map<String, Object> _meta; }",
				"public static class EmptyResult extends Result {}",
				"public static class ClientRequest extends Request {}",
				"public static class ClientResult extends Result {}",
				"public static class ClientNotification extends JSONRPCNotification {}",
				"public static class ServerRequest extends Request {}",
				"public static class ServerResult extends Result {}",
				"public static class ServerNotification extends JSONRPCNotification {}",
				"public static class CancelledNotificationParams { @JsonProperty('requestId') public Object requestId; @JsonProperty('reason') public String reason; }",
				"public static class CancelledNotification extends Notification { @JsonProperty('params') public CancelledNotificationParams params; }",
				"public static class ProgressNotificationParams { @JsonProperty('progressToken') public Object progressToken; @JsonProperty('progress') public Double progress; @JsonProperty('total') public Double total; }",
				"public static class ProgressNotification extends Notification { @JsonProperty('params') public ProgressNotificationParams params; }",
				"public static class ClientCapabilitiesRoots { @JsonProperty('listChanged') public Boolean listChanged; }",
				"public static class ClientCapabilities { @JsonProperty('experimental') public java.util.Map<String, Object> experimental; @JsonProperty('roots') public ClientCapabilitiesRoots roots; @JsonProperty('sampling') public java.util.Map<String, Object> sampling; }",
				"public static class ServerCapabilitiesPrompts { @JsonProperty('listChanged') public Boolean listChanged; }",
				"public static class ServerCapabilitiesResources { @JsonProperty('listChanged') public Boolean listChanged; @JsonProperty('subscribe') public Boolean subscribe; }",
				"public static class ServerCapabilitiesTools { @JsonProperty('listChanged') public Boolean listChanged; }",
				"public static class ServerCapabilities { @JsonProperty('experimental') public java.util.Map<String, Object> experimental; @JsonProperty('logging') public java.util.Map<String, Object> logging; @JsonProperty('prompts') public ServerCapabilitiesPrompts prompts; @JsonProperty('resources') public ServerCapabilitiesResources resources; @JsonProperty('tools') public ServerCapabilitiesTools tools; }",
				"public static class Implementation { @JsonProperty('name') public String name; @JsonProperty('version') public String version; }",
				"public static class InitializeRequestParams { @JsonProperty('protocolVersion') public String protocolVersion; @JsonProperty('capabilities') public ClientCapabilities capabilities; @JsonProperty('clientInfo') public Implementation clientInfo; }",
				"public static class InitializeRequest extends Request { @JsonProperty('params') public InitializeRequestParams params; }",
				"public static class InitializeResult extends Result { @JsonProperty('protocolVersion') public String protocolVersion; @JsonProperty('capabilities') public ServerCapabilities capabilities; @JsonProperty('serverInfo') public Implementation serverInfo; @JsonProperty('instructions') public String instructions; }",
				"public static class InitializedNotificationParams { @JsonProperty('_meta') public java.util.Map<String, Object> _meta; }",
				"public static class InitializedNotification extends Notification { @JsonProperty('params') public InitializedNotificationParams params; }",
				"public static class Notification extends JSONRPCNotification { @JsonProperty('params') public java.util.Map<String, Object> params; }",
				"public static class Resource { @JsonProperty('uri') public String uri; @JsonProperty('name') public String name; @JsonProperty('description') public String description; @JsonProperty('mimeType') public String mimeType; }",
				"public static class PaginatedRequestParams { @JsonProperty('cursor') public String cursor; }",
				"public static class PaginatedRequest extends Request { @JsonProperty('params') public PaginatedRequestParams params; }",
				"public static class PaginatedResult extends Result { @JsonProperty('nextCursor') public String nextCursor; }",
				"public static class ListResourcesRequest extends PaginatedRequest {}",
				"public static class ListResourcesResult extends PaginatedResult { @JsonProperty('resources') public java.util.List<Resource> resources; }",
				"public static class ReadResourceRequestParams { @JsonProperty('uri') public String uri; }",
				"public static class ReadResourceRequest extends Request { @JsonProperty('params') public ReadResourceRequestParams params; }",
				"public static class ReadResourceResult extends Result { @JsonProperty('contents') public java.util.List<java.util.Map<String, Object>> contents; }",
				"public static class ResourceReference { @JsonProperty('type') public String type; @JsonProperty('uri') public String uri; }",
				"public static class PromptReference { @JsonProperty('type') public String type; @JsonProperty('name') public String name; }",
				"public static class AnnotatedAnnotations { @JsonProperty('audience') public java.util.List<String> audience; @JsonProperty('priority') public Double priority; }",
				"public static class Annotated { @JsonProperty('annotations') public AnnotatedAnnotations annotations; }",
				"public static class ImageContent extends Annotated { @JsonProperty('type') public String type; @JsonProperty('data') public String data; @JsonProperty('mimeType') public String mimeType; }",
				"public static class BlobResourceContents { @JsonProperty('uri') public String uri; @JsonProperty('mimeType') public String mimeType; @JsonProperty('blob') public String blob; }",
				"public static class EmbeddedResource extends Annotated { @JsonProperty('type') public String type; @JsonProperty('resource') public java.util.Map<String, Object> resource; }",
				"public static class ProgressToken {}", "public static class Cursor {}",
				"public static class ResourceTemplate { @JsonProperty('uriTemplate') public String uriTemplate; @JsonProperty('name') public String name; @JsonProperty('description') public String description; @JsonProperty('mimeType') public String mimeType; }",
				"public static class ListResourceTemplatesRequest extends PaginatedRequest {}",
				"public static class ListResourceTemplatesResult extends PaginatedResult { @JsonProperty('resourceTemplates') public java.util.List<ResourceTemplate> resourceTemplates; }",
				"public static class ResourceUpdatedNotificationParams { @JsonProperty('uri') public String uri; }",
				"public static class ResourceUpdatedNotification extends Notification { @JsonProperty('params') public ResourceUpdatedNotificationParams params; }",
				"public static class ResourceListChangedNotification extends Notification {}",
				"public static class SubscribeRequestParams { @JsonProperty('uri') public String uri; }",
				"public static class SubscribeRequest extends Request { @JsonProperty('params') public SubscribeRequestParams params; }",
				"public static class UnsubscribeRequestParams { @JsonProperty('uri') public String uri; }",
				"public static class UnsubscribeRequest extends Request { @JsonProperty('params') public UnsubscribeRequestParams params; }",
				"public static class Prompt { @JsonProperty('name') public String name; @JsonProperty('description') public String description; @JsonProperty('arguments') public java.util.List<java.util.Map<String, Object>> arguments; }",
				"public static class PromptArgument { @JsonProperty('name') public String name; @JsonProperty('description') public String description; @JsonProperty('required') public Boolean required; }",
				"public static class ListPromptsRequest extends PaginatedRequest {}",
				"public static class ListPromptsResult extends PaginatedResult { @JsonProperty('prompts') public java.util.List<Prompt> prompts; }",
				"public static class GetPromptRequestParams { @JsonProperty('name') public String name; @JsonProperty('arguments') public java.util.Map<String, String> arguments; }",
				"public static class GetPromptRequest extends Request { @JsonProperty('params') public GetPromptRequestParams params; }",
				"public static class GetPromptResult extends Result { @JsonProperty('description') public String description; @JsonProperty('messages') public java.util.List<java.util.Map<String, Object>> messages; }",
				"public static class PromptMessage { @JsonProperty('role') public String role; @JsonProperty('content') public java.util.Map<String, Object> content; }",
				"public static class PromptListChangedNotification extends Notification {}",
				"public static class ToolListChangedNotification extends Notification {}",
				"public static class LoggingMessageNotificationParams { @JsonProperty('level') public String level; @JsonProperty('logger') public String logger; @JsonProperty('data') public Object data; }",
				"public static class LoggingMessageNotification extends Notification { @JsonProperty('params') public LoggingMessageNotificationParams params; }",
				"public static class SetLevelRequestParams { @JsonProperty('level') public String level; }",
				"public static class SetLevelRequest extends Request { @JsonProperty('params') public SetLevelRequestParams params; }",
				"public static class PingRequest extends Request {}",
				"public static class CreateMessageRequestParams { @JsonProperty('messages') public java.util.List<java.util.Map<String, Object>> messages; @JsonProperty('modelPreferences') public java.util.Map<String, Object> modelPreferences; @JsonProperty('systemPrompt') public String systemPrompt; @JsonProperty('includeContext') public String includeContext; @JsonProperty('temperature') public Double temperature; @JsonProperty('maxTokens') public Integer maxTokens; @JsonProperty('stopSequences') public java.util.List<String> stopSequences; @JsonProperty('metadata') public java.util.Map<String, Object> metadata; }",
				"public static class CreateMessageRequest extends Request { @JsonProperty('params') public CreateMessageRequestParams params; }",
				"public static class CreateMessageResult extends Result { @JsonProperty('role') public String role; @JsonProperty('content') public java.util.List<TextContent> content; @JsonProperty('model') public String model; @JsonProperty('stopReason') public String stopReason; }",
				"public static class CompleteRequestParams { @JsonProperty('ref') public java.util.Map<String, Object> ref; @JsonProperty('argument') public java.util.Map<String, Object> argument; }",
				"public static class CompleteRequest extends Request { @JsonProperty('params') public CompleteRequestParams params; }",
				"public static class CompleteResultCompletion { @JsonProperty('values') public java.util.List<String> values; @JsonProperty('total') public Integer total; @JsonProperty('hasMore') public Boolean hasMore; }",
				"public static class CompleteResult extends Result { @JsonProperty('completion') public CompleteResultCompletion completion; }",
				"public static class Root { @JsonProperty('uri') public String uri; @JsonProperty('name') public String name; }",
				"public static class ListRootsRequest extends Request {}",
				"public static class ListRootsResult extends Result { @JsonProperty('roots') public java.util.List<Root> roots; }",
				"public static class RootsListChangedNotification extends Notification {}"};
		String q = String.valueOf((char) 34);
		for (String decl : mcpDeclarations) {
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(decl.replace("'", q)));
		}
		return mcpAdapter;
	}
}
