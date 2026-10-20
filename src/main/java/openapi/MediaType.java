package openapi;

import common.ExtensibleElement;
import common.PayloadValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaType object.
 */
public class MediaType extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public MediaType() {
	}

	/**
	 * The schema property.
	 */
	public ReferenceOr<Schema> schema;

	/**
	 * The itemSchema property.
	 */
	public ReferenceOr<Schema> itemSchema;

	/**
	 * The example property.
	 */
	public PayloadValue example;

	/**
	 * The examples property.
	 */
	public Map<String, ReferenceOr<Example>> examples;

	/**
	 * The encoding property.
	 */
	public Map<String, Encoding> encoding;

	/**
	 * The prefixEncoding property.
	 */
	public List<Encoding> prefixEncoding;

	/**
	 * The itemEncoding property.
	 */
	public Encoding itemEncoding;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(schema, itemSchema, example, examples, encoding, prefixEncoding, itemEncoding,
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
		if (!(o instanceof MediaType)) {
			return false;
		}
		return fieldValues().equals(((MediaType) o).fieldValues());
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
		return "MediaType" + fieldValues();
	}
}
