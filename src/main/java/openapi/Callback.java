package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Callback object.
 */
public class Callback extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Callback() {
	}

	/**
	 * Path items.
	 */
	public Map<String, PathItem> pathItems = new HashMap<>();

	/**
	 * Adds a callback runtime expression and its associated {@link PathItem}.
	 *
	 * @param expression
	 *            The callback runtime expression key.
	 * @param pathItem
	 *            The {@link PathItem} definition.
	 */
	public void addPathItem(String expression, PathItem pathItem) {
		if (expression != null && pathItem != null && !expression.startsWith("x-")) {
			pathItems.put(expression, pathItem);
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
		if (!(o instanceof Callback)) {
			return false;
		}
		return fieldValues().equals(((Callback) o).fieldValues());
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
		return "Callback" + fieldValues();
	}
}
