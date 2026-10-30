package docstrings;

import arazzo.Arazzo;
import arazzo.ArazzoParameter;
import arazzo.ReusableOr;
import arazzo.Workflow;
import common.PayloadValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import openapi.MediaType;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.Parameter;
import openapi.PathItem;
import openapi.ReferenceOr;
import openapi.RequestBody;
import openapi.Schema;
import openapi.TypeMapper;
import org.json.JSONObject;

/**
 * Emits structured documentation JSON containing rich Java SDK code components
 * ({@code imports}, {@code wrapper_start}, {@code snippet},
 * {@code wrapper_end}, and assembled {@code code}) for OpenAPI endpoints,
 * webhooks, and Arazzo workflows.
 */
public class Emit {

	/** Line feed separator constant. */
	private static final String NL = String.valueOf((char) 10);

	/** Double-quote character constant. */
	private static final String Q = String.valueOf((char) 34);

	/**
	 * Standard Java SDK import block included when {@code noImports} is
	 * {@code false}.
	 */
	public static final String DEFAULT_IMPORTS = "import my.api.ApiClient;" + NL + "import my.api.ApiException;";

	/**
	 * Standard Java class, {@code main} method, client instantiation, and
	 * {@code try} block opening included when {@code noWrapping} is {@code false}.
	 */
	public static final String DEFAULT_WRAPPER_START = "public class Example {" + NL
			+ "    public static void main(String[] args) {" + NL + "        ApiClient client = new ApiClient();" + NL
			+ "        try {";

	/**
	 * Standard {@code catch} block and closing braces included when
	 * {@code noWrapping} is {@code false}.
	 */
	public static final String DEFAULT_WRAPPER_END = "        } catch (ApiException e) {" + NL
			+ "            e.printStackTrace();" + NL + "        }" + NL + "    }" + NL + "}";

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits structured documentation JSON for an {@link OpenAPI} model.
	 *
	 * @param api
	 *            The {@link OpenAPI} model.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer {@code Example} class and {@code main}
	 *            method wrapper.
	 * @return Generated documentation JSON string.
	 * @throws Exception
	 *             If generation fails.
	 */
	public static String emitDocsJson(OpenAPI api, boolean noImports, boolean noWrapping) throws Exception {
		return emitDocsJson(api, null, noImports, noWrapping);
	}

	/**
	 * Emits structured documentation JSON combining an {@link OpenAPI} model (with
	 * {@code paths} and {@code webhooks}) and an optional {@link Arazzo} workflow
	 * specification (with {@code workflows}).
	 *
	 * @param api
	 *            The {@link OpenAPI} model, or {@code null}.
	 * @param arazzoSpec
	 *            The {@link Arazzo} workflow specification, or {@code null}.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer {@code Example} class and {@code main}
	 *            method wrapper.
	 * @return Generated documentation JSON string.
	 * @throws Exception
	 *             If generation fails.
	 */
	public static String emitDocsJson(OpenAPI api, Arazzo arazzoSpec, boolean noImports, boolean noWrapping)
			throws Exception {
		JSONObject result = new JSONObject();
		String importsStr = noImports ? "" : DEFAULT_IMPORTS;
		String wrapperStartStr = noWrapping ? "" : DEFAULT_WRAPPER_START;
		String wrapperEndStr = noWrapping ? "" : DEFAULT_WRAPPER_END;

		result.put("imports", importsStr);
		result.put("wrapper_start", wrapperStartStr);
		result.put("wrapper_end", wrapperEndStr);

		JSONObject endpoints = new JSONObject();
		if (api != null && api.paths != null && api.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				PathItem pi = entry.getValue();
				JSONObject pathMap = buildPathItemMap(pi, api, noImports, noWrapping);
				if (pathMap.length() > 0) {
					endpoints.put(path, pathMap);
				}
			}
		}
		result.put("endpoints", endpoints);

		if (api != null && api.webhooks != null && !api.webhooks.isEmpty()) {
			JSONObject webhooks = new JSONObject();
			for (Map.Entry<String, PathItem> entry : api.webhooks.entrySet()) {
				String hookName = entry.getKey();
				PathItem pi = entry.getValue();
				JSONObject hookMap = buildPathItemMap(pi, api, noImports, noWrapping);
				if (hookMap.length() > 0) {
					webhooks.put(hookName, hookMap);
				}
			}
			if (webhooks.length() > 0) {
				result.put("webhooks", webhooks);
			}
		}

		if (arazzoSpec != null && arazzoSpec.workflows != null && !arazzoSpec.workflows.isEmpty()) {
			JSONObject workflows = new JSONObject();
			OpenAPI effectiveApi = api != null ? api : new OpenAPI();
			for (Workflow wf : arazzoSpec.workflows) {
				String wfId = (wf.workflowId != null && !wf.workflowId.isEmpty()) ? wf.workflowId : "runWorkflow";
				workflows.put(wfId, buildWorkflowCodeObject(wf, wfId, arazzoSpec, effectiveApi, noImports, noWrapping));
			}
			result.put("workflows", workflows);
		}

		return result.toString(2);
	}

	/**
	 * Builds a map of HTTP verbs to structured code objects for all operations on a
	 * {@link PathItem}.
	 *
	 * @param pi
	 *            The {@link PathItem} to inspect.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer wrapper.
	 * @return A {@link JSONObject} mapping HTTP method names to structured code
	 *         objects.
	 */
	private static JSONObject buildPathItemMap(PathItem pi, OpenAPI api, boolean noImports, boolean noWrapping) {
		JSONObject pathMap = new JSONObject();
		for (Map.Entry<openapi.HttpVerb, Operation> opEntry : pi.getOperationsMap().entrySet()) {
			pathMap.put(opEntry.getKey().specName,
					buildOperationCodeObject(opEntry.getValue(), pi, api, noImports, noWrapping));
		}
		return pathMap;
	}

	/**
	 * Builds a structured code {@link JSONObject} ({@code imports},
	 * {@code wrapper_start}, {@code snippet}, {@code wrapper_end}, and
	 * {@code code}) for an OpenAPI {@link Operation}.
	 *
	 * @param op
	 *            The {@link Operation} to generate a snippet for.
	 * @param pi
	 *            The parent {@link PathItem}.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer {@code Example} class and {@code main}
	 *            method wrapper.
	 * @return The structured code {@link JSONObject}.
	 */
	private static JSONObject buildOperationCodeObject(Operation op, PathItem pi, OpenAPI api, boolean noImports,
			boolean noWrapping) {
		String snippetOpId = (op.operationId != null && !op.operationId.isEmpty()) ? op.operationId : "callEndpoint";
		List<String> args = buildOperationArguments(op, pi, api);
		String snippet = "var response = client." + snippetOpId + "(" + String.join(", ", args) + ");" + NL
				+ "System.out.println(response);";
		return buildCodeJsonObject(snippet, noImports, noWrapping);
	}

	/**
	 * Builds a structured code {@link JSONObject} for an Arazzo {@link Workflow}.
	 *
	 * @param wf
	 *            The {@link Workflow} to generate a snippet for.
	 * @param wfId
	 *            The resolved workflow identifier or default method name.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer wrapper.
	 * @return The structured code {@link JSONObject}.
	 */
	private static JSONObject buildWorkflowCodeObject(Workflow wf, String wfId, Arazzo spec, OpenAPI api,
			boolean noImports, boolean noWrapping) {
		List<String> args = buildWorkflowArguments(wf, spec, api);
		String snippet = "var response = client." + wfId + "(" + String.join(", ", args) + ");" + NL
				+ "System.out.println(response);";
		return buildCodeJsonObject(snippet, noImports, noWrapping);
	}

	/**
	 * Assembles a structured code {@link JSONObject} containing {@code imports},
	 * {@code wrapper_start}, {@code snippet}, {@code wrapper_end}, and {@code code}
	 * fields.
	 *
	 * @param snippet
	 *            The core SDK invocation snippet.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer wrapper.
	 * @return The populated {@link JSONObject}.
	 */
	private static JSONObject buildCodeJsonObject(String snippet, boolean noImports, boolean noWrapping) {
		String importsStr = noImports ? "" : DEFAULT_IMPORTS;
		String wrapperStartStr = noWrapping ? "" : DEFAULT_WRAPPER_START;
		String wrapperEndStr = noWrapping ? "" : DEFAULT_WRAPPER_END;

		StringBuilder fullCode = new StringBuilder();
		if (!noImports) {
			fullCode.append(DEFAULT_IMPORTS).append(NL).append(NL);
		}
		if (!noWrapping) {
			fullCode.append(DEFAULT_WRAPPER_START).append(NL);
			String[] lines = snippet.split(NL);
			for (String line : lines) {
				fullCode.append("            ").append(line).append(NL);
			}
			fullCode.append(DEFAULT_WRAPPER_END);
		} else {
			fullCode.append(snippet);
		}

		JSONObject codeObj = new JSONObject();
		codeObj.put("imports", importsStr);
		codeObj.put("wrapper_start", wrapperStartStr);
		codeObj.put("snippet", snippet);
		codeObj.put("wrapper_end", wrapperEndStr);
		codeObj.put("code", fullCode.toString());
		return codeObj;
	}

	/**
	 * Inspects {@code pi.parameters}, {@code op.parameters}, and
	 * {@code op.requestBody} to synthesize sample typed Java arguments matching the
	 * generated SDK method signature.
	 *
	 * @param op
	 *            The {@link Operation}.
	 * @param pi
	 *            The parent {@link PathItem}.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return Ordered list of formatted Java argument expressions.
	 */
	private static List<String> buildOperationArguments(Operation op, PathItem pi, OpenAPI api) {
		List<Parameter> allParams = new ArrayList<>();
		if (pi.parameters != null) {
			for (ReferenceOr<Parameter> po : pi.parameters) {
				Parameter resolved = resolveParameter(po, api);
				if (resolved != null) {
					allParams.add(resolved);
				}
			}
		}
		if (op.parameters != null) {
			for (ReferenceOr<Parameter> po : op.parameters) {
				Parameter resolved = resolveParameter(po, api);
				if (resolved != null) {
					boolean exists = false;
					for (Parameter existing : allParams) {
						if (Objects.equals(existing.name, resolved.name) && Objects.equals(existing.in, resolved.in)) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						allParams.add(resolved);
					}
				}
			}
		}

		List<String> args = new ArrayList<>();
		for (Parameter p : allParams) {
			args.add(sampleValueForParameter(p, api));
		}
		if (op.requestBody != null) {
			args.add(sampleValueForRequestBody(op.requestBody, api));
		}
		return args;
	}

	/**
	 * Synthesizes sample typed Java arguments for an Arazzo {@link Workflow} from
	 * its {@code inputs} schema or {@code parameters}.
	 *
	 * @param wf
	 *            The {@link Workflow}.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return Ordered list of formatted Java argument expressions.
	 */
	private static List<String> buildWorkflowArguments(Workflow wf, Arazzo spec, OpenAPI api) {
		List<String> args = new ArrayList<>();
		Schema inSchema = arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec);
		if (inSchema != null && inSchema.properties != null) {
			for (Map.Entry<String, ReferenceOr<Schema>> prop : inSchema.properties.entrySet()) {
				Schema propSchema = prop.getValue().value;
				args.add(sampleValueForSchema(propSchema, prop.getKey(), api));
			}
		} else if (wf.parameters != null) {
			for (ReusableOr<ArazzoParameter> paramRef : wf.parameters) {
				ArazzoParameter param = arazzo.ArazzoResolver.resolveParameter(paramRef, spec);
				if (param != null && param.value != null) {
					args.add(formatPayloadValue(param.value));
				}
			}
		}
		return args;
	}

	/**
	 * Resolves a {@link ReferenceOr}{@code <}{@link Parameter}{@code >} against
	 * {@code api.components.parameters} if it is a {@code $ref}, or returns its
	 * inline value.
	 *
	 * @param paramRef
	 *            The parameter reference or inline wrapper.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return The resolved {@link Parameter}, or {@code null}.
	 */
	private static Parameter resolveParameter(ReferenceOr<Parameter> paramRef, OpenAPI api) {
		if (paramRef.value != null) {
			return paramRef.value;
		}
		if (paramRef.isRef() && api.components != null && api.components.parameters != null) {
			String refStr = paramRef.resolveRefString();
			String name = refStr.substring(refStr.lastIndexOf('/') + 1);
			ReferenceOr<Parameter> target = api.components.parameters.get(name);
			if (target != null) {
				return target.value;
			}
		}
		return null;
	}

	/**
	 * Resolves a {@code $ref} schema URI against {@code api.components.schemas}.
	 *
	 * @param refStr
	 *            The {@code $ref} URI string.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return The resolved {@link Schema}, or {@code null}.
	 */
	private static Schema resolveComponentSchema(String refStr, OpenAPI api) {
		if (api.components != null && api.components.schemas != null) {
			String name = refStr.substring(refStr.lastIndexOf('/') + 1);
			ReferenceOr<Schema> target = api.components.schemas.get(name);
			if (target != null) {
				return target.value;
			}
		}
		return null;
	}

	/**
	 * Synthesizes a sample Java literal expression for an OpenAPI
	 * {@link Parameter}.
	 *
	 * @param p
	 *            The {@link Parameter}.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return A Java literal string representing a sample argument value.
	 */
	private static String sampleValueForParameter(Parameter p, OpenAPI api) {
		if (p.example != null && !p.example.isNull()) {
			return formatPayloadValue(p.example);
		}
		if (p.defaultValue != null && !p.defaultValue.isNull()) {
			return formatPayloadValue(p.defaultValue);
		}
		if (p.enumValues != null && !p.enumValues.isEmpty()) {
			return formatPayloadValue(p.enumValues.get(0));
		}
		if (p.schema != null) {
			Schema s = p.schema.isRef() ? resolveComponentSchema(p.schema.resolveRefString(), api) : p.schema.value;
			if (s != null) {
				return sampleValueForSchema(s, p.name, api);
			}
		}
		if (p.type != null) {
			return sampleValueForTypeAndFormat(p.type, p.format, p.name);
		}
		return defaultStringLiteral(p.name);
	}

	/**
	 * Synthesizes a sample Java expression for an OpenAPI {@link RequestBody}.
	 *
	 * @param rbRef
	 *            The {@link ReferenceOr}{@code <}{@link RequestBody}{@code >}.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return A Java expression representing a sample request body argument.
	 */
	private static String sampleValueForRequestBody(ReferenceOr<RequestBody> rbRef, OpenAPI api) {
		RequestBody rb = rbRef.value;
		if (rbRef.isRef() && api.components != null && api.components.requestBodies != null) {
			String refStr = rbRef.resolveRefString();
			String name = refStr.substring(refStr.lastIndexOf('/') + 1);
			ReferenceOr<RequestBody> target = api.components.requestBodies.get(name);
			if (target != null) {
				rb = target.value;
			}
		}
		if (rb != null && rb.content != null && !rb.content.isEmpty()) {
			MediaType mt = rb.content.get("application/json");
			if (mt == null) {
				mt = rb.content.values().iterator().next();
			}
			if (mt.example != null && !mt.example.isNull()) {
				return formatPayloadValue(mt.example);
			}
			if (mt.schema != null) {
				if (mt.schema.isRef()) {
					String refStr = mt.schema.resolveRefString();
					String schemaName = TypeMapper.sanitizeAlphanumeric(refStr.substring(refStr.lastIndexOf('/') + 1));
					return "new " + schemaName + "()";
				}
				if (mt.schema.value != null) {
					Schema s = mt.schema.value;
					if (s.$ref != null && !s.$ref.isEmpty()) {
						String schemaName = TypeMapper
								.sanitizeAlphanumeric(s.$ref.substring(s.$ref.lastIndexOf('/') + 1));
						return "new " + schemaName + "()";
					}
					if (s.title != null && !s.title.isEmpty()) {
						return "new " + TypeMapper.sanitizeAlphanumeric(s.title) + "()";
					}
					if (s.type != null && !"object".equals(s.type.single)) {
						return sampleValueForSchema(s, "body", api);
					}
				}
			}
		}
		return Q + "{}" + Q;
	}

	/**
	 * Synthesizes a sample Java literal expression for an OpenAPI {@link Schema}.
	 *
	 * @param s
	 *            The {@link Schema}, or {@code null}.
	 * @param hintName
	 *            Optional parameter or property name used for string examples.
	 * @param api
	 *            The root {@link OpenAPI} model.
	 * @return A Java literal string.
	 */
	private static String sampleValueForSchema(Schema s, String hintName, OpenAPI api) {
		if (s == null) {
			return defaultStringLiteral(hintName);
		}
		if (s.$ref != null && !s.$ref.isEmpty()) {
			Schema target = resolveComponentSchema(s.$ref, api);
			if (target != null) {
				return sampleValueForSchema(target, hintName, api);
			}
			String name = s.$ref.substring(s.$ref.lastIndexOf('/') + 1);
			return "new " + TypeMapper.sanitizeAlphanumeric(name) + "()";
		}
		if (s.example != null && !s.example.isNull()) {
			return formatPayloadValue(s.example);
		}
		if (s.defaultValue != null && !s.defaultValue.isNull()) {
			return formatPayloadValue(s.defaultValue);
		}
		if (s.enumValues != null && !s.enumValues.isEmpty()) {
			return formatPayloadValue(s.enumValues.get(0));
		}
		String t = s.type != null ? s.type.single : null;
		return sampleValueForTypeAndFormat(t, s.format, hintName);
	}

	/**
	 * Synthesizes a sample Java literal from an OpenAPI primitive {@code type} and
	 * {@code format}.
	 *
	 * @param type
	 *            The OpenAPI type name (e.g. {@code "integer"}, {@code "number"},
	 *            {@code "boolean"}, {@code "array"}, {@code "object"},
	 *            {@code "string"}).
	 * @param format
	 *            The optional OpenAPI format (e.g. {@code "int64"},
	 *            {@code "float"}).
	 * @param hintName
	 *            Optional parameter or field name for string literals.
	 * @return A Java literal expression string.
	 */
	private static String sampleValueForTypeAndFormat(String type, String format, String hintName) {
		if ("integer".equals(type)) {
			return "int64".equals(format) ? "1L" : "1";
		}
		if ("number".equals(type)) {
			return "float".equals(format) ? "1.0f" : "1.0";
		}
		if ("boolean".equals(type)) {
			return "true";
		}
		if ("array".equals(type)) {
			return "java.util.List.of()";
		}
		if ("object".equals(type)) {
			return Q + "{}" + Q;
		}
		return defaultStringLiteral(hintName);
	}

	/**
	 * Formats a {@link PayloadValue} as a Java code literal.
	 *
	 * @param pv
	 *            The {@link PayloadValue} to format.
	 * @return A Java literal string.
	 */
	private static String formatPayloadValue(PayloadValue pv) {
		if (pv.isNumber()) {
			return String.valueOf(pv.numberVal);
		}
		if (pv.isBoolean()) {
			return String.valueOf(pv.booleanVal);
		}
		if (pv.isArray()) {
			return "java.util.List.of()";
		}
		if (pv.isObject()) {
			return Q + "{}" + Q;
		}
		return Q + TypeMapper.escape(pv.stringVal) + Q;
	}

	/**
	 * Builds a default quoted Java string literal using {@code hintName} if
	 * non-empty, or {@code "example"} otherwise.
	 *
	 * @param hintName
	 *            Optional parameter or property name.
	 * @return A quoted Java string literal.
	 */
	private static String defaultStringLiteral(String hintName) {
		if (hintName != null && !hintName.isEmpty()) {
			return Q + TypeMapper.escape(hintName) + "_example" + Q;
		}
		return Q + "example" + Q;
	}
}
