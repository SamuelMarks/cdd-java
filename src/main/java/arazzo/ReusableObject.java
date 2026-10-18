package arazzo;

import openapi.TypeMapper;

/**
 * Arazzo Reusable Object referencing a component definition in
 * {@code $components.parameters.*}, {@code $components.successActions.*}, or
 * {@code $components.failureActions.*}, with an optional value override.
 */
public class ReusableObject {
	/**
	 * Default constructor.
	 */
	public ReusableObject() {
	}

	/**
	 * Runtime expression pointing to the reusable component (e.g.
	 * {@code $components.parameters.petId}).
	 */
	public String reference;

	/**
	 * Optional overridden value when referencing a reusable parameter.
	 */
	public PayloadValue value;

	/**
	 * Compares this object with another for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal.
	 */
	@Override
	public boolean equals(Object o) {
		return TypeMapper.structuralEquals(this, o);
	}

	/**
	 * Returns a structural hash code for this object.
	 *
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		return TypeMapper.structuralHashCode(this);
	}

	/**
	 * Returns a structural string representation of this object.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return TypeMapper.structuralToString(this);
	}
}
