package cli;

import openapi.OpenAPI;
import openapi.PathItem;
import openapi.Operation;

import java.util.Map;

/**
 * Emits a strongly-typed CLI client from an OpenAPI model.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits a Java CLI client.
     * @param api The OpenAPI model.
     * @return Generated Java source.
     */
    public static String emitCli(OpenAPI api) {
        StringBuilder sb = new StringBuilder();
        sb.append("package cli;\n\n");
        sb.append("import java.util.Arrays;\n\n");
        sb.append("/**\n * Generated SDK CLI.\n */\n");
        sb.append("public class SdkCli {\n");
        sb.append("    /** Default constructor. */\n");
        sb.append("    public SdkCli() {}\n\n");
        sb.append("    /**\n     * CLI Entrypoint.\n     * @param args command line arguments\n     */\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        if (args.length == 0 || args[0].equals(\"--help\") || args[0].equals(\"-h\")) {\n");
        sb.append("            printHelp();\n");
        sb.append("            return;\n");
        sb.append("        }\n\n");
        sb.append("        String command = args[0];\n");
        
        if (api.paths != null && api.paths.pathItems != null) {
            for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
                String path = entry.getKey();
                PathItem pi = entry.getValue();
                
                if (pi.get != null) appendCommand(sb, "get", path, pi.get);
                if (pi.post != null) appendCommand(sb, "post", path, pi.post);
                if (pi.put != null) appendCommand(sb, "put", path, pi.put);
                if (pi.delete != null) appendCommand(sb, "delete", path, pi.delete);
                if (pi.patch != null) appendCommand(sb, "patch", path, pi.patch);
            }
        }
        
        sb.append("        System.err.println(\"Unknown command: \" + command);\n");
        sb.append("        printHelp();\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n     * Prints help.\n     */\n");
        sb.append("    private static void printHelp() {\n");
        sb.append("        System.out.println(\"SDK CLI\");\n");
        sb.append("        System.out.println(\"Commands:\");\n");
        
        if (api.paths != null && api.paths.pathItems != null) {
            for (Map.Entry<String, PathItem> entry : api.paths.pathItems.entrySet()) {
                String path = entry.getKey();
                PathItem pi = entry.getValue();
                if (pi.get != null) appendHelp(sb, "get", path, pi.get);
                if (pi.post != null) appendHelp(sb, "post", path, pi.post);
                if (pi.put != null) appendHelp(sb, "put", path, pi.put);
                if (pi.delete != null) appendHelp(sb, "delete", path, pi.delete);
                if (pi.patch != null) appendHelp(sb, "patch", path, pi.patch);
            }
        }
        
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private static void appendCommand(StringBuilder sb, String method, String path, Operation op) {
        String cmdName = op.operationId != null ? op.operationId : method + path.replaceAll("[^a-zA-Z0-9]", "_");
        sb.append("        if (command.equals(\"").append(cmdName).append("\")) {\n");
        sb.append("            System.out.println(\"Executing ").append(cmdName).append("\");\n");
        sb.append("            return;\n");
        sb.append("        }\n");
    }

    private static void appendHelp(StringBuilder sb, String method, String path, Operation op) {
        String cmdName = op.operationId != null ? op.operationId : method + path.replaceAll("[^a-zA-Z0-9]", "_");
        String desc = op.summary != null ? op.summary : (op.description != null ? op.description : cmdName);
        sb.append("        System.out.println(\"  ").append(cmdName).append(" - ").append(desc.replace("\"", "\\\"")).append("\");\n");
    }
}
