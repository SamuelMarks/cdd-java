package openapi;

/**
 * Strongly typed HTTP method verbs supported by OpenAPI 3.2.0 {@link PathItem}
 * operations.
 */
public enum HttpVerb {
	/** HTTP GET method. */
	GET("get"),
	/** HTTP PUT method. */
	PUT("put"),
	/** HTTP POST method. */
	POST("post"),
	/** HTTP DELETE method. */
	DELETE("delete"),
	/** HTTP OPTIONS method. */
	OPTIONS("options"),
	/** HTTP HEAD method. */
	HEAD("head"),
	/** HTTP PATCH method. */
	PATCH("patch"),
	/** HTTP TRACE method. */
	TRACE("trace"),
	/** HTTP QUERY method (OpenAPI 3.2.0). */
	QUERY("query");

	/** Lowercase specification property name. */
	public final String specName;

	/**
	 * Constructs an {@code HttpVerb} with its OpenAPI property key.
	 *
	 * @param specName
	 *            The lowercase OpenAPI property key.
	 */
	HttpVerb(String specName) {
		this.specName = specName;
	}

	/**
	 * Resolves a string token (case-insensitive) to a standard {@link HttpVerb}, or
	 * {@code null} if the token is null or represents a custom/additional HTTP
	 * method.
	 *
	 * @param token
	 *            The HTTP method string (e.g. {@code "GET"} or {@code "post"}).
	 * @return The matching {@link HttpVerb}, or {@code null}.
	 */
	public static HttpVerb fromString(String token) {
		if (token == null) {
			return null;
		}
		for (HttpVerb v : values()) {
			if (v.name().equalsIgnoreCase(token)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Resolves a string token (case-insensitive) to a standard {@link HttpVerb},
	 * falling back to {@code defaultVerb} when unrecognized or {@code null}.
	 *
	 * @param token
	 *            The HTTP method string.
	 * @param defaultVerb
	 *            The fallback {@link HttpVerb}.
	 * @return The resolved {@link HttpVerb}.
	 */
	public static HttpVerb fromStringOrDefault(String token, HttpVerb defaultVerb) {
		HttpVerb verb = fromString(token);
		return verb != null ? verb : defaultVerb;
	}
}
