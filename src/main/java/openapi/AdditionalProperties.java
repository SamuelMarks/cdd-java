package openapi;

import java.util.Objects;

/**
 * Strongly typed union representing an OpenAPI / JSON Schema
 * {@code additionalProperties} or boolean-or-schema constraint (holding either
 * a {@link Boolean} or a {@link ReferenceOr}&lt;{@link Schema}&gt;).
 */
public final class AdditionalProperties {
	/**
	 * The boolean flag when {@code additionalProperties} is a boolean literal, or
	 * {@code null}.
	 */
	public final Boolean allowed;

	/**
	 * The schema or reference when {@code additionalProperties} is a schema object,
	 * or {@code null}.
	 */
	public final ReferenceOr<Schema> schema;

	/**
	 * Constructs an {@code AdditionalProperties} instance.
	 *
	 * @param allowed
	 *            The boolean value, or {@code null}.
	 * @param schema
	 *            The schema or reference value, or {@code null}.
	 */
	public AdditionalProperties(Boolean allowed, ReferenceOr<Schema> schema) {
		this.allowed = allowed;
		this.schema = schema;
	}

	/**
	 * Creates an {@code AdditionalProperties} from a boolean value.
	 *
	 * @param allowed
	 *            Whether additional properties are allowed.
	 * @return The created {@code AdditionalProperties}.
	 */
	public static AdditionalProperties ofBoolean(boolean allowed) {
		return new AdditionalProperties(allowed, null);
	}

	/**
	 * Creates an {@code AdditionalProperties} from a {@link Schema}.
	 *
	 * @param schema
	 *            The value {@link Schema}.
	 * @return The created {@code AdditionalProperties}.
	 */
	public static AdditionalProperties ofSchema(Schema schema) {
		return new AdditionalProperties(null, ReferenceOr.ofValue(schema));
	}

	/**
	 * Creates an {@code AdditionalProperties} from a {@link ReferenceOr} schema.
	 *
	 * @param schemaOrRef
	 *            The {@link ReferenceOr} wrapping a {@link Schema}.
	 * @return The created {@code AdditionalProperties}.
	 */
	public static AdditionalProperties ofRefOrSchema(ReferenceOr<Schema> schemaOrRef) {
		return new AdditionalProperties(null, schemaOrRef);
	}

	/**
	 * Returns {@code true} if this instance represents a boolean value.
	 *
	 * @return {@code true} if {@link #allowed} is non-null.
	 */
	public boolean isBoolean() {
		return allowed != null;
	}

	/**
	 * Returns {@code true} if this instance represents a schema or reference.
	 *
	 * @return {@code true} if {@link #schema} is non-null.
	 */
	public boolean isSchema() {
		return schema != null;
	}

	/**
	 * Compares this instance to another object for equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if both instances hold equal boolean or schema values.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AdditionalProperties)) {
			return false;
		}
		AdditionalProperties that = (AdditionalProperties) o;
		return Objects.equals(allowed, that.allowed) && Objects.equals(schema, that.schema);
	}

	/**
	 * Computes the hash code for this instance.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(allowed, schema);
	}

	/**
	 * Returns a string representation of this instance.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isBoolean() ? String.valueOf(allowed) : String.valueOf(schema);
	}
}
