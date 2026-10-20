package arazzo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Evaluates Arazzo 1.1.0 runtime expressions, embedded {@code {...}} template
 * expressions, {@link Criterion} assertions, {@link Selector} queries, and
 * {@link PayloadReplacement} injections.
 */
public class RuntimeExpression {
	/**
	 * Default constructor.
	 */
	public RuntimeExpression() {
	}

	/**
	 * Holds the runtime state for a workflow and step execution.
	 */
	public static class Context {
		/** Default constructor. */
		public Context() {
		}

		/** Resolved request URL ({@code $url}). */
		public String url = "";
		/** HTTP method verb ({@code $method}). */
		public String method = "";
		/** HTTP status code ({@code $statusCode}). */
		public int statusCode = 200;
		/** Document canonical URI ({@code $self}). */
		public String selfUri = "";
		/** Outgoing request headers ({@code $request.header.<name>}). */
		public Map<String, String> requestHeaders = new LinkedHashMap<>();
		/** Outgoing request query parameters ({@code $request.query.<name>}). */
		public Map<String, String> requestQuery = new LinkedHashMap<>();
		/** Outgoing request path parameters ({@code $request.path.<name>}). */
		public Map<String, String> requestPath = new LinkedHashMap<>();
		/** Outgoing request body payload string ({@code $request.body}). */
		public String requestBody = "{}";
		/** Incoming response headers ({@code $response.header.<name>}). */
		public Map<String, String> responseHeaders = new LinkedHashMap<>();
		/** Incoming response body payload string ({@code $response.body}). */
		public String responseBody = "{}";
		/** AsyncAPI message headers ({@code $message.header.<name>}). */
		public Map<String, String> messageHeaders = new LinkedHashMap<>();
		/** AsyncAPI message payload string ({@code $message.payload}). */
		public String messagePayload = "{}";
		/** Workflow input values ({@code $inputs.<name>}). */
		public Map<String, String> inputs = new LinkedHashMap<>();
		/** Current workflow output values ({@code $outputs.<name>}). */
		public Map<String, String> outputs = new LinkedHashMap<>();
		/** Step outputs keyed by {@code stepId -> outputName -> value}. */
		public Map<String, Map<String, String>> steps = new LinkedHashMap<>();
		/**
		 * Cross-workflow outputs keyed by {@code workflowId -> outputName -> value}.
		 */
		public Map<String, Map<String, String>> workflows = new LinkedHashMap<>();
		/**
		 * Source description metadata keyed by {@code sourceName -> property -> value}.
		 */
		public Map<String, Map<String, String>> sourceDescriptions = new LinkedHashMap<>();
		/** Component values keyed by {@code category.name -> value}. */
		public Map<String, String> components = new LinkedHashMap<>();
	}

	/**
	 * Evaluates an Arazzo 1.1.0 runtime expression or template string against the
	 * given execution {@link Context}.
	 *
	 * @param expr
	 *            The runtime expression or template string.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return The resolved string representation (or empty string if unresolved).
	 */
	public static String evaluate(String expr, Context ctx) {
		if (expr == null) {
			return "";
		}
		if (expr.contains("{$")) {
			StringBuilder sb = new StringBuilder();
			int idx = 0;
			int open;
			while ((open = expr.indexOf("{$", idx)) >= 0) {
				int close = expr.indexOf('}', open);
				if (close < 0) {
					break;
				}
				sb.append(expr, idx, open);
				String inner = expr.substring(open + 1, close);
				sb.append(evaluateSingle(inner, ctx));
				idx = close + 1;
			}
			sb.append(expr.substring(idx));
			return sb.toString();
		}
		if (expr.startsWith("$")) {
			return evaluateSingle(expr, ctx);
		}
		return expr;
	}

	/**
	 * Evaluates a single {@code $}-prefixed Arazzo 1.1.0 runtime expression.
	 *
	 * @param expr
	 *            The single runtime expression starting with {@code $}.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return The resolved value string.
	 */
	private static String evaluateSingle(String expr, Context ctx) {
		if ("$url".equals(expr)) {
			return ctx.url;
		}
		if ("$method".equals(expr)) {
			return ctx.method;
		}
		if ("$statusCode".equals(expr)) {
			return String.valueOf(ctx.statusCode);
		}
		if ("$self".equals(expr)) {
			return ctx.selfUri;
		}
		if (expr.startsWith("$request.header.")) {
			return lookupIgnoreCase(ctx.requestHeaders, expr.substring("$request.header.".length()));
		}
		if (expr.startsWith("$request.query.")) {
			return ctx.requestQuery.getOrDefault(expr.substring("$request.query.".length()), "");
		}
		if (expr.startsWith("$request.path.")) {
			return ctx.requestPath.getOrDefault(expr.substring("$request.path.".length()), "");
		}
		if (expr.startsWith("$request.body")) {
			return resolveBodyPointer(ctx.requestBody, expr.substring("$request.body".length()));
		}
		if (expr.startsWith("$response.header.")) {
			return lookupIgnoreCase(ctx.responseHeaders, expr.substring("$response.header.".length()));
		}
		if (expr.startsWith("$response.body")) {
			return resolveBodyPointer(ctx.responseBody, expr.substring("$response.body".length()));
		}
		if (expr.startsWith("$message.header.")) {
			return lookupIgnoreCase(ctx.messageHeaders, expr.substring("$message.header.".length()));
		}
		if (expr.startsWith("$message.payload")) {
			return resolveBodyPointer(ctx.messagePayload, expr.substring("$message.payload".length()));
		}
		if (expr.startsWith("$inputs.")) {
			return ctx.inputs.getOrDefault(expr.substring("$inputs.".length()), "");
		}
		if (expr.startsWith("$outputs.")) {
			return ctx.outputs.getOrDefault(expr.substring("$outputs.".length()), "");
		}
		if (expr.startsWith("$steps.")) {
			String rest = expr.substring("$steps.".length());
			int dot = rest.indexOf(".outputs.");
			if (dot > 0) {
				String stepId = rest.substring(0, dot);
				String outKey = rest.substring(dot + ".outputs.".length());
				Map<String, String> stepMap = ctx.steps.get(stepId);
				return stepMap != null ? stepMap.getOrDefault(outKey, "") : "";
			}
		}
		if (expr.startsWith("$workflows.")) {
			String rest = expr.substring("$workflows.".length());
			int dot = rest.indexOf(".outputs.");
			if (dot > 0) {
				String wfId = rest.substring(0, dot);
				String outKey = rest.substring(dot + ".outputs.".length());
				Map<String, String> wfMap = ctx.workflows.get(wfId);
				return wfMap != null ? wfMap.getOrDefault(outKey, "") : "";
			}
		}
		if (expr.startsWith("$sourceDescriptions.")) {
			String rest = expr.substring("$sourceDescriptions.".length());
			int dot = rest.indexOf('.');
			if (dot > 0) {
				String sdName = rest.substring(0, dot);
				String prop = rest.substring(dot + 1);
				Map<String, String> sdMap = ctx.sourceDescriptions.get(sdName);
				return sdMap != null ? sdMap.getOrDefault(prop, "") : "";
			}
		}
		if (expr.startsWith("$components.")) {
			return ctx.components.getOrDefault(expr.substring("$components.".length()), "");
		}
		return "";
	}

	/**
	 * Evaluates an {@link OutputValue} (either a raw runtime expression or a
	 * structured {@link Selector}) against the given {@link Context}.
	 *
	 * @param outputValue
	 *            The {@link OutputValue} to evaluate.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return The extracted string value.
	 */
	public static String evaluateOutputValue(OutputValue outputValue, Context ctx) {
		if (outputValue == null) {
			return "";
		}
		if (outputValue.isSelector()) {
			return evaluateSelector(outputValue.selector, ctx);
		}
		return evaluate(outputValue.expression, ctx);
	}

	/**
	 * Evaluates a structured {@link Selector} against the given {@link Context}.
	 *
	 * @param selector
	 *            The {@link Selector} object.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return The extracted value string.
	 */
	public static String evaluateSelector(Selector selector, Context ctx) {
		if (selector == null || selector.selector == null) {
			return "";
		}
		String doc = selector.context != null ? evaluate(selector.context, ctx) : ctx.responseBody;
		CriterionEngineType engine = selector.type != null
				? selector.type.resolveEngineType()
				: CriterionEngineType.JSONPATH;
		if (engine == CriterionEngineType.XPATH) {
			return queryXPath(doc, selector.selector);
		}
		if (engine == CriterionEngineType.JSONPOINTER) {
			return queryJsonPointer(doc, selector.selector);
		}
		return queryJsonPath(doc, selector.selector);
	}

	/**
	 * Evaluates a {@link Criterion} assertion against the given {@link Context}.
	 *
	 * @param criterion
	 *            The {@link Criterion} to evaluate.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return {@code true} if the criterion holds; {@code false} otherwise.
	 */
	public static boolean evaluateCriterion(Criterion criterion, Context ctx) {
		if (criterion == null || criterion.condition == null) {
			return true;
		}
		CriterionEngineType engine = criterion.type != null
				? criterion.type.resolveEngineType()
				: CriterionEngineType.SIMPLE;
		String contextVal = criterion.context != null ? evaluate(criterion.context, ctx) : "";
		if (engine == CriterionEngineType.REGEX) {
			return Pattern.compile(criterion.condition).matcher(contextVal).find();
		}
		if (engine == CriterionEngineType.JSONPOINTER) {
			String doc = criterion.context != null ? contextVal : ctx.responseBody;
			return evaluateTargetedCondition(doc, criterion.condition, true);
		}
		if (engine == CriterionEngineType.JSONPATH) {
			String doc = criterion.context != null ? contextVal : ctx.responseBody;
			return evaluateTargetedCondition(doc, criterion.condition, false);
		}
		if (engine == CriterionEngineType.XPATH) {
			String doc = criterion.context != null ? contextVal : ctx.responseBody;
			return !queryXPath(doc, criterion.condition).isEmpty();
		}
		return evaluateSimpleCondition(criterion.condition, contextVal, ctx);
	}

	/**
	 * Applies a list of {@link PayloadReplacement} rules to a JSON payload string.
	 *
	 * @param basePayload
	 *            The base JSON payload string.
	 * @param replacements
	 *            The list of replacements to apply.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return The updated JSON payload string.
	 */
	public static String applyReplacements(String basePayload, java.util.List<PayloadReplacement> replacements,
			Context ctx) {
		if (replacements == null || replacements.isEmpty()) {
			return basePayload;
		}
		String trimmed = basePayload != null ? basePayload.trim() : "{}";
		if (!trimmed.startsWith("{")) {
			return basePayload;
		}
		JSONObject root = new JSONObject(trimmed);
		for (PayloadReplacement pr : replacements) {
			if (pr.target == null || pr.value == null) {
				continue;
			}
			String rawVal = pr.value.isString() ? evaluate(pr.value.stringVal, ctx) : pr.value.toString();
			String path = pr.target;
			if (path.startsWith("$.")) {
				path = "/" + path.substring(2).replace('.', '/');
			}
			if (path.startsWith("/")) {
				String[] tokens = path.substring(1).split("/");
				JSONObject curr = root;
				for (int i = 0; i < tokens.length - 1; i++) {
					String key = unescapePointerToken(tokens[i]);
					if (!curr.has(key) || !(curr.get(key) instanceof JSONObject)) {
						curr.put(key, new JSONObject());
					}
					curr = curr.getJSONObject(key);
				}
				String leaf = unescapePointerToken(tokens[tokens.length - 1]);
				curr.put(leaf, rawVal);
			}
		}
		return root.toString();
	}

	/**
	 * Evaluates a JSONPath or JSON Pointer condition (optionally containing a
	 * comparison operator like {@code $.status == 'available'} or
	 * {@code /id == 1}).
	 *
	 * @param document
	 *            The JSON document string.
	 * @param condition
	 *            The query or comparison condition.
	 * @param isPointer
	 *            {@code true} for JSON Pointer; {@code false} for JSONPath.
	 * @return {@code true} if the condition is satisfied.
	 */
	private static boolean evaluateTargetedCondition(String document, String condition, boolean isPointer) {
		String[] ops = {"==", "!=", ">=", "<=", ">", "<"};
		for (String op : ops) {
			int idx = condition.indexOf(op);
			if (idx > 0) {
				String leftQuery = condition.substring(0, idx).trim();
				String rightExpected = stripQuotes(condition.substring(idx + op.length()).trim());
				String actual = isPointer ? queryJsonPointer(document, leftQuery) : queryJsonPath(document, leftQuery);
				return compareValues(actual, op, rightExpected);
			}
		}
		String extracted = isPointer
				? queryJsonPointer(document, condition.trim())
				: queryJsonPath(document, condition.trim());
		return !extracted.isEmpty() && !"false".equalsIgnoreCase(extracted) && !"null".equalsIgnoreCase(extracted);
	}

	/**
	 * Evaluates a simple boolean/relational condition expression.
	 *
	 * @param condition
	 *            The simple condition string (supporting {@code &&}, {@code ||},
	 *            and relational operators).
	 * @param contextVal
	 *            The evaluated {@code Criterion.context} value if present.
	 * @param ctx
	 *            The execution {@link Context}.
	 * @return {@code true} if the condition holds.
	 */
	private static boolean evaluateSimpleCondition(String condition, String contextVal, Context ctx) {
		if (condition.contains("&&")) {
			for (String part : condition.split("&&")) {
				if (!evaluateSimpleCondition(part.trim(), contextVal, ctx)) {
					return false;
				}
			}
			return true;
		}
		if (condition.contains("||")) {
			for (String part : condition.split("\\|\\|")) {
				if (evaluateSimpleCondition(part.trim(), contextVal, ctx)) {
					return true;
				}
			}
			return false;
		}
		String[] ops = {"==", "!=", ">=", "<=", ">", "<"};
		for (String op : ops) {
			int idx = condition.indexOf(op);
			if (idx >= 0) {
				String leftRaw = condition.substring(0, idx).trim();
				String rightRaw = condition.substring(idx + op.length()).trim();
				String leftVal = leftRaw.isEmpty()
						? contextVal
						: (leftRaw.startsWith("$") ? evaluate(leftRaw, ctx) : stripQuotes(leftRaw));
				String rightVal = rightRaw.startsWith("$") ? evaluate(rightRaw, ctx) : stripQuotes(rightRaw);
				return compareValues(leftVal, op, rightVal);
			}
		}
		String resolved = condition.startsWith("$") ? evaluate(condition, ctx) : condition;
		return !resolved.isEmpty() && !"false".equalsIgnoreCase(resolved) && !"null".equalsIgnoreCase(resolved);
	}

	/**
	 * Compares two string values using numeric or lexicographical relational
	 * operators.
	 *
	 * @param left
	 *            The left operand.
	 * @param op
	 *            The operator ({@code ==}, {@code !=}, {@code >=}, {@code <=},
	 *            {@code >}, {@code <}).
	 * @param right
	 *            The right operand.
	 * @return {@code true} if the comparison holds.
	 */
	private static boolean compareValues(String left, String op, String right) {
		if ("==".equals(op)) {
			return left.equals(right);
		}
		if ("!=".equals(op)) {
			return !left.equals(right);
		}
		try {
			double dLeft = Double.parseDouble(left);
			double dRight = Double.parseDouble(right);
			if (">=".equals(op)) {
				return dLeft >= dRight;
			}
			if ("<=".equals(op)) {
				return dLeft <= dRight;
			}
			if (">".equals(op)) {
				return dLeft > dRight;
			}
			return dLeft < dRight;
		} catch (NumberFormatException e) {
			int cmp = left.compareTo(right);
			if (">=".equals(op)) {
				return cmp >= 0;
			}
			if ("<=".equals(op)) {
				return cmp <= 0;
			}
			if (">".equals(op)) {
				return cmp > 0;
			}
			return cmp < 0;
		}
	}

	/**
	 * Strips surrounding single or double quotes from a literal token.
	 *
	 * @param token
	 *            The raw token.
	 * @return The unquoted token.
	 */
	private static String stripQuotes(String token) {
		if (token.length() >= 2 && ((token.startsWith("'") && token.endsWith("'"))
				|| (token.startsWith("\"") && token.endsWith("\"")))) {
			return token.substring(1, token.length() - 1);
		}
		return token;
	}

	/**
	 * Resolves a body expression suffix (empty, {@code #/pointer}, or
	 * {@code .prop}) against a JSON body string.
	 *
	 * @param body
	 *            The JSON body string.
	 * @param suffix
	 *            The expression suffix after {@code $response.body},
	 *            {@code $request.body}, or {@code $message.payload}.
	 * @return The resolved value string.
	 */
	private static String resolveBodyPointer(String body, String suffix) {
		if (suffix.isEmpty()) {
			return body == null ? "" : body;
		}
		if (suffix.startsWith("#")) {
			return queryJsonPointer(body, suffix.substring(1));
		}
		if (suffix.startsWith(".")) {
			return queryJsonPath(body, "$" + suffix);
		}
		return "";
	}

	/**
	 * Evaluates an RFC 6901 JSON Pointer against a JSON string.
	 *
	 * @param json
	 *            The JSON string.
	 * @param pointer
	 *            The RFC 6901 JSON Pointer (e.g. {@code "/items/0/id"}).
	 * @return The extracted value as a string, or empty string if not found.
	 */
	public static String queryJsonPointer(String json, String pointer) {
		if (json == null || pointer == null) {
			return "";
		}
		String trimmed = json.trim();
		if (pointer.isEmpty() || "/".equals(pointer)) {
			return trimmed;
		}
		String norm = pointer.startsWith("#") ? pointer.substring(1) : pointer;
		if (!norm.startsWith("/")) {
			norm = "/" + norm;
		}
		Object curr;
		if (trimmed.startsWith("{")) {
			curr = new JSONObject(trimmed);
		} else if (trimmed.startsWith("[")) {
			curr = new JSONArray(trimmed);
		} else {
			return "";
		}
		String[] tokens = norm.substring(1).split("/");
		for (String rawTok : tokens) {
			String tok = unescapePointerToken(rawTok);
			if (curr instanceof JSONObject) {
				JSONObject jo = (JSONObject) curr;
				if (!jo.has(tok) || jo.isNull(tok)) {
					return "";
				}
				curr = jo.get(tok);
			} else if (curr instanceof JSONArray) {
				JSONArray ja = (JSONArray) curr;
				try {
					int idx = Integer.parseInt(tok);
					if (idx < 0 || idx >= ja.length() || ja.isNull(idx)) {
						return "";
					}
					curr = ja.get(idx);
				} catch (NumberFormatException e) {
					return "";
				}
			} else {
				return "";
			}
		}
		return String.valueOf(curr);
	}

	/**
	 * Evaluates a JSONPath query (e.g. {@code $.items[0].id}) against a JSON
	 * string.
	 *
	 * @param json
	 *            The JSON string.
	 * @param jsonPath
	 *            The JSONPath expression.
	 * @return The extracted value as a string, or empty string if not found.
	 */
	public static String queryJsonPath(String json, String jsonPath) {
		if (json == null || jsonPath == null) {
			return "";
		}
		String path = jsonPath.trim();
		if (path.startsWith("$")) {
			path = path.substring(1);
		}
		String pointer = path.replaceAll("\\[(\\d+)\\]", "/$1").replace('.', '/');
		if (!pointer.startsWith("/")) {
			pointer = "/" + pointer;
		}
		return queryJsonPointer(json, pointer);
	}

	/**
	 * Evaluates a lightweight XPath tag query (e.g. {@code //id} or
	 * {@code /user/id}) against an XML string.
	 *
	 * @param xml
	 *            The XML payload string.
	 * @param xpath
	 *            The XPath expression.
	 * @return The inner text of the matched XML element, or empty string if not
	 *         found.
	 */
	public static String queryXPath(String xml, String xpath) {
		if (xml == null || xpath == null) {
			return "";
		}
		String[] segments = xpath.replaceAll("^/+", "").split("/");
		String leafTag = segments[segments.length - 1].replaceAll("[^a-zA-Z0-9_\\-:]", "");
		if (leafTag.isEmpty()) {
			return "";
		}
		Matcher m = Pattern.compile("<" + Pattern.quote(leafTag) + "[^>]*>([^<]*)</" + Pattern.quote(leafTag) + ">")
				.matcher(xml);
		return m.find() ? m.group(1).trim() : "";
	}

	/**
	 * Unescapes RFC 6901 JSON Pointer escape sequences ({@code ~1} -> {@code /},
	 * {@code ~0} -> {@code ~}).
	 *
	 * @param token
	 *            The escaped pointer token.
	 * @return The unescaped token.
	 */
	private static String unescapePointerToken(String token) {
		return common.JsonPointerUtils.unescapePointerToken(token);
	}

	/**
	 * Performs a case-insensitive key lookup in a string map.
	 *
	 * @param map
	 *            The header map.
	 * @param key
	 *            The header name to look up.
	 * @return The header value, or empty string if not found.
	 */
	private static String lookupIgnoreCase(Map<String, String> map, String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		for (Map.Entry<String, String> e : map.entrySet()) {
			if (e.getKey().equalsIgnoreCase(key)) {
				return e.getValue();
			}
		}
		return "";
	}
}
