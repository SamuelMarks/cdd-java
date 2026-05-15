package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response object.
 */
public class Response {
    /**
     * Default constructor.
     */
    public Response() {}

    /**
     * The description property.
     */
    public String description;

    /**
     * The schema property.
     */
    public Schema schema;

    /**
     * The headers property.
     */
    public Map<String, Object> headers;

    /**
     * The examples property.
     */
    public Map<String, Object> examples;

    /**
     * The content property.
     */
    public Map<String, MediaType> content;

    /**
     * The links property.
     */
    public Map<String, Link> links;



    /**
     * The summary property.
     */
    public String summary;
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
