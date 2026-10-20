package openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation object compliant with OpenAPI 3.2.0.
 */
public class Operation {

	/**
	 * Default constructor.
	 */
	public Operation() {
	}

	/**
	 * The tags property.
	 */
	public List<String> tags;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The consumes property.
	 */
	public List<String> consumes;

	/**
	 * The produces property.
	 */
	public List<String> produces;

	/**
	 * The schemes property.
	 */
	public List<String> schemes;

	/**
	 * The externalDocs property.
	 */
	public ExternalDocumentation externalDocs;

	/**
	 * The operationId property.
	 */
	public String operationId;

	/**
	 * The parameters property.
	 */
	public List<ReferenceOr<Parameter>> parameters;

	/**
	 * The requestBody property.
	 */
	public ReferenceOr<RequestBody> requestBody;

	/**
	 * The responses property.
	 */
	public Responses responses;

	/**
	 * The callbacks property.
	 */
	public Map<String, ReferenceOr<Callback>> callbacks;

	/**
	 * The deprecated property.
	 */
	public Boolean deprecated;

	/**
	 * The security property.
	 */
	public List<SecurityRequirement> security;

	/**
	 * The servers property.
	 */
	public List<Server> servers;

	/**
	 * Extensions.
	 */
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

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(tags, summary, description, consumes, produces, schemes, externalDocs,
				operationId, parameters, requestBody, responses, callbacks, deprecated, security, servers, extensions);
	}

	/**
	 * Compares this object with another for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Operation)) {
			return false;
		}
		return fieldValues().equals(((Operation) o).fieldValues());
	}

	/**
	 * Returns a structural hash code for this object.
	 *
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		return fieldValues().hashCode();
	}

	/**
	 * Returns a structural string representation of this object.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return "Operation" + fieldValues();
	}
}
