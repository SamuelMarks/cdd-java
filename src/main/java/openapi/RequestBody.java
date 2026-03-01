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
 * RequestBody object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestBody {
    /**
     * Default constructor.
     */
    public RequestBody() {}

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The content property.
     */
    @JsonProperty("content")
    public Map<String, MediaType> content;

    /**
     * The required property.
     */
    @JsonProperty("required")
    public Boolean required;


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
