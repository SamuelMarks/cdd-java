package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML object.
 */
public class XML {
	/**
	 * Default constructor.
	 */
	public XML() {
	}

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The namespace property.
	 */
	public String namespace;

	/**
	 * The prefix property.
	 */
	public String prefix;

	/**
	 * The attribute property.
	 */
	public Boolean attribute;

	/**
	 * The wrapped property.
	 */
	public Boolean wrapped;

	/**
	 * The nodeType property.
	 */
	public String nodeType;
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
