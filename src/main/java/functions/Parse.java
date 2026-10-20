package functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import openapi.MediaType;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.Parameter;
import openapi.PathItem;
import openapi.Paths;
import openapi.RequestBody;
import openapi.Schema;

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
		try {
			CompilationUnit cu = StaticJavaParser.parse(input);
			for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
				String name = md.getNameAsString();
				String verb = "GET";
				String path = "/" + name;
				Operation op = new Operation();
				op.operationId = name;

				Optional<JavadocComment> javadoc = md.getJavadocComment();
				if (javadoc.isPresent()) {
					Javadoc parsedDoc = javadoc.get().parse();
					String cleanDesc = parsedDoc.getDescription().toText().trim();
					if (!cleanDesc.isEmpty()) {
						op.description = cleanDesc;
					}
					for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
						String tagName = tag.getTagName();
						String content = tag.getContent().toText().trim();
						if ("httpMethod".equals(tagName) && !content.isEmpty()) {
							verb = content.toUpperCase();
						} else if ("httpPath".equals(tagName) && !content.isEmpty()) {
							path = content;
						} else if ("httpParam".equals(tagName)) {
							String[] parts = content.split("\\s+", 2);
							if (parts.length == 2) {
								if (op.parameters == null) {
									op.parameters = new ArrayList<>();
								}
								Parameter p = new Parameter();
								p.in = parts[0];
								p.name = parts[1];
								Schema s = new Schema();
								s.type = openapi.SchemaType.ofSingle("string");
								p.schema = openapi.ReferenceOr.ofValue(s);
								op.parameters.add(openapi.ReferenceOr.ofValue(p));
							}
						} else if ("httpRequestBody".equals(tagName) && "true".equalsIgnoreCase(content)) {
							RequestBody rb = new RequestBody();
							rb.content = new HashMap<>();
							MediaType mt = new MediaType();
							Schema s = new Schema();
							s.type = openapi.SchemaType.ofSingle("object");
							mt.schema = openapi.ReferenceOr.ofValue(s);
							rb.content.put("application/json", mt);
							op.requestBody = openapi.ReferenceOr.ofValue(rb);
						}
					}
				}

				PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
				assignOperation(item, verb, op);
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger(Parse.class.getName())
					.fine("Skipping unparseable function source: " + e.getMessage());
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
		item.setOperation(openapi.HttpVerb.fromStringOrDefault(verb, openapi.HttpVerb.GET), op);
	}
}
