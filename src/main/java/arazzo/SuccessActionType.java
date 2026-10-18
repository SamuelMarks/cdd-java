package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 {@link SuccessAction} types
 * ({@code end}, {@code goto}).
 */
public enum SuccessActionType {
	/** Terminates the workflow immediately upon step success. */
	END("end"),
	/** Branches execution to another step or workflow upon step success. */
	GOTO("goto");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code SuccessActionType} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	SuccessActionType(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this success action type.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link SuccessActionType}, or returns
	 * {@code null} if unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link SuccessActionType}, or {@code null}.
	 */
	public static SuccessActionType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (SuccessActionType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
