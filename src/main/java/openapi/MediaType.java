package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaType object.
 */
public class MediaType {
	/**
	 * Default constructor.
	 */
	public MediaType() {
	}

	/**
	 * The schema property.
	 */
	public Object schema;

	/**
	 * The itemSchema property.
	 */
	public Object itemSchema;

	/**
	 * The example property.
	 */
	public Object example;

	/**
	 * The examples property.
	 */
	public Map<String, Object> examples;

	/**
	 * The encoding property.
	 */
	public Map<String, Encoding> encoding;

	/**
	 * The prefixEncoding property.
	 */
	public List<Encoding> prefixEncoding;

	/**
	 * The itemEncoding property.
	 */
	public Encoding itemEncoding;

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
