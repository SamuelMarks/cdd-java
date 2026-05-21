package openapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses OpenAPI descriptions.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parse OpenAPI description from string.
	 *
	 * @param content
	 *            The JSON string.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromString(String content) throws IOException {
		try {
			JSONObject root = new JSONObject(content);
			OpenAPI api = new OpenAPI();

			if (root.has("openapi"))
				api.openapi = root.getString("openapi");

			if (root.has("swagger"))
				api.swagger = root.getString("swagger");
			if (root.has("host"))
				api.host = root.getString("host");
			if (root.has("basePath"))
				api.basePath = root.getString("basePath");
			if (root.has("schemes")) {
				api.schemes = new ArrayList<>();
				JSONArray arr = root.getJSONArray("schemes");
				for (int i = 0; i < arr.length(); i++)
					api.schemes.add(arr.getString(i));
			}
			if (root.has("consumes")) {
				api.consumes = new ArrayList<>();
				JSONArray arr = root.getJSONArray("consumes");
				for (int i = 0; i < arr.length(); i++)
					api.consumes.add(arr.getString(i));
			}
			if (root.has("produces")) {
				api.produces = new ArrayList<>();
				JSONArray arr = root.getJSONArray("produces");
				for (int i = 0; i < arr.length(); i++)
					api.produces.add(arr.getString(i));
			}

			if (root.has("info")) {
				JSONObject infoObj = root.getJSONObject("info");
				api.info = new Info();
				if (infoObj.has("title"))
					api.info.title = infoObj.getString("title");
				if (infoObj.has("version"))
					api.info.version = infoObj.getString("version");
				if (infoObj.has("description") && !infoObj.isNull("description")) {
					api.info.description = infoObj.getString("description");
				}
			}

			if (root.has("paths")) {
				api.paths = new Paths();
				api.paths.pathItems = new HashMap<>();
				JSONObject pathsObj = root.getJSONObject("paths");
				for (String pathKey : pathsObj.keySet()) {
					if (pathKey.startsWith("x-"))
						continue;
					JSONObject piObj = pathsObj.getJSONObject(pathKey);
					PathItem pi = new PathItem();

					if (piObj.has("get"))
						pi.get = parseOperation(piObj.getJSONObject("get"));
					if (piObj.has("post"))
						pi.post = parseOperation(piObj.getJSONObject("post"));
					if (piObj.has("put"))
						pi.put = parseOperation(piObj.getJSONObject("put"));
					if (piObj.has("delete"))
						pi.delete = parseOperation(piObj.getJSONObject("delete"));
					if (piObj.has("patch"))
						pi.patch = parseOperation(piObj.getJSONObject("patch"));

					if (piObj.has("parameters")) {
						pi.parameters = parseParameters(piObj.getJSONArray("parameters"));
					}

					api.paths.pathItems.put(pathKey, pi);
				}
			}

			if (root.has("definitions")) {
				api.definitions = new HashMap<>();
				JSONObject schemasObj = root.getJSONObject("definitions");
				for (String sKey : schemasObj.keySet()) {
					Schema s = parseSchema(schemasObj.getJSONObject(sKey));
					api.definitions.put(sKey, s);
				}
			}

			if (root.has("components")) {
				api.components = new Components();
				JSONObject compObj = root.getJSONObject("components");
				if (compObj.has("schemas")) {
					api.components.schemas = new HashMap<>();
					JSONObject schemasObj = compObj.getJSONObject("schemas");
					for (String sKey : schemasObj.keySet()) {
						Schema s = parseSchema(schemasObj.getJSONObject(sKey));
						api.components.schemas.put(sKey, s);
					}
				}
			}

			return api;
		} catch (Exception e) {
			throw new IOException("Failed to parse OpenAPI: " + e.getMessage(), e);
		}
	}

	private static Schema parseSchema(JSONObject sObj) {
		Schema s = new Schema();
		if (sObj.has("type"))
			s.type = sObj.getString("type");
		if (sObj.has("format"))
			s.format = sObj.getString("format");
		if (sObj.has("description"))
			s.description = sObj.getString("description");
		if (sObj.has("$ref"))
			s.$ref = sObj.getString("$ref");
		if (sObj.has("items")) {
			s.items = parseSchema(sObj.getJSONObject("items"));
		}
		if (sObj.has("properties")) {
			s.properties = new HashMap<>();
			JSONObject propsObj = sObj.getJSONObject("properties");
			for (String pKey : propsObj.keySet()) {
				s.properties.put(pKey, parseSchema(propsObj.getJSONObject(pKey)));
			}
		}
		return s;
	}

	private static Operation parseOperation(JSONObject obj) {
		Operation op = new Operation();
		if (obj.has("operationId"))
			op.operationId = obj.getString("operationId");
		if (obj.has("summary"))
			op.summary = obj.getString("summary");
		if (obj.has("description"))
			op.description = obj.getString("description");

		if (obj.has("parameters")) {
			op.parameters = parseParameters(obj.getJSONArray("parameters"));
		}

		if (obj.has("requestBody")) {
			RequestBody reqBody = new RequestBody();
			JSONObject rbObj = obj.getJSONObject("requestBody");
			if (rbObj.has("description"))
				reqBody.description = rbObj.getString("description");
			if (rbObj.has("required"))
				reqBody.required = rbObj.getBoolean("required");
			if (rbObj.has("content")) {
				reqBody.content = new HashMap<>();
				JSONObject contentObj = rbObj.getJSONObject("content");
				for (String cKey : contentObj.keySet()) {
					MediaType mt = new MediaType();
					JSONObject mtObj = contentObj.getJSONObject(cKey);
					if (mtObj.has("schema")) {
						Schema schema = parseSchema(mtObj.getJSONObject("schema"));
						mt.schema = schema;
					}
					reqBody.content.put(cKey, mt);
				}
			}
			op.requestBody = reqBody;
		}

		if (obj.has("responses")) {
			op.responses = new Responses();
			op.responses.statusCodes = new HashMap<>();
			JSONObject respObj = obj.getJSONObject("responses");
			for (String rKey : respObj.keySet()) {
				Response r = new Response();
				JSONObject ro = respObj.getJSONObject(rKey);
				if (ro.has("description"))
					r.description = ro.getString("description");
				if (ro.has("content")) {
					r.content = new HashMap<>();
					JSONObject contentObj = ro.getJSONObject("content");
					for (String cKey : contentObj.keySet()) {
						MediaType mt = new MediaType();
						JSONObject mtObj = contentObj.getJSONObject(cKey);
						if (mtObj.has("schema")) {
							Schema schema = parseSchema(mtObj.getJSONObject("schema"));
							mt.schema = schema;
						}
						r.content.put(cKey, mt);
					}
				}
				op.responses.statusCodes.put(rKey, r);
			}
		}

		return op;
	}

	private static java.util.List<Object> parseParameters(JSONArray arr) {
		java.util.List<Object> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject pObj = arr.getJSONObject(i);
			Parameter p = new Parameter();
			if (pObj.has("name"))
				p.name = pObj.getString("name");
			if (pObj.has("in"))
				p.in = pObj.getString("in");
			if (pObj.has("description"))
				p.description = pObj.getString("description");
			if (pObj.has("required"))
				p.required = pObj.getBoolean("required");
			if (pObj.has("schema")) {
				Schema schema = new Schema();
				JSONObject sObj = pObj.getJSONObject("schema");
				if (sObj.has("type"))
					schema.type = sObj.getString("type");
				if (sObj.has("$ref"))
					schema.$ref = sObj.getString("$ref");
				p.schema = schema;
			}
			list.add(p);
		}
		return list;
	}

	/**
	 * Parse OpenAPI description from file.
	 *
	 * @param file
	 *            The file.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromFile(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			return fromString(new String(data, "UTF-8"));
		}
	}
}
