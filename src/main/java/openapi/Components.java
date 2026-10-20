package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Components object.
 */
public class Components extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Components() {
	}

	/**
	 * The schemas property.
	 */
	public Map<String, ReferenceOr<Schema>> schemas;

	/**
	 * The responses property.
	 */
	public Map<String, ReferenceOr<Response>> responses;

	/**
	 * The parameters property.
	 */
	public Map<String, ReferenceOr<Parameter>> parameters;

	/**
	 * The examples property.
	 */
	public Map<String, ReferenceOr<Example>> examples;

	/**
	 * The requestBodies property.
	 */
	public Map<String, ReferenceOr<RequestBody>> requestBodies;

	/**
	 * The headers property.
	 */
	public Map<String, ReferenceOr<Header>> headers;

	/**
	 * The securitySchemes property.
	 */
	public Map<String, ReferenceOr<SecurityScheme>> securitySchemes;

	/**
	 * The links property.
	 */
	public Map<String, ReferenceOr<Link>> links;

	/**
	 * The callbacks property.
	 */
	public Map<String, ReferenceOr<Callback>> callbacks;

	/**
	 * The pathItems property.
	 */
	public Map<String, ReferenceOr<PathItem>> pathItems;

	/**
	 * The mediaTypes property.
	 */
	public Map<String, ReferenceOr<MediaType>> mediaTypes;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(schemas, responses, parameters, examples, requestBodies, headers,
				securitySchemes, links, callbacks, pathItems, mediaTypes, extensions);
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
		if (!(o instanceof Components)) {
			return false;
		}
		return fieldValues().equals(((Components) o).fieldValues());
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
		return "Components" + fieldValues();
	}
}
