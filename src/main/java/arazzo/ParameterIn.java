package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 parameter locations ({@code path},
 * {@code query}, {@code querystring}, {@code header}, {@code cookie}).
 */
public enum ParameterIn {
	/** URL path template parameter. */
	PATH("path"),
	/** Individual URL query parameter. */
	QUERY("query"),
	/** Entire raw URL query string parameter (Arazzo 1.1.0 / OpenAPI 3.2.0). */
	QUERYSTRING("querystring"),
	/** HTTP request or message header parameter. */
	HEADER("header"),
	/** HTTP Cookie header parameter. */
	COOKIE("cookie");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code ParameterIn} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	ParameterIn(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this parameter location.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link ParameterIn}, or returns {@code null} if
	 * unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link ParameterIn}, or {@code null}.
	 */
	public static ParameterIn fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (ParameterIn p : values()) {
			if (p.value.equalsIgnoreCase(raw)) {
				return p;
			}
		}
		return null;
	}
}
