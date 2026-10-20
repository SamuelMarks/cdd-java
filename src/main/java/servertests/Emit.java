package servertests;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Map;
import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Schema;

/**
 * Emitter for server tests.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for server tests based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();

		// Model Tests
		if (model.components != null && model.components.schemas != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse"))
					continue;
				if (entry.getValue().value.enumValues != null)
					continue;

				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("models");
				cu.addImport("org.junit.Test");
				cu.addImport("static org.junit.Assert.*");

				ClassOrInterfaceDeclaration testClass = cu.addClass(className + "Test")
						.setModifier(Modifier.Keyword.PUBLIC, true);
				testClass.setJavadocComment("Tests for " + className + " model.");

				MethodDeclaration md = testClass.addMethod("testInstantiation", Modifier.Keyword.PUBLIC);
				md.addAnnotation("Test");
				md.setJavadocComment("Test instantiation of " + className + ".");
				BlockStmt body = new BlockStmt();
				body.addStatement(StaticJavaParser.parseStatement(className + " obj = new " + className + "();"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(obj);"));
				md.setBody(body);

				files.put("models/" + className + "Test.java", cu.toString());
			}
		}

		// Route Tests using Stub DAOs
		if (model.paths != null && model.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				PathItem pi = entry.getValue();
				String resourceName = serverroutes.Emit.getResourceName(path);
				String className = resourceName + "Routes";
				String singularName = serverroutes.Emit.resolveEntityName(resourceName, pi, model);
				boolean hasDao = serverroutes.Emit.hasEntityModel(singularName, model);

				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("routes");
				cu.addImport("org.junit.Test");
				cu.addImport("static org.junit.Assert.*");
				cu.addImport("io.javalin.Javalin");
				cu.addImport("mocks.*");
				cu.addImport("models.*");

				ClassOrInterfaceDeclaration testClass = cu.addClass(className + "Test")
						.setModifier(Modifier.Keyword.PUBLIC, true);
				testClass.setJavadocComment("Tests for " + className + ".");

				MethodDeclaration md = testClass.addMethod("testWiring", Modifier.Keyword.PUBLIC);
				md.addAnnotation("Test");
				md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
				md.setJavadocComment("Test DAO factory wiring, stub DAO CRUD, and Javalin route registration.");
				BlockStmt body = new BlockStmt();
				body.addStatement(StaticJavaParser.parseStatement("DaoFactory.useStub = true;"));
				body.addStatement(StaticJavaParser.parseStatement("DaoFactory factory = new DaoFactory();"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(factory);"));
				if (hasDao) {
					body.addStatement(
							StaticJavaParser.parseStatement("assertNotNull(factory.get" + singularName + "Dao());"));
					body.addStatement(StaticJavaParser
							.parseStatement("assertNotNull(factory.get" + singularName + "Dao().list());"));
				}
				body.addStatement(StaticJavaParser.parseStatement("Javalin app = Javalin.create();"));
				body.addStatement(StaticJavaParser.parseStatement(className + ".register(app, factory);"));
				body.addStatement(StaticJavaParser.parseStatement("assertNotNull(app);"));
				md.setBody(body);

				files.put("routes/" + className + "Test.java", cu.toString());
			}
		}

		return files;
	}
}
