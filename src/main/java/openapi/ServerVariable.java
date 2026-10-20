package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServerVariable object.
 */
public class ServerVariable extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public ServerVariable() {
	}

	/**
	 * The enumValues property.
	 */
	public List<String> enumValues;

	/**
	 * The defaultValue property.
	 */
	public String defaultValue;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(enumValues, defaultValue, description, extensions);
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
		if (!(o instanceof ServerVariable)) {
			return false;
		}
		return fieldValues().equals(((ServerVariable) o).fieldValues());
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
		return "ServerVariable" + fieldValues();
	}
}
