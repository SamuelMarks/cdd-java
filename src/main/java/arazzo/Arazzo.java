package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root Arazzo Specification Object (Arazzo 1.1.0).
 */
public class Arazzo {
	/**
	 * Default constructor.
	 */
	public Arazzo() {
	}

	/**
	 * The Arazzo specification semantic version string (e.g. {@code "1.1.0"}).
	 */
	public String arazzo = "1.1.0";

	/**
	 * The canonical base URI of this Arazzo document for resolving relative
	 * references.
	 */
	public String $self;

	/**
	 * Metadata about the Arazzo workflow specification.
	 */
	public ArazzoInfo info;

	/**
	 * List of external API specifications ({@code openapi}, {@code asyncapi},
	 * {@code arazzo}) referenced by workflows in this document.
	 */
	public List<SourceDescription> sourceDescriptions;

	/**
	 * List of workflows defined in this Arazzo document.
	 */
	public List<Workflow> workflows;

	/**
	 * Reusable component definitions (inputs, parameters, successActions,
	 * failureActions).
	 */
	public ArazzoComponents components;

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
		return java.util.Arrays.asList(arazzo, $self, info, sourceDescriptions, workflows, components, extensions);
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
		if (!(o instanceof Arazzo)) {
			return false;
		}
		return fieldValues().equals(((Arazzo) o).fieldValues());
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
		return "Arazzo" + fieldValues();
	}
}
