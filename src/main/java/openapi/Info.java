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
 * Info object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Info {
    /**
     * Default constructor.
     */
    public Info() {}

    /**
     * The title property.
     */
    @JsonProperty("title")
    public String title;

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
     * The termsOfService property.
     */
    @JsonProperty("termsOfService")
    public String termsOfService;

    /**
     * The contact property.
     */
    @JsonProperty("contact")
    public Contact contact;

    /**
     * The license property.
     */
    @JsonProperty("license")
    public License license;

    /**
     * The version property.
     */
    @JsonProperty("version")
    public String version;


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
