package arazzo;

import java.util.HashMap;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Arazzo Payload Replacement Object describing an in-place value injection at a
 * target JSON Pointer, JSONPath, or XPath location within a step request body.
 */
public class PayloadReplacement {
	/**
	 * Default constructor.
	 */
	public PayloadReplacement() {
	}

	/**
	 * Target location expression within the request payload (e.g.
	 * {@code "/customer/id"} or {@code "$.customer.id"}).
	 */
	public String target;

	/**
	 * Optional selector engine dialect for interpreting {@link #target} (Arazzo
	 * 1.1.0).
	 */
	public CriterionTypeOrExpression targetSelectorType;

	/**
	 * The runtime expression or literal value to inject at {@link #target}.
	 */
	public PayloadValue value;

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
