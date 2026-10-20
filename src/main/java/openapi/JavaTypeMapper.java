package openapi;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Maps OpenAPI {@link Schema} definitions to and from JavaParser AST
 * {@link Type} representations.
 */
public final class JavaTypeMapper {
	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private JavaTypeMapper() {
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
		if (refOr == null) {
			return "Object";
		}
		if (refOr.isRef()) {
			return refOr.ref.ref.substring(refOr.ref.ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
		}
		return toJavaType(refOr.value, model, mapAsObject);
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
		if (schemaMap == null) {
			return "Object";
		}
		if (schemaMap.$ref != null) {
			return schemaMap.$ref.substring(schemaMap.$ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
		}
		String schemaType = schemaMap.type != null ? schemaMap.type.single : null;
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
			if (!mapAsObject && schemaMap.additionalProperties != null && schemaMap.additionalProperties.isSchema()) {
				String innerType = toJavaType(schemaMap.additionalProperties.schema, model, false);
				return "Map<String, " + innerType + ">";
			}
			return "Map<String, Object>";
		}
		return "Object";
	}

	/**
	 * Resolves an OpenAPI {@link Schema} or {@link ReferenceOr} object into its
	 * corresponding Java type string.
	 *
	 * @param schemaObj
	 *            The OpenAPI {@link Schema} or {@link ReferenceOr} object.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @param mapAsObject
	 *            Whether to map {@code object} schemas with
	 *            {@code additionalProperties} to {@code Map<String, Object>}
	 *            unconditionally (used by seeder) or recursively resolve value
	 *            types.
	 * @return The resolved Java type string.
	 */
	@SuppressWarnings("unchecked")
	public static String toJavaType(Object schemaObj, OpenAPI model, boolean mapAsObject) {
		if (schemaObj instanceof ReferenceOr) {
			return toJavaType((ReferenceOr<Schema>) schemaObj, model, mapAsObject);
		}
		if (schemaObj instanceof Schema) {
			return toJavaType((Schema) schemaObj, model, mapAsObject);
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
				propSchema.type = SchemaType.ofSingle("string");
			} else if ("Integer".equals(name)) {
				propSchema.type = SchemaType.ofSingle("integer");
			} else if ("Long".equals(name)) {
				propSchema.type = SchemaType.ofSingle("integer");
				propSchema.format = "int64";
			} else if ("Double".equals(name) || "Float".equals(name)) {
				propSchema.type = SchemaType.ofSingle("number");
				if ("Float".equals(name)) {
					propSchema.format = "float";
				}
			} else if ("Boolean".equals(name)) {
				propSchema.type = SchemaType.ofSingle("boolean");
			} else if ("UUID".equals(name)) {
				propSchema.type = SchemaType.ofSingle("string");
				propSchema.format = "uuid";
			} else if ("LocalDate".equals(name)) {
				propSchema.type = SchemaType.ofSingle("string");
				propSchema.format = "date";
			} else if ("OffsetDateTime".equals(name) || "ZonedDateTime".equals(name)) {
				propSchema.type = SchemaType.ofSingle("string");
				propSchema.format = "date-time";
			} else if ("List".equals(name) || "ArrayList".equals(name) || "Set".equals(name)) {
				propSchema.type = SchemaType.ofSingle("array");
				Schema items = new Schema();
				if (ciType.getTypeArguments().isPresent() && !ciType.getTypeArguments().get().isEmpty()) {
					toSchema(ciType.getTypeArguments().get().get(0), items, fallbackPrimitiveToObject);
				} else {
					items.type = SchemaType.ofSingle("string");
				}
				propSchema.items = ReferenceOr.ofValue(items);
			} else if ("Map".equals(name) || "HashMap".equals(name)) {
				propSchema.type = SchemaType.ofSingle("object");
				if (ciType.getTypeArguments().isPresent() && ciType.getTypeArguments().get().size() > 1) {
					Schema addProps = new Schema();
					toSchema(ciType.getTypeArguments().get().get(1), addProps, fallbackPrimitiveToObject);
					propSchema.additionalProperties = AdditionalProperties.ofSchema(addProps);
				}
			} else {
				propSchema.$ref = "#/components/schemas/" + name;
			}
		} else if (type.isArrayType()) {
			String elemType = type.asArrayType().getComponentType().toString();
			if ("byte".equals(elemType)) {
				propSchema.type = SchemaType.ofSingle("string");
				propSchema.format = "binary";
			} else {
				propSchema.type = SchemaType.ofSingle("array");
				Schema items = new Schema();
				toSchema(type.asArrayType().getComponentType(), items, fallbackPrimitiveToObject);
				propSchema.items = ReferenceOr.ofValue(items);
			}
		} else if (type.isPrimitiveType()) {
			String pType = type.asPrimitiveType().toString();
			if ("int".equals(pType)) {
				propSchema.type = SchemaType.ofSingle("integer");
			} else if ("long".equals(pType)) {
				propSchema.type = SchemaType.ofSingle("integer");
				propSchema.format = "int64";
			} else if ("double".equals(pType) || "float".equals(pType)) {
				propSchema.type = SchemaType.ofSingle("number");
			} else if ("boolean".equals(pType)) {
				propSchema.type = SchemaType.ofSingle("boolean");
			}
		} else if (fallbackPrimitiveToObject) {
			propSchema.type = SchemaType.ofSingle("object");
		}
	}
}
