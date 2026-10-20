package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI object.
 */
public class OpenAPI extends ExtensibleElement {
	/** Default constructor. */
	public OpenAPI() {
	}

	/** The openapi property. */
	public String openapi;

	/** The swagger property. */
	public String swagger;

	/** The self property. */
	public String $self;

	/** The info property. */
	public Info info;

	/** The host property. */
	public String host;

	/** The basePath property. */
	public String basePath;

	/** The schemes property. */
	public List<String> schemes;

	/** The consumes property. */
	public List<String> consumes;

	/** The produces property. */
	public List<String> produces;

	/** The jsonSchemaDialect property. */
	public String jsonSchemaDialect;

	/** The servers property. */
	public List<Server> servers;

	/** The paths property. */
	public Paths paths;

	/** The definitions property. */
	public Map<String, Schema> definitions;

	/** The parameters property. */
	public Map<String, Parameter> parameters;

	/** The responses property. */
	public Map<String, Response> responses;

	/** The securityDefinitions property. */
	public Map<String, SecurityScheme> securityDefinitions;

	/** The webhooks property. */
	public Map<String, PathItem> webhooks;

	/** The components property. */
	public Components components;

	/** The security property. */
	public List<SecurityRequirement> security;

	/** The tags property. */
	public List<Tag> tags;

	/** The externalDocs property. */
	public ExternalDocumentation externalDocs;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(openapi, swagger, $self, info, host, basePath, schemes, consumes, produces,
				jsonSchemaDialect, servers, paths, definitions, parameters, responses, securityDefinitions, webhooks,
				components, security, tags, externalDocs, extensions);
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
		if (!(o instanceof OpenAPI)) {
			return false;
		}
		return fieldValues().equals(((OpenAPI) o).fieldValues());
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
		return "OpenAPI" + fieldValues();
	}
}
