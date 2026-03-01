package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Paths object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Paths {
    /**
     * Default constructor.
     */
    public Paths() {}

    /**
     * Path items.
     */
    @JsonIgnore public Map<String, PathItem> pathItems = new HashMap<>();
    /**
     * Extensions.
     */
    @JsonIgnore public Map<String, Object> extensions = new HashMap<>();

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Add property.
     * @param name Name
     * @param value Value
     */
    @JsonAnySetter
    public void addProperty(String name, Object value) {
        if (name.startsWith("x-")) {
            extensions.put(name, value);
        } else if (name.startsWith("/")) {
            try {
                PathItem pi = mapper.convertValue(value, PathItem.class);
                pathItems.put(name, pi);
            } catch (Exception e) {
                // ignore
            }
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
