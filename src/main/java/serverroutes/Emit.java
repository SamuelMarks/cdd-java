package serverroutes;

import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Operation;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;

import java.util.Map;

/**
 * Emitter for server routes.
 */
@cli.Generated
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits Modular Java code for server routes based on the OpenAPI models.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @return Map of filenames to generated Java source.
	 */
	public static Map<String, String> emitModular(OpenAPI model) {
		Map<String, String> files = new java.util.HashMap<>();
		if (model.paths == null || model.paths.pathItems == null)
			return files;

		for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
			String path = entry.getKey();
			PathItem pi = entry.getValue();

			String resourceName = getResourceName(path);
			String className = resourceName + "Routes";

			CompilationUnit cu = new CompilationUnit();
			cu.setPackageDeclaration("routes");
			cu.addImport("io.javalin.Javalin");
			cu.addImport("models.*");
			cu.addImport("mocks.*");

			ClassOrInterfaceDeclaration classDecl = cu.addClass(className).setModifier(Modifier.Keyword.PUBLIC, true);
			classDecl.setJavadocComment(resourceName + " routes.");

			MethodDeclaration md = classDecl.addMethod("register", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			md.addParameter("Javalin", "app");
			md.addParameter("DaoFactory", "daoFactory");
			md.setJavadocComment("Registers routes for " + resourceName + ".");

			StringBuilder sb = new StringBuilder();
			sb.append("{\n");

			String javalinPath = path.replaceAll("\\{([^}]+)\\}", "<$1>");

			// Link routes to DAO operations
			String operationId = null;
			if (pi.get != null)
				operationId = pi.get.operationId;
			else if (pi.post != null)
				operationId = pi.post.operationId;
			else if (pi.put != null)
				operationId = pi.put.operationId;
			else if (pi.patch != null)
				operationId = pi.patch.operationId;
			else if (pi.delete != null)
				operationId = pi.delete.operationId;

			String singularName = resourceName.endsWith("s")
					? resourceName.substring(0, resourceName.length() - 1)
					: resourceName;

			if (singularName.equals("Store")) {
				singularName = "Order";
			} else if (singularName.equals("User")) {
				singularName = "User"; // Keep user
			}

			if (pi.get != null) {
				if (path.contains("{id}")) {
					sb.append("  app.get(\"").append(javalinPath).append("\", ctx -> {\n");
					sb.append("    try {\n");
					sb.append("      Object res = daoFactory.get").append(singularName)
							.append("Dao().get(ctx.pathParam(\"id\"));\n");
					sb.append("      if (res != null) ctx.json(res); else ctx.status(404);\n");
					sb.append("    } catch (Exception e) { ctx.status(500).result(e.getMessage()); }\n");
					sb.append("  });\n");
				} else {
					sb.append("  app.get(\"").append(javalinPath).append("\", ctx -> {\n");
					sb.append("    try { ctx.json(daoFactory.get").append(singularName).append("Dao().list()); }\n");
					sb.append("    catch (Exception e) { ctx.status(500).result(e.getMessage()); }\n");
					sb.append("  });\n");
				}
			}
			if (pi.post != null) {
				sb.append("  app.post(\"").append(javalinPath).append("\", ctx -> {\n");
				sb.append("    try {\n");
				sb.append("      models.").append(singularName).append(" entity = ctx.bodyAsClass(models.")
						.append(singularName).append(".class);\n");
				sb.append("      ctx.json(daoFactory.get").append(singularName).append("Dao().create(entity));\n");
				sb.append("    } catch (Exception e) { ctx.status(500).result(e.getMessage()); }\n");
				sb.append("  });\n");
			}
			if (pi.put != null || pi.patch != null) {
				sb.append("  app.put(\"").append(javalinPath).append("\", ctx -> {\n");
				sb.append("    try {\n");
				sb.append("      models.").append(singularName).append(" entity = ctx.bodyAsClass(models.")
						.append(singularName).append(".class);\n");
				sb.append("      ctx.json(daoFactory.get").append(singularName).append("Dao().update(entity));\n");
				sb.append("    } catch (Exception e) { ctx.status(500).result(e.getMessage()); }\n");
				sb.append("  });\n");
			}
			if (pi.delete != null) {
				sb.append("  app.delete(\"").append(javalinPath).append("\", ctx -> {\n");
				sb.append("    try { daoFactory.get").append(singularName)
						.append("Dao().delete(ctx.pathParam(\"id\")); ctx.status(204); }\n");
				sb.append("    catch (Exception e) { ctx.status(500).result(e.getMessage()); }\n");
				sb.append("  });\n");
			}
			sb.append("}\n");
			md.setBody(StaticJavaParser.parseBlock(sb.toString()));

			files.put("routes/" + className + ".java", cu.toString());
		}

		return files;
	}

	private static String getResourceName(String path) {
		String[] parts = path.split("/");
		for (String p : parts) {
			if (!p.isEmpty() && !p.startsWith("{")) {
				return p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Root";
	}
}
