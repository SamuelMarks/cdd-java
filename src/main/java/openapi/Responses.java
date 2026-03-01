package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Responses object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Responses {
    /**
     * Default constructor.
     */
    public Responses() {}

    /**
     * Default response.
     */
    @JsonProperty("default") public Object defaultResponse;
    /**
     * Status codes.
     */
    @JsonIgnore public Map<String, Object> statusCodes = new HashMap<>();
    /**
     * Extensions.
     */
    @JsonIgnore public Map<String, Object> extensions = new HashMap<>();

    /**
     * Add property.
     * @param name Name
     * @param value Value
     */
    @JsonAnySetter
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
     * @return properties
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>(statusCodes);
        props.putAll(extensions);
        return props;
    }
}
