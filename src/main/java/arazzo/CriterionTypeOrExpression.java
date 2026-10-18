package arazzo;

import java.util.Objects;

/**
 * Strongly typed union holding either a simple {@link CriterionEngineType} enum
 * value or a structured {@link ExpressionType} object with explicit dialect
 * version.
 */
public final class CriterionTypeOrExpression {
	/**
	 * The simple engine type when specified as a string literal, or {@code null}.
	 */
	public final CriterionEngineType simpleType;

	/**
	 * The versioned expression type object when specified as an object, or
	 * {@code null}.
	 */
	public final ExpressionType expressionType;

	/**
	 * Constructs a {@code CriterionTypeOrExpression} with the given simple or
	 * versioned expression type.
	 *
	 * @param simpleType
	 *            The simple {@link CriterionEngineType}, or {@code null}.
	 * @param expressionType
	 *            The versioned {@link ExpressionType}, or {@code null}.
	 */
	public CriterionTypeOrExpression(CriterionEngineType simpleType, ExpressionType expressionType) {
		this.simpleType = simpleType;
		this.expressionType = expressionType;
	}

	/**
	 * Creates a {@code CriterionTypeOrExpression} wrapping a simple
	 * {@link CriterionEngineType}.
	 *
	 * @param simpleType
	 *            The simple engine type.
	 * @return A new {@code CriterionTypeOrExpression} instance.
	 */
	public static CriterionTypeOrExpression ofSimple(CriterionEngineType simpleType) {
		return new CriterionTypeOrExpression(simpleType, null);
	}

	/**
	 * Creates a {@code CriterionTypeOrExpression} wrapping a versioned
	 * {@link ExpressionType}.
	 *
	 * @param expressionType
	 *            The versioned expression type object.
	 * @return A new {@code CriterionTypeOrExpression} instance.
	 */
	public static CriterionTypeOrExpression ofExpressionType(ExpressionType expressionType) {
		return new CriterionTypeOrExpression(null, expressionType);
	}

	/**
	 * Returns {@code true} if this instance holds a simple
	 * {@link CriterionEngineType}.
	 *
	 * @return {@code true} if {@link #simpleType} is non-null.
	 */
	public boolean isSimple() {
		return simpleType != null;
	}

	/**
	 * Returns {@code true} if this instance holds a versioned
	 * {@link ExpressionType}.
	 *
	 * @return {@code true} if {@link #expressionType} is non-null.
	 */
	public boolean isExpressionType() {
		return expressionType != null;
	}

	/**
	 * Resolves the effective {@link CriterionEngineType} regardless of whether this
	 * wrapper holds a simple enum or an {@link ExpressionType}.
	 *
	 * @return The resolved {@link CriterionEngineType}, or
	 *         {@link CriterionEngineType#SIMPLE} if unset.
	 */
	public CriterionEngineType resolveEngineType() {
		if (simpleType != null) {
			return simpleType;
		}
		if (expressionType != null && expressionType.type != null) {
			return expressionType.type;
		}
		return CriterionEngineType.SIMPLE;
	}

	/**
	 * Compares this wrapper to another object for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CriterionTypeOrExpression)) {
			return false;
		}
		CriterionTypeOrExpression that = (CriterionTypeOrExpression) o;
		return simpleType == that.simpleType && Objects.equals(expressionType, that.expressionType);
	}

	/**
	 * Computes the hash code for this wrapper.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(simpleType, expressionType);
	}

	/**
	 * Returns a human-readable string representation of this wrapper.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isExpressionType() ? String.valueOf(expressionType) : String.valueOf(simpleType);
	}
}
