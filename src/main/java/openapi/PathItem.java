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
 * PathItem object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathItem {
    /**
     * Default constructor.
     */
    public PathItem() {}

    /**
     * The ref property.
     */
    @JsonProperty("$ref")
    public String ref;

    /**
     * The summary property.
     */
    @JsonProperty("summary")
    public String summary;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The get property.
     */
    @JsonProperty("get")
    public Operation get;

    /**
     * The put property.
     */
    @JsonProperty("put")
    public Operation put;

    /**
     * The post property.
     */
    @JsonProperty("post")
    public Operation post;

    /**
     * The delete property.
     */
    @JsonProperty("delete")
    public Operation delete;

    /**
     * The options property.
     */
    @JsonProperty("options")
    public Operation options;

    /**
     * The head property.
     */
    @JsonProperty("head")
    public Operation head;

    /**
     * The patch property.
     */
    @JsonProperty("patch")
    public Operation patch;

    /**
     * The trace property.
     */
    @JsonProperty("trace")
    public Operation trace;

    /**
     * The query property.
     */
    @JsonProperty("query")
    public Operation query;

    /**
     * The additionalOperations property.
     */
    @JsonProperty("additionalOperations")
    public Map<String, Operation> additionalOperations;

    /**
     * The servers property.
     */
    @JsonProperty("servers")
    public List<Server> servers;

    /**
     * The parameters property.
     */
    @JsonProperty("parameters")
    public List<Object> parameters;


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
