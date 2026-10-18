package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 {@link FailureAction} types
 * ({@code end}, {@code retry}, {@code goto}).
 */
public enum FailureActionType {
	/** Terminates workflow execution immediately and propagates the failure. */
	END("end"),
	/** Retries the failed step (optionally after executing a recovery target). */
	RETRY("retry"),
	/** Branches execution to a fallback step or recovery workflow. */
	GOTO("goto");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code FailureActionType} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	FailureActionType(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this failure action type.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link FailureActionType}, or returns
	 * {@code null} if unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link FailureActionType}, or {@code null}.
	 */
	public static FailureActionType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (FailureActionType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
