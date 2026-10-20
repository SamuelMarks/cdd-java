package openapi;

import common.ExtensibleElement;
import common.PayloadValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Link object.
 */
public class Link extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Link() {
	}

	/**
	 * The operationRef property.
	 */
	public String operationRef;

	/**
	 * The operationId property.
	 */
	public String operationId;

	/**
	 * The parameters property.
	 */
	public Map<String, PayloadValue> parameters;

	/**
	 * The requestBody property.
	 */
	public PayloadValue requestBody;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The server property.
	 */
	public Server server;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(operationRef, operationId, parameters, requestBody, description, server,
				extensions);
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
		if (!(o instanceof Link)) {
			return false;
		}
		return fieldValues().equals(((Link) o).fieldValues());
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
		return "Link" + fieldValues();
	}
}
