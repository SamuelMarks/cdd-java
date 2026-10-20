package servertests;

import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;
import java.util.Map;

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
				md.setBody(StaticJavaParser
						.parseBlock("{ " + className + " obj = new " + className + "(); assertNotNull(obj); }"));

				files.put("models/" + className + "Test.java", cu.toString());
			}
		}

		// Route Tests using Stub DAOs
		if (model.paths != null && model.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				String resourceName = "Root";
				String[] parts = path.split("/");
				for (String p : parts) {
					if (!p.isEmpty() && !p.startsWith("{")) {
						resourceName = p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
						break;
					}
				}
				String className = resourceName + "Routes";

				CompilationUnit cu = new CompilationUnit();
				cu.setPackageDeclaration("routes");
				cu.addImport("org.junit.Test");
				cu.addImport("static org.junit.Assert.*");
				cu.addImport("io.javalin.Javalin");
				cu.addImport("mocks.*");

				ClassOrInterfaceDeclaration testClass = cu.addClass(className + "Test")
						.setModifier(Modifier.Keyword.PUBLIC, true);
				testClass.setJavadocComment("Tests for " + className + ".");

				MethodDeclaration md = testClass.addMethod("testWiring", Modifier.Keyword.PUBLIC);
				md.addAnnotation("Test");
				md.setJavadocComment("Test DAO factory wiring and Javalin route registration.");
				md.setBody(StaticJavaParser.parseBlock("{ DaoFactory factory = new DaoFactory(); "
						+ "assertNotNull(factory); " + "Javalin app = Javalin.create(); " + className
						+ ".register(app, factory); " + "assertNotNull(app); }"));

				files.put("routes/" + className + "Test.java", cu.toString());
			}
		}

		return files;
	}
}
