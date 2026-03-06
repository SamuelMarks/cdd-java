package cli;

import openapi.*;
import java.util.Map;

public class Emit {
    public Emit() {}

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public static String emitCli(OpenAPI api) {
        StringBuilder sb = new StringBuilder();
        sb.append("package cli;\n\n");
        sb.append("import java.util.Arrays;\n\n");
        sb.append("public class SdkCli {\n");
        sb.append("    public SdkCli() {}\n\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        if (args.length == 0 || args[0].equals(\"--help\") || args[0].equals(\"-h\")) {\n");
        sb.append("            printHelp();\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("        String command = args[0];\n");
        sb.append("        System.err.println(\"Unknown command: \" + command);\n");
        sb.append("        printHelp();\n");
        sb.append("    }\n\n");
        
        sb.append("    private static void printHelp() {\n");
        sb.append("        System.out.println(\"SDK CLI\");\n");
        
        if (api.info != null) {
            if (api.info.title != null) sb.append("        System.out.println(\"Info Object title: ").append(escape(api.info.title)).append("\");\n");
            if (api.info.version != null) sb.append("        System.out.println(\"Info Object version: ").append(escape(api.info.version)).append("\");\n");
            if (api.info.summary != null) sb.append("        System.out.println(\"Info Object summary: ").append(escape(api.info.summary)).append("\");\n");
            if (api.info.description != null) sb.append("        System.out.println(\"Info Object description: ").append(escape(api.info.description)).append("\");\n");
            if (api.info.termsOfService != null) sb.append("        System.out.println(\"Info Object termsOfService: ").append(escape(api.info.termsOfService)).append("\");\n");
            
            if (api.info.contact != null) {
                if (api.info.contact.name != null) sb.append("        System.out.println(\"Contact Object name: ").append(escape(api.info.contact.name)).append("\");\n");
                if (api.info.contact.email != null) sb.append("        System.out.println(\"Contact Object email: ").append(escape(api.info.contact.email)).append("\");\n");
                if (api.info.contact.url != null) sb.append("        System.out.println(\"Contact Object url: ").append(escape(api.info.contact.url)).append("\");\n");
            }
            if (api.info.license != null) {
                if (api.info.license.name != null) sb.append("        System.out.println(\"License Object name: ").append(escape(api.info.license.name)).append("\");\n");
                if (api.info.license.identifier != null) sb.append("        System.out.println(\"License Object identifier: ").append(escape(api.info.license.identifier)).append("\");\n");
                if (api.info.license.url != null) sb.append("        System.out.println(\"License Object url: ").append(escape(api.info.license.url)).append("\");\n");
            }
        }
        
        if (api.servers != null) {
            for (Server s : api.servers) {
                sb.append("        System.out.println(\"Server Object url: ").append(escape(s.url))
                  .append(" name: ").append(escape(s.name == null ? "" : s.name))
                  .append(" description: ").append(escape(s.description == null ? "" : s.description)).append("\");\n");
                  
                if (s.variables != null) {
                    for (Map.Entry<String, ServerVariable> ve : s.variables.entrySet()) {
                        ServerVariable v = ve.getValue();
                        String enums = v.enumValues != null ? String.join(",", v.enumValues) : "";
                        sb.append("        System.out.println(\"Server Variable Object ").append(escape(ve.getKey()))
                          .append(" defaultValue: ").append(escape(v.defaultValue))
                          .append(" description: ").append(escape(v.description == null ? "" : v.description))
                          .append(" enumValues: ").append(escape(enums)).append("\");\n");
                    }
                }
            }
        }
        
        if (api.components != null) {
            if (api.components.schemas != null) {
                for (java.util.Map.Entry<String, Object> entry : api.components.schemas.entrySet()) {
                    String k = entry.getKey();
                    sb.append("        System.out.println(\"Component schemas ").append(escape(k)).append("\");\n");
                    Object val = entry.getValue();
                    if (val instanceof java.util.Map) {
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) val;
                        if (map.containsKey("discriminator")) {
                            Object dObj = map.get("discriminator");
                            if (dObj instanceof openapi.Discriminator) {
                                openapi.Discriminator d = (openapi.Discriminator) dObj;
                                String mappingStr = "";
                                if (d.mapping != null) {
                                    java.util.List<String> mappings = new java.util.ArrayList<>();
                                    for (java.util.Map.Entry<String, String> me : d.mapping.entrySet()) {
                                        mappings.add(me.getKey() + "=" + me.getValue());
                                    }
                                    mappingStr = String.join(",", mappings);
                                }
                                sb.append("        System.out.println(\"  Discriminator propertyName=").append(escape(d.propertyName)).append(" mapping=").append(escape(mappingStr)).append(" defaultMapping=").append(escape(d.defaultMapping)).append("\");\n");
                            } else if (dObj instanceof java.util.Map) {
                                java.util.Map<String, Object> d = (java.util.Map<String, Object>) dObj;
                                String mappingStr = "";
                                if (d.get("mapping") instanceof java.util.Map) {
                                    java.util.Map<String, String> m = (java.util.Map<String, String>) d.get("mapping");
                                    java.util.List<String> mappings = new java.util.ArrayList<>();
                                    for (java.util.Map.Entry<String, String> me : m.entrySet()) {
                                        mappings.add(me.getKey() + "=" + me.getValue());
                                    }
                                    mappingStr = String.join(",", mappings);
                                }
                                sb.append("        System.out.println(\"  Discriminator propertyName=").append(escape((String)d.get("propertyName"))).append(" mapping=").append(escape(mappingStr)).append(" defaultMapping=").append(escape((String)d.get("defaultMapping"))).append("\");\n");
                            }
                        }
                        if (map.containsKey("xml")) {
                            Object xmlObj = map.get("xml");
                            if (xmlObj instanceof openapi.XML) {
                                openapi.XML x = (openapi.XML) xmlObj;
                                sb.append("        System.out.println(\"  XML name=").append(escape(x.name)).append(" namespace=").append(escape(x.namespace)).append(" prefix=").append(escape(x.prefix)).append(" attribute=").append(x.attribute != null && x.attribute ? "true" : "false").append(" wrapped=").append(x.wrapped != null && x.wrapped ? "true" : "false").append("\");\n");
                            } else if (xmlObj instanceof java.util.Map) {
                                java.util.Map<String, Object> x = (java.util.Map<String, Object>) xmlObj;
                                Boolean attr = (Boolean) x.get("attribute");
                                Boolean wrapped = (Boolean) x.get("wrapped");
                                sb.append("        System.out.println(\"  XML name=").append(escape((String)x.get("name"))).append(" namespace=").append(escape((String)x.get("namespace"))).append(" prefix=").append(escape((String)x.get("prefix"))).append(" attribute=").append(attr != null && attr ? "true" : "false").append(" wrapped=").append(wrapped != null && wrapped ? "true" : "false").append("\");\n");
                            }
                        }
                    } else if (val instanceof openapi.Schema) {
                        openapi.Schema schema = (openapi.Schema) val;
                        if (schema.discriminator != null) {
                            String mappingStr = "";
                            if (schema.discriminator.mapping != null) {
                                java.util.List<String> mappings = new java.util.ArrayList<>();
                                for (java.util.Map.Entry<String, String> me : schema.discriminator.mapping.entrySet()) {
                                    mappings.add(me.getKey() + "=" + me.getValue());
                                }
                                mappingStr = String.join(",", mappings);
                            }
                            sb.append("        System.out.println(\"  Discriminator propertyName=").append(escape(schema.discriminator.propertyName)).append(" mapping=").append(escape(mappingStr)).append(" defaultMapping=").append(escape(schema.discriminator.defaultMapping)).append("\");\n");
                        }
                        if (schema.xml != null) {
                            sb.append("        System.out.println(\"  XML name=").append(escape(schema.xml.name)).append(" namespace=").append(escape(schema.xml.namespace)).append(" prefix=").append(escape(schema.xml.prefix)).append(" attribute=").append(schema.xml.attribute != null && schema.xml.attribute ? "true" : "false").append(" wrapped=").append(schema.xml.wrapped != null && schema.xml.wrapped ? "true" : "false").append("\");\n");
                        }
                    }
                }
            }

            if (api.components.responses != null) for (String k : api.components.responses.keySet()) sb.append("        System.out.println(\"Component responses ").append(escape(k)).append("\");\n");
            if (api.components.parameters != null) for (String k : api.components.parameters.keySet()) sb.append("        System.out.println(\"Component parameters ").append(escape(k)).append("\");\n");
            if (api.components.requestBodies != null) for (String k : api.components.requestBodies.keySet()) sb.append("        System.out.println(\"Component requestBodies ").append(escape(k)).append("\");\n");
            if (api.components.headers != null) for (String k : api.components.headers.keySet()) sb.append("        System.out.println(\"Component headers ").append(escape(k)).append("\");\n");
            if (api.components.securitySchemes != null) {
                for (java.util.Map.Entry<String, Object> entry : api.components.securitySchemes.entrySet()) {
                    String k = entry.getKey();
                    if (entry.getValue() instanceof openapi.SecurityScheme) {
                        openapi.SecurityScheme sc = (openapi.SecurityScheme) entry.getValue();
                        sb.append("        System.out.println(\"Component securitySchemes ").append(escape(k))
                          .append(" type=").append(sc.type != null ? escape(sc.type) : "-")
                          .append(" scheme=").append(sc.scheme != null ? escape(sc.scheme) : "-")
                          .append(" in=").append(sc.in != null ? escape(sc.in) : "-")
                          .append(" name=").append(sc.name != null ? escape(sc.name) : "-")
                          .append(" bearerFormat=").append(sc.bearerFormat != null ? escape(sc.bearerFormat) : "-")
                          .append(" openIdConnectUrl=").append(sc.openIdConnectUrl != null ? escape(sc.openIdConnectUrl) : "-")
                          .append(" oauth2MetadataUrl=").append(sc.oauth2MetadataUrl != null ? escape(sc.oauth2MetadataUrl) : "-")
                          .append(" deprecated=").append(sc.deprecated != null && sc.deprecated ? "true" : "false");
                        if (sc.description != null) {
                            sb.append(" description=\\\"").append(escape(sc.description)).append("\\\"");
                        }
                        sb.append("\");\n");

                        if (sc.flows != null) {
                            if (sc.flows.implicit != null) {
                                sb.append("        System.out.println(\"Component securitySchemesFlow ").append(escape(k)).append(" implicit ").append(sc.flows.implicit.authorizationUrl != null ? escape(sc.flows.implicit.authorizationUrl) : "-").append(" ").append(sc.flows.implicit.tokenUrl != null ? escape(sc.flows.implicit.tokenUrl) : "-").append(" ").append(sc.flows.implicit.refreshUrl != null ? escape(sc.flows.implicit.refreshUrl) : "-").append(" ").append(sc.flows.implicit.deviceAuthorizationUrl != null ? escape(sc.flows.implicit.deviceAuthorizationUrl) : "-").append("\");\n");
                                if (sc.flows.implicit.scopes != null) {
                                    for (Map.Entry<String, String> entry2 : sc.flows.implicit.scopes.entrySet()) {
                                        sb.append("        System.out.println(\"Component securitySchemesFlowScope ").append(escape(k)).append(" implicit ").append(escape(entry2.getKey())).append(" ").append(escape(entry2.getValue())).append("\");\n");
                                    }
                                }
                            }
                            if (sc.flows.password != null) {
                                sb.append("        System.out.println(\"Component securitySchemesFlow ").append(escape(k)).append(" password ").append(sc.flows.password.authorizationUrl != null ? escape(sc.flows.password.authorizationUrl) : "-").append(" ").append(sc.flows.password.tokenUrl != null ? escape(sc.flows.password.tokenUrl) : "-").append(" ").append(sc.flows.password.refreshUrl != null ? escape(sc.flows.password.refreshUrl) : "-").append(" ").append(sc.flows.password.deviceAuthorizationUrl != null ? escape(sc.flows.password.deviceAuthorizationUrl) : "-").append("\");\n");
                                if (sc.flows.password.scopes != null) {
                                    for (Map.Entry<String, String> entry2 : sc.flows.password.scopes.entrySet()) {
                                        sb.append("        System.out.println(\"Component securitySchemesFlowScope ").append(escape(k)).append(" password ").append(escape(entry2.getKey())).append(" ").append(escape(entry2.getValue())).append("\");\n");
                                    }
                                }
                            }
                            if (sc.flows.clientCredentials != null) {
                                sb.append("        System.out.println(\"Component securitySchemesFlow ").append(escape(k)).append(" clientCredentials ").append(sc.flows.clientCredentials.authorizationUrl != null ? escape(sc.flows.clientCredentials.authorizationUrl) : "-").append(" ").append(sc.flows.clientCredentials.tokenUrl != null ? escape(sc.flows.clientCredentials.tokenUrl) : "-").append(" ").append(sc.flows.clientCredentials.refreshUrl != null ? escape(sc.flows.clientCredentials.refreshUrl) : "-").append(" ").append(sc.flows.clientCredentials.deviceAuthorizationUrl != null ? escape(sc.flows.clientCredentials.deviceAuthorizationUrl) : "-").append("\");\n");
                                if (sc.flows.clientCredentials.scopes != null) {
                                    for (Map.Entry<String, String> entry2 : sc.flows.clientCredentials.scopes.entrySet()) {
                                        sb.append("        System.out.println(\"Component securitySchemesFlowScope ").append(escape(k)).append(" clientCredentials ").append(escape(entry2.getKey())).append(" ").append(escape(entry2.getValue())).append("\");\n");
                                    }
                                }
                            }
                            if (sc.flows.authorizationCode != null) {
                                sb.append("        System.out.println(\"Component securitySchemesFlow ").append(escape(k)).append(" authorizationCode ").append(sc.flows.authorizationCode.authorizationUrl != null ? escape(sc.flows.authorizationCode.authorizationUrl) : "-").append(" ").append(sc.flows.authorizationCode.tokenUrl != null ? escape(sc.flows.authorizationCode.tokenUrl) : "-").append(" ").append(sc.flows.authorizationCode.refreshUrl != null ? escape(sc.flows.authorizationCode.refreshUrl) : "-").append(" ").append(sc.flows.authorizationCode.deviceAuthorizationUrl != null ? escape(sc.flows.authorizationCode.deviceAuthorizationUrl) : "-").append("\");\n");
                                if (sc.flows.authorizationCode.scopes != null) {
                                    for (Map.Entry<String, String> entry2 : sc.flows.authorizationCode.scopes.entrySet()) {
                                        sb.append("        System.out.println(\"Component securitySchemesFlowScope ").append(escape(k)).append(" authorizationCode ").append(escape(entry2.getKey())).append(" ").append(escape(entry2.getValue())).append("\");\n");
                                    }
                                }
                            }
                            if (sc.flows.deviceAuthorization != null) {
                                sb.append("        System.out.println(\"Component securitySchemesFlow ").append(escape(k)).append(" deviceAuthorization ").append(sc.flows.deviceAuthorization.authorizationUrl != null ? escape(sc.flows.deviceAuthorization.authorizationUrl) : "-").append(" ").append(sc.flows.deviceAuthorization.tokenUrl != null ? escape(sc.flows.deviceAuthorization.tokenUrl) : "-").append(" ").append(sc.flows.deviceAuthorization.refreshUrl != null ? escape(sc.flows.deviceAuthorization.refreshUrl) : "-").append(" ").append(sc.flows.deviceAuthorization.deviceAuthorizationUrl != null ? escape(sc.flows.deviceAuthorization.deviceAuthorizationUrl) : "-").append("\");\n");
                                if (sc.flows.deviceAuthorization.scopes != null) {
                                    for (Map.Entry<String, String> entry2 : sc.flows.deviceAuthorization.scopes.entrySet()) {
                                        sb.append("        System.out.println(\"Component securitySchemesFlowScope ").append(escape(k)).append(" deviceAuthorization ").append(escape(entry2.getKey())).append(" ").append(escape(entry2.getValue())).append("\");\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (api.components.links != null) {
                for (java.util.Map.Entry<String, Object> entry : api.components.links.entrySet()) {
                    String k = entry.getKey();
                    if (entry.getValue() instanceof openapi.Link) {
                        openapi.Link lnk = (openapi.Link) entry.getValue();
                        sb.append("        System.out.println(\"Component links ").append(escape(k));
                        if (lnk.operationId != null) sb.append(" operationId=").append(escape(lnk.operationId));
                        if (lnk.operationRef != null) sb.append(" operationRef=").append(escape(lnk.operationRef));
                        if (lnk.description != null) sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
                        if (lnk.requestBody != null) sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
                        if (lnk.server != null && lnk.server.url != null) sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
                        sb.append("\");\n");
                        if (lnk.parameters != null) {
                            for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
                                sb.append("        System.out.println(\"Component linksParam ").append(escape(k)).append(" ").append(escape(pe.getKey())).append(" ").append(escape(pe.getValue().toString())).append("\");\n");
                            }
                        }
                    } else {
                        sb.append("        System.out.println(\"Component links ").append(escape(k)).append("\");\n");
                    }
                }
            }
            if (api.components.callbacks != null) for (String k : api.components.callbacks.keySet()) sb.append("        System.out.println(\"Component callbacks ").append(escape(k)).append("\");\n");
            if (api.components.pathItems != null) for (String k : api.components.pathItems.keySet()) sb.append("        System.out.println(\"Component pathItems ").append(escape(k)).append("\");\n");
            if (api.components.mediaTypes != null) for (String k : api.components.mediaTypes.keySet()) sb.append("        System.out.println(\"Component mediaTypes ").append(escape(k)).append("\");\n");
        }
        
        sb.append("        System.out.println(\"Commands:\");\n");
        
        if (api.paths != null && api.paths.pathItems != null) {
            for (Map.Entry<String, PathItem> pe : api.paths.pathItems.entrySet()) {
                String path = pe.getKey();
                PathItem pi = pe.getValue();
                
                if (pi.get != null) appendOp(sb, "get", path, pi.get);
                if (pi.post != null) appendOp(sb, "post", path, pi.post);
                if (pi.put != null) appendOp(sb, "put", path, pi.put);
                if (pi.delete != null) appendOp(sb, "delete", path, pi.delete);
                if (pi.patch != null) appendOp(sb, "patch", path, pi.patch);
                if (pi.options != null) appendOp(sb, "options", path, pi.options);
                if (pi.head != null) appendOp(sb, "head", path, pi.head);
                if (pi.trace != null) appendOp(sb, "trace", path, pi.trace);
                if (pi.query != null) appendOp(sb, "query", path, pi.query);
            }
        }
        
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private static void appendOp(StringBuilder sb, String method, String path, Operation op) {
        sb.append("        System.out.println(\"Operation: ").append(method).append(" ").append(escape(path)).append("\");\n");
        String tags = op.tags != null ? " (Tags: " + String.join(", ", op.tags) + ")" : "";
        String dep = (op.deprecated != null && op.deprecated) ? "[DEPRECATED] " : "";
        String sum = op.summary != null ? op.summary : "";
        sb.append("        System.out.println(\"Operation Object ").append(dep).append(escape(sum)).append(escape(tags)).append("\");\n");
        if (op.operationId != null) {
            sb.append("        System.out.println(\"    OperationId: ").append(escape(op.operationId)).append("\");\n");
        }
        if (op.description != null) {
            sb.append("        System.out.println(\"    Description: ").append(escape(op.description)).append("\");\n");
        }
        
        if (op.externalDocs != null) {
            sb.append("        System.out.println(\"See also: ").append(escape(op.externalDocs.url)).append(" ").append(escape(op.externalDocs.description == null ? "" : op.externalDocs.description)).append("\");\n");
        }
        
        if (op.callbacks != null) {
            for (String ck : op.callbacks.keySet()) {
                sb.append("        System.out.println(\"Callback: ").append(escape(ck)).append("\");\n");
            }
        }
        
        if (op.parameters != null) {
            for (Object pObj : op.parameters) {
                if (!(pObj instanceof Parameter)) continue;
                Parameter p = (Parameter) pObj;
                String req = (p.required != null && p.required) ? " (required)" : "";
                String pDep = (p.deprecated != null && p.deprecated) ? " [DEPRECATED]" : "";
                sb.append("        System.out.println(\"    --").append(escape(p.name)).append(req).append(pDep).append(" : ").append(escape(p.description == null ? "" : p.description)).append("\");\n");
                if (p.example != null) {
                    sb.append("        System.out.println(\"      Example: ").append(escape(p.example.toString())).append("\");\n");
                }
                if (p.examples != null) {
                    for (Map.Entry<String, Example> exEntry : p.examples.entrySet()) {
                        String key = exEntry.getKey();
                        Example ex = exEntry.getValue();
                        if (ex == null) continue;
                        String exSum = ex.summary != null ? ex.summary : "null";
                        String exDesc = ex.description != null ? ex.description : "null";
                        String exVal = ex.value != null ? ex.value.toString() : "null";
                        sb.append("        System.out.println(\"      Example ").append(escape(key)).append(": ").append(escape(exSum)).append(" - ").append(escape(exDesc)).append(" = ").append(escape(exVal)).append("\");\n");
                    }
                }
            }
        }
        

        if (op.requestBody != null && op.requestBody instanceof RequestBody) {
            String rreq = (((RequestBody)op.requestBody).required != null && ((RequestBody)op.requestBody).required) ? " (required)" : "";
            String rdesc = ((RequestBody)op.requestBody).description != null ? ((RequestBody)op.requestBody).description : "";
            String rct = ((RequestBody)op.requestBody).content != null ? " [Content-Types: " + String.join(", ", ((RequestBody)op.requestBody).content.keySet()) + "]" : "";
            sb.append("        System.out.println(\"    --requestBody").append(rreq).append(": ").append(escape(rdesc)).append(escape(rct)).append("\");\n");
            
            if (((RequestBody)op.requestBody).content != null) {
                for (Map.Entry<String, MediaType> mEntry : ((RequestBody)op.requestBody).content.entrySet()) {
                    MediaType mt = mEntry.getValue();
                    if (mt.itemSchema != null && mt.itemSchema instanceof Map) {
                        Map<String, Object> schMap = (Map<String, Object>) mt.itemSchema;
                        if (schMap.containsKey("type")) {
                            sb.append("        System.out.println(\"      RequestBodyContentItemSchema \" + escape(mEntry.getKey()) + \" \" + escape(schMap.get(\"type\").toString()) + \"\");\n");
                        }
                    }
                    if (mt.prefixEncoding != null) {
                        for (Encoding enc : mt.prefixEncoding) {
                            if (enc.contentType != null) {
                                sb.append("        System.out.println(\"      RequestBodyContentPrefixEncoding \" + escape(mEntry.getKey()) + \" \" + escape(enc.contentType) + \"\");\n");
                            }
                        }
                    }
                    if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
                        sb.append("        System.out.println(\"      RequestBodyContentItemEncoding \" + escape(mEntry.getKey()) + \" \" + escape(mt.itemEncoding.contentType) + \"\");\n");
                    }
                    if (mt.encoding != null) {
                        for (Map.Entry<String, Encoding> encEntry : mt.encoding.entrySet()) {
                            Encoding enc = encEntry.getValue();
                            if (enc.contentType != null) {
                                sb.append("        System.out.println(\"      RequestBodyEncoding \" + escape(mEntry.getKey()) + \" \" + escape(encEntry.getKey()) + \" \" + escape(enc.contentType) + \"\");\n");
                            }
                            if (enc.prefixEncoding != null) {
                                for (Encoding pEnc : enc.prefixEncoding) {
                                    if (pEnc.contentType != null) {
                                        sb.append("        System.out.println(\"      RequestBodyEncodingPrefixEncoding \" + escape(mEntry.getKey()) + \" \" + escape(encEntry.getKey()) + \" \" + escape(pEnc.contentType) + \"\");\n");
                                    }
                                }
                            }
                            if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
                                sb.append("        System.out.println(\"      RequestBodyEncodingItemEncoding \" + escape(mEntry.getKey()) + \" \" + escape(encEntry.getKey()) + \" \" + escape(enc.itemEncoding.contentType) + \"\");\n");
                            }
                        }
                    }
                }
            }
        }
        
        if (op.responses != null && op.responses.statusCodes != null) {
            for (Map.Entry<String, Object> reEntry : op.responses.statusCodes.entrySet()) {
                String code = reEntry.getKey();
                if (!(reEntry.getValue() instanceof Response)) continue;
                Response r = (Response) reEntry.getValue();
                String rdesc = r.description != null ? r.description : "";
                String rct = r.content != null ? " [Content-Types: " + String.join(", ", r.content.keySet()) + "]" : "";
                sb.append("        System.out.println(\"    Returns ").append(escape(code)).append(": ").append(escape(rdesc)).append(escape(rct)).append("\");\n");
                
                if (r.headers != null) {
                    for (Map.Entry<String, Object> heEntry : r.headers.entrySet()) {
                        String hName = heEntry.getKey();
                        if (!(heEntry.getValue() instanceof Header)) continue;
                        Header h = (Header) heEntry.getValue();
                        String hreq = (h.required != null && h.required) ? " (required)" : "";
                        String hDep = (h.deprecated != null && h.deprecated) ? " [DEPRECATED]" : "";
                        sb.append("        System.out.println(\"      Header ").append(escape(hName)).append(hreq).append(hDep).append(": ").append(escape(h.description == null ? "" : h.description)).append("\");\n");
                    }
                }
                if (r.links != null) {
                    for (Map.Entry<String, openapi.Link> leEntry : r.links.entrySet()) {
                        String lName = leEntry.getKey();
                        if (!(leEntry.getValue() instanceof Link)) continue;
                        Link lnk = (Link) leEntry.getValue();
                        sb.append("        System.out.println(\"      Link ").append(escape(lName));
                        if (lnk.operationId != null) sb.append(" operationId=").append(escape(lnk.operationId));
                        if (lnk.operationRef != null) sb.append(" operationRef=").append(escape(lnk.operationRef));
                        if (lnk.description != null) sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
                        if (lnk.requestBody != null) sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
                        if (lnk.server != null && lnk.server.url != null) sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
                        sb.append("\");\n");
                        
                        if (lnk.parameters != null) {
                            for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
                                sb.append("        System.out.println(\"        LinkParam ").append(escape(lName)).append(" ").append(escape(pe.getKey())).append(" ").append(escape(pe.getValue().toString())).append("\");\n");
                            }
                        }
                    }
                }
            }
        }
        if (op.responses != null && op.responses.defaultResponse != null && op.responses.defaultResponse instanceof Response) {
            Response r = (Response) op.responses.defaultResponse;
            String rdesc = r.description != null ? r.description : "";
            String rct = r.content != null ? " [Content-Types: " + String.join(", ", r.content.keySet()) + "]" : "";
            sb.append("        System.out.println(\"    Returns default: ").append(escape(rdesc)).append(escape(rct)).append("\");\n");
            if (r.headers != null) {
                for (Map.Entry<String, Object> heEntry : r.headers.entrySet()) {
                    String hName = heEntry.getKey();
                    if (!(heEntry.getValue() instanceof Header)) continue;
                    Header h = (Header) heEntry.getValue();
                    String hreq = (h.required != null && h.required) ? " (required)" : "";
                    String hDep = (h.deprecated != null && h.deprecated) ? " [DEPRECATED]" : "";
                    sb.append("        System.out.println(\"      Header ").append(escape(hName)).append(hreq).append(hDep).append(": ").append(escape(h.description == null ? "" : h.description)).append("\");\n");
                }
            }
            if (r.links != null) {
                for (Map.Entry<String, Link> leEntry : r.links.entrySet()) {
                    String lName = leEntry.getKey();
                    Link lnk = leEntry.getValue();
                    sb.append("        System.out.println(\"      Link ").append(escape(lName));
                    if (lnk.operationId != null) sb.append(" operationId=").append(escape(lnk.operationId));
                    if (lnk.operationRef != null) sb.append(" operationRef=").append(escape(lnk.operationRef));
                    if (lnk.description != null) sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
                    if (lnk.requestBody != null) sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
                    if (lnk.server != null && lnk.server.url != null) sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
                    sb.append("\");\n");
                    
                    if (lnk.parameters != null) {
                        for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
                            sb.append("        System.out.println(\"        LinkParam ").append(escape(lName)).append(" ").append(escape(pe.getKey())).append(" ").append(escape(pe.getValue().toString())).append("\");\n");
                        }
                    }
                }
            }
        }
    }
}
