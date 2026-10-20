package openapi;

import common.ExtensibleElement;
import common.PayloadValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Header object.
 */
public class Header extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Header() {
	}

	/**
	 * The description property.
	 */
	public String description;

	/**
	 * The required property.
	 */
	public Boolean required;

	/**
	 * The deprecated property.
	 */
	public Boolean deprecated;

	/**
	 * The allowEmptyValue property.
	 */
	public Boolean allowEmptyValue;

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
	 * The style property.
	 */
	public String style;

	/**
	 * The explode property.
	 */
	public Boolean explode;

	/**
	 * The allowReserved property.
	 */
	public Boolean allowReserved;

	/**
	 * The schema property.
	 */
	public ReferenceOr<Schema> schema;

	/**
	 * The example property.
	 */
	public PayloadValue example;

	/**
	 * The examples property.
	 */
	public Map<String, ReferenceOr<Example>> examples;

	/**
	 * The content property.
	 */
	public Map<String, MediaType> content;

	/**
	 * Returns the strongly typed {@link ParameterStyle} for {@link #style}.
	 *
	 * @return The resolved {@link ParameterStyle}, or {@code null}.
	 */
	public ParameterStyle getParameterStyle() {
		return ParameterStyle.fromString(style);
	}

	/**
	 * Sets {@link #style} from a strongly typed {@link ParameterStyle}.
	 *
	 * @param parameterStyle
	 *            The {@link ParameterStyle}.
	 */
	public void setParameterStyle(ParameterStyle parameterStyle) {
		this.style = parameterStyle != null ? parameterStyle.value : null;
	}

	/**
	 * Returns all field values of this object for structural comparison and
	 * hashing.
	 *
	 * @return The list of field values.
	 */
	@Override
	protected java.util.List<Object> fieldValues() {
		return java.util.Arrays.asList(description, required, deprecated, allowEmptyValue, type, format, items,
				collectionFormat, defaultValue, maximum, exclusiveMaximum, minimum, exclusiveMinimum, maxLength,
				minLength, pattern, maxItems, minItems, uniqueItems, enumValues, multipleOf, style, explode,
				allowReserved, schema, example, examples, content, extensions);
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
		if (!(o instanceof Header)) {
			return false;
		}
		return fieldValues().equals(((Header) o).fieldValues());
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
		return "Header" + fieldValues();
	}
}
