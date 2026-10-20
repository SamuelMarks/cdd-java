package arazzo;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo 1.1.0 Selector Object defining a structured data extraction query over
 * a runtime context using JSONPath, XPath, or JSON Pointer.
 */
public class Selector extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Selector() {
	}

	/**
	 * Runtime expression identifying the source document or payload (e.g.
	 * {@code "$response.body"} or {@code "$message.payload"}).
	 */
	public String context;

	/**
	 * Query expression executed against {@link #context} (e.g.
	 * {@code "$.items[0].id"}, {@code "/id"}, {@code "//user/id"}).
	 */
	public String selector;

	/**
	 * Selector engine dialect ({@code jsonpath}, {@code xpath},
	 * {@code jsonpointer}, or a versioned {@link ExpressionType}).
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
		return java.util.Arrays.asList(context, selector, type, extensions);
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
		if (!(o instanceof Selector)) {
			return false;
		}
		return fieldValues().equals(((Selector) o).fieldValues());
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
		return "Selector" + fieldValues();
	}
}
