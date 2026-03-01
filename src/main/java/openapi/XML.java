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
 * XML object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XML {
    /**
     * Default constructor.
     */
    public XML() {}

    /**
     * The name property.
     */
    @JsonProperty("name")
    public String name;

    /**
     * The namespace property.
     */
    @JsonProperty("namespace")
    public String namespace;

    /**
     * The prefix property.
     */
    @JsonProperty("prefix")
    public String prefix;

    /**
     * The attribute property.
     */
    @JsonProperty("attribute")
    public Boolean attribute;

    /**
     * The wrapped property.
     */
    @JsonProperty("wrapped")
    public Boolean wrapped;



    /**
     * The nodeType property.
     */
    @JsonProperty("nodeType")
    public String nodeType;
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
