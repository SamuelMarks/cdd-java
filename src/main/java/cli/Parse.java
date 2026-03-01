package cli;

import openapi.OpenAPI;

/**
 * Parses a strongly-typed CLI client back into an OpenAPI model.
 */
public class Parse {
    /** Default constructor. */
    public Parse() {}

    /**
     * Parses a Java CLI client to OpenAPI.
     * @param existingSource Java source.
     * @return Extracted OpenAPI model.
     */
    public static OpenAPI parse(String existingSource) {
        OpenAPI api = new OpenAPI();
        api.paths = new openapi.Paths();
        api.paths.pathItems = new java.util.HashMap<>();
        
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("System\\.out\\.println\\(\"\\\\s+([a-zA-Z0-9_]+) - ([^\"]+)\"\\);").matcher(existingSource);
        while (m.find()) {
            String cmd = m.group(1);
            String desc = m.group(2);
            openapi.PathItem pi = new openapi.PathItem();
            openapi.Operation op = new openapi.Operation();
            op.operationId = cmd;
            op.summary = desc;
            pi.get = op;
            api.paths.pathItems.put("/" + cmd, pi);
        }
        
        return api;
    }
}
