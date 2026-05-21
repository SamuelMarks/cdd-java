package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RequestBody object.
 */
public class RequestBody {
	/**
	 * Default constructor.
	 */
	public RequestBody() {
	}

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The content property.
	 */
	public Map<String, MediaType> content;

	/**
	 * The required property.
	 */
	public Boolean required;

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
