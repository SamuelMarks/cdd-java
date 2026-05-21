package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PathItem object.
 */
public class PathItem {
	/**
	 * Default constructor.
	 */
	public PathItem() {
	}

	/**
	 * The ref property.
	 */
	public String ref;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The get property.
	 */
	public Operation get;

	/**
	 * The put property.
	 */
	public Operation put;

	/**
	 * The post property.
	 */
	public Operation post;

	/**
	 * The delete property.
	 */
	public Operation delete;

	/**
	 * The options property.
	 */
	public Operation options;

	/**
	 * The head property.
	 */
	public Operation head;

	/**
	 * The patch property.
	 */
	public Operation patch;

	/**
	 * The trace property.
	 */
	public Operation trace;

	/**
	 * The query property.
	 */
	public Operation query;

	/**
	 * The additionalOperations property.
	 */
	public Map<String, Operation> additionalOperations;

	/**
	 * The servers property.
	 */
	public List<Server> servers;

	/**
	 * The parameters property.
	 */
	public List<Object> parameters;

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
