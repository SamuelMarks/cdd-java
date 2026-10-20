package openapi;

import common.ExtensibleElement;
import common.PayloadValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Items object for Swagger 2.0.
 */
public class Items extends ExtensibleElement {
	/** Default constructor. */
	public Items() {
	}

	/** The type property. */
	public String type;
	/** The format property. */
	public String format;
	/** The items property. */
	public Items items;
	/** The collectionFormat property. */
	public String collectionFormat;
	/** The defaultValue property. */
	public PayloadValue defaultValue;
	/** The maximum property. */
	public Number maximum;
	/** The exclusiveMaximum property. */
	public Boolean exclusiveMaximum;
	/** The minimum property. */
	public Number minimum;
	/** The exclusiveMinimum property. */
	public Boolean exclusiveMinimum;
	/** The maxLength property. */
	public Integer maxLength;
	/** The minLength property. */
	public Integer minLength;
	/** The pattern property. */
	public String pattern;
	/** The maxItems property. */
	public Integer maxItems;
	/** The minItems property. */
	public Integer minItems;
	/** The uniqueItems property. */
	public Boolean uniqueItems;
	/** The enumValues property. */
	public List<PayloadValue> enumValues;
	/** The multipleOf property. */
	public Number multipleOf;

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(type, format, items, collectionFormat, defaultValue, maximum, exclusiveMaximum,
				minimum, exclusiveMinimum, maxLength, minLength, pattern, maxItems, minItems, uniqueItems, enumValues,
				multipleOf, extensions);
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
		if (!(o instanceof Items)) {
			return false;
		}
		return fieldValues().equals(((Items) o).fieldValues());
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
		return "Items" + fieldValues();
	}
}
