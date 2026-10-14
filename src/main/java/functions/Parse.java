package functions;

import java.util.HashMap;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;
import openapi.Paths;

/**
 * Parses helper functions from Java source code into an OpenAPI representation.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses public static or instance methods from Java source code into OpenAPI
	 * path operations.
	 *
	 * @param input
	 *            The Java source code input.
	 * @return The parsed {@link OpenAPI} object.
	 */
	public static OpenAPI parse(String input) {
		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		if (input == null || !input.contains("class ")) {
			return api;
		}
		CompilationUnit cu = StaticJavaParser.parse(input);
		for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
			String name = md.getNameAsString();
			PathItem item = new PathItem();
			Operation op = new Operation();
			op.operationId = name;
			item.get = op;
			api.paths.pathItems.put("/" + name, item);
		}
		return api;
	}
}
