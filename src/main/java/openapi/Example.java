package openapi;

import common.ExtensibleElement;
import common.PayloadValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example object.
 */
public class Example extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Example() {
	}

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The value property.
	 */
	public PayloadValue value;

	/**
	 * The externalValue property.
	 */
	public String externalValue;

	/**
	 * The dataValue property.
	 */
	public PayloadValue dataValue;

	/**
	 * The serializedValue property.
	 */
	public String serializedValue;
	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(summary, description, value, externalValue, dataValue, serializedValue,
				extensions);
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
		if (!(o instanceof Example)) {
			return false;
		}
		return fieldValues().equals(((Example) o).fieldValues());
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
		return "Example" + fieldValues();
	}
}
