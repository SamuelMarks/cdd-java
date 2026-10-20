package openapi;

/**
 * Provides identifier sanitization and Java string literal escaping utilities.
 */
public final class IdentifierSanitizer {
	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private IdentifierSanitizer() {
	}

	/**
	 * Sanitizes a raw name into a valid Java identifier containing only ASCII
	 * letters, digits, and underscores.
	 *
	 * @param name
	 *            The raw identifier string.
	 * @return The sanitized identifier string (or empty string if {@code name} is
	 *         {@code null}).
	 */
	public static String sanitizeIdentifier(String name) {
		if (name == null) {
			return "";
		}
		return name.replaceAll("[^a-zA-Z0-9_]", "");
	}

	/**
	 * Sanitizes a raw string into strictly alphanumeric ASCII characters.
	 *
	 * @param name
	 *            The raw string.
	 * @return The sanitized alphanumeric string (or empty string if {@code name} is
	 *         {@code null}).
	 */
	public static String sanitizeAlphanumeric(String name) {
		if (name == null) {
			return "";
		}
		return name.replaceAll("[^a-zA-Z0-9]", "");
	}

	/**
	 * Escapes backslashes, double quotes, newlines, and carriage returns for Java
	 * string literals.
	 *
	 * @param value
	 *            The input string to escape.
	 * @return The escaped string (or empty string if {@code value} is
	 *         {@code null}).
	 */
	public static String escape(String value) {
		if (value == null) {
			return "";
		}
		String bs = String.valueOf((char) 92);
		String qt = String.valueOf((char) 34);
		String nl = String.valueOf((char) 10);
		String cr = String.valueOf((char) 13);
		return value.replace(bs, bs + bs).replace(qt, bs + qt).replace(nl, bs + "n").replace(cr, bs + "r");
	}
}
