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
 * SecurityScheme object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityScheme {
    /**
     * Default constructor.
     */
    public SecurityScheme() {}

    /**
     * The type property.
     */
    @JsonProperty("type")
    public String type;

    /**
     * The description property.
     */
    @JsonProperty("description")
    public String description;

    /**
     * The name property.
     */
    @JsonProperty("name")
    public String name;

    /**
     * The in property.
     */
    @JsonProperty("in")
    public String in;

    /**
     * The scheme property.
     */
    @JsonProperty("scheme")
    public String scheme;

    /**
     * The bearerFormat property.
     */
    @JsonProperty("bearerFormat")
    public String bearerFormat;

    /**
     * The flows property.
     */
    @JsonProperty("flows")
    public OAuthFlows flows;

    /**
     * The openIdConnectUrl property.
     */
    @JsonProperty("openIdConnectUrl")
    public String openIdConnectUrl;



    /**
     * The oauth2MetadataUrl property.
     */
    @JsonProperty("oauth2MetadataUrl")
    public String oauth2MetadataUrl;

    /**
     * The deprecated property.
     */
    @JsonProperty("deprecated")
    public Boolean deprecated;
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
