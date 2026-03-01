package tests;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import openapi.PathItem;
import openapi.Operation;
import java.util.Optional;

/**
 * Parses tests from language source to OpenAPI representation or vice versa.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses tests into OpenAPI.
     * @param sourceCode The input.
     * @return The parsed object.
     */
    public static OpenAPI parse(String sourceCode) {
        OpenAPI api = new OpenAPI();
        api.paths = new openapi.Paths();
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (!classDecl.getNameAsString().endsWith("IntegrationTest")) continue;
                
                for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                    if (methodDecl.getNameAsString().startsWith("test_")) {
                        // Attempt to extract method name -> operationId mapping
                        String opId = methodDecl.getNameAsString().substring(5);
                        // In tests, we don't know the exact path without analyzing the client, 
                        // but we register the operationId in a generic path to be merged later
                        String dummyPath = "/" + opId;
                        PathItem item = api.paths.pathItems.computeIfAbsent(dummyPath, k -> new PathItem());
                        Operation op = new Operation();
                        op.operationId = opId;
                        op.description = "Generated from test case " + methodDecl.getNameAsString();
                        if (item.get == null) {
                            item.get = op;
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
