package mocks;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * Emits mock servers to language source preserving lexical layout.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits Java code for mock servers using com.sun.net.httpserver.HttpServer.
     * @param model The OpenAPI model.
     * @param existingSource Existing Java code to preserve formatting, or null if new.
     * @return Generated Java source.
     */
    public static String emit(OpenAPI model, String existingSource) {
        String title = (model.info != null && model.info.title != null) ? model.info.title.replaceAll("[^a-zA-Z0-9]", "") : "ApiMock";
        if (title.isEmpty()) title = "ApiMock";
        
        CompilationUnit cu;
        boolean isNew = false;
        if (existingSource != null && !existingSource.trim().isEmpty()) {
            cu = StaticJavaParser.parse(existingSource);
            LexicalPreservingPrinter.setup(cu);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("import com.sun.net.httpserver.HttpServer;\n");
            sb.append("import com.sun.net.httpserver.HttpExchange;\n");
            sb.append("import java.net.InetSocketAddress;\n");
            sb.append("import java.io.IOException;\n");
            sb.append("import java.io.OutputStream;\n\n");
            sb.append("/**\n * Auto-generated mock server for ").append(title).append(".\n */\n");
            sb.append("class ").append(title).append("MockServer {\n");
            sb.append("    private HttpServer server;\n\n");
            sb.append("    public void stop() {\n");
            sb.append("        if (server != null) {\n");
            sb.append("            server.stop(0);\n");
            sb.append("        }\n");
            sb.append("    }\n");
            sb.append("}\n");
            cu = StaticJavaParser.parse(sb.toString());
            isNew = true;
            LexicalPreservingPrinter.setup(cu);
        }

        ClassOrInterfaceDeclaration classDecl = cu.getClassByName(title + "MockServer").orElse(null);
        if (classDecl == null) {
            if (!isNew && false) {
                classDecl = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);
            }
        }

        if (classDecl != null) {
            if (!hasMember(classDecl, "start")) {
                StringBuilder sb = new StringBuilder();
                sb.append("public void start(int port) throws IOException {\n");
                sb.append("    server = HttpServer.create(new InetSocketAddress(port), 0);\n");
                
                if (model.paths != null && model.paths.pathItems != null) {
                    for (String path : model.paths.pathItems.keySet()) {
                        String handlerPath = path.replaceAll("\\{[^}]+\\}", "");
                        if (handlerPath.endsWith("/") && handlerPath.length() > 1) {
                             handlerPath = handlerPath.substring(0, handlerPath.length() - 1);
                        }
                        
                        sb.append("    server.createContext(\"").append(handlerPath).append("\", (HttpExchange exchange) -> {\n");
                        sb.append("        String response = \"{\\\"mock\\\": \\\"true\\\"}\";\n");
                        sb.append("        exchange.sendResponseHeaders(200, response.length());\n");
                        sb.append("        try (OutputStream os = exchange.getResponseBody()) {\n");
                        sb.append("            os.write(response.getBytes());\n");
                        sb.append("        }\n");
                        sb.append("    });\n");
                    }
                }
                
                sb.append("    server.setExecutor(null);\n");
                sb.append("    server.start();\n");
                sb.append("    System.out.println(\"Mock server started on port \" + port);\n");
                sb.append("}\n");
                
                classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
            }
        }

        if (isNew) {
            return cu.toString();
        } else {
            return LexicalPreservingPrinter.print(cu);
        }
    }

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     * @param classDecl param doc
     * @param name param doc
     * @return return doc
     */
    private static boolean hasMember(ClassOrInterfaceDeclaration classDecl, String name) {
        for (BodyDeclaration<?> member : classDecl.getMembers()) {
            if (member instanceof MethodDeclaration) {
                if (((MethodDeclaration) member).getNameAsString().equals(name)) return true;
            }
        }
        return false;
    }
}
