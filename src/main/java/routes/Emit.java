package routes;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;
import java.util.List;
import openapi.PathItem;
import openapi.Operation;
import openapi.Parameter;

/**
 * Emits routes to language source (HttpClient) preserving lexical layout.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits Java code using java.net.http.HttpClient.
     * @param model The OpenAPI model.
     * @param existingSource Existing Java code to preserve formatting, or null if new.
     * @return Generated Java source.
     */
    public static String emit(OpenAPI model, String existingSource) {
        String title = (model.info != null && model.info.title != null) ? model.info.title.replaceAll("[^a-zA-Z0-9]", "") : "ApiClient";
        if (title.isEmpty()) title = "ApiClient";
        
        CompilationUnit cu;
        boolean isNew = false;
        if (existingSource != null && !existingSource.trim().isEmpty()) {
            cu = StaticJavaParser.parse(existingSource);
            LexicalPreservingPrinter.setup(cu);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("import java.net.http.HttpClient;\n");
            sb.append("import java.net.http.HttpRequest;\n");
            sb.append("import java.net.http.HttpResponse;\n");
            sb.append("import java.net.URI;\n");
            sb.append("import java.io.IOException;\n");
            sb.append("import java.net.URLEncoder;\n");
            sb.append("import java.nio.charset.StandardCharsets;\n\n");
            StringBuilder classDoc = new StringBuilder();
            classDoc.append("/**\n * Auto-generated client for ").append(title).append(".\n");
            if (model.openapi != null) classDoc.append(" * @openapiVersion ").append(model.openapi).append("\n");
            if (model.$self != null) classDoc.append(" * @openapiSelf ").append(model.$self).append("\n");
            if (model.jsonSchemaDialect != null) classDoc.append(" * @jsonSchemaDialect ").append(model.jsonSchemaDialect).append("\n");
            classDoc.append(" */\n");
            sb.append(classDoc.toString());
            sb.append("public class ").append(title).append("Client {\n");
            sb.append("    private final HttpClient httpClient;\n");
            sb.append("    private final String baseUrl;\n\n");
            sb.append("    /**\n     * Constructor.\n     * @param baseUrl Base URL\n     */\n");
            sb.append("    public ").append(title).append("Client(String baseUrl) {\n");
            sb.append("        this.httpClient = HttpClient.newHttpClient();\n");
            sb.append("        this.baseUrl = baseUrl;\n");
            sb.append("    }\n\n");
            sb.append("}\n");
            cu = StaticJavaParser.parse(sb.toString());
            isNew = true;
            LexicalPreservingPrinter.setup(cu);
        }

        ClassOrInterfaceDeclaration classDecl = cu.getClassByName(title + "Client").orElse(null);
        if (classDecl == null) {
            // fallback if class name doesn't match
            if (!isNew && false) {
                classDecl = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);
            }
        }

        if (classDecl != null) {
            if (model.paths != null && model.paths.pathItems != null) {
                for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
                    String path = entry.getKey();
                    PathItem item = entry.getValue();
                    
                    if (item.get != null) emitMethodToAST(classDecl, "GET", path, item.get, item, model);
                    if (item.post != null) emitMethodToAST(classDecl, "POST", path, item.post, item, model);
                    if (item.put != null) emitMethodToAST(classDecl, "PUT", path, item.put, item, model);
                    if (item.delete != null) emitMethodToAST(classDecl, "DELETE", path, item.delete, item, model);
                    if (item.patch != null) emitMethodToAST(classDecl, "PATCH", path, item.patch, item, model);
                    if (item.query != null) emitMethodToAST(classDecl, "QUERY", path, item.query, item, model);

                    if (item.get != null && item.get.callbacks != null) emitCallbacksToAST(classDecl, item.get.callbacks);
                    if (item.post != null && item.post.callbacks != null) emitCallbacksToAST(classDecl, item.post.callbacks);
                    if (item.put != null && item.put.callbacks != null) emitCallbacksToAST(classDecl, item.put.callbacks);
                    if (item.delete != null && item.delete.callbacks != null) emitCallbacksToAST(classDecl, item.delete.callbacks);
                    if (item.patch != null && item.patch.callbacks != null) emitCallbacksToAST(classDecl, item.patch.callbacks);
                    if (item.query != null && item.query.callbacks != null) emitCallbacksToAST(classDecl, item.query.callbacks);
                }
            }
            
            if (model.webhooks != null && !model.webhooks.isEmpty()) {
                for (Map.Entry<String, PathItem> entry : model.webhooks.entrySet()) {
                    String name = entry.getKey();
                    PathItem item = entry.getValue();
                    String safeName = name.replaceAll("[^a-zA-Z0-9_]", "");
                    String interfaceName = safeName + "WebhookHandler";
                    
                    if (!hasMember(classDecl, interfaceName)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("public interface ").append(interfaceName).append(" {\n");
                        if (item.post != null) {
                            sb.append("    void onPost(");
                            boolean first = true;
                            if (item.post.requestBody != null) {
                                sb.append("String body");
                                first = false;
                            }
                            if (item.parameters != null) {
                                for (Object o : item.parameters) {
                                    if (o instanceof Parameter) {
                                        if (!first) sb.append(", ");
                                        sb.append("String ").append(((Parameter)o).name);
                                        first = false;
                                    }
                                }
                            }
                            sb.append(");\n");
                        }
                        sb.append("}\n");
                        classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
                    }
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
     * @param classDecl param doc
     * @param name param doc
     * @return return doc
     */
    private static boolean hasMember(ClassOrInterfaceDeclaration classDecl, String name) {
        for (BodyDeclaration<?> member : classDecl.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration) {
                if (((ClassOrInterfaceDeclaration) member).getNameAsString().equals(name)) return true;
            } else if (member instanceof MethodDeclaration) {
                /**
                 * Generated JavaDoc.
                 */
                if (((MethodDeclaration) member).getNameAsString().equals(name)) return true;
            }
        }
        return false;
    }

    /**
     * Generated JavaDoc.
     * @param classDecl param doc
     * @param callbacks param doc
     */
    private static void emitCallbacksToAST(ClassOrInterfaceDeclaration classDecl, Map<String, Object> callbacks) {
        if (callbacks == null) return;
        for (Map.Entry<String, Object> cbEntry : callbacks.entrySet()) {
            if (cbEntry.getValue() instanceof openapi.Callback) {
                openapi.Callback cb = (openapi.Callback) cbEntry.getValue();
                String safeName = cbEntry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
                String interfaceName = safeName + "CallbackHandler";
                
                if (!hasMember(classDecl, interfaceName)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("public interface ").append(interfaceName).append(" {\n");
                    if (cb.pathItems != null) {
                        for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
                            PathItem innerItem = piEntry.getValue();
                            if (innerItem.post != null) {
                                sb.append("    void onPost(");
                                boolean first = true;
                                if (innerItem.post.requestBody != null) {
                                    sb.append("String body");
                                    first = false;
                                }
                                if (innerItem.parameters != null) {
                                    for (Object o : innerItem.parameters) {
                                        if (o instanceof Parameter) {
                                            if (!first) sb.append(", ");
                                            sb.append("String ").append(((Parameter)o).name.replaceAll("[^a-zA-Z0-9_]", ""));
                                            first = false;
                                        }
                                    }
                                }
                                /**
                                 * Generated JavaDoc.
                                 */
                                sb.append(");\n");
                            }
                        }
                    }
                    sb.append("}\n");
                    classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
                }
            }
        }
    }

    /**
     * Generated JavaDoc.
     * @param classDecl param doc
     * @param httpMethod param doc
     * @param path param doc
     * @param op param doc
     * @param pathParams param doc
     * @param model param doc
     */
    private static void emitMethodToAST(ClassOrInterfaceDeclaration classDecl, String httpMethod, String path, Operation op, openapi.PathItem item, OpenAPI model) {
        String methodName = op.operationId;
        if (methodName == null || methodName.isEmpty()) {
            methodName = httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", "");
        } else {
            methodName = methodName.replaceAll("[^a-zA-Z0-9_]", "");
        }
        
        if (hasMember(classDecl, methodName)) {
            return; // Preserve existing custom implementation
        }

        String processedPath = path.replaceAll("\\{([^}]+)\\}", "\"+ $1 +\"");
        if (processedPath.endsWith("+\"\"")) {
            processedPath = processedPath.substring(0, processedPath.length() - 3);
        }
        
        StringBuilder argsBuilder = new StringBuilder();
        StringBuilder docBuilder = new StringBuilder();
        
        docBuilder.append("/**\n");
        if (op.summary != null && !op.summary.isEmpty()) {
            docBuilder.append(" * ").append(op.summary.replace("\n", "\n * ")).append("\n");
        }
        if (op.description != null && !op.description.isEmpty()) {
            docBuilder.append(" * ").append(op.description.replace("\n", "\n * ")).append("\n");
        }
        
        java.util.List<Parameter> allParams = new java.util.ArrayList<>();
        if (item != null && item.parameters != null) {
            for (Object po : item.parameters) {
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
        
        java.util.List<openapi.SecurityRequirement> secReqs = op.security != null ? op.security : model.security;
        if (secReqs != null && model.components != null && model.components.securitySchemes != null) {
            for (openapi.SecurityRequirement req : secReqs) {
                if (req.requirements != null) {
                    for (String secName : req.requirements.keySet()) {
                        Object schemeObj = model.components.securitySchemes.get(secName);
                        if (schemeObj instanceof openapi.SecurityScheme) {
                            openapi.SecurityScheme scheme = (openapi.SecurityScheme) schemeObj;
                            if ("apiKey".equals(scheme.type) && scheme.name != null && scheme.in != null) {
                                Parameter sp = new Parameter();
                                sp.name = scheme.name;
                                sp.in = scheme.in;
                                sp.description = "Security credential for " + secName;
                                boolean exists = false;
                                for (Parameter e : allParams) {
                                    if (e.name != null && e.name.equals(sp.name)) exists = true;
                                }
                                if (!exists) allParams.add(sp);
                            } else if ("http".equals(scheme.type) && "bearer".equals(scheme.scheme)) {
                                Parameter sp = new Parameter();
                                sp.name = "Authorization";
                                sp.in = "header";
                                sp.description = "Bearer token for " + secName + " (prefix with 'Bearer ')";
                                boolean exists = false;
                                for (Parameter e : allParams) {
                                    if (e.name != null && e.name.equals(sp.name)) exists = true;
                                }
                                if (!exists) allParams.add(sp);
                            } else if ("http".equals(scheme.type) && "basic".equals(scheme.scheme)) {
                                Parameter sp = new Parameter();
                                sp.name = "Authorization";
                                sp.in = "header";
                                sp.description = "Basic auth for " + secName + " (prefix with 'Basic ')";
                                boolean exists = false;
                                for (Parameter e : allParams) {
                                    if (e.name != null && e.name.equals(sp.name)) exists = true;
                                }
                                if (!exists) allParams.add(sp);
                            }
                        }
                    }
                }
            }
        }

        boolean hasQuery = false;
        boolean hasHeader = false;
        
        for (Parameter p : allParams) {
            if (p.name == null) continue;
            String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
            if (safeName.isEmpty()) continue;
            
            if (argsBuilder.length() > 0) argsBuilder.append(", ");
            argsBuilder.append("String ").append(safeName);
            docBuilder.append(" * @param ").append(safeName).append(" ").append(p.description != null ? p.description : p.name).append("\n");
            
            if ("query".equals(p.in)) hasQuery = true;
            if ("header".equals(p.in)) hasHeader = true;
        }
        
        boolean hasBody = false;
        if (op.requestBody != null) {
            hasBody = true;
            if (argsBuilder.length() > 0) argsBuilder.append(", ");
            argsBuilder.append("String requestBody");
            docBuilder.append(" * @param requestBody The request body payload\n");
        }
        
        if (op.callbacks != null && !op.callbacks.isEmpty()) {
            for (Map.Entry<String, Object> cbEntry : op.callbacks.entrySet()) {
                if (cbEntry.getValue() instanceof openapi.Callback) {
                    openapi.Callback cb = (openapi.Callback) cbEntry.getValue();
                    if (cb.pathItems != null) {
                        for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
                            PathItem innerItem = piEntry.getValue();
                            String method = "POST";
                            if (item.get != null) method = "GET";
                            else if (item.put != null) method = "PUT";
                            else if (item.delete != null) method = "DELETE";
                            else if (item.patch != null) method = "PATCH";
                            else if (item.query != null) method = "QUERY";
                            docBuilder.append(" * @callback ").append(cbEntry.getKey()).append(" ").append(piEntry.getKey()).append(" ").append(method).append("\n");
                        }
                    }
                }
            }
        }
        
        if (item != null) {
            if (item.ref != null && !item.ref.isEmpty()) {
                docBuilder.append(" * @pathRef ").append(path).append(" ").append(item.ref).append("\n");
            }
            if (item.summary != null && !item.summary.isEmpty()) {
                docBuilder.append(" * @pathSummary ").append(path).append(" ").append(item.summary.replace("\n", " ")).append("\n");
            }
            if (item.description != null && !item.description.isEmpty()) {
                docBuilder.append(" * @pathDescription ").append(path).append(" ").append(item.description.replace("\n", " ")).append("\n");
            }
        }

        if (op.requestBody instanceof openapi.RequestBody) {
            openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
            if (rb.content != null) {
                for (java.util.Map.Entry<String, openapi.MediaType> mEntry : rb.content.entrySet()) {
                    String mtStr = mEntry.getKey();
                    openapi.MediaType mt = mEntry.getValue();
                    docBuilder.append(" * @requestBodyContent ").append(mtStr).append("\n");
                    if (mt.schema != null && mt.schema instanceof java.util.Map) {
                        java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.schema;
                        if (schMap.containsKey("type")) {
                            docBuilder.append(" * @requestBodyContentSchema ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                        }
                    }
                    if (mt.itemSchema != null && mt.itemSchema instanceof java.util.Map) {
                        java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.itemSchema;
                        if (schMap.containsKey("type")) {
                            docBuilder.append(" * @requestBodyContentItemSchema ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                        }
                    }
                    if (mt.prefixEncoding != null) {
                        for (openapi.Encoding enc : mt.prefixEncoding) {
                            if (enc.contentType != null) docBuilder.append(" * @requestBodyContentPrefixEncoding ").append(mtStr).append(" ").append(enc.contentType).append("\n");
                        }
                    }
                    if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
                        docBuilder.append(" * @requestBodyContentItemEncoding ").append(mtStr).append(" ").append(mt.itemEncoding.contentType).append("\n");
                    }
                    if (mt.encoding != null) {
                        for (java.util.Map.Entry<String, openapi.Encoding> encEntry : mt.encoding.entrySet()) {
                            String encKey = encEntry.getKey();
                            openapi.Encoding enc = encEntry.getValue();
                            if (enc.contentType != null) {
                                docBuilder.append(" * @requestBodyEncoding ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.contentType).append("\n");
                            }
                            if (enc.prefixEncoding != null) {
                                for (openapi.Encoding pEnc : enc.prefixEncoding) {
                                    if (pEnc.contentType != null) docBuilder.append(" * @requestBodyEncodingPrefixEncoding ").append(mtStr).append(" ").append(encKey).append(" ").append(pEnc.contentType).append("\n");
                                }
                            }
                            if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
                                docBuilder.append(" * @requestBodyEncodingItemEncoding ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.itemEncoding.contentType).append("\n");
                            }
                        }
                    }
                    if (mt.example != null) {
                        docBuilder.append(" * @requestBodyContentExample ").append(mtStr).append(" ").append(mt.example).append("\n");
                    }
                    if (mt.examples != null) {
                        for (java.util.Map.Entry<String, Object> exEntry : mt.examples.entrySet()) {
                            if (exEntry.getValue() instanceof openapi.Example) {
                                openapi.Example ex = (openapi.Example) exEntry.getValue();
                                String exValue = ex.value != null ? ex.value.toString() : "";
                                String exSummary = ex.summary != null ? ex.summary : "";
                                String exDesc = ex.description != null ? ex.description : "";
                                docBuilder.append(" * @requestBodyContentExamples ").append(mtStr).append(" ").append(exEntry.getKey()).append(" ").append(exSummary.replace(" ", "_")).append("|").append(exDesc.replace(" ", "_")).append("|").append(exValue).append("\n");
                            }
                        }
                    }
                }
            }
        }

        if (op.responses != null && op.responses.statusCodes != null) {
            for (java.util.Map.Entry<String, Object> rEntry : op.responses.statusCodes.entrySet()) {
                String statusCode = rEntry.getKey();
                if (rEntry.getValue() instanceof openapi.Response) {
                    openapi.Response r = (openapi.Response) rEntry.getValue();
                    if (r.content != null) {
                        for (java.util.Map.Entry<String, openapi.MediaType> mEntry : r.content.entrySet()) {
                            String mtStr = mEntry.getKey();
                            openapi.MediaType mt = mEntry.getValue();
                            docBuilder.append(" * @responseContent ").append(statusCode).append(" ").append(mtStr).append("\n");
                            if (mt.schema != null && mt.schema instanceof java.util.Map) {
                                java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.schema;
                                if (schMap.containsKey("type")) {
                                    docBuilder.append(" * @responseContentSchema ").append(statusCode).append(" ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                                }
                            }
                            if (mt.example != null) {
                                docBuilder.append(" * @responseContentExample ").append(statusCode).append(" ").append(mtStr).append(" ").append(mt.example).append("\n");
                            }
                            if (mt.examples != null) {
                                for (java.util.Map.Entry<String, Object> exEntry : mt.examples.entrySet()) {
                                    if (exEntry.getValue() instanceof openapi.Example) {
                                        openapi.Example ex = (openapi.Example) exEntry.getValue();
                                        String exValue = ex.value != null ? ex.value.toString() : "";
                                        String exSummary = ex.summary != null ? ex.summary : "";
                                        String exDesc = ex.description != null ? ex.description : "";
                                        docBuilder.append(" * @responseContentExamples ").append(statusCode).append(" ").append(mtStr).append(" ").append(exEntry.getKey()).append(" ").append(exSummary.replace(" ", "_")).append("|").append(exDesc.replace(" ", "_")).append("|").append(exValue).append("\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (op.responses != null && op.responses.defaultResponse instanceof openapi.Response) {
            openapi.Response r = (openapi.Response) op.responses.defaultResponse;
            String statusCode = "default";
            if (r.content != null) {
                for (java.util.Map.Entry<String, openapi.MediaType> mEntry : r.content.entrySet()) {
                    String mtStr = mEntry.getKey();
                    openapi.MediaType mt = mEntry.getValue();
                    docBuilder.append(" * @responseContent ").append(statusCode).append(" ").append(mtStr).append("\n");
                    if (mt.schema != null && mt.schema instanceof java.util.Map) {
                        java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.schema;
                        if (schMap.containsKey("type")) {
                            docBuilder.append(" * @responseContentSchema ").append(statusCode).append(" ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                        }
                    }
                    if (mt.itemSchema != null && mt.itemSchema instanceof java.util.Map) {
                        java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.itemSchema;
                        if (schMap.containsKey("type")) {
                            docBuilder.append(" * @responseContentItemSchema ").append(statusCode).append(" ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                        }
                    }
                    if (mt.prefixEncoding != null) {
                        for (openapi.Encoding enc : mt.prefixEncoding) {
                            if (enc.contentType != null) docBuilder.append(" * @responseContentPrefixEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(enc.contentType).append("\n");
                        }
                    }
                    if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
                        docBuilder.append(" * @responseContentItemEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(mt.itemEncoding.contentType).append("\n");
                    }
                    if (mt.encoding != null) {
                        for (java.util.Map.Entry<String, openapi.Encoding> encEntry : mt.encoding.entrySet()) {
                            String encKey = encEntry.getKey();
                            openapi.Encoding enc = encEntry.getValue();
                            if (enc.contentType != null) {
                                docBuilder.append(" * @responseEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.contentType).append("\n");
                            }
                            if (enc.prefixEncoding != null) {
                                for (openapi.Encoding pEnc : enc.prefixEncoding) {
                                    if (pEnc.contentType != null) docBuilder.append(" * @responseEncodingPrefixEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(pEnc.contentType).append("\n");
                                }
                            }
                            if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
                                docBuilder.append(" * @responseEncodingItemEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.itemEncoding.contentType).append("\n");
                            }
                        }
                    }
                    if (mt.itemSchema != null && mt.itemSchema instanceof java.util.Map) {
                        java.util.Map<String, Object> schMap = (java.util.Map<String, Object>) mt.itemSchema;
                        if (schMap.containsKey("type")) {
                            docBuilder.append(" * @responseContentItemSchema ").append(statusCode).append(" ").append(mtStr).append(" ").append(schMap.get("type")).append("\n");
                        }
                    }
                    if (mt.prefixEncoding != null) {
                        for (openapi.Encoding enc : mt.prefixEncoding) {
                            if (enc.contentType != null) docBuilder.append(" * @responseContentPrefixEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(enc.contentType).append("\n");
                        }
                    }
                    if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
                        docBuilder.append(" * @responseContentItemEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(mt.itemEncoding.contentType).append("\n");
                    }
                    if (mt.encoding != null) {
                        for (java.util.Map.Entry<String, openapi.Encoding> encEntry : mt.encoding.entrySet()) {
                            String encKey = encEntry.getKey();
                            openapi.Encoding enc = encEntry.getValue();
                            if (enc.contentType != null) {
                                docBuilder.append(" * @responseEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.contentType).append("\n");
                            }
                            if (enc.prefixEncoding != null) {
                                for (openapi.Encoding pEnc : enc.prefixEncoding) {
                                    if (pEnc.contentType != null) docBuilder.append(" * @responseEncodingPrefixEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(pEnc.contentType).append("\n");
                                }
                            }
                            if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
                                docBuilder.append(" * @responseEncodingItemEncoding ").append(statusCode).append(" ").append(mtStr).append(" ").append(encKey).append(" ").append(enc.itemEncoding.contentType).append("\n");
                            }
                        }
                    }
                    if (mt.example != null) {
                        docBuilder.append(" * @responseContentExample ").append(statusCode).append(" ").append(mtStr).append(" ").append(mt.example).append("\n");
                    }
                    if (mt.examples != null) {
                        for (java.util.Map.Entry<String, Object> exEntry : mt.examples.entrySet()) {
                            if (exEntry.getValue() instanceof openapi.Example) {
                                openapi.Example ex = (openapi.Example) exEntry.getValue();
                                String exValue = ex.value != null ? ex.value.toString() : "";
                                String exSummary = ex.summary != null ? ex.summary : "";
                                String exDesc = ex.description != null ? ex.description : "";
                                docBuilder.append(" * @responseContentExamples ").append(statusCode).append(" ").append(mtStr).append(" ").append(exEntry.getKey()).append(" ").append(exSummary.replace(" ", "_")).append("|").append(exDesc.replace(" ", "_")).append("|").append(exValue).append("\n");
                            }
                        }
                    }
                }
            }
        }

        docBuilder.append(" * @return HttpResponse\n");
        docBuilder.append(" * @throws IOException on error\n");
        docBuilder.append(" * @throws InterruptedException on error\n");
        docBuilder.append(" */\n");
        
        StringBuilder sb = new StringBuilder();
        sb.append(docBuilder.toString());
        sb.append("public HttpResponse<String> ").append(methodName).append("(").append(argsBuilder.toString()).append(") throws IOException, InterruptedException {\n");
        sb.append("    StringBuilder uriBuilder = new StringBuilder(baseUrl + \"").append(processedPath).append("\");\n");
        
        if (hasQuery) {
            sb.append("    boolean firstQuery = true;\n");
            for (Parameter p : allParams) {
                if ("query".equals(p.in)) {
                    String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
                    sb.append("    if (").append(safeName).append(" != null) {\n");
                    sb.append("        uriBuilder.append(firstQuery ? \"?\" : \"&\");\n");
                    sb.append("        uriBuilder.append(\"").append(p.name).append("=\").append(URLEncoder.encode(").append(safeName).append(", StandardCharsets.UTF_8));\n");
                    sb.append("        firstQuery = false;\n");
                    sb.append("    }\n");
                }
            }
        }
        
        sb.append("    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()\n");
        sb.append("        .uri(URI.create(uriBuilder.toString()));\n");
        
        if (hasHeader) {
            for (Parameter p : allParams) {
                if ("header".equals(p.in)) {
                    String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
                    sb.append("    if (").append(safeName).append(" != null) {\n");
                    sb.append("        requestBuilder.header(\"").append(p.name).append("\", ").append(safeName).append(");\n");
                    sb.append("    }\n");
                }
            }
        }
        
        String bodyPublisher = hasBody ? "HttpRequest.BodyPublishers.ofString(requestBody)" : "HttpRequest.BodyPublishers.noBody()";
        
        if (httpMethod.equals("GET") || httpMethod.equals("DELETE")) {
            sb.append("    requestBuilder.").append(httpMethod).append("();\n");
        } else if (httpMethod.equals("QUERY") || httpMethod.equals("PATCH")) {
            sb.append("    requestBuilder.method(\"").append(httpMethod).append("\", ").append(bodyPublisher).append(");\n");
        } else {
            sb.append("    requestBuilder.").append(httpMethod).append("(").append(bodyPublisher).append(");\n");
        }
        
        sb.append("    return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());\n");
        sb.append("}\n");
        
        classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
    }
}
