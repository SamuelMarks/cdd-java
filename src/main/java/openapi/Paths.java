package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Paths object.
 */
public class Paths extends ExtensibleElement {
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
	 * Adds a path template and its associated {@link PathItem} if the path starts
	 * with {@code "/"}.
	 *
	 * @param path
	 *            The relative path template starting with {@code "/"}.
	 * @param pathItem
	 *            The {@link PathItem} definition.
	 */
	public void addPathItem(String path, PathItem pathItem) {
		if (path != null && path.startsWith("/") && pathItem != null) {
			pathItems.put(path, pathItem);
		}
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
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
