package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Callback object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Callback {
    /**
     * Default constructor.
     */
    public Callback() {}

    /**
     * Path items.
     */
    @JsonIgnore public Map<String, PathItem> pathItems = new HashMap<>();
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
        if (name.startsWith("x-")) {
            extensions.put(name, value);
        } else if (value instanceof PathItem) {
            pathItems.put(name, (PathItem) value);
        }
    }

    /**
     * Get properties.
     * @return properties
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>(extensions);
        props.putAll(pathItems);
        return props;
    }
}
