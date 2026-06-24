package servermain;

import openapi.OpenAPI;
import openapi.PathItem;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;
import java.util.Map;

/**
 * Emitter for server main.
 */
@cli.Generated
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

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  DbConfig config = DbConfig.load();\n");
		sb.append("  DaoFactory.init(config);\n");
		sb.append("  if (config.ephemeral && System.getenv(\"SEED_DB\") != null) {\n");
		sb.append("      Seeder.seedDatabase(DaoFactory.em);\n");
		sb.append("  }\n");
		sb.append(
				"  Javalin app = Javalin.create(c -> { c.plugins.enableCors(cors -> { cors.add(it -> { it.anyHost(); }); }); }).start(8080);\n");

		if (model.paths != null && model.paths.pathItems != null) {
			java.util.Set<String> registered = new java.util.HashSet<>();
			for (Map.Entry<String, openapi.PathItem> entry : model.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				String[] parts = path.split("/");
				String resourceName = "Root";
				for (String p : parts) {
					if (!p.isEmpty() && !p.startsWith("{")) {
						resourceName = p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
						break;
					}
				}
				String className = resourceName + "Routes";
				if (registered.add(className)) {
					sb.append("  ").append(className).append(".register(app, new DaoFactory());\n");
				}
			}
		}

		sb.append("}\n");
		mainMethod.setBody(StaticJavaParser.parseBlock(sb.toString()));

		files.put("servermain/Main.java", cu.toString());
		return files;
	}
}
