package arazzo;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo Criterion Object defining a validation assertion evaluated against a
 * step response, status code, header, or message payload.
 */
public class Criterion extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Criterion() {
	}

	/**
	 * Runtime expression defining the evaluation root context (e.g.
	 * {@code "$statusCode"}, {@code "$response.body"}, {@code "$message.payload"}).
	 */
	public String context;

	/**
	 * The assertion condition expression (e.g. {@code "$statusCode == 200"},
	 * {@code "$.status == 'available'"}, or a regex pattern).
	 */
	public String condition;

	/**
	 * The evaluation engine type ({@code simple}, {@code regex}, {@code jsonpath},
	 * {@code xpath}, {@code jsonpointer}, or a versioned {@link ExpressionType}).
	 */
	public CriterionTypeOrExpression type;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(context, condition, type, extensions);
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
		if (!(o instanceof Criterion)) {
			return false;
		}
		return fieldValues().equals(((Criterion) o).fieldValues());
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
		return "Criterion" + fieldValues();
	}
}
