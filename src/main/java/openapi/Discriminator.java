package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discriminator object.
 */
public class Discriminator extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Discriminator() {
	}

	/**
	 * The propertyName property.
	 */
	public String propertyName;

	/**
	 * The mapping property.
	 */
	public Map<String, String> mapping;

	/**
	 * The defaultMapping property.
	 */
	public String defaultMapping;
	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(propertyName, mapping, defaultMapping, extensions);
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
		if (!(o instanceof Discriminator)) {
			return false;
		}
		return fieldValues().equals(((Discriminator) o).fieldValues());
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
		return "Discriminator" + fieldValues();
	}
}
