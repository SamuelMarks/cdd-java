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
 * Tag object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag {
    /**
     * Default constructor.
     */
    public Tag() {}

    /**
     * The name property.
     */
    @JsonProperty("name")
    public String name;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The externalDocs property.
     */
    @JsonProperty("externalDocs")
    public ExternalDocumentation externalDocs;



    /**
     * The summary property.
     */
    @JsonProperty("summary")
    public String summary;

    /**
     * The parent property.
     */
    @JsonProperty("parent")
    public String parent;

    /**
     * The kind property.
     */
    @JsonProperty("kind")
    public String kind;
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
