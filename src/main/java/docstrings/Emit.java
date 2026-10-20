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
				for (Map.Entry<openapi.HttpVerb, Operation> opEntry : pi.getOperationsMap().entrySet()) {
					pathMap.put(opEntry.getKey().specName, createSnippet(opEntry.getValue(), noImports, noWrapping));
				}
				if (pathMap.length() > 0) {
					endpoints.put(path, pathMap);
				}
			}
		}
		result.put("endpoints", endpoints);
		return result.toString(2);
	}

	/**
	 * Creates a formatted Java SDK usage snippet for an OpenAPI operation.
	 *
	 * @param op
	 *            The {@link Operation} to generate a snippet for.
	 * @param noImports
	 *            Whether to omit {@code import} statements.
	 * @param noWrapping
	 *            Whether to omit the outer {@code Example} class and {@code main}
	 *            method wrapper.
	 * @return The generated Java code snippet string.
	 */
	private static String createSnippet(Operation op, boolean noImports, boolean noWrapping) {
		StringBuilder sb = new StringBuilder();
		if (!noImports) {
			sb.append("import my.api.ApiClient;\nimport my.api.ApiException;\n\n");
		}
		if (!noWrapping) {
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
