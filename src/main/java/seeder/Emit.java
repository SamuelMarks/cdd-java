package seeder;

import openapi.OpenAPI;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Emits the Fake Data Seeder.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for Database Seeder based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();
		if (model.components == null || model.components.schemas == null)
			return files;

		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("seeder");
		cu.addImport("java.util.List");
		cu.addImport("java.util.ArrayList");
		cu.addImport("java.util.Map");
		cu.addImport("java.util.HashMap");
		cu.addImport("net.datafaker.Faker");
		cu.addImport("jakarta.persistence.EntityManager");
		cu.addImport("models.*");

		ClassOrInterfaceDeclaration seederClass = cu.addClass("Seeder").setModifier(Modifier.Keyword.PUBLIC, true);
		seederClass.setJavadocComment(
				"Seeder module for populating the database with fake data.\nReferential integrity is managed via an Entity Pool.");
		seederClass.addMember(StaticJavaParser.parseBodyDeclaration("private static Faker faker = new Faker();"));
		seederClass.addMember(StaticJavaParser
				.parseBodyDeclaration("private static Map<String, List<Object>> entityPool = new HashMap<>();"));

		MethodDeclaration seedMethod = seederClass.addMethod("seedDatabase", Modifier.Keyword.PUBLIC,
				Modifier.Keyword.STATIC);
		seedMethod.addParameter("EntityManager", "em");
		seedMethod.setJavadocComment("Seeds the database using concrete DAO connection.\n@param em The EntityManager.");
		seedMethod.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));

		StringBuilder body = new StringBuilder();
		body.append("{\n");
		for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse"))
				continue;
			if (entry.getValue().value.enumValues != null)
				continue;

			body.append("  entityPool.put(\"").append(className).append("\", new ArrayList<>());\n");
			body.append("  for (int i = 0; i < 10; i++) {\n");
			body.append("      ").append(className).append(" entity = generate").append(className).append("();\n");
			body.append("      em.getTransaction().begin();\n");
			body.append("      em.persist(entity);\n");
			body.append("      em.getTransaction().commit();\n");
			body.append("      entityPool.get(\"").append(className).append("\").add(entity);\n");
			body.append("  }\n");

			emitGeneratorMethod(seederClass, className, entry.getValue().value, model);
		}
		body.append("}\n");
		seedMethod.setBody(StaticJavaParser.parseBlock(body.toString()));
		files.put("seeder/Seeder.java", cu.toString());
		return files;
	}

	/**
	 * Emits Java code for Database Seeder based on the OpenAPI models.
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
			cu.addImport("java.util.List");
			cu.addImport("java.util.ArrayList");
			cu.addImport("java.util.Map");
			cu.addImport("java.util.HashMap");
			cu.addImport("net.datafaker.Faker");
			cu.addImport("jakarta.persistence.EntityManager");
		}

		ClassOrInterfaceDeclaration seederClass = cu.getClassByName("Seeder").orElse(null);
		if (seederClass == null) {
			seederClass = cu.addClass("Seeder");
			seederClass.setJavadocComment(
					"Seeder module for populating the database with fake data.\nReferential integrity is managed via an Entity Pool.");

			seederClass.addMember(StaticJavaParser.parseBodyDeclaration("private static Faker faker = new Faker();"));
			seederClass.addMember(StaticJavaParser
					.parseBodyDeclaration("private static Map<String, List<Object>> entityPool = new HashMap<>();"));

			MethodDeclaration seedMethod = seederClass.addMethod("seedDatabase", Modifier.Keyword.PUBLIC,
					Modifier.Keyword.STATIC);
			seedMethod.addParameter("EntityManager", "em");
			seedMethod.setJavadocComment(
					"Seeds the database using concrete DAO connection.\n@param em The EntityManager.");
			seedMethod.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));

			StringBuilder body = new StringBuilder();
			body.append("{\n");

			for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse"))
					continue;
				if (entry.getValue().value.enumValues != null)
					continue;

				body.append("  entityPool.put(\"").append(className).append("\", new ArrayList<>());\n");
				body.append("  for (int i = 0; i < 10; i++) {\n");
				body.append("      ").append(className).append(" entity = generate").append(className).append("();\n");
				body.append("      em.getTransaction().begin();\n");
				body.append("      em.persist(entity);\n");
				body.append("      em.getTransaction().commit();\n");
				body.append("      entityPool.get(\"").append(className).append("\").add(entity);\n");
				body.append("  }\n");

				emitGeneratorMethod(seederClass, className, entry.getValue().value, model);
			}
			body.append("}\n");
			seedMethod.setBody(StaticJavaParser.parseBlock(body.toString()));
		}

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Emits a static fake-data factory method for an entity class into the seeder
	 * class.
	 *
	 * @param seederClass
	 *            The target {@link ClassOrInterfaceDeclaration} for {@code Seeder}.
	 * @param className
	 *            The sanitized entity class name.
	 * @param schemaMap
	 *            The OpenAPI {@link Schema} definition for the entity.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void emitGeneratorMethod(ClassOrInterfaceDeclaration seederClass, String className, Schema schemaMap,
			OpenAPI model) {
		MethodDeclaration genMethod = seederClass.addMethod("generate" + className, Modifier.Keyword.PRIVATE,
				Modifier.Keyword.STATIC);
		genMethod.setType(className);
		genMethod.setJavadocComment("Mapping factory for " + className + ".\n@return Generated " + className + ".");

		StringBuilder b = new StringBuilder();
		b.append("{\n");
		b.append("  ").append(className).append(" obj = new ").append(className).append("();\n");

		if (schemaMap.properties != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> prop : schemaMap.properties.entrySet()) {
				String propName = prop.getKey();
				String safePropName = propName.replaceAll("[^a-zA-Z0-9_]", "_");
				if (Character.isDigit(safePropName.charAt(0)))
					safePropName = "_" + safePropName;
				if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName)
						|| "class".equals(safePropName)) {
					safePropName += "Value";
				}

				if (propName.equalsIgnoreCase("id"))
					continue; // Auto-generated ID

				String type = resolveType(prop.getValue(), model);
				Schema propSchema = prop.getValue().value;
				if (propSchema != null && propSchema.enumValues != null) {
					String firstVal = String.valueOf(propSchema.enumValues.get(0)).replace("\\", "\\\\").replace("\"",
							"\\\"");
					b.append("  obj.").append(safePropName).append(" = \"").append(firstVal).append("\";\n");
				} else if (type.equals("String")) {
					if (propName.toLowerCase().contains("email")) {
						b.append("  obj.").append(safePropName).append(" = faker.internet().emailAddress();\n");
					} else if (propName.toLowerCase().contains("name")) {
						b.append("  obj.").append(safePropName).append(" = faker.name().fullName();\n");
					} else if (propName.toLowerCase().contains("phone")) {
						b.append("  obj.").append(safePropName).append(" = faker.phoneNumber().phoneNumber();\n");
					} else {
						b.append("  obj.").append(safePropName).append(" = faker.lorem().word();\n");
					}
				} else if (type.equals("Integer")) {
					b.append("  obj.").append(safePropName).append(" = faker.number().randomDigit();\n");
				} else if (type.equals("Long")) {
					b.append("  obj.").append(safePropName).append(" = faker.number().randomNumber();\n");
				} else if (type.equals("Boolean")) {
					b.append("  obj.").append(safePropName).append(" = faker.bool().bool();\n");
				} else if (type.equals("Double")) {
					b.append("  obj.").append(safePropName).append(" = faker.number().randomDouble(2, 0, 1000);\n");
				} else if (type.equals("Float")) {
					b.append("  obj.").append(safePropName)
							.append(" = (float) faker.number().randomDouble(2, 0, 1000);\n");
				} else if (type.equals("java.time.OffsetDateTime")) {
					b.append("  obj.").append(safePropName).append(" = java.time.OffsetDateTime.now();\n");
				} else if (type.equals("java.time.LocalDate")) {
					b.append("  obj.").append(safePropName).append(" = java.time.LocalDate.now();\n");
				} else if (type.equals("java.util.UUID")) {
					b.append("  obj.").append(safePropName).append(" = java.util.UUID.randomUUID();\n");
				} else if (type.equals("byte[]")) {
					b.append("  obj.").append(safePropName).append(" = faker.lorem().word().getBytes();\n");
				} else if (type.startsWith("List<")) {
					b.append("  obj.").append(safePropName).append(" = new ArrayList<>();\n");
				} else if (type.startsWith("Map<")) {
					b.append("  obj.").append(safePropName).append(" = new HashMap<>();\n");
				} else if (!type.equals("Object")) {
					// Foreign Key Reference
					b.append("  if (entityPool.containsKey(\"").append(type).append("\") && !entityPool.get(\"")
							.append(type).append("\").isEmpty()) {\n");
					b.append("      obj.").append(safePropName).append(" = (").append(type)
							.append(") entityPool.get(\"").append(type)
							.append("\").get(faker.random().nextInt(entityPool.get(\"").append(type)
							.append("\").size()));\n");
					b.append("  }\n");
				}
			}
		}
		b.append("  return obj;\n");
		b.append("}\n");
		genMethod.setBody(StaticJavaParser.parseBlock(b.toString()));
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
		return openapi.TypeMapper.toJavaType(schemaObj, model, true);
	}
}
