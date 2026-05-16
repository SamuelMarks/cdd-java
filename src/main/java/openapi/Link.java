package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Link object.
 */
public class Link {
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
	public Map<String, Object> parameters;

	/**
	 * The requestBody property.
	 */
	public Object requestBody;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The server property.
	 */
	public Server server;

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
