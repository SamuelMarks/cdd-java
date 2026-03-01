package docstrings;

import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Emits JSON designed for documentation from an OpenAPI model.
 */
public class Emit {
    /**
     * Default constructor.
     */
    public Emit() {}

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Emits JSON for doc generators.
     * @param api The OpenAPI model.
     * @param noImports Whether to strip imports.
     * @param noWrapping Whether to remove the outer wrapper (paths/components).
     * @return Generated JSON.
     * @throws Exception If generation fails.
     */
    public static String emitDocsJson(OpenAPI api, boolean noImports, boolean noWrapping) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> langObj = new HashMap<>();
        langObj.put("language", "java");

        List<Map<String, Object>> operations = new ArrayList<>();
        langObj.put("operations", operations);
        result.add(langObj);

        if (api != null && api.paths != null && api.paths.pathItems != null) {
            for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
                String path = entry.getKey();
                PathItem pi = entry.getValue();

                if (pi.get != null) operations.add(createOperation("GET", path, pi.get, noImports, noWrapping));
                if (pi.post != null) operations.add(createOperation("POST", path, pi.post, noImports, noWrapping));
                if (pi.put != null) operations.add(createOperation("PUT", path, pi.put, noImports, noWrapping));
                if (pi.delete != null) operations.add(createOperation("DELETE", path, pi.delete, noImports, noWrapping));
                if (pi.options != null) operations.add(createOperation("OPTIONS", path, pi.options, noImports, noWrapping));
                if (pi.head != null) operations.add(createOperation("HEAD", path, pi.head, noImports, noWrapping));
                if (pi.patch != null) operations.add(createOperation("PATCH", path, pi.patch, noImports, noWrapping));
                if (pi.trace != null) operations.add(createOperation("TRACE", path, pi.trace, noImports, noWrapping));
            }
        }

        return mapper.writeValueAsString(result);
    }

    private static Map<String, Object> createOperation(String method, String path, Operation op, boolean noImports, boolean noWrapping) {
        Map<String, Object> opMap = new HashMap<>();
        opMap.put("method", method);
        opMap.put("path", path);
        if (op.operationId != null) {
            opMap.put("operationId", op.operationId);
        }

        Map<String, String> code = new HashMap<>();
        
        if (!noImports) {
            code.put("imports", "import my.api.ApiClient;\nimport my.api.ApiException;");
        }
        
        if (!noWrapping) {
            code.put("wrapper_start", "public class Example {\n    public static void main(String[] args) {");
            code.put("wrapper_end", "    }\n}");
        }

        // Just a basic generic snippet for Java calling the endpoint.
        String snippetOpId = op.operationId != null ? op.operationId : "callEndpoint";
        code.put("snippet", "        ApiClient client = new ApiClient();\n        try {\n            client." + snippetOpId + "();\n        } catch (ApiException e) {\n            e.printStackTrace();\n        }");

        opMap.put("code", code);
        return opMap;
    }
}
