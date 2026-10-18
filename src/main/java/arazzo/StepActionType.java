package arazzo;

/**
 * Strongly typed enumeration of Arazzo 1.1.0 AsyncAPI step action directions
 * ({@code send}, {@code receive}).
 */
public enum StepActionType {
	/** Sends/publishes a message to the target AsyncAPI channel. */
	SEND("send"),
	/** Receives/subscribes to a message from the target AsyncAPI channel. */
	RECEIVE("receive");

	/** The wire string representation in Arazzo JSON/YAML. */
	private final String value;

	/**
	 * Constructs a {@code StepActionType} with the given wire value.
	 *
	 * @param value
	 *            The wire string representation.
	 */
	StepActionType(String value) {
		this.value = value;
	}

	/**
	 * Returns the wire string representation of this step action type.
	 *
	 * @return The wire string value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a wire string into a {@link StepActionType}, or returns {@code null}
	 * if unrecognized or {@code null}.
	 *
	 * @param raw
	 *            The raw wire string.
	 * @return The matching {@link StepActionType}, or {@code null}.
	 */
	public static StepActionType fromString(String raw) {
		if (raw == null) {
			return null;
		}
		for (StepActionType t : values()) {
			if (t.value.equalsIgnoreCase(raw)) {
				return t;
			}
		}
		return null;
	}
}
