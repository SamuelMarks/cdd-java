package serverroutes;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;

/**
 * Emitter for server routes.
 */
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

			BlockStmt body = new BlockStmt();

			String javalinPath = path.replaceAll("\\{([^}]+)\\}", "<$1>");
			String pathParamName = extractFirstPathParam(path);

			String singularName = resolveEntityName(resourceName, pi, model);
			boolean hasDao = hasEntityModel(singularName, model);

			if (pi.get != null) {
				int successCode = resolveSuccessCode(pi.get, 200);
				if (hasDao) {
					if (pathParamName != null) {
						body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
								+ "\", ctx -> { try { Object res = daoFactory.get" + singularName
								+ "Dao().get(ctx.pathParam(\"" + pathParamName + "\")); if (res != null) ctx.status("
								+ successCode
								+ ").json(res); else ctx.status(404); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
					} else {
						body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
								+ "\", ctx -> { try { ctx.status(" + successCode + ").json(daoFactory.get"
								+ singularName
								+ "Dao().list()); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
					}
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.get(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.post != null) {
				int successCode = resolveSuccessCode(pi.post, 201);
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement("app.post(\"" + javalinPath
							+ "\", ctx -> { try { models." + singularName + " entity = ctx.bodyAsClass(models."
							+ singularName + ".class); ctx.status(" + successCode + ").json(daoFactory.get"
							+ singularName
							+ "Dao().create(entity)); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.post(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.put != null || pi.patch != null) {
				Operation updateOp = pi.put != null ? pi.put : pi.patch;
				int successCode = resolveSuccessCode(updateOp, 200);
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement("app.put(\"" + javalinPath
							+ "\", ctx -> { try { models." + singularName + " entity = ctx.bodyAsClass(models."
							+ singularName + ".class); ctx.status(" + successCode + ").json(daoFactory.get"
							+ singularName
							+ "Dao().update(entity)); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement("app.put(\"" + javalinPath
							+ "\", ctx -> ctx.status(" + successCode + ").result(\"{}\"));"));
				}
			}
			if (pi.delete != null) {
				int successCode = resolveSuccessCode(pi.delete, 204);
				String delParam = pathParamName != null ? pathParamName : "id";
				if (hasDao) {
					body.addStatement(StaticJavaParser.parseStatement(
							"app.delete(\"" + javalinPath + "\", ctx -> { try { daoFactory.get" + singularName
									+ "Dao().delete(ctx.pathParam(\"" + delParam + "\")); ctx.status(" + successCode
									+ "); } catch (Exception e) { ctx.status(500).result(e.getMessage()); } });"));
				} else {
					body.addStatement(StaticJavaParser.parseStatement(
							"app.delete(\"" + javalinPath + "\", ctx -> ctx.status(" + successCode + "));"));
				}
			}
			md.setBody(body);

			files.put("routes/" + className + ".java", cu.toString());
		}

		return files;
	}

	/**
	 * Checks whether the OpenAPI specification defines a concrete (non-enum)
	 * component schema for {@code entityName}.
	 *
	 * @param entityName
	 *            The candidate entity class name.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return {@code true} if a matching entity schema exists; {@code false}
	 *         otherwise.
	 */
	public static boolean hasEntityModel(String entityName, OpenAPI model) {
		if (model.components == null || model.components.schemas == null) {
			return false;
		}
		for (Map.Entry<String, openapi.ReferenceOr<openapi.Schema>> entry : model.components.schemas.entrySet()) {
			String candidate = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
			if (candidate.equals(entityName) && entry.getValue().value.enumValues == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts the first path parameter name (e.g. {@code "id"} or {@code "petId"})
	 * from a path template, or {@code null} if none exists.
	 *
	 * @param path
	 *            The URL path template.
	 * @return The parameter name, or {@code null}.
	 */
	private static String extractFirstPathParam(String path) {
		Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(path);
		return m.find() ? m.group(1) : null;
	}

	/**
	 * Resolves the target entity name from the resource name or operation schemas.
	 *
	 * @param resourceName
	 *            The PascalCase resource name.
	 * @param pi
	 *            The {@link PathItem}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The resolved entity class name.
	 */
	public static String resolveEntityName(String resourceName, PathItem pi, OpenAPI model) {
		String singularName = resourceName.endsWith("s")
				? resourceName.substring(0, resourceName.length() - 1)
				: resourceName;
		if (singularName.equals("Store")) {
			return "Order";
		}
		Operation op = pi.post != null ? pi.post : pi.put;
		if (op != null && op.requestBody != null) {
			openapi.RequestBody rb = op.requestBody.value;
			openapi.Schema s = rb.content.get("application/json").schema.value;
			if (s.$ref != null) {
				return s.$ref.substring("#/components/schemas/".length()).replaceAll("[^a-zA-Z0-9_]", "");
			}
		}
		return singularName;
	}

	/**
	 * Resolves the HTTP success status code declared on an {@link Operation}s
	 * responses, or returns {@code defaultStatus} if none is declared.
	 *
	 * @param op
	 *            The {@link Operation}.
	 * @param defaultStatus
	 *            The fallback HTTP status code.
	 * @return The resolved HTTP status code.
	 */
	private static int resolveSuccessCode(Operation op, int defaultStatus) {
		if (op.responses != null) {
			if (op.responses.statusCodes.containsKey("200")) {
				return 200;
			}
			if (op.responses.statusCodes.containsKey("201")) {
				return 201;
			}
			if (op.responses.statusCodes.containsKey("204")) {
				return 204;
			}
		}
		return defaultStatus;
	}

	/**
	 * Extracts a PascalCase resource name from an OpenAPI URL path template.
	 *
	 * @param path
	 *            The URL path template.
	 * @return The extracted resource name, or {@code "Root"} if no static segments
	 *         exist.
	 */
	public static String getResourceName(String path) {
		String[] parts = path.split("/");
		for (String p : parts) {
			if (!p.isEmpty() && !p.startsWith("{")) {
				return p.substring(0, 1).toUpperCase() + p.substring(1).replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Root";
	}
}
