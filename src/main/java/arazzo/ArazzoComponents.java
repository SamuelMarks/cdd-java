package arazzo;

import java.util.HashMap;
import java.util.Map;
import openapi.Schema;
import openapi.TypeMapper;

/**
 * Arazzo Components Object holding reusable input schemas, parameters, success
 * actions, and failure actions.
 */
public class ArazzoComponents {
	/**
	 * Default constructor.
	 */
	public ArazzoComponents() {
	}

	/**
	 * Reusable JSON Schema 2020-12 input definitions keyed by component name.
	 */
	public Map<String, Schema> inputs;

	/**
	 * Reusable {@link ArazzoParameter} definitions keyed by component name.
	 */
	public Map<String, ArazzoParameter> parameters;

	/**
	 * Reusable {@link SuccessAction} definitions keyed by component name.
	 */
	public Map<String, SuccessAction> successActions;

	/**
	 * Reusable {@link FailureAction} definitions keyed by component name.
	 */
	public Map<String, FailureAction> failureActions;

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
