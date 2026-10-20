package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML object.
 */
public class XML {
	/**
	 * Default constructor.
	 */
	public XML() {
	}

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The namespace property.
	 */
	public String namespace;

	/**
	 * The prefix property.
	 */
	public String prefix;

	/**
	 * The attribute property.
	 */
	public Boolean attribute;

	/**
	 * The wrapped property.
	 */
	public Boolean wrapped;

	/**
	 * The nodeType property.
	 */
	public String nodeType;
	/**
	 * Extensions.
	 */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Get extensions.
	 *
	 * @return extensions
	 */
	public Map<String, Object> getExtensions() {
		return extensions;
	}

	/**
	 * Add extension.
	 *
	 * @param name
	 *            extension name
	 * @param value
	 *            extension value
	 */
	public void addExtension(String name, Object value) {
		if (name.startsWith("x-"))
			extensions.put(name, value);
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(name, namespace, prefix, attribute, wrapped, nodeType, extensions);
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
		if (!(o instanceof XML)) {
			return false;
		}
		return fieldValues().equals(((XML) o).fieldValues());
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
		return "XML" + fieldValues();
	}
}
