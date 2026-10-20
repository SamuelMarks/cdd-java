package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arazzo Request Body Object defining the MIME content type, base payload
 * template, and dynamic field replacements for a step request or channel
 * message.
 */
public class ArazzoRequestBody {
	/**
	 * Default constructor.
	 */
	public ArazzoRequestBody() {
	}

	/**
	 * The Content-Type media type for the request payload (e.g.
	 * {@code "application/json"}).
	 */
	public String contentType;

	/**
	 * The base payload literal, JSON object/array template, or runtime expression.
	 */
	public PayloadValue payload;

	/**
	 * Ordered list of dynamic field replacements applied to {@link #payload}.
	 */
	public List<PayloadReplacement> replacements;

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
		return java.util.Arrays.asList(contentType, payload, replacements, extensions);
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
		if (!(o instanceof ArazzoRequestBody)) {
			return false;
		}
		return fieldValues().equals(((ArazzoRequestBody) o).fieldValues());
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
		return "ArazzoRequestBody" + fieldValues();
	}
}
