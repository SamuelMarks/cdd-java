package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Responses object.
 */
public class Responses {
	/**
	 * Default constructor.
	 */
	public Responses() {
	}

	/**
	 * Default response.
	 */
	public ReferenceOr<Response> defaultResponse;
	/**
	 * Status codes.
	 */
	public Map<String, ReferenceOr<Response>> statusCodes = new HashMap<>();
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
	@SuppressWarnings("unchecked")
	public void addProperty(String name, Object value) {
		if (name.equals("default")) {
			if (value instanceof ReferenceOr) {
				defaultResponse = (ReferenceOr<Response>) value;
			} else if (value instanceof Response) {
				defaultResponse = ReferenceOr.ofValue((Response) value);
			}
		} else if (name.startsWith("x-")) {
			extensions.put(name, value);
		} else if (value instanceof ReferenceOr) {
			statusCodes.put(name, (ReferenceOr<Response>) value);
		} else if (value instanceof Response) {
			statusCodes.put(name, ReferenceOr.ofValue((Response) value));
		}
	}

	/**
	 * Get properties.
	 *
	 * @return properties
	 */
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>(statusCodes);
		props.putAll(extensions);
		return props;
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(defaultResponse, statusCodes, extensions);
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
		if (!(o instanceof Responses)) {
			return false;
		}
		return fieldValues().equals(((Responses) o).fieldValues());
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
		return "Responses" + fieldValues();
	}
}
