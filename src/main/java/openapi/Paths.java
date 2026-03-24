package openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Paths object.
 */
public class Paths {
    /**
     * Default constructor.
     */
    public Paths() {}

    /**
     * Path items.
     */
    public Map<String, PathItem> pathItems = new HashMap<>();
    /**
     * Extensions.
     */
    public Map<String, Object> extensions = new HashMap<>();

    /**
     * Add property.
     * @param name Name
     * @param value Value
     */
    public void addProperty(String name, Object value) {
        if (name.startsWith("x-")) {
            extensions.put(name, value);
        } else if (name.startsWith("/")) {
            if (value instanceof PathItem) {
                pathItems.put(name, (PathItem) value);
            }
        }
    }

    /**
     * Get properties.
     * @return properties
     */
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>(extensions);
        props.putAll(pathItems);
        return props;
    }
}
