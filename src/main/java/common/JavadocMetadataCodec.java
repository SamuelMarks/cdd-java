package common;

/**
 * Lossless encoding and decoding utilities for custom Javadoc metadata tags
 * where spaces, pipes, and underscores must round-trip without corruption.
 */
public final class JavadocMetadataCodec {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private JavadocMetadataCodec() {
	}

	/**
	 * Encodes a token so spaces are represented as {@code _} while literal
	 * underscores {@code _} are escaped as {@code %5F} and literal percent signs
	 * {@code %} are escaped as {@code %25}.
	 *
	 * @param text
	 *            The raw text token.
	 * @return The encoded token (or empty string if {@code text} is {@code null}).
	 */
	public static String encodeUnderscoreToken(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("%", "%25").replace("_", "%5F").replace(" ", "_");
	}

	/**
	 * Decodes a token encoded by {@link #encodeUnderscoreToken(String)}, restoring
	 * spaces from {@code _}, literal underscores from {@code %5F}, and literal
	 * percent signs from {@code %25}.
	 *
	 * @param encoded
	 *            The encoded token.
	 * @return The decoded text token (or empty string if {@code encoded} is
	 *         {@code null}).
	 */
	public static String decodeUnderscoreToken(String encoded) {
		if (encoded == null) {
			return "";
		}
		return encoded.replace("_", " ").replace("%5F", "_").replace("%25", "%");
	}
}
