package arazzo;

import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo Parameter Object binding a named value or runtime expression to a
 * step, workflow, or action parameter location.
 */
public class ArazzoParameter {
	/**
	 * Default constructor.
	 */
	public ArazzoParameter() {
	}

	/**
	 * The name of the parameter.
	 */
	public String name;

	/**
	 * The location of the parameter ({@code path}, {@code query},
	 * {@code querystring}, {@code header}, {@code cookie}), or {@code null} when
	 * passing inputs to a sub-workflow.
	 */
	public ParameterIn in;

	/**
	 * The literal value, structured object/array, or runtime expression bound to
	 * this parameter.
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
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	private java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(name, in, value, extensions);
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
		if (!(o instanceof ArazzoParameter)) {
			return false;
		}
		return fieldValues().equals(((ArazzoParameter) o).fieldValues());
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
		return "ArazzoParameter" + fieldValues();
	}
}
