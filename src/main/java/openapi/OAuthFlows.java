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
 * OAuthFlows object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthFlows {
    /**
     * Default constructor.
     */
    public OAuthFlows() {}

    /**
     * The implicit property.
     */
    @JsonProperty("implicit")
    public OAuthFlow implicit;

    /**
     * The password property.
     */
    @JsonProperty("password")
    public OAuthFlow password;

    /**
     * The clientCredentials property.
     */
    @JsonProperty("clientCredentials")
    public OAuthFlow clientCredentials;

    /**
     * The authorizationCode property.
     */
    @JsonProperty("authorizationCode")
    public OAuthFlow authorizationCode;



    /**
     * The deviceAuthorization property.
     */
    @JsonProperty("deviceAuthorization")
    public OAuthFlow deviceAuthorization;
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
