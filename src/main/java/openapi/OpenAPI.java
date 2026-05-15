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

    /** The swagger property. */
    public String swagger;

    /** The self property. */
    public String $self;

    /** The info property. */
    public Info info;

    /** The host property. */
    public String host;

    /** The basePath property. */
    public String basePath;

    /** The schemes property. */
    public List<String> schemes;

    /** The consumes property. */
    public List<String> consumes;

    /** The produces property. */
    public List<String> produces;

    /** The jsonSchemaDialect property. */
    public String jsonSchemaDialect;

    /** The servers property. */
    public List<Server> servers;

    /** The paths property. */
    public Paths paths;

    /** The definitions property. */
    public Map<String, Schema> definitions;

    /** The parameters property. */
    public Map<String, Parameter> parameters;

    /** The responses property. */
    public Map<String, Response> responses;

    /** The securityDefinitions property. */
    public Map<String, SecurityScheme> securityDefinitions;

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
