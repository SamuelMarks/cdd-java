package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameter object compliant with OpenAPI 3.2.0.
 */
public class Parameter {
	/** Default constructor. */
	public Parameter() {
	}

	/** The name property. */
	public String name;
	/** The in property. */
	public String in;
	/** The description property. */
	public String description;
	/** The required property. */
	public Boolean required;
	/** The deprecated property. */
	public Boolean deprecated;
	/** The allowEmptyValue property. */
	public Boolean allowEmptyValue;

	/** The type property. */
	public String type;
	/** The format property. */
	public String format;
	/** The items property. */
	public Items items;
	/** The collectionFormat property. */
	public String collectionFormat;
	/** The defaultValue property. */
	public Object defaultValue;
	/** The maximum property. */
	public Number maximum;
	/** The exclusiveMaximum property. */
	public Boolean exclusiveMaximum;
	/** The minimum property. */
	public Number minimum;
	/** The exclusiveMinimum property. */
	public Boolean exclusiveMinimum;
	/** The maxLength property. */
	public Integer maxLength;
	/** The minLength property. */
	public Integer minLength;
	/** The pattern property. */
	public String pattern;
	/** The maxItems property. */
	public Integer maxItems;
	/** The minItems property. */
	public Integer minItems;
	/** The uniqueItems property. */
	public Boolean uniqueItems;
	/** The enumValues property. */
	public List<Object> enumValues;
	/** The multipleOf property. */
	public Number multipleOf;

	/** The style property. */
	public String style;
	/** The explode property. */
	public Boolean explode;
	/** The allowReserved property. */
	public Boolean allowReserved;
	/** The schema property. */
	public Schema schema;
	/** The example property. */
	public Object example;
	/** The examples property. */
	public Map<String, Example> examples;
	/** The content property. */
	public Map<String, MediaType> content;

	/** Extensions. */
	/** Extensions. */
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
