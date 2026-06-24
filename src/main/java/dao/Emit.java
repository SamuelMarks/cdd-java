package dao;

import openapi.OpenAPI;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;

/**
 * Emits DAO interfaces and implementations for the CDD Server architecture.
 */
@cli.Generated
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for DAOs based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();
		if (model.components == null || model.components.schemas == null)
			return files;

		// Base Dao interface
		CompilationUnit genericDaoCu = new CompilationUnit();
		genericDaoCu.setPackageDeclaration("mocks");
		genericDaoCu.addImport("java.util.List");
		ClassOrInterfaceDeclaration genericDao = genericDaoCu.addInterface("Dao").setModifier(Modifier.Keyword.PUBLIC,
				true);
		genericDao.addTypeParameter("T");
		genericDao.setJavadocComment("Generic Data Access Object interface.\n@param <T> The entity type.");
		genericDao.addMethod("create", Modifier.Keyword.PUBLIC).setBody(null).setType("T").addParameter("T", "entity")
				.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
				.setJavadocComment("Creates an entity.");
		genericDao.addMethod("get", Modifier.Keyword.PUBLIC).setBody(null).setType("T").addParameter("Object", "id")
				.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
				.setJavadocComment("Retrieves an entity.");
		genericDao.addMethod("update", Modifier.Keyword.PUBLIC).setBody(null).setType("T").addParameter("T", "entity")
				.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
				.setJavadocComment("Updates an entity.");
		genericDao.addMethod("delete", Modifier.Keyword.PUBLIC).setBody(null).setType("void")
				.addParameter("Object", "id")
				.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
				.setJavadocComment("Deletes an entity.");
		genericDao.addMethod("list", Modifier.Keyword.PUBLIC).setBody(null).setType("List<T>")
				.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
				.setJavadocComment("Lists all entities.");
		files.put("mocks/Dao.java", genericDaoCu.toString());

		for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse"))
				continue;
			Schema schemaMap = entry.getValue();
			if (schemaMap.enumValues == null) {
				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("mocks");
				cu.addImport("models." + className);
				cu.addImport("java.util.List");
				cu.addImport("jakarta.persistence.EntityManager");

				emitEntityDaos(cu, className, schemaMap);
				cu.findAll(ClassOrInterfaceDeclaration.class)
						.forEach(c -> c.setModifier(Modifier.Keyword.PUBLIC, true));

				files.put("mocks/" + className + "Daos.java", cu.toString());
			}
		}

		CompilationUnit cfgCu = new CompilationUnit();
		cfgCu.setPackageDeclaration("mocks");
		emitDbConfig(cfgCu);
		cfgCu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> c.setModifier(Modifier.Keyword.PUBLIC, true));
		files.put("mocks/DbConfig.java", cfgCu.toString());

		CompilationUnit factCu = new CompilationUnit();
		factCu.setPackageDeclaration("mocks");
		factCu.addImport("java.util.Map");
		factCu.addImport("java.util.HashMap");
		factCu.addImport("jakarta.persistence.EntityManager");
		factCu.addImport("jakarta.persistence.EntityManagerFactory");
		factCu.addImport("jakarta.persistence.Persistence");
		emitDaoFactory(factCu, model);
		factCu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> c.setModifier(Modifier.Keyword.PUBLIC, true));
		files.put("mocks/DaoFactory.java", factCu.toString());

		return files;
	}

	/**
	 * Emits Java code for DAOs based on the OpenAPI models.
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
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
			cu.addImport("java.util.HashMap");
			cu.addImport("java.util.ArrayList");
			cu.addImport("jakarta.persistence.EntityManager");
			cu.addImport("jakarta.persistence.EntityManagerFactory");
			cu.addImport("jakarta.persistence.Persistence");
		}

		// 1. Generate Dao<T> interface
		ClassOrInterfaceDeclaration genericDao = cu.getClassByName("Dao").orElse(null);
		if (genericDao == null) {
			genericDao = cu.addInterface("Dao");
			genericDao.removeModifier(Modifier.Keyword.PUBLIC);
			genericDao.addTypeParameter("T");
			genericDao.setJavadocComment("Generic Data Access Object interface.\n@param <T> The entity type.");

			genericDao.addMethod("create", Modifier.Keyword.PUBLIC).setBody(null).setType("T")
					.addParameter("T", "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setJavadocComment(
							"Creates an entity.\n@param entity The entity to create.\n@return The created entity.\n@throws Exception on error");

			genericDao.addMethod("get", Modifier.Keyword.PUBLIC).setBody(null).setType("T").addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setJavadocComment(
							"Retrieves an entity by ID.\n@param id The entity ID.\n@return The entity, or null if not found.\n@throws Exception on error");

			genericDao.addMethod("update", Modifier.Keyword.PUBLIC).setBody(null).setType("T")
					.addParameter("T", "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setJavadocComment(
							"Updates an entity.\n@param entity The entity to update.\n@return The updated entity.\n@throws Exception on error");

			genericDao.addMethod("delete", Modifier.Keyword.PUBLIC).setBody(null).setType("void")
					.addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setJavadocComment(
							"Deletes an entity by ID.\n@param id The entity ID.\n@throws Exception on error");

			genericDao.addMethod("list", Modifier.Keyword.PUBLIC).setBody(null).setType("List<T>")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setJavadocComment("Lists all entities.\n@return A list of entities.\n@throws Exception on error");
		}

		for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
			String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (className.equals("Emit") || className.equals("Parse")) {
				continue;
			}
			Schema schemaMap = entry.getValue();
			if (schemaMap.enumValues == null) {
				emitEntityDaos(cu, className, schemaMap);
			}
		}

		emitDbConfig(cu);
		emitDaoFactory(cu, model);

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	/**
	 * Emits specific DAOs for an entity.
	 *
	 * @param cu
	 *            The CompilationUnit.
	 * @param className
	 *            The entity class name.
	 * @param schemaMap
	 *            The entity schema.
	 */
	private static void emitEntityDaos(CompilationUnit cu, String className, Schema schemaMap) {
		ClassOrInterfaceDeclaration wrapper = cu.getClassByName(className + "Daos").orElse(null);
		if (wrapper == null) {
			wrapper = cu.addClass(className + "Daos");
			wrapper.setModifier(Modifier.Keyword.PUBLIC, true);
			wrapper.setJavadocComment("Wrapper class for " + className + " DAOs.");
		}

		String daoInterfaceName = className + "Dao";
		ClassOrInterfaceDeclaration daoInterface = null;
		for (com.github.javaparser.ast.body.BodyDeclaration<?> m : wrapper.getMembers()) {
			if (m instanceof ClassOrInterfaceDeclaration
					&& ((ClassOrInterfaceDeclaration) m).getNameAsString().equals(daoInterfaceName)) {
				daoInterface = (ClassOrInterfaceDeclaration) m;
				break;
			}
		}
		if (daoInterface == null) {
			daoInterface = new ClassOrInterfaceDeclaration().setName(daoInterfaceName).setInterface(true);
			wrapper.addMember(daoInterface);
			daoInterface.setModifier(Modifier.Keyword.PUBLIC, true);
			daoInterface.addExtendedType("Dao<" + className + ">");
			daoInterface.setJavadocComment("DAO interface for " + className + ".");
		}

		String stubDaoName = "Stub" + className + "Dao";
		ClassOrInterfaceDeclaration stubDao = null;
		for (com.github.javaparser.ast.body.BodyDeclaration<?> m : wrapper.getMembers()) {
			if (m instanceof ClassOrInterfaceDeclaration
					&& ((ClassOrInterfaceDeclaration) m).getNameAsString().equals(stubDaoName)) {
				stubDao = (ClassOrInterfaceDeclaration) m;
				break;
			}
		}
		if (stubDao == null) {
			stubDao = new ClassOrInterfaceDeclaration().setName(stubDaoName).setInterface(false);
			wrapper.addMember(stubDao);
			stubDao.setModifier(Modifier.Keyword.PUBLIC, true);
			stubDao.setModifier(Modifier.Keyword.STATIC, true);
			stubDao.addImplementedType(daoInterfaceName);
			stubDao.setJavadocComment("Stub DAO implementation for " + className + ".");

			stubDao.addMethod("create", Modifier.Keyword.PUBLIC).setType(className).addParameter(className, "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser
							.parseBlock("{ throw new UnsupportedOperationException(\"NotImplementedError\"); }"));
			stubDao.addMethod("get", Modifier.Keyword.PUBLIC).setType(className).addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser
							.parseBlock("{ throw new UnsupportedOperationException(\"NotImplementedError\"); }"));
			stubDao.addMethod("update", Modifier.Keyword.PUBLIC).setType(className).addParameter(className, "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser
							.parseBlock("{ throw new UnsupportedOperationException(\"NotImplementedError\"); }"));
			stubDao.addMethod("delete", Modifier.Keyword.PUBLIC).setType("void").addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser
							.parseBlock("{ throw new UnsupportedOperationException(\"NotImplementedError\"); }"));
			stubDao.addMethod("list", Modifier.Keyword.PUBLIC).setType("List<" + className + ">")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser
							.parseBlock("{ throw new UnsupportedOperationException(\"NotImplementedError\"); }"));
		}

		String concreteDaoName = "Concrete" + className + "Dao";
		ClassOrInterfaceDeclaration concreteDao = null;
		for (com.github.javaparser.ast.body.BodyDeclaration<?> m : wrapper.getMembers()) {
			if (m instanceof ClassOrInterfaceDeclaration
					&& ((ClassOrInterfaceDeclaration) m).getNameAsString().equals(concreteDaoName)) {
				concreteDao = (ClassOrInterfaceDeclaration) m;
				break;
			}
		}
		if (concreteDao == null) {
			concreteDao = new ClassOrInterfaceDeclaration().setName(concreteDaoName).setInterface(false);
			wrapper.addMember(concreteDao);
			concreteDao.setModifier(Modifier.Keyword.PUBLIC, true);
			concreteDao.setModifier(Modifier.Keyword.STATIC, true);
			concreteDao.addImplementedType(daoInterfaceName);
			concreteDao.setJavadocComment("Concrete JPA DAO implementation for " + className + ".");

			concreteDao.addField("EntityManager", "em", Modifier.Keyword.PRIVATE);
			concreteDao.addConstructor(Modifier.Keyword.PUBLIC).addParameter("EntityManager", "em")
					.setBody(StaticJavaParser.parseBlock("{ this.em = em; }"));

			concreteDao.addMethod("create", Modifier.Keyword.PUBLIC).setType(className)
					.addParameter(className, "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser.parseBlock(
							"{ em.getTransaction().begin(); em.persist(entity); em.getTransaction().commit(); return entity; }"));
			concreteDao.addMethod("get", Modifier.Keyword.PUBLIC).setType(className).addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser.parseBlock("{ return em.find(" + className + ".class, id); }"));
			concreteDao.addMethod("update", Modifier.Keyword.PUBLIC).setType(className)
					.addParameter(className, "entity")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser.parseBlock("{ em.getTransaction().begin(); " + className
							+ " merged = em.merge(entity); em.getTransaction().commit(); return merged; }"));
			concreteDao.addMethod("delete", Modifier.Keyword.PUBLIC).setType("void").addParameter("Object", "id")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser.parseBlock("{ em.getTransaction().begin(); " + className
							+ " entity = em.find(" + className
							+ ".class, id); if (entity != null) { em.remove(entity); } em.getTransaction().commit(); }"));
			concreteDao.addMethod("list", Modifier.Keyword.PUBLIC).setType("List<" + className + ">")
					.addThrownException(StaticJavaParser.parseClassOrInterfaceType("exceptions.ServerException"))
					.setBody(StaticJavaParser.parseBlock("{ return em.createQuery(\"SELECT e FROM \" + \"" + className
							+ "\" + \" e\", " + className + ".class).getResultList(); }"));
		}
	}

	/**
	 * Emits the DbConfig class.
	 *
	 * @param cu
	 *            The CompilationUnit.
	 */
	private static void emitDbConfig(CompilationUnit cu) {
		ClassOrInterfaceDeclaration dbConfig = cu.getClassByName("DbConfig").orElse(null);
		if (dbConfig == null) {
			dbConfig = cu.addClass("DbConfig");
			dbConfig.removeModifier(Modifier.Keyword.PUBLIC);
			dbConfig.setJavadocComment("Database Connection configuration class.");
			dbConfig.addField("boolean", "ephemeral", Modifier.Keyword.PUBLIC)
					.setJavadocComment("Whether to use an ephemeral DB.");
			dbConfig.addField("String", "databaseUrl", Modifier.Keyword.PUBLIC).setJavadocComment("The database URL.");

			MethodDeclaration loadMethod = dbConfig.addMethod("load", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			loadMethod.setType("DbConfig");
			loadMethod.setJavadocComment(
					"Loads configuration from environment variables.\n@return The loaded DbConfig instance.");
			loadMethod.setBody(StaticJavaParser.parseBlock("{\n" + "  DbConfig config = new DbConfig();\n"
					+ "  config.databaseUrl = System.getenv(\"DATABASE_URL\");\n"
					+ "  String ephemeralEnv = System.getenv(\"EPHEMERAL_DB\");\n"
					+ "  config.ephemeral = \"true\".equalsIgnoreCase(ephemeralEnv) || \"1\".equals(ephemeralEnv);\n"
					+ "  return config;\n" + "}"));
		}
	}

	/**
	 * Emits the DaoFactory class.
	 *
	 * @param cu
	 *            The CompilationUnit.
	 * @param model
	 *            The OpenAPI model.
	 */
	private static void emitDaoFactory(CompilationUnit cu, OpenAPI model) {
		ClassOrInterfaceDeclaration factory = cu.getClassByName("DaoFactory").orElse(null);
		if (factory == null) {
			factory = cu.addClass("DaoFactory");
			factory.setModifier(Modifier.Keyword.PUBLIC, true);
			factory.setJavadocComment("Dependency Injection Factory for DAOs.");

			factory.addField("EntityManagerFactory", "emf", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			factory.addField("EntityManager", "em", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			factory.addField("boolean", "useStub", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);

			MethodDeclaration initMethod = factory.addMethod("init", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			initMethod.addParameter("DbConfig", "config");
			initMethod.setJavadocComment(
					"Initializes the DAO Factory based on environment configuration.\n@param config The database configuration.");
			initMethod.setBody(StaticJavaParser.parseBlock("{\n"
					+ "  if (config.databaseUrl == null && !config.ephemeral) {\n" + "      useStub = true;\n"
					+ "  } else {\n" + "      useStub = false;\n"
					+ "      Map<String, String> properties = new HashMap<>();\n" + "      if (config.ephemeral) {\n"
					+ "          properties.put(\"jakarta.persistence.jdbc.url\", \"jdbc:sqlite::memory:\");\n"
					+ "          properties.put(\"jakarta.persistence.jdbc.driver\", \"org.sqlite.JDBC\");\n"
					+ "          properties.put(\"hibernate.hbm2ddl.auto\", \"create-drop\");\n" + "      } else {\n"
					+ "          properties.put(\"jakarta.persistence.jdbc.url\", config.databaseUrl);\n"
					+ "          properties.put(\"hibernate.hbm2ddl.auto\", \"update\");\n" + "      }\n"
					+ "      emf = Persistence.createEntityManagerFactory(\"default\", properties);\n"
					+ "      em = emf.createEntityManager();\n" + "  }\n" + "}"));

			for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse"))
					continue;
				if (entry.getValue().enumValues != null)
					continue;

				String methodName = "get" + className + "Dao";
				factory.addMethod(methodName, Modifier.Keyword.PUBLIC).setType(className + "Daos." + className + "Dao")
						.setJavadocComment("Gets the DAO for " + className + ".\n@return The " + className + " DAO.")
						.setBody(StaticJavaParser.parseBlock("{ if (useStub) return new " + className + "Daos.Stub"
								+ className + "Dao(); return new " + className + "Daos.Concrete" + className
								+ "Dao(em); }"));
			}
		}
	}
}
