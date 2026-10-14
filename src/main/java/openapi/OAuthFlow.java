package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuthFlow object.
 */
public class OAuthFlow {
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
	 * Compares this object with another for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal.
	 */
	@Override
	public boolean equals(Object o) {
		return TypeMapper.structuralEquals(this, o);
	}

	/**
	 * Returns a structural hash code for this object.
	 *
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		return TypeMapper.structuralHashCode(this);
	}

	/**
	 * Returns a structural string representation of this object.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return TypeMapper.structuralToString(this);
	}
}
