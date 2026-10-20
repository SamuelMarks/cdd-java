package openapi;

import arazzo.PayloadValue;
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
	public SchemaType type;
	/** The title property. */
	public String title;
	/** The multipleOf property. */
	public Number multipleOf;
	/** The maximum property. */
	public Number maximum;
	/** The exclusiveMaximum property. */
	public ExclusiveBound exclusiveMaximum;
	/** The minimum property. */
	public Number minimum;
	/** The exclusiveMinimum property. */
	public ExclusiveBound exclusiveMinimum;
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
	public List<PayloadValue> enumValues;

	/** The allOf property. */
	public List<ReferenceOr<Schema>> allOf;
	/** The anyOf property. */
	public List<ReferenceOr<Schema>> anyOf;
	/** The oneOf property. */
	public List<ReferenceOr<Schema>> oneOf;
	/** The not property. */
	public ReferenceOr<Schema> not;

	/** The items property. */
	public ReferenceOr<Schema> items;
	/** The properties property. */
	public Map<String, ReferenceOr<Schema>> properties;
	/** The additionalProperties property. */
	public AdditionalProperties additionalProperties;
	/** The description property. */
	public String description;
	/** The format property. */
	public String format;
	/** The defaultValue property. */
	public PayloadValue defaultValue;
	/** The readOnly property. */
	public Boolean readOnly;
	/** The writeOnly property. */
	public Boolean writeOnly;
	/** The example property. */
	public PayloadValue example;
	/** The examples property. */
	public List<PayloadValue> examples;
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
	public Map<String, ReferenceOr<Schema>> defs;

	/** The constValue property. */
	public PayloadValue constValue;
	/** The dependentRequired property. */
	public Map<String, List<String>> dependentRequired;
	/** The dependentSchemas property. */
	public Map<String, ReferenceOr<Schema>> dependentSchemas;
	/** The propertyNames property. */
	public ReferenceOr<Schema> propertyNames;
	/** The patternProperties property. */
	public Map<String, ReferenceOr<Schema>> patternProperties;
	/** The unevaluatedProperties property. */
	public AdditionalProperties unevaluatedProperties;
	/** The unevaluatedItems property. */
	public AdditionalProperties unevaluatedItems;
	/** The prefixItems property. */
	public List<ReferenceOr<Schema>> prefixItems;
	/** The contains property. */
	public ReferenceOr<Schema> contains;
	/** The minContains property. */
	public Integer minContains;
	/** The maxContains property. */
	public Integer maxContains;

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

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList($ref, type, title, multipleOf, maximum, exclusiveMaximum, minimum,
				exclusiveMinimum, maxLength, minLength, pattern, maxItems, minItems, uniqueItems, maxProperties,
				minProperties, required, enumValues, allOf, anyOf, oneOf, not, items, properties, additionalProperties,
				description, format, defaultValue, readOnly, writeOnly, example, examples, externalDocs, deprecated,
				xml, discriminator, id, schema, anchor, dynamicAnchor, vocabulary, defs, constValue, dependentRequired,
				dependentSchemas, propertyNames, patternProperties, unevaluatedProperties, unevaluatedItems,
				prefixItems, contains, minContains, maxContains, extensions);
	}

	/**
	 * Compares this object with another for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Schema)) {
			return false;
		}
		return fieldValues().equals(((Schema) o).fieldValues());
	}

	/**
	 * Returns a structural hash code for this object.
	 *
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		return fieldValues().hashCode();
	}

	/**
	 * Returns a structural string representation of this object.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return "Schema" + fieldValues();
	}
}
