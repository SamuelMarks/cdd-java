package arazzo;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo Info Object providing metadata about the Arazzo description.
 */
public class ArazzoInfo extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public ArazzoInfo() {
	}

	/**
	 * A human-readable title of the Arazzo workflow specification.
	 */
	public String title;

	/**
	 * A short summary of the Arazzo workflow specification.
	 */
	public String summary;

	/**
	 * A detailed description of the purpose of the workflows (supports CommonMark).
	 */
	public String description;

	/**
	 * The version string of the Arazzo document.
	 */
	public String version;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(title, summary, description, version, extensions);
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
		if (!(o instanceof ArazzoInfo)) {
			return false;
		}
		return fieldValues().equals(((ArazzoInfo) o).fieldValues());
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
		return "ArazzoInfo" + fieldValues();
	}
}
