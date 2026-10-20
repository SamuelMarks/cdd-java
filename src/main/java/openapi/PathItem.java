package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PathItem object.
 */
public class PathItem extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public PathItem() {
	}

	/**
	 * The ref property.
	 */
	public String ref;

	/**
	 * The summary property.
	 */
	public String summary;

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The get property.
	 */
	public Operation get;

	/**
	 * The put property.
	 */
	public Operation put;

	/**
	 * The post property.
	 */
	public Operation post;

	/**
	 * The delete property.
	 */
	public Operation delete;

	/**
	 * The options property.
	 */
	public Operation options;

	/**
	 * The head property.
	 */
	public Operation head;

	/**
	 * The patch property.
	 */
	public Operation patch;

	/**
	 * The trace property.
	 */
	public Operation trace;

	/**
	 * The query property.
	 */
	public Operation query;

	/**
	 * The additionalOperations property.
	 */
	public Map<String, Operation> additionalOperations;

	/**
	 * The servers property.
	 */
	public List<Server> servers;

	/**
	 * The parameters property.
	 */
	public List<ReferenceOr<Parameter>> parameters;

	/**
	 * Retrieves the {@link Operation} for the given {@link HttpVerb}.
	 *
	 * @param verb
	 *            The {@link HttpVerb}.
	 * @return The matching {@link Operation}, or {@code null}.
	 */
	public Operation getOperation(HttpVerb verb) {
		if (verb == null) {
			return null;
		}
		if (verb == HttpVerb.GET) {
			return get;
		}
		if (verb == HttpVerb.PUT) {
			return put;
		}
		if (verb == HttpVerb.POST) {
			return post;
		}
		if (verb == HttpVerb.DELETE) {
			return delete;
		}
		if (verb == HttpVerb.OPTIONS) {
			return options;
		}
		if (verb == HttpVerb.HEAD) {
			return head;
		}
		if (verb == HttpVerb.PATCH) {
			return patch;
		}
		if (verb == HttpVerb.TRACE) {
			return trace;
		}
		return query;
	}

	/**
	 * Retrieves the {@link Operation} for a verb string (checking standard
	 * {@link HttpVerb}s first, then {@link #additionalOperations}).
	 *
	 * @param verbStr
	 *            The HTTP method string (case-insensitive for standard verbs).
	 * @return The matching {@link Operation}, or {@code null}.
	 */
	public Operation getOperation(String verbStr) {
		HttpVerb verb = HttpVerb.fromString(verbStr);
		if (verb != null) {
			return getOperation(verb);
		}
		if (verbStr != null && additionalOperations != null) {
			return additionalOperations.get(verbStr);
		}
		return null;
	}

	/**
	 * Sets the {@link Operation} for the given {@link HttpVerb}.
	 *
	 * @param verb
	 *            The {@link HttpVerb}.
	 * @param op
	 *            The {@link Operation} to assign.
	 */
	public void setOperation(HttpVerb verb, Operation op) {
		if (verb == null) {
			return;
		}
		if (verb == HttpVerb.GET) {
			get = op;
		} else if (verb == HttpVerb.PUT) {
			put = op;
		} else if (verb == HttpVerb.POST) {
			post = op;
		} else if (verb == HttpVerb.DELETE) {
			delete = op;
		} else if (verb == HttpVerb.OPTIONS) {
			options = op;
		} else if (verb == HttpVerb.HEAD) {
			head = op;
		} else if (verb == HttpVerb.PATCH) {
			patch = op;
		} else if (verb == HttpVerb.TRACE) {
			trace = op;
		} else {
			query = op;
		}
	}

	/**
	 * Sets the {@link Operation} for a verb string (assigning standard
	 * {@link HttpVerb} fields when recognized, or {@link #additionalOperations}
	 * otherwise).
	 *
	 * @param verbStr
	 *            The HTTP method string.
	 * @param op
	 *            The {@link Operation} to assign.
	 */
	public void setOperation(String verbStr, Operation op) {
		HttpVerb verb = HttpVerb.fromString(verbStr);
		if (verb != null) {
			setOperation(verb, op);
		} else if (verbStr != null) {
			if (additionalOperations == null) {
				additionalOperations = new HashMap<>();
			}
			additionalOperations.put(verbStr, op);
		}
	}

	/**
	 * Returns an ordered map of all non-null standard {@link HttpVerb} operations
	 * on this {@code PathItem}.
	 *
	 * @return An ordered map from {@link HttpVerb} to {@link Operation}.
	 */
	public Map<HttpVerb, Operation> getOperationsMap() {
		Map<HttpVerb, Operation> map = new java.util.LinkedHashMap<>();
		for (HttpVerb verb : HttpVerb.values()) {
			Operation op = getOperation(verb);
			if (op != null) {
				map.put(verb, op);
			}
		}
		return map;
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(ref, summary, description, get, put, post, delete, options, head, patch, trace,
				query, additionalOperations, servers, parameters, extensions);
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
		if (!(o instanceof PathItem)) {
			return false;
		}
		return fieldValues().equals(((PathItem) o).fieldValues());
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
		return "PathItem" + fieldValues();
	}
}
