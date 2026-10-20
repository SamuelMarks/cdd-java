package cli;

import org.json.JSONObject;

/**
 * Strongly typed JSON-RPC 2.0 error object ({@code code}, {@code message}).
 */
public final class JsonRpcError {
	/** Standard JSON-RPC 2.0 parse error code ({@code -32700}). */
	public static final int PARSE_ERROR = -32700;
	/** Standard JSON-RPC 2.0 invalid request error code ({@code -32600}). */
	public static final int INVALID_REQUEST = -32600;
	/** Standard JSON-RPC 2.0 method not found error code ({@code -32601}). */
	public static final int METHOD_NOT_FOUND = -32601;
	/** Standard JSON-RPC 2.0 invalid params error code ({@code -32602}). */
	public static final int INVALID_PARAMS = -32602;

	/** Numeric JSON-RPC 2.0 error code. */
	public final int code;
	/** Human-readable error message. */
	public final String message;

	/**
	 * Constructs a {@code JsonRpcError} with the given code and message.
	 *
	 * @param code
	 *            The JSON-RPC 2.0 error code.
	 * @param message
	 *            The error message.
	 */
	public JsonRpcError(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Serializes this error into a {@link JSONObject}.
	 *
	 * @return The serialized error {@link JSONObject}.
	 */
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("code", code);
		obj.put("message", message);
		return obj;
	}
}
