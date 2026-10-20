package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PathItem object.
 */
public class PathItem {
	/**
	 * Default constructor.
	 */
	public PathItem() {
	}

	/**
	 * The ref property.
	 */
	public String ref;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The get property.
	 */
	public Operation get;

	/**
	 * The put property.
	 */
	public Operation put;

	/**
	 * The post property.
	 */
	public Operation post;

	/**
	 * The delete property.
	 */
	public Operation delete;

	/**
	 * The options property.
	 */
	public Operation options;

	/**
	 * The head property.
	 */
	public Operation head;

	/**
	 * The patch property.
	 */
	public Operation patch;

	/**
	 * The trace property.
	 */
	public Operation trace;

	/**
	 * The query property.
	 */
	public Operation query;

	/**
	 * The additionalOperations property.
	 */
	public Map<String, Operation> additionalOperations;

	/**
	 * The servers property.
	 */
	public List<Server> servers;

	/**
	 * The parameters property.
	 */
	public List<ReferenceOr<Parameter>> parameters;

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
		return java.util.Arrays.asList(ref, summary, description, get, put, post, delete, options, head, patch, trace,
				query, additionalOperations, servers, parameters, extensions);
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
		if (!(o instanceof PathItem)) {
			return false;
		}
		return fieldValues().equals(((PathItem) o).fieldValues());
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
		return "PathItem" + fieldValues();
	}
}
