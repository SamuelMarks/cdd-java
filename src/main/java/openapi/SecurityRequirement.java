package openapi;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * SecurityRequirement object.
 */
public class SecurityRequirement {
	/**
	 * Default constructor.
	 */
	public SecurityRequirement() {
	}

	/**
	 * Requirements.
	 */
	public Map<String, List<String>> requirements = new HashMap<>();

	/**
	 * Add requirement.
	 * 
	 * @param name
	 *            Name
	 * @param value
	 *            Value
	 */
	public void addRequirement(String name, List<String> value) {
		requirements.put(name, value);
	}

	/**
	 * Get requirements.
	 * 
	 * @return requirements
	 */
	public Map<String, List<String>> getRequirements() {
		return requirements;
	}
}
