package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag object.
 */
public class Tag {
    /**
     * Default constructor.
     */
    public Tag() {}

    /**
     * The name property.
     */
    public String name;

    /**
     * The description property.
     */
    public String description;

    /**
     * The externalDocs property.
     */
    public ExternalDocumentation externalDocs;



    /**
     * The summary property.
     */
    public String summary;

    /**
     * The parent property.
     */
    public String parent;

    /**
     * The kind property.
     */
    public String kind;
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
