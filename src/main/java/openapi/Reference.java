package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference object.
 */
public class Reference {
    /**
     * Default constructor.
     */
    public Reference() {}

    /**
     * The ref property.
     */
    public String ref;

    /**
     * The summary property.
     */
    public String summary;

    /**
     * The description property.
     */
    public String description;


    /**
     * Extensions.
     */
    public Map<String, Object> extensions = new HashMap<>();

    /**
     * Get extensions.
     * @return extensions
     */
    public Map<String, Object> getExtensions() { return extensions; }

    /**
     * Add extension.
     * @param name extension name
     * @param value extension value
     */
    public void addExtension(String name, Object value) {
        if (name.startsWith("x-")) extensions.put(name, value);
    }
}
