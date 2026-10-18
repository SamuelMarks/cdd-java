package arazzo;

import java.util.HashMap;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Arazzo 1.1.0 Selector Object defining a structured data extraction query over
 * a runtime context using JSONPath, XPath, or JSON Pointer.
 */
public class Selector {
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
