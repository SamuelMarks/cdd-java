package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Responses object.
 */
public class Responses extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Responses() {
	}

	/**
	 * Default response.
	 */
	public ReferenceOr<Response> defaultResponse;

	/**
	 * Status codes.
	 */
	public Map<String, ReferenceOr<Response>> statusCodes = new HashMap<>();

	/**
	 * Adds a response or reference for a status code (or {@code "default"}).
	 *
	 * @param statusCode
	 *            The HTTP status code (e.g. {@code "200"}, {@code "4XX"}, or
	 *            {@code "default"}).
	 * @param response
	 *            The {@link ReferenceOr} wrapping the {@link Response}.
	 */
	public void addStatusCode(String statusCode, ReferenceOr<Response> response) {
		if (statusCode == null || response == null) {
			return;
		}
		if ("default".equals(statusCode)) {
			defaultResponse = response;
		} else if (!statusCode.startsWith("x-")) {
			statusCodes.put(statusCode, response);
		}
	}

	/**
	 * Adds an inline {@link Response} for a status code (or {@code "default"}).
	 *
	 * @param statusCode
	 *            The HTTP status code (e.g. {@code "200"}, {@code "4XX"}, or
	 *            {@code "default"}).
	 * @param response
	 *            The inline {@link Response}.
	 */
	public void addStatusCode(String statusCode, Response response) {
		if (response != null) {
			addStatusCode(statusCode, ReferenceOr.ofValue(response));
		}
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(defaultResponse, statusCodes, extensions);
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
		if (!(o instanceof Responses)) {
			return false;
		}
		return fieldValues().equals(((Responses) o).fieldValues());
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
		return "Responses" + fieldValues();
	}
}
