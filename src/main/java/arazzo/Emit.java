package arazzo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import openapi.Schema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Emits Arazzo 1.1.0 workflow specifications (JSON and YAML) from the strongly
 * typed {@link Arazzo} AST.
 */
public class Emit {
	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Serializes an {@link Arazzo} model into a formatted JSON string.
	 *
	 * @param spec
	 *            The {@link Arazzo} model.
	 * @return The formatted JSON string.
	 */
	public static String toString(Arazzo spec) {
		return toJson(spec).toString(2);
	}

	/**
	 * Serializes an {@link Arazzo} model into a YAML string.
	 *
	 * @param spec
	 *            The {@link Arazzo} model.
	 * @return The formatted YAML string.
	 */
	public static String toYaml(Arazzo spec) {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);
		return yaml.dump(toJson(spec).toMap());
	}

	/**
	 * Writes an {@link Arazzo} model to a file (emitting YAML if the filename ends
	 * with {@code .yaml} or {@code .yml}, or JSON otherwise).
	 *
	 * @param spec
	 *            The {@link Arazzo} model.
	 * @param file
	 *            The destination file.
	 * @throws IOException
	 *             If writing fails.
	 */
	public static void toFile(Arazzo spec, File file) throws IOException {
		String name = file.getName().toLowerCase();
		String data = (name.endsWith(".yaml") || name.endsWith(".yml")) ? toYaml(spec) : toString(spec);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Serializes an {@link Arazzo} model into a root {@link JSONObject}.
	 *
	 * @param spec
	 *            The {@link Arazzo} model.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject toJson(Arazzo spec) {
		JSONObject root = new JSONObject();
		putIfNotNull(root, "arazzo", spec.arazzo);
		putIfNotNull(root, "$self", spec.$self);
		if (spec.info != null) {
			root.put("info", serializeInfo(spec.info));
		}
		if (isNonEmpty(spec.sourceDescriptions)) {
			JSONArray arr = new JSONArray();
			for (SourceDescription sd : spec.sourceDescriptions) {
				arr.put(serializeSourceDescription(sd));
			}
			root.put("sourceDescriptions", arr);
		}
		if (isNonEmpty(spec.workflows)) {
			JSONArray arr = new JSONArray();
			for (Workflow wf : spec.workflows) {
				arr.put(serializeWorkflow(wf));
			}
			root.put("workflows", arr);
		}
		if (spec.components != null) {
			JSONObject compObj = serializeComponents(spec.components);
			if (!compObj.isEmpty()) {
				root.put("components", compObj);
			}
		}
		putExtensions(root, spec.extensions);
		return root;
	}

	/**
	 * Serializes an {@link ArazzoInfo} object to JSON.
	 *
	 * @param info
	 *            The {@link ArazzoInfo} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeInfo(ArazzoInfo info) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "title", info.title);
		putIfNotNull(obj, "summary", info.summary);
		putIfNotNull(obj, "description", info.description);
		putIfNotNull(obj, "version", info.version);
		putExtensions(obj, info.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link SourceDescription} object to JSON.
	 *
	 * @param sd
	 *            The {@link SourceDescription} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeSourceDescription(SourceDescription sd) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "name", sd.name);
		putIfNotNull(obj, "url", sd.url);
		if (sd.type != null) {
			obj.put("type", sd.type.getValue());
		}
		putExtensions(obj, sd.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link Workflow} object to JSON.
	 *
	 * @param wf
	 *            The {@link Workflow} object.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeWorkflow(Workflow wf) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "workflowId", wf.workflowId);
		putIfNotNull(obj, "summary", wf.summary);
		putIfNotNull(obj, "description", wf.description);
		if (wf.inputs != null) {
			if (wf.inputs.isRef()) {
				JSONObject refObj = new JSONObject();
				refObj.put("$ref", wf.inputs.ref.ref);
				obj.put("inputs", refObj);
			} else if (wf.inputs.isValue()) {
				obj.put("inputs", openapi.Emit.serializeSchema(wf.inputs.value));
			}
		}
		putNonEmptyList(obj, "dependsOn", wf.dependsOn);
		if (isNonEmpty(wf.steps)) {
			JSONArray arr = new JSONArray();
			for (Step s : wf.steps) {
				arr.put(serializeStep(s));
			}
			obj.put("steps", arr);
		}
		if (isNonEmpty(wf.successActions)) {
			obj.put("successActions", serializeSuccessActionList(wf.successActions));
		}
		if (isNonEmpty(wf.failureActions)) {
			obj.put("failureActions", serializeFailureActionList(wf.failureActions));
		}
		if (isNonEmpty(wf.outputs)) {
			obj.put("outputs", serializeOutputValueMap(wf.outputs));
		}
		if (isNonEmpty(wf.parameters)) {
			obj.put("parameters", serializeParameterList(wf.parameters));
		}
		putExtensions(obj, wf.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link Step} object to JSON.
	 *
	 * @param s
	 *            The {@link Step} object.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeStep(Step s) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "stepId", s.stepId);
		putIfNotNull(obj, "description", s.description);
		putIfNotNull(obj, "operationId", s.operationId);
		putIfNotNull(obj, "operationPath", s.operationPath);
		putIfNotNull(obj, "channelPath", s.channelPath);
		putIfNotNull(obj, "workflowId", s.workflowId);
		if (isNonEmpty(s.parameters)) {
			obj.put("parameters", serializeParameterList(s.parameters));
		}
		if (s.requestBody != null) {
			obj.put("requestBody", serializeRequestBody(s.requestBody));
		}
		if (isNonEmpty(s.successCriteria)) {
			obj.put("successCriteria", serializeCriteriaList(s.successCriteria));
		}
		if (isNonEmpty(s.onSuccess)) {
			obj.put("onSuccess", serializeSuccessActionList(s.onSuccess));
		}
		if (isNonEmpty(s.onFailure)) {
			obj.put("onFailure", serializeFailureActionList(s.onFailure));
		}
		if (isNonEmpty(s.outputs)) {
			obj.put("outputs", serializeOutputValueMap(s.outputs));
		}
		putIfNotNull(obj, "timeout", s.timeout);
		putIfNotNull(obj, "correlationId", s.correlationId);
		if (s.action != null) {
			obj.put("action", s.action.getValue());
		}
		putNonEmptyList(obj, "dependsOn", s.dependsOn);
		putExtensions(obj, s.extensions);
		return obj;
	}

	/**
	 * Serializes a map of {@link OutputValue} entries to a {@link JSONObject}.
	 *
	 * @param map
	 *            The output value map.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeOutputValueMap(Map<String, OutputValue> map) {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, OutputValue> e : map.entrySet()) {
			OutputValue val = e.getValue();
			if (val.isSelector()) {
				obj.put(e.getKey(), serializeSelector(val.selector));
			} else {
				putIfNotNull(obj, e.getKey(), val.expression);
			}
		}
		return obj;
	}

	/**
	 * Serializes a {@link Selector} object to JSON.
	 *
	 * @param sel
	 *            The {@link Selector} object.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeSelector(Selector sel) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "context", sel.context);
		putIfNotNull(obj, "selector", sel.selector);
		if (sel.type != null) {
			obj.put("type", serializeCriterionTypeOrExpression(sel.type));
		}
		putExtensions(obj, sel.extensions);
		return obj;
	}

	/**
	 * Serializes a list of {@link ReusableOr} wrapping {@link ArazzoParameter}
	 * items.
	 *
	 * @param list
	 *            The parameter list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeParameterList(List<ReusableOr<ArazzoParameter>> list) {
		JSONArray arr = new JSONArray();
		for (ReusableOr<ArazzoParameter> item : list) {
			if (item.isReusable()) {
				arr.put(serializeReusableObject(item.reusable));
			} else if (item.isItem()) {
				arr.put(serializeParameter(item.item));
			}
		}
		return arr;
	}

	/**
	 * Serializes a single {@link ArazzoParameter} to JSON.
	 *
	 * @param p
	 *            The {@link ArazzoParameter}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeParameter(ArazzoParameter p) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "name", p.name);
		if (p.in != null) {
			obj.put("in", p.in.getValue());
		}
		if (p.value != null) {
			obj.put("value", serializePayloadValue(p.value));
		}
		putExtensions(obj, p.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link ReusableObject} to JSON.
	 *
	 * @param r
	 *            The {@link ReusableObject}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeReusableObject(ReusableObject r) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "reference", r.reference);
		if (r.value != null) {
			obj.put("value", serializePayloadValue(r.value));
		}
		return obj;
	}

	/**
	 * Serializes a list of {@link ReusableOr} wrapping {@link SuccessAction} items.
	 *
	 * @param list
	 *            The success action list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeSuccessActionList(List<ReusableOr<SuccessAction>> list) {
		JSONArray arr = new JSONArray();
		for (ReusableOr<SuccessAction> item : list) {
			if (item.isReusable()) {
				arr.put(serializeReusableObject(item.reusable));
			} else if (item.isItem()) {
				arr.put(serializeSuccessAction(item.item));
			}
		}
		return arr;
	}

	/**
	 * Serializes a single {@link SuccessAction} to JSON.
	 *
	 * @param sa
	 *            The {@link SuccessAction}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeSuccessAction(SuccessAction sa) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "name", sa.name);
		if (sa.type != null) {
			obj.put("type", sa.type.getValue());
		}
		putIfNotNull(obj, "workflowId", sa.workflowId);
		putIfNotNull(obj, "stepId", sa.stepId);
		if (isNonEmpty(sa.parameters)) {
			obj.put("parameters", serializeParameterList(sa.parameters));
		}
		if (isNonEmpty(sa.criteria)) {
			obj.put("criteria", serializeCriteriaList(sa.criteria));
		}
		putExtensions(obj, sa.extensions);
		return obj;
	}

	/**
	 * Serializes a list of {@link ReusableOr} wrapping {@link FailureAction} items.
	 *
	 * @param list
	 *            The failure action list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeFailureActionList(List<ReusableOr<FailureAction>> list) {
		JSONArray arr = new JSONArray();
		for (ReusableOr<FailureAction> item : list) {
			if (item.isReusable()) {
				arr.put(serializeReusableObject(item.reusable));
			} else if (item.isItem()) {
				arr.put(serializeFailureAction(item.item));
			}
		}
		return arr;
	}

	/**
	 * Serializes a single {@link FailureAction} to JSON.
	 *
	 * @param fa
	 *            The {@link FailureAction}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeFailureAction(FailureAction fa) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "name", fa.name);
		if (fa.type != null) {
			obj.put("type", fa.type.getValue());
		}
		putIfNotNull(obj, "workflowId", fa.workflowId);
		putIfNotNull(obj, "stepId", fa.stepId);
		if (isNonEmpty(fa.parameters)) {
			obj.put("parameters", serializeParameterList(fa.parameters));
		}
		putIfNotNull(obj, "retryAfter", fa.retryAfter);
		putIfNotNull(obj, "retryLimit", fa.retryLimit);
		if (isNonEmpty(fa.criteria)) {
			obj.put("criteria", serializeCriteriaList(fa.criteria));
		}
		putExtensions(obj, fa.extensions);
		return obj;
	}

	/**
	 * Serializes a list of {@link Criterion} objects to a {@link JSONArray}.
	 *
	 * @param list
	 *            The criteria list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeCriteriaList(List<Criterion> list) {
		JSONArray arr = new JSONArray();
		for (Criterion c : list) {
			arr.put(serializeCriterion(c));
		}
		return arr;
	}

	/**
	 * Serializes a single {@link Criterion} to JSON.
	 *
	 * @param c
	 *            The {@link Criterion}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeCriterion(Criterion c) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "context", c.context);
		putIfNotNull(obj, "condition", c.condition);
		if (c.type != null) {
			obj.put("type", serializeCriterionTypeOrExpression(c.type));
		}
		putExtensions(obj, c.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link CriterionTypeOrExpression} to its wire representation
	 * (string or {@link JSONObject}).
	 *
	 * @param cte
	 *            The {@link CriterionTypeOrExpression}.
	 * @return The serialized wire value.
	 */
	private static Object serializeCriterionTypeOrExpression(CriterionTypeOrExpression cte) {
		if (cte.isExpressionType()) {
			JSONObject eo = new JSONObject();
			if (cte.expressionType.type != null) {
				eo.put("type", cte.expressionType.type.getValue());
			}
			putIfNotNull(eo, "version", cte.expressionType.version);
			putExtensions(eo, cte.expressionType.extensions);
			return eo;
		}
		return cte.simpleType != null ? cte.simpleType.getValue() : "simple";
	}

	/**
	 * Serializes an {@link ArazzoRequestBody} to JSON.
	 *
	 * @param rb
	 *            The {@link ArazzoRequestBody}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeRequestBody(ArazzoRequestBody rb) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "contentType", rb.contentType);
		if (rb.payload != null) {
			obj.put("payload", serializePayloadValue(rb.payload));
		}
		if (isNonEmpty(rb.replacements)) {
			JSONArray arr = new JSONArray();
			for (PayloadReplacement pr : rb.replacements) {
				arr.put(serializePayloadReplacement(pr));
			}
			obj.put("replacements", arr);
		}
		putExtensions(obj, rb.extensions);
		return obj;
	}

	/**
	 * Serializes a single {@link PayloadReplacement} to JSON.
	 *
	 * @param pr
	 *            The {@link PayloadReplacement}.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializePayloadReplacement(PayloadReplacement pr) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "target", pr.target);
		if (pr.targetSelectorType != null) {
			obj.put("targetSelectorType", serializeCriterionTypeOrExpression(pr.targetSelectorType));
		}
		if (pr.value != null) {
			obj.put("value", serializePayloadValue(pr.value));
		}
		putExtensions(obj, pr.extensions);
		return obj;
	}

	/**
	 * Serializes an {@link ArazzoComponents} object to JSON.
	 *
	 * @param comp
	 *            The {@link ArazzoComponents}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeComponents(ArazzoComponents comp) {
		JSONObject obj = new JSONObject();
		if (isNonEmpty(comp.inputs)) {
			JSONObject inMap = new JSONObject();
			for (Map.Entry<String, Schema> e : comp.inputs.entrySet()) {
				inMap.put(e.getKey(), openapi.Emit.serializeSchema(e.getValue()));
			}
			obj.put("inputs", inMap);
		}
		if (isNonEmpty(comp.parameters)) {
			JSONObject pMap = new JSONObject();
			for (Map.Entry<String, ArazzoParameter> e : comp.parameters.entrySet()) {
				pMap.put(e.getKey(), serializeParameter(e.getValue()));
			}
			obj.put("parameters", pMap);
		}
		if (isNonEmpty(comp.successActions)) {
			JSONObject saMap = new JSONObject();
			for (Map.Entry<String, SuccessAction> e : comp.successActions.entrySet()) {
				saMap.put(e.getKey(), serializeSuccessAction(e.getValue()));
			}
			obj.put("successActions", saMap);
		}
		if (isNonEmpty(comp.failureActions)) {
			JSONObject faMap = new JSONObject();
			for (Map.Entry<String, FailureAction> e : comp.failureActions.entrySet()) {
				faMap.put(e.getKey(), serializeFailureAction(e.getValue()));
			}
			obj.put("failureActions", faMap);
		}
		putExtensions(obj, comp.extensions);
		return obj;
	}

	/**
	 * Recursively converts a strongly typed {@link PayloadValue} into a
	 * {@link org.json} compatible object.
	 *
	 * @param pv
	 *            The {@link PayloadValue}.
	 * @return The serialized JSON value.
	 */
	public static Object serializePayloadValue(PayloadValue pv) {
		if (pv.isNull()) {
			return JSONObject.NULL;
		}
		if (pv.isBoolean()) {
			return pv.booleanVal;
		}
		if (pv.isNumber()) {
			return pv.numberVal;
		}
		if (pv.isObject()) {
			JSONObject jo = new JSONObject();
			for (Map.Entry<String, PayloadValue> e : pv.objectVal.entrySet()) {
				jo.put(e.getKey(), serializePayloadValue(e.getValue()));
			}
			return jo;
		}
		if (pv.isArray()) {
			JSONArray ja = new JSONArray();
			for (PayloadValue item : pv.arrayVal) {
				ja.put(serializePayloadValue(item));
			}
			return ja;
		}
		return pv.stringVal;
	}

	/**
	 * Checks whether a collection is non-null and non-empty.
	 *
	 * @param c
	 *            The collection to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	private static boolean isNonEmpty(Collection<?> c) {
		return c != null && !c.isEmpty();
	}

	/**
	 * Checks whether a map is non-null and non-empty.
	 *
	 * @param m
	 *            The map to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	private static boolean isNonEmpty(Map<?, ?> m) {
		return m != null && !m.isEmpty();
	}

	/**
	 * Puts a value into a {@link JSONObject} if the value is non-null.
	 *
	 * @param obj
	 *            The target JSON object.
	 * @param key
	 *            The property key.
	 * @param val
	 *            The value to put.
	 */
	private static void putIfNotNull(JSONObject obj, String key, Object val) {
		if (val != null) {
			obj.put(key, val);
		}
	}

	/**
	 * Puts a list into a {@link JSONObject} if the list is non-null and non-empty.
	 *
	 * @param obj
	 *            The target JSON object.
	 * @param key
	 *            The property key.
	 * @param list
	 *            The list to put.
	 */
	private static void putNonEmptyList(JSONObject obj, String key, List<?> list) {
		if (isNonEmpty(list)) {
			obj.put(key, new JSONArray(list));
		}
	}

	/**
	 * Puts all vendor extensions into a {@link JSONObject}.
	 *
	 * @param obj
	 *            The target JSON object.
	 * @param extensions
	 *            The extensions map.
	 */
	private static void putExtensions(JSONObject obj, Map<String, Object> extensions) {
		if (isNonEmpty(extensions)) {
			for (Map.Entry<String, Object> e : extensions.entrySet()) {
				obj.put(e.getKey(), e.getValue());
			}
		}
	}
}
