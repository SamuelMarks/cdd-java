package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encoding object.
 */
public class Encoding {
	/**
	 * Default constructor.
	 */
	public Encoding() {
	}

	/**
	 * The contentType property.
	 */
	public String contentType;

	/**
	 * The headers property.
	 */
	public Map<String, Object> headers;

	/**
	 * The style property.
	 */
	public String style;

	/**
	 * The explode property.
	 */
	public Boolean explode;

	/**
	 * The allowReserved property.
	 */
	public Boolean allowReserved;

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
