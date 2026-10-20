package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Paths object.
 */
public class Paths {
	/**
	 * Default constructor.
	 */
	public Paths() {
	}

	/**
	 * Path items.
	 */
	public Map<String, PathItem> pathItems = new HashMap<>();
	/**
	 * Extensions.
	 */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Add property.
	 *
	 * @param name
	 *            Name
	 * @param value
	 *            Value
	 */
	public void addProperty(String name, Object value) {
		if (name.startsWith("x-")) {
			extensions.put(name, value);
		} else if (name.startsWith("/")) {
			if (value instanceof PathItem) {
				pathItems.put(name, (PathItem) value);
			}
		}
	}

	/**
	 * Get properties.
	 *
	 * @return properties
	 */
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>(extensions);
		props.putAll(pathItems);
		return props;
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(pathItems, extensions);
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
		if (!(o instanceof Paths)) {
			return false;
		}
		return fieldValues().equals(((Paths) o).fieldValues());
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
		return "Paths" + fieldValues();
	}
}
