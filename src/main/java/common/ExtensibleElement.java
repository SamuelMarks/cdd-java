package common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for OpenAPI 3.2.0 and Arazzo 1.1.0 AST elements that support
 * specification vendor extensions ({@code ^x-}) and structural value equality.
 */
public abstract class ExtensibleElement {
	/**
	 * Default constructor.
	 */
	protected ExtensibleElement() {
	}

	/**
	 * Specification vendor extensions ({@code ^x-}).
	 */
	public Map<String, PayloadValue> extensions = new HashMap<>();

	/**
	 * Returns the vendor extensions map.
	 *
	 * @return The extensions map.
	 */
	public Map<String, PayloadValue> getExtensions() {
		return extensions;
	}

	/**
	 * Adds a strongly typed vendor extension if its name starts with {@code "x-"}.
	 *
	 * @param name
	 *            The extension property name.
	 * @param value
	 *            The strongly typed {@link PayloadValue} extension value.
	 */
	public void addExtension(String name, PayloadValue value) {
		if (name.startsWith("x-")) {
			extensions.put(name, value);
		}
	}

	/**
	 * Adds a string vendor extension if its name starts with {@code "x-"}.
	 *
	 * @param name
	 *            The extension property name.
	 * @param value
	 *            The string extension value.
	 */
	public void addExtension(String name, String value) {
		addExtension(name, PayloadValue.ofString(value));
	}

	/**
	 * Adds a boolean vendor extension if its name starts with {@code "x-"}.
	 *
	 * @param name
	 *            The extension property name.
	 * @param value
	 *            The boolean extension value.
	 */
	public void addExtension(String name, Boolean value) {
		addExtension(name, PayloadValue.ofBoolean(value));
	}

	/**
	 * Adds a numeric vendor extension if its name starts with {@code "x-"}.
	 *
	 * @param name
	 *            The extension property name.
	 * @param value
	 *            The numeric extension value.
	 */
	public void addExtension(String name, Number value) {
		addExtension(name, PayloadValue.ofNumber(value));
	}

	/**
	 * Returns all field values of this object (including {@link #extensions}) for
	 * structural comparison, hashing, and string formatting.
	 *
	 * @return The ordered list of field values.
	 */
	protected abstract List<Object> fieldValues();
}
