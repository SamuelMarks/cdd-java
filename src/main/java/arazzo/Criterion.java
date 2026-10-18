package arazzo;

import java.util.HashMap;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Arazzo Criterion Object defining a validation assertion evaluated against a
 * step response, status code, header, or message payload.
 */
public class Criterion {
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
	 * Specification vendor extensions ({@code ^x-}).
	 */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Returns the vendor extensions map.
	 *
	 * @return The extensions map.
	 */
	public Map<String, Object> getExtensions() {
		return extensions;
	}

	/**
	 * Adds a vendor extension if its name starts with {@code "x-"}.
	 *
	 * @param name
	 *            The extension property name.
	 * @param value
	 *            The extension value.
	 */
	public void addExtension(String name, Object value) {
		if (name.startsWith("x-")) {
			extensions.put(name, value);
		}
	}

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
