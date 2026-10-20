package openapi;

import java.util.Objects;

/**
 * Strongly typed generic union wrapper holding either an inline OpenAPI element
 * {@code T} or a {@link Reference} ({@code $ref}).
 *
 * @param <T>
 *            The concrete OpenAPI model type when not a reference.
 */
public final class ReferenceOr<T> {
	/**
	 * The inline value, or {@code null} if this instance represents a reference.
	 */
	public final T value;

	/**
	 * The reference object, or {@code null} if this instance holds an inline value.
	 */
	public final Reference ref;

	/**
	 * Constructs a {@code ReferenceOr} with the given value and reference.
	 *
	 * @param value
	 *            The inline value, or {@code null}.
	 * @param ref
	 *            The reference object, or {@code null}.
	 */
	public ReferenceOr(T value, Reference ref) {
		this.value = value;
		this.ref = ref;
	}

	/**
	 * Creates a {@code ReferenceOr} wrapping an inline value.
	 *
	 * @param <T>
	 *            The value type.
	 * @param value
	 *            The inline value.
	 * @return A new {@code ReferenceOr} holding {@code value}.
	 */
	public static <T> ReferenceOr<T> ofValue(T value) {
		return new ReferenceOr<>(value, null);
	}

	/**
	 * Creates a {@code ReferenceOr} wrapping a {@link Reference}.
	 *
	 * @param <T>
	 *            The target type.
	 * @param ref
	 *            The {@link Reference} instance.
	 * @return A new {@code ReferenceOr} holding {@code ref}.
	 */
	public static <T> ReferenceOr<T> ofRef(Reference ref) {
		return new ReferenceOr<>(null, ref);
	}

	/**
	 * Creates a {@code ReferenceOr} wrapping a {@code $ref} URI string.
	 *
	 * @param <T>
	 *            The target type.
	 * @param refUri
	 *            The {@code $ref} URI string.
	 * @return A new {@code ReferenceOr} holding a {@link Reference} with
	 *         {@code refUri}.
	 */
	public static <T> ReferenceOr<T> ofRef(String refUri) {
		Reference r = new Reference();
		r.ref = refUri;
		return new ReferenceOr<>(null, r);
	}

	/**
	 * Returns {@code true} if this wrapper holds a {@link Reference}.
	 *
	 * @return {@code true} if {@link #ref} is non-null; {@code false} otherwise.
	 */
	public boolean isRef() {
		return ref != null;
	}

	/**
	 * Returns {@code true} if this wrapper holds an inline value.
	 *
	 * @return {@code true} if {@link #value} is non-null; {@code false} otherwise.
	 */
	public boolean isValue() {
		return value != null;
	}

	/**
	 * Resolves the {@code $ref} URI string from either {@link #ref} or an inline
	 * {@link Schema#$ref}, or returns {@code null} if neither is present.
	 *
	 * @return The resolved {@code $ref} URI string, or {@code null}.
	 */
	public String resolveRefString() {
		if (ref != null && ref.ref != null) {
			return ref.ref;
		}
		if (value instanceof Schema) {
			return ((Schema) value).$ref;
		}
		return null;
	}

	/**
	 * Compares this wrapper to another object for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if both instances wrap equal values or references.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ReferenceOr)) {
			return false;
		}
		ReferenceOr<?> that = (ReferenceOr<?>) o;
		return Objects.equals(value, that.value) && Objects.equals(ref, that.ref);
	}

	/**
	 * Computes the hash code for this wrapper.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(value, ref);
	}

	/**
	 * Returns a human-readable string representation of this wrapper.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isRef() ? "Ref(" + ref.ref + ")" : "Value(" + value + ")";
	}
}
