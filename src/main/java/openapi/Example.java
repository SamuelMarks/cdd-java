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
 * Example object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Example {
    /**
     * Default constructor.
     */
    public Example() {}

    /**
     * The summary property.
     */
    @JsonProperty("summary")
    public String summary;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The value property.
     */
    @JsonProperty("value")
    public Object value;

    /**
     * The externalValue property.
     */
    @JsonProperty("externalValue")
    public String externalValue;



    /**
     * The dataValue property.
     */
    @JsonProperty("dataValue")
    public Object dataValue;

    /**
     * The serializedValue property.
     */
    @JsonProperty("serializedValue")
    public String serializedValue;
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
