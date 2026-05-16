package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SecurityScheme object.
 */
public class SecurityScheme {
	/**
	 * Default constructor.
	 */
	public SecurityScheme() {
	}

	/**
	 * The type property.
	 */
	public String type;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The in property.
	 */
	public String in;

	/**
	 * The scheme property.
	 */
	public String scheme;

	/**
	 * The bearerFormat property.
	 */
	public String bearerFormat;

	/**
	 * The flow property.
	 */
	public String flow;

	/**
	 * The authorizationUrl property.
	 */
	public String authorizationUrl;

	/**
	 * The tokenUrl property.
	 */
	public String tokenUrl;

	/**
	 * The scopes property.
	 */
	public Map<String, String> scopes;

	/**
	 * The flows property.
	 */
	public OAuthFlows flows;

	/**
	 * The openIdConnectUrl property.
	 */
	public String openIdConnectUrl;

	/**
	 * The oauth2MetadataUrl property.
	 */
	public String oauth2MetadataUrl;

	/**
	 * The deprecated property.
	 */
	public Boolean deprecated;
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
