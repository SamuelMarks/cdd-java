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
 * Operation object compliant with OpenAPI 3.2.0.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Operation {
    /** Default constructor. */
    public Operation() {}

    /** The tags property. */
    @JsonProperty("tags") public List<String> tags;
    /** The summary property. */
    @JsonProperty("summary") public String summary;
    /** The description property. */
    @JsonProperty("description") public String description;
    /** The externalDocs property. */
    @JsonProperty("externalDocs") public ExternalDocumentation externalDocs;
    /** The operationId property. */
    @JsonProperty("operationId") public String operationId;
    /** The parameters property. */
    @JsonProperty("parameters") public List<Object> parameters; // Parameter or Reference
    /** The requestBody property. */
    @JsonProperty("requestBody") public Object requestBody; // RequestBody or Reference
    /** The responses property. */
    @JsonProperty("responses") public Responses responses;
    /** The callbacks property. */
    @JsonProperty("callbacks") public Map<String, Object> callbacks; // Callback or Reference
    /** The deprecated property. */
    @JsonProperty("deprecated") public Boolean deprecated;
    /** The security property. */
    @JsonProperty("security") public List<SecurityRequirement> security;
    /** The servers property. */
    @JsonProperty("servers") public List<Server> servers;

        /** Extensions. */
    @JsonIgnore
    /** Extensions. */
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
