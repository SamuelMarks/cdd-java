package openapi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Strongly typed representation of a JSON Schema {@code type} keyword in
 * OpenAPI 3.1/3.2 (either a single type string or a multi-type list of
 * strings).
 */
public final class SchemaType {
	/**
	 * Single type name (e.g. {@code "string"}), or the primary type when multiple
	 * types are specified.
	 */
	public final String single;

	/**
	 * Immutable list of type names when a union type array is specified (e.g.
	 * {@code ["string", "null"]}).
	 */
	public final List<String> types;

	/**
	 * Constructs a {@code SchemaType} from a single type name.
	 *
	 * @param single
	 *            The single type name.
	 */
	public SchemaType(String single) {
		this.single = single;
		this.types = single != null ? Collections.singletonList(single) : Collections.emptyList();
	}

	/**
	 * Constructs a {@code SchemaType} from a list of type names.
	 *
	 * @param types
	 *            The list of type names.
	 */
	public SchemaType(List<String> types) {
		this.types = types != null ? Collections.unmodifiableList(types) : Collections.emptyList();
		this.single = !this.types.isEmpty() ? this.types.get(0) : null;
	}

	/**
	 * Returns {@code true} if this schema type represents a multi-type array.
	 *
	 * @return {@code true} if more than one type is present.
	 */
	public boolean isMultiType() {
		return types.size() > 1;
	}

	/**
	 * Compares this schema type to another object for equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if both instances represent the same schema type(s).
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SchemaType)) {
			return false;
		}
		SchemaType that = (SchemaType) o;
		return Objects.equals(single, that.single) && Objects.equals(types, that.types);
	}

	/**
	 * Computes the hash code for this schema type.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(single, types);
	}

	/**
	 * Returns the string representation of this schema type.
	 *
	 * @return The type string or list representation.
	 */
	@Override
	public String toString() {
		return isMultiType() ? types.toString() : String.valueOf(single);
	}
}
