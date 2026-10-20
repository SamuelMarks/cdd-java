package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arazzo Success Action Object defining control flow transitions ({@code end}
 * or {@code goto}) when a step succeeds.
 */
public class SuccessAction {
	/**
	 * Default constructor.
	 */
	public SuccessAction() {
	}

	/**
	 * The name of the success action.
	 */
	public String name;

	/**
	 * The action transition type ({@link SuccessActionType#END} or
	 * {@link SuccessActionType#GOTO}).
	 */
	public SuccessActionType type;

	/**
	 * Target {@code workflowId} to jump to when {@code type == GOTO} (mutually
	 * exclusive with {@link #stepId}).
	 */
	public String workflowId;

	/**
	 * Target {@code stepId} in the current workflow to jump to when
	 * {@code type == GOTO} (mutually exclusive with {@link #workflowId}).
	 */
	public String stepId;

	/**
	 * Parameters passed to the target workflow or step (Arazzo 1.1.0).
	 */
	public List<ReusableOr<ArazzoParameter>> parameters;

	/**
	 * Guard criteria assertions that must hold for this success action to trigger.
	 */
	public List<Criterion> criteria;

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
		return java.util.Arrays.asList(name, type, workflowId, stepId, parameters, criteria, extensions);
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
		if (!(o instanceof SuccessAction)) {
			return false;
		}
		return fieldValues().equals(((SuccessAction) o).fieldValues());
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
		return "SuccessAction" + fieldValues();
	}
}
