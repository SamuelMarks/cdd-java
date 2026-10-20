package arazzo;

import common.PayloadValue;

/**
 * Arazzo Reusable Object referencing a component definition in
 * {@code $components.parameters.*}, {@code $components.successActions.*}, or
 * {@code $components.failureActions.*}, with an optional value override.
 */
public class ReusableObject {
	/**
	 * Default constructor.
	 */
	public ReusableObject() {
	}

	/**
	 * Runtime expression pointing to the reusable component (e.g.
	 * {@code $components.parameters.petId}).
	 */
	public String reference;

	/**
	 * Optional overridden value when referencing a reusable parameter.
	 */
	public PayloadValue value;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(reference, value);
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
		if (!(o instanceof ReusableObject)) {
			return false;
		}
		return fieldValues().equals(((ReusableObject) o).fieldValues());
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
		return "ReusableObject" + fieldValues();
	}
}
