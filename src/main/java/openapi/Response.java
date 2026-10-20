package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Response object.
 */
public class Response {
	/**
	 * Default constructor.
	 */
	public Response() {
	}

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The schema property.
	 */
	public ReferenceOr<Schema> schema;

	/**
	 * The headers property.
	 */
	public Map<String, ReferenceOr<Header>> headers;

	/**
	 * The examples property.
	 */
	public Map<String, ReferenceOr<Example>> examples;

	/**
	 * The content property.
	 */
	public Map<String, MediaType> content;

	/**
	 * The links property.
	 */
	public Map<String, ReferenceOr<Link>> links;

	/**
	 * The summary property.
	 */
	public String summary;
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
		return java.util.Arrays.asList(description, schema, headers, examples, content, links, summary, extensions);
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
		if (!(o instanceof Response)) {
			return false;
		}
		return fieldValues().equals(((Response) o).fieldValues());
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
		return "Response" + fieldValues();
	}
}
