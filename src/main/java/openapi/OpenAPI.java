package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAPI {
    /** Default constructor. */
    public OpenAPI() {}

    /** The openapi property. */
    @JsonProperty("openapi")
    public String openapi;

    /** The self property. */
    @JsonProperty("$self")
    public String self;

    /** The info property. */
    @JsonProperty("info")
    public Info info;

    /** The jsonSchemaDialect property. */
    @JsonProperty("jsonSchemaDialect")
    public String jsonSchemaDialect;

    /** The servers property. */
    @JsonProperty("servers")
    public List<Server> servers;

    /** The paths property. */
    @JsonProperty("paths")
    public Paths paths;

    /** The webhooks property. */
    @JsonProperty("webhooks")
    public Map<String, PathItem> webhooks;

    /** The components property. */
    @JsonProperty("components")
    public Components components;

    /** The security property. */
    @JsonProperty("security")
    public List<SecurityRequirement> security;

    /** The tags property. */
    @JsonProperty("tags")
    public List<Tag> tags;

    /** The externalDocs property. */
    @JsonProperty("externalDocs")
    public ExternalDocumentation externalDocs;

    /** Extensions. */
    @JsonIgnore
    public Map<String, Object> extensions = new HashMap<>();

    /**
     * Get extensions.
     * @return extensions
     */
    @JsonAnyGetter
    public Map<String, Object> getExtensions() { return extensions; }

    /**
     * Add extension.
     * @param name extension name
     * @param value extension value
     */
    @JsonAnySetter
    public void addExtension(String name, Object value) {
        if (name.startsWith("x-")) extensions.put(name, value);
    }
}
