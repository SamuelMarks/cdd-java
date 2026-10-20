package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RequestBody object.
 */
public class RequestBody extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public RequestBody() {
	}

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The content property.
	 */
	public Map<String, MediaType> content;

	/**
	 * The required property.
	 */
	public Boolean required;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(description, content, required, extensions);
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
		if (!(o instanceof RequestBody)) {
			return false;
		}
		return fieldValues().equals(((RequestBody) o).fieldValues());
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
		return "RequestBody" + fieldValues();
	}
}
