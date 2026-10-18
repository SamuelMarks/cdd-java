package arazzo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import openapi.Reference;
import openapi.ReferenceOr;
import openapi.Schema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

/**
 * Parses Arazzo 1.1.0 workflow specifications (JSON or YAML) into the strongly
 * typed {@link Arazzo} AST.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses an Arazzo 1.1.0 specification from a JSON or YAML string.
	 *
	 * @param content
	 *            The raw JSON or YAML string.
	 * @return The parsed {@link Arazzo} AST model.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static Arazzo fromString(String content) throws IOException {
		try {
			String trimmed = content.trim();
			JSONObject root;
			if (trimmed.startsWith("{")) {
				root = new JSONObject(trimmed);
			} else {
				Yaml yaml = new Yaml();
				Map<String, Object> map = yaml.load(content);
				root = new JSONObject(map);
			}
			return fromJson(root);
		} catch (Exception e) {
			throw new IOException("Failed to parse Arazzo: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses an Arazzo 1.1.0 specification from a file.
	 *
	 * @param file
	 *            The JSON or YAML file.
	 * @return The parsed {@link Arazzo} AST model.
	 * @throws IOException
	 *             If reading or parsing fails.
	 */
	public static Arazzo fromFile(File file) throws IOException {
		String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
		return fromString(content);
	}

	/**
	 * Parses an Arazzo 1.1.0 specification from a root {@link JSONObject}.
	 *
	 * @param root
	 *            The root {@link JSONObject}.
	 * @return The parsed {@link Arazzo} AST model.
	 */
	public static Arazzo fromJson(JSONObject root) {
		Arazzo spec = new Arazzo();
		String ver = optStr(root, "arazzo");
		if (ver != null) {
			spec.arazzo = ver;
		}
		spec.$self = optStr(root, "$self");
		if (root.has("info")) {
			spec.info = parseInfo(root.getJSONObject("info"));
		}
		if (root.has("sourceDescriptions")) {
			spec.sourceDescriptions = parseSourceDescriptions(root.getJSONArray("sourceDescriptions"));
		}
		if (root.has("workflows")) {
			spec.workflows = parseWorkflows(root.getJSONArray("workflows"));
		}
		if (root.has("components")) {
			spec.components = parseComponents(root.getJSONObject("components"));
		}
		copyExtensions(root, spec.extensions);
		return spec;
	}

	/**
	 * Parses an {@link ArazzoInfo} object from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link ArazzoInfo}.
	 */
	private static ArazzoInfo parseInfo(JSONObject obj) {
		ArazzoInfo info = new ArazzoInfo();
		info.title = optStr(obj, "title");
		info.summary = optStr(obj, "summary");
		info.description = optStr(obj, "description");
		info.version = optStr(obj, "version");
		copyExtensions(obj, info.extensions);
		return info;
	}

	/**
	 * Parses a list of {@link SourceDescription} objects from a {@link JSONArray}.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list of {@link SourceDescription} objects.
	 */
	private static List<SourceDescription> parseSourceDescriptions(JSONArray arr) {
		List<SourceDescription> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			SourceDescription sd = new SourceDescription();
			sd.name = optStr(obj, "name");
			sd.url = optStr(obj, "url");
			sd.type = SourceType.fromString(optStr(obj, "type"));
			copyExtensions(obj, sd.extensions);
			list.add(sd);
		}
		return list;
	}

	/**
	 * Parses a list of {@link Workflow} objects from a {@link JSONArray}.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list of {@link Workflow} objects.
	 */
	private static List<Workflow> parseWorkflows(JSONArray arr) {
		List<Workflow> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(parseWorkflow(arr.getJSONObject(i)));
		}
		return list;
	}

	/**
	 * Parses a single {@link Workflow} object from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link Workflow}.
	 */
	public static Workflow parseWorkflow(JSONObject obj) {
		Workflow wf = new Workflow();
		wf.workflowId = optStr(obj, "workflowId");
		wf.summary = optStr(obj, "summary");
		wf.description = optStr(obj, "description");
		if (obj.has("inputs")) {
			JSONObject inObj = obj.getJSONObject("inputs");
			String ref = optStr(inObj, "$ref");
			if (ref == null) {
				ref = optStr(inObj, "reference");
			}
			if (ref != null && inObj.keySet().size() == 1) {
				Reference r = new Reference();
				r.ref = ref;
				wf.inputs = ReferenceOr.ofRef(r);
			} else {
				wf.inputs = ReferenceOr.ofValue(openapi.Parse.parseSchema(inObj));
			}
		}
		wf.dependsOn = optStrList(obj, "dependsOn");
		if (obj.has("steps")) {
			JSONArray stepsArr = obj.getJSONArray("steps");
			wf.steps = new ArrayList<>();
			for (int i = 0; i < stepsArr.length(); i++) {
				wf.steps.add(parseStep(stepsArr.getJSONObject(i)));
			}
		}
		if (obj.has("successActions")) {
			wf.successActions = parseSuccessActionList(obj.getJSONArray("successActions"));
		}
		if (obj.has("failureActions")) {
			wf.failureActions = parseFailureActionList(obj.getJSONArray("failureActions"));
		}
		if (obj.has("outputs")) {
			wf.outputs = parseOutputValueMap(obj.getJSONObject("outputs"));
		}
		if (obj.has("parameters")) {
			wf.parameters = parseParameterList(obj.getJSONArray("parameters"));
		}
		copyExtensions(obj, wf.extensions);
		return wf;
	}

	/**
	 * Parses a single {@link Step} object from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link Step}.
	 */
	public static Step parseStep(JSONObject obj) {
		Step s = new Step();
		s.stepId = optStr(obj, "stepId");
		s.description = optStr(obj, "description");
		s.operationId = optStr(obj, "operationId");
		s.operationPath = optStr(obj, "operationPath");
		s.channelPath = optStr(obj, "channelPath");
		s.workflowId = optStr(obj, "workflowId");
		if (obj.has("parameters")) {
			s.parameters = parseParameterList(obj.getJSONArray("parameters"));
		}
		if (obj.has("requestBody")) {
			s.requestBody = parseRequestBody(obj.getJSONObject("requestBody"));
		}
		if (obj.has("successCriteria")) {
			s.successCriteria = parseCriteriaList(obj.getJSONArray("successCriteria"));
		}
		if (obj.has("onSuccess")) {
			s.onSuccess = parseSuccessActionList(obj.getJSONArray("onSuccess"));
		}
		if (obj.has("onFailure")) {
			s.onFailure = parseFailureActionList(obj.getJSONArray("onFailure"));
		}
		if (obj.has("outputs")) {
			s.outputs = parseOutputValueMap(obj.getJSONObject("outputs"));
		}
		s.timeout = optInt(obj, "timeout");
		s.correlationId = optStr(obj, "correlationId");
		s.action = StepActionType.fromString(optStr(obj, "action"));
		s.dependsOn = optStrList(obj, "dependsOn");
		copyExtensions(obj, s.extensions);
		return s;
	}

	/**
	 * Parses a map of {@link OutputValue} entries from a {@link JSONObject}.
	 *
	 * @param obj
	 *            The JSON object mapping output names to expressions or selectors.
	 * @return The parsed {@link OutputValue} map.
	 */
	private static Map<String, OutputValue> parseOutputValueMap(JSONObject obj) {
		Map<String, OutputValue> map = new LinkedHashMap<>();
		for (String k : obj.keySet()) {
			Object raw = obj.get(k);
			if (raw instanceof JSONObject) {
				map.put(k, OutputValue.ofSelector(parseSelector((JSONObject) raw)));
			} else {
				map.put(k, OutputValue.ofExpression(String.valueOf(raw)));
			}
		}
		return map;
	}

	/**
	 * Parses a {@link Selector} object from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link Selector}.
	 */
	public static Selector parseSelector(JSONObject obj) {
		Selector sel = new Selector();
		sel.context = optStr(obj, "context");
		sel.selector = optStr(obj, "selector");
		if (!obj.isNull("type")) {
			sel.type = parseCriterionTypeOrExpression(obj.get("type"));
		}
		copyExtensions(obj, sel.extensions);
		return sel;
	}

	/**
	 * Parses a list of {@link ReusableOr} wrapping {@link ArazzoParameter} items.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list.
	 */
	private static List<ReusableOr<ArazzoParameter>> parseParameterList(JSONArray arr) {
		List<ReusableOr<ArazzoParameter>> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			if (obj.has("reference")) {
				list.add(ReusableOr.ofReusable(parseReusableObject(obj)));
			} else {
				list.add(ReusableOr.ofItem(parseParameter(obj)));
			}
		}
		return list;
	}

	/**
	 * Parses a single {@link ArazzoParameter} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link ArazzoParameter}.
	 */
	public static ArazzoParameter parseParameter(JSONObject obj) {
		ArazzoParameter p = new ArazzoParameter();
		p.name = optStr(obj, "name");
		p.in = ParameterIn.fromString(optStr(obj, "in"));
		if (obj.has("value")) {
			p.value = parsePayloadValue(obj.get("value"));
		}
		copyExtensions(obj, p.extensions);
		return p;
	}

	/**
	 * Parses a {@link ReusableObject} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link ReusableObject}.
	 */
	public static ReusableObject parseReusableObject(JSONObject obj) {
		ReusableObject r = new ReusableObject();
		r.reference = optStr(obj, "reference");
		if (obj.has("value")) {
			r.value = parsePayloadValue(obj.get("value"));
		}
		return r;
	}

	/**
	 * Parses a list of {@link ReusableOr} wrapping {@link SuccessAction} items.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list.
	 */
	private static List<ReusableOr<SuccessAction>> parseSuccessActionList(JSONArray arr) {
		List<ReusableOr<SuccessAction>> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			if (obj.has("reference")) {
				list.add(ReusableOr.ofReusable(parseReusableObject(obj)));
			} else {
				list.add(ReusableOr.ofItem(parseSuccessAction(obj)));
			}
		}
		return list;
	}

	/**
	 * Parses a single {@link SuccessAction} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link SuccessAction}.
	 */
	public static SuccessAction parseSuccessAction(JSONObject obj) {
		SuccessAction sa = new SuccessAction();
		sa.name = optStr(obj, "name");
		sa.type = SuccessActionType.fromString(optStr(obj, "type"));
		sa.workflowId = optStr(obj, "workflowId");
		sa.stepId = optStr(obj, "stepId");
		if (obj.has("parameters")) {
			sa.parameters = parseParameterList(obj.getJSONArray("parameters"));
		}
		if (obj.has("criteria")) {
			sa.criteria = parseCriteriaList(obj.getJSONArray("criteria"));
		}
		copyExtensions(obj, sa.extensions);
		return sa;
	}

	/**
	 * Parses a list of {@link ReusableOr} wrapping {@link FailureAction} items.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list.
	 */
	private static List<ReusableOr<FailureAction>> parseFailureActionList(JSONArray arr) {
		List<ReusableOr<FailureAction>> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			if (obj.has("reference")) {
				list.add(ReusableOr.ofReusable(parseReusableObject(obj)));
			} else {
				list.add(ReusableOr.ofItem(parseFailureAction(obj)));
			}
		}
		return list;
	}

	/**
	 * Parses a single {@link FailureAction} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link FailureAction}.
	 */
	public static FailureAction parseFailureAction(JSONObject obj) {
		FailureAction fa = new FailureAction();
		fa.name = optStr(obj, "name");
		fa.type = FailureActionType.fromString(optStr(obj, "type"));
		fa.workflowId = optStr(obj, "workflowId");
		fa.stepId = optStr(obj, "stepId");
		if (obj.has("parameters")) {
			fa.parameters = parseParameterList(obj.getJSONArray("parameters"));
		}
		fa.retryAfter = optDouble(obj, "retryAfter");
		fa.retryLimit = optInt(obj, "retryLimit");
		if (obj.has("criteria")) {
			fa.criteria = parseCriteriaList(obj.getJSONArray("criteria"));
		}
		copyExtensions(obj, fa.extensions);
		return fa;
	}

	/**
	 * Parses a list of {@link Criterion} objects from a {@link JSONArray}.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The parsed list of {@link Criterion} objects.
	 */
	private static List<Criterion> parseCriteriaList(JSONArray arr) {
		List<Criterion> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(parseCriterion(arr.getJSONObject(i)));
		}
		return list;
	}

	/**
	 * Parses a single {@link Criterion} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link Criterion}.
	 */
	public static Criterion parseCriterion(JSONObject obj) {
		Criterion c = new Criterion();
		c.context = optStr(obj, "context");
		c.condition = optStr(obj, "condition");
		if (!obj.isNull("type")) {
			c.type = parseCriterionTypeOrExpression(obj.get("type"));
		}
		copyExtensions(obj, c.extensions);
		return c;
	}

	/**
	 * Parses a {@link CriterionTypeOrExpression} from a raw JSON value (string or
	 * {@link JSONObject}).
	 *
	 * @param raw
	 *            The raw JSON value.
	 * @return The parsed {@link CriterionTypeOrExpression}.
	 */
	private static CriterionTypeOrExpression parseCriterionTypeOrExpression(Object raw) {
		if (raw instanceof JSONObject) {
			JSONObject eo = (JSONObject) raw;
			ExpressionType et = new ExpressionType();
			et.type = CriterionEngineType.fromString(optStr(eo, "type"));
			et.version = optStr(eo, "version");
			copyExtensions(eo, et.extensions);
			return CriterionTypeOrExpression.ofExpressionType(et);
		}
		return CriterionTypeOrExpression.ofSimple(CriterionEngineType.fromString(String.valueOf(raw)));
	}

	/**
	 * Parses an {@link ArazzoRequestBody} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link ArazzoRequestBody}.
	 */
	public static ArazzoRequestBody parseRequestBody(JSONObject obj) {
		ArazzoRequestBody rb = new ArazzoRequestBody();
		rb.contentType = optStr(obj, "contentType");
		if (obj.has("payload")) {
			rb.payload = parsePayloadValue(obj.get("payload"));
		}
		if (obj.has("replacements")) {
			JSONArray arr = obj.getJSONArray("replacements");
			rb.replacements = new ArrayList<>();
			for (int i = 0; i < arr.length(); i++) {
				rb.replacements.add(parsePayloadReplacement(arr.getJSONObject(i)));
			}
		}
		copyExtensions(obj, rb.extensions);
		return rb;
	}

	/**
	 * Parses a single {@link PayloadReplacement} from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link PayloadReplacement}.
	 */
	public static PayloadReplacement parsePayloadReplacement(JSONObject obj) {
		PayloadReplacement pr = new PayloadReplacement();
		pr.target = optStr(obj, "target");
		if (!obj.isNull("targetSelectorType")) {
			pr.targetSelectorType = parseCriterionTypeOrExpression(obj.get("targetSelectorType"));
		}
		if (obj.has("value")) {
			pr.value = parsePayloadValue(obj.get("value"));
		}
		copyExtensions(obj, pr.extensions);
		return pr;
	}

	/**
	 * Parses an {@link ArazzoComponents} object from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed {@link ArazzoComponents}.
	 */
	private static ArazzoComponents parseComponents(JSONObject obj) {
		ArazzoComponents comp = new ArazzoComponents();
		if (obj.has("inputs")) {
			JSONObject inMap = obj.getJSONObject("inputs");
			comp.inputs = new LinkedHashMap<>();
			for (String k : inMap.keySet()) {
				comp.inputs.put(k, openapi.Parse.parseSchema(inMap.getJSONObject(k)));
			}
		}
		if (obj.has("parameters")) {
			JSONObject pMap = obj.getJSONObject("parameters");
			comp.parameters = new LinkedHashMap<>();
			for (String k : pMap.keySet()) {
				comp.parameters.put(k, parseParameter(pMap.getJSONObject(k)));
			}
		}
		if (obj.has("successActions")) {
			JSONObject saMap = obj.getJSONObject("successActions");
			comp.successActions = new LinkedHashMap<>();
			for (String k : saMap.keySet()) {
				comp.successActions.put(k, parseSuccessAction(saMap.getJSONObject(k)));
			}
		}
		if (obj.has("failureActions")) {
			JSONObject faMap = obj.getJSONObject("failureActions");
			comp.failureActions = new LinkedHashMap<>();
			for (String k : faMap.keySet()) {
				comp.failureActions.put(k, parseFailureAction(faMap.getJSONObject(k)));
			}
		}
		copyExtensions(obj, comp.extensions);
		return comp;
	}

	/**
	 * Recursively converts a raw {@link org.json} value into a strongly typed
	 * {@link PayloadValue}.
	 *
	 * @param raw
	 *            The raw JSON value ({@link JSONObject#NULL}, {@link String},
	 *            {@link Number}, {@link Boolean}, {@link JSONObject}, or
	 *            {@link JSONArray}).
	 * @return The parsed {@link PayloadValue}.
	 */
	public static PayloadValue parsePayloadValue(Object raw) {
		if (raw == null || JSONObject.NULL.equals(raw)) {
			return PayloadValue.ofNull();
		}
		if (raw instanceof Boolean) {
			return PayloadValue.ofBoolean((Boolean) raw);
		}
		if (raw instanceof Number) {
			return PayloadValue.ofNumber((Number) raw);
		}
		if (raw instanceof JSONObject) {
			JSONObject jo = (JSONObject) raw;
			Map<String, PayloadValue> map = new LinkedHashMap<>();
			for (String k : jo.keySet()) {
				map.put(k, parsePayloadValue(jo.get(k)));
			}
			return PayloadValue.ofObject(map);
		}
		if (raw instanceof JSONArray) {
			JSONArray ja = (JSONArray) raw;
			List<PayloadValue> list = new ArrayList<>();
			for (int i = 0; i < ja.length(); i++) {
				list.add(parsePayloadValue(ja.get(i)));
			}
			return PayloadValue.ofArray(list);
		}
		return PayloadValue.ofString(String.valueOf(raw));
	}

	/**
	 * Extracts an optional string property from a {@link JSONObject}.
	 *
	 * @param obj
	 *            The JSON object.
	 * @param key
	 *            The property key.
	 * @return The string value, or {@code null} if absent or null.
	 */
	private static String optStr(JSONObject obj, String key) {
		return obj.isNull(key) ? null : obj.getString(key);
	}

	/**
	 * Extracts an optional integer property from a {@link JSONObject}.
	 *
	 * @param obj
	 *            The JSON object.
	 * @param key
	 *            The property key.
	 * @return The integer value, or {@code null} if absent or null.
	 */
	private static Integer optInt(JSONObject obj, String key) {
		return obj.isNull(key) ? null : obj.getInt(key);
	}

	/**
	 * Extracts an optional double property from a {@link JSONObject}.
	 *
	 * @param obj
	 *            The JSON object.
	 * @param key
	 *            The property key.
	 * @return The double value, or {@code null} if absent or null.
	 */
	private static Double optDouble(JSONObject obj, String key) {
		return obj.isNull(key) ? null : obj.getDouble(key);
	}

	/**
	 * Extracts an optional list of strings from a {@link JSONObject}.
	 *
	 * @param obj
	 *            The JSON object.
	 * @param key
	 *            The property key.
	 * @return The list of strings, or {@code null} if absent or null.
	 */
	private static List<String> optStrList(JSONObject obj, String key) {
		if (obj.isNull(key)) {
			return null;
		}
		JSONArray arr = obj.getJSONArray(key);
		List<String> res = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			res.add(arr.getString(i));
		}
		return res;
	}

	/**
	 * Copies all {@code x-} vendor extension properties from a {@link JSONObject}
	 * into the target map.
	 *
	 * @param src
	 *            The source JSON object.
	 * @param target
	 *            The target extensions map.
	 */
	private static void copyExtensions(JSONObject src, Map<String, Object> target) {
		for (String k : src.keySet()) {
			if (k.startsWith("x-")) {
				Object v = src.get(k);
				if (v instanceof JSONObject) {
					target.put(k, ((JSONObject) v).toMap());
				} else if (v instanceof JSONArray) {
					target.put(k, ((JSONArray) v).toList());
				} else {
					target.put(k, v);
				}
			}
		}
	}
}
