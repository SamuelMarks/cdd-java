package openapi;

import java.util.Objects;

/**
 * Strongly typed union representing an OpenAPI / JSON Schema
 * {@code exclusiveMaximum} or {@code exclusiveMinimum} constraint (holding
 * either a {@link Boolean} for Swagger 2.0 / OpenAPI 3.0 or a {@link Number}
 * for JSON Schema Draft 2020-12 / OpenAPI 3.1+).
 */
public final class ExclusiveBound {
	/**
	 * The boolean flag when the exclusive bound is a boolean literal, or
	 * {@code null}.
	 */
	public final Boolean flag;

	/**
	 * The numeric threshold when the exclusive bound is a number literal, or
	 * {@code null}.
	 */
	public final Number bound;

	/**
	 * Constructs an {@code ExclusiveBound} instance.
	 *
	 * @param flag
	 *            The boolean flag, or {@code null}.
	 * @param bound
	 *            The numeric bound, or {@code null}.
	 */
	public ExclusiveBound(Boolean flag, Number bound) {
		this.flag = flag;
		this.bound = bound;
	}

	/**
	 * Creates an {@code ExclusiveBound} from a boolean value.
	 *
	 * @param flag
	 *            Whether the bound is exclusive.
	 * @return The created {@code ExclusiveBound}.
	 */
	public static ExclusiveBound ofBoolean(boolean flag) {
		return new ExclusiveBound(flag, null);
	}

	/**
	 * Creates an {@code ExclusiveBound} from a numeric value.
	 *
	 * @param bound
	 *            The exclusive numeric bound.
	 * @return The created {@code ExclusiveBound}.
	 */
	public static ExclusiveBound ofNumber(Number bound) {
		return new ExclusiveBound(null, bound);
	}

	/**
	 * Returns {@code true} if this instance holds a boolean flag.
	 *
	 * @return {@code true} if {@link #flag} is non-null.
	 */
	public boolean isBoolean() {
		return flag != null;
	}

	/**
	 * Returns {@code true} if this instance holds a numeric bound.
	 *
	 * @return {@code true} if {@link #bound} is non-null.
	 */
	public boolean isNumber() {
		return bound != null;
	}

	/**
	 * Compares this instance to another object for equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if both instances hold equal boolean or numeric values.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExclusiveBound)) {
			return false;
		}
		ExclusiveBound that = (ExclusiveBound) o;
		return Objects.equals(flag, that.flag) && Objects.equals(bound, that.bound);
	}

	/**
	 * Computes the hash code for this instance.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(flag, bound);
	}

	/**
	 * Returns a string representation of this instance.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isBoolean() ? String.valueOf(flag) : String.valueOf(bound);
	}
}
