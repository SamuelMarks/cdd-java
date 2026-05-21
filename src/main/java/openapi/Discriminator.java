package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discriminator object.
 */
public class Discriminator {
	/**
	 * Default constructor.
	 */
	public Discriminator() {
	}

	/**
	 * The propertyName property.
	 */
	public String propertyName;

	/**
	 * The mapping property.
	 */
	public Map<String, String> mapping;

	/**
	 * The defaultMapping property.
	 */
	public String defaultMapping;
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
