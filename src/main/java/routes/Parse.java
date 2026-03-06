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
        api.info.version = "0.0.1";
        api.paths = new openapi.Paths();

        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {

                if (classDecl.getNameAsString().endsWith("Client")) {
                    api.info.title = classDecl.getNameAsString().replace("Client", "");
                }
                
                Optional<JavadocComment> cJavadoc = classDecl.getJavadocComment();
                if (cJavadoc.isPresent()) {
                    com.github.javaparser.javadoc.Javadoc cDoc = cJavadoc.get().parse();
                    for (com.github.javaparser.javadoc.JavadocBlockTag tag : cDoc.getBlockTags()) {
                        String tagName = tag.getTagName();
                        String tagContent = tag.getContent().toText().trim();
                        if (tagName.equals("openapiVersion")) api.openapi = tagContent;
                        else if (tagName.equals("openapiSelf")) api.$self = tagContent;
                        else if (tagName.equals("jsonSchemaDialect")) api.jsonSchemaDialect = tagContent;
                        else if (tagName.equals("title")) api.info.title = tagContent;
                        else if (tagName.equals("version")) api.info.version = tagContent;
                        else if (tagName.equals("summary")) api.info.summary = tagContent;
                        else if (tagName.equals("description")) api.info.description = tagContent;
                        else if (tagName.equals("termsOfService")) api.info.termsOfService = tagContent;
                        else if (tagName.equals("contactName")) {
                            if (api.info.contact == null) api.info.contact = new openapi.Contact();
                            api.info.contact.name = tagContent;
                        } else if (tagName.equals("contactEmail")) {
                            if (api.info.contact == null) api.info.contact = new openapi.Contact();
                            api.info.contact.email = tagContent;
                        } else if (tagName.equals("contactUrl")) {
                            if (api.info.contact == null) api.info.contact = new openapi.Contact();
                            api.info.contact.url = tagContent;
                        } else if (tagName.equals("licenseName")) {
                            if (api.info.license == null) api.info.license = new openapi.License();
                            api.info.license.name = tagContent;
                        } else if (tagName.equals("licenseIdentifier")) {
                            if (api.info.license == null) api.info.license = new openapi.License();
                            api.info.license.identifier = tagContent;
                        } else if (tagName.equals("licenseUrl")) {
                            if (api.info.license == null) api.info.license = new openapi.License();
                            api.info.license.url = tagContent;
                        } else if (tagName.equals("globalTag")) {
                            if (api.tags == null) api.tags = new java.util.ArrayList<>();
                            openapi.Tag tagModel = new openapi.Tag();
                            String[] parts = tagContent.split("\\s+");
                            if (parts.length > 0) tagModel.name = parts[0];
                            for (int i = 1; i < parts.length; i++) {
                                String part = parts[i];
                                if (part.startsWith("summary=")) tagModel.summary = part.substring(8).replace("_", " ");
                                else if (part.startsWith("description=")) tagModel.description = part.substring(12).replace("_", " ");
                                else if (part.startsWith("externalDocsUrl=")) {
                                    if (tagModel.externalDocs == null) tagModel.externalDocs = new openapi.ExternalDocumentation();
                                    tagModel.externalDocs.url = part.substring(16);
                                }
                                else if (part.startsWith("parent=")) tagModel.parent = part.substring(7);
                                else if (part.startsWith("kind=")) tagModel.kind = part.substring(5);
                            }
                            api.tags.add(tagModel);
                        } else if (tagName.equals("server")) {
                            if (api.servers == null) api.servers = new java.util.ArrayList<>();
                            openapi.Server s = new openapi.Server();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) s.url = parts[0];
                            if (parts.length > 1) s.description = parts[1];
                            api.servers.add(s);
                        } else if (tagName.equals("serverName")) {
                            if (api.servers != null && !api.servers.isEmpty()) {
                                api.servers.get(api.servers.size() - 1).name = tagContent;
                            }
                        } else if (tagName.equals("serverVariable")) {
                            String[] parts = tagContent.split("\\s+", 4);
                            if (parts.length >= 3) {
                                String serverName = parts[0];
                                String varName = parts[1];
                                String defaultValue = parts[2];
                                String description = parts.length > 3 ? parts[3] : null;

                                if (api.servers != null) {
                                    for (openapi.Server s : api.servers) {
                                        if (serverName.equals(s.name)) {
                                            if (s.variables == null) s.variables = new java.util.HashMap<>();
                                            openapi.ServerVariable sv = s.variables.computeIfAbsent(varName, k -> new openapi.ServerVariable());
                                            sv.defaultValue = defaultValue;
                                            if (description != null) sv.description = description;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else if (tagName.equals("serverVariableEnum")) {
                            String[] parts = tagContent.split("\\s+", 3);
                            if (parts.length >= 3) {
                                String serverName = parts[0];
                                String varName = parts[1];
                                String enumList = parts[2];

                                if (api.servers != null) {
                                    for (openapi.Server s : api.servers) {
                                        if (serverName.equals(s.name)) {
                                            if (s.variables == null) s.variables = new java.util.HashMap<>();
                                            openapi.ServerVariable sv = s.variables.computeIfAbsent(varName, k -> new openapi.ServerVariable());
                                            sv.enumValues = new java.util.ArrayList<>(java.util.Arrays.asList(enumList.split(",")));
                                            break;
                                        }
                                    }
                                }
                            }
                        } else if (tagName.equals("securityScheme")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.securitySchemes == null) api.components.securitySchemes = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+");
                            if (parts.length >= 1) {
                                String sName = parts[0];
                                openapi.SecurityScheme sc = new openapi.SecurityScheme();
                                for (int i = 1; i < parts.length; i++) {
                                    String part = parts[i];
                                    if (part.startsWith("type=")) {
                                        String val = part.substring(5);
                                        if (!val.equals("-")) sc.type = val;
                                    } else if (part.startsWith("scheme=")) {
                                        String val = part.substring(7);
                                        if (!val.equals("-")) sc.scheme = val;
                                    } else if (part.startsWith("in=")) {
                                        String val = part.substring(3);
                                        if (!val.equals("-")) sc.in = val;
                                    } else if (part.startsWith("name=")) {
                                        String val = part.substring(5);
                                        if (!val.equals("-")) sc.name = val;
                                    } else if (part.startsWith("bearerFormat=")) {
                                        String val = part.substring(13);
                                        if (!val.equals("-")) sc.bearerFormat = val;
                                    } else if (part.startsWith("openIdConnectUrl=")) {
                                        String val = part.substring(17);
                                        if (!val.equals("-")) sc.openIdConnectUrl = val;
                                    }
                                }
                                api.components.securitySchemes.put(sName, sc);
                                }
                                } else if (tagName.equals("securitySchemeFlow")) {
                                if (api.components == null) api.components = new openapi.Components();
                                if (api.components.securitySchemes == null) api.components.securitySchemes = new java.util.HashMap<>();
                                String[] parts = tagContent.split("\\s+");
                                if (parts.length >= 2) {
                                String flowType = parts[0];
                                String sName = parts[1];
                                openapi.SecurityScheme sc = (openapi.SecurityScheme) api.components.securitySchemes.computeIfAbsent(sName, k -> new openapi.SecurityScheme());
                                if (sc.flows == null) sc.flows = new openapi.OAuthFlows();

                                if (flowType.equals("implicit") && parts.length >= 4) {
                                    if (sc.flows.implicit == null) sc.flows.implicit = new openapi.OAuthFlow();
                                    sc.flows.implicit.authorizationUrl = parts[2].equals("-") ? null : parts[2];
                                    sc.flows.implicit.refreshUrl = parts[3].equals("-") ? null : parts[3];
                                } else if (flowType.equals("password") && parts.length >= 4) {
                                    if (sc.flows.password == null) sc.flows.password = new openapi.OAuthFlow();
                                    sc.flows.password.tokenUrl = parts[2].equals("-") ? null : parts[2];
                                    sc.flows.password.refreshUrl = parts[3].equals("-") ? null : parts[3];
                                } else if (flowType.equals("clientCredentials") && parts.length >= 4) {
                                    if (sc.flows.clientCredentials == null) sc.flows.clientCredentials = new openapi.OAuthFlow();
                                    sc.flows.clientCredentials.tokenUrl = parts[2].equals("-") ? null : parts[2];
                                    sc.flows.clientCredentials.refreshUrl = parts[3].equals("-") ? null : parts[3];
                                } else if (flowType.equals("authorizationCode") && parts.length >= 5) {
                                    if (sc.flows.authorizationCode == null) sc.flows.authorizationCode = new openapi.OAuthFlow();
                                    sc.flows.authorizationCode.authorizationUrl = parts[2].equals("-") ? null : parts[2];
                                    sc.flows.authorizationCode.tokenUrl = parts[3].equals("-") ? null : parts[3];
                                    sc.flows.authorizationCode.refreshUrl = parts[4].equals("-") ? null : parts[4];
                                } else if (flowType.equals("deviceAuthorization") && parts.length >= 5) {
                                    if (sc.flows.deviceAuthorization == null) sc.flows.deviceAuthorization = new openapi.OAuthFlow();
                                    sc.flows.deviceAuthorization.deviceAuthorizationUrl = parts[2].equals("-") ? null : parts[2];
                                    sc.flows.deviceAuthorization.tokenUrl = parts[3].equals("-") ? null : parts[3];
                                    sc.flows.deviceAuthorization.refreshUrl = parts[4].equals("-") ? null : parts[4];
                                }
                                }
                                } else if (tagName.equals("securitySchemeFlowScope")) {
                                if (api.components == null) api.components = new openapi.Components();
                                if (api.components.securitySchemes == null) api.components.securitySchemes = new java.util.HashMap<>();
                                String[] parts = tagContent.split("\\s+", 4);
                                if (parts.length >= 3) {
                                String sName = parts[0];
                                String flowType = parts[1];
                                String scopeName = parts[2];
                                String scopeDesc = parts.length > 3 ? (parts[3].equals("-") ? "" : parts[3]) : "";

                                openapi.SecurityScheme sc = (openapi.SecurityScheme) api.components.securitySchemes.computeIfAbsent(sName, k -> new openapi.SecurityScheme());
                                if (sc.flows == null) sc.flows = new openapi.OAuthFlows();

                                if (flowType.equals("implicit")) {
                                    if (sc.flows.implicit == null) sc.flows.implicit = new openapi.OAuthFlow();
                                    if (sc.flows.implicit.scopes == null) sc.flows.implicit.scopes = new java.util.HashMap<>();
                                    sc.flows.implicit.scopes.put(scopeName, scopeDesc);
                                } else if (flowType.equals("password")) {
                                    if (sc.flows.password == null) sc.flows.password = new openapi.OAuthFlow();
                                    if (sc.flows.password.scopes == null) sc.flows.password.scopes = new java.util.HashMap<>();
                                    sc.flows.password.scopes.put(scopeName, scopeDesc);
                                } else if (flowType.equals("clientCredentials")) {
                                    if (sc.flows.clientCredentials == null) sc.flows.clientCredentials = new openapi.OAuthFlow();
                                    if (sc.flows.clientCredentials.scopes == null) sc.flows.clientCredentials.scopes = new java.util.HashMap<>();
                                    sc.flows.clientCredentials.scopes.put(scopeName, scopeDesc);
                                } else if (flowType.equals("authorizationCode")) {
                                    if (sc.flows.authorizationCode == null) sc.flows.authorizationCode = new openapi.OAuthFlow();
                                    if (sc.flows.authorizationCode.scopes == null) sc.flows.authorizationCode.scopes = new java.util.HashMap<>();
                                    sc.flows.authorizationCode.scopes.put(scopeName, scopeDesc);
                                } else if (flowType.equals("deviceAuthorization")) {
                                    if (sc.flows.deviceAuthorization == null) sc.flows.deviceAuthorization = new openapi.OAuthFlow();
                                    if (sc.flows.deviceAuthorization.scopes == null) sc.flows.deviceAuthorization.scopes = new java.util.HashMap<>();
                                    sc.flows.deviceAuthorization.scopes.put(scopeName, scopeDesc);
                                }
                                }
                                } else if (tagName.equals("componentResponse")) {                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.responses == null) api.components.responses = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.Response res = new openapi.Response();
                                if (parts.length > 1) res.description = parts[1];
                                api.components.responses.put(parts[0], res);
                            }
                        } else if (tagName.equals("componentParameter")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.parameters == null) api.components.parameters = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 4);
                            if (parts.length > 0) {
                                openapi.Parameter p = new openapi.Parameter();
                                if (parts.length > 1 && !parts[1].equals("-")) p.name = parts[1];
                                if (parts.length > 2 && !parts[2].equals("-")) p.in = parts[2];
                                if (parts.length > 3) p.description = parts[3];
                                api.components.parameters.put(parts[0], p);
                            }
                        } else if (tagName.equals("componentRequestBody")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.requestBodies == null) api.components.requestBodies = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.RequestBody rb = new openapi.RequestBody();
                                if (parts.length > 1) rb.description = parts[1];
                                api.components.requestBodies.put(parts[0], rb);
                            }
                        } else if (tagName.equals("componentHeader")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.headers == null) api.components.headers = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.Header hdr = new openapi.Header();
                                if (parts.length > 1) hdr.description = parts[1];
                                api.components.headers.put(parts[0], hdr);
                            }                        } else if (tagName.equals("componentLink")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                if (parts.length > 1) link.operationId = parts[1];
                            }
                        } else if (tagName.equals("componentLinkOpId")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                link.operationId = parts[1];
                            }
                        } else if (tagName.equals("componentLinkOpRef")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                link.operationRef = parts[1];
                            }
                        } else if (tagName.equals("componentLinkDesc")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                link.description = parts[1];
                            }
                        } else if (tagName.equals("componentLinkServer")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                if (link.server == null) link.server = new openapi.Server();
                                link.server.url = parts[1];
                            }
                        } else if (tagName.equals("componentLinkParam")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 3);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                if (link.parameters == null) link.parameters = new java.util.HashMap<>();
                                link.parameters.put(parts[1], parts.length > 2 ? parts[2] : "");
                            }
                        } else if (tagName.equals("componentLinkRequestBody")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.links == null) api.components.links = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 1) {
                                openapi.Link link = (openapi.Link) api.components.links.computeIfAbsent(parts[0], k -> new openapi.Link());
                                link.requestBody = parts[1];
                            }
                        } else if (tagName.equals("componentCallback")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.callbacks == null) api.components.callbacks = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 3);
                            if (parts.length >= 3) {
                                String cbKey = parts[0];
                                String cbPath = parts[1];
                                String cbMethod = parts[2].toLowerCase();
                                openapi.Callback cb = new openapi.Callback();
                                cb.pathItems = new java.util.HashMap<>();
                                openapi.PathItem pi = new openapi.PathItem();
                                openapi.Operation cbOp = new openapi.Operation();
                                cbOp.description = "Callback operation";
                                switch (cbMethod) {
                                    case "get": pi.get = cbOp; break;
                                    case "post": pi.post = cbOp; break;
                                    case "put": pi.put = cbOp; break;
                                    case "delete": pi.delete = cbOp; break;
                                    case "patch": pi.patch = cbOp; break;
                                    case "query": pi.query = cbOp; break;
                                }
                                cb.pathItems.put(cbPath, pi);
                                api.components.callbacks.put(cbKey, cb);
                            }
                        } else if (tagName.equals("componentPathItem")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.pathItems == null) api.components.pathItems = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.PathItem pi = new openapi.PathItem();
                                if (parts.length > 1) pi.description = parts[1];
                                api.components.pathItems.put(parts[0], pi);
                            }
                        } else if (tagName.equals("componentMediaType")) {
                            if (api.components == null) api.components = new openapi.Components();
                            if (api.components.mediaTypes == null) api.components.mediaTypes = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.MediaType mt = new openapi.MediaType();
                                if (parts.length > 1 && !parts[1].equals("-")) {
                                    java.util.HashMap<String, Object> schema = new java.util.HashMap<>();
                                    schema.put("type", parts[1]);
                                    mt.schema = schema;
                                }
                                api.components.mediaTypes.put(parts[0], mt);
                            }
                        } else if (tagName.equals("pathSummary")) {
                            if (api.paths == null) api.paths = new openapi.Paths();
                            if (api.paths.pathItems == null) api.paths.pathItems = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
                                if (parts.length > 1) pi.summary = parts[1];
                            }
                        } else if (tagName.equals("pathDescription")) {
                            if (api.paths == null) api.paths = new openapi.Paths();
                            if (api.paths.pathItems == null) api.paths.pathItems = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 2);
                            if (parts.length > 0) {
                                openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
                                if (parts.length > 1) pi.description = parts[1];
                            }
                        } else if (tagName.equals("pathServer")) {
                            if (api.paths == null) api.paths = new openapi.Paths();
                            if (api.paths.pathItems == null) api.paths.pathItems = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 3);
                            if (parts.length > 1) {
                                openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
                                if (pi.servers == null) pi.servers = new java.util.ArrayList<>();
                                openapi.Server s = new openapi.Server();
                                s.url = parts[1];
                                if (parts.length > 2) s.description = parts[2];
                                pi.servers.add(s);
                            }
                        } else if (tagName.equals("pathParameter")) {
                            if (api.paths == null) api.paths = new openapi.Paths();
                            if (api.paths.pathItems == null) api.paths.pathItems = new java.util.HashMap<>();
                            String[] parts = tagContent.split("\\s+", 4);
                            if (parts.length > 2) {
                                openapi.PathItem pi = api.paths.pathItems.computeIfAbsent(parts[0], k -> new openapi.PathItem());
                                if (pi.parameters == null) pi.parameters = new java.util.ArrayList<>();
                                openapi.Parameter p = new openapi.Parameter();
                                p.name = parts[1].equals("-") ? null : parts[1];
                                p.in = parts[2].equals("-") ? null : parts[2];
                                if (parts.length > 3) p.description = parts[3];
                                pi.parameters.add(p);
                            }
                        }
                    }
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
                        
                        java.util.Map<String, String> paramDescriptions = new java.util.HashMap<>();
                        java.util.Set<String> requiredParams = new java.util.HashSet<>();
                        java.util.Set<String> deprecatedParams = new java.util.HashSet<>();
                        java.util.Set<String> allowEmptyValueParams = new java.util.HashSet<>();
                        java.util.Map<String, String> paramStyles = new java.util.HashMap<>();
                        java.util.Set<String> explodeParams = new java.util.HashSet<>();
                        java.util.Set<String> allowReservedParams = new java.util.HashSet<>();
                        java.util.Map<String, String> paramSchemas = new java.util.HashMap<>();
                        java.util.Map<String, java.util.Set<String>> paramContents = new java.util.HashMap<>();
                        java.util.Map<String, String> paramExamples = new java.util.HashMap<>();
                        java.util.Map<String, java.util.Map<String, openapi.Example>> paramExamplesMap = new java.util.HashMap<>();

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
                                } else if (tag.getTagName().equals("tag")) {
                                    if (op.tags == null) op.tags = new ArrayList<>();
                                    op.tags.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("externalDocs")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 0) {
                                        if (op.externalDocs == null) op.externalDocs = new openapi.ExternalDocumentation();
                                        op.externalDocs.url = parts[0];
                                        if (parts.length > 1) {
                                            op.externalDocs.description = parts[1];
                                        }
                                    }
                                } else if (tag.getTagName().equals("deprecated")) {
                                    op.deprecated = true;
                                } else if (tag.getTagName().equals("operationServer")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 0) {
                                        if (op.servers == null) op.servers = new java.util.ArrayList<>();
                                        openapi.Server s = new openapi.Server();
                                        s.url = parts[0];
                                        if (parts.length > 1) s.description = parts[1];
                                        op.servers.add(s);
                                    }
                                } else if (tag.getTagName().equals("operationSecurity")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 0) {
                                        if (op.security == null) op.security = new java.util.ArrayList<>();
                                        openapi.SecurityRequirement req = new openapi.SecurityRequirement();
                                        req.requirements = new java.util.HashMap<>();
                                        java.util.List<String> scopes = new java.util.ArrayList<>();
                                        if (parts.length > 1 && !parts[1].isEmpty()) {
                                            scopes.addAll(java.util.Arrays.asList(parts[1].split(",")));
                                        }
                                        req.requirements.put(parts[0], scopes);
                                        op.security.add(req);
                                    }
                                } else if (tag.getTagName().equals("param")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        paramDescriptions.put(parts[0], parts[1]);
                                    }
                                } else if (tag.getTagName().equals("requiredParam")) {
                                    requiredParams.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("deprecatedParam")) {
                                    deprecatedParams.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("paramAllowEmptyValue")) {
                                    allowEmptyValueParams.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("paramStyle")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        paramStyles.put(parts[0], parts[1]);
                                    }
                                } else if (tag.getTagName().equals("paramExplode")) {
                                    explodeParams.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("paramAllowReserved")) {
                                    allowReservedParams.add(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("paramSchema")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        paramSchemas.put(parts[0], parts[1]);
                                    }
                                } else if (tag.getTagName().equals("paramContent")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        paramContents.computeIfAbsent(parts[0], k -> new java.util.HashSet<>()).add(parts[1]);
                                    }
                                } else if (tag.getTagName().equals("paramExample")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        paramExamples.put(parts[0], parts[1]);
                                    }
                                } else if (tag.getTagName().equals("paramExamples")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        String paramName = parts[0];
                                        String exKey = parts[1];
                                        String exData = parts[2];
                                        String[] exParts = exData.split("\\|", -1); // Keep empty parts
                                        openapi.Example ex = new openapi.Example();
                                        if (exParts.length > 0 && !exParts[0].isEmpty()) ex.summary = exParts[0];
                                        if (exParts.length > 1 && !exParts[1].isEmpty()) ex.description = exParts[1];
                                        if (exParts.length > 2 && !exParts[2].isEmpty()) ex.value = exParts[2];
                                        
                                        paramExamplesMap.computeIfAbsent(paramName, k -> new java.util.HashMap<>()).put(exKey, ex);
                                    }
                                } else if (tag.getTagName().equals("requestBody")) {
                                    if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                    ((openapi.RequestBody) op.requestBody).description = tag.getContent().toText().trim();
                                } else if (tag.getTagName().equals("requestBodyRequired")) {
                                    if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                    ((openapi.RequestBody) op.requestBody).required = Boolean.parseBoolean(tag.getContent().toText().trim());
                                } else if (tag.getTagName().equals("requestBodyContent")) {
                                    if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                    openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                    if (rb.content == null) rb.content = new java.util.HashMap<>();
                                    openapi.MediaType mt = new openapi.MediaType();
                                    mt.schema = new java.util.HashMap<>();
                                    ((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
                                    rb.content.put(tag.getContent().toText().trim(), mt);
                                
                                } else if (tag.getTagName().equals("requestBodyContentSchema")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.schema == null) mt.schema = new java.util.HashMap<>();
                                        ((java.util.HashMap<String, Object>) mt.schema).put("type", parts[1]);
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentExample")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        mt.example = parts[1];
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentItemSchema")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.itemSchema == null) mt.itemSchema = new java.util.HashMap<>();
                                        ((java.util.HashMap<String, Object>) mt.itemSchema).put("type", parts[1]);
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentPrefixEncoding")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.prefixEncoding == null) mt.prefixEncoding = new java.util.ArrayList<>();
                                        openapi.Encoding enc = new openapi.Encoding();
                                        enc.contentType = parts[1];
                                        mt.prefixEncoding.add(enc);
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentItemEncoding")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.itemEncoding == null) mt.itemEncoding = new openapi.Encoding();
                                        mt.itemEncoding.contentType = parts[1];
                                    }
                                } else if (tag.getTagName().equals("requestBodyEncoding")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                        openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new openapi.Encoding());
                                        enc.contentType = parts[2];
                                    }
                                } else if (tag.getTagName().equals("requestBodyEncodingPrefixEncoding")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                        openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new openapi.Encoding());
                                        if (enc.prefixEncoding == null) enc.prefixEncoding = new java.util.ArrayList<>();
                                        openapi.Encoding pEnc = new openapi.Encoding();
                                        pEnc.contentType = parts[2];
                                        enc.prefixEncoding.add(pEnc);
                                    }
                                } else if (tag.getTagName().equals("requestBodyEncodingItemEncoding")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                        openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new openapi.Encoding());
                                        if (enc.itemEncoding == null) enc.itemEncoding = new openapi.Encoding();
                                        enc.itemEncoding.contentType = parts[2];
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentExamples")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.examples == null) mt.examples = new java.util.HashMap<>();
                                        openapi.Example exObj = new openapi.Example();
                                        String[] exParts = parts[2].split("\\|", -1);
                                        if (exParts.length > 0 && !exParts[0].isEmpty()) exObj.summary = exParts[0].replace("_", " ");
                                        if (exParts.length > 1 && !exParts[1].isEmpty()) exObj.description = exParts[1].replace("_", " ");
                                        if (exParts.length > 2 && !exParts[2].isEmpty()) exObj.value = exParts[2];
                                        mt.examples.put(parts[1], exObj);
                                    }

                                } else if (tag.getTagName().equals("requestBodyContentSchema")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.schema == null) mt.schema = new java.util.HashMap<>();
                                        ((java.util.HashMap<String, Object>) mt.schema).put("type", parts[1]);
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentExample")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 1) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        mt.example = parts[1];
                                    }
                                } else if (tag.getTagName().equals("requestBodyContentExamples")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 3);
                                    if (parts.length > 2) {
                                        if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                        openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                        if (rb.content == null) rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = rb.content.computeIfAbsent(parts[0], k -> new openapi.MediaType());
                                        if (mt.examples == null) mt.examples = new java.util.HashMap<>();
                                        openapi.Example exObj = new openapi.Example();
                                        String[] exParts = parts[2].split("\\|", -1);
                                        if (exParts.length > 0 && !exParts[0].isEmpty()) exObj.summary = exParts[0].replace("_", " ");
                                        if (exParts.length > 1 && !exParts[1].isEmpty()) exObj.description = exParts[1].replace("_", " ");
                                        if (exParts.length > 2 && !exParts[2].isEmpty()) exObj.value = exParts[2];
                                        mt.examples.put(parts[1], exObj);
                                    }
} else if (tag.getTagName().equals("responseDefault")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    if (op.responses == null) {
                                        op.responses = new openapi.Responses();
                                        op.responses.statusCodes = new java.util.HashMap<>();
                                    } else if (op.responses.statusCodes == null) {
                                        op.responses.statusCodes = new java.util.HashMap<>();
                                    }
                                    openapi.Response r = new openapi.Response();
                                    if (!tagContent.isEmpty()) r.description = tagContent;
                                    op.responses.defaultResponse = r;
                                } else if (tag.getTagName().equals("response")) {
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", 2);
                                    if (parts.length > 0) {
                                        String statusCode = parts[0];
                                        if (op.responses == null) {
                                            op.responses = new openapi.Responses();
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        } else if (op.responses.statusCodes == null) {
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        }
                                        Object objR = statusCode.equals("default") ? op.responses.defaultResponse : op.responses.statusCodes.get(statusCode);
                                        if (!(objR instanceof openapi.Response)) {
                                            objR = new openapi.Response();
                                            if (statusCode.equals("default")) {
                                                op.responses.defaultResponse = objR;
                                            } else {
                                                op.responses.statusCodes.put(statusCode, objR);
                                            }
                                        }
                                        openapi.Response r = (openapi.Response) objR;
                                        if (parts.length > 1) r.description = parts[1];
                                    }
                                } else if (tag.getTagName().startsWith("responseHeader")) {
                                    String tagName = tag.getTagName();
                                    String tagContent = tag.getContent().toText().trim();
                                    String[] parts = tagContent.split("\\s+", tagName.equals("responseHeaderExamples") ? 4 : 3);
                                    if (parts.length > 1) {
                                        String statusCode = parts[0];
                                        String headerName = parts[1];
                                        if (op.responses == null) {
                                            op.responses = new openapi.Responses();
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        } else if (op.responses.statusCodes == null) {
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        }
                                        Object objR = statusCode.equals("default") ? op.responses.defaultResponse : op.responses.statusCodes.get(statusCode);
                                        if (!(objR instanceof openapi.Response)) {
                                            objR = new openapi.Response();
                                            if (statusCode.equals("default")) {
                                                op.responses.defaultResponse = objR;
                                            } else {
                                                op.responses.statusCodes.put(statusCode, objR);
                                            }
                                        }
                                        openapi.Response r = (openapi.Response) objR;
                                        if (r.headers == null) r.headers = new java.util.HashMap<>();
                                        Object objH = r.headers.get(headerName);
                                        if (!(objH instanceof openapi.Header)) {
                                            objH = new openapi.Header();
                                            r.headers.put(headerName, objH);
                                        }
                                        openapi.Header h = (openapi.Header) objH;
                                        
                                        if (tagName.equals("responseHeader") && parts.length > 2) {
                                            h.description = parts[2];
                                        } else if (tagName.equals("responseHeaderRequired")) {
                                            h.required = true;
                                        } else if (tagName.equals("responseHeaderDeprecated")) {
                                            h.deprecated = true;
                                        } else if (tagName.equals("responseHeaderStyle") && parts.length > 2) {
                                            h.style = parts[2];
                                        } else if (tagName.equals("responseHeaderExplode")) {
                                            h.explode = true;
                                        } else if (tagName.equals("responseHeaderSchema") && parts.length > 2) {
                                            openapi.Schema sch = new openapi.Schema();
                                            sch.type = parts[2];
                                            h.schema = sch;
                                        } else if (tagName.equals("responseHeaderExample") && parts.length > 2) {
                                            h.example = parts[2];
                                        } else if (tagName.equals("responseHeaderContent") && parts.length > 2) {
                                            if (h.content == null) h.content = new java.util.HashMap<>();
                                            h.content.put(parts[2], new openapi.MediaType());
                                        } else if (tagName.equals("responseHeaderExamples") && parts.length > 2) {
                                            if (h.examples == null) h.examples = new java.util.HashMap<>();
                                            String exName = parts[2];
                                            openapi.Example exObj = new openapi.Example();
                                            if (parts.length > 3) {
                                                String[] exParts = parts[3].split("\\|", -1);
                                                if (exParts.length > 0 && !exParts[0].isEmpty()) exObj.summary = exParts[0];
                                                if (exParts.length > 1 && !exParts[1].isEmpty()) exObj.description = exParts[1];
                                                if (exParts.length > 2 && !exParts[2].isEmpty()) exObj.value = exParts[2];
                                            }
                                            h.examples.put(exName, exObj);
                                        }
                                    }
                                } else if (tag.getTagName().startsWith("responseContent") || tag.getTagName().startsWith("responseEncoding")) {
                                    String tagName = tag.getTagName();
                                    String tagContent = tag.getContent().toText().trim();
                                    int limit = 3;
                                    if (tagName.equals("responseContentExamples")) limit = 4;
                                    else if (tagName.startsWith("responseEncoding")) limit = 4;
                                    String[] parts = tagContent.split("\\s+", limit);
                                    if (parts.length > 1) {
                                        String statusCode = parts[0];
                                        String mediaTypeStr = parts[1];
                                        if (op.responses == null) {
                                            op.responses = new openapi.Responses();
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        } else if (op.responses.statusCodes == null) {
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        }
                                        Object objR = statusCode.equals("default") ? op.responses.defaultResponse : op.responses.statusCodes.get(statusCode);
                                        if (!(objR instanceof openapi.Response)) {
                                            objR = new openapi.Response();
                                            if (statusCode.equals("default")) {
                                                op.responses.defaultResponse = objR;
                                            } else {
                                                op.responses.statusCodes.put(statusCode, objR);
                                            }
                                        }
                                        openapi.Response r = (openapi.Response) objR;
                                        if (r.content == null) r.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = r.content.computeIfAbsent(mediaTypeStr, k -> new openapi.MediaType());
                                        
                                        if (tagName.equals("responseContent")) {
                                            if (mt.schema == null) {
                                                mt.schema = new java.util.HashMap<>();
                                                ((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
                                            }
                                        } else if (tagName.equals("responseContentItemSchema") && parts.length > 2) {
                                            if (mt.itemSchema == null) mt.itemSchema = new java.util.HashMap<>();
                                            ((java.util.HashMap<String, Object>) mt.itemSchema).put("type", parts[2]);
                                        } else if (tagName.equals("responseContentPrefixEncoding") && parts.length > 2) {
                                            if (mt.prefixEncoding == null) mt.prefixEncoding = new java.util.ArrayList<>();
                                            openapi.Encoding enc = new openapi.Encoding();
                                            enc.contentType = parts[2];
                                            mt.prefixEncoding.add(enc);
                                        } else if (tagName.equals("responseContentItemEncoding") && parts.length > 2) {
                                            if (mt.itemEncoding == null) mt.itemEncoding = new openapi.Encoding();
                                            mt.itemEncoding.contentType = parts[2];
                                        } else if (tagName.equals("responseEncoding") && parts.length > 3) {
                                            if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                            openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new openapi.Encoding());
                                            enc.contentType = parts[3];
                                        } else if (tagName.equals("responseEncodingPrefixEncoding") && parts.length > 3) {
                                            if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                            openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new openapi.Encoding());
                                            if (enc.prefixEncoding == null) enc.prefixEncoding = new java.util.ArrayList<>();
                                            openapi.Encoding pEnc = new openapi.Encoding();
                                            pEnc.contentType = parts[3];
                                            enc.prefixEncoding.add(pEnc);
                                        } else if (tagName.equals("responseEncodingItemEncoding") && parts.length > 3) {
                                            if (mt.encoding == null) mt.encoding = new java.util.HashMap<>();
                                            openapi.Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new openapi.Encoding());
                                            if (enc.itemEncoding == null) enc.itemEncoding = new openapi.Encoding();
                                            enc.itemEncoding.contentType = parts[3];
                                        } else if (tagName.equals("responseContentSchema") && parts.length > 2) {
                                            if (mt.schema == null) mt.schema = new java.util.HashMap<>();
                                            ((java.util.HashMap<String, Object>) mt.schema).put("type", parts[2]);
                                        } else if (tagName.equals("responseContentExample") && parts.length > 2) {
                                            mt.example = parts[2];
                                        } else if (tagName.equals("responseContentExamples") && parts.length > 2) {
                                            if (mt.examples == null) mt.examples = new java.util.HashMap<>();
                                            String exName = parts[2];
                                            openapi.Example exObj = new openapi.Example();
                                            if (parts.length > 3) {
                                                String[] exParts = parts[3].split("\\|", -1);
                                                if (exParts.length > 0 && !exParts[0].isEmpty()) exObj.summary = exParts[0].replace("_", " ");
                                                if (exParts.length > 1 && !exParts[1].isEmpty()) exObj.description = exParts[1].replace("_", " ");
                                                if (exParts.length > 2 && !exParts[2].isEmpty()) exObj.value = exParts[2];
                                            }
                                            mt.examples.put(exName, exObj);
                                        }
                                    }
                                } else if (tag.getTagName().startsWith("responseLink")) {
                                    String tagName = tag.getTagName();
                                    String tagContent = tag.getContent().toText().trim();
                                    int limit = tagName.equals("responseLinkParam") ? 4 : 3;
                                    String[] parts = tagContent.split("\\s+", limit);
                                    if (parts.length > 1) {
                                        String statusCode = parts[0];
                                        String linkKey = parts[1];
                                        if (op.responses == null) {
                                            op.responses = new openapi.Responses();
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        } else if (op.responses.statusCodes == null) {
                                            op.responses.statusCodes = new java.util.HashMap<>();
                                        }
                                        Object objR = statusCode.equals("default") ? op.responses.defaultResponse : op.responses.statusCodes.get(statusCode);
                                        if (!(objR instanceof openapi.Response)) {
                                            objR = new openapi.Response();
                                            if (statusCode.equals("default")) {
                                                op.responses.defaultResponse = objR;
                                            } else {
                                                op.responses.statusCodes.put(statusCode, objR);
                                            }
                                        }
                                        openapi.Response r = (openapi.Response) objR;
                                        if (r.links == null) r.links = new java.util.HashMap<>();
                                        Object objL = r.links.get(linkKey);
                                        if (!(objL instanceof openapi.Link)) {
                                            objL = new openapi.Link();
                                            r.links.put(linkKey, (openapi.Link) objL);
                                        }
                                        openapi.Link l = (openapi.Link) objL;
                                        
                                        if (tagName.equals("responseLink")) {
                                            // just creates the link if not exist
                                        } else if (tagName.equals("responseLinkOpId") && parts.length > 2) {
                                            l.operationId = parts[2];
                                        } else if (tagName.equals("responseLinkOpRef") && parts.length > 2) {
                                            l.operationRef = parts[2];
                                        } else if (tagName.equals("responseLinkDesc") && parts.length > 2) {
                                            l.description = parts[2];
                                        } else if (tagName.equals("responseLinkServer") && parts.length > 2) {
                                            if (l.server == null) l.server = new openapi.Server();
                                            l.server.url = parts[2];
                                        } else if (tagName.equals("responseLinkParam") && parts.length > 2) {
                                            if (l.parameters == null) l.parameters = new java.util.HashMap<>();
                                            l.parameters.put(parts[2], parts.length > 3 ? parts[3] : "");
                                        } else if (tagName.equals("responseLinkRequestBody") && parts.length > 2) {
                                            l.requestBody = parts[2];
                                        }
                                    }
                                }
                            }
                        }

                        String method = null;
                        String path = null;

                        for (com.github.javaparser.ast.expr.ObjectCreationExpr objCreate : methodDecl.findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)) {
                            if (objCreate.getTypeAsString().equals("StringBuilder") && objCreate.getArguments().isNonEmpty()) {
                                String argStr = objCreate.getArgument(0).toString();
                                java.util.regex.Matcher sm = java.util.regex.Pattern.compile("baseUrl\\s*\\+\\s*(.*)").matcher(argStr);
                                if (sm.find()) {
                                    String rawPath = sm.group(1);
                                    path = rawPath.replaceAll("\"\\s*\\+\\s*([a-zA-Z0-9_]+)\\s*\\+\\s*\"", "{$1}");
                                    path = path.replaceAll("\"\\s*\\+\\s*([a-zA-Z0-9_]+)", "{$1}");
                                    path = path.replaceAll("([a-zA-Z0-9_]+)\\s*\\+\\s*\"", "{$1}");
                                    path = path.replaceAll("\"", "");
                                    if (path.endsWith(")")) path = path.substring(0, path.length() - 1);
                                    if (path.contains("?")) path = path.substring(0, path.indexOf("?"));
                                }
                            }
                        }
                        List<String> headersUsed = new ArrayList<>();

                        for (MethodCallExpr methodCall : methodDecl.findAll(MethodCallExpr.class)) {
                            String callName = methodCall.getNameAsString();
                            if (callName.equals("GET") || callName.equals("POST") || callName.equals("PUT") || callName.equals("DELETE") || callName.equals("PATCH") || callName.equals("QUERY") || callName.equals("OPTIONS") || callName.equals("HEAD") || callName.equals("TRACE") || callName.equals("method")) {
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

                        System.out.println("Method: " + method + " Path: " + path);
                        if (method != null && path != null) {
                            PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
                            op.parameters = new ArrayList<>();
                            
                            
                            boolean isSecurity = false;
                            for (com.github.javaparser.ast.body.Parameter astParam : methodDecl.getParameters()) {
                                String pName = astParam.getNameAsString();
                                
                                if (pName.equals("body") || pName.equals("requestBody")) {
                                    if (!(op.requestBody instanceof openapi.RequestBody)) op.requestBody = new openapi.RequestBody();
                                    openapi.RequestBody rb = (openapi.RequestBody) op.requestBody;
                                    if (rb.content == null) {
                                        rb.content = new java.util.HashMap<>();
                                        openapi.MediaType mt = new openapi.MediaType();
                                        mt.schema = new java.util.HashMap<>();
                                        ((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
                                        rb.content.put("application/json", mt);
                                    }
                                    continue;
                                }
                                
                                openapi.Parameter p = new openapi.Parameter();
                                p.name = pName;
                                p.schema = new openapi.Schema();
                                p.schema.type = "string";
                                
                                if (paramDescriptions.containsKey(pName)) {
                                    p.description = paramDescriptions.get(pName);
                                }
                                if (requiredParams.contains(pName)) {
                                    p.required = true;
                                }
                                if (deprecatedParams.contains(pName)) {
                                    p.deprecated = true;
                                }
                                if (allowEmptyValueParams.contains(pName)) {
                                    p.allowEmptyValue = true;
                                }
                                if (paramStyles.containsKey(pName)) {
                                    p.style = paramStyles.get(pName);
                                }
                                if (explodeParams.contains(pName)) {
                                    p.explode = true;
                                }
                                if (allowReservedParams.contains(pName)) {
                                    p.allowReserved = true;
                                }
                                if (paramSchemas.containsKey(pName)) {
                                    p.schema.type = paramSchemas.get(pName);
                                }
                                if (paramContents.containsKey(pName)) {
                                    if (p.content == null) {
                                        p.content = new java.util.HashMap<>();
                                    }
                                    for (String mediaTypeStr : paramContents.get(pName)) {
                                        openapi.MediaType mt = new openapi.MediaType();
                                        mt.schema = new java.util.HashMap<>();
                                        ((java.util.HashMap<String, Object>) mt.schema).put("type", "string");
                                        p.content.put(mediaTypeStr, mt);
                                    }
                                }
                                if (paramExamples.containsKey(pName)) {
                                    p.example = paramExamples.get(pName);
                                }
                                if (paramExamplesMap.containsKey(pName)) {
                                    p.examples = paramExamplesMap.get(pName);
                                }
                                
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
                                if (!api.components.securitySchemes.containsKey("BearerAuth")) {
                                    openapi.SecurityScheme sc = new openapi.SecurityScheme();
                                    sc.type = "http";
                                    sc.scheme = "bearer";
                                    api.components.securitySchemes.put("BearerAuth", sc);
                                }
                                
                                if (op.security == null) {
                                    op.security = new java.util.ArrayList<>();
                                    openapi.SecurityRequirement req = new openapi.SecurityRequirement();
                                    req.requirements = new java.util.HashMap<>();
                                    req.requirements.put("BearerAuth", new java.util.ArrayList<>());
                                    op.security.add(req);
                                }
                            }


                            
                            if (op.parameters.isEmpty()) op.parameters = null;

                            if (op.responses == null) {
                                op.responses = new openapi.Responses();
                                op.responses.statusCodes = new java.util.HashMap<>();
                            } else if (op.responses.statusCodes == null) {
                                op.responses.statusCodes = new java.util.HashMap<>();
                            }
                            if (op.responses.statusCodes.isEmpty() && op.responses.defaultResponse == null) {
                                openapi.Response r200 = new openapi.Response();
                                r200.description = "Successful response";
                                op.responses.statusCodes.put("200", r200);
                            }
                            
                            switch (method) {

                                case "GET": item.get = op; break;
                                case "POST": item.post = op; break;
                                case "PUT": item.put = op; break;
                                case "DELETE": item.delete = op; break;
                                case "PATCH": item.patch = op; break;
                                case "QUERY": item.query = op; break;
                                case "OPTIONS": item.options = op; break;
                                case "HEAD": item.head = op; break;
                                case "TRACE": item.trace = op; break;
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
