package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server object.
 */
public class Server {
    /**
     * Default constructor.
     */
    public Server() {}

    /**
     * The url property.
     */
    public String url;

    /**
     * The description property.
     */
    public String description;

    /**
     * The name property.
     */
    public String name;

    /**
     * The variables property.
     */
    public Map<String, ServerVariable> variables;


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
