package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema object, fully compliant with OpenAPI 3.2.0 (JSON Schema Draft 2020-12).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schema {
    /** Default constructor. */
    public Schema() {}

    /** The type property. */
    @JsonProperty("type") public Object type;
    /** The title property. */
    @JsonProperty("title") public String title;
    /** The multipleOf property. */
    @JsonProperty("multipleOf") public Number multipleOf;
    /** The maximum property. */
    @JsonProperty("maximum") public Number maximum;
    /** The exclusiveMaximum property. */
    @JsonProperty("exclusiveMaximum") public Number exclusiveMaximum;
    /** The minimum property. */
    @JsonProperty("minimum") public Number minimum;
    /** The exclusiveMinimum property. */
    @JsonProperty("exclusiveMinimum") public Number exclusiveMinimum;
    /** The maxLength property. */
    @JsonProperty("maxLength") public Integer maxLength;
    /** The minLength property. */
    @JsonProperty("minLength") public Integer minLength;
    /** The pattern property. */
    @JsonProperty("pattern") public String pattern;
    /** The maxItems property. */
    @JsonProperty("maxItems") public Integer maxItems;
    /** The minItems property. */
    @JsonProperty("minItems") public Integer minItems;
    /** The uniqueItems property. */
    @JsonProperty("uniqueItems") public Boolean uniqueItems;
    /** The maxProperties property. */
    @JsonProperty("maxProperties") public Integer maxProperties;
    /** The minProperties property. */
    @JsonProperty("minProperties") public Integer minProperties;
    /** The required property. */
    @JsonProperty("required") public List<String> required;
    /** The enumValues property. */
    @JsonProperty("enum") public List<Object> enumValues;

    /** The allOf property. */
    @JsonProperty("allOf") public List<Object> allOf;
    /** The anyOf property. */
    @JsonProperty("anyOf") public List<Object> anyOf;
    /** The oneOf property. */
    @JsonProperty("oneOf") public List<Object> oneOf;
    /** The not property. */
    @JsonProperty("not") public Object not;

    /** The items property. */
    @JsonProperty("items") public Object items;
    /** The properties property. */
    @JsonProperty("properties") public Map<String, Object> properties;
    /** The additionalProperties property. */
    @JsonProperty("additionalProperties") public Object additionalProperties;
    /** The description property. */
    @JsonProperty("description") public String description;
    /** The format property. */
    @JsonProperty("format") public String format;
    /** The defaultValue property. */
    @JsonProperty("default") public Object defaultValue;
    /** The readOnly property. */
    @JsonProperty("readOnly") public Boolean readOnly;
    /** The writeOnly property. */
    @JsonProperty("writeOnly") public Boolean writeOnly;
    /** The example property. */
    @JsonProperty("example") public Object example;
    /** The examples property. */
    @JsonProperty("examples") public List<Object> examples;
    /** The externalDocs property. */
    @JsonProperty("externalDocs") public ExternalDocumentation externalDocs;
    /** The deprecated property. */
    @JsonProperty("deprecated") public Boolean deprecated;
    /** The xml property. */
    @JsonProperty("xml") public XML xml;
    /** The discriminator property. */
    @JsonProperty("discriminator") public Discriminator discriminator;

    // JSON Schema Draft 2020-12 features
    /** The id property. */
    @JsonProperty("$id") public String id;
    /** The schema property. */
    @JsonProperty("$schema") public String schema;
    /** The anchor property. */
    @JsonProperty("$anchor") public String anchor;
    /** The dynamicAnchor property. */
    @JsonProperty("$dynamicAnchor") public String dynamicAnchor;
    /** The vocabulary property. */
    @JsonProperty("$vocabulary") public Map<String, Boolean> vocabulary;
    /** The defs property. */
    @JsonProperty("$defs") public Map<String, Object> defs;
    
    /** The constValue property. */
    @JsonProperty("const") public Object constValue;
    /** The dependentRequired property. */
    @JsonProperty("dependentRequired") public Map<String, List<String>> dependentRequired;
    /** The dependentSchemas property. */
    @JsonProperty("dependentSchemas") public Map<String, Object> dependentSchemas;
    /** The propertyNames property. */
    @JsonProperty("propertyNames") public Object propertyNames;
    /** The patternProperties property. */
    @JsonProperty("patternProperties") public Map<String, Object> patternProperties;
    /** The unevaluatedProperties property. */
    @JsonProperty("unevaluatedProperties") public Object unevaluatedProperties;
    /** The unevaluatedItems property. */
    @JsonProperty("unevaluatedItems") public Object unevaluatedItems;
    /** The prefixItems property. */
    @JsonProperty("prefixItems") public List<Object> prefixItems;
    /** The contains property. */
    @JsonProperty("contains") public Object contains;
    /** The minContains property. */
    @JsonProperty("minContains") public Integer minContains;
    /** The maxContains property. */
    @JsonProperty("maxContains") public Integer maxContains;

        /** Extensions. */
    @JsonIgnore
    /** Extensions. */
    public Map<String, Object> extensions = new HashMap<>();

    /**
     * Get extensions.
     * @return extensions
     */
    @JsonAnyGetter
    public Map<String, Object> getExtensions() { return extensions; }

    /**
     * Add extension.
     * @param name extension name
     * @param value extension value
     */
    @JsonAnySetter
    public void addExtension(String name, Object value) {
        if (name.startsWith("x-")) extensions.put(name, value);
    }
}
