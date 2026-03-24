package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Components object.
 */
public class Components {
    /**
     * Default constructor.
     */
    public Components() {}

    /**
     * The schemas property.
     */
    public Map<String, Schema> schemas;

    /**
     * The responses property.
     */
    public Map<String, Object> responses;

    /**
     * The parameters property.
     */
    public Map<String, Object> parameters;

    /**
     * The examples property.
     */
    public Map<String, Object> examples;

    /**
     * The requestBodies property.
     */
    public Map<String, Object> requestBodies;

    /**
     * The headers property.
     */
    public Map<String, Object> headers;

    /**
     * The securitySchemes property.
     */
    public Map<String, Object> securitySchemes;

    /**
     * The links property.
     */
    public Map<String, Object> links;

    /**
     * The callbacks property.
     */
    public Map<String, Object> callbacks;

    /**
     * The pathItems property.
     */
    public Map<String, PathItem> pathItems;

    /**
     * The mediaTypes property.
     */
    public Map<String, Object> mediaTypes;


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
