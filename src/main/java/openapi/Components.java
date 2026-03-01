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
 * Components object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Components {
    /**
     * Default constructor.
     */
    public Components() {}

    /**
     * The schemas property.
     */
    @JsonProperty("schemas")
    public Map<String, Object> schemas;

    /**
     * The responses property.
     */
    @JsonProperty("responses")
    public Map<String, Object> responses;

    /**
     * The parameters property.
     */
    @JsonProperty("parameters")
    public Map<String, Object> parameters;

    /**
     * The examples property.
     */
    @JsonProperty("examples")
    public Map<String, Object> examples;

    /**
     * The requestBodies property.
     */
    @JsonProperty("requestBodies")
    public Map<String, Object> requestBodies;

    /**
     * The headers property.
     */
    @JsonProperty("headers")
    public Map<String, Object> headers;

    /**
     * The securitySchemes property.
     */
    @JsonProperty("securitySchemes")
    public Map<String, Object> securitySchemes;

    /**
     * The links property.
     */
    @JsonProperty("links")
    public Map<String, Object> links;

    /**
     * The callbacks property.
     */
    @JsonProperty("callbacks")
    public Map<String, Object> callbacks;

    /**
     * The pathItems property.
     */
    @JsonProperty("pathItems")
    public Map<String, PathItem> pathItems;

    /**
     * The mediaTypes property.
     */
    @JsonProperty("mediaTypes")
    public Map<String, Object> mediaTypes;


    /**
     * Extensions.
     */
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
