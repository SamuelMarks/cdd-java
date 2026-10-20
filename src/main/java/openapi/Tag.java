package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag object.
 */
public class Tag extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Tag() {
	}

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The externalDocs property.
	 */
	public ExternalDocumentation externalDocs;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The parent property.
	 */
	public String parent;

	/**
	 * The kind property.
	 */
	public String kind;
	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(name, description, externalDocs, summary, parent, kind, extensions);
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
		if (!(o instanceof Tag)) {
			return false;
		}
		return fieldValues().equals(((Tag) o).fieldValues());
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
		return "Tag" + fieldValues();
	}
}
