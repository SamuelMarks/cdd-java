package routes;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import openapi.PathItem;
import openapi.Operation;
import openapi.Parameter;
import openapi.Info;
import openapi.RequestBody;
import openapi.MediaType;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses routes from language source to OpenAPI representation using
 * JavaParser.
 */
public class Parse {
	/** Supported HTTP method names on HttpRequest.Builder. */
	private static final java.util.Set<String> HTTP_VERBS = new java.util.HashSet<>(
			java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "QUERY", "OPTIONS", "HEAD", "TRACE"));

	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses Java source files into an OpenAPI model.
	 *
	 * @param sourceCode
	 *            The Java source.
	 * @return The parsed OpenAPI object.
	 */
	public static OpenAPI parse(String sourceCode) {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.2.0";
		api.info = new Info();
		api.info.title = "Extracted API";
		api.info.version = "0.0.4";
		api.paths = new openapi.Paths();

		try {
			CompilationUnit cu = StaticJavaParser.parse(sourceCode);
			java.util.Map<String, openapi.Callback> parsedCallbacks = new java.util.HashMap<>();
			for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {

				if (classDecl.getNameAsString().endsWith("Client")) {
					api.info.title = classDecl.getNameAsString().replace("Client", "");
				}

				Optional<JavadocComment> cJavadoc = classDecl.getJavadocComment();
				if (cJavadoc.isPresent()) {
					parseClassLevelJavadoc(cJavadoc.get().parse(), api);
				}

				if (classDecl.isInterface() && classDecl.getNameAsString().endsWith("WebhookHandler")) {
					parseWebhookInterface(classDecl, api);
					continue;
				}

				if (classDecl.isInterface() && classDecl.getNameAsString().endsWith("CallbackHandler")) {
					parseCallbackInterface(classDecl, parsedCallbacks);
					continue;
				}

				for (MethodDeclaration methodDecl : classDecl.getMethods()) {
					if (methodDecl.getTypeAsString().contains("HttpResponse")) {
						parseRouteMethod(methodDecl, api, parsedCallbacks);
					}
				}
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger(Parse.class.getName())
					.fine("Skipping unparseable route source: " + e.getMessage());
		}

		return api;
	}

	/**
	 * Parses class-level Javadoc tags ({@code @openapiVersion},
	 * {@code @openapiSelf}, {@code @jsonSchemaDialect}, {@code @info*},
	 * {@code @contact*}, {@code @license*}, {@code @server*}, {@code @security*},
	 * {@code @globalTag}, and {@code @component*}) into the {@link OpenAPI} model.
	 *
	 * @param cDoc
	 *            The parsed class-level
	 *            {@link com.github.javaparser.javadoc.Javadoc}.
	 * @param api
	 *            The target {@link OpenAPI} model.
	 */
	public static void parseClassLevelJavadoc(com.github.javaparser.javadoc.Javadoc cDoc, OpenAPI api) {
		RouteJavadocParser.parseClassLevelJavadoc(cDoc, api);
	}

	/**
	 * Parses a {@code *WebhookHandler} interface declaration into
	 * {@link OpenAPI#webhooks}.
	 *
	 * @param classDecl
	 *            The webhook interface AST node.
	 * @param api
	 *            The target {@link OpenAPI} model.
	 */
	public static void parseWebhookInterface(ClassOrInterfaceDeclaration classDecl, OpenAPI api) {
		String hookName = classDecl.getNameAsString().replace("WebhookHandler", "");
		if (api.webhooks == null)
			api.webhooks = new HashMap<>();

		PathItem hookItem = new PathItem();

		for (MethodDeclaration hookMethod : classDecl.getMethods()) {
			if (hookMethod.getNameAsString().equals("onPost")) {
				Operation op = new Operation();
				op.operationId = hookName + "Post";
				op.parameters = new ArrayList<>();

				for (com.github.javaparser.ast.body.Parameter astParam : hookMethod.getParameters()) {
					String pName = astParam.getNameAsString();
					if (pName.equals("body") || pName.equals("requestBody")) {
						RequestBody rb = new RequestBody();
						rb.content = new HashMap<>();
						MediaType mt = new MediaType();
						openapi.Schema sch = new openapi.Schema();
						sch.type = openapi.SchemaType.ofSingle("string");
						mt.schema = openapi.ReferenceOr.ofValue(sch);
						rb.content.put("application/json", mt);
						op.requestBody = openapi.ReferenceOr.ofValue(rb);
					} else {
						Parameter p = new Parameter();
						p.name = pName;
						p.in = "query"; // Default fallback for webhook extra params
						openapi.Schema sch = new openapi.Schema();
						sch.type = openapi.SchemaType.ofSingle("string");
						p.schema = openapi.ReferenceOr.ofValue(sch);
						op.parameters.add(openapi.ReferenceOr.ofValue(p));
					}
				}

				if (op.parameters.isEmpty())
					op.parameters = null;
				hookItem.post = op;
			}
		}
		api.webhooks.put(hookName, hookItem);
	}

	/**
	 * Parses a {@code *CallbackHandler} interface declaration into a callback map.
	 *
	 * @param classDecl
	 *            The callback handler interface AST node.
	 * @param callbacks
	 *            The target map of callback name to {@link openapi.Callback}.
	 */
	public static void parseCallbackInterface(ClassOrInterfaceDeclaration classDecl,
			java.util.Map<String, openapi.Callback> callbacks) {
		String cbName = classDecl.getNameAsString().replace("CallbackHandler", "");
		if (callbacks == null) {
			return;
		}
		openapi.Callback cb = callbacks.computeIfAbsent(cbName, k -> new openapi.Callback());
		for (MethodDeclaration cbMethod : classDecl.getMethods()) {
			String mName = cbMethod.getNameAsString();
			if (mName.startsWith("on")) {
				String verb = mName.substring(2).toLowerCase();
				PathItem pi = cb.pathItems.computeIfAbsent("{$request.body#/callbackUrl}", k -> new PathItem());
				Operation op = new Operation();
				op.description = "Callback operation";
				RouteJavadocParser.assignCallbackVerb(pi, verb, op);
			}
		}
	}

	/**
	 * Parses an HTTP client route method declaration and its Javadoc tags into
	 * {@link OpenAPI#paths}.
	 *
	 * @param methodDecl
	 *            The route method AST node.
	 * @param api
	 *            The target {@link OpenAPI} model.
	 * @param callbacks
	 *            Map of parsed callback handler interfaces.
	 */
	public static void parseRouteMethod(MethodDeclaration methodDecl, OpenAPI api,
			java.util.Map<String, openapi.Callback> callbacks) {
		String opId = methodDecl.getNameAsString();
		Operation op = new Operation();
		op.operationId = opId;

		Optional<JavadocComment> javadoc = methodDecl.getJavadocComment();
		RouteJavadocParser.MethodJavadocMetadata meta = new RouteJavadocParser.MethodJavadocMetadata();

		if (javadoc.isPresent()) {
			RouteJavadocParser.parseMethodJavadoc(javadoc.get().parse(), op, meta);
		}

		String[] verbAndPath = new String[2];
		List<String> headersUsed = parseMethodBodyHttpCall(methodDecl.getBody().get(), op, verbAndPath);
		String method = verbAndPath[0];
		String path = verbAndPath[1];

		if (method != null && path != null) {
			PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
			op.parameters = new ArrayList<>();

			boolean isSecurity = false;
			for (com.github.javaparser.ast.body.Parameter astParam : methodDecl.getParameters()) {
				String pName = astParam.getNameAsString();

				if (pName.equals("body") || pName.equals("requestBody")) {
					if (op.requestBody == null)
						op.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
					openapi.RequestBody rb = op.requestBody.value;
					if (rb.content == null) {
						rb.content = new java.util.HashMap<>();
						openapi.MediaType mt = new openapi.MediaType();
						openapi.Schema sch = new openapi.Schema();
						sch.type = openapi.SchemaType.ofSingle("string");
						mt.schema = openapi.ReferenceOr.ofValue(sch);
						rb.content.put("application/json", mt);
					}
					continue;
				}

				openapi.Parameter p = new openapi.Parameter();
				p.name = pName;
				openapi.Schema pSch = new openapi.Schema();
				pSch.type = openapi.SchemaType.ofSingle("string");
				p.schema = openapi.ReferenceOr.ofValue(pSch);

				if (meta.paramDescriptions.containsKey(pName)) {
					p.description = meta.paramDescriptions.get(pName);
				}
				if (meta.requiredParams.contains(pName)) {
					p.required = true;
				}
				if (meta.deprecatedParams.contains(pName)) {
					p.deprecated = true;
				}
				if (meta.allowEmptyValueParams.contains(pName)) {
					p.allowEmptyValue = true;
				}
				if (meta.paramStyles.containsKey(pName)) {
					p.style = meta.paramStyles.get(pName);
				}
				if (meta.explodeParams.contains(pName)) {
					p.explode = true;
				}
				if (meta.allowReservedParams.contains(pName)) {
					p.allowReserved = true;
				}
				if (meta.paramSchemas.containsKey(pName)) {
					pSch.type = openapi.SchemaType.ofSingle(meta.paramSchemas.get(pName));
				}
				if (meta.paramContents.containsKey(pName)) {
					p.content = new java.util.HashMap<>();
					for (String mediaTypeStr : meta.paramContents.get(pName)) {
						openapi.MediaType mt = new openapi.MediaType();
						openapi.Schema mtSch = new openapi.Schema();
						mtSch.type = openapi.SchemaType.ofSingle("string");
						mt.schema = openapi.ReferenceOr.ofValue(mtSch);
						p.content.put(mediaTypeStr, mt);
					}
				}
				if (meta.paramExamples.containsKey(pName)) {
					p.example = common.PayloadValue.ofString(meta.paramExamples.get(pName));
				}
				if (meta.paramExamplesMap.containsKey(pName)) {
					p.examples = meta.paramExamplesMap.get(pName);
				}

				if (path.contains("{" + pName + "}")) {
					p.in = "path";
					p.required = true;
				} else if (headersUsed.contains(pName) || pName.equalsIgnoreCase("authorization")) {
					if (pName.equalsIgnoreCase("authorization")) {
						p.in = "header";
						p.name = "Authorization";
						isSecurity = true;
					} else {
						p.in = "header";
					}
				} else {
					p.in = "query";
				}

				if (!isSecurity) {
					op.parameters.add(openapi.ReferenceOr.ofValue(p));
				}
			}

			if (isSecurity) {
				RouteJavadocParser.ensureComponents(api);
				if (!api.components.securitySchemes.containsKey("BearerAuth")) {
					openapi.SecurityScheme sc = new openapi.SecurityScheme();
					sc.type = "http";
					sc.scheme = "bearer";
					api.components.securitySchemes.put("BearerAuth", openapi.ReferenceOr.ofValue(sc));
				}

				if (op.security == null) {
					op.security = new java.util.ArrayList<>();
					openapi.SecurityRequirement req = new openapi.SecurityRequirement();
					req.requirements = new java.util.HashMap<>();
					req.requirements.put("BearerAuth", new java.util.ArrayList<>());
					op.security.add(req);
				}
			}

			if (op.parameters.isEmpty())
				op.parameters = null;

			if (op.responses == null) {
				op.responses = new openapi.Responses();
				op.responses.statusCodes = new java.util.HashMap<>();
			}
			if (op.responses.statusCodes.isEmpty() && op.responses.defaultResponse == null) {
				openapi.Response r200 = new openapi.Response();
				r200.description = "Successful response";
				op.responses.statusCodes.put("200", openapi.ReferenceOr.ofValue(r200));
			}

			item.setOperation(openapi.HttpVerb.fromString(method), op);
		}
	}

	/**
	 * Extracts a normalized OpenAPI path template from an expression containing
	 * {@code baseUrl + ...}, or returns {@code null} if the expression does not
	 * reference {@code baseUrl}.
	 *
	 * @param exprStr
	 *            The expression string to inspect.
	 * @return The extracted path template, or {@code null}.
	 */
	private static String extractPathFromBaseUrlExpr(String exprStr) {
		Matcher m = Pattern.compile("baseUrl\\s*\\+\\s*(.*)").matcher(exprStr);
		if (!m.find()) {
			return null;
		}
		String path = m.group(1);
		path = path.replaceAll("\"\\s*\\+\\s*([a-zA-Z0-9_]+)\\s*\\+\\s*\"", "{$1}");
		path = path.replaceAll("\"\\s*\\+\\s*([a-zA-Z0-9_]+)", "{$1}");
		path = path.replaceAll("([a-zA-Z0-9_]+)\\s*\\+\\s*\"", "{$1}");
		path = path.replaceAll("\"", "");
		while (path.endsWith(")")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.contains("?")) {
			path = path.substring(0, path.indexOf("?"));
		}
		return path;
	}

	/**
	 * Extracts the HTTP verb, URI path template, and referenced header variable
	 * names from a route method's AST body.
	 *
	 * @param bodyStmt
	 *            The method body {@link com.github.javaparser.ast.stmt.BlockStmt}.
	 * @param op
	 *            The target {@link Operation} being populated.
	 * @param verbAndPath
	 *            A two-element array where index {@code 0} receives the HTTP method
	 *            and index {@code 1} receives the URI path template.
	 * @return The list of parameter names passed into {@code .header(...)} calls.
	 */
	public static List<String> parseMethodBodyHttpCall(com.github.javaparser.ast.stmt.BlockStmt bodyStmt, Operation op,
			String[] verbAndPath) {
		String method = null;
		String path = null;

		for (com.github.javaparser.ast.expr.ObjectCreationExpr objCreate : bodyStmt
				.findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)) {
			if (objCreate.getTypeAsString().equals("StringBuilder")) {
				String extracted = extractPathFromBaseUrlExpr(objCreate.toString());
				if (extracted != null) {
					path = extracted;
				}
			}
		}
		List<String> headersUsed = new ArrayList<>();

		for (MethodCallExpr methodCall : bodyStmt.findAll(MethodCallExpr.class)) {
			String callName = methodCall.getNameAsString();
			if (HTTP_VERBS.contains(callName)) {
				method = callName;
			} else if (callName.equals("method")) {
				method = methodCall.getArgument(0).toString().replace("\"", "");
			} else if (callName.equals("uri")) {
				String extracted = extractPathFromBaseUrlExpr(methodCall.toString());
				if (extracted != null) {
					path = extracted;
				}
			} else if (callName.equals("header")) {
				headersUsed.add(methodCall.getArgument(1).toString());
			}
		}
		verbAndPath[0] = method;
		verbAndPath[1] = path;
		return headersUsed;
	}
}
