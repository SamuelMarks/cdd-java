package tests;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import openapi.PathItem;
import openapi.Operation;
import openapi.Parameter;

/**
 * Emits API integration tests to language source preserving lexical layout.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits Java code for JUnit-style (but dependency-free) integration tests.
     * @param model The OpenAPI model.
     * @param existingSource Existing Java code to preserve formatting, or null if new.
     * @return Generated Java source.
     */
    public static String emit(OpenAPI model, String existingSource) {
        String title = (model.info != null && model.info.title != null) ? model.info.title.replaceAll("[^a-zA-Z0-9]", "") : "Api";
        if (title.isEmpty()) title = "Api";
        
        String clientClass = title + "Client";
        String mockClass = title + "MockServer";
        String testClass = title + "IntegrationTest";
        
        CompilationUnit cu;
        boolean isNew = false;
        if (existingSource != null && !existingSource.trim().isEmpty()) {
            cu = StaticJavaParser.parse(existingSource);
            LexicalPreservingPrinter.setup(cu);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("import java.net.http.HttpResponse;\n");
            sb.append("import java.io.IOException;\n\n");
            sb.append("/**\n * Auto-generated integration tests for ").append(title).append(".\n */\n");
            sb.append("public class ").append(testClass).append(" {\n");
            sb.append("}\n");
            cu = StaticJavaParser.parse(sb.toString());
            isNew = true;
            LexicalPreservingPrinter.setup(cu);
        }

        ClassOrInterfaceDeclaration classDecl = cu.getClassByName(testClass).orElse(null);
        if (classDecl == null) {
            if (!isNew && false) {
                classDecl = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);
            }
        }

        if (classDecl != null) {
            if (!hasMember(classDecl, "main")) {
                StringBuilder sb = new StringBuilder();
                sb.append("public static void main(String[] args) throws Exception {\n");
                sb.append("    ").append(mockClass).append(" server = new ").append(mockClass).append("();\n");
                sb.append("    server.start(8080);\n");
                sb.append("    ").append(clientClass).append(" client = new ").append(clientClass).append("(\"http://localhost:8080\");\n");
                sb.append("    int failures = 0;\n\n");
                
                if (model.paths != null && model.paths.pathItems != null) {
                    for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
                        String path = entry.getKey();
                        PathItem item = entry.getValue();
                        
                        if (item.get != null) emitTestCall(sb, "GET", path, item.get);
                        if (item.post != null) emitTestCall(sb, "POST", path, item.post);
                        if (item.put != null) emitTestCall(sb, "PUT", path, item.put);
                        if (item.delete != null) emitTestCall(sb, "DELETE", path, item.delete);
                        if (item.patch != null) emitTestCall(sb, "PATCH", path, item.patch);
                        if (item.query != null) emitTestCall(sb, "QUERY", path, item.query);
                    }
                }
                
                sb.append("    server.stop();\n");
                sb.append("    System.out.println(\"Tests completed. Failures: \" + failures);\n");
                sb.append("    if (failures > 0) System.exit(1);\n");
                sb.append("}\n");
                classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
            }

            if (model.paths != null && model.paths.pathItems != null) {
                for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
                    String path = entry.getKey();
                    PathItem item = entry.getValue();
                    
                    if (item.get != null) emitTestMethodToAST(classDecl, "GET", path, item.get, item.parameters);
                    if (item.post != null) emitTestMethodToAST(classDecl, "POST", path, item.post, item.parameters);
                    if (item.put != null) emitTestMethodToAST(classDecl, "PUT", path, item.put, item.parameters);
                    if (item.delete != null) emitTestMethodToAST(classDecl, "DELETE", path, item.delete, item.parameters);
                    if (item.patch != null) emitTestMethodToAST(classDecl, "PATCH", path, item.patch, item.parameters);
                    if (item.query != null) emitTestMethodToAST(classDecl, "QUERY", path, item.query, item.parameters);
                }
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
     * @param sb param doc
     * @param httpMethod param doc
     * @param path param doc
     * @param op param doc
     */
    private static void emitTestCall(StringBuilder sb, String httpMethod, String path, Operation op) {
        String methodName = op.operationId;
        if (methodName == null || methodName.isEmpty()) {
            /**
             * Generated JavaDoc.
             */
            methodName = httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", "");
        } else {
            methodName = methodName.replaceAll("[^a-zA-Z0-9_]", "");
        }
        sb.append("    failures += test_").append(methodName).append("(client);\n");
    }

    /**
     * Generated JavaDoc.
     * @param classDecl param doc
     * @param httpMethod param doc
     * @param path param doc
     * @param op param doc
     * @param pathParams param doc
     */
    private static void emitTestMethodToAST(ClassOrInterfaceDeclaration classDecl, String httpMethod, String path, Operation op, List<Object> pathParams) {
        String methodName = op.operationId;
        if (methodName == null || methodName.isEmpty()) {
            methodName = httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", "");
        } else {
            methodName = methodName.replaceAll("[^a-zA-Z0-9_]", "");
        }
        
        String testMethodName = "test_" + methodName;
        if (hasMember(classDecl, testMethodName)) {
            return;
        }

        List<Parameter> allParams = new ArrayList<>();
        if (pathParams != null) {
            for (Object po : pathParams) {
                if (po instanceof Parameter) allParams.add((Parameter) po);
            }
        }
        if (op.parameters != null) {
            for (Object po : op.parameters) {
                if (po instanceof Parameter) {
                    Parameter p = (Parameter) po;
                    boolean exists = false;
                    for (Parameter e : allParams) {
                        if (e.name != null && e.name.equals(p.name) && e.in != null && e.in.equals(p.in)) { exists = true; break; }
                    }
                    if (!exists) allParams.add(p);
                }
            }
        }
        
        int paramCount = 0;
        for (Parameter p : allParams) {
            if (p.name == null) continue;
            String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
            if (!safeName.isEmpty()) paramCount++;
        }
        
        boolean hasBody = (op.requestBody != null);
        
        StringBuilder sb = new StringBuilder();
        sb.append("private static int ").append(testMethodName).append("(Object rawClient) {\n");
        sb.append("    int fails = 0;\n");
        sb.append("    try {\n");
        sb.append("        Class<?> clientClass = rawClient.getClass();\n");
        sb.append("        java.lang.reflect.Method method = null;\n");
        sb.append("        for (java.lang.reflect.Method m : clientClass.getMethods()) {\n");
        sb.append("            if (m.getName().equals(\"").append(methodName).append("\")) {\n");
        sb.append("                method = m;\n");
        sb.append("                break;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        
        int totalArgs = paramCount + (hasBody ? 1 : 0);
        
        if (totalArgs > 0) {
            sb.append("        java.net.http.HttpResponse<String> res = (java.net.http.HttpResponse<String>) method.invoke(rawClient");
            for(int i = 0; i < paramCount; i++) {
                sb.append(", \"test_param_").append(i).append("\"");
            }
            if (hasBody) {
                sb.append(", \"{\\\"mock\\\": \\\"true\\\"}\"");
            }
            sb.append(");\n");
        } else {
            sb.append("        java.net.http.HttpResponse<String> res = (java.net.http.HttpResponse<String>) method.invoke(rawClient);\n");
        }
        
        sb.append("        if (res.statusCode() != 200) {\n");
        /**
         * Generated JavaDoc.
         */
        sb.append("            System.err.println(\"Failed ").append(methodName).append(": Expected 200, got \" + res.statusCode());\n");
        sb.append("            fails++;\n");
        sb.append("        } else {\n");
        sb.append("            System.out.println(\"Passed ").append(methodName).append("\");\n");
        sb.append("        }\n");
        sb.append("    } catch (Exception e) {\n");
        sb.append("        System.err.println(\"Exception in ").append(methodName).append(": \" + e.getMessage());\n");
        sb.append("        fails++;\n");
        sb.append("    }\n");
        sb.append("    return fails;\n");
        sb.append("}\n");

        classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
    }

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