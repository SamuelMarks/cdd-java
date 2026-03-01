package openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * SecurityRequirement object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityRequirement {
    /**
     * Default constructor.
     */
    public SecurityRequirement() {}

    /**
     * Requirements.
     */
    @JsonIgnore public Map<String, List<String>> requirements = new HashMap<>();

    /**
     * Add requirement.
     * @param name Name
     * @param value Value
     */
    @JsonAnySetter
    public void addRequirement(String name, List<String> value) {
        requirements.put(name, value);
    }

    /**
     * Get requirements.
     * @return requirements
     */
    @JsonAnyGetter
    public Map<String, List<String>> getRequirements() {
        return requirements;
    }
}
