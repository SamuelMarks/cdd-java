package common;

/**
 * Shared RFC 6901 JSON Pointer escaping and unescaping utilities used across
 * OpenAPI 3.2.0 and Arazzo 1.1.0 reference and runtime expression resolvers.
 */
public final class JsonPointerUtils {
	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private JsonPointerUtils() {
	}

	/**
	 * Unescapes RFC 6901 JSON Pointer escape sequences ({@code ~1} -> {@code /},
	 * {@code ~0} -> {@code ~}) in the RFC-mandated order.
	 *
	 * @param token
	 *            The escaped pointer token.
	 * @return The unescaped token (or empty string if {@code token} is
	 *         {@code null}).
	 */
	public static String unescapePointerToken(String token) {
		if (token == null) {
			return "";
		}
		return token.replace("~1", "/").replace("~0", "~");
	}

	/**
	 * Escapes a literal property token into an RFC 6901 JSON Pointer segment
	 * ({@code ~} -> {@code ~0}, {@code /} -> {@code ~1}).
	 *
	 * @param token
	 *            The raw property token.
	 * @return The escaped RFC 6901 pointer segment (or empty string if
	 *         {@code token} is {@code null}).
	 */
	public static String escapePointerToken(String token) {
		if (token == null) {
			return "";
		}
		return token.replace("~", "~0").replace("/", "~1");
	}
}
