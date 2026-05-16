package orm;

import openapi.OpenAPI;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.Map;

/**
 * Emits ORM entities using Hibernate/JPA annotations.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Java code for JPA entities.
	 * 
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		if (model.components == null || model.components.schemas == null) {
			return existingSource != null ? existingSource : "";
		}

		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = StaticJavaParser.parse(existingSource);
			LexicalPreservingPrinter.setup(cu);
		} else {
			cu = new CompilationUnit();
			isNew = true;
			cu.addImport("jakarta.persistence.Entity");
			cu.addImport("jakarta.persistence.Table");
			cu.addImport("jakarta.persistence.Column");
			cu.addImport("jakarta.persistence.Id");
			cu.addImport("jakarta.persistence.GeneratedValue");
			cu.addImport("jakarta.persistence.GenerationType");
			cu.addImport("jakarta.persistence.OneToMany");
			cu.addImport("jakarta.persistence.ManyToOne");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
		}

		for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse")) {
				continue;
			}

			Schema schemaMap = entry.getValue();
			if (schemaMap.enumValues == null) {
				emitEntity(cu, className, schemaMap, model);
			}
		}

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Generated JavaDoc.
	 * 
	 * @param cu
	 *            param doc
	 * @param className
	 *            param doc
	 * @param schemaMap
	 *            param doc
	 * @param model
	 *            param doc
	 */
	private static void emitEntity(CompilationUnit cu, String className, Schema schemaMap, OpenAPI model) {
		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
		if (classDecl == null) {
			classDecl = cu.addClass(className);
		}

		if (!classDecl.getAnnotationByName("Entity").isPresent()) {
			classDecl.addAnnotation("Entity");
		}
		if (!classDecl.getAnnotationByName("Table").isPresent()) {
			NormalAnnotationExpr tableAnn = new NormalAnnotationExpr();
			tableAnn.setName("Table");
			tableAnn.addPair("name", "\"" + className.toLowerCase() + "s\"");
			classDecl.addAnnotation(tableAnn);
		}

		Map<String, Object> properties = schemaMap.properties;
		if (properties != null) {
			for (Map.Entry<String, Object> prop : properties.entrySet()) {
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

					if (propName.equalsIgnoreCase("id")) {
						fd.addAnnotation("Id");
						NormalAnnotationExpr genAnn = new NormalAnnotationExpr();
						genAnn.setName("GeneratedValue");
						genAnn.addPair("strategy", new NameExpr("GenerationType.IDENTITY"));
						fd.addAnnotation(genAnn);
					} else if (type.startsWith("List<")) {
						fd.addAnnotation("OneToMany");
					} else {
						NormalAnnotationExpr colAnn = new NormalAnnotationExpr();
						colAnn.setName("Column");
						colAnn.addPair("name", "\"" + propName + "\"");
						fd.addAnnotation(colAnn);
					}
				}
			}
		}
	}

	/**
	 * Generated JavaDoc.
	 * 
	 * @param schemaObj
	 *            param doc
	 * @param model
	 *            param doc
	 * @return return doc
	 */
	private static String resolveType(Object schemaObj, OpenAPI model) {
		if (!(schemaObj instanceof Schema))
			return "Object";
		Schema schemaMap = (Schema) schemaObj;
		if (schemaMap.$ref != null) {
			return schemaMap.$ref.substring(schemaMap.$ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
		}
		String schemaType = (String) schemaMap.type;
		if ("string".equals(schemaType)) {
			if ("date-time".equals(schemaMap.format))
				return "java.time.OffsetDateTime";
			if ("date".equals(schemaMap.format))
				return "java.time.LocalDate";
			if ("uuid".equals(schemaMap.format))
				return "java.util.UUID";
			if ("binary".equals(schemaMap.format))
				return "byte[]";
			return "String";
		} else if ("integer".equals(schemaType)) {
			if ("int64".equals(schemaMap.format))
				return "Long";
			return "Integer";
		} else if ("number".equals(schemaType)) {
			if ("float".equals(schemaMap.format))
				return "Float";
			return "Double";
		} else if ("boolean".equals(schemaType)) {
			return "Boolean";
		} else if ("array".equals(schemaType)) {
			String innerType = resolveType(schemaMap.items, model);
			return "List<" + innerType + ">";
		} else if ("object".equals(schemaType) || schemaMap.additionalProperties != null) {
			if (schemaMap.additionalProperties instanceof Schema) {
				String innerType = resolveType(schemaMap.additionalProperties, model);
				return "Map<String, " + innerType + ">";
			}
			return "Map<String, Object>";
		}
		return "Object";
	}
}
