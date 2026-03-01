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
 * Response object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    /**
     * Default constructor.
     */
    public Response() {}

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The headers property.
     */
    @JsonProperty("headers")
    public Map<String, Object> headers;

    /**
     * The content property.
     */
    @JsonProperty("content")
    public Map<String, MediaType> content;

    /**
     * The links property.
     */
    @JsonProperty("links")
    public Map<String, Link> links;



    /**
     * The summary property.
     */
    @JsonProperty("summary")
    public String summary;
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
