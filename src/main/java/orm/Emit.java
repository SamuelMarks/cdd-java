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
	 * Emits Modular Java code for JPA entities.
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
			Schema schemaMap = entry.getValue().value;
			if (schemaMap.enumValues == null) {
				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("models");
				cu.addImport("jakarta.persistence.Entity");
				cu.addImport("jakarta.persistence.Table");
				cu.addImport("jakarta.persistence.Column");
				cu.addImport("jakarta.persistence.Id");
				cu.addImport("jakarta.persistence.GeneratedValue");
				cu.addImport("jakarta.persistence.GenerationType");
				cu.addImport("jakarta.persistence.OneToMany");
				cu.addImport("jakarta.persistence.ManyToOne");
				cu.addImport("jakarta.persistence.JoinColumn");
				cu.addImport("java.util.List");
				cu.addImport("java.util.Map");

				emitEntity(cu, className, schemaMap, model);

				cu.getClassByName(className).ifPresent(c -> c.setModifier(Modifier.Keyword.PUBLIC, true));
				files.put("models/" + className + ".java", cu.toString());
			}
		}
		return files;
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
			cu = common.AstMergeUtils.parseLexicalPreserving(existingSource);
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
			cu.addImport("jakarta.persistence.JoinColumn");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
		}

		for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse")) {
				continue;
			}

			Schema schemaMap = entry.getValue().value;
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
	 * Emits a Jakarta Persistence {@code @Entity} class declaration for the given
	 * OpenAPI schema.
	 *
	 * @param cu
	 *            The target {@link CompilationUnit}.
	 * @param className
	 *            The sanitized entity class name.
	 * @param schemaMap
	 *            The OpenAPI {@link Schema} definition.
	 * @param model
	 *            The root {@link OpenAPI} model.
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

				openapi.ReferenceOr<Schema> propVal = prop.getValue();
				String type = resolveType(propVal, model);
				boolean isRef = propVal != null && propVal.resolveRefString() != null;
				boolean isRequired = schemaMap.required != null && schemaMap.required.contains(propName);

				if (!common.AstMergeUtils.hasField(classDecl, safePropName)) {
					FieldDeclaration fd = classDecl.addField(type, safePropName, Modifier.Keyword.PUBLIC);

					if (propName.equalsIgnoreCase("id")) {
						fd.addAnnotation("Id");
						NormalAnnotationExpr genAnn = new NormalAnnotationExpr();
						genAnn.setName("GeneratedValue");
						genAnn.addPair("strategy", new NameExpr("GenerationType.IDENTITY"));
						fd.addAnnotation(genAnn);
					} else if (type.startsWith("List<")) {
						fd.addAnnotation("OneToMany");
					} else if (isRef) {
						fd.addAnnotation("ManyToOne");
						NormalAnnotationExpr joinAnn = new NormalAnnotationExpr();
						joinAnn.setName("JoinColumn");
						joinAnn.addPair("name", "\"" + propName + "_id\"");
						if (isRequired) {
							joinAnn.addPair("nullable", "false");
						}
						fd.addAnnotation(joinAnn);
					} else {
						NormalAnnotationExpr colAnn = new NormalAnnotationExpr();
						colAnn.setName("Column");
						colAnn.addPair("name", "\"" + propName + "\"");
						if (isRequired) {
							colAnn.addPair("nullable", "false");
						}
						fd.addAnnotation(colAnn);
					}
				}
			}
		}
	}

	/**
	 * Resolves an OpenAPI schema object into its corresponding Java entity field
	 * type.
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
