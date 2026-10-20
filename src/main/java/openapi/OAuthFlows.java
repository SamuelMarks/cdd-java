package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuthFlows object.
 */
public class OAuthFlows {
	/**
	 * Default constructor.
	 */
	public OAuthFlows() {
	}

	/**
	 * The implicit property.
	 */
	public OAuthFlow implicit;

	/**
	 * The password property.
	 */
	public OAuthFlow password;

	/**
	 * The clientCredentials property.
	 */
	public OAuthFlow clientCredentials;

	/**
	 * The authorizationCode property.
	 */
	public OAuthFlow authorizationCode;

	/**
	 * The deviceAuthorization property.
	 */
	public OAuthFlow deviceAuthorization;
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
		return java.util.Arrays.asList(implicit, password, clientCredentials, authorizationCode, deviceAuthorization,
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
		if (!(o instanceof OAuthFlows)) {
			return false;
		}
		return fieldValues().equals(((OAuthFlows) o).fieldValues());
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
		return "OAuthFlows" + fieldValues();
	}
}
