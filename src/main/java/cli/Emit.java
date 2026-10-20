package cli;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.Map;
import openapi.*;

/**
 * Emits the CLI application from an OpenAPI spec.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
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

	/**
	 * Emits the CLI Java code using JavaParser AST construction.
	 *
	 * @param api
	 *            The OpenAPI spec to emit.
	 * @return The generated Java source code.
	 */
	public static String emitCli(OpenAPI api) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("cli");
		cu.addImport("java.util.Arrays");
		cu.addImport("java.io.BufferedReader");
		cu.addImport("java.io.InputStreamReader");
		cu.addImport("com.fasterxml.jackson.databind.ObjectMapper");
		cu.addImport("com.fasterxml.jackson.databind.JsonNode");
		cu.addImport("com.fasterxml.jackson.databind.node.ObjectNode");
		cu.addImport("com.fasterxml.jackson.databind.node.ArrayNode");

		ClassOrInterfaceDeclaration cls = cu.addClass("SdkCli", Modifier.Keyword.PUBLIC);
		cls.addConstructor(Modifier.Keyword.PUBLIC);

		MethodDeclaration mainMd = cls.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		mainMd.addParameter("String[]", "args");
		mainMd.setBody(StaticJavaParser
				.parseBlock("{\n" + "if (args.length == 0 || args[0].equals(\"--help\") || args[0].equals(\"-h\")) {\n"
						+ "    printHelp();\n" + "    return;\n" + "}\n" + "String command = args[0];\n"
						+ "if (command.equals(\"mcp\")) {\n" + "    runMcpServer();\n" + "    return;\n" + "}\n"
						+ "System.err.println(\"Unknown command: \" + command);\n" + "printHelp();\n" + "}"));

		StringBuilder mcpSb = new StringBuilder();
		emitMcpServerMethod(mcpSb, api);
		cls.addMember(StaticJavaParser.parseBodyDeclaration(mcpSb.toString()));

		MethodDeclaration helpMd = cls.addMethod("printHelp", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
		StringBuilder helpSb = new StringBuilder("{\n");
		helpSb.append("        System.out.println(\"SDK CLI\");\n");
		CliHelpCodec.emitInfoAndServers(helpSb, api);
		CliHelpCodec.emitComponents(helpSb, api);
		CliHelpCodec.emitOperations(helpSb, api);
		helpSb.append("}");
		helpMd.setBody(StaticJavaParser.parseBlock(helpSb.toString()));

		return cu.toString();
	}

	/**
	 * Emits the {@code runMcpServer()} method for STDIO JSON-RPC 2.0 tool
	 * execution.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param api
	 *            The OpenAPI specification.
	 */
	private static void emitMcpServerMethod(StringBuilder sb, OpenAPI api) {
		sb.append("    private static void runMcpServer() {\n");
		sb.append("        try {\n");
		sb.append(
				"            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, \"UTF-8\"));\n");
		sb.append("            ObjectMapper mapper = new ObjectMapper();\n");
		sb.append("            String line;\n");
		sb.append("            while ((line = reader.readLine()) != null) {\n");
		sb.append("                line = line.trim();\n");
		sb.append("                if (line.isEmpty()) continue;\n");
		sb.append("                String response;\n");
		sb.append("                try {\n");
		sb.append("                    JsonNode req = mapper.readTree(line);\n");
		sb.append("                    JsonNode idNode = req.get(\"id\");\n");
		sb.append(
				"                    String idStr = (idNode != null && !idNode.isNull()) ? idNode.toString() : \"null\";\n");
		sb.append("                    if (req.has(\"jsonrpc\") && \"2.0\".equals(req.get(\"jsonrpc\").asText())) {\n");
		sb.append(
				"                        String method = req.has(\"method\") ? req.get(\"method\").asText() : \"\";\n");
		sb.append("                        if (\"initialize\".equals(method)) {\n");
		sb.append(
				"                            response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"result\\\":{\\\"protocolVersion\\\":\\\"2024-11-05\\\",\\\"capabilities\\\":{\\\"tools\\\":{}},\\\"serverInfo\\\":{\\\"name\\\":\\\"sdk-mcp\\\",\\\"version\\\":\\\"0.0.4\\\"}},\\\"id\\\":\" + idStr + \"}\";\n");
		sb.append(
				"                        } else if (\"notifications/initialized\".equals(method) || \"initialized\".equals(method)) {\n");
		sb.append("                            continue;\n");
		sb.append("                        } else if (\"ping\".equals(method)) {\n");
		sb.append(
				"                            response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"result\\\":{},\\\"id\\\":\" + idStr + \"}\";\n");
		sb.append(
				"                        } else if (\"$/cancelRequest\".equals(method) || \"cancelled\".equals(method)) {\n");
		sb.append("                            continue;\n");
		sb.append("                        } else if (\"tools/list\".equals(method)) {\n");
		sb.append("                            ObjectNode result = mapper.createObjectNode();\n");
		sb.append("                            ArrayNode tools = mapper.createArrayNode();\n");
		if (api.paths != null && api.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> pe : api.paths.pathItems.entrySet()) {
				String path = pe.getKey();
				PathItem pi = pe.getValue();
				if (pi.get != null)
					appendToolSchema(sb, "get", path, pi.get);
				if (pi.post != null)
					appendToolSchema(sb, "post", path, pi.post);
				if (pi.put != null)
					appendToolSchema(sb, "put", path, pi.put);
				if (pi.delete != null)
					appendToolSchema(sb, "delete", path, pi.delete);
				if (pi.patch != null)
					appendToolSchema(sb, "patch", path, pi.patch);
			}
		}
		sb.append("                            result.set(\"tools\", tools);\n");
		sb.append("                            ObjectNode resObj = mapper.createObjectNode();\n");
		sb.append("                            resObj.put(\"jsonrpc\", \"2.0\");\n");
		sb.append("                            resObj.set(\"result\", result);\n");
		sb.append("                            resObj.set(\"id\", idNode);\n");
		sb.append("                            response = mapper.writeValueAsString(resObj);\n");
		sb.append("                        } else if (\"tools/call\".equals(method)) {\n");
		sb.append("                            JsonNode params = req.get(\"params\");\n");
		sb.append(
				"                            String toolName = (params != null && params.has(\"name\")) ? params.get(\"name\").asText() : \"\";\n");
		sb.append(
				"                            response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"error\\\":{\\\"code\\\":-32601,\\\"message\\\":\\\"Tool not found\\\"},\\\"id\\\":\" + idStr + \"}\";\n");
		if (api.paths != null && api.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> pe : api.paths.pathItems.entrySet()) {
				String path = pe.getKey();
				PathItem pi = pe.getValue();
				if (pi.get != null)
					appendToolCall(sb, "get", path, pi.get);
				if (pi.post != null)
					appendToolCall(sb, "post", path, pi.post);
				if (pi.put != null)
					appendToolCall(sb, "put", path, pi.put);
				if (pi.delete != null)
					appendToolCall(sb, "delete", path, pi.delete);
				if (pi.patch != null)
					appendToolCall(sb, "patch", path, pi.patch);
			}
		}
		sb.append(
				"                        } else if (\"notifications/progress\".equals(method) || \"progress\".equals(method)) {\n");
		sb.append("                            continue;\n");
		sb.append("                        } else {\n");
		sb.append(
				"                            response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"error\\\":{\\\"code\\\":-32601,\\\"message\\\":\\\"Method not found\\\"},\\\"id\\\":\" + idStr + \"}\";\n");
		sb.append("                        }\n");
		sb.append("                    } else {\n");
		sb.append(
				"                        response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"error\\\":{\\\"code\\\":-32600,\\\"message\\\":\\\"Invalid Request\\\"},\\\"id\\\":null}\";\n");
		sb.append("                    }\n");
		sb.append("                } catch (Exception e) {\n");
		sb.append(
				"                    response = \"{\\\"jsonrpc\\\":\\\"2.0\\\",\\\"error\\\":{\\\"code\\\":-32700,\\\"message\\\":\\\"Parse error\\\"},\\\"id\\\":null}\";\n");
		sb.append("                }\n");
		sb.append("                System.out.println(response);\n");
		sb.append("                System.out.flush();\n");
		sb.append("            }\n");
		sb.append("        } catch (Exception e) {\n");
		sb.append("            e.printStackTrace();\n");
		sb.append("        }\n");
		sb.append("    }\n");
	}

	/**
	 * Appends an MCP tool schema definition block for an OpenAPI operation.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param method
	 *            The HTTP method (lowercase, e.g. {@code get}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 */
	private static void appendToolSchema(StringBuilder sb, String method, String path, Operation op) {
		String toolName = (op.operationId != null
				? op.operationId
				: method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_"));
		String desc = (op.summary != null ? op.summary : (op.description != null ? op.description : ""));
		sb.append("                            {\n");
		sb.append("                                ObjectNode tool = mapper.createObjectNode();\n");
		sb.append("                                tool.put(\"name\", \"").append(escape(toolName)).append("\");\n");
		sb.append("                                tool.put(\"description\", \"").append(escape(desc)).append("\");\n");
		sb.append("                                ObjectNode inputSchema = mapper.createObjectNode();\n");
		sb.append("                                inputSchema.put(\"type\", \"object\");\n");
		sb.append("                                ObjectNode properties = mapper.createObjectNode();\n");
		sb.append("                                ArrayNode required = mapper.createArrayNode();\n");
		if (op.parameters != null) {
			for (ReferenceOr<Parameter> pObj : op.parameters) {
				if (pObj == null)
					continue;
				Parameter p = pObj.value;
				sb.append("                                {\n");
				sb.append("                                    ObjectNode prop = mapper.createObjectNode();\n");
				sb.append("                                    prop.put(\"type\", \"string\");\n");
				if (p.description != null) {
					sb.append("                                    prop.put(\"description\", \"")
							.append(escape(p.description)).append("\");\n");
				}
				sb.append("                                    properties.set(\"").append(escape(p.name))
						.append("\", prop);\n");
				if (p.required != null && p.required) {
					sb.append("                                    required.add(\"").append(escape(p.name))
							.append("\");\n");
				}
				sb.append("                                }\n");
			}
		}
		if (op.requestBody != null && op.requestBody.value != null) {
			RequestBody rb = op.requestBody.value;
			sb.append("                                {\n");
			sb.append("                                    ObjectNode prop = mapper.createObjectNode();\n");
			sb.append("                                    prop.put(\"type\", \"string\");\n");
			if (rb.description != null) {
				sb.append("                                    prop.put(\"description\", \"")
						.append(escape(rb.description)).append("\");\n");
			}
			sb.append("                                    properties.set(\"requestBody\", prop);\n");
			if (rb.required != null && rb.required) {
				sb.append("                                    required.add(\"requestBody\");\n");
			}
			sb.append("                                }\n");
		}
		sb.append("                                inputSchema.set(\"properties\", properties);\n");
		sb.append("                                inputSchema.set(\"required\", required);\n");
		sb.append("                                tool.set(\"inputSchema\", inputSchema);\n");
		sb.append("                                tools.add(tool);\n");
		sb.append("                            }\n");
	}

	/**
	 * Appends an MCP {@code tools/call} dispatch block for an OpenAPI operation.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param method
	 *            The HTTP method (lowercase, e.g. {@code get}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 */
	private static void appendToolCall(StringBuilder sb, String method, String path, Operation op) {
		String toolName = (op.operationId != null
				? op.operationId
				: method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_"));
		sb.append("                            if (\"").append(escape(toolName)).append("\".equals(toolName)) {\n");
		sb.append("                                ObjectNode callRes = mapper.createObjectNode();\n");
		sb.append("                                ArrayNode content = mapper.createArrayNode();\n");
		sb.append("                                ObjectNode textItem = mapper.createObjectNode();\n");
		sb.append("                                textItem.put(\"type\", \"text\");\n");
		sb.append("                                textItem.put(\"text\", \"Executed ").append(escape(toolName))
				.append("\");\n");
		sb.append("                                content.add(textItem);\n");
		sb.append("                                callRes.set(\"content\", content);\n");
		sb.append("                                ObjectNode resObj = mapper.createObjectNode();\n");
		sb.append("                                resObj.put(\"jsonrpc\", \"2.0\");\n");
		sb.append("                                resObj.set(\"result\", callRes);\n");
		sb.append("                                resObj.set(\"id\", idNode);\n");
		sb.append("                                response = mapper.writeValueAsString(resObj);\n");
		sb.append("                            }\n");
	}
}
