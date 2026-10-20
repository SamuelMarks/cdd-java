package openapi;

/**
 * Strongly typed domain exception for {@code cdd-java} parsing, emission, and
 * resolution errors.
 */
public class CddException extends RuntimeException {
	/** Serialization version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Categorizes domain-level failure modes across OpenAPI and Arazzo compilers.
	 */
	public enum ErrorCode {
		/** Malformed JSON, YAML, or Java AST input. */
		PARSE_ERROR,
		/** Unresolvable JSON Pointer, URI, or component reference. */
		RESOLUTION_ERROR,
		/** Invalid specification or emission invariant violation. */
		VALIDATION_ERROR
	}

	/** The strongly typed domain error code. */
	public final ErrorCode errorCode;

	/**
	 * Constructs a {@link CddException} with an error code and detail message.
	 *
	 * @param errorCode
	 *            The domain {@link ErrorCode}.
	 * @param message
	 *            The human-readable error message.
	 */
	public CddException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Constructs a {@link CddException} with an error code, detail message, and
	 * underlying cause.
	 *
	 * @param errorCode
	 *            The domain {@link ErrorCode}.
	 * @param message
	 *            The human-readable error message.
	 * @param cause
	 *            The underlying exception cause.
	 */
	public CddException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
}
