package openapi;

/**
 * Strongly typed OpenAPI 3.2.0 and Swagger 2.0 parameter and security scheme
 * locations ({@code in}).
 */
public enum ParameterLocation {
	/** Query string parameter ({@code "query"}). */
	QUERY("query"),
	/** HTTP header parameter ({@code "header"}). */
	HEADER("header"),
	/** URL path template parameter ({@code "path"}). */
	PATH("path"),
	/** HTTP cookie parameter ({@code "cookie"}). */
	COOKIE("cookie"),
	/** Swagger 2.0 request body parameter ({@code "body"}). */
	BODY("body"),
	/** Swagger 2.0 form data parameter ({@code "formData"}). */
	FORM_DATA("formData");

	/** Specification wire value. */
	public final String value;

	/**
	 * Constructs a {@code ParameterLocation} with its wire string.
	 *
	 * @param value
	 *            The OpenAPI/Swagger wire string.
	 */
	ParameterLocation(String value) {
		this.value = value;
	}

	/**
	 * Parses a wire string into a {@link ParameterLocation}, or {@code null} if
	 * unrecognized.
	 *
	 * @param raw
	 *            The wire string.
	 * @return The matching {@link ParameterLocation}, or {@code null}.
	 */
	public static ParameterLocation fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (ParameterLocation loc : values()) {
			if (loc.value.equalsIgnoreCase(raw)) {
				return loc;
			}
		}
		return null;
	}
}
