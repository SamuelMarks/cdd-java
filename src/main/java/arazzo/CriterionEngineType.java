package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 criterion and selector evaluation
 * engine dialects ({@code simple}, {@code regex}, {@code jsonpath},
 * {@code xpath}, {@code jsonpointer}).
 */
public enum CriterionEngineType {
	/** Simple relational and boolean expression evaluation. */
	SIMPLE("simple"),
	/** Regular expression pattern matching. */
	REGEX("regex"),
	/** JSONPath query evaluation (RFC 9535 or Goessner draft). */
	JSONPATH("jsonpath"),
	/** XPath query evaluation over XML payloads (XPath 1.0, 2.0, or 3.1). */
	XPATH("xpath"),
	/** JSON Pointer evaluation (RFC 6901, Arazzo 1.1.0). */
	JSONPOINTER("jsonpointer");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code CriterionEngineType} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	CriterionEngineType(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this criterion engine type.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link CriterionEngineType}, or returns
	 * {@code null} if unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link CriterionEngineType}, or {@code null}.
	 */
	public static CriterionEngineType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (CriterionEngineType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
