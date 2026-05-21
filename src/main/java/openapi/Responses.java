package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Responses object.
 */
public class Responses {
	/**
	 * Default constructor.
	 */
	public Responses() {
	}

	/**
	 * Default response.
	 */
	public Object defaultResponse;
	/**
	 * Status codes.
	 */
	public Map<String, Object> statusCodes = new HashMap<>();
	/**
	 * Extensions.
	 */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Add property.
	 *
	 * @param name
	 *            Name
	 * @param value
	 *            Value
	 */
	public void addProperty(String name, Object value) {
		if (name.equals("default")) {
			defaultResponse = value;
		} else if (name.startsWith("x-")) {
			extensions.put(name, value);
		} else {
			statusCodes.put(name, value);
		}
	}

	/**
	 * Get properties.
	 *
	 * @return properties
	 */
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>(statusCodes);
		props.putAll(extensions);
		return props;
	}
}
