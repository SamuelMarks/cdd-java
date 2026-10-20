package mocks;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import openapi.PathItem;
import openapi.Operation;
import openapi.Response;
import openapi.Responses;
import java.util.HashMap;
import java.util.Optional;

/**
 * Parses mocks from language source to OpenAPI representation using JavaParser.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses mock server code to an OpenAPI object.
	 *
	 * @param sourceCode
	 *            The Java source.
	 * @return The parsed OpenAPI object.
	 */
	public static OpenAPI parse(String sourceCode) {
		OpenAPI api = new OpenAPI();
		api.paths = new openapi.Paths();

		try {
			String toParse = sourceCode;
			if (!sourceCode.contains("class ")) {
				toParse = "class Dummy { void dummy() { " + sourceCode + " } }";
			}
			CompilationUnit cu = StaticJavaParser.parse(toParse);
			boolean foundMockRoutes = false;
			for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
				Optional<JavadocComment> javadoc = md.getJavadocComment();
				if (javadoc.isPresent()) {
					Javadoc parsedDoc = javadoc.get().parse();
					for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
						if ("mockRoute".equals(tag.getTagName())) {
							String[] parts = tag.getContent().toText().trim().split("\\s+", 5);
							if (parts.length >= 4) {
								foundMockRoutes = true;
								String verb = parts[0].toUpperCase();
								String path = parts[1];
								String opId = "-".equals(parts[2]) ? null : parts[2];
								String status = parts[3];
								String desc = parts.length >= 5 ? parts[4] : "Generated from mock";

								PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
								Operation op = new Operation();
								op.operationId = opId;
								op.description = desc;
								op.responses = new Responses();
								op.responses.statusCodes = new HashMap<>();
								Response resp = new Response();
								resp.description = desc;
								op.responses.statusCodes.put(status, openapi.ReferenceOr.ofValue(resp));
								assignOperation(item, verb, op);
							}
						}
					}
				}
			}

			if (!foundMockRoutes) {
				for (MethodCallExpr methodCall : cu.findAll(MethodCallExpr.class)) {
					if (methodCall.getNameAsString().equals("createContext")) {
						if (methodCall.getScope().isPresent()
								&& methodCall.getScope().get().toString().equals("server")) {
							if (methodCall.getArguments().isNonEmpty()) {
								String pathStr = methodCall.getArgument(0).toString().replace("\"", "");
								PathItem item = api.paths.pathItems.computeIfAbsent(pathStr, k -> new PathItem());
								Operation op = new Operation();
								op.description = "Generated from mock";
								if (item.get == null) {
									item.get = op;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger(Parse.class.getName())
					.fine("Skipping unparseable mock source: " + e.getMessage());
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
	private static void assignOperation(PathItem item, String verb, Operation op) {
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
