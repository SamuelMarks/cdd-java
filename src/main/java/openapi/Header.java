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
 * Header object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Header {
    /**
     * Default constructor.
     */
    public Header() {}

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The required property.
     */
    @JsonProperty("required")
    public Boolean required;

    /**
     * The deprecated property.
     */
    @JsonProperty("deprecated")
    public Boolean deprecated;

    /**
     * The allowEmptyValue property.
     */

    /**
     * The style property.
     */
    @JsonProperty("style")
    public String style;

    /**
     * The explode property.
     */
    @JsonProperty("explode")
    public Boolean explode;

    /**
     * The allowReserved property.
     */

    /**
     * The schema property.
     */
    @JsonProperty("schema")
    public Object schema;

    /**
     * The example property.
     */
    @JsonProperty("example")
    public Object example;

    /**
     * The examples property.
     */
    @JsonProperty("examples")
    public Map<String, Object> examples;

    /**
     * The content property.
     */
    @JsonProperty("content")
    public Map<String, MediaType> content;


    /**
     * Extensions.
     */
    @JsonIgnore
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
