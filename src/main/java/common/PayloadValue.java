package common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Strongly typed recursive representation of a JSON/YAML value or runtime
 * expression used across OpenAPI 3.2.0 and Arazzo 1.1.0 AST nodes.
 */
public final class PayloadValue {
	/**
	 * Discriminator kind for a {@link PayloadValue}.
	 */
	public enum Kind {
		/** Explicit JSON/YAML {@code null} literal. */
		NULL,
		/** String literal or runtime expression (e.g. {@code $inputs.id}). */
		STRING,
		/** Numeric literal ({@link Integer}, {@link Long}, or {@link Double}). */
		NUMBER,
		/** Boolean literal ({@code true} or {@code false}). */
		BOOLEAN,
		/** Key-value map of nested {@link PayloadValue} nodes. */
		OBJECT,
		/** Ordered list of nested {@link PayloadValue} nodes. */
		ARRAY
	}

	/** The discriminator kind of this value. */
	public final Kind kind;
	/** The string or runtime expression value when {@code kind == STRING}. */
	public final String stringVal;
	/** The numeric value when {@code kind == NUMBER}. */
	public final Number numberVal;
	/** The boolean value when {@code kind == BOOLEAN}. */
	public final Boolean booleanVal;
	/** The nested object map when {@code kind == OBJECT}. */
	public final Map<String, PayloadValue> objectVal;
	/** The nested array list when {@code kind == ARRAY}. */
	public final List<PayloadValue> arrayVal;

	/**
	 * Constructs a {@code PayloadValue} with the given typed components.
	 *
	 * @param kind
	 *            The value kind discriminator.
	 * @param stringVal
	 *            String value if {@code STRING}.
	 * @param numberVal
	 *            Numeric value if {@code NUMBER}.
	 * @param booleanVal
	 *            Boolean value if {@code BOOLEAN}.
	 * @param objectVal
	 *            Object map if {@code OBJECT}.
	 * @param arrayVal
	 *            Array list if {@code ARRAY}.
	 */
	public PayloadValue(Kind kind, String stringVal, Number numberVal, Boolean booleanVal,
			Map<String, PayloadValue> objectVal, List<PayloadValue> arrayVal) {
		this.kind = kind;
		this.stringVal = stringVal;
		this.numberVal = numberVal;
		this.booleanVal = booleanVal;
		this.objectVal = objectVal;
		this.arrayVal = arrayVal;
	}

	/**
	 * Creates an explicit {@code null} {@link PayloadValue}.
	 *
	 * @return A {@code NULL} {@link PayloadValue}.
	 */
	public static PayloadValue ofNull() {
		return new PayloadValue(Kind.NULL, null, null, null, null, null);
	}

	/**
	 * Creates a string or runtime expression {@link PayloadValue}.
	 *
	 * @param val
	 *            The string or runtime expression.
	 * @return A {@code STRING} {@link PayloadValue} (or {@code NULL} if {@code val}
	 *         is {@code null}).
	 */
	public static PayloadValue ofString(String val) {
		if (val == null) {
			return ofNull();
		}
		return new PayloadValue(Kind.STRING, val, null, null, null, null);
	}

	/**
	 * Creates a numeric {@link PayloadValue}.
	 *
	 * @param val
	 *            The numeric value.
	 * @return A {@code NUMBER} {@link PayloadValue} (or {@code NULL} if {@code val}
	 *         is {@code null}).
	 */
	public static PayloadValue ofNumber(Number val) {
		if (val == null) {
			return ofNull();
		}
		return new PayloadValue(Kind.NUMBER, null, val, null, null, null);
	}

	/**
	 * Creates a boolean {@link PayloadValue}.
	 *
	 * @param val
	 *            The boolean value.
	 * @return A {@code BOOLEAN} {@link PayloadValue} (or {@code NULL} if
	 *         {@code val} is {@code null}).
	 */
	public static PayloadValue ofBoolean(Boolean val) {
		if (val == null) {
			return ofNull();
		}
		return new PayloadValue(Kind.BOOLEAN, null, null, val, null, null);
	}

	/**
	 * Creates an object map {@link PayloadValue}.
	 *
	 * @param val
	 *            The map of property names to nested {@link PayloadValue}s.
	 * @return An {@code OBJECT} {@link PayloadValue} (or {@code NULL} if
	 *         {@code val} is {@code null}).
	 */
	public static PayloadValue ofObject(Map<String, PayloadValue> val) {
		if (val == null) {
			return ofNull();
		}
		return new PayloadValue(Kind.OBJECT, null, null, null, val, null);
	}

	/**
	 * Creates an array list {@link PayloadValue}.
	 *
	 * @param val
	 *            The ordered list of nested {@link PayloadValue}s.
	 * @return An {@code ARRAY} {@link PayloadValue} (or {@code NULL} if {@code val}
	 *         is {@code null}).
	 */
	public static PayloadValue ofArray(List<PayloadValue> val) {
		if (val == null) {
			return ofNull();
		}
		return new PayloadValue(Kind.ARRAY, null, null, null, null, val);
	}

	/**
	 * Recursively converts a raw {@link org.json} value into a strongly typed
	 * {@link PayloadValue}.
	 *
	 * @param raw
	 *            The raw JSON value ({@link JSONObject#NULL}, {@link String},
	 *            {@link Number}, {@link Boolean}, {@link JSONObject}, or
	 *            {@link JSONArray}).
	 * @return The parsed {@link PayloadValue}.
	 */
	public static PayloadValue fromJsonValue(Object raw) {
		if (raw == null || JSONObject.NULL.equals(raw)) {
			return ofNull();
		}
		if (raw instanceof Boolean) {
			return ofBoolean((Boolean) raw);
		}
		if (raw instanceof Number) {
			return ofNumber((Number) raw);
		}
		if (raw instanceof JSONObject) {
			JSONObject jo = (JSONObject) raw;
			Map<String, PayloadValue> map = new LinkedHashMap<>();
			for (String k : jo.keySet()) {
				map.put(k, fromJsonValue(jo.get(k)));
			}
			return ofObject(map);
		}
		if (raw instanceof JSONArray) {
			JSONArray ja = (JSONArray) raw;
			List<PayloadValue> list = new ArrayList<>();
			for (int i = 0; i < ja.length(); i++) {
				list.add(fromJsonValue(ja.get(i)));
			}
			return ofArray(list);
		}
		return ofString(String.valueOf(raw));
	}

	/**
	 * Recursively converts a strongly typed {@link PayloadValue} into a
	 * {@link org.json} compatible object.
	 *
	 * @param pv
	 *            The {@link PayloadValue}.
	 * @return The serialized JSON value.
	 */
	public static Object toJsonValue(PayloadValue pv) {
		if (pv.isNull()) {
			return JSONObject.NULL;
		}
		if (pv.isBoolean()) {
			return pv.booleanVal;
		}
		if (pv.isNumber()) {
			return pv.numberVal;
		}
		if (pv.isObject()) {
			JSONObject jo = new JSONObject();
			for (Map.Entry<String, PayloadValue> e : pv.objectVal.entrySet()) {
				jo.put(e.getKey(), toJsonValue(e.getValue()));
			}
			return jo;
		}
		if (pv.isArray()) {
			JSONArray ja = new JSONArray();
			for (PayloadValue item : pv.arrayVal) {
				ja.put(toJsonValue(item));
			}
			return ja;
		}
		return pv.stringVal;
	}

	/**
	 * Returns {@code true} if this value holds a string or runtime expression.
	 *
	 * @return {@code true} if {@code kind == Kind.STRING}.
	 */
	public boolean isString() {
		return kind == Kind.STRING;
	}

	/**
	 * Returns {@code true} if this value holds a number.
	 *
	 * @return {@code true} if {@code kind == Kind.NUMBER}.
	 */
	public boolean isNumber() {
		return kind == Kind.NUMBER;
	}

	/**
	 * Returns {@code true} if this value holds a boolean.
	 *
	 * @return {@code true} if {@code kind == Kind.BOOLEAN}.
	 */
	public boolean isBoolean() {
		return kind == Kind.BOOLEAN;
	}

	/**
	 * Returns {@code true} if this value holds an object map.
	 *
	 * @return {@code true} if {@code kind == Kind.OBJECT}.
	 */
	public boolean isObject() {
		return kind == Kind.OBJECT;
	}

	/**
	 * Returns {@code true} if this value holds an array list.
	 *
	 * @return {@code true} if {@code kind == Kind.ARRAY}.
	 */
	public boolean isArray() {
		return kind == Kind.ARRAY;
	}

	/**
	 * Returns {@code true} if this value represents {@code null}.
	 *
	 * @return {@code true} if {@code kind == Kind.NULL}.
	 */
	public boolean isNull() {
		return kind == Kind.NULL;
	}

	/**
	 * Compares this {@code PayloadValue} to another object for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PayloadValue)) {
			return false;
		}
		PayloadValue that = (PayloadValue) o;
		boolean numbersMatch;
		if (numberVal != null && that.numberVal != null) {
			numbersMatch = Double.compare(numberVal.doubleValue(), that.numberVal.doubleValue()) == 0;
		} else {
			numbersMatch = Objects.equals(numberVal, that.numberVal);
		}
		return kind == that.kind && Objects.equals(stringVal, that.stringVal) && numbersMatch
				&& Objects.equals(booleanVal, that.booleanVal) && Objects.equals(objectVal, that.objectVal)
				&& Objects.equals(arrayVal, that.arrayVal);
	}

	/**
	 * Computes a structural hash code for this {@code PayloadValue}.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		Double normNum = numberVal == null ? null : numberVal.doubleValue();
		return Objects.hash(kind, stringVal, normNum, booleanVal, objectVal, arrayVal);
	}

	/**
	 * Returns a human-readable string representation of this {@code PayloadValue}.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		if (kind == Kind.STRING) {
			return stringVal;
		}
		if (kind == Kind.NUMBER) {
			return String.valueOf(numberVal);
		}
		if (kind == Kind.BOOLEAN) {
			return String.valueOf(booleanVal);
		}
		if (kind == Kind.OBJECT) {
			return String.valueOf(objectVal);
		}
		if (kind == Kind.ARRAY) {
			return String.valueOf(arrayVal);
		}
		return "null";
	}
}
