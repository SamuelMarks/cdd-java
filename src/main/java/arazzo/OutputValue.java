package arazzo;

import java.util.Objects;

/**
 * Strongly typed union representing an Arazzo 1.1.0 output extraction value,
 * holding either a raw runtime expression string (e.g.
 * {@code $response.body#/id}) or a structured {@link Selector} object.
 */
public final class OutputValue {
	/**
	 * The raw runtime expression string when specified as a string, or
	 * {@code null}.
	 */
	public final String expression;

	/**
	 * The structured {@link Selector} object when specified as an object, or
	 * {@code null}.
	 */
	public final Selector selector;

	/**
	 * Constructs an {@code OutputValue} with the given runtime expression or
	 * selector.
	 *
	 * @param expression
	 *            The runtime expression string, or {@code null}.
	 * @param selector
	 *            The structured {@link Selector}, or {@code null}.
	 */
	public OutputValue(String expression, Selector selector) {
		this.expression = expression;
		this.selector = selector;
	}

	/**
	 * Creates an {@code OutputValue} wrapping a runtime expression string.
	 *
	 * @param expression
	 *            The runtime expression string.
	 * @return A new {@code OutputValue} holding {@code expression}.
	 */
	public static OutputValue ofExpression(String expression) {
		return new OutputValue(expression, null);
	}

	/**
	 * Creates an {@code OutputValue} wrapping a structured {@link Selector}.
	 *
	 * @param selector
	 *            The {@link Selector} object.
	 * @return A new {@code OutputValue} holding {@code selector}.
	 */
	public static OutputValue ofSelector(Selector selector) {
		return new OutputValue(null, selector);
	}

	/**
	 * Returns {@code true} if this output value holds a raw runtime expression.
	 *
	 * @return {@code true} if {@link #expression} is non-null.
	 */
	public boolean isExpression() {
		return expression != null;
	}

	/**
	 * Returns {@code true} if this output value holds a structured
	 * {@link Selector}.
	 *
	 * @return {@code true} if {@link #selector} is non-null.
	 */
	public boolean isSelector() {
		return selector != null;
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
		if (!(o instanceof OutputValue)) {
			return false;
		}
		OutputValue that = (OutputValue) o;
		return Objects.equals(expression, that.expression) && Objects.equals(selector, that.selector);
	}

	/**
	 * Computes the hash code for this wrapper.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(expression, selector);
	}

	/**
	 * Returns a human-readable string representation of this wrapper.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isSelector() ? String.valueOf(selector) : String.valueOf(expression);
	}
}
