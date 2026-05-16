package openapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;

/**
 * Emits OpenAPI descriptions.
 */
public class Emit {
	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits OpenAPI description to string.
	 * 
	 * @param api
	 *            The OpenAPI object.
	 * @return JSON string.
	 */
	public static String toString(OpenAPI api) {
		JSONObject root = new JSONObject();
		if (api.openapi != null)
			root.put("openapi", api.openapi);

		if (api.swagger != null)
			root.put("swagger", api.swagger);
		if (api.host != null)
			root.put("host", api.host);
		if (api.basePath != null)
			root.put("basePath", api.basePath);
		if (api.schemes != null && !api.schemes.isEmpty()) {
			JSONArray arr = new JSONArray();
			for (String s : api.schemes)
				arr.put(s);
			root.put("schemes", arr);
		}
		if (api.consumes != null && !api.consumes.isEmpty()) {
			JSONArray arr = new JSONArray();
			for (String s : api.consumes)
				arr.put(s);
			root.put("consumes", arr);
		}
		if (api.produces != null && !api.produces.isEmpty()) {
			JSONArray arr = new JSONArray();
			for (String s : api.produces)
				arr.put(s);
			root.put("produces", arr);
		}

		if (api.info != null) {
			JSONObject infoObj = new JSONObject();
			if (api.info.title != null)
				infoObj.put("title", api.info.title);
			if (api.info.version != null)
				infoObj.put("version", api.info.version);
			if (api.info.description != null)
				infoObj.put("description", api.info.description);
			root.put("info", infoObj);
		}

		if (api.paths != null && api.paths.pathItems != null) {
			JSONObject pathsObj = new JSONObject();
			for (Map.Entry<String, PathItem> e : api.paths.pathItems.entrySet()) {
				PathItem pi = e.getValue();
				JSONObject piObj = new JSONObject();
				if (pi.get != null)
					piObj.put("get", serializeOperation(pi.get));
				if (pi.post != null)
					piObj.put("post", serializeOperation(pi.post));
				if (pi.put != null)
					piObj.put("put", serializeOperation(pi.put));
				if (pi.delete != null)
					piObj.put("delete", serializeOperation(pi.delete));
				if (pi.patch != null)
					piObj.put("patch", serializeOperation(pi.patch));

				if (pi.parameters != null && !pi.parameters.isEmpty()) {
					piObj.put("parameters", serializeParameters(pi.parameters));
				}

				pathsObj.put(e.getKey(), piObj);
			}
			root.put("paths", pathsObj);
		}

		if (api.definitions != null) {
			JSONObject defsObj = new JSONObject();
			for (Map.Entry<String, Schema> e : api.definitions.entrySet()) {
				if (e.getValue() != null) {
					defsObj.put(e.getKey(), serializeSchema(e.getValue()));
				}
			}
			root.put("definitions", defsObj);
		}

		if (api.components != null && api.components.schemas != null) {
			JSONObject compObj = new JSONObject();
			JSONObject schemasObj = new JSONObject();
			for (Map.Entry<String, Schema> e : api.components.schemas.entrySet()) {
				if (e.getValue() != null) {
					schemasObj.put(e.getKey(), serializeSchema(e.getValue()));
				}
			}
			compObj.put("schemas", schemasObj);
			root.put("components", compObj);
		}

		return root.toString(2);
	}

	private static JSONObject serializeOperation(Operation op) {
		JSONObject obj = new JSONObject();
		if (op.operationId != null)
			obj.put("operationId", op.operationId);
		if (op.summary != null)
			obj.put("summary", op.summary);
		if (op.description != null)
			obj.put("description", op.description);

		if (op.parameters != null && !op.parameters.isEmpty()) {
			obj.put("parameters", serializeParameters(op.parameters));
		}

		if (op.requestBody != null && op.requestBody instanceof RequestBody) {
			RequestBody rb = (RequestBody) op.requestBody;
			JSONObject rbObj = new JSONObject();
			if (rb.description != null)
				rbObj.put("description", rb.description);
			if (rb.required != null)
				rbObj.put("required", rb.required);
			if (rb.content != null) {
				JSONObject contentObj = new JSONObject();
				for (Map.Entry<String, MediaType> me : rb.content.entrySet()) {
					JSONObject mtObj = new JSONObject();
					if (me.getValue().schema != null && me.getValue().schema instanceof Schema) {
						mtObj.put("schema", serializeSchema((Schema) me.getValue().schema));
					}
					contentObj.put(me.getKey(), mtObj);
				}
				rbObj.put("content", contentObj);
			}
			obj.put("requestBody", rbObj);
		}

		if (op.responses != null && op.responses.statusCodes != null) {
			JSONObject responsesObj = new JSONObject();
			for (Map.Entry<String, Object> _re : op.responses.statusCodes.entrySet()) {
				String re_k = _re.getKey();
				if (_re.getValue() instanceof Response) {
					Response re_v = (Response) _re.getValue();
					JSONObject rObj = new JSONObject();
					if (re_v.description != null)
						rObj.put("description", re_v.description);
					if (re_v.content != null) {
						JSONObject contentObj = new JSONObject();
						for (Map.Entry<String, MediaType> me : re_v.content.entrySet()) {
							JSONObject mtObj = new JSONObject();
							if (me.getValue().schema != null && me.getValue().schema instanceof Schema) {
								mtObj.put("schema", serializeSchema((Schema) me.getValue().schema));
							}
							contentObj.put(me.getKey(), mtObj);
						}
						rObj.put("content", contentObj);
					}
					responsesObj.put(re_k, rObj);
				}
			}
			obj.put("responses", responsesObj);
		}

		return obj;
	}

	private static JSONArray serializeParameters(java.util.List<Object> params) {
		JSONArray arr = new JSONArray();
		for (Object objP : params) {
			if (!(objP instanceof Parameter))
				continue;
			Parameter p = (Parameter) objP;
			JSONObject pObj = new JSONObject();
			if (p.name != null)
				pObj.put("name", p.name);
			if (p.in != null)
				pObj.put("in", p.in);
			if (p.description != null)
				pObj.put("description", p.description);
			if (p.required != null)
				pObj.put("required", p.required);
			if (p.schema != null && p.schema instanceof Schema) {
				pObj.put("schema", serializeSchema((Schema) p.schema));
			}
			arr.put(pObj);
		}
		return arr;
	}

	private static JSONObject serializeSchema(Schema s) {
		JSONObject obj = new JSONObject();
		if (s.type != null)
			obj.put("type", s.type);
		if (s.format != null)
			obj.put("format", s.format);
		if (s.description != null)
			obj.put("description", s.description);
		if (s.$ref != null)
			obj.put("$ref", s.$ref);
		if (s.items != null && s.items instanceof Schema) {
			obj.put("items", serializeSchema((Schema) s.items));
		}
		if (s.properties != null) {
			JSONObject propsObj = new JSONObject();
			for (Map.Entry<String, Object> pe : s.properties.entrySet()) {
				if (pe.getValue() instanceof Schema) {
					propsObj.put(pe.getKey(), serializeSchema((Schema) pe.getValue()));
				}
			}
			obj.put("properties", propsObj);
		}
		return obj;
	}

	/**
	 * Emits OpenAPI description to file.
	 * 
	 * @param api
	 *            The OpenAPI object.
	 * @param file
	 *            The file.
	 * @throws IOException
	 *             If emitting fails.
	 */
	public static void toFile(OpenAPI api, File file) throws IOException {
		String data = toString(api);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data.getBytes("UTF-8"));
		}
	}
}
