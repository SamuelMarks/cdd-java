package openapi;

import common.ExtensibleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encoding object.
 */
public class Encoding extends ExtensibleElement {
	/**
	 * Default constructor.
	 */
	public Encoding() {
	}

	/**
	 * The contentType property.
	 */
	public String contentType;

	/**
	 * The headers property.
	 */
	public Map<String, ReferenceOr<Header>> headers;

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
	 * The encoding property.
	 */
	public Map<String, Encoding> encoding;

	/**
	 * The prefixEncoding property.
	 */
	public List<Encoding> prefixEncoding;

	/**
	 * The itemEncoding property.
	 */
	public Encoding itemEncoding;

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
		return java.util.Arrays.asList(contentType, headers, style, explode, allowReserved, encoding, prefixEncoding,
				itemEncoding, extensions);
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
		if (!(o instanceof Encoding)) {
			return false;
		}
		return fieldValues().equals(((Encoding) o).fieldValues());
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
		return "Encoding" + fieldValues();
	}
}
