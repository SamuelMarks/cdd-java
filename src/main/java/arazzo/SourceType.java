package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 {@link SourceDescription}
 * specification types ({@code openapi}, {@code asyncapi}, {@code arazzo}).
 */
public enum SourceType {
	/** OpenAPI specification source description. */
	OPENAPI("openapi"),
	/** AsyncAPI specification source description. */
	ASYNCAPI("asyncapi"),
	/** Nested Arazzo workflow specification source description. */
	ARAZZO("arazzo");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code SourceType} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	SourceType(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this source type.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link SourceType}, or returns {@code null} if
	 * unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link SourceType}, or {@code null}.
	 */
	public static SourceType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (SourceType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
