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
 * Link object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {
    /**
     * Default constructor.
     */
    public Link() {}

    /**
     * The operationRef property.
     */
    @JsonProperty("operationRef")
    public String operationRef;

    /**
     * The operationId property.
     */
    @JsonProperty("operationId")
    public String operationId;

    /**
     * The parameters property.
     */
    @JsonProperty("parameters")
    public Map<String, Object> parameters;

    /**
     * The requestBody property.
     */
    @JsonProperty("requestBody")
    public Object requestBody;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The server property.
     */
    @JsonProperty("server")
    public Server server;


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
