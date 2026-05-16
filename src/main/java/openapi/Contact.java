package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contact object.
 */
public class Contact {
	/**
	 * Default constructor.
	 */
	public Contact() {
	}

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The url property.
	 */
	public String url;

	/**
	 * The email property.
	 */
	public String email;

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
