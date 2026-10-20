package openapi;

/**
 * Strongly typed OpenAPI 3.2.0 and Swagger 2.0 security scheme types
 * ({@code type}).
 */
public enum SecuritySchemeType {
	/** API key security scheme ({@code "apiKey"}). */
	API_KEY("apiKey"),
	/** HTTP authentication security scheme ({@code "http"}). */
	HTTP("http"),
	/** Mutual TLS security scheme ({@code "mutualTLS"}). */
	MUTUAL_TLS("mutualTLS"),
	/** OAuth 2.0 security scheme ({@code "oauth2"}). */
	OAUTH2("oauth2"),
	/** OpenID Connect security scheme ({@code "openIdConnect"}). */
	OPENID_CONNECT("openIdConnect"),
	/** Swagger 2.0 Basic authentication security scheme ({@code "basic"}). */
	BASIC("basic");

	/** Specification wire value. */
	public final String value;

	/**
	 * Constructs a {@code SecuritySchemeType} with its wire string.
	 *
	 * @param value
	 *            The OpenAPI/Swagger wire string.
	 */
	SecuritySchemeType(String value) {
		this.value = value;
	}

	/**
	 * Parses a wire string into a {@link SecuritySchemeType}, or {@code null} if
	 * unrecognized.
	 *
	 * @param raw
	 *            The wire string.
	 * @return The matching {@link SecuritySchemeType}, or {@code null}.
	 */
	public static SecuritySchemeType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (SecuritySchemeType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
