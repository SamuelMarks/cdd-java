package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema object, fully compliant with OpenAPI 3.2.0 (JSON Schema Draft
 * 2020-12).
 */
public class Schema {
	/** Default constructor. */
	public Schema() {
	}

	/** The $ref property. */
	public String $ref;
	/** The type property. */
	public Object type;
	/** The title property. */
	public String title;
	/** The multipleOf property. */
	public Number multipleOf;
	/** The maximum property. */
	public Number maximum;
	/** The exclusiveMaximum property. */
	public Number exclusiveMaximum;
	/** The minimum property. */
	public Number minimum;
	/** The exclusiveMinimum property. */
	public Number exclusiveMinimum;
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
	/** The maxProperties property. */
	public Integer maxProperties;
	/** The minProperties property. */
	public Integer minProperties;
	/** The required property. */
	public List<String> required;
	/** The enumValues property. */
	public List<Object> enumValues;

	/** The allOf property. */
	public List<Object> allOf;
	/** The anyOf property. */
	public List<Object> anyOf;
	/** The oneOf property. */
	public List<Object> oneOf;
	/** The not property. */
	public Object not;

	/** The items property. */
	public Object items;
	/** The properties property. */
	public Map<String, Object> properties;
	/** The additionalProperties property. */
	public Object additionalProperties;
	/** The description property. */
	public String description;
	/** The format property. */
	public String format;
	/** The defaultValue property. */
	public Object defaultValue;
	/** The readOnly property. */
	public Boolean readOnly;
	/** The writeOnly property. */
	public Boolean writeOnly;
	/** The example property. */
	public Object example;
	/** The examples property. */
	public List<Object> examples;
	/** The externalDocs property. */
	public ExternalDocumentation externalDocs;
	/** The deprecated property. */
	public Boolean deprecated;
	/** The xml property. */
	public XML xml;
	/** The discriminator property. */
	public Discriminator discriminator;

	// JSON Schema Draft 2020-12 features
	/** The id property. */
	public String id;
	/** The schema property. */
	public String schema;
	/** The anchor property. */
	public String anchor;
	/** The dynamicAnchor property. */
	public String dynamicAnchor;
	/** The vocabulary property. */
	public Map<String, Boolean> vocabulary;
	/** The defs property. */
	public Map<String, Object> defs;

	/** The constValue property. */
	public Object constValue;
	/** The dependentRequired property. */
	public Map<String, List<String>> dependentRequired;
	/** The dependentSchemas property. */
	public Map<String, Object> dependentSchemas;
	/** The propertyNames property. */
	public Object propertyNames;
	/** The patternProperties property. */
	public Map<String, Object> patternProperties;
	/** The unevaluatedProperties property. */
	public Object unevaluatedProperties;
	/** The unevaluatedItems property. */
	public Object unevaluatedItems;
	/** The prefixItems property. */
	public List<Object> prefixItems;
	/** The contains property. */
	public Object contains;
	/** The minContains property. */
	public Integer minContains;
	/** The maxContains property. */
	public Integer maxContains;

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
