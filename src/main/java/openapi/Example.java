package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example object.
 */
public class Example {
    /**
     * Default constructor.
     */
    public Example() {}

    /**
     * The summary property.
     */
    public String summary;

    /**
     * The description property.
     */
    public String description;

    /**
     * The value property.
     */
    public Object value;

    /**
     * The externalValue property.
     */
    public String externalValue;



    /**
     * The dataValue property.
     */
    public Object dataValue;

    /**
     * The serializedValue property.
     */
    public String serializedValue;
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
