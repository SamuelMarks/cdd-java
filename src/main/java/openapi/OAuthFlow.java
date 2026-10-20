package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuthFlow object.
 */
public class OAuthFlow extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public OAuthFlow() {
	}

	/**
	 * The authorizationUrl property.
	 */
	public String authorizationUrl;

	/**
	 * The tokenUrl property.
	 */
	public String tokenUrl;

	/**
	 * The refreshUrl property.
	 */
	public String refreshUrl;

	/**
	 * The scopes property.
	 */
	public Map<String, String> scopes;

	/**
	 * The deviceAuthorizationUrl property.
	 */
	public String deviceAuthorizationUrl;
	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(authorizationUrl, tokenUrl, refreshUrl, scopes, deviceAuthorizationUrl,
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
		if (!(o instanceof OAuthFlow)) {
			return false;
		}
		return fieldValues().equals(((OAuthFlow) o).fieldValues());
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
		return "OAuthFlow" + fieldValues();
	}
}
