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
}
