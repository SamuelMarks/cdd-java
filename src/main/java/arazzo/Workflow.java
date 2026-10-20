package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import openapi.ReferenceOr;
import openapi.Schema;

/**
 * Arazzo Workflow Object describing an executable sequence of API steps,
 * inputs, outputs, dependencies, and default actions.
 */
public class Workflow {
	/**
	 * Default constructor.
	 */
	public Workflow() {
	}

	/**
	 * Unique identifier for this workflow within the Arazzo document.
	 */
	public String workflowId;

	/**
	 * Short summary of what this workflow accomplishes.
	 */
	public String summary;

	/**
	 * Detailed description of the workflow (supports CommonMark).
	 */
	public String description;

	/**
	 * JSON Schema 2020-12 definition (or {@code $components.inputs.<name>}
	 * reference) describing workflow input parameters.
	 */
	public ReferenceOr<Schema> inputs;

	/**
	 * List of prerequisite {@code workflowId} strings or runtime expressions that
	 * must complete before this workflow executes.
	 */
	public List<String> dependsOn;

	/**
	 * Ordered list of {@link Step} objects making up this workflow.
	 */
	public List<Step> steps;

	/**
	 * Workflow-level default success actions applied to steps in this workflow.
	 */
	public List<ReusableOr<SuccessAction>> successActions;

	/**
	 * Workflow-level default failure actions applied to steps in this workflow.
	 */
	public List<ReusableOr<FailureAction>> failureActions;

	/**
	 * Map of workflow output names to runtime expressions or {@link Selector}
	 * definitions.
	 */
	public Map<String, OutputValue> outputs;

	/**
	 * Workflow-level default parameters inherited by steps in this workflow.
	 */
	public List<ReusableOr<ArazzoParameter>> parameters;

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
		return java.util.Arrays.asList(workflowId, summary, description, inputs, dependsOn, steps, successActions,
				failureActions, outputs, parameters, extensions);
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
		if (!(o instanceof Workflow)) {
			return false;
		}
		return fieldValues().equals(((Workflow) o).fieldValues());
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
		return "Workflow" + fieldValues();
	}
}
