package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Arazzo Step Object representing a single operation, AsyncAPI channel message,
 * or nested sub-workflow invocation within a {@link Workflow}.
 */
public class Step {
	/**
	 * Default constructor.
	 */
	public Step() {
	}

	/**
	 * Unique identifier for this step within its enclosing {@link Workflow}.
	 */
	public String stepId;

	/**
	 * Detailed description of what this step performs.
	 */
	public String description;

	/**
	 * Target OpenAPI {@code operationId} (optionally prefixed with
	 * {@code $sourceDescriptions.<name>.}); mutually exclusive with
	 * {@link #operationPath}, {@link #channelPath}, and {@link #workflowId}.
	 */
	public String operationId;

	/**
	 * Target OpenAPI operation JSON Pointer expression (e.g.
	 * {@code {$sourceDescriptions.petstore.url}#/paths/~1pet/post}); mutually
	 * exclusive with {@link #operationId}, {@link #channelPath}, and
	 * {@link #workflowId}.
	 */
	public String operationPath;

	/**
	 * Target AsyncAPI channel path expression (Arazzo 1.1.0); mutually exclusive
	 * with {@link #operationId}, {@link #operationPath}, and {@link #workflowId}.
	 */
	public String channelPath;

	/**
	 * Target local or external {@code workflowId}; mutually exclusive with
	 * {@link #operationId}, {@link #operationPath}, and {@link #channelPath}.
	 */
	public String workflowId;

	/**
	 * Step-level parameter bindings and reusable parameter overrides.
	 */
	public List<ReusableOr<ArazzoParameter>> parameters;

	/**
	 * Step request body payload and field replacement definitions.
	 */
	public ArazzoRequestBody requestBody;

	/**
	 * Assertions evaluated to determine whether this step succeeded.
	 */
	public List<Criterion> successCriteria;

	/**
	 * Step-specific success transition actions.
	 */
	public List<ReusableOr<SuccessAction>> onSuccess;

	/**
	 * Step-specific failure, retry, or fallback transition actions.
	 */
	public List<ReusableOr<FailureAction>> onFailure;

	/**
	 * Map of step output names to runtime expressions or {@link Selector} objects.
	 */
	public Map<String, OutputValue> outputs;

	/**
	 * Per-step execution timeout in milliseconds (Arazzo 1.1.0).
	 */
	public Integer timeout;

	/**
	 * AsyncAPI message correlation identifier or runtime expression (Arazzo 1.1.0).
	 */
	public String correlationId;

	/**
	 * AsyncAPI message direction ({@link StepActionType#SEND} or
	 * {@link StepActionType#RECEIVE}, Arazzo 1.1.0).
	 */
	public StepActionType action;

	/**
	 * List of prerequisite {@code stepId} values within the same workflow (Arazzo
	 * 1.1.0).
	 */
	public List<String> dependsOn;

	/**
	 * Specification vendor extensions ({@code ^x-}).
	 */
	public Map<String, Object> extensions = new HashMap<>();

	/**
	 * Validates that exactly one target ({@link #operationId},
	 * {@link #operationPath}, {@link #channelPath}, or {@link #workflowId}) is set
	 * on this step.
	 *
	 * @return {@code true} if exactly one target is non-null and non-empty;
	 *         {@code false} otherwise.
	 */
	public boolean hasValidSingleTarget() {
		int count = 0;
		if (operationId != null && !operationId.isEmpty()) {
			count++;
		}
		if (operationPath != null && !operationPath.isEmpty()) {
			count++;
		}
		if (channelPath != null && !channelPath.isEmpty()) {
			count++;
		}
		if (workflowId != null && !workflowId.isEmpty()) {
			count++;
		}
		return count == 1;
	}

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
