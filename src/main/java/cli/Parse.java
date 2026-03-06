package cli;

import openapi.*;
import java.util.*;
import java.util.regex.*;

public class Parse {
    public Parse() {}

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static OpenAPI parse(String existingSource) {
        OpenAPI api = new OpenAPI();
        String helpBody = "";
        Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{([^}]+)\\}").matcher(existingSource);
        if (hm.find()) {
            helpBody = hm.group(1);
        }
        
        Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(helpBody);
        
        Operation currentOp = null;
        Parameter currentParameter = null;
        String currentPath = null;
        Response currentResponse = null;
        Map<String, Object> lastParsedSchema = null;
        
        while (lm.find()) {
            String line = unescape(lm.group(1));
            
            if (line.startsWith("Info Object title: ")) {
                if (api.info == null) api.info = new Info();
                api.info.title = line.substring(19);
            } else if (line.startsWith("Info Object version: ")) {
                if (api.info == null) api.info = new Info();
                api.info.version = line.substring(21);
            } else if (line.startsWith("Info Object summary: ")) {
                if (api.info == null) api.info = new Info();
                api.info.summary = line.substring(21);
            } else if (line.startsWith("Info Object description: ")) {
                if (api.info == null) api.info = new Info();
                api.info.description = line.substring(25);
            } else if (line.startsWith("Info Object termsOfService: ")) {
                if (api.info == null) api.info = new Info();
                api.info.termsOfService = line.substring(28);
            } else if (line.startsWith("Contact Object name: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.contact == null) api.info.contact = new Contact();
                api.info.contact.name = line.substring(21);
            } else if (line.startsWith("Contact Object email: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.contact == null) api.info.contact = new Contact();
                api.info.contact.email = line.substring(22);
            } else if (line.startsWith("Contact Object url: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.contact == null) api.info.contact = new Contact();
                api.info.contact.url = line.substring(20);
            } else if (line.startsWith("License Object name: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.license == null) api.info.license = new License();
                api.info.license.name = line.substring(21);
            } else if (line.startsWith("License Object identifier: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.license == null) api.info.license = new License();
                api.info.license.identifier = line.substring(27);
            } else if (line.startsWith("License Object url: ")) {
                if (api.info == null) api.info = new Info();
                if (api.info.license == null) api.info.license = new License();
                api.info.license.url = line.substring(20);
            } else if (line.startsWith("Server Object url: ")) {
                if (api.servers == null) api.servers = new ArrayList<>();
                Server s = new Server();
                String[] parts = line.substring(19).split(" name: | description: ");
                if (parts.length > 0) s.url = parts[0];
                if (line.contains(" name: ")) {
                    int nIdx = line.indexOf(" name: ") + 7;
                    int dIdx = line.indexOf(" description: ");
                    if (dIdx > nIdx) s.name = line.substring(nIdx, dIdx);
                    else s.name = line.substring(nIdx);
                    if (s.name.isEmpty()) s.name = null;
                }
                if (line.contains(" description: ")) {
                    s.description = line.substring(line.indexOf(" description: ") + 14);
                    if (s.description.isEmpty()) s.description = null;
                }
                api.servers.add(s);
            } else if (line.startsWith("Server Variable Object ")) {
                if (api.servers != null && !api.servers.isEmpty()) {
                    Server s = api.servers.get(api.servers.size() - 1);
                    if (s.variables == null) s.variables = new HashMap<>();
                    String rem = line.substring(23);
                    int dIdx = rem.indexOf(" defaultValue: ");
                    String name = rem.substring(0, dIdx);
                    rem = rem.substring(dIdx + 15);
                    int descIdx = rem.indexOf(" description: ");
                    String def = rem.substring(0, descIdx);
                    rem = rem.substring(descIdx + 14);
                    int enumIdx = rem.indexOf(" enumValues: ");
                    String desc = rem.substring(0, enumIdx);
                    String enums = rem.substring(enumIdx + 13);
                    ServerVariable sv = new ServerVariable();
                    sv.defaultValue = def;
                    if (!desc.isEmpty()) sv.description = desc;
                    if (!enums.isEmpty()) sv.enumValues = Arrays.asList(enums.split(", "));
                    s.variables.put(name, sv);
                }
            } else if (line.startsWith("Component schemas ")) {
                if (api.components == null) api.components = new Components();
                if (api.components.schemas == null) api.components.schemas = new HashMap<>();
                String key = line.substring("Component schemas ".length());
                lastParsedSchema = new HashMap<>();
                api.components.schemas.put(key, lastParsedSchema);
            } else if (line.startsWith("  Discriminator propertyName=")) {
                if (lastParsedSchema != null) {
                    Matcher m = Pattern.compile("  Discriminator propertyName=(.*?) mapping=(.*?) defaultMapping=(.*)").matcher(line);
                    if (m.matches()) {
                        Discriminator d = new Discriminator();
                        String pName = m.group(1); if (!pName.isEmpty() && !pName.equals("null")) d.propertyName = pName;
                        String mappingStr = m.group(2);
                        if (!mappingStr.isEmpty() && !mappingStr.equals("null")) {
                            d.mapping = new HashMap<>();
                            for (String pair : mappingStr.split(",")) {
                                String[] kv = pair.split("=");
                                if (kv.length == 2) d.mapping.put(kv[0], kv[1]);
                            }
                        }
                        String dMapping = m.group(3); if (!dMapping.isEmpty() && !dMapping.equals("null")) d.defaultMapping = dMapping;
                        lastParsedSchema.put("discriminator", d);
                    }
                }
            } else if (line.startsWith("  XML name=")) {
                if (lastParsedSchema != null) {
                    Matcher m = Pattern.compile("  XML name=(.*?) namespace=(.*?) prefix=(.*?) attribute=(.*?) wrapped=(.*)").matcher(line);
                    if (m.matches()) {
                        XML x = new XML();
                        String name = m.group(1); if (!name.isEmpty() && !name.equals("null")) x.name = name;
                        String namespace = m.group(2); if (!namespace.isEmpty() && !namespace.equals("null")) x.namespace = namespace;
                        String prefix = m.group(3); if (!prefix.isEmpty() && !prefix.equals("null")) x.prefix = prefix;
                        String attr = m.group(4); if (attr.equals("true")) x.attribute = true; else if (attr.equals("false")) x.attribute = false;
                        String wrapped = m.group(5); if (wrapped.equals("true")) x.wrapped = true; else if (wrapped.equals("false")) x.wrapped = false;
                        lastParsedSchema.put("xml", x);
                    }
                }
            } else if (line.startsWith("Component securitySchemes ")) {
                if (api.components == null) api.components = new Components();
                if (api.components.securitySchemes == null) api.components.securitySchemes = new HashMap<>();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("Component securitySchemes (\\S+) type=([^ ]+) scheme=([^ ]+) in=([^ ]+) name=([^ ]+) bearerFormat=([^ ]+) openIdConnectUrl=([^ ]+) oauth2MetadataUrl=([^ ]+) deprecated=([^ ]+)(?: description=\\\"(.+?)\\\"?)?").matcher(line);
                if (m.matches()) {
                    SecurityScheme sc = new SecurityScheme();
                    String key = m.group(1);
                    String type = m.group(2); if (!type.equals("-")) sc.type = unescape(type);
                    String scheme = m.group(3); if (!scheme.equals("-")) sc.scheme = unescape(scheme);
                    String in = m.group(4); if (!in.equals("-")) sc.in = unescape(in);
                    String name = m.group(5); if (!name.equals("-")) sc.name = unescape(name);
                    String bearerFormat = m.group(6); if (!bearerFormat.equals("-")) sc.bearerFormat = unescape(bearerFormat);
                    String openIdConnectUrl = m.group(7); if (!openIdConnectUrl.equals("-")) sc.openIdConnectUrl = unescape(openIdConnectUrl);
                    String oauth2MetadataUrl = m.group(8); if (!oauth2MetadataUrl.equals("-")) sc.oauth2MetadataUrl = unescape(oauth2MetadataUrl);
                    String deprecated = m.group(9); if (deprecated.equals("true")) sc.deprecated = true;
                    String desc = m.group(10); if (desc != null) sc.description = unescape(desc);
                    api.components.securitySchemes.put(key, sc);
                }
            } else if (line.startsWith("Component securitySchemesFlowScope ")) {
                if (api.components != null && api.components.securitySchemes != null) {
                    Matcher m = Pattern.compile("^Component securitySchemesFlowScope (\\S+) (\\S+) (\\S+) (.*)$").matcher(line);
                    if (m.find()) {
                        String k = unescape(m.group(1));
                        String flowType = unescape(m.group(2));
                        String scopeKey = unescape(m.group(3));
                        String scopeDesc = unescape(m.group(4));

                        SecurityScheme sc = (SecurityScheme) api.components.securitySchemes.get(k);
                        if (sc != null && sc.flows != null) {
                            OAuthFlow flow = null;
                            if (flowType.equals("implicit")) flow = sc.flows.implicit;
                            else if (flowType.equals("password")) flow = sc.flows.password;
                            else if (flowType.equals("clientCredentials")) flow = sc.flows.clientCredentials;
                            else if (flowType.equals("authorizationCode")) flow = sc.flows.authorizationCode;
                            else if (flowType.equals("deviceAuthorization")) flow = sc.flows.deviceAuthorization;

                            if (flow != null) {
                                if (flow.scopes == null) flow.scopes = new java.util.HashMap<>();
                                flow.scopes.put(scopeKey, scopeDesc);
                            }
                        }
                    }
                }
            } else if (line.startsWith("Component securitySchemesFlow ")) {
                if (api.components != null && api.components.securitySchemes != null) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 7) {
                        String k = parts[2];
                        String flowType = parts[3];
                        String authUrl = parts[4].equals("-") ? null : unescape(parts[4]);
                        String tokenUrl = parts[5].equals("-") ? null : unescape(parts[5]);
                        String refreshUrl = parts[6].equals("-") ? null : unescape(parts[6]);
                        String devUrl = (parts.length >= 8 && !parts[7].equals("-")) ? unescape(parts[7]) : null;

                        SecurityScheme sc = (SecurityScheme) api.components.securitySchemes.get(k);
                        if (sc != null) {
                            if (sc.flows == null) sc.flows = new OAuthFlows();
                            OAuthFlow flow = new OAuthFlow();
                            flow.authorizationUrl = authUrl;
                            flow.tokenUrl = tokenUrl;
                            flow.refreshUrl = refreshUrl;
                            flow.deviceAuthorizationUrl = devUrl;

                            if (flowType.equals("implicit")) sc.flows.implicit = flow;
                            else if (flowType.equals("password")) sc.flows.password = flow;
                            else if (flowType.equals("clientCredentials")) sc.flows.clientCredentials = flow;
                            else if (flowType.equals("authorizationCode")) sc.flows.authorizationCode = flow;
                            else if (flowType.equals("deviceAuthorization")) sc.flows.deviceAuthorization = flow;
                        }
                    }
                }
            } else if (line.startsWith("Component linksParam ")) {
                if (api.components != null && api.components.links != null) {
                    String[] parts = line.substring(21).split(" ", 3);
                    if (parts.length >= 3) {
                        Link lnk = (Link) api.components.links.get(parts[0]);
                        if (lnk != null) {
                            if (lnk.parameters == null) lnk.parameters = new HashMap<>();
                            lnk.parameters.put(parts[1], parts[2]);
                        }
                    }
                }
            } else if (line.startsWith("Component links ")) {
                if (api.components == null) api.components = new Components();
                if (api.components.links == null) api.components.links = new HashMap<>();
                String rem = line.substring(16);
                String[] parts = rem.split(" ", 2);
                String key = parts[0];
                Link lnk = new Link();
                if (parts.length > 1) {
                    Matcher mId = Pattern.compile(" operationId=([^ ]+)").matcher(" " + parts[1]);
                    if (mId.find()) lnk.operationId = mId.group(1);
                    Matcher mRef = Pattern.compile(" operationRef=([^ ]+)").matcher(" " + parts[1]);
                    if (mRef.find()) lnk.operationRef = mRef.group(1);
                    Matcher mDesc = Pattern.compile(" description=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                    if (mDesc.find()) lnk.description = mDesc.group(1);
                    Matcher mReq = Pattern.compile(" requestBody=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                    if (mReq.find()) lnk.requestBody = mReq.group(1);
                    Matcher mSrv = Pattern.compile(" serverUrl=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                    if (mSrv.find()) {
                        lnk.server = new Server();
                        lnk.server.url = mSrv.group(1);
                    }
                }
                api.components.links.put(key, lnk);
            } else if (line.startsWith("Component ")) {
                if (api.components == null) api.components = new Components();
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    String type = parts[1];
                    String key = parts[2];
                    if (type.equals("responses")) { if (api.components.responses == null) api.components.responses = new HashMap<>(); api.components.responses.put(key, new Response()); }
                    else if (type.equals("parameters")) { if (api.components.parameters == null) api.components.parameters = new HashMap<>(); api.components.parameters.put(key, new Parameter()); }
                    else if (type.equals("requestBodies")) { if (api.components.requestBodies == null) api.components.requestBodies = new HashMap<>(); api.components.requestBodies.put(key, new RequestBody()); }
                    else if (type.equals("headers")) { if (api.components.headers == null) api.components.headers = new HashMap<>(); api.components.headers.put(key, new Header()); }
                    // securitySchemes handled above
                    else if (type.equals("links")) { if (api.components.links == null) api.components.links = new HashMap<>(); api.components.links.put(key, new Link()); }
                    else if (type.equals("callbacks")) { if (api.components.callbacks == null) api.components.callbacks = new HashMap<>(); api.components.callbacks.put(key, new Callback()); }
                    else if (type.equals("pathItems")) { if (api.components.pathItems == null) api.components.pathItems = new HashMap<>(); api.components.pathItems.put(key, new PathItem()); }
                    else if (type.equals("mediaTypes")) { if (api.components.mediaTypes == null) api.components.mediaTypes = new HashMap<>(); api.components.mediaTypes.put(key, new MediaType()); }
                }
            } else if (line.startsWith("Operation: ")) {
                currentParameter = null;
                String[] parts = line.split(" ");
                String method = parts[1];
                currentPath = parts[2];
                if (api.paths == null) api.paths = new Paths();
                if (api.paths.pathItems == null) api.paths.pathItems = new HashMap<>();
                PathItem pi = api.paths.pathItems.get(currentPath);
                if (pi == null) { pi = new PathItem(); api.paths.pathItems.put(currentPath, pi); }
                currentOp = new Operation();
                if (method.equals("get")) pi.get = currentOp;
                else if (method.equals("post")) pi.post = currentOp;
                else if (method.equals("put")) pi.put = currentOp;
                else if (method.equals("delete")) pi.delete = currentOp;
                else if (method.equals("patch")) pi.patch = currentOp;
                else if (method.equals("options")) pi.options = currentOp;
                else if (method.equals("head")) pi.head = currentOp;
                else if (method.equals("trace")) pi.trace = currentOp;
                else if (method.equals("query")) pi.query = currentOp;
            } else if (line.startsWith("Operation Object ")) {
                if (currentOp != null) {
                    String l = line.substring(17);
                    Matcher depMatcher = Pattern.compile("^\\[DEPRECATED\\] ").matcher(l);
                    if (depMatcher.find()) {
                        currentOp.deprecated = true;
                        l = l.substring(13);
                    }
                    Matcher tagsMatcher = Pattern.compile(" \\(Tags: (.*?)\\)$").matcher(l);
                    if (tagsMatcher.find()) {
                        currentOp.tags = Arrays.asList(tagsMatcher.group(1).split(", "));
                        l = l.substring(0, l.length() - tagsMatcher.group(0).length());
                    }
                    if (!l.isEmpty()) currentOp.summary = l;
                }
            } else if (line.startsWith("    OperationId: ")) {
                if (currentOp != null) currentOp.operationId = line.substring(17);
            } else if (line.startsWith("    Description: ")) {
                if (currentOp != null) currentOp.description = line.substring(17);
            } else if (line.startsWith("See also: ")) {
                if (currentOp != null) {
                    String[] parts = line.substring(10).split(" ", 2);
                    currentOp.externalDocs = new ExternalDocumentation();
                    currentOp.externalDocs.url = parts[0];
                    if (parts.length > 1 && !parts[1].isEmpty()) currentOp.externalDocs.description = parts[1];
                }
            } else if (line.startsWith("Callback: ")) {
                if (currentOp != null) {
                    if (currentOp.callbacks == null) currentOp.callbacks = new HashMap<>();
                    currentOp.callbacks.put(line.substring(10), new Callback());
                }
            } else if (line.startsWith("    --requestBody")) {
                currentParameter = null;
                if (currentOp != null) {
                    currentOp.requestBody = new RequestBody();
                    if (line.contains(" (required)")) {
                        ((RequestBody)currentOp.requestBody).required = true;
                        line = line.replace(" (required)", "");
                    }
                    line = line.substring(17); // past "    --requestBody: " or "    --requestBody"
                    if (line.startsWith(": ")) line = line.substring(2);
                    
                    Matcher ctMatcher = Pattern.compile(" \\[Content-Types: (.*?)\\]$").matcher(line);
                    if (ctMatcher.find()) {
                        ((RequestBody)currentOp.requestBody).content = new HashMap<>();
                        for (String ct : ctMatcher.group(1).split(", ")) {
                            ((RequestBody)currentOp.requestBody).content.put(ct, new MediaType());
                        }
                        line = line.substring(0, line.length() - ctMatcher.group(0).length());
                    }
                    if (!line.isEmpty()) ((RequestBody)currentOp.requestBody).description = line;
                }
} else if (line.startsWith("      RequestBodyContentItemSchema ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(35).split(" ", 2);
                    if (parts.length > 1 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.itemSchema == null) mt.itemSchema = new HashMap<String, Object>();
                        ((Map<String, Object>) mt.itemSchema).put("type", parts[1]);
                    }
                }
            } else if (line.startsWith("      RequestBodyContentPrefixEncoding ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(39).split(" ", 2);
                    if (parts.length > 1 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.prefixEncoding == null) mt.prefixEncoding = new ArrayList<>();
                        Encoding enc = new Encoding();
                        enc.contentType = parts[1];
                        mt.prefixEncoding.add(enc);
                    }
                }
            } else if (line.startsWith("      RequestBodyContentItemEncoding ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(37).split(" ", 2);
                    if (parts.length > 1 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.itemEncoding == null) mt.itemEncoding = new Encoding();
                        mt.itemEncoding.contentType = parts[1];
                    }
                }
            } else if (line.startsWith("      RequestBodyEncoding ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(26).split(" ", 3);
                    if (parts.length > 2 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.encoding == null) mt.encoding = new HashMap<>();
                        Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new Encoding());
                        enc.contentType = parts[2];
                    }
                }
            } else if (line.startsWith("      RequestBodyEncodingPrefixEncoding ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(40).split(" ", 3);
                    if (parts.length > 2 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.encoding == null) mt.encoding = new HashMap<>();
                        Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new Encoding());
                        if (enc.prefixEncoding == null) enc.prefixEncoding = new ArrayList<>();
                        Encoding pEnc = new Encoding();
                        pEnc.contentType = parts[2];
                        enc.prefixEncoding.add(pEnc);
                    }
                }
            } else if (line.startsWith("      RequestBodyEncodingItemEncoding ")) {
                if (currentOp != null && currentOp.requestBody instanceof RequestBody) {
                    RequestBody rb = (RequestBody) currentOp.requestBody;
                    String[] parts = line.substring(38).split(" ", 3);
                    if (parts.length > 2 && rb.content != null && rb.content.containsKey(parts[0])) {
                        MediaType mt = rb.content.get(parts[0]);
                        if (mt.encoding == null) mt.encoding = new HashMap<>();
                        Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new Encoding());
                        if (enc.itemEncoding == null) enc.itemEncoding = new Encoding();
                        enc.itemEncoding.contentType = parts[2];
                    }
                }
            } else if (line.startsWith("      Example: ")) {
                if (currentParameter != null) {
                    currentParameter.example = line.substring(15);
                }
            } else if (line.startsWith("      Example ")) {
                if (currentParameter != null) {
                    String l = line.substring(14);
                    int colIdx = l.indexOf(": ");
                    if (colIdx != -1) {
                        String key = l.substring(0, colIdx);
                        String rem = l.substring(colIdx + 2);
                        int dashIdx = rem.indexOf(" - ");
                        int eqIdx = rem.indexOf(" = ", dashIdx != -1 ? dashIdx : 0);
                        if (dashIdx != -1 && eqIdx != -1) {
                            String sum = rem.substring(0, dashIdx);
                            String desc = rem.substring(dashIdx + 3, eqIdx);
                            String val = rem.substring(eqIdx + 3);
                            if (sum.equals("null")) sum = null;
                            if (desc.equals("null")) desc = null;
                            if (val.equals("null")) val = null;
                            if (currentParameter.examples == null) currentParameter.examples = new HashMap<>();
                            Example ex = new Example();
                            ex.summary = sum;
                            ex.description = desc;
                            ex.value = val;
                            currentParameter.examples.put(key, ex);
                        }
                    }
                }
            } else if (line.startsWith("    --")) {                if (currentOp != null) {
                    if (currentOp.parameters == null) currentOp.parameters = new ArrayList<>();
                    Parameter p = new Parameter();
                    String l = line.substring(6);
                    int colIdx = l.indexOf(" : ");
                    String pre = l.substring(0, colIdx);
                    String desc = l.substring(colIdx + 3);
                    if (pre.contains(" [DEPRECATED]")) {
                        p.deprecated = true;
                        pre = pre.replace(" [DEPRECATED]", "");
                    }
                    if (pre.contains(" (required)")) {
                        p.required = true;
                        pre = pre.replace(" (required)", "");
                    }
                    p.name = pre;
                    if (!desc.isEmpty()) p.description = desc;
                    currentOp.parameters.add(p);
                    currentParameter = p;
                }
            } else if (line.startsWith("    Returns ")) {
                currentParameter = null;
                if (currentOp != null) {
                    if (currentOp.responses == null) currentOp.responses = new Responses();
                    if (currentOp.responses.statusCodes == null) currentOp.responses.statusCodes = new HashMap<>();
                    
                    String l = line.substring(12);
                    int colIdx = l.indexOf(": ");
                    String code = l.substring(0, colIdx);
                    String desc = l.substring(colIdx + 2);
                    
                    currentResponse = new Response();
                    Matcher ctMatcher = Pattern.compile(" \\[Content-Types: (.*?)\\]$").matcher(desc);
                    if (ctMatcher.find()) {
                        currentResponse.content = new HashMap<>();
                        for (String ct : ctMatcher.group(1).split(", ")) {
                            currentResponse.content.put(ct, new MediaType());
                        }
                        desc = desc.substring(0, desc.length() - ctMatcher.group(0).length());
                    }
                    if (!desc.isEmpty()) currentResponse.description = desc;
                    
                    if (code.equals("default")) currentOp.responses.defaultResponse = currentResponse;
                    else currentOp.responses.statusCodes.put(code, currentResponse);
                }
} else if (line.startsWith("      ResponseContentItemSchema ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(32).split(" ", 3);
                    if (parts.length > 2) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.itemSchema == null) mt.itemSchema = new HashMap<String, Object>();
                            ((Map<String, Object>) mt.itemSchema).put("type", parts[2]);
                        }
                    }
                }
            } else if (line.startsWith("      ResponseContentPrefixEncoding ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(36).split(" ", 3);
                    if (parts.length > 2) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.prefixEncoding == null) mt.prefixEncoding = new ArrayList<>();
                            Encoding enc = new Encoding();
                            enc.contentType = parts[2];
                            mt.prefixEncoding.add(enc);
                        }
                    }
                }
            } else if (line.startsWith("      ResponseContentItemEncoding ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(34).split(" ", 3);
                    if (parts.length > 2) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.itemEncoding == null) mt.itemEncoding = new Encoding();
                            mt.itemEncoding.contentType = parts[2];
                        }
                    }
                }
            } else if (line.startsWith("      ResponseEncoding ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(23).split(" ", 4);
                    if (parts.length > 3) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.encoding == null) mt.encoding = new HashMap<>();
                            Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new Encoding());
                            enc.contentType = parts[3];
                        }
                    }
                }
            } else if (line.startsWith("      ResponseEncodingPrefixEncoding ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(37).split(" ", 4);
                    if (parts.length > 3) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.encoding == null) mt.encoding = new HashMap<>();
                            Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new Encoding());
                            if (enc.prefixEncoding == null) enc.prefixEncoding = new ArrayList<>();
                            Encoding pEnc = new Encoding();
                            pEnc.contentType = parts[3];
                            enc.prefixEncoding.add(pEnc);
                        }
                    }
                }
            } else if (line.startsWith("      ResponseEncodingItemEncoding ")) {
                if (currentOp != null && currentOp.responses != null) {
                    String[] parts = line.substring(35).split(" ", 4);
                    if (parts.length > 3) {
                        Response r = parts[0].equals("default") ? (Response) currentOp.responses.defaultResponse : (Response) currentOp.responses.statusCodes.get(parts[0]);
                        if (r != null && r.content != null && r.content.containsKey(parts[1])) {
                            MediaType mt = r.content.get(parts[1]);
                            if (mt.encoding == null) mt.encoding = new HashMap<>();
                            Encoding enc = mt.encoding.computeIfAbsent(parts[2], k -> new Encoding());
                            if (enc.itemEncoding == null) enc.itemEncoding = new Encoding();
                            enc.itemEncoding.contentType = parts[3];
                        }
                    }
                }
            } else if (line.startsWith("      Header ")) {
                currentParameter = null;
                if (currentResponse != null) {
                    if (currentResponse.headers == null) currentResponse.headers = new HashMap<>();
                    Header h = new Header();
                    String l = line.substring(13);
                    int colIdx = l.indexOf(": ");
                    String pre = l.substring(0, colIdx);
                    String desc = l.substring(colIdx + 2);
                    
                    if (pre.contains(" [DEPRECATED]")) {
                        h.deprecated = true;
                        pre = pre.replace(" [DEPRECATED]", "");
                    }
                    if (pre.contains(" (required)")) {
                        h.required = true;
                        pre = pre.replace(" (required)", "");
                    }
                    if (!desc.isEmpty()) h.description = desc;
                    currentResponse.headers.put(pre, h);
                }
            } else if (line.startsWith("        LinkParam ")) {
                if (currentResponse != null && currentResponse.links != null) {
                    String[] parts = line.substring(18).split(" ", 3);
                    if (parts.length >= 3) {
                        Link lnk = (Link) currentResponse.links.get(parts[0]);
                        if (lnk != null) {
                            if (lnk.parameters == null) lnk.parameters = new HashMap<>();
                            lnk.parameters.put(parts[1], parts[2]);
                        }
                    }
                }
            } else if (line.startsWith("      Link ")) {
                if (currentResponse != null) {
                    if (currentResponse.links == null) currentResponse.links = new HashMap<>();
                    String rem = line.substring(11);
                    String[] parts = rem.split(" ", 2);
                    String key = parts[0];
                    Link lnk = new Link();
                    if (parts.length > 1) {
                        Matcher mId = Pattern.compile(" operationId=([^ ]+)").matcher(" " + parts[1]);
                        if (mId.find()) lnk.operationId = mId.group(1);
                        Matcher mRef = Pattern.compile(" operationRef=([^ ]+)").matcher(" " + parts[1]);
                        if (mRef.find()) lnk.operationRef = mRef.group(1);
                        Matcher mDesc = Pattern.compile(" description=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                        if (mDesc.find()) lnk.description = mDesc.group(1);
                        Matcher mReq = Pattern.compile(" requestBody=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                        if (mReq.find()) lnk.requestBody = mReq.group(1);
                        Matcher mSrv = Pattern.compile(" serverUrl=\\\"([^\\\"]*)\\\"").matcher(" " + parts[1]);
                        if (mSrv.find()) {
                            lnk.server = new Server();
                            lnk.server.url = mSrv.group(1);
                        }
                    }
                    currentResponse.links.put(key, lnk);
                }
            }
        }
        
        return api;
    }
}
