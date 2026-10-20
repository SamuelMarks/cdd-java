package openapi;

import com.github.javaparser.ast.type.Type;

/**
 * Facade delegating identifier sanitization to {@link IdentifierSanitizer} and
 * OpenAPI/Java AST type mapping to {@link JavaTypeMapper}.
 */
public final class TypeMapper {
	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private TypeMapper() {
	}

	/**
	 * Sanitizes a raw name into a valid Java identifier containing only ASCII
	 * letters, digits, and underscores.
	 *
	 * @param name
	 *            The raw identifier string.
	 * @return The sanitized identifier string (or empty string if {@code name} is
	 *         {@code null}).
	 */
	public static String sanitizeIdentifier(String name) {
		return IdentifierSanitizer.sanitizeIdentifier(name);
	}

	/**
	 * Sanitizes a raw string into strictly alphanumeric ASCII characters.
	 *
	 * @param name
	 *            The raw string.
	 * @return The sanitized alphanumeric string (or empty string if {@code name} is
	 *         {@code null}).
	 */
	public static String sanitizeAlphanumeric(String name) {
		return IdentifierSanitizer.sanitizeAlphanumeric(name);
	}

	/**
	 * Escapes backslashes, double quotes, newlines, and carriage returns for Java
	 * string literals.
	 *
	 * @param value
	 *            The input string to escape.
	 * @return The escaped string (or empty string if {@code value} is
	 *         {@code null}).
	 */
	public static String escape(String value) {
		return IdentifierSanitizer.escape(value);
	}

	/**
	 * Resolves an OpenAPI {@link ReferenceOr}{@code <}{@link Schema}{@code >}
	 * wrapper into its corresponding Java type string.
	 *
	 * @param refOr
	 *            The {@link ReferenceOr}{@code <}{@link Schema}{@code >} wrapper.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @param mapAsObject
	 *            Whether to map {@code object} schemas with
	 *            {@code additionalProperties} to {@code Map<String, Object>}
	 *            unconditionally.
	 * @return The resolved Java type string.
	 */
	public static String toJavaType(ReferenceOr<Schema> refOr, OpenAPI model, boolean mapAsObject) {
		return JavaTypeMapper.toJavaType(refOr, model, mapAsObject);
	}

	/**
	 * Resolves an OpenAPI {@link Schema} into its corresponding Java type string.
	 *
	 * @param schemaMap
	 *            The OpenAPI {@link Schema}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @param mapAsObject
	 *            Whether to map {@code object} schemas with
	 *            {@code additionalProperties} to {@code Map<String, Object>}
	 *            unconditionally.
	 * @return The resolved Java type string.
	 */
	public static String toJavaType(Schema schemaMap, OpenAPI model, boolean mapAsObject) {
		return JavaTypeMapper.toJavaType(schemaMap, model, mapAsObject);
	}

	/**
	 * Resolves an OpenAPI {@link Schema} object into its corresponding Java type
	 * string.
	 *
	 * @param schemaObj
	 *            The OpenAPI {@link Schema} or reference object.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @param mapAsObject
	 *            Whether to map {@code object} schemas with
	 *            {@code additionalProperties} to {@code Map<String, Object>}
	 *            unconditionally (used by seeder) or recursively resolve value
	 *            types.
	 * @return The resolved Java type string.
	 */
	public static String toJavaType(Object schemaObj, OpenAPI model, boolean mapAsObject) {
		return JavaTypeMapper.toJavaType(schemaObj, model, mapAsObject);
	}

	/**
	 * Maps a JavaParser AST {@link Type} into its corresponding OpenAPI
	 * {@link Schema} type, format, and reference properties.
	 *
	 * @param type
	 *            The JavaParser AST {@link Type}.
	 * @param propSchema
	 *            The target OpenAPI {@link Schema} to populate.
	 * @param fallbackPrimitiveToObject
	 *            Whether non-primitive, non-class, non-array types default to
	 *            {@code "object"}.
	 */
	public static void toSchema(Type type, Schema propSchema, boolean fallbackPrimitiveToObject) {
		JavaTypeMapper.toSchema(type, propSchema, fallbackPrimitiveToObject);
	}
}
