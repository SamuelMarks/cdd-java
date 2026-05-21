package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Callback object.
 */
public class Callback {
	/**
	 * Default constructor.
	 */
	public Callback() {
	}

	/**
	 * Path items.
	 */
	public Map<String, PathItem> pathItems = new HashMap<>();
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
		if (name.startsWith("x-")) {
			extensions.put(name, value);
		} else if (value instanceof PathItem) {
			pathItems.put(name, (PathItem) value);
		}
	}

	/**
	 * Get properties.
	 *
	 * @return properties
	 */
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>(extensions);
		props.putAll(pathItems);
		return props;
	}
}
