package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation object compliant with OpenAPI 3.2.0.
 */
public class Operation {
	/** Default constructor. */
	public Operation() {
	}

	/** The tags property. */
	public List<String> tags;
	/** The summary property. */
	public String summary;
	/** The description property. */
	public String description;
	/** The consumes property. */
	public List<String> consumes;
	/** The produces property. */
	public List<String> produces;
	/** The schemes property. */
	public List<String> schemes;
	/** The externalDocs property. */
	public ExternalDocumentation externalDocs;
	/** The operationId property. */
	public String operationId;
	/** The parameters property. */
	public List<Object> parameters; // Parameter or Reference
	/** The requestBody property. */
	public Object requestBody; // RequestBody or Reference
	/** The responses property. */
	public Responses responses;
	/** The callbacks property. */
	public Map<String, Object> callbacks; // Callback or Reference
	/** The deprecated property. */
	public Boolean deprecated;
	/** The security property. */
	public List<SecurityRequirement> security;
	/** The servers property. */
	public List<Server> servers;

	/** Extensions. */
	/** Extensions. */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Get extensions.
	 * 
	 * @return extensions
	 */
	public Map<String, Object> getExtensions() {
		return extensions;
	}

	/**
	 * Add extension.
	 * 
	 * @param name
	 *            extension name
	 * @param value
	 *            extension value
	 */
	public void addExtension(String name, Object value) {
		if (name.startsWith("x-"))
			extensions.put(name, value);
	}
}
