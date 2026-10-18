package arazzo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Arazzo Failure Action Object defining error handling, retry policies, and
 * fallback transitions ({@code end}, {@code retry}, or {@code goto}) when a
 * step fails.
 */
public class FailureAction {
	/**
	 * Default constructor.
	 */
	public FailureAction() {
	}

	/**
	 * The name of the failure action.
	 */
	public String name;

	/**
	 * The failure transition type ({@link FailureActionType#END},
	 * {@link FailureActionType#RETRY}, or {@link FailureActionType#GOTO}).
	 */
	public FailureActionType type;

	/**
	 * Target {@code workflowId} for {@code goto} or pre-retry recovery execution
	 * (mutually exclusive with {@link #stepId}).
	 */
	public String workflowId;

	/**
	 * Target {@code stepId} in the current workflow for {@code goto} or pre-retry
	 * recovery execution (mutually exclusive with {@link #workflowId}).
	 */
	public String stepId;

	/**
	 * Parameters passed to the target fallback or pre-retry step/workflow (Arazzo
	 * 1.1.0).
	 */
	public List<ReusableOr<ArazzoParameter>> parameters;

	/**
	 * Non-negative delay in seconds before retrying a failed step.
	 */
	public Double retryAfter;

	/**
	 * Non-negative maximum number of retry attempts for a failed step.
	 */
	public Integer retryLimit;

	/**
	 * Guard criteria assertions matching specific failure conditions.
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
