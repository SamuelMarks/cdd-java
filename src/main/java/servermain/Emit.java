package servermain;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Map;
import openapi.OpenAPI;

/**
 * Emitter for server main.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for the server main class based on the OpenAPI
	 * models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();

		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("servermain");
		cu.addImport("io.javalin.Javalin");
		cu.addImport("mocks.*");
		cu.addImport("routes.*");
		cu.addImport("seeder.*");

		ClassOrInterfaceDeclaration mainClass = cu.addClass("Main").setModifier(Modifier.Keyword.PUBLIC, true);
		mainClass.setJavadocComment("Modular Mock Server entrypoint.");

		MethodDeclaration mainMethod = mainClass.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		mainMethod.addParameter("String[]", "args");
		mainMethod.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));
		mainMethod.setJavadocComment("Main method.");

		BlockStmt body = new BlockStmt();
		body.addStatement(StaticJavaParser.parseStatement("DbConfig config = DbConfig.load();"));
		body.addStatement(StaticJavaParser.parseStatement("DaoFactory.init(config);"));
		body.addStatement(StaticJavaParser.parseStatement(
				"if (config.ephemeral && System.getenv(\"SEED_DB\") != null) { Seeder.seedDatabase(DaoFactory.em); }"));
		body.addStatement(StaticJavaParser.parseStatement(
				"Javalin app = Javalin.create(c -> { c.plugins.enableCors(cors -> { cors.add(it -> { it.anyHost(); }); }); }).start(8080);"));

		if (model.paths != null && model.paths.pathItems != null) {
			java.util.Set<String> registered = new java.util.HashSet<>();
			for (Map.Entry<String, openapi.PathItem> entry : model.paths.pathItems.entrySet()) {
				String resourceName = serverroutes.Emit.getResourceName(entry.getKey());
				String className = resourceName + "Routes";
				if (registered.add(className)) {
					body.addStatement(StaticJavaParser.parseStatement(className + ".register(app, new DaoFactory());"));
				}
			}
		}

		mainMethod.setBody(body);

		files.put("servermain/Main.java", cu.toString());
		return files;
	}
}
