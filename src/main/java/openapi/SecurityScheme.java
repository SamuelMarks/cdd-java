package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SecurityScheme object.
 */
public class SecurityScheme extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public SecurityScheme() {
	}

	/**
	 * The type property.
	 */
	public String type;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The name property.
	 */
	public String name;

	/**
	 * The in property.
	 */
	public String in;

	/**
	 * The scheme property.
	 */
	public String scheme;

	/**
	 * The bearerFormat property.
	 */
	public String bearerFormat;

	/**
	 * The flow property.
	 */
	public String flow;

	/**
	 * The authorizationUrl property.
	 */
	public String authorizationUrl;

	/**
	 * The tokenUrl property.
	 */
	public String tokenUrl;

	/**
	 * The scopes property.
	 */
	public Map<String, String> scopes;

	/**
	 * The flows property.
	 */
	public OAuthFlows flows;

	/**
	 * The openIdConnectUrl property.
	 */
	public String openIdConnectUrl;

	/**
	 * The oauth2MetadataUrl property.
	 */
	public String oauth2MetadataUrl;

	/**
	 * The deprecated property.
	 */
	public Boolean deprecated;

	/**
	 * Returns the strongly typed {@link SecuritySchemeType} for {@link #type}.
	 *
	 * @return The resolved {@link SecuritySchemeType}, or {@code null}.
	 */
	public SecuritySchemeType getSchemeType() {
		return SecuritySchemeType.fromString(type);
	}

	/**
	 * Sets {@link #type} from a strongly typed {@link SecuritySchemeType}.
	 *
	 * @param schemeType
	 *            The {@link SecuritySchemeType}.
	 */
	public void setSchemeType(SecuritySchemeType schemeType) {
		this.type = schemeType != null ? schemeType.value : null;
	}

	/**
	 * Returns the strongly typed {@link ParameterLocation} for {@link #in}.
	 *
	 * @return The resolved {@link ParameterLocation}, or {@code null}.
	 */
	public ParameterLocation getLocation() {
		return ParameterLocation.fromString(in);
	}

	/**
	 * Sets {@link #in} from a strongly typed {@link ParameterLocation}.
	 *
	 * @param location
	 *            The {@link ParameterLocation}.
	 */
	public void setLocation(ParameterLocation location) {
		this.in = location != null ? location.value : null;
	}
	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(type, description, name, in, scheme, bearerFormat, flow, authorizationUrl,
				tokenUrl, scopes, flows, openIdConnectUrl, oauth2MetadataUrl, deprecated, extensions);
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
		if (!(o instanceof SecurityScheme)) {
			return false;
		}
		return fieldValues().equals(((SecurityScheme) o).fieldValues());
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
		return "SecurityScheme" + fieldValues();
	}
}
