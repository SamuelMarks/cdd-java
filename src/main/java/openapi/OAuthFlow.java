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
 * OAuthFlow object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthFlow {
    /**
     * Default constructor.
     */
    public OAuthFlow() {}

    /**
     * The authorizationUrl property.
     */
    @JsonProperty("authorizationUrl")
    public String authorizationUrl;

    /**
     * The tokenUrl property.
     */
    @JsonProperty("tokenUrl")
    public String tokenUrl;

    /**
     * The refreshUrl property.
     */
    @JsonProperty("refreshUrl")
    public String refreshUrl;

    /**
     * The scopes property.
     */
    @JsonProperty("scopes")
    public Map<String, String> scopes;



    /**
     * The deviceAuthorizationUrl property.
     */
    @JsonProperty("deviceAuthorizationUrl")
    public String deviceAuthorizationUrl;
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
