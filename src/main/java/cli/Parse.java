package cli;

import openapi.*;
import java.util.*;
import java.util.regex.*;

/**
 * Parses a CLI application to an OpenAPI model.
 */
public class Parse {

	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Unescapes newline, carriage return, quote, and backslash escape sequences.
	 *
	 * @param s
	 *            The escaped string.
	 * @return The unescaped string.
	 */
	private static String unescape(String s) {
		return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
	}

	/**
	 * Mutable cursor tracking state across sequential CLI help output lines.
	 */
	private static final class ParseState {
		/** Currently active operation being parsed. */
		Operation currentOp;
		/** Currently active parameter being parsed. */
		Parameter currentParameter;
		/** Currently active route path template. */
		String currentPath;
		/** Currently active response being parsed. */
		Response currentResponse;
		/** Last component schema parsed. */
		openapi.Schema lastParsedSchema;
	}

	/**
	 * Parses the CLI source code into an OpenAPI object.
	 *
	 * @param existingSource
	 *            The source code string.
	 * @return The parsed OpenAPI object.
	 */
	public static OpenAPI parse(String existingSource) {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.responses = new HashMap<>();
		api.components.parameters = new HashMap<>();
		api.components.requestBodies = new HashMap<>();
		api.components.headers = new HashMap<>();
		api.components.links = new HashMap<>();
		api.components.callbacks = new HashMap<>();
		api.components.pathItems = new HashMap<>();
		api.components.securitySchemes = new HashMap<>();
		api.components.mediaTypes = new HashMap<>();
		String helpBody = "";
		Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{(.*?)\\n    \\}")
				.matcher(existingSource);
		if (hm.find()) {
			helpBody = hm.group(1);
		}
		Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(helpBody);
		ParseState state = new ParseState();
		while (lm.find()) {
			String line = unescape(lm.group(1));
			if (parseInfoAndServers(line, api)) {
				continue;
			}
			if (parseComponentLine(line, api, state)) {
				continue;
			}
			parseOperationLine(line, api, state);
		}
		return api;
	}

	/**
	 * Parses Info, Contact, License, and Server lines from {@code printHelp()}.
	 *
	 * @param line
	 *            The unescaped output line.
	 * @param api
	 *            The target {@link OpenAPI} specification.
	 * @return {@code true} if the line was handled.
	 */
	private static boolean parseInfoAndServers(String line, OpenAPI api) {
		if (line.startsWith("Info Object title: ")) {
			if (api.info == null)
				api.info = new Info();
			api.info.title = line.substring(19);
			return true;
		} else if (line.startsWith("Info Object version: ")) {
			if (api.info == null)
				api.info = new Info();
			api.info.version = line.substring(21);
			return true;
		} else if (line.startsWith("Info Object summary: ")) {
			if (api.info == null)
				api.info = new Info();
			api.info.summary = line.substring(21);
			return true;
		} else if (line.startsWith("Info Object description: ")) {
			if (api.info == null)
				api.info = new Info();
			api.info.description = line.substring(25);
			return true;
		} else if (line.startsWith("Info Object termsOfService: ")) {
			if (api.info == null)
				api.info = new Info();
			api.info.termsOfService = line.substring(28);
			return true;
		} else if (line.startsWith("Contact Object name: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.contact == null)
				api.info.contact = new Contact();
			api.info.contact.name = line.substring(21);
			return true;
		} else if (line.startsWith("Contact Object email: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.contact == null)
				api.info.contact = new Contact();
			api.info.contact.email = line.substring(22);
			return true;
		} else if (line.startsWith("Contact Object url: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.contact == null)
				api.info.contact = new Contact();
			api.info.contact.url = line.substring(20);
			return true;
		} else if (line.startsWith("License Object name: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.license == null)
				api.info.license = new License();
			api.info.license.name = line.substring(21);
			return true;
		} else if (line.startsWith("License Object identifier: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.license == null)
				api.info.license = new License();
			api.info.license.identifier = line.substring(27);
			return true;
		} else if (line.startsWith("License Object url: ")) {
			if (api.info == null)
				api.info = new Info();
			if (api.info.license == null)
				api.info.license = new License();
			api.info.license.url = line.substring(20);
			return true;
		} else if (line.startsWith("Server Object url: ")) {
			if (api.servers == null)
				api.servers = new ArrayList<>();
			Server s = new Server();
			String[] parts = line.substring(19).split(" name: | description: ");
			s.url = parts[0];
			if (line.contains(" name: ")) {
				int nIdx = line.indexOf(" name: ") + 7;
				int dIdx = line.indexOf(" description: ");
				if (dIdx > nIdx)
					s.name = line.substring(nIdx, dIdx);
				else
					s.name = line.substring(nIdx);
				if (s.name.isEmpty())
					s.name = null;
			}
			if (line.contains(" description: ")) {
				s.description = line.substring(line.indexOf(" description: ") + 14);
				if (s.description.isEmpty())
					s.description = null;
			}
			s.variables = new HashMap<>();
			api.servers.add(s);
			return true;
		} else if (line.startsWith("Server Variable Object ")) {
			if (api.servers != null) {
				Server s = api.servers.get(api.servers.size() - 1);
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
				if (!desc.isEmpty())
					sv.description = desc;
				if (!enums.isEmpty())
					sv.enumValues = Arrays.asList(enums.split(", "));
				s.variables.put(name, sv);
			}
			return true;
		}
		return false;
	}

	/**
	 * Parses Components, Discriminator, XML, SecurityScheme, and Link lines.
	 *
	 * @param line
	 *            The unescaped output line.
	 * @param api
	 *            The target {@link OpenAPI} specification.
	 * @param state
	 *            The mutable {@link ParseState}.
	 * @return {@code true} if the line was handled.
	 */
	private static boolean parseComponentLine(String line, OpenAPI api, ParseState state) {
		if (line.startsWith("Component schemas ")) {
			String key = line.substring("Component schemas ".length());
			state.lastParsedSchema = new openapi.Schema();
			api.components.schemas.put(key, state.lastParsedSchema);
			return true;
		} else if (line.startsWith("  Discriminator propertyName=")) {
			if (state.lastParsedSchema != null) {
				Matcher m = Pattern.compile("  Discriminator propertyName=(.*?) mapping=(.*?) defaultMapping=(.*)")
						.matcher(line);
				if (m.matches()) {
					Discriminator d = new Discriminator();
					String pName = m.group(1);
					if (!pName.isEmpty() && !pName.equals("null"))
						d.propertyName = pName;
					String mappingStr = m.group(2);
					if (!mappingStr.isEmpty() && !mappingStr.equals("null")) {
						d.mapping = new HashMap<>();
						for (String pair : mappingStr.split(",")) {
							String[] kv = pair.split("=");
							if (kv.length == 2)
								d.mapping.put(kv[0], kv[1]);
						}
					}
					String dMapping = m.group(3);
					if (!dMapping.isEmpty() && !dMapping.equals("null"))
						d.defaultMapping = dMapping;
					state.lastParsedSchema.discriminator = d;
				}
			}
			return true;
		} else if (line.startsWith("  XML name=")) {
			if (state.lastParsedSchema != null) {
				Matcher m = Pattern
						.compile("  XML name=(.*?) namespace=(.*?) prefix=(.*?) attribute=(.*?) wrapped=(.*)")
						.matcher(line);
				if (m.matches()) {
					XML x = new XML();
					String name = m.group(1);
					if (!name.isEmpty() && !name.equals("null"))
						x.name = name;
					String namespace = m.group(2);
					if (!namespace.isEmpty() && !namespace.equals("null"))
						x.namespace = namespace;
					String prefix = m.group(3);
					if (!prefix.isEmpty() && !prefix.equals("null"))
						x.prefix = prefix;
					String attr = m.group(4);
					if (attr.equals("true"))
						x.attribute = true;
					else if (attr.equals("false"))
						x.attribute = false;
					String wrapped = m.group(5);
					if (wrapped.equals("true"))
						x.wrapped = true;
					else if (wrapped.equals("false"))
						x.wrapped = false;
					state.lastParsedSchema.xml = x;
				}
			}
			return true;
		} else if (line.startsWith("Component securitySchemes ")) {
			java.util.regex.Matcher m = java.util.regex.Pattern.compile(
					"Component securitySchemes (\\S+) type=([^ ]+) scheme=([^ ]+) in=([^ ]+) name=([^ ]+) bearerFormat=([^ ]+) openIdConnectUrl=([^ ]+) oauth2MetadataUrl=([^ ]+) deprecated=([^ ]+)(?: description=\\\"(.+?)\\\"?)?")
					.matcher(line);
			if (m.matches()) {
				SecurityScheme sc = new SecurityScheme();
				String key = m.group(1);
				String type = m.group(2);
				if (!type.equals("-"))
					sc.type = unescape(type);
				String scheme = m.group(3);
				if (!scheme.equals("-"))
					sc.scheme = unescape(scheme);
				String in = m.group(4);
				if (!in.equals("-"))
					sc.in = unescape(in);
				String name = m.group(5);
				if (!name.equals("-"))
					sc.name = unescape(name);
				String bearerFormat = m.group(6);
				if (!bearerFormat.equals("-"))
					sc.bearerFormat = unescape(bearerFormat);
				String openIdConnectUrl = m.group(7);
				if (!openIdConnectUrl.equals("-"))
					sc.openIdConnectUrl = unescape(openIdConnectUrl);
				String oauth2MetadataUrl = m.group(8);
				if (!oauth2MetadataUrl.equals("-"))
					sc.oauth2MetadataUrl = unescape(oauth2MetadataUrl);
				String deprecated = m.group(9);
				if (deprecated.equals("true"))
					sc.deprecated = true;
				String desc = m.group(10);
				if (desc != null)
					sc.description = unescape(desc);
				api.components.securitySchemes.put(key, sc);
			}
			return true;
		} else if (line.startsWith("Component securitySchemesFlowScope ")) {
			Matcher m = Pattern.compile("^Component securitySchemesFlowScope (\\S+) (\\S+) (\\S+) (.*)$").matcher(line);
			if (m.find()) {
				String k = unescape(m.group(1));
				String flowType = unescape(m.group(2));
				String scopeKey = unescape(m.group(3));
				String scopeDesc = unescape(m.group(4));
				SecurityScheme sc = (SecurityScheme) api.components.securitySchemes.get(k);
				if (sc != null && sc.flows != null) {
					OAuthFlow flow = getOAuthFlow(sc.flows, flowType);
					if (flow != null) {
						if (flow.scopes == null)
							flow.scopes = new java.util.HashMap<>();
						flow.scopes.put(scopeKey, scopeDesc);
					}
				}
			}
			return true;
		} else if (line.startsWith("Component securitySchemesFlow ")) {
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
					if (sc.flows == null)
						sc.flows = new OAuthFlows();
					OAuthFlow flow = new OAuthFlow();
					flow.authorizationUrl = authUrl;
					flow.tokenUrl = tokenUrl;
					flow.refreshUrl = refreshUrl;
					flow.deviceAuthorizationUrl = devUrl;
					if (flowType.equals("implicit"))
						sc.flows.implicit = flow;
					else if (flowType.equals("password"))
						sc.flows.password = flow;
					else if (flowType.equals("clientCredentials"))
						sc.flows.clientCredentials = flow;
					else if (flowType.equals("authorizationCode"))
						sc.flows.authorizationCode = flow;
					else if (flowType.equals("deviceAuthorization"))
						sc.flows.deviceAuthorization = flow;
				}
			}
			return true;
		} else if (line.startsWith("Component linksParam ")) {
			String[] parts = line.substring(21).split(" ", 3);
			if (parts.length >= 3) {
				Link lnk = (Link) api.components.links.get(parts[0]);
				if (lnk != null) {
					if (lnk.parameters == null)
						lnk.parameters = new HashMap<>();
					lnk.parameters.put(parts[1], parts[2]);
				}
			}
			return true;
		} else if (line.startsWith("Component links ")) {
			String rem = line.substring(16);
			String[] parts = rem.split(" ", 2);
			api.components.links.put(parts[0], parseLinkAttributes(parts.length > 1 ? parts[1] : ""));
			return true;
		} else if (line.startsWith("Component ")) {
			String[] parts = line.split(" ");
			if (parts.length >= 3) {
				String type = parts[1];
				String key = parts[2];
				if (type.equals("responses")) {
					api.components.responses.put(key, new Response());
				} else if (type.equals("parameters")) {
					api.components.parameters.put(key, new Parameter());
				} else if (type.equals("requestBodies")) {
					api.components.requestBodies.put(key, new RequestBody());
				} else if (type.equals("headers")) {
					api.components.headers.put(key, new Header());
				} else if (type.equals("callbacks")) {
					api.components.callbacks.put(key, new Callback());
				} else if (type.equals("pathItems")) {
					api.components.pathItems.put(key, new PathItem());
				} else if (type.equals("mediaTypes")) {
					api.components.mediaTypes.put(key, new MediaType());
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Looks up an {@link OAuthFlow} by flow type name on an {@link OAuthFlows}
	 * container.
	 *
	 * @param flows
	 *            The {@link OAuthFlows} container.
	 * @param flowType
	 *            The flow type name.
	 * @return The matching {@link OAuthFlow} or {@code null}.
	 */
	private static OAuthFlow getOAuthFlow(OAuthFlows flows, String flowType) {
		switch (flowType) {
			case "implicit" :
				return flows.implicit;
			case "password" :
				return flows.password;
			case "clientCredentials" :
				return flows.clientCredentials;
			case "authorizationCode" :
				return flows.authorizationCode;
			case "deviceAuthorization" :
				return flows.deviceAuthorization;
			default :
				return null;
		}
	}

	/**
	 * Parses inline key-value attributes of a {@link Link} line.
	 *
	 * @param attrString
	 *            The attribute suffix string (may be empty).
	 * @return The populated {@link Link} instance.
	 */
	private static Link parseLinkAttributes(String attrString) {
		Link lnk = new Link();
		if (!attrString.isEmpty()) {
			String padded = " " + attrString;
			Matcher mId = Pattern.compile(" operationId=([^ ]+)").matcher(padded);
			if (mId.find())
				lnk.operationId = mId.group(1);
			Matcher mRef = Pattern.compile(" operationRef=([^ ]+)").matcher(padded);
			if (mRef.find())
				lnk.operationRef = mRef.group(1);
			Matcher mDesc = Pattern.compile(" description=\\\"([^\\\"]*)\\\"").matcher(padded);
			if (mDesc.find())
				lnk.description = mDesc.group(1);
			Matcher mReq = Pattern.compile(" requestBody=\\\"([^\\\"]*)\\\"").matcher(padded);
			if (mReq.find())
				lnk.requestBody = mReq.group(1);
			Matcher mSrv = Pattern.compile(" serverUrl=\\\"([^\\\"]*)\\\"").matcher(padded);
			if (mSrv.find()) {
				lnk.server = new Server();
				lnk.server.url = mSrv.group(1);
			}
		}
		return lnk;
	}

	/**
	 * Parses Operation, Parameter, RequestBody, Response, Header, Link, and
	 * Encoding lines from {@code printHelp()}.
	 *
	 * @param line
	 *            The unescaped output line.
	 * @param api
	 *            The target {@link OpenAPI} specification.
	 * @param state
	 *            The mutable {@link ParseState}.
	 */
	@SuppressWarnings("unchecked")
	private static void parseOperationLine(String line, OpenAPI api, ParseState state) {
		if (line.startsWith("Operation: ")) {
			state.currentParameter = null;
			String[] parts = line.split(" ");
			String method = parts[1];
			state.currentPath = parts[2];
			if (api.paths == null)
				api.paths = new Paths();
			PathItem pi = api.paths.pathItems.computeIfAbsent(state.currentPath, k -> new PathItem());
			state.currentOp = new Operation();
			if (method.equals("get"))
				pi.get = state.currentOp;
			else if (method.equals("post"))
				pi.post = state.currentOp;
			else if (method.equals("put"))
				pi.put = state.currentOp;
			else if (method.equals("delete"))
				pi.delete = state.currentOp;
			else if (method.equals("patch"))
				pi.patch = state.currentOp;
			else if (method.equals("options"))
				pi.options = state.currentOp;
			else if (method.equals("head"))
				pi.head = state.currentOp;
			else if (method.equals("trace"))
				pi.trace = state.currentOp;
			else if (method.equals("query"))
				pi.query = state.currentOp;
		} else if (line.startsWith("Operation Object ")) {
			if (state.currentOp != null) {
				String l = line.substring(17);
				Matcher depMatcher = Pattern.compile("^\\[DEPRECATED\\] ").matcher(l);
				if (depMatcher.find()) {
					state.currentOp.deprecated = true;
					l = l.substring(13);
				}
				Matcher tagsMatcher = Pattern.compile(" \\(Tags: (.*?)\\)$").matcher(l);
				if (tagsMatcher.find()) {
					state.currentOp.tags = Arrays.asList(tagsMatcher.group(1).split(", "));
					l = l.substring(0, l.length() - tagsMatcher.group(0).length());
				}
				if (!l.isEmpty())
					state.currentOp.summary = l;
			}
		} else if (line.startsWith("    OperationId: ")) {
			if (state.currentOp != null)
				state.currentOp.operationId = line.substring(17);
		} else if (line.startsWith("    Description: ")) {
			if (state.currentOp != null)
				state.currentOp.description = line.substring(17);
		} else if (line.startsWith("See also: ")) {
			if (state.currentOp != null) {
				String[] parts = line.substring(10).split(" ", 2);
				state.currentOp.externalDocs = new ExternalDocumentation();
				state.currentOp.externalDocs.url = parts[0];
				if (parts.length > 1 && !parts[1].isEmpty())
					state.currentOp.externalDocs.description = parts[1];
			}
		} else if (line.startsWith("Callback: ")) {
			if (state.currentOp != null) {
				if (state.currentOp.callbacks == null)
					state.currentOp.callbacks = new HashMap<>();
				state.currentOp.callbacks.put(line.substring(10), new Callback());
			}
		} else if (line.startsWith("    --requestBody")) {
			state.currentParameter = null;
			if (state.currentOp != null) {
				state.currentOp.requestBody = new RequestBody();
				if (line.contains(" (required)")) {
					((RequestBody) state.currentOp.requestBody).required = true;
					line = line.replace(" (required)", "");
				}
				line = line.substring(17);
				if (line.startsWith(": "))
					line = line.substring(2);
				Matcher ctMatcher = Pattern.compile(" \\[Content-Types: (.*?)\\]$").matcher(line);
				if (ctMatcher.find()) {
					((RequestBody) state.currentOp.requestBody).content = new HashMap<>();
					for (String ct : ctMatcher.group(1).split(", ")) {
						((RequestBody) state.currentOp.requestBody).content.put(ct, new MediaType());
					}
					line = line.substring(0, line.length() - ctMatcher.group(0).length());
				}
				if (!line.isEmpty())
					((RequestBody) state.currentOp.requestBody).description = line;
			}
		} else if (line.startsWith("      RequestBody")) {
			if (state.currentOp != null && state.currentOp.requestBody instanceof RequestBody) {
				RequestBody rb = (RequestBody) state.currentOp.requestBody;
				String suffix = line.substring(17);
				int sp = suffix.indexOf(' ');
				if (sp != -1) {
					applyMediaTypeEncoding(rb.content, suffix.substring(0, sp), suffix.substring(sp + 1));
				}
			}
		} else if (line.startsWith("      Example: ")) {
			if (state.currentParameter != null) {
				state.currentParameter.example = line.substring(15);
			}
		} else if (line.startsWith("      Example ")) {
			if (state.currentParameter != null) {
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
						if (sum.equals("null"))
							sum = null;
						if (desc.equals("null"))
							desc = null;
						if (val.equals("null"))
							val = null;
						if (state.currentParameter.examples == null)
							state.currentParameter.examples = new HashMap<>();
						Example ex = new Example();
						ex.summary = sum;
						ex.description = desc;
						ex.value = val;
						state.currentParameter.examples.put(key, ex);
					}
				}
			}
		} else if (line.startsWith("    --")) {
			if (state.currentOp != null) {
				if (state.currentOp.parameters == null)
					state.currentOp.parameters = new ArrayList<>();
				Parameter p = new Parameter();
				String l = line.substring(6);
				int colIdx = l.indexOf(" : ");
				String pre = colIdx != -1 ? l.substring(0, colIdx) : l;
				String desc = colIdx != -1 ? l.substring(colIdx + 3) : "";
				if (pre.contains(" [DEPRECATED]")) {
					p.deprecated = true;
					pre = pre.replace(" [DEPRECATED]", "");
				}
				if (pre.contains(" (required)")) {
					p.required = true;
					pre = pre.replace(" (required)", "");
				}
				p.name = pre;
				if (!desc.isEmpty())
					p.description = desc;
				state.currentOp.parameters.add(p);
				state.currentParameter = p;
			}
		} else if (line.startsWith("    Returns ")) {
			state.currentParameter = null;
			if (state.currentOp != null) {
				if (state.currentOp.responses == null)
					state.currentOp.responses = new Responses();
				String l = line.substring(12);
				int colIdx = l.indexOf(": ");
				String code = colIdx != -1 ? l.substring(0, colIdx) : l;
				String desc = colIdx != -1 ? l.substring(colIdx + 2) : "";
				state.currentResponse = new Response();
				Matcher ctMatcher = Pattern.compile(" \\[Content-Types: (.*?)\\]$").matcher(desc);
				if (ctMatcher.find()) {
					state.currentResponse.content = new HashMap<>();
					for (String ct : ctMatcher.group(1).split(", ")) {
						state.currentResponse.content.put(ct, new MediaType());
					}
					desc = desc.substring(0, desc.length() - ctMatcher.group(0).length());
				}
				if (!desc.isEmpty())
					state.currentResponse.description = desc;
				if (code.equals("default"))
					state.currentOp.responses.defaultResponse = state.currentResponse;
				else
					state.currentOp.responses.statusCodes.put(code, state.currentResponse);
			}
		} else if (line.startsWith("      Response")) {
			if (state.currentOp != null && state.currentOp.responses != null) {
				String suffix = line.substring(14);
				int sp1 = suffix.indexOf(' ');
				if (sp1 != -1) {
					String kind = suffix.substring(0, sp1);
					String restWithCode = suffix.substring(sp1 + 1);
					int sp2 = restWithCode.indexOf(' ');
					if (sp2 != -1) {
						String code = restWithCode.substring(0, sp2);
						String rest = restWithCode.substring(sp2 + 1);
						Response r = code.equals("default")
								? (Response) state.currentOp.responses.defaultResponse
								: (Response) state.currentOp.responses.statusCodes.get(code);
						if (r != null) {
							applyMediaTypeEncoding(r.content, kind, rest);
						}
					}
				}
			}
		} else if (line.startsWith("      Header ")) {
			state.currentParameter = null;
			if (state.currentResponse != null) {
				if (state.currentResponse.headers == null)
					state.currentResponse.headers = new HashMap<>();
				Header h = new Header();
				String l = line.substring(13);
				int colIdx = l.indexOf(": ");
				String pre = colIdx != -1 ? l.substring(0, colIdx) : l;
				String desc = colIdx != -1 ? l.substring(colIdx + 2) : "";
				if (pre.contains(" [DEPRECATED]")) {
					h.deprecated = true;
					pre = pre.replace(" [DEPRECATED]", "");
				}
				if (pre.contains(" (required)")) {
					h.required = true;
					pre = pre.replace(" (required)", "");
				}
				if (!desc.isEmpty())
					h.description = desc;
				state.currentResponse.headers.put(pre, h);
			}
		} else if (line.startsWith("        LinkParam ")) {
			if (state.currentResponse != null && state.currentResponse.links != null) {
				String[] parts = line.substring(18).split(" ", 3);
				if (parts.length >= 3) {
					Link lnk = (Link) state.currentResponse.links.get(parts[0]);
					if (lnk != null) {
						if (lnk.parameters == null)
							lnk.parameters = new HashMap<>();
						lnk.parameters.put(parts[1], parts[2]);
					}
				}
			}
		} else if (line.startsWith("      Link ")) {
			if (state.currentResponse != null) {
				if (state.currentResponse.links == null)
					state.currentResponse.links = new HashMap<>();
				String rem = line.substring(11);
				String[] parts = rem.split(" ", 2);
				state.currentResponse.links.put(parts[0], parseLinkAttributes(parts.length > 1 ? parts[1] : ""));
			}
		}
	}

	/**
	 * Applies a parsed media type schema or encoding attribute to a content map.
	 *
	 * @param contentMap
	 *            The target {@link MediaType} map (from a {@link RequestBody} or
	 *            {@link Response}).
	 * @param kind
	 *            The directive suffix ({@code ContentItemSchema},
	 *            {@code ContentPrefixEncoding}, {@code ContentItemEncoding},
	 *            {@code Encoding}, {@code EncodingPrefixEncoding}, or
	 *            {@code EncodingItemEncoding}).
	 * @param rest
	 *            The space-delimited arguments following the directive.
	 */
	@SuppressWarnings("unchecked")
	private static void applyMediaTypeEncoding(Map<String, MediaType> contentMap, String kind, String rest) {
		if (contentMap == null) {
			return;
		}
		if (kind.startsWith("Content")) {
			String[] parts = rest.split(" ", 2);
			if (parts.length > 1 && contentMap.containsKey(parts[0])) {
				MediaType mt = contentMap.get(parts[0]);
				if (kind.equals("ContentItemSchema")) {
					if (mt.itemSchema == null)
						mt.itemSchema = new HashMap<String, Object>();
					((Map<String, Object>) mt.itemSchema).put("type", parts[1]);
				} else if (kind.equals("ContentPrefixEncoding")) {
					if (mt.prefixEncoding == null)
						mt.prefixEncoding = new ArrayList<>();
					Encoding enc = new Encoding();
					enc.contentType = parts[1];
					mt.prefixEncoding.add(enc);
				} else if (kind.equals("ContentItemEncoding")) {
					if (mt.itemEncoding == null)
						mt.itemEncoding = new Encoding();
					mt.itemEncoding.contentType = parts[1];
				}
			}
		} else if (kind.startsWith("Encoding")) {
			String[] parts = rest.split(" ", 3);
			if (parts.length > 2 && contentMap.containsKey(parts[0])) {
				MediaType mt = contentMap.get(parts[0]);
				if (mt.encoding == null)
					mt.encoding = new HashMap<>();
				Encoding enc = mt.encoding.computeIfAbsent(parts[1], k -> new Encoding());
				if (kind.equals("Encoding")) {
					enc.contentType = parts[2];
				} else if (kind.equals("EncodingPrefixEncoding")) {
					if (enc.prefixEncoding == null)
						enc.prefixEncoding = new ArrayList<>();
					Encoding pEnc = new Encoding();
					pEnc.contentType = parts[2];
					enc.prefixEncoding.add(pEnc);
				} else if (kind.equals("EncodingItemEncoding")) {
					if (enc.itemEncoding == null)
						enc.itemEncoding = new Encoding();
					enc.itemEncoding.contentType = parts[2];
				}
			}
		}
	}
}
