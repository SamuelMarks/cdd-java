package cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import openapi.OpenAPI;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON-RPC 2.0 and Model Context Protocol (MCP) server implementation for
 * {@link CddCli}.
 */
public class McpJsonRpcServer {

	/**
	 * Default constructor.
	 */
	public McpJsonRpcServer() {
	}

	/**
	 * Handles a single HTTP JSON-RPC 2.0 exchange on the embedded
	 * {@link com.sun.net.httpserver.HttpServer}.
	 *
	 * @param exchange
	 *            The incoming {@link com.sun.net.httpserver.HttpExchange}.
	 * @throws java.io.IOException
	 *             If reading the request or writing the response fails.
	 */
	public static void handleHttpExchange(com.sun.net.httpserver.HttpExchange exchange) throws java.io.IOException {
		byte[] reqBytes = exchange.getRequestBody().readAllBytes();
		String reqBody = new String(reqBytes, StandardCharsets.UTF_8).trim();
		String resp = handleJsonRpc(reqBody);
		if (resp != null) {
			byte[] respBytes = resp.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, respBytes.length);
			try (java.io.OutputStream os = exchange.getResponseBody()) {
				os.write(respBytes);
			}
		} else {
			exchange.sendResponseHeaders(204, -1);
		}
	}

	/**
	 * Starts an HTTP JSON-RPC 2.0 / MCP server on the specified host and port, also
	 * processing any lines buffered on standard input.
	 *
	 * @param listen
	 *            The host address to bind to.
	 * @param port
	 *            The TCP port to listen on (0 for an ephemeral port).
	 * @throws Exception
	 *             If starting or running the server fails.
	 */
	public static void serveJsonRpcHttp(String listen, int port) throws Exception {
		if (CddCli.activeHttpServer != null) {
			CddCli.activeHttpServer.stop(0);
			CddCli.activeHttpServer = null;
		}
		com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
				.create(new java.net.InetSocketAddress(listen, port), 0);
		server.createContext("/", McpJsonRpcServer::handleHttpExchange);
		server.start();
		CddCli.activeHttpServer = server;
		serveJsonRpcStdio();
	}

	/**
	 * Starts the JSON-RPC 2.0 / MCP server loop over standard input and standard
	 * output.
	 *
	 * @throws Exception
	 *             If reading from standard input fails.
	 */
	public static void serveJsonRpcStdio() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			String response = handleJsonRpc(line);
			if (response != null) {
				System.out.println(response);
				System.out.flush();
			}
		}
	}

	/**
	 * Builds the JSON-RPC result payload for the MCP {@code tools/list} method.
	 *
	 * @param idStr
	 *            The serialized JSON-RPC request ID.
	 * @return The serialized JSON-RPC response string.
	 */
	public static String buildMcpToolsList(String idStr) {
		return "{\"jsonrpc\":\"2.0\",\"result\":{\"tools\":[{\"name\":\"cdd_generate\",\"description\":\"Generate code from OpenAPI or sync code\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"command\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"files\":{\"type\":\"object\"}},\"required\":[\"command\"]}}]},\"id\":"
				+ idStr + "}";
	}

	/**
	 * Processes a single JSON-RPC 2.0 request line and returns the serialized
	 * JSON-RPC response string, or {@code null} for notifications that require no
	 * response.
	 *
	 * @param line
	 *            The raw JSON-RPC 2.0 request string.
	 * @return The JSON-RPC 2.0 response string, or {@code null} for notifications.
	 */
	public static String handleJsonRpc(String line) {
		try {
			JSONObject req = new JSONObject(line);
			Object idObj = req.has("id") && !req.isNull("id") ? req.get("id") : null;
			String idStr = idObj != null ? JSONObject.valueToString(idObj) : "null";
			if (req.has("jsonrpc") && "2.0".equals(req.getString("jsonrpc"))) {
				String method = req.optString("method", "");
				if ("initialize".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{\"tools\":{},\"resources\":{}},\"serverInfo\":{\"name\":\"cdd-java\",\"version\":\"0.0.4\"}},\"id\":"
							+ idStr + "}";
				} else if ("notifications/initialized".equals(method) || "initialized".equals(method)
						|| "$/cancelRequest".equals(method) || "cancelled".equals(method)
						|| "notifications/progress".equals(method) || "progress".equals(method)) {
					return null;
				} else if ("ping".equals(method)) {
					return JsonRpcResponse.ofResult(idObj, new JSONObject()).toJsonString();
				} else if ("tools/list".equals(method)) {
					return buildMcpToolsList(idStr);
				} else if ("resources/list".equals(method)) {
					return "{\"jsonrpc\":\"2.0\",\"result\":{\"resources\":[{\"uri\":\"cdd://ast/openapi\",\"name\":\"OpenAPI AST\",\"mimeType\":\"application/json\"},{\"uri\":\"cdd://ast/arazzo\",\"name\":\"Arazzo AST\",\"mimeType\":\"application/json\"}]},\"id\":"
							+ idStr + "}";
				} else if ("resources/read".equals(method)) {
					JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
					String uri = params.optString("uri", "");
					if ("cdd://ast/openapi".equals(uri)) {
						OpenAPI api = SyncCommand.extractOpenAPI(CddCli.workspaceDir);
						String astText = openapi.Emit.toString(api);
						return "{\"jsonrpc\":\"2.0\",\"result\":{\"contents\":[{\"uri\":\"cdd://ast/openapi\",\"mimeType\":\"application/json\",\"text\":"
								+ JSONObject.quote(astText) + "}]},\"id\":" + idStr + "}";
					} else if ("cdd://ast/arazzo".equals(uri)) {
						arazzo.Arazzo spec = SyncCommand.extractArazzo(CddCli.workspaceDir);
						String astText = arazzo.Emit.toString(spec);
						return "{\"jsonrpc\":\"2.0\",\"result\":{\"contents\":[{\"uri\":\"cdd://ast/arazzo\",\"mimeType\":\"application/json\",\"text\":"
								+ JSONObject.quote(astText) + "}]},\"id\":" + idStr + "}";
					} else {
						return JsonRpcResponse.ofError(idObj, JsonRpcError.INVALID_PARAMS, "Resource not found")
								.toJsonString();
					}
				} else if ("tools/call".equals(method)) {
					JSONObject params = req.has("params") ? req.getJSONObject("params") : new JSONObject();
					String toolName = params.optString("name", "");
					if ("cdd_generate".equals(toolName)) {
						JSONObject args = params.has("arguments")
								? params.getJSONObject("arguments")
								: new JSONObject();
						java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
						java.io.PrintStream ps = new java.io.PrintStream(baos);
						java.io.PrintStream oldOut = System.out;
						System.setOut(ps);
						try {
							CddCli.processInMemory(args.toString());
						} finally {
							System.setOut(oldOut);
						}
						String output = baos.toString("UTF-8");
						JSONObject content = new JSONObject();
						content.put("type", "text");
						content.put("text", output);
						JSONArray contentArr = new JSONArray();
						contentArr.put(content);
						JSONObject resultObj = new JSONObject();
						resultObj.put("content", contentArr);
						return JsonRpcResponse.ofResult(idObj, resultObj).toJsonString();
					} else {
						return JsonRpcResponse.ofError(idObj, JsonRpcError.METHOD_NOT_FOUND, "Tool not found")
								.toJsonString();
					}
				} else if ("version".equals(method)) {
					return JsonRpcResponse.ofResult(idObj, "0.0.4").toJsonString();
				} else {
					if (idObj == null)
						return null;
					return JsonRpcResponse.ofError(idObj, JsonRpcError.METHOD_NOT_FOUND, "Method not found")
							.toJsonString();
				}
			} else {
				if (idObj == null)
					return null;
				return JsonRpcResponse.ofError(null, JsonRpcError.INVALID_REQUEST, "Invalid Request").toJsonString();
			}
		} catch (Exception e) {
			return JsonRpcResponse.ofError(null, JsonRpcError.PARSE_ERROR, "Parse error").toJsonString();
		}
	}
}
