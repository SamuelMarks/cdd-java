package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Info object.
 */
public class Info {
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
