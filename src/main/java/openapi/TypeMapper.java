package openapi;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Provides shared utilities for mapping OpenAPI schemas to and from Java AST
 * types, sanitizing identifiers, and escaping string literals.
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
		if (name == null) {
			return "";
		}
		return name.replaceAll("[^a-zA-Z0-9_]", "");
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
		if (name == null) {
			return "";
		}
		return name.replaceAll("[^a-zA-Z0-9]", "");
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
		if (value == null) {
			return "";
		}
		String bs = String.valueOf((char) 92);
		String qt = String.valueOf((char) 34);
		String nl = String.valueOf((char) 10);
		String cr = String.valueOf((char) 13);
		return value.replace(bs, bs + bs).replace(qt, bs + qt).replace(nl, bs + "n").replace(cr, bs + "r");
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
		if (!(schemaObj instanceof Schema)) {
			return "Object";
		}
		Schema schemaMap = (Schema) schemaObj;
		if (schemaMap.$ref != null) {
			return schemaMap.$ref.substring(schemaMap.$ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
		}
		String schemaType = schemaMap.type instanceof String ? (String) schemaMap.type : null;
		if ("string".equals(schemaType)) {
			if ("date-time".equals(schemaMap.format)) {
				return "java.time.OffsetDateTime";
			}
			if ("date".equals(schemaMap.format)) {
				return "java.time.LocalDate";
			}
			if ("uuid".equals(schemaMap.format)) {
				return "java.util.UUID";
			}
			if ("binary".equals(schemaMap.format)) {
				return "byte[]";
			}
			return "String";
		} else if ("integer".equals(schemaType)) {
			if ("int64".equals(schemaMap.format)) {
				return "Long";
			}
			return "Integer";
		} else if ("number".equals(schemaType)) {
			if ("float".equals(schemaMap.format)) {
				return "Float";
			}
			return "Double";
		} else if ("boolean".equals(schemaType)) {
			return "Boolean";
		} else if ("array".equals(schemaType)) {
			return "List<" + toJavaType(schemaMap.items, model, mapAsObject) + ">";
		} else if ("object".equals(schemaType) || schemaMap.additionalProperties != null) {
			if (!mapAsObject && schemaMap.additionalProperties instanceof Schema) {
				String innerType = toJavaType(schemaMap.additionalProperties, model, false);
				return "Map<String, " + innerType + ">";
			}
			return "Map<String, Object>";
		}
		return "Object";
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
		if (type.isClassOrInterfaceType()) {
			ClassOrInterfaceType ciType = type.asClassOrInterfaceType();
			String name = ciType.getNameAsString();
			if ("String".equals(name)) {
				propSchema.type = "string";
			} else if ("Integer".equals(name)) {
				propSchema.type = "integer";
			} else if ("Long".equals(name)) {
				propSchema.type = "integer";
				propSchema.format = "int64";
			} else if ("Double".equals(name) || "Float".equals(name)) {
				propSchema.type = "number";
				if ("Float".equals(name)) {
					propSchema.format = "float";
				}
			} else if ("Boolean".equals(name)) {
				propSchema.type = "boolean";
			} else if ("UUID".equals(name)) {
				propSchema.type = "string";
				propSchema.format = "uuid";
			} else if ("LocalDate".equals(name)) {
				propSchema.type = "string";
				propSchema.format = "date";
			} else if ("OffsetDateTime".equals(name) || "ZonedDateTime".equals(name)) {
				propSchema.type = "string";
				propSchema.format = "date-time";
			} else if ("List".equals(name) || "ArrayList".equals(name) || "Set".equals(name)) {
				propSchema.type = "array";
				Schema items = new Schema();
				if (ciType.getTypeArguments().isPresent() && !ciType.getTypeArguments().get().isEmpty()) {
					toSchema(ciType.getTypeArguments().get().get(0), items, fallbackPrimitiveToObject);
				} else {
					items.type = "string";
				}
				propSchema.items = items;
			} else if ("Map".equals(name) || "HashMap".equals(name)) {
				propSchema.type = "object";
				if (ciType.getTypeArguments().isPresent() && ciType.getTypeArguments().get().size() > 1) {
					Schema addProps = new Schema();
					toSchema(ciType.getTypeArguments().get().get(1), addProps, fallbackPrimitiveToObject);
					propSchema.additionalProperties = addProps;
				}
			} else {
				propSchema.$ref = "#/components/schemas/" + name;
			}
		} else if (type.isArrayType()) {
			String elemType = type.asArrayType().getComponentType().toString();
			if ("byte".equals(elemType)) {
				propSchema.type = "string";
				propSchema.format = "binary";
			} else {
				propSchema.type = "array";
				Schema items = new Schema();
				toSchema(type.asArrayType().getComponentType(), items, fallbackPrimitiveToObject);
				propSchema.items = items;
			}
		} else if (type.isPrimitiveType()) {
			String pType = type.asPrimitiveType().toString();
			if ("int".equals(pType)) {
				propSchema.type = "integer";
			} else if ("long".equals(pType)) {
				propSchema.type = "integer";
				propSchema.format = "int64";
			} else if ("double".equals(pType) || "float".equals(pType)) {
				propSchema.type = "number";
			} else if ("boolean".equals(pType)) {
				propSchema.type = "boolean";
			}
		} else if (fallbackPrimitiveToObject) {
			propSchema.type = "object";
		}
	}

	/**
	 * Performs a structural field-by-field equality check between two OpenAPI model
	 * objects of the same class.
	 *
	 * @param self
	 *            The first object.
	 * @param other
	 *            The object to compare against.
	 * @return {@code true} if both objects are of the same class and all public
	 *         fields are equal.
	 */
	public static boolean structuralEquals(Object self, Object other) {
		if (self == other) {
			return true;
		}
		if (other == null || self.getClass() != other.getClass()) {
			return false;
		}
		for (java.lang.reflect.Field field : self.getClass().getFields()) {
			Object v1 = getFieldValue(field, self);
			Object v2 = getFieldValue(field, other);
			if (!java.util.Objects.equals(v1, v2)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Computes a structural hash code from all public fields of an OpenAPI model
	 * object.
	 *
	 * @param self
	 *            The object to hash.
	 * @return The computed hash code.
	 */
	public static int structuralHashCode(Object self) {
		int result = 1;
		for (java.lang.reflect.Field field : self.getClass().getFields()) {
			Object val = getFieldValue(field, self);
			result = 31 * result + (val == null ? 0 : val.hashCode());
		}
		return result;
	}

	/**
	 * Produces a human-readable structural string representation of all non-null
	 * public fields of an OpenAPI model object.
	 *
	 * @param self
	 *            The object to format.
	 * @return The formatted string representation.
	 */
	public static String structuralToString(Object self) {
		StringBuilder sb = new StringBuilder(self.getClass().getSimpleName()).append("{");
		boolean first = true;
		for (java.lang.reflect.Field field : self.getClass().getFields()) {
			Object val = getFieldValue(field, self);
			if (val != null) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(field.getName()).append("=").append(val);
				first = false;
			}
		}
		return sb.append("}").toString();
	}

	/**
	 * Reads a public field value from an object instance.
	 *
	 * @param field
	 *            The reflection {@link java.lang.reflect.Field}.
	 * @param target
	 *            The object instance.
	 * @return The field value, or {@code null} if inaccessible.
	 */
	public static Object getFieldValue(java.lang.reflect.Field field, Object target) {
		try {
			return field.get(target);
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}
}
