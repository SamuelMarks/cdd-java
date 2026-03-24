package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Header object.
 */
public class Header {
    /**
     * Default constructor.
     */
    public Header() {}

    /**
     * The description property.
     */
    public String description;

    /**
     * The required property.
     */
    public Boolean required;

    /**
     * The deprecated property.
     */
    public Boolean deprecated;

    /**
     * The allowEmptyValue property.
     */

    /**
     * The style property.
     */
    public String style;

    /**
     * The explode property.
     */
    public Boolean explode;

    /**
     * The allowReserved property.
     */

    /**
     * The schema property.
     */
    public Object schema;

    /**
     * The example property.
     */
    public Object example;

    /**
     * The examples property.
     */
    public Map<String, Object> examples;

    /**
     * The content property.
     */
    public Map<String, MediaType> content;


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
