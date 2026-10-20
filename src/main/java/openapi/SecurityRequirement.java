package openapi;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * SecurityRequirement object.
 */
public class SecurityRequirement {
	/**
	 * Default constructor.
	 */
	public SecurityRequirement() {
	}

	/**
	 * Requirements.
	 */
	public Map<String, List<String>> requirements = new HashMap<>();

	/**
	 * Add requirement.
	 *
	 * @param name
	 *            Name
	 * @param value
	 *            Value
	 */
	public void addRequirement(String name, List<String> value) {
		requirements.put(name, value);
	}

	/**
	 * Get requirements.
	 *
	 * @return requirements
	 */
	public Map<String, List<String>> getRequirements() {
		return requirements;
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(requirements);
	}

	/**
	 * Compares this object with another for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SecurityRequirement)) {
			return false;
		}
		return fieldValues().equals(((SecurityRequirement) o).fieldValues());
	}

	/**
	 * Returns a structural hash code for this object.
	 *
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		return fieldValues().hashCode();
	}

	/**
	 * Returns a structural string representation of this object.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return "SecurityRequirement" + fieldValues();
	}
}
