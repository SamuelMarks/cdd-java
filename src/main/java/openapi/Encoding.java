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
 * Encoding object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Encoding {
    /**
     * Default constructor.
     */
    public Encoding() {}

    /**
     * The contentType property.
     */
    @JsonProperty("contentType")
    public String contentType;

    /**
     * The headers property.
     */
    @JsonProperty("headers")
    public Map<String, Object> headers;

    /**
     * The style property.
     */
    @JsonProperty("style")
    public String style;

    /**
     * The explode property.
     */
    @JsonProperty("explode")
    public Boolean explode;

    /**
     * The allowReserved property.
     */
    @JsonProperty("allowReserved")
    public Boolean allowReserved;

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
