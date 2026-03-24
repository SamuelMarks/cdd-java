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
    public Emit() {}

    /**
     * Emits JSON for doc generators.
     * @param api The OpenAPI model.
     * @param noImports Whether to strip imports.
     * @param noWrapping Whether to remove the outer wrapper (paths/components).
     * @return Generated JSON.
     * @throws Exception If generation fails.
     */
    public static String emitDocsJson(OpenAPI api, boolean noImports, boolean noWrapping) throws Exception {
        JSONArray result = new JSONArray();
        JSONObject langObj = new JSONObject();
        langObj.put("language", "java");

        JSONArray operations = new JSONArray();
        langObj.put("operations", operations);
        result.put(langObj);

        if (api != null && api.paths != null && api.paths.pathItems != null) {
            for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
                String path = entry.getKey();
                PathItem pi = entry.getValue();

                if (pi.get != null) operations.put(createOperation("GET", path, pi.get, noImports, noWrapping));
                if (pi.post != null) operations.put(createOperation("POST", path, pi.post, noImports, noWrapping));
                if (pi.put != null) operations.put(createOperation("PUT", path, pi.put, noImports, noWrapping));
                if (pi.delete != null) operations.put(createOperation("DELETE", path, pi.delete, noImports, noWrapping));
                if (pi.patch != null) operations.put(createOperation("PATCH", path, pi.patch, noImports, noWrapping));
            }
        }

        return result.toString(2);
    }

    private static JSONObject createOperation(String method, String path, Operation op, boolean noImports, boolean noWrapping) {
        JSONObject opMap = new JSONObject();
        opMap.put("method", method);
        opMap.put("path", path);
        if (op.operationId != null) {
            opMap.put("operationId", op.operationId);
        }

        JSONObject code = new JSONObject();
        
        if (!noImports) {
            code.put("imports", "import my.api.ApiClient;\nimport my.api.ApiException;");
        }
        
        if (!noWrapping) {
            code.put("wrapper_start", "public class Example {\n    public static void main(String[] args) {");
            code.put("wrapper_end", "    }\n}");
        }

        String snippetOpId = op.operationId != null ? op.operationId : "callEndpoint";
        code.put("snippet", "        ApiClient client = new ApiClient();\n        try {\n            client." + snippetOpId + "();\n        } catch (ApiException e) {\n            e.printStackTrace();\n        }");

        opMap.put("code", code);
        return opMap;
    }
}
