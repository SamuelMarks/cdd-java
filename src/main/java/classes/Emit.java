package classes;

import openapi.OpenAPI;
import openapi.Schema;
import openapi.ExternalDocumentation;
import openapi.XML;
import openapi.Discriminator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.util.Map;
import java.util.List;

/**
 * Emits DTOs to language source while preserving whitespace and comments.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for schemas.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();
		if (model.components == null || model.components.schemas == null)
			return files;

		for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse")) {
				continue;
			}
			CompilationUnit cu = new CompilationUnit();
			cu.setPackageDeclaration("models");
			cu.addImport("com.fasterxml.jackson.annotation.JsonInclude");
			cu.addImport("com.fasterxml.jackson.annotation.JsonProperty");
			cu.addImport("com.fasterxml.jackson.annotation.JsonTypeInfo");
			cu.addImport("com.fasterxml.jackson.annotation.JsonSubTypes");
			cu.addImport("com.fasterxml.jackson.annotation.JsonValue");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");

			emitClass(cu, className, entry.getValue().value, model);

			// For enums, set Modifier to PUBLIC
			cu.getEnumByName(className).ifPresent(e -> e.setModifier(Modifier.Keyword.PUBLIC, true));
			// For classes, set Modifier to PUBLIC
			cu.getClassByName(className).ifPresent(c -> c.setModifier(Modifier.Keyword.PUBLIC, true));

			files.put("models/" + className + ".java", cu.toString());
		}
		return files;
	}

	/**
	 * Emits Java code for schemas.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = common.AstMergeUtils.parseLexicalPreserving(existingSource);
		} else {
			cu = new CompilationUnit();
			isNew = true;
			cu.addImport("com.fasterxml.jackson.annotation.JsonInclude");
			cu.addImport("com.fasterxml.jackson.annotation.JsonProperty");
			cu.addImport("com.fasterxml.jackson.annotation.JsonTypeInfo");
			cu.addImport("com.fasterxml.jackson.annotation.JsonSubTypes");
			cu.addImport("com.fasterxml.jackson.annotation.JsonValue");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
			cu.addImport("java.net.http.HttpClient");
			cu.addImport("java.net.http.HttpRequest");
			cu.addImport("java.net.http.HttpResponse");
			cu.addImport("java.net.URI");
		}
		routes.Emit.emitSdkClientClass(cu, model);
		emitSchemasInto(cu, model);
		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Emits all DTO schema and enum definitions from
	 * {@code model.components.schemas} into the target {@link CompilationUnit}.
	 *
	 * @param cu
	 *            The target {@link CompilationUnit}.
	 * @param model
	 *            The OpenAPI model containing component schemas.
	 */
	public static void emitSchemasInto(CompilationUnit cu, OpenAPI model) {
		if (model.components != null && model.components.schemas != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse")) {
					continue;
				}
				emitClass(cu, className, entry.getValue().value, model);
			}
		}
	}

	/**
	 * Emits a DTO class or enum declaration into the compilation unit for the given
	 * OpenAPI schema.
	 *
	 * @param cu
	 *            The target {@link CompilationUnit}.
	 * @param className
	 *            The sanitized Java class or enum name.
	 * @param schemaMap
	 *            The OpenAPI {@link Schema} definition.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void emitClass(CompilationUnit cu, String className, Schema schemaMap, OpenAPI model) {
		if (schemaMap.enumValues != null) {
			EnumDeclaration enumDecl = cu.getEnumByName(className).orElse(null);
			if (enumDecl == null) {
				enumDecl = cu.addEnum(className);
				enumDecl.setModifier(Modifier.Keyword.PUBLIC, false);
			} else {
				enumDecl.getEntries().clear();
			}
			List<common.PayloadValue> enumValues = schemaMap.enumValues;
			for (common.PayloadValue val : enumValues) {
				String valStr = String.valueOf(val);
				String safeName = valStr.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
				if (safeName.isEmpty() || Character.isDigit(safeName.charAt(0)))
					safeName = "_" + safeName;
				enumDecl.addEnumConstant(safeName);
			}
			return;
		}
		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
		if (classDecl == null) {
			classDecl = cu.addClass(className);
			classDecl.setModifier(Modifier.Keyword.PUBLIC, false);
		}
		StringBuilder classDoc = new StringBuilder();
		if (schemaMap.description != null && !schemaMap.description.isEmpty()) {
			classDoc.append(schemaMap.description);
		}
		if (schemaMap.xml != null) {
			XML xmlMap = schemaMap.xml;
			if (xmlMap.name != null)
				classDoc.append("\n@xmlName ").append(xmlMap.name);
			if (xmlMap.namespace != null)
				classDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
			if (xmlMap.prefix != null)
				classDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
			if (xmlMap.attribute != null)
				classDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
			if (xmlMap.wrapped != null)
				classDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
		}
		if (schemaMap.externalDocs != null) {
			ExternalDocumentation extDocsMap = schemaMap.externalDocs;
			if (extDocsMap.url != null) {
				classDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
				if (extDocsMap.description != null) {
					classDoc.append(" ").append(extDocsMap.description);
				}
			}
		}
		if (schemaMap.example != null) {
			classDoc.append("\n@schemaExample ").append(schemaMap.example);
		}
		if (schemaMap.discriminator != null) {
			Discriminator discMap = schemaMap.discriminator;
			if (discMap.propertyName != null) {
				classDoc.append("\n@discriminatorProperty ").append(discMap.propertyName);
			}
			if (discMap.mapping != null) {
				for (Map.Entry<String, String> entry : discMap.mapping.entrySet()) {
					classDoc.append("\n@discriminatorMapping ").append(entry.getKey()).append(" ")
							.append(entry.getValue());
				}
			}
			if (discMap.extensions != null && discMap.extensions.containsKey("defaultMapping")) {
				classDoc.append("\n@discriminatorDefault ").append(discMap.extensions.get("defaultMapping"));
			}
		}
		if (classDoc.length() > 0) {
			classDecl.setJavadocComment(classDoc.toString().trim());
		}
		Map<String, openapi.ReferenceOr<Schema>> properties = schemaMap.properties;
		if (properties != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> prop : properties.entrySet()) {
				String propName = prop.getKey();
				String safePropName = propName.replaceAll("[^a-zA-Z0-9_]", "_");
				if (Character.isDigit(safePropName.charAt(0)))
					safePropName = "_" + safePropName;
				if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName)
						|| "class".equals(safePropName)) {
					safePropName += "Value";
				}
				String type = resolveType(prop.getValue(), model);
				if (!classDecl.getFieldByName(safePropName).isPresent()) {
					FieldDeclaration fd = classDecl.addField(type, safePropName, Modifier.Keyword.PUBLIC);
					fd.addAnnotation(StaticJavaParser.parseAnnotation("@JsonProperty(\"" + propName + "\")"));
					Schema propMap = prop.getValue() != null ? prop.getValue().value : null;
					if (propMap != null) {
						StringBuilder fieldDoc = new StringBuilder();
						if (propMap.description != null && !propMap.description.isEmpty()) {
							fieldDoc.append(propMap.description);
						}
						if (propMap.externalDocs != null) {
							ExternalDocumentation extDocsMap = propMap.externalDocs;
							if (extDocsMap.url != null) {
								fieldDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
								if (extDocsMap.description != null) {
									fieldDoc.append(" ").append(extDocsMap.description);
								}
							}
						}
						if (propMap.example != null) {
							fieldDoc.append("\n@schemaExample ").append(propMap.example);
						}
						if (propMap.xml != null) {
							XML xmlMap = propMap.xml;
							if (xmlMap.name != null)
								fieldDoc.append("\n@xmlName ").append(xmlMap.name);
							if (xmlMap.namespace != null)
								fieldDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
							if (xmlMap.prefix != null)
								fieldDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
							if (xmlMap.attribute != null)
								fieldDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
							if (xmlMap.wrapped != null)
								fieldDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
						}
						if (fieldDoc.length() > 0) {
							fd.setJavadocComment(fieldDoc.toString().trim());
						}
					}
				}
			}
		}
	}

	/**
	 * Resolves an OpenAPI schema object into its corresponding Java type
	 * representation.
	 *
	 * @param schemaObj
	 *            The OpenAPI {@link Schema} or reference object.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The resolved Java type string.
	 */
	private static String resolveType(openapi.ReferenceOr<Schema> schemaObj, OpenAPI model) {
		return openapi.TypeMapper.toJavaType(schemaObj, model, false);
	}
}
