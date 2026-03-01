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
 * ServerVariable object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerVariable {
    /**
     * Default constructor.
     */
    public ServerVariable() {}

    /**
     * The enumValues property.
     */
    @JsonProperty("enum")
    public List<String> enumValues;

    /**
     * The defaultValue property.
     */
    @JsonProperty("default")
    public String defaultValue;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;


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
