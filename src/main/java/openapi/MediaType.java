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
 * MediaType object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaType {
    /**
     * Default constructor.
     */
    public MediaType() {}

    /**
     * The schema property.
     */
    @JsonProperty("schema")
    public Object schema;

    /**
     * The itemSchema property.
     */
    @JsonProperty("itemSchema")
    public Object itemSchema;

    /**
     * The example property.
     */
    @JsonProperty("example")
    public Object example;

    /**
     * The examples property.
     */
    @JsonProperty("examples")
    public Map<String, Object> examples;

    /**
     * The encoding property.
     */
    @JsonProperty("encoding")
    public Map<String, Encoding> encoding;

    /**
     * The prefixEncoding property.
     */
    @JsonProperty("prefixEncoding")
    public List<Encoding> prefixEncoding;

    /**
     * The itemEncoding property.
     */
    @JsonProperty("itemEncoding")
    public Encoding itemEncoding;


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
