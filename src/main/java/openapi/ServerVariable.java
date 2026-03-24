package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServerVariable object.
 */
public class ServerVariable {
    /**
     * Default constructor.
     */
    public ServerVariable() {}

    /**
     * The enumValues property.
     */
    public List<String> enumValues;

    /**
     * The defaultValue property.
     */
    public String defaultValue;

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
