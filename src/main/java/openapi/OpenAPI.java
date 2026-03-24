package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI object.
 */
public class OpenAPI {
    /** Default constructor. */
    public OpenAPI() {}

    /** The openapi property. */
    public String openapi;

    /** The self property. */
    public String $self;

    /** The info property. */
    public Info info;

    /** The jsonSchemaDialect property. */
    public String jsonSchemaDialect;

    /** The servers property. */
    public List<Server> servers;

    /** The paths property. */
    public Paths paths;

    /** The webhooks property. */
    public Map<String, PathItem> webhooks;

    /** The components property. */
    public Components components;

    /** The security property. */
    public List<SecurityRequirement> security;

    /** The tags property. */
    public List<Tag> tags;

    /** The externalDocs property. */
    public ExternalDocumentation externalDocs;

    /** Extensions. */
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
