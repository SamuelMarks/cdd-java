package routes;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import openapi.PathItem;
import openapi.Operation;
import openapi.Parameter;
import openapi.Info;
import openapi.RequestBody;
import openapi.MediaType;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses routes from language source to OpenAPI representation using JavaParser.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses Java source files into an OpenAPI model.
     * @param sourceCode The Java source.
     * @return The parsed OpenAPI object.
     */
    public static OpenAPI parse(String sourceCode) {
        OpenAPI api = new OpenAPI();
        api.openapi = "3.2.0";
        api.info = new Info();
        api.info.title = "Extracted API";
        api.info.version = "1.0.0";
        api.paths = new openapi.Paths();

        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (classDecl.getNameAsString().endsWith("Client")) {
                    api.info.title = classDecl.getNameAsString().replace("Client", "");
                }

                
                if (classDecl.isInterface() && classDecl.getNameAsString().endsWith("WebhookHandler")) {
                    String hookName = classDecl.getNameAsString().replace("WebhookHandler", "");
                    if (api.webhooks == null) api.webhooks = new HashMap<>();
                    
                    PathItem hookItem = new PathItem();
                    
                    for (MethodDeclaration hookMethod : classDecl.getMethods()) {
                        if (hookMethod.getNameAsString().equals("onPost")) {
                            Operation op = new Operation();
                            op.operationId = hookName + "Post";
                            op.parameters = new ArrayList<>();
                            
                            for (com.github.javaparser.ast.body.Parameter astParam : hookMethod.getParameters()) {
                                String pName = astParam.getNameAsString();
                                if (pName.equals("body") || pName.equals("requestBody")) {
                                    RequestBody rb = new RequestBody();
                                    rb.content = new HashMap<>();
                                    MediaType mt = new MediaType();
                                    mt.schema = new HashMap<>();
                                    ((HashMap<String, Object>) mt.schema).put("type", "string");
                                    rb.content.put("application/json", mt);
                                    op.requestBody = rb;
                                } else {
                                    Parameter p = new Parameter();
                                    p.name = pName;
                                    p.in = "query"; // Default fallback for webhook extra params
                                    p.schema = new openapi.Schema();
                                    p.schema.type = "string";
                                    op.parameters.add(p);
                                }
                            }
                            
                            if (op.parameters.isEmpty()) op.parameters = null;
                            hookItem.post = op;
                        }
                    }
                    api.webhooks.put(hookName, hookItem);
                    continue;
                }

                for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                    if (methodDecl.getTypeAsString().contains("HttpResponse")) {
                        String opId = methodDecl.getNameAsString();
                        Operation op = new Operation();
                        op.operationId = opId;

                        Optional<JavadocComment> javadoc = methodDecl.getJavadocComment();

                        if (javadoc.isPresent()) {
                            com.github.javaparser.javadoc.Javadoc doc = javadoc.get().parse();
                            String cleanDoc = doc.getDescription().toText().trim();
                            if (!cleanDoc.isEmpty()) {
                                String[] lines = cleanDoc.split("\\r?\\n", 2);
                                op.summary = lines[0].trim();
                                if (lines.length > 1 && !lines[1].trim().isEmpty()) {
                                    op.description = lines[1].trim();
                                }
                            }
                            
                            for (com.github.javaparser.javadoc.JavadocBlockTag tag : doc.getBlockTags()) {
                                if (tag.getTagName().equals("callback")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length >= 3) {
                                        String cbName = parts[0];
                                        String cbExpr = parts[1];
                                        String cbMethod = parts[2].toLowerCase();
                                        
                                        if (op.callbacks == null) op.callbacks = new java.util.HashMap<>();
                                        openapi.Callback cb = new openapi.Callback();
                                        openapi.PathItem cbPath = new openapi.PathItem();
                                        openapi.Operation cbOp = new openapi.Operation();
                                        cbOp.description = "Callback operation";
                                        
                                        switch (cbMethod) {
                                            case "get": cbPath.get = cbOp; break;
                                            case "post": cbPath.post = cbOp; break;
                                            case "put": cbPath.put = cbOp; break;
                                            case "delete": cbPath.delete = cbOp; break;
                                            case "patch": cbPath.patch = cbOp; break;
                                            case "query": cbPath.query = cbOp; break;
                                        }
                                        cb.addProperty(cbExpr, cbPath);
                                        op.callbacks.put(cbName, cb);
                                    }
                                }
                            }
                        }

                        String method = null;
                        String path = null;
                        List<String> headersUsed = new ArrayList<>();

                        for (MethodCallExpr methodCall : methodDecl.findAll(MethodCallExpr.class)) {
                            String callName = methodCall.getNameAsString();
                            if (callName.equals("GET") || callName.equals("POST") || callName.equals("PUT") || callName.equals("DELETE") || callName.equals("PATCH") || callName.equals("QUERY") || callName.equals("method")) {
                                if (callName.equals("method") && methodCall.getArguments().size() > 0) {
                                    method = methodCall.getArgument(0).toString().replace("\"", "");
                                } else {
                                    method = callName;
                                }
                            } else if (callName.equals("uri")) {
                                if (methodCall.getArguments().isNonEmpty()) {
                                    String uriArg = methodCall.getArgument(0).toString();
                                    Matcher m = Pattern.compile("baseUrl\\s*\\+\\s*(.*)").matcher(uriArg);
                                    if (m.find()) {
                                        String rawPath = m.group(1);
                                        path = rawPath.replaceAll("\"\\\\s*\\\\+\\\\s*([a-zA-Z0-9_]+)\\\\s*\\\\+\\\\s*\"", "{$1}");
                                        path = path.replaceAll("\"\\\\s*\\\\+\\\\s*([a-zA-Z0-9_]+)", "{$1}");
                                        path = path.replaceAll("([a-zA-Z0-9_]+)\\\\s*\\\\+\\\\s*\"", "{$1}");
                                        path = path.replaceAll("\"", "");
                                        if (path.endsWith(")")) {
                                            path = path.substring(0, path.length() - 1);
                                        }
                                        if (path.contains("?")) {
                                            path = path.substring(0, path.indexOf("?"));
                                        }
                                    }
                                }
                            } else if (callName.equals("header") && methodCall.getArguments().size() == 2) {
                                headersUsed.add(methodCall.getArgument(1).toString());
                            }
                        }

                        if (method != null && path != null) {
                            PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
                            op.parameters = new ArrayList<>();
                            
                            
                            boolean isSecurity = false;
                            for (com.github.javaparser.ast.body.Parameter astParam : methodDecl.getParameters()) {
                                String pName = astParam.getNameAsString();
                                
                                if (pName.equals("body") || pName.equals("requestBody")) {
                                    openapi.RequestBody rb = new openapi.RequestBody();
                                    rb.content = new java.util.HashMap<>();
                                    openapi.MediaType mt = new openapi.MediaType();
                                    mt.schema = new java.util.HashMap<>();
                                    ((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
                                    rb.content.put("application/json", mt);
                                    op.requestBody = rb;
                                    continue;
                                }
                                
                                openapi.Parameter p = new openapi.Parameter();
                                p.name = pName;
                                p.schema = new openapi.Schema();
                                p.schema.type = "string";
                                
                                if (path.contains("{" + pName + "}")) {
                                    p.in = "path";
                                    p.required = true;
                                } else if (headersUsed.contains(pName) || pName.equalsIgnoreCase("authorization")) {
                                    if (pName.equalsIgnoreCase("authorization")) {
                                        p.in = "header";
                                        p.name = "Authorization";
                                        isSecurity = true;
                                    } else {
                                        p.in = "header";
                                    }
                                } else {
                                    p.in = "query";
                                }
                                
                                if (!isSecurity) {
                                    op.parameters.add(p);
                                }
                            }
                            
                            if (isSecurity) {
                                if (api.components == null) api.components = new openapi.Components();
                                if (api.components.securitySchemes == null) api.components.securitySchemes = new java.util.HashMap<>();
                                openapi.SecurityScheme sc = new openapi.SecurityScheme();
                                sc.type = "http";
                                sc.scheme = "bearer";
                                api.components.securitySchemes.put("BearerAuth", sc);
                                
                                op.security = new java.util.ArrayList<>();
                                openapi.SecurityRequirement req = new openapi.SecurityRequirement();
                                req.requirements = new java.util.HashMap<>();
                                req.requirements.put("BearerAuth", new java.util.ArrayList<>());
                                op.security.add(req);
                            }


                            
                            if (op.parameters.isEmpty()) op.parameters = null;

                            
                            openapi.Responses responses = new openapi.Responses();
                            responses.statusCodes = new java.util.HashMap<>();
                            openapi.Response r200 = new openapi.Response();
                            r200.description = "Successful response";
                            responses.statusCodes.put("200", r200);
                            op.responses = responses;
                            
                            switch (method) {

                                case "GET": item.get = op; break;
                                case "POST": item.post = op; break;
                                case "PUT": item.put = op; break;
                                case "DELETE": item.delete = op; break;
                                case "PATCH": item.patch = op; break;
                                case "QUERY": item.query = op; break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore unparseable code
        }
        
        return api;
    }
}
