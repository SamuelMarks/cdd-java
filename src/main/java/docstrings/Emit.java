package docstrings;

import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Operation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Emits JSON designed for documentation from an OpenAPI model.
 */
public class Emit {
	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits JSON for doc generators.
	 * 
	 * @param api
	 *            The OpenAPI model.
	 * @param noImports
	 *            Whether to strip imports.
	 * @param noWrapping
	 *            Whether to remove the outer wrapper (paths/components).
	 * @return Generated JSON.
	 * @throws Exception
	 *             If generation fails.
	 */
	public static String emitDocsJson(OpenAPI api, boolean noImports, boolean noWrapping) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject endpoints = new JSONObject();

		if (api != null && api.paths != null && api.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
				String path = entry.getKey();
				PathItem pi = entry.getValue();

				JSONObject pathMap = new JSONObject();

				if (pi.get != null)
					pathMap.put("get", createSnippet(pi.get, noImports, noWrapping));
				if (pi.post != null)
					pathMap.put("post", createSnippet(pi.post, noImports, noWrapping));
				if (pi.put != null)
					pathMap.put("put", createSnippet(pi.put, noImports, noWrapping));
				if (pi.delete != null)
					pathMap.put("delete", createSnippet(pi.delete, noImports, noWrapping));
				if (pi.patch != null)
					pathMap.put("patch", createSnippet(pi.patch, noImports, noWrapping));
				if (pi.options != null)
					pathMap.put("options", createSnippet(pi.options, noImports, noWrapping));
				if (pi.head != null)
					pathMap.put("head", createSnippet(pi.head, noImports, noWrapping));
				if (pi.trace != null)
					pathMap.put("trace", createSnippet(pi.trace, noImports, noWrapping));

				if (pathMap.length() > 0) {
					endpoints.put(path, pathMap);
				}
			}
		}

		result.put("endpoints", endpoints);
		return result.toString(2);
	}

	private static String createSnippet(Operation op, boolean noImports, boolean noWrapping) {
		StringBuilder sb = new StringBuilder();

		if (!noImports) {
			sb.append("import my.api.ApiClient;\nimport my.api.ApiException;\n\n");
		}

		if (!noWrapping) {
			/**
			 * Documented.
			 */
			sb.append("public class Example {\n    public static void main(String[] args) {\n");
		}

		String snippetOpId = (op.operationId != null && !op.operationId.isEmpty()) ? op.operationId : "callEndpoint";
		sb.append("        ApiClient client = new ApiClient();\n        try {\n            client.");
		sb.append(snippetOpId);
		sb.append("();\n        } catch (ApiException e) {\n            e.printStackTrace();\n        }\n");

		if (!noWrapping) {
			sb.append("    }\n}");
		}

		return sb.toString();
	}
}
