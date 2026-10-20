package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Info object.
 */
public class Info extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Info() {
	}

	/**
	 * The title property.
	 */
	public String title;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The termsOfService property.
	 */
	public String termsOfService;

	/**
	 * The contact property.
	 */
	public Contact contact;

	/**
	 * The license property.
	 */
	public License license;

	/**
	 * The version property.
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
		return java.util.Arrays.asList(title, summary, description, termsOfService, contact, license, version,
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
		if (!(o instanceof Info)) {
			return false;
		}
		return fieldValues().equals(((Info) o).fieldValues());
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
		return "Info" + fieldValues();
	}
}
