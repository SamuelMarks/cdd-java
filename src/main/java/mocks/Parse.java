package mocks;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import openapi.PathItem;
import openapi.Operation;

/**
 * Parses mocks from language source to OpenAPI representation using JavaParser.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses mock server code to an OpenAPI object.
     * @param sourceCode The Java source.
     * @return The parsed OpenAPI object.
     */
    public static OpenAPI parse(String sourceCode) {
        OpenAPI api = new OpenAPI();
        api.paths = new openapi.Paths();

        try {
            String toParse = sourceCode;
            if (!sourceCode.contains("class ")) {
                toParse = "class Dummy { void dummy() { " + sourceCode + " } }";
            }
            CompilationUnit cu = StaticJavaParser.parse(toParse);
            for (MethodCallExpr methodCall : cu.findAll(MethodCallExpr.class)) {
                if (methodCall.getNameAsString().equals("createContext")) {
                    if (methodCall.getScope().isPresent() && methodCall.getScope().get().toString().equals("server")) {
                        if (methodCall.getArguments().isNonEmpty()) {
                            String pathStr = methodCall.getArgument(0).toString().replace("\"", "");
                            PathItem item = api.paths.pathItems.computeIfAbsent(pathStr, k -> new PathItem());
                            Operation op = new Operation();
                            op.description = "Generated from mock";
                            if (item.get == null) {
                                item.get = op;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return api;
    }
    
}
