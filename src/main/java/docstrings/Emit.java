package docstrings;

import openapi.OpenAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import java.util.HashMap;

/**
 * Emits JSON designed for documentation from an OpenAPI model.
 */
public class Emit {
    /**
     * Default constructor.
     */
    public Emit() {}

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     */
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
        Map<String, Object> output = new HashMap<>();
        
        if (noWrapping) {
            // Flatten the output
            if (api.paths != null && api.paths.pathItems != null) output.put("routes", api.paths);
            if (api.components != null && api.components.schemas != null) {
                output.put("models", api.components.schemas);
            }
            if (api.info != null) {
                output.put("info", api.info);
            }
            return mapper.writeValueAsString(output);
        } else {
            // Include wrapping
            Map<String, Object> docs = new HashMap<>();
            docs.put("api", api);
            return mapper.writeValueAsString(docs);
        }
    }
}
