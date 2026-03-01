package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter object compliant with OpenAPI 3.2.0.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter {
    /** Default constructor. */
    public Parameter() {}

    /** The name property. */
    @JsonProperty("name") public String name;
    /** The in property. */
    @JsonProperty("in") public String in;
    /** The description property. */
    @JsonProperty("description") public String description;
    /** The required property. */
    @JsonProperty("required") public Boolean required;
    /** The deprecated property. */
    @JsonProperty("deprecated") public Boolean deprecated;
    /** The allowEmptyValue property. */
    @JsonProperty("allowEmptyValue") public Boolean allowEmptyValue;

    /** The style property. */
    @JsonProperty("style") public String style;
    /** The explode property. */
    @JsonProperty("explode") public Boolean explode;
    /** The allowReserved property. */
    @JsonProperty("allowReserved") public Boolean allowReserved;
    /** The schema property. */
    @JsonProperty("schema") public Schema schema;
    /** The example property. */
    @JsonProperty("example") public Object example;
    /** The examples property. */
    @JsonProperty("examples") public Map<String, Example> examples;
    /** The content property. */
    @JsonProperty("content") public Map<String, MediaType> content;

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
