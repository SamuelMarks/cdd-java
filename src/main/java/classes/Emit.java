package classes;

import openapi.OpenAPI;
import openapi.Schema;
import openapi.ExternalDocumentation;
import openapi.XML;
import openapi.Discriminator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.util.Map;
import java.util.List;

/**
 * Emits DTOs to language source while preserving whitespace and comments.
 */
@cli.Generated
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * escape doc
	 */
	private static String escape(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Emits Java code for schemas.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = StaticJavaParser.parse(existingSource);
			LexicalPreservingPrinter.setup(cu);
		} else {
			cu = new CompilationUnit();
			isNew = true;
			cu.addImport("com.fasterxml.jackson.annotation.JsonInclude");
			cu.addImport("com.fasterxml.jackson.annotation.JsonProperty");
			cu.addImport("com.fasterxml.jackson.annotation.JsonTypeInfo");
			cu.addImport("com.fasterxml.jackson.annotation.JsonSubTypes");
			cu.addImport("com.fasterxml.jackson.annotation.JsonValue");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
			cu.addImport("java.net.http.HttpClient");
			cu.addImport("java.net.http.HttpRequest");
			cu.addImport("java.net.http.HttpResponse");
			cu.addImport("java.net.URI");
		}
		String title = (model.info != null && model.info.title != null)
				? model.info.title.replaceAll("[^a-zA-Z0-9]", "")
				: "Api";
		if (title.isEmpty())
			title = "Api";
		String clientClass = title + "Client";
		ClassOrInterfaceDeclaration clientDecl = cu.getClassByName(clientClass).orElse(null);
		if (clientDecl == null) {
			clientDecl = cu.addClass(clientClass);
			clientDecl.setModifier(Modifier.Keyword.PUBLIC, false);
			clientDecl.addField("String", "baseUrl", Modifier.Keyword.PRIVATE);
			clientDecl.addField("HttpClient", "httpClient", Modifier.Keyword.PRIVATE);
			clientDecl.addField("String", "authorizationToken", Modifier.Keyword.PUBLIC);
			clientDecl.addConstructor(Modifier.Keyword.PUBLIC).addParameter("String", "baseUrl")
					.setBody(StaticJavaParser.parseBlock(
							"{ this.baseUrl = baseUrl; this.httpClient = HttpClient.newHttpClient(); this.mcp = new McpAdapter(); }"));
			ClassOrInterfaceDeclaration mcpAdapter = clientDecl.addMember(new ClassOrInterfaceDeclaration()
					.setName("McpAdapter").setModifier(Modifier.Keyword.PUBLIC, false));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ModelHint { @JsonProperty(\"name\") public String name; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ModelPreferences { @JsonProperty(\"hints\") public java.util.List<ModelHint> hints; @JsonProperty(\"costPriority\") public Double costPriority; @JsonProperty(\"speedPriority\") public Double speedPriority; @JsonProperty(\"intelligencePriority\") public Double intelligencePriority; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration("public static class LoggingLevel { }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Tool { @JsonProperty(\"name\") public String name; @JsonProperty(\"description\") public String description; @JsonProperty(\"inputSchema\") public java.util.Map<String, Object> inputSchema; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class TextContent extends Annotated { @JsonProperty(\"type\") public String type; @JsonProperty(\"text\") public String text; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class TextResourceContents { @JsonProperty(\"uri\") public String uri; @JsonProperty(\"mimeType\") public String mimeType; @JsonProperty(\"text\") public String text; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceContents { @JsonProperty(\"uri\") public String uri; @JsonProperty(\"mimeType\") public String mimeType; }"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ListToolsRequest extends PaginatedRequest {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListToolsResult extends PaginatedResult { @JsonProperty(\"tools\") public java.util.List<Tool> tools; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration("public static class RequestId {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration("public static class Role {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class SamplingMessage { @JsonProperty(\"role\") public String role; @JsonProperty(\"content\") public java.util.Map<String, Object> content; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ClientInfo { @JsonProperty(\"name\") public String name; @JsonProperty(\"version\") public String version; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CallToolResult { @JsonProperty(\"content\") public java.util.List<TextContent> content; @JsonProperty(\"isError\") public Boolean isError; @JsonProperty(\"_meta\") public java.util.Map<String, Object> _meta; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CallToolRequestParams { @JsonProperty(\"name\") public String name; @JsonProperty(\"arguments\") public java.util.Map<String, Object> arguments; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CallToolRequest { @JsonProperty(\"method\") public String method; @JsonProperty(\"params\") public CallToolRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCRequest extends JSONRPCMessage { @JsonProperty(\"id\") public Object id; @JsonProperty(\"method\") public String method; @JsonProperty(\"params\") public java.util.Map<String, Object> params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCResponse extends JSONRPCMessage { @JsonProperty(\"id\") public Object id; @JsonProperty(\"result\") public Object result; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCErrorError { @JsonProperty(\"code\") public Integer code; @JsonProperty(\"message\") public String message; @JsonProperty(\"data\") public Object data; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCError extends JSONRPCMessage { @JsonProperty(\"id\") public Object id; @JsonProperty(\"error\") public JSONRPCErrorError error; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCNotification extends JSONRPCMessage { @JsonProperty(\"method\") public String method; @JsonProperty(\"params\") public java.util.Map<String, Object> params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class JSONRPCMessage { @JsonProperty(\"jsonrpc\") public String jsonrpc = \"2.0\"; }"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class Request extends JSONRPCRequest {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Result { @JsonProperty(\"_meta\") public java.util.Map<String, Object> _meta; }"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class EmptyResult extends Result {}"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class ClientRequest extends Request {}"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class ClientResult extends Result {}"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ClientNotification extends JSONRPCNotification {}"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class ServerRequest extends Request {}"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class ServerResult extends Result {}"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ServerNotification extends JSONRPCNotification {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CancelledNotificationParams { @JsonProperty(\"requestId\") public Object requestId; @JsonProperty(\"reason\") public String reason; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CancelledNotification extends Notification { @JsonProperty(\"params\") public CancelledNotificationParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ProgressNotificationParams { @JsonProperty(\"progressToken\") public Object progressToken; @JsonProperty(\"progress\") public Double progress; @JsonProperty(\"total\") public Double total; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ProgressNotification extends Notification { @JsonProperty(\"params\") public ProgressNotificationParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ClientCapabilitiesRoots { @JsonProperty(\"listChanged\") public Boolean listChanged; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ClientCapabilities { @JsonProperty(\"experimental\") public java.util.Map<String, Object> experimental; @JsonProperty(\"roots\") public ClientCapabilitiesRoots roots; @JsonProperty(\"sampling\") public java.util.Map<String, Object> sampling; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ServerCapabilitiesPrompts { @JsonProperty(\"listChanged\") public Boolean listChanged; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ServerCapabilitiesResources { @JsonProperty(\"listChanged\") public Boolean listChanged; @JsonProperty(\"subscribe\") public Boolean subscribe; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ServerCapabilitiesTools { @JsonProperty(\"listChanged\") public Boolean listChanged; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ServerCapabilities { @JsonProperty(\"experimental\") public java.util.Map<String, Object> experimental; @JsonProperty(\"logging\") public java.util.Map<String, Object> logging; @JsonProperty(\"prompts\") public ServerCapabilitiesPrompts prompts; @JsonProperty(\"resources\") public ServerCapabilitiesResources resources; @JsonProperty(\"tools\") public ServerCapabilitiesTools tools; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Implementation { @JsonProperty(\"name\") public String name; @JsonProperty(\"version\") public String version; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class InitializeRequestParams { @JsonProperty(\"protocolVersion\") public String protocolVersion; @JsonProperty(\"capabilities\") public ClientCapabilities capabilities; @JsonProperty(\"clientInfo\") public Implementation clientInfo; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class InitializeRequest extends Request { @JsonProperty(\"params\") public InitializeRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class InitializeResult extends Result { @JsonProperty(\"protocolVersion\") public String protocolVersion; @JsonProperty(\"capabilities\") public ServerCapabilities capabilities; @JsonProperty(\"serverInfo\") public Implementation serverInfo; @JsonProperty(\"instructions\") public String instructions; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class InitializedNotificationParams { @JsonProperty(\"_meta\") public java.util.Map<String, Object> _meta; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class InitializedNotification extends Notification { @JsonProperty(\"params\") public InitializedNotificationParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Notification extends JSONRPCNotification { @JsonProperty(\"params\") public java.util.Map<String, Object> params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Resource { @JsonProperty(\"uri\") public String uri; @JsonProperty(\"name\") public String name; @JsonProperty(\"description\") public String description; @JsonProperty(\"mimeType\") public String mimeType; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PaginatedRequestParams { @JsonProperty(\"cursor\") public String cursor; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PaginatedRequest extends Request { @JsonProperty(\"params\") public PaginatedRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PaginatedResult extends Result { @JsonProperty(\"nextCursor\") public String nextCursor; }"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ListResourcesRequest extends PaginatedRequest {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListResourcesResult extends PaginatedResult { @JsonProperty(\"resources\") public java.util.List<Resource> resources; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ReadResourceRequestParams { @JsonProperty(\"uri\") public String uri; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ReadResourceRequest extends Request { @JsonProperty(\"params\") public ReadResourceRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ReadResourceResult extends Result { @JsonProperty(\"contents\") public java.util.List<java.util.Map<String, Object>> contents; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceReference { @JsonProperty(\"type\") public String type; @JsonProperty(\"uri\") public String uri; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PromptReference { @JsonProperty(\"type\") public String type; @JsonProperty(\"name\") public String name; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class AnnotatedAnnotations { @JsonProperty(\"audience\") public java.util.List<String> audience; @JsonProperty(\"priority\") public Double priority; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Annotated { @JsonProperty(\"annotations\") public AnnotatedAnnotations annotations; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ImageContent extends Annotated { @JsonProperty(\"type\") public String type; @JsonProperty(\"data\") public String data; @JsonProperty(\"mimeType\") public String mimeType; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class BlobResourceContents { @JsonProperty(\"uri\") public String uri; @JsonProperty(\"mimeType\") public String mimeType; @JsonProperty(\"blob\") public String blob; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class EmbeddedResource extends Annotated { @JsonProperty(\"type\") public String type; @JsonProperty(\"resource\") public java.util.Map<String, Object> resource; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration("public static class ProgressToken {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration("public static class Cursor {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceTemplate { @JsonProperty(\"uriTemplate\") public String uriTemplate; @JsonProperty(\"name\") public String name; @JsonProperty(\"description\") public String description; @JsonProperty(\"mimeType\") public String mimeType; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListResourceTemplatesRequest extends PaginatedRequest {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListResourceTemplatesResult extends PaginatedResult { @JsonProperty(\"resourceTemplates\") public java.util.List<ResourceTemplate> resourceTemplates; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceUpdatedNotificationParams { @JsonProperty(\"uri\") public String uri; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceUpdatedNotification extends Notification { @JsonProperty(\"params\") public ResourceUpdatedNotificationParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ResourceListChangedNotification extends Notification {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class SubscribeRequestParams { @JsonProperty(\"uri\") public String uri; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class SubscribeRequest extends Request { @JsonProperty(\"params\") public SubscribeRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class UnsubscribeRequestParams { @JsonProperty(\"uri\") public String uri; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class UnsubscribeRequest extends Request { @JsonProperty(\"params\") public UnsubscribeRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Prompt { @JsonProperty(\"name\") public String name; @JsonProperty(\"description\") public String description; @JsonProperty(\"arguments\") public java.util.List<java.util.Map<String, Object>> arguments; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PromptArgument { @JsonProperty(\"name\") public String name; @JsonProperty(\"description\") public String description; @JsonProperty(\"required\") public Boolean required; }"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ListPromptsRequest extends PaginatedRequest {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListPromptsResult extends PaginatedResult { @JsonProperty(\"prompts\") public java.util.List<Prompt> prompts; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class GetPromptRequestParams { @JsonProperty(\"name\") public String name; @JsonProperty(\"arguments\") public java.util.Map<String, String> arguments; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class GetPromptRequest extends Request { @JsonProperty(\"params\") public GetPromptRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class GetPromptResult extends Result { @JsonProperty(\"description\") public String description; @JsonProperty(\"messages\") public java.util.List<java.util.Map<String, Object>> messages; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class PromptMessage { @JsonProperty(\"role\") public String role; @JsonProperty(\"content\") public java.util.Map<String, Object> content; }"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class PromptListChangedNotification extends Notification {}"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class ToolListChangedNotification extends Notification {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class LoggingMessageNotificationParams { @JsonProperty(\"level\") public String level; @JsonProperty(\"logger\") public String logger; @JsonProperty(\"data\") public Object data; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class LoggingMessageNotification extends Notification { @JsonProperty(\"params\") public LoggingMessageNotificationParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class SetLevelRequestParams { @JsonProperty(\"level\") public String level; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class SetLevelRequest extends Request { @JsonProperty(\"params\") public SetLevelRequestParams params; }"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class PingRequest extends Request {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CreateMessageRequestParams { @JsonProperty(\"messages\") public java.util.List<java.util.Map<String, Object>> messages; @JsonProperty(\"modelPreferences\") public java.util.Map<String, Object> modelPreferences; @JsonProperty(\"systemPrompt\") public String systemPrompt; @JsonProperty(\"includeContext\") public String includeContext; @JsonProperty(\"temperature\") public Double temperature; @JsonProperty(\"maxTokens\") public Integer maxTokens; @JsonProperty(\"stopSequences\") public java.util.List<String> stopSequences; @JsonProperty(\"metadata\") public java.util.Map<String, Object> metadata; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CreateMessageRequest extends Request { @JsonProperty(\"params\") public CreateMessageRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CreateMessageResult extends Result { @JsonProperty(\"role\") public String role; @JsonProperty(\"content\") public java.util.List<TextContent> content; @JsonProperty(\"model\") public String model; @JsonProperty(\"stopReason\") public String stopReason; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CompleteRequestParams { @JsonProperty(\"ref\") public java.util.Map<String, Object> ref; @JsonProperty(\"argument\") public java.util.Map<String, Object> argument; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CompleteRequest extends Request { @JsonProperty(\"params\") public CompleteRequestParams params; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CompleteResultCompletion { @JsonProperty(\"values\") public java.util.List<String> values; @JsonProperty(\"total\") public Integer total; @JsonProperty(\"hasMore\") public Boolean hasMore; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class CompleteResult extends Result { @JsonProperty(\"completion\") public CompleteResultCompletion completion; }"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class Root { @JsonProperty(\"uri\") public String uri; @JsonProperty(\"name\") public String name; }"));
			mcpAdapter.addMember(
					StaticJavaParser.parseBodyDeclaration("public static class ListRootsRequest extends Request {}"));
			mcpAdapter.addMember(StaticJavaParser.parseBodyDeclaration(
					"public static class ListRootsResult extends Result { @JsonProperty(\"roots\") public java.util.List<Root> roots; }"));
			mcpAdapter.addMember(StaticJavaParser
					.parseBodyDeclaration("public static class RootsListChangedNotification extends Notification {}"));
			com.github.javaparser.ast.body.MethodDeclaration getTools = mcpAdapter.addMethod("getTools",
					Modifier.Keyword.PUBLIC);
			getTools.setType("java.util.List<Tool>");
			StringBuilder toolsBody = new StringBuilder();
			toolsBody.append("{\n");
			toolsBody.append("  java.util.List<Tool> tools = new java.util.ArrayList<>();\n");
			com.github.javaparser.ast.body.MethodDeclaration callTool = mcpAdapter.addMethod("callTool",
					Modifier.Keyword.PUBLIC);
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
						emitClientMethod(clientDecl, "GET", path, pi.get);
						appendMcpToolCall(mcpBody, "GET", path, pi.get, clientClass);
						appendMcpToolSchema(toolsBody, "GET", path, pi.get);
					}
					if (pi.post != null) {
						emitClientMethod(clientDecl, "POST", path, pi.post);
						appendMcpToolCall(mcpBody, "POST", path, pi.post, clientClass);
						appendMcpToolSchema(toolsBody, "POST", path, pi.post);
					}
					if (pi.put != null) {
						emitClientMethod(clientDecl, "PUT", path, pi.put);
						appendMcpToolCall(mcpBody, "PUT", path, pi.put, clientClass);
						appendMcpToolSchema(toolsBody, "PUT", path, pi.put);
					}
					if (pi.delete != null) {
						emitClientMethod(clientDecl, "DELETE", path, pi.delete);
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
					Modifier.Keyword.PUBLIC);
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
			clientDecl.addField("McpAdapter", "mcp", Modifier.Keyword.PUBLIC);
		}
		if (model.components != null && model.components.schemas != null) {
			for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse")) {
					continue;
				}
				emitClass(cu, className, entry.getValue(), model);
			}
		}
		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * appendMcpToolSchema doc
	 */
	private static void appendMcpToolSchema(StringBuilder body, String method, String path, openapi.Operation op) {
		String toolName = op.operationId != null ? op.operationId : method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
		String desc = (op.summary != null ? op.summary : (op.description != null ? op.description : ""));
		body.append("  {\n");
		body.append("    Tool tool = new Tool();\n");
		body.append("    tool.name = \"").append(escape(toolName)).append("\";\n");
		body.append("    tool.description = \"").append(escape(desc)).append("\";\n");
		body.append("    java.util.Map<String, Object> inputSchema = new java.util.HashMap<>();\n");
		body.append("    inputSchema.put(\"type\", \"object\");\n");
		body.append("    java.util.Map<String, Object> properties = new java.util.HashMap<>();\n");
		body.append("    java.util.List<String> required = new java.util.ArrayList<>();\n");
		if (op.parameters != null) {
			for (Object pObj : op.parameters) {
				if (!(pObj instanceof openapi.Parameter))
					continue;
				openapi.Parameter p = (openapi.Parameter) pObj;
				body.append("    {\n");
				body.append("      java.util.Map<String, Object> prop = new java.util.HashMap<>();\n");
				// simplistic mapping
				body.append("      prop.put(\"type\", \"string\");\n");
				if (p.description != null) {
					body.append("      prop.put(\"description\", \"").append(escape(p.description)).append("\");\n");
				}
				body.append("      properties.put(\"").append(escape(p.name)).append("\", prop);\n");
				if (p.required != null && p.required) {
					body.append("      required.add(\"").append(escape(p.name)).append("\");\n");
				}
				body.append("    }\n");
			}
		}
		if (op.requestBody != null && op.requestBody instanceof openapi.RequestBody) {
			openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
			body.append("    {\n");
			body.append("      java.util.Map<String, Object> prop = new java.util.HashMap<>();\n");
			body.append("      prop.put(\"type\", \"string\");\n");
			if (rb.description != null) {
				body.append("      prop.put(\"description\", \"").append(escape(rb.description)).append("\");\n");
			}
			body.append("      properties.put(\"requestBody\", prop);\n");
			if (rb.required != null && rb.required) {
				body.append("      required.add(\"requestBody\");\n");
			}
			body.append("    }\n");
		}
		body.append("    inputSchema.put(\"properties\", properties);\n");
		body.append("    inputSchema.put(\"required\", required);\n");
		body.append("    tool.inputSchema = inputSchema;\n");
		body.append("    tools.add(tool);\n");
		body.append("  }\n");
	}

	/**
	 * appendMcpToolCall doc
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
			int pIdx = 0;
			for (Object po : op.parameters) {
				openapi.Parameter p = (openapi.Parameter) po;
				String pName = p.name != null ? p.name.replaceAll("[^a-zA-Z0-9_]", "") : ("p" + pIdx);
				if (pName.isEmpty())
					pName = "param";
				if (!first)
					body.append(", ");
				body.append("arguments.get(\"").append(p.name).append("\") != null ? arguments.get(\"").append(p.name)
						.append("\").toString() : null");
				first = false;
				pIdx++;
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
	 * emitClientMethod doc
	 */
	private static void emitClientMethod(ClassOrInterfaceDeclaration classDecl, String method, String path,
			openapi.Operation op) {
		String methodName = op.operationId != null
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (method.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		if (methodName.isEmpty())
			methodName = "method";
		com.github.javaparser.ast.body.MethodDeclaration md = classDecl.addMethod(methodName, Modifier.Keyword.PUBLIC);
		md.setType("HttpResponse<String>");
		md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
		String resolvedPath = path;
		if (op.parameters != null) {
			int pIdx = 0;
			for (Object po : op.parameters) {
				openapi.Parameter p = (openapi.Parameter) po;
				String pName = p.name != null ? p.name.replaceAll("[^a-zA-Z0-9_]", "") : ("p" + pIdx);
				if (pName.isEmpty())
					pName = "param";
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
	 * Generated JavaDoc.
	 *
	 * @param cu
	 *            param doc
	 * @param className
	 *            param doc
	 * @param schemaMap
	 *            param doc
	 * @param model
	 *            param doc
	 */
	private static void emitClass(CompilationUnit cu, String className, Schema schemaMap, OpenAPI model) {
		if (schemaMap.enumValues != null) {
			EnumDeclaration enumDecl = cu.getEnumByName(className).orElse(null);
			if (enumDecl == null) {
				enumDecl = cu.addEnum(className);
				enumDecl.setModifier(Modifier.Keyword.PUBLIC, false);
			} else {
				enumDecl.getEntries().clear();
			}
			List<Object> enumValues = schemaMap.enumValues;
			for (Object val : enumValues) {
				String valStr = String.valueOf(val);
				String safeName = valStr.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
				if (safeName.isEmpty() || Character.isDigit(safeName.charAt(0)))
					safeName = "_" + safeName;
				enumDecl.addEnumConstant(safeName);
			}
			return;
		}
		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
		if (classDecl == null) {
			classDecl = cu.addClass(className);
			classDecl.setModifier(Modifier.Keyword.PUBLIC, false);
		}
		StringBuilder classDoc = new StringBuilder();
		if (schemaMap.description != null && !schemaMap.description.isEmpty()) {
			classDoc.append(schemaMap.description);
		}
		if (schemaMap.xml != null) {
			XML xmlMap = schemaMap.xml;
			if (xmlMap.name != null)
				classDoc.append("\n@xmlName ").append(xmlMap.name);
			if (xmlMap.namespace != null)
				classDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
			if (xmlMap.prefix != null)
				classDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
			if (xmlMap.attribute != null)
				classDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
			if (xmlMap.wrapped != null)
				classDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
		}
		if (schemaMap.externalDocs != null) {
			ExternalDocumentation extDocsMap = schemaMap.externalDocs;
			if (extDocsMap.url != null) {
				classDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
				if (extDocsMap.description != null) {
					classDoc.append(" ").append(extDocsMap.description);
				}
			}
		}
		if (schemaMap.example != null) {
			classDoc.append("\n@schemaExample ").append(schemaMap.example);
		}
		if (schemaMap.discriminator != null) {
			Discriminator discMap = schemaMap.discriminator;
			if (discMap.propertyName != null) {
				classDoc.append("\n@discriminatorProperty ").append(discMap.propertyName);
			}
			if (discMap.mapping != null) {
				for (Map.Entry<String, String> entry : discMap.mapping.entrySet()) {
					classDoc.append("\n@discriminatorMapping ").append(entry.getKey()).append(" ")
							.append(entry.getValue());
				}
			}
			if (discMap.extensions != null && discMap.extensions.containsKey("defaultMapping")) {
				classDoc.append("\n@discriminatorDefault ").append(discMap.extensions.get("defaultMapping"));
			}
		}
		if (classDoc.length() > 0) {
			classDecl.setJavadocComment(classDoc.toString().trim());
		}
		Map<String, Object> properties = schemaMap.properties;
		if (properties != null) {
			for (Map.Entry<String, Object> prop : properties.entrySet()) {
				String propName = prop.getKey();
				String safePropName = propName.replaceAll("[^a-zA-Z0-9_]", "_");
				if (Character.isDigit(safePropName.charAt(0)))
					safePropName = "_" + safePropName;
				if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName)
						|| "class".equals(safePropName)) {
					safePropName += "Value";
				}
				String type = resolveType(prop.getValue(), model);
				if (!classDecl.getFieldByName(safePropName).isPresent()) {
					FieldDeclaration fd = classDecl.addField(type, safePropName, Modifier.Keyword.PUBLIC);
					fd.addAnnotation(StaticJavaParser.parseAnnotation("@JsonProperty(\"" + propName + "\")"));
					if (prop.getValue() instanceof Schema) {
						Schema propMap = (Schema) prop.getValue();
						StringBuilder fieldDoc = new StringBuilder();
						if (propMap.description != null && !propMap.description.isEmpty()) {
							fieldDoc.append(propMap.description);
						}
						if (propMap.externalDocs != null) {
							ExternalDocumentation extDocsMap = propMap.externalDocs;
							if (extDocsMap.url != null) {
								fieldDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
								if (extDocsMap.description != null) {
									fieldDoc.append(" ").append(extDocsMap.description);
								}
							}
						}
						if (propMap.example != null) {
							fieldDoc.append("\n@schemaExample ").append(propMap.example);
						}
						if (propMap.xml != null) {
							XML xmlMap = propMap.xml;
							if (xmlMap.name != null)
								fieldDoc.append("\n@xmlName ").append(xmlMap.name);
							if (xmlMap.namespace != null)
								fieldDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
							if (xmlMap.prefix != null)
								fieldDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
							if (xmlMap.attribute != null)
								fieldDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
							if (xmlMap.wrapped != null)
								fieldDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
						}
						if (fieldDoc.length() > 0) {
							fd.setJavadocComment(fieldDoc.toString().trim());
						}
					}
				}
			}
		}
	}

	/**
	 * Generated JavaDoc.
	 *
	 * @param schemaObj
	 *            param doc
	 * @param model
	 *            param doc
	 * @return return doc
	 */
	private static String resolveType(Object schemaObj, OpenAPI model) {
		if (!(schemaObj instanceof Schema))
			return "Object";
		Schema schemaMap = (Schema) schemaObj;
		String schemaType = (String) schemaMap.type;
		if ("string".equals(schemaType)) {
			if ("date-time".equals(schemaMap.format))
				return "java.time.OffsetDateTime";
			if ("date".equals(schemaMap.format))
				return "java.time.LocalDate";
			if ("uuid".equals(schemaMap.format))
				return "java.util.UUID";
			if ("binary".equals(schemaMap.format))
				return "byte[]";
			return "String";
		} else if ("integer".equals(schemaType)) {
			if ("int64".equals(schemaMap.format))
				return "Long";
			return "Integer";
		} else if ("number".equals(schemaType)) {
			if ("float".equals(schemaMap.format))
				return "Float";
			return "Double";
		} else if ("boolean".equals(schemaType)) {
			return "Boolean";
		} else if ("array".equals(schemaType)) {
			String innerType = resolveType(schemaMap.items, model);
			return "List<" + innerType + ">";
		} else if ("object".equals(schemaType) || schemaMap.additionalProperties != null) {
			if (schemaMap.additionalProperties instanceof Schema) {
				String innerType = resolveType(schemaMap.additionalProperties, model);
				return "Map<String, " + innerType + ">";
			}
			return "Map<String, Object>";
		}
		return "Object";
	}
}
