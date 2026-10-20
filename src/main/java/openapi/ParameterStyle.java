package openapi;

/**
 * Strongly typed OpenAPI 3.2.0 parameter, header, and encoding serialization
 * styles ({@code style}).
 */
public enum ParameterStyle {
	/** Matrix-style path parameters ({@code "matrix"}). */
	MATRIX("matrix"),
	/** Label-style path parameters ({@code "label"}). */
	LABEL("label"),
	/** Form-style query/cookie parameters ({@code "form"}). */
	FORM("form"),
	/** Simple comma-separated parameters ({@code "simple"}). */
	SIMPLE("simple"),
	/** Space-delimited array/object parameters ({@code "spaceDelimited"}). */
	SPACE_DELIMITED("spaceDelimited"),
	/** Pipe-delimited array/object parameters ({@code "pipeDelimited"}). */
	PIPE_DELIMITED("pipeDelimited"),
	/** Deep-object bracketed parameters ({@code "deepObject"}). */
	DEEP_OBJECT("deepObject"),
	/** Cookie serialization style ({@code "cookie"}). */
	COOKIE("cookie");

	/** Specification wire value. */
	public final String value;

	/**
	 * Constructs a {@code ParameterStyle} with its wire string.
	 *
	 * @param value
	 *            The OpenAPI wire string.
	 */
	ParameterStyle(String value) {
		this.value = value;
	}

	/**
	 * Parses a wire string into a {@link ParameterStyle}, or {@code null} if
	 * unrecognized.
	 *
	 * @param raw
	 *            The wire string.
	 * @return The matching {@link ParameterStyle}, or {@code null}.
	 */
	public static ParameterStyle fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (ParameterStyle s : values()) {
			if (s.value.equalsIgnoreCase(raw)) {
				return s;
			}
		}
		return null;
	}
}
