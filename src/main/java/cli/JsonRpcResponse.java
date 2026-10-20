package cli;

import org.json.JSONObject;

/**
 * Strongly typed JSON-RPC 2.0 response envelope holding either a {@code result}
 * or a {@link JsonRpcError}.
 */
public final class JsonRpcResponse {
	/** The request identifier (or {@code null}). */
	public final Object id;
	/** The successful result value (when {@code error == null}). */
	public final Object result;
	/** The error descriptor (when the request failed). */
	public final JsonRpcError error;

	/**
	 * Constructs a {@code JsonRpcResponse}.
	 *
	 * @param id
	 *            The request identifier.
	 * @param result
	 *            The result payload if successful.
	 * @param error
	 *            The {@link JsonRpcError} if failed.
	 */
	private JsonRpcResponse(Object id, Object result, JsonRpcError error) {
		this.id = id;
		this.result = result;
		this.error = error;
	}

	/**
	 * Creates a successful {@link JsonRpcResponse}.
	 *
	 * @param id
	 *            The request identifier.
	 * @param result
	 *            The result value ({@link JSONObject}, {@link String}, etc.).
	 * @return The constructed {@link JsonRpcResponse}.
	 */
	public static JsonRpcResponse ofResult(Object id, Object result) {
		return new JsonRpcResponse(id, result, null);
	}

	/**
	 * Creates an error {@link JsonRpcResponse}.
	 *
	 * @param id
	 *            The request identifier (or {@code null}).
	 * @param code
	 *            The JSON-RPC 2.0 error code.
	 * @param message
	 *            The error message.
	 * @return The constructed {@link JsonRpcResponse}.
	 */
	public static JsonRpcResponse ofError(Object id, int code, String message) {
		return new JsonRpcResponse(id, null, new JsonRpcError(code, message));
	}

	/**
	 * Serializes this JSON-RPC 2.0 response into a wire JSON string.
	 *
	 * @return The serialized JSON-RPC 2.0 response string.
	 */
	public String toJsonString() {
		String idStr = id != null ? JSONObject.valueToString(id) : "null";
		if (error != null) {
			return "{\"jsonrpc\":\"2.0\",\"error\":" + error.toJson().toString() + ",\"id\":" + idStr + "}";
		}
		return "{\"jsonrpc\":\"2.0\",\"result\":" + JSONObject.valueToString(result) + ",\"id\":" + idStr + "}";
	}
}
