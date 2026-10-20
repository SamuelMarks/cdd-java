package arazzo;

import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo Expression Type Object specifying a structured evaluation dialect
 * ({@code jsonpath}, {@code xpath}, or {@code jsonpointer}) and its explicit
 * specification version.
 */
public class ExpressionType {
	/**
	 * Default constructor.
	 */
	public ExpressionType() {
	}

	/**
	 * The evaluation dialect type ({@link CriterionEngineType#JSONPATH},
	 * {@link CriterionEngineType#XPATH}, or
	 * {@link CriterionEngineType#JSONPOINTER}).
	 */
	public CriterionEngineType type;

	/**
	 * The dialect specification version (e.g. {@code "rfc9535"},
	 * {@code "draft-goessner-dispatch-jsonpath-00"}, {@code "xpath-31"},
	 * {@code "xpath-20"}, {@code "xpath-10"}, {@code "rfc6901"}).
	 */
	public String version;

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
		return java.util.Arrays.asList(type, version, extensions);
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
		if (!(o instanceof ExpressionType)) {
			return false;
		}
		return fieldValues().equals(((ExpressionType) o).fieldValues());
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
		return "ExpressionType" + fieldValues();
	}
}
