package arazzo;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Arazzo Source Description Object linking a named specification
 * ({@code openapi}, {@code asyncapi}, or {@code arazzo}) to its URI.
 */
public class SourceDescription extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public SourceDescription() {
	}

	/**
	 * Unique name identifying this source description (matching
	 * {@code [A-Za-z0-9_\-]+}).
	 */
	public String name;

	/**
	 * Absolute URI or relative reference to the source description document.
	 */
	public String url;

	/**
	 * The specification type ({@link SourceType#OPENAPI},
	 * {@link SourceType#ASYNCAPI}, or {@link SourceType#ARAZZO}).
	 */
	public SourceType type;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(name, url, type, extensions);
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
		if (!(o instanceof SourceDescription)) {
			return false;
		}
		return fieldValues().equals(((SourceDescription) o).fieldValues());
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
		return "SourceDescription" + fieldValues();
	}
}
