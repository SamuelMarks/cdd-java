package tests;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import openapi.PathItem;
import openapi.Operation;
import java.util.Optional;

/**
 * Parses tests from language source to OpenAPI representation or vice versa.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses tests into OpenAPI.
	 *
	 * @param sourceCode
	 *            The input.
	 * @return The parsed object.
	 */
	public static OpenAPI parse(String sourceCode) {
		OpenAPI api = new OpenAPI();
		api.paths = new openapi.Paths();
		try {
			CompilationUnit cu = StaticJavaParser.parse(sourceCode);
			for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
				if (!classDecl.getNameAsString().endsWith("IntegrationTest"))
					continue;

				for (MethodDeclaration methodDecl : classDecl.getMethods()) {
					String mName = methodDecl.getNameAsString();
					String bodyStr = methodDecl.getBody().map(Object::toString).orElse("");
					java.util.regex.Matcher matcher = java.util.regex.Pattern
							.compile("\"(GET|POST|PUT|DELETE|PATCH) (/[^\\s\"]*) (/[^\\s\"]*|HTTP/1\\.1)")
							.matcher(bodyStr);
					if (matcher.find()) {
						String verb = matcher.group(1);
						String fullPath = matcher.group(2);
						String path = fullPath.replaceFirst("^/(v[0-9]+|api/v[0-9]+)", "");
						if (path.isEmpty())
							path = "/";
						PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
						Operation op = new Operation();
						op.operationId = mName.length() > 4
								? Character.toLowerCase(mName.charAt(4)) + mName.substring(5)
								: mName;
						op.description = "Generated from test case " + mName;
						assignOperation(item, verb, op);
					} else if (mName.startsWith("test_")) {
						String opId = mName.substring(5);
						String dummyPath = "/" + opId;
						PathItem item = api.paths.pathItems.computeIfAbsent(dummyPath, k -> new PathItem());
						Operation op = new Operation();
						op.operationId = opId;
						op.description = "Generated from test case " + mName;
						if (item.get == null) {
							item.get = op;
						}
					}
				}
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger(Parse.class.getName())
					.fine("Skipping unparseable test source: " + e.getMessage());
		}
		return api;
	}

	/**
	 * Assigns an {@link Operation} to the matching HTTP verb slot on a
	 * {@link PathItem}.
	 *
	 * @param item
	 *            The target {@link PathItem}.
	 * @param verb
	 *            The uppercase HTTP verb.
	 * @param op
	 *            The {@link Operation} to assign.
	 */
	public static void assignOperation(PathItem item, String verb, Operation op) {
		if ("POST".equals(verb)) {
			item.post = op;
		} else if ("PUT".equals(verb)) {
			item.put = op;
		} else if ("DELETE".equals(verb)) {
			item.delete = op;
		} else if ("PATCH".equals(verb)) {
			item.patch = op;
		} else {
			item.get = op;
		}
	}

}
