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
		api.info.version = "0.0.3";
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
			// Ignore unparseable code
		}

		return api;
	}

	/**
	 * Ensures {@code api.components} and all its component maps are initialized.
	 *
	 * @param api
	 *            The target {@link OpenAPI} model.
	 */
	private static void ensureComponents(OpenAPI api) {
		if (api.components == null)
			api.components = new openapi.Components();
		if (api.components.securitySchemes == null)
			api.components.securitySchemes = new java.util.HashMap<>();
		if (api.components.responses == null)
			api.components.responses = new java.util.HashMap<>();
		if (api.components.parameters == null)
			api.components.parameters = new java.util.HashMap<>();
		if (api.components.requestBodies == null)
			api.components.requestBodies = new java.util.HashMap<>();
		if (api.components.headers == null)
			api.components.headers = new java.util.HashMap<>();
		if (api.components.links == null)
			api.components.links = new java.util.HashMap<>();
		if (api.components.callbacks == null)
			api.components.callbacks = new java.util.HashMap<>();
		if (api.components.pathItems == null)
			api.components.pathItems = new java.util.HashMap<>();
		if (api.components.mediaTypes == null)
			api.components.mediaTypes = new java.util.HashMap<>();
	}

	/**
	 * Returns (and initializes if needed) the {@link openapi.OAuthFlow} for the
	 * given flow type on {@link openapi.OAuthFlows}.
	 *
	 * @param flows
	 *            The {@link openapi.OAuthFlows} container.
	 * @param flowType
	 *            The OAuth flow type name.
	 * @return The matching {@link openapi.OAuthFlow} or {@code null}.
	 */
	private static openapi.OAuthFlow ensureOAuthFlow(openapi.OAuthFlows flows, String flowType) {
		switch (flowType) {
			case "implicit" :
				if (flows.implicit == null)
					flows.implicit = new openapi.OAuthFlow();
				return flows.implicit;
			case "password" :
				if (flows.password == null)
					flows.password = new openapi.OAuthFlow();
				return flows.password;
			case "clientCredentials" :
				if (flows.clientCredentials == null)
					flows.clientCredentials = new openapi.OAuthFlow();
				return flows.clientCredentials;
			case "authorizationCode" :
				if (flows.authorizationCode == null)
					flows.authorizationCode = new openapi.OAuthFlow();
				return flows.authorizationCode;
			case "deviceAuthorization" :
				if (flows.deviceAuthorization == null)
					flows.deviceAuthorization = new openapi.OAuthFlow();
				return flows.deviceAuthorization;
			default :
				return null;
		}
	}

	/**
	 * Ensures {@code api.info.contact} is initialized and returns it.
	 *
	 * @param api
	 *            The target {@link OpenAPI} model.
	 * @return The initialized {@link openapi.Contact}.
	 */
	private static openapi.Contact ensureContact(OpenAPI api) {
		if (api.info.contact == null)
			api.info.contact = new openapi.Contact();
		return api.info.contact;
	}

	/**
	 * Ensures {@code api.info.license} is initialized and returns it.
	 *
	 * @param api
	 *            The target {@link OpenAPI} model.
	 * @return The initialized {@link openapi.License}.
	 */
	private static openapi.License ensureLicense(OpenAPI api) {
		if (api.info.license == null)
			api.info.license = new openapi.License();
		return api.info.license;
	}

	/**
	 * Finds a named server in {@code api.servers} and ensures its variable entry
	 * for {@code varName} is initialized.
	 *
	 * @param api
	 *            The target {@link OpenAPI} model.
	 * @param serverName
	 *            The server name to match.
	 * @param varName
	 *            The server variable name.
	 * @return The matching {@link openapi.ServerVariable}, or {@code null} if no
	 *         matching server is found.
	 */
	private static openapi.ServerVariable findOrCreateServerVariable(OpenAPI api, String serverName, String varName) {
		if (api.servers != null) {
			for (openapi.Server s : api.servers) {
				if (serverName.equals(s.name)) {
					if (s.variables == null)
						s.variables = new java.util.HashMap<>();
					return s.variables.computeIfAbsent(varName, k -> new openapi.ServerVariable());
				}
			}
		}
		return null;
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
	@SuppressWarnings("unchecked")
	public static void parseClassLevelJavadoc(com.github.javaparser.javadoc.Javadoc cDoc, OpenAPI api) {
		ensureComponents(api);
		for (com.github.javaparser.javadoc.JavadocBlockTag tag : cDoc.getBlockTags()) {
			String tagName = tag.getTagName();
			String tagContent = tag.getContent().toText().trim();
			if (tagName.equals("openapiVersion"))
				api.openapi = tagContent;
			else if (tagName.equals("openapiSelf"))
				api.$self = tagContent;
			else if (tagName.equals("jsonSchemaDialect"))
				api.jsonSchemaDialect = tagContent;
			else if (tagName.equals("title"))
				api.info.title = tagContent;
			else if (tagName.equals("version"))
				api.info.version = tagContent;
			else if (tagName.equals("summary"))
				api.info.summary = tagContent;
			else if (tagName.equals("description"))
				api.info.description = tagContent;
			else if (tagName.equals("termsOfService"))
				api.info.termsOfService = tagContent;
			else if (tagName.equals("contactName")) {
				ensureContact(api).name = tagContent;
			} else if (tagName.equals("contactEmail")) {
				ensureContact(api).email = tagContent;
			} else if (tagName.equals("contactUrl")) {
				ensureContact(api).url = tagContent;
			} else if (tagName.equals("licenseName")) {
				ensureLicense(api).name = tagContent;
			} else if (tagName.equals("licenseIdentifier")) {
				ensureLicense(api).identifier = tagContent;
			} else if (tagName.equals("licenseUrl")) {
				ensureLicense(api).url = tagContent;
			} else if (tagName.equals("globalTag")) {
				if (api.tags == null)
					api.tags = new java.util.ArrayList<>();
				openapi.Tag tagModel = new openapi.Tag();
				String[] parts = tagContent.split("\\s+");
				tagModel.name = parts[0];
				for (int i = 1; i < parts.length; i++) {
					String part = parts[i];
					if (part.startsWith("summary="))
						tagModel.summary = part.substring(8).replace("_", " ");
					else if (part.startsWith("description="))
						tagModel.description = part.substring(12).replace("_", " ");
					else if (part.startsWith("externalDocsUrl=")) {
						tagModel.externalDocs = new openapi.ExternalDocumentation();
						tagModel.externalDocs.url = part.substring(16);
					} else if (part.startsWith("parent="))
						tagModel.parent = part.substring(7);
					else if (part.startsWith("kind="))
						tagModel.kind = part.substring(5);
				}
				api.tags.add(tagModel);
			} else if (tagName.equals("server")) {
				if (api.servers == null)
					api.servers = new java.util.ArrayList<>();
				openapi.Server s = new openapi.Server();
				String[] parts = tagContent.split("\\s+", 2);
				s.url = parts[0];
				if (parts.length > 1)
					s.description = parts[1];
				api.servers.add(s);
			} else if (tagName.equals("serverName")) {
				if (api.servers != null) {
					api.servers.get(api.servers.size() - 1).name = tagContent;
				}
			} else if (tagName.equals("serverVariable")) {
				String[] parts = tagContent.split("\\s+", 4);
				if (parts.length >= 3) {
					openapi.ServerVariable sv = findOrCreateServerVariable(api, parts[0], parts[1]);
					if (sv != null) {
						sv.defaultValue = parts[2];
						if (parts.length > 3)
							sv.description = parts[3];
					}
				}
			} else if (tagName.equals("serverVariableEnum")) {
				String[] parts = tagContent.split("\\s+", 3);
				if (parts.length >= 3) {
					openapi.ServerVariable sv = findOrCreateServerVariable(api, parts[0], parts[1]);
					if (sv != null) {
						sv.enumValues = new java.util.ArrayList<>(java.util.Arrays.asList(parts[2].split(",")));
					}
				}
			} else if (tagName.equals("securityScheme")) {
				String[] parts = tagContent.split("\\s+");
				String sName = parts[0];
				openapi.SecurityScheme sc = new openapi.SecurityScheme();
				for (int i = 1; i < parts.length; i++) {
					String part = parts[i];
					if (part.startsWith("type=")) {
						String val = part.substring(5);
						if (!val.equals("-"))
							sc.type = val;
					} else if (part.startsWith("scheme=")) {
						String val = part.substring(7);
						if (!val.equals("-"))
							sc.scheme = val;
					} else if (part.startsWith("in=")) {
						String val = part.substring(3);
						if (!val.equals("-"))
							sc.in = val;
					} else if (part.startsWith("name=")) {
						String val = part.substring(5);
						if (!val.equals("-"))
							sc.name = val;
					} else if (part.startsWith("bearerFormat=")) {
						String val = part.substring(13);
						if (!val.equals("-"))
							sc.bearerFormat = val;
					} else if (part.startsWith("openIdConnectUrl=")) {
						String val = part.substring(17);
						if (!val.equals("-"))
							sc.openIdConnectUrl = val;
					}
				}
				api.components.securitySchemes.put(sName, sc);
			} else if (tagName.equals("securitySchemeFlow")) {
				String[] parts = tagContent.split("\\s+");
				if (parts.length >= 4) {
					String flowType = parts[0];
					String sName = parts[1];
					openapi.SecurityScheme sc = (openapi.SecurityScheme) api.components.securitySchemes
							.computeIfAbsent(sName, k -> new openapi.SecurityScheme());
					if (sc.flows == null)
						sc.flows = new openapi.OAuthFlows();
					openapi.OAuthFlow flow = ensureOAuthFlow(sc.flows, flowType);
					if (flow != null) {
						if (flowType.equals("implicit")) {
							flow.authorizationUrl = parts[2].equals("-") ? null : parts[2];
							flow.refreshUrl = parts[3].equals("-") ? null : parts[3];
						} else if (flowType.equals("password") || flowType.equals("clientCredentials")) {
							flow.tokenUrl = parts[2].equals("-") ? null : parts[2];
							flow.refreshUrl = parts[3].equals("-") ? null : parts[3];
						} else if (flowType.equals("authorizationCode")) {
							flow.authorizationUrl = parts[2].equals("-") ? null : parts[2];
							flow.tokenUrl = parts[3].equals("-") ? null : parts[3];
							flow.refreshUrl = parts[4].equals("-") ? null : parts[4];
						} else {
							flow.deviceAuthorizationUrl = parts[2].equals("-") ? null : parts[2];
							flow.tokenUrl = parts[3].equals("-") ? null : parts[3];
							flow.refreshUrl = parts[4].equals("-") ? null : parts[4];
						}
					}
				}
			} else if (tagName.equals("securitySchemeFlowScope")) {
				String[] parts = tagContent.split("\\s+", 4);
				if (parts.length >= 3) {
					String sName = parts[0];
					String flowType = parts[1];
					String scopeName = parts[2];
					String scopeDesc = parts.length > 3 ? (parts[3].equals("-") ? "" : parts[3]) : "";

					openapi.SecurityScheme sc = (openapi.SecurityScheme) api.components.securitySchemes
							.computeIfAbsent(sName, k -> new openapi.SecurityScheme());
					if (sc.flows == null)
						sc.flows = new openapi.OAuthFlows();
					openapi.OAuthFlow flow = ensureOAuthFlow(sc.flows, flowType);
					if (flow != null) {
						if (flow.scopes == null)
							flow.scopes = new java.util.HashMap<>();
						flow.scopes.put(scopeName, scopeDesc);
					}
				}
			} else if (tagName.equals("componentResponse")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.Response res = new openapi.Response();
				if (parts.length > 1)
					res.description = parts[1];
				api.components.responses.put(parts[0], res);
			} else if (tagName.equals("componentParameter")) {
				String[] parts = tagContent.split("\\s+", 4);
				openapi.Parameter p = new openapi.Parameter();
				if (parts.length > 1 && !parts[1].equals("-"))
					p.name = parts[1];
				if (parts.length > 2 && !parts[2].equals("-"))
					p.in = parts[2];
				if (parts.length > 3)
					p.description = parts[3];
				api.components.parameters.put(parts[0], p);
			} else if (tagName.equals("componentRequestBody")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.RequestBody rb = new openapi.RequestBody();
				if (parts.length > 1)
					rb.description = parts[1];
				api.components.requestBodies.put(parts[0], rb);
			} else if (tagName.equals("componentHeader")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.Header hdr = new openapi.Header();
				if (parts.length > 1)
					hdr.description = parts[1];
				api.components.headers.put(parts[0], hdr);
			} else if (tagName.equals("componentLink")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
						k -> new openapi.Link());
				if (parts.length > 1)
					link.operationId = parts[1];
			} else if (tagName.equals("componentLinkOpId")) {
				String[] parts = tagContent.split("\\s+", 2);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					link.operationId = parts[1];
				}
			} else if (tagName.equals("componentLinkOpRef")) {
				String[] parts = tagContent.split("\\s+", 2);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					link.operationRef = parts[1];
				}
			} else if (tagName.equals("componentLinkDesc")) {
				String[] parts = tagContent.split("\\s+", 2);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					link.description = parts[1];
				}
			} else if (tagName.equals("componentLinkServer")) {
				String[] parts = tagContent.split("\\s+", 2);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					link.server = new openapi.Server();
					link.server.url = parts[1];
				}
			} else if (tagName.equals("componentLinkParam")) {
				String[] parts = tagContent.split("\\s+", 3);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					if (link.parameters == null)
						link.parameters = new java.util.HashMap<>();
					link.parameters.put(parts[1], parts.length > 2 ? parts[2] : "");
				}
			} else if (tagName.equals("componentLinkRequestBody")) {
				String[] parts = tagContent.split("\\s+", 2);
				if (parts.length > 1) {
					openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0],
							k -> new openapi.Link());
					link.requestBody = parts[1];
				}
			} else if (tagName.equals("componentCallback")) {
				String[] parts = tagContent.split("\\s+", 3);
				if (parts.length >= 3) {
					String cbKey = parts[0];
					String cbPath = parts[1];
					String cbMethod = parts[2].toLowerCase();
					openapi.Callback cb = new openapi.Callback();
					cb.pathItems = new java.util.HashMap<>();
					openapi.PathItem pi = new openapi.PathItem();
					openapi.Operation cbOp = new openapi.Operation();
					cbOp.description = "Callback operation";
					assignCallbackVerb(pi, cbMethod, cbOp);
					cb.pathItems.put(cbPath, pi);
					api.components.callbacks.put(cbKey, cb);
				}
			} else if (tagName.equals("componentPathItem")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.PathItem pi = new openapi.PathItem();
				if (parts.length > 1)
					pi.description = parts[1];
				api.components.pathItems.put(parts[0], pi);
			} else if (tagName.equals("componentMediaType")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.MediaType mt = new openapi.MediaType();
				if (parts.length > 1 && !parts[1].equals("-")) {
					java.util.HashMap<String, Object> schema = new java.util.HashMap<>();
					schema.put("type", parts[1]);
					mt.schema = schema;
				}
				api.components.mediaTypes.put(parts[0], mt);
			} else if (tagName.equals("pathSummary")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
				if (parts.length > 1)
					pi.summary = parts[1];
			} else if (tagName.equals("pathDescription")) {
				String[] parts = tagContent.split("\\s+", 2);
				openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
				if (parts.length > 1)
					pi.description = parts[1];
			} else if (tagName.equals("pathServer")) {
				String[] parts = tagContent.split("\\s+", 3);
				if (parts.length > 1) {
					openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
					if (pi.servers == null)
						pi.servers = new java.util.ArrayList<>();
					openapi.Server s = new openapi.Server();
					s.url = parts[1];
					if (parts.length > 2)
						s.description = parts[2];
					pi.servers.add(s);
				}
			} else if (tagName.equals("pathParameter")) {
				String[] parts = tagContent.split("\\s+", 4);
				if (parts.length > 2) {
					openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
					if (pi.parameters == null)
						pi.parameters = new java.util.ArrayList<>();
					openapi.Parameter p = new openapi.Parameter();
					p.name = parts[1].equals("-") ? null : parts[1];
					p.in = parts[2].equals("-") ? null : parts[2];
					if (parts.length > 3)
						p.description = parts[3];
					pi.parameters.add(p);
				}
			}
		}
	}

	/**
	 * Assigns a callback operation onto a {@link PathItem} for the specified HTTP
	 * verb.
	 *
	 * @param pi
	 *            The target {@link PathItem}.
	 * @param verb
	 *            The lowercase HTTP verb.
	 * @param op
	 *            The {@link Operation} to assign.
	 */
	private static void assignCallbackVerb(PathItem pi, String verb, Operation op) {
		switch (verb) {
			case "get" :
				pi.get = op;
				break;
			case "post" :
				pi.post = op;
				break;
			case "put" :
				pi.put = op;
				break;
			case "delete" :
				pi.delete = op;
				break;
			case "patch" :
				pi.patch = op;
				break;
			case "query" :
				pi.query = op;
				break;
			default :
				break;
		}
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
	@SuppressWarnings("unchecked")
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
						mt.schema = new HashMap<>();
						((HashMap<String, Object>) mt.schema).put("type", "string");
						rb.content.put("application/json", mt);
						op.requestBody = rb;
					} else {
						Parameter p = new Parameter();
						p.name = pName;
						p.in = "query"; // Default fallback for webhook extra params
						p.schema = new openapi.Schema();
						p.schema.type = "string";
						op.parameters.add(p);
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
	@SuppressWarnings("unchecked")
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
				assignCallbackVerb(pi, verb, op);
			}
		}
	}

	/**
	 * Ensures {@code op.requestBody} is a {@link openapi.RequestBody} with an
	 * initialized {@code content} map and returns it.
	 *
	 * @param op
	 *            The target {@link Operation}.
	 * @return The initialized {@link openapi.RequestBody}.
	 */
	private static openapi.RequestBody ensureRequestBody(Operation op) {
		if (!(op.requestBody instanceof openapi.RequestBody))
			op.requestBody = new openapi.RequestBody();
		openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
		if (rb.content == null)
			rb.content = new java.util.HashMap<>();
		return rb;
	}

	/**
	 * Ensures {@code op.responses} and the {@link openapi.Response} for
	 * {@code statusCode} (or {@code "default"}) exist and returns the response.
	 *
	 * @param op
	 *            The target {@link Operation}.
	 * @param statusCode
	 *            The HTTP status code or {@code "default"}.
	 * @return The initialized {@link openapi.Response}.
	 */
	private static openapi.Response ensureResponse(Operation op, String statusCode) {
		if (op.responses == null) {
			op.responses = new openapi.Responses();
			op.responses.statusCodes = new java.util.HashMap<>();
		}
		Object objR = statusCode.equals("default")
				? op.responses.defaultResponse
				: op.responses.statusCodes.get(statusCode);
		if (!(objR instanceof openapi.Response)) {
			objR = new openapi.Response();
			if (statusCode.equals("default")) {
				op.responses.defaultResponse = objR;
			} else {
				op.responses.statusCodes.put(statusCode, objR);
			}
		}
		return (openapi.Response) objR;
	}

	/**
	 * Parses a pipe-delimited {@code summary|description|value} string into an
	 * {@link openapi.Example}.
	 *
	 * @param pipeStr
	 *            The pipe-delimited string.
	 * @param replaceUnderscores
	 *            Whether to replace underscores with spaces in summary/description.
	 * @return The populated {@link openapi.Example}.
	 */
	private static openapi.Example parsePipeExample(String pipeStr, boolean replaceUnderscores) {
		openapi.Example exObj = new openapi.Example();
		if (pipeStr != null) {
			String[] exParts = pipeStr.split("\\|", -1);
			if (!exParts[0].isEmpty())
				exObj.summary = replaceUnderscores ? exParts[0].replace("_", " ") : exParts[0];
			if (exParts.length > 1 && !exParts[1].isEmpty())
				exObj.description = replaceUnderscores ? exParts[1].replace("_", " ") : exParts[1];
			if (exParts.length > 2 && !exParts[2].isEmpty())
				exObj.value = exParts[2];
		}
		return exObj;
	}

	/**
	 * Applies a media-type or encoding Javadoc tag to the target content map.
	 *
	 * @param contentMap
	 *            The target media type map.
	 * @param suffix
	 *            The tag suffix after {@code requestBody} or {@code response}.
	 * @param rest
	 *            The tag text following any status code prefix.
	 */
	@SuppressWarnings("unchecked")
	private static void applyRouteMediaTypeTag(java.util.Map<String, openapi.MediaType> contentMap, String suffix,
			String rest) {
		int limit = (suffix.equals("ContentExamples") || suffix.startsWith("Encoding")) ? 3 : 2;
		String[] parts = rest.split("\\s+", limit);
		String mediaTypeStr = parts[0];
		openapi.MediaType mt = contentMap.computeIfAbsent(mediaTypeStr, k -> new openapi.MediaType());
		if (suffix.equals("Content")) {
			if (mt.schema == null) {
				mt.schema = new java.util.HashMap<>();
				((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
			}
		} else if (suffix.equals("ContentSchema") && parts.length > 1) {
			mt.schema = new java.util.HashMap<>();
			((java.util.HashMap<String, Object>) mt.schema).put("type", parts[1]);
		} else if (suffix.equals("ContentExample") && parts.length > 1) {
			mt.example = parts[1];
		} else if (suffix.equals("ContentItemSchema") && parts.length > 1) {
			mt.itemSchema = new java.util.HashMap<>();
			((java.util.HashMap<String, Object>) mt.itemSchema).put("type", parts[1]);
		} else if (suffix.equals("ContentPrefixEncoding") && parts.length > 1) {
			if (mt.prefixEncoding == null)
				mt.prefixEncoding = new java.util.ArrayList<>();
			openapi.Encoding enc = new openapi.Encoding();
			enc.contentType = parts[1];
			mt.prefixEncoding.add(enc);
		} else if (suffix.equals("ContentItemEncoding") && parts.length > 1) {
			mt.itemEncoding = new openapi.Encoding();
			mt.itemEncoding.contentType = parts[1];
		} else if (suffix.startsWith("Encoding") && parts.length > 2) {
			if (mt.encoding == null)
				mt.encoding = new java.util.HashMap<>();
			openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new openapi.Encoding());
			if (suffix.equals("Encoding")) {
				enc.contentType = parts[2];
			} else if (suffix.equals("EncodingPrefixEncoding")) {
				if (enc.prefixEncoding == null)
					enc.prefixEncoding = new java.util.ArrayList<>();
				openapi.Encoding pEnc = new openapi.Encoding();
				pEnc.contentType = parts[2];
				enc.prefixEncoding.add(pEnc);
			} else {
				enc.itemEncoding = new openapi.Encoding();
				enc.itemEncoding.contentType = parts[2];
			}
		} else if (suffix.equals("ContentExamples") && parts.length > 1) {
			if (mt.examples == null)
				mt.examples = new java.util.HashMap<>();
			mt.examples.put(parts[1], parsePipeExample(parts.length > 2 ? parts[2] : null, true));
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
	@SuppressWarnings("unchecked")
	public static void parseRouteMethod(MethodDeclaration methodDecl, OpenAPI api,
			java.util.Map<String, openapi.Callback> callbacks) {
		String opId = methodDecl.getNameAsString();
		Operation op = new Operation();
		op.operationId = opId;

		Optional<JavadocComment> javadoc = methodDecl.getJavadocComment();

		java.util.Map<String, String> paramDescriptions = new java.util.HashMap<>();
		java.util.Set<String> requiredParams = new java.util.HashSet<>();
		java.util.Set<String> deprecatedParams = new java.util.HashSet<>();
		java.util.Set<String> allowEmptyValueParams = new java.util.HashSet<>();
		java.util.Map<String, String> paramStyles = new java.util.HashMap<>();
		java.util.Set<String> explodeParams = new java.util.HashSet<>();
		java.util.Set<String> allowReservedParams = new java.util.HashSet<>();
		java.util.Map<String, String> paramSchemas = new java.util.HashMap<>();
		java.util.Map<String, java.util.Set<String>> paramContents = new java.util.HashMap<>();
		java.util.Map<String, String> paramExamples = new java.util.HashMap<>();
		java.util.Map<String, java.util.Map<String, openapi.Example>> paramExamplesMap = new java.util.HashMap<>();

		if (javadoc.isPresent()) {
			com.github.javaparser.javadoc.Javadoc doc = javadoc.get().parse();
			String cleanDoc = doc.getDescription().toText().trim();
			if (!cleanDoc.isEmpty()) {
				String[] lines = cleanDoc.split("\\r?\\n", 2);
				op.summary = lines[0].trim();
				if (lines.length > 1) {
					op.description = lines[1].trim();
				}
			}

			for (com.github.javaparser.javadoc.JavadocBlockTag tag : doc.getBlockTags()) {
				String tagName = tag.getTagName();
				String tagContent = tag.getContent().toText().trim();
				if (tagName.equals("callback")) {
					String[] parts = tagContent.split("\\s+", 3);
					if (parts.length >= 3) {
						String cbName = parts[0];
						String cbExpr = parts[1];
						String cbMethod = parts[2].toLowerCase();

						if (op.callbacks == null)
							op.callbacks = new java.util.HashMap<>();
						openapi.Callback cb = new openapi.Callback();
						openapi.PathItem cbPath = new openapi.PathItem();
						openapi.Operation cbOp = new openapi.Operation();
						cbOp.description = "Callback operation";
						assignCallbackVerb(cbPath, cbMethod, cbOp);
						cb.addProperty(cbExpr, cbPath);
						op.callbacks.put(cbName, cb);
					}
				} else if (tagName.equals("tag")) {
					if (op.tags == null)
						op.tags = new ArrayList<>();
					op.tags.add(tagContent);
				} else if (tagName.equals("externalDocs")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (op.externalDocs == null)
						op.externalDocs = new openapi.ExternalDocumentation();
					op.externalDocs.url = parts[0];
					if (parts.length > 1) {
						op.externalDocs.description = parts[1];
					}
				} else if (tagName.equals("deprecated")) {
					op.deprecated = true;
				} else if (tagName.equals("operationServer")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (op.servers == null)
						op.servers = new java.util.ArrayList<>();
					openapi.Server s = new openapi.Server();
					s.url = parts[0];
					if (parts.length > 1)
						s.description = parts[1];
					op.servers.add(s);
				} else if (tagName.equals("operationSecurity")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (op.security == null)
						op.security = new java.util.ArrayList<>();
					openapi.SecurityRequirement req = new openapi.SecurityRequirement();
					req.requirements = new java.util.HashMap<>();
					java.util.List<String> scopes = new java.util.ArrayList<>();
					if (parts.length > 1) {
						scopes.addAll(java.util.Arrays.asList(parts[1].split(",")));
					}
					req.requirements.put(parts[0], scopes);
					op.security.add(req);
				} else if (tagName.equals("param")) {
					String pTagName = tag.getName().orElse("");
					if (!tagContent.isEmpty() && !tagContent.equals(pTagName)) {
						paramDescriptions.put(pTagName, tagContent);
					}
				} else if (tagName.equals("requiredParam")) {
					requiredParams.add(tagContent);
				} else if (tagName.equals("deprecatedParam")) {
					deprecatedParams.add(tagContent);
				} else if (tagName.equals("paramAllowEmptyValue")) {
					allowEmptyValueParams.add(tagContent);
				} else if (tagName.equals("paramStyle")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (parts.length > 1) {
						paramStyles.put(parts[0], parts[1]);
					}
				} else if (tagName.equals("paramExplode")) {
					explodeParams.add(tagContent);
				} else if (tagName.equals("paramAllowReserved")) {
					allowReservedParams.add(tagContent);
				} else if (tagName.equals("paramSchema")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (parts.length > 1) {
						paramSchemas.put(parts[0], parts[1]);
					}
				} else if (tagName.equals("paramContent")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (parts.length > 1) {
						paramContents.computeIfAbsent(parts[0], k -> new java.util.HashSet<>()).add(parts[1]);
					}
				} else if (tagName.equals("paramExample")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (parts.length > 1) {
						paramExamples.put(parts[0], parts[1]);
					}
				} else if (tagName.equals("paramExamples")) {
					String[] parts = tagContent.split("\\s+", 3);
					if (parts.length > 2) {
						paramExamplesMap.computeIfAbsent(parts[0], k -> new java.util.HashMap<>()).put(parts[1],
								parsePipeExample(parts[2], false));
					}
				} else if (tagName.equals("requestBody")) {
					ensureRequestBody(op).description = tagContent;
				} else if (tagName.equals("requestBodyRequired")) {
					ensureRequestBody(op).required = Boolean.parseBoolean(tagContent);
				} else if (tagName.startsWith("requestBodyContent") || tagName.startsWith("requestBodyEncoding")) {
					openapi.RequestBody rb = ensureRequestBody(op);
					applyRouteMediaTypeTag(rb.content, tagName.substring(11), tagContent);
				} else if (tagName.equals("responseDefault")) {
					openapi.Response r = ensureResponse(op, "default");
					if (!tagContent.isEmpty())
						r.description = tagContent;
				} else if (tagName.equals("response")) {
					String[] parts = tagContent.split("\\s+", 2);
					openapi.Response r = ensureResponse(op, parts[0]);
					if (parts.length > 1)
						r.description = parts[1];
				} else if (tagName.startsWith("responseHeader")) {
					String[] parts = tagContent.split("\\s+", tagName.equals("responseHeaderExamples") ? 4 : 3);
					if (parts.length > 1) {
						openapi.Response r = ensureResponse(op, parts[0]);
						if (r.headers == null)
							r.headers = new java.util.HashMap<>();
						openapi.Header h = (openapi.Header) r.headers.computeIfAbsent(parts[1],
								k -> new openapi.Header());

						if (tagName.equals("responseHeader") && parts.length > 2) {
							h.description = parts[2];
						} else if (tagName.equals("responseHeaderRequired")) {
							h.required = true;
						} else if (tagName.equals("responseHeaderDeprecated")) {
							h.deprecated = true;
						} else if (tagName.equals("responseHeaderStyle") && parts.length > 2) {
							h.style = parts[2];
						} else if (tagName.equals("responseHeaderExplode")) {
							h.explode = true;
						} else if (tagName.equals("responseHeaderSchema") && parts.length > 2) {
							openapi.Schema sch = new openapi.Schema();
							sch.type = parts[2];
							h.schema = sch;
						} else if (tagName.equals("responseHeaderExample") && parts.length > 2) {
							h.example = parts[2];
						} else if (tagName.equals("responseHeaderContent") && parts.length > 2) {
							h.content = new java.util.HashMap<>();
							h.content.put(parts[2], new openapi.MediaType());
						} else if (tagName.equals("responseHeaderExamples") && parts.length > 2) {
							if (h.examples == null)
								h.examples = new java.util.HashMap<>();
							h.examples.put(parts[2], parsePipeExample(parts.length > 3 ? parts[3] : null, false));
						}
					}
				} else if (tagName.startsWith("responseContent") || tagName.startsWith("responseEncoding")) {
					String[] parts = tagContent.split("\\s+", 2);
					if (parts.length > 1) {
						openapi.Response r = ensureResponse(op, parts[0]);
						if (r.content == null)
							r.content = new java.util.HashMap<>();
						applyRouteMediaTypeTag(r.content, tagName.substring(8), parts[1]);
					}
				} else if (tagName.startsWith("responseLink")) {
					int limit = tagName.equals("responseLinkParam") ? 4 : 3;
					String[] parts = tagContent.split("\\s+", limit);
					if (parts.length > 1) {
						openapi.Response r = ensureResponse(op, parts[0]);
						if (r.links == null)
							r.links = new java.util.HashMap<>();
						openapi.Link l = r.links.computeIfAbsent(parts[1], k -> new openapi.Link());

						if (tagName.equals("responseLinkOpId") && parts.length > 2) {
							l.operationId = parts[2];
						} else if (tagName.equals("responseLinkOpRef") && parts.length > 2) {
							l.operationRef = parts[2];
						} else if (tagName.equals("responseLinkDesc") && parts.length > 2) {
							l.description = parts[2];
						} else if (tagName.equals("responseLinkServer") && parts.length > 2) {
							l.server = new openapi.Server();
							l.server.url = parts[2];
						} else if (tagName.equals("responseLinkParam") && parts.length > 2) {
							if (l.parameters == null)
								l.parameters = new java.util.HashMap<>();
							l.parameters.put(parts[2], parts.length > 3 ? parts[3] : "");
						} else if (tagName.equals("responseLinkRequestBody") && parts.length > 2) {
							l.requestBody = parts[2];
						}
					}
				}
			}
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
					if (!(op.requestBody instanceof openapi.RequestBody))
						op.requestBody = new openapi.RequestBody();
					openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
					if (rb.content == null) {
						rb.content = new java.util.HashMap<>();
						openapi.MediaType mt = new openapi.MediaType();
						mt.schema = new java.util.HashMap<>();
						((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
						rb.content.put("application/json", mt);
					}
					continue;
				}

				openapi.Parameter p = new openapi.Parameter();
				p.name = pName;
				p.schema = new openapi.Schema();
				p.schema.type = "string";

				if (paramDescriptions.containsKey(pName)) {
					p.description = paramDescriptions.get(pName);
				}
				if (requiredParams.contains(pName)) {
					p.required = true;
				}
				if (deprecatedParams.contains(pName)) {
					p.deprecated = true;
				}
				if (allowEmptyValueParams.contains(pName)) {
					p.allowEmptyValue = true;
				}
				if (paramStyles.containsKey(pName)) {
					p.style = paramStyles.get(pName);
				}
				if (explodeParams.contains(pName)) {
					p.explode = true;
				}
				if (allowReservedParams.contains(pName)) {
					p.allowReserved = true;
				}
				if (paramSchemas.containsKey(pName)) {
					p.schema.type = paramSchemas.get(pName);
				}
				if (paramContents.containsKey(pName)) {
					p.content = new java.util.HashMap<>();
					for (String mediaTypeStr : paramContents.get(pName)) {
						openapi.MediaType mt = new openapi.MediaType();
						mt.schema = new java.util.HashMap<>();
						((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
						p.content.put(mediaTypeStr, mt);
					}
				}
				if (paramExamples.containsKey(pName)) {
					p.example = paramExamples.get(pName);
				}
				if (paramExamplesMap.containsKey(pName)) {
					p.examples = paramExamplesMap.get(pName);
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
					op.parameters.add(p);
				}
			}

			if (isSecurity) {
				ensureComponents(api);
				if (!api.components.securitySchemes.containsKey("BearerAuth")) {
					openapi.SecurityScheme sc = new openapi.SecurityScheme();
					sc.type = "http";
					sc.scheme = "bearer";
					api.components.securitySchemes.put("BearerAuth", sc);
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
				op.responses.statusCodes.put("200", r200);
			}

			switch (method) {

				case "GET" :
					item.get = op;
					break;
				case "POST" :
					item.post = op;
					break;
				case "PUT" :
					item.put = op;
					break;
				case "DELETE" :
					item.delete = op;
					break;
				case "PATCH" :
					item.patch = op;
					break;
				case "QUERY" :
					item.query = op;
					break;
				case "OPTIONS" :
					item.options = op;
					break;
				case "HEAD" :
					item.head = op;
					break;
				case "TRACE" :
					item.trace = op;
					break;
			}
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
