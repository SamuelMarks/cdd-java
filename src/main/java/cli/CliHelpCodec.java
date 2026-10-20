package cli;

import java.util.*;
import java.util.regex.*;
import openapi.*;

/**
 * Shared codec for formatting and parsing OpenAPI CLI help metadata between
 * {@link Emit} and {@link Parse}.
 */
public class CliHelpCodec {

	/**
	 * Default constructor.
	 */
	public CliHelpCodec() {
	}

	/**
	 * Escapes backslashes, double quotes, and newlines for Java string literals.
	 *
	 * @param s
	 *            The input string to escape.
	 * @return The escaped string (or empty string if {@code s} is {@code null}).
	 */
	public static String escape(String s) {
		return openapi.TypeMapper.escape(s);
	}

	/**
	 * Unescapes newline, carriage return, quote, and backslash escape sequences.
	 *
	 * @param s
	 *            The escaped string.
	 * @return The unescaped string.
	 */
	public static String unescape(String s) {
		return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
	}

	/**
	 * Mutable cursor tracking state across sequential CLI help output lines.
	 */
	public static final class ParseState {
		/** Default constructor. */
		public ParseState() {
		}

		/** Currently active operation being parsed. */
		public Operation currentOp;
		/** Currently active parameter being parsed. */
		public Parameter currentParameter;
		/** Currently active route path template. */
		public String currentPath;
		/** Currently active response being parsed. */
		public Response currentResponse;
		/** Last component schema parsed. */
		public openapi.Schema lastParsedSchema;
	}

	/**
	 * Emits Info, Contact, License, and Server metadata lines inside
	 * {@code printHelp()}.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param api
	 *            The OpenAPI specification.
	 */
	public static void emitInfoAndServers(StringBuilder sb, OpenAPI api) {
		if (api.info != null) {
			if (api.info.title != null)
				sb.append("        System.out.println(\"Info Object title: ").append(escape(api.info.title))
						.append("\");\n");
			if (api.info.version != null)
				sb.append("        System.out.println(\"Info Object version: ").append(escape(api.info.version))
						.append("\");\n");
			if (api.info.summary != null)
				sb.append("        System.out.println(\"Info Object summary: ").append(escape(api.info.summary))
						.append("\");\n");
			if (api.info.description != null)
				sb.append("        System.out.println(\"Info Object description: ").append(escape(api.info.description))
						.append("\");\n");
			if (api.info.termsOfService != null)
				sb.append("        System.out.println(\"Info Object termsOfService: ")
						.append(escape(api.info.termsOfService)).append("\");\n");
			if (api.info.contact != null) {
				if (api.info.contact.name != null)
					sb.append("        System.out.println(\"Contact Object name: ")
							.append(escape(api.info.contact.name)).append("\");\n");
				if (api.info.contact.email != null)
					sb.append("        System.out.println(\"Contact Object email: ")
							.append(escape(api.info.contact.email)).append("\");\n");
				if (api.info.contact.url != null)
					sb.append("        System.out.println(\"Contact Object url: ").append(escape(api.info.contact.url))
							.append("\");\n");
			}
			if (api.info.license != null) {
				if (api.info.license.name != null)
					sb.append("        System.out.println(\"License Object name: ")
							.append(escape(api.info.license.name)).append("\");\n");
				if (api.info.license.identifier != null)
					sb.append("        System.out.println(\"License Object identifier: ")
							.append(escape(api.info.license.identifier)).append("\");\n");
				if (api.info.license.url != null)
					sb.append("        System.out.println(\"License Object url: ").append(escape(api.info.license.url))
							.append("\");\n");
			}
		}
		if (api.servers != null) {
			for (Server s : api.servers) {
				sb.append("        System.out.println(\"Server Object url: ").append(escape(s.url)).append(" name: ")
						.append(escape(s.name == null ? "" : s.name)).append(" description: ")
						.append(escape(s.description == null ? "" : s.description)).append("\");\n");
				if (s.variables != null) {
					for (Map.Entry<String, ServerVariable> ve : s.variables.entrySet()) {
						ServerVariable v = ve.getValue();
						String enums = v.enumValues != null ? String.join(",", v.enumValues) : "";
						sb.append("        System.out.println(\"Server Variable Object ").append(escape(ve.getKey()))
								.append(" defaultValue: ").append(escape(v.defaultValue)).append(" description: ")
								.append(escape(v.description == null ? "" : v.description)).append(" enumValues: ")
								.append(escape(enums)).append("\");\n");
					}
				}
			}
		}
	}

	/**
	 * Emits OpenAPI Components metadata (schemas, responses, parameters,
	 * requestBodies, headers, securitySchemes, links, callbacks, pathItems,
	 * mediaTypes) inside {@code printHelp()}.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param api
	 *            The OpenAPI specification.
	 */
	public static void emitComponents(StringBuilder sb, OpenAPI api) {
		if (api.components != null) {
			if (api.components.schemas != null) {
				for (java.util.Map.Entry<String, ReferenceOr<openapi.Schema>> entry : api.components.schemas
						.entrySet()) {
					String k = entry.getKey();
					sb.append("        System.out.println(\"Component schemas ").append(escape(k)).append("\");\n");
					openapi.Schema schema = entry.getValue() != null ? entry.getValue().value : null;
					if (schema != null) {
						if (schema.discriminator != null) {
							String mappingStr = "";
							if (schema.discriminator.mapping != null) {
								java.util.List<String> mappings = new java.util.ArrayList<>();
								for (java.util.Map.Entry<String, String> me : schema.discriminator.mapping.entrySet()) {
									mappings.add(me.getKey() + "=" + me.getValue());
								}
								mappingStr = String.join(",", mappings);
							}
							sb.append("        System.out.println(\"  Discriminator propertyName=")
									.append(escape(schema.discriminator.propertyName)).append(" mapping=")
									.append(escape(mappingStr)).append(" defaultMapping=")
									.append(escape(schema.discriminator.defaultMapping)).append("\");\n");
						}
						if (schema.xml != null) {
							sb.append("        System.out.println(\"  XML name=").append(escape(schema.xml.name))
									.append(" namespace=").append(escape(schema.xml.namespace)).append(" prefix=")
									.append(escape(schema.xml.prefix)).append(" attribute=")
									.append(schema.xml.attribute != null && schema.xml.attribute ? "true" : "false")
									.append(" wrapped=")
									.append(schema.xml.wrapped != null && schema.xml.wrapped ? "true" : "false")
									.append("\");\n");
						}
					}
				}
			}
			if (api.components.responses != null)
				for (String k : api.components.responses.keySet())
					sb.append("        System.out.println(\"Component responses ").append(escape(k)).append("\");\n");
			if (api.components.parameters != null)
				for (String k : api.components.parameters.keySet())
					sb.append("        System.out.println(\"Component parameters ").append(escape(k)).append("\");\n");
			if (api.components.requestBodies != null)
				for (String k : api.components.requestBodies.keySet())
					sb.append("        System.out.println(\"Component requestBodies ").append(escape(k))
							.append("\");\n");
			if (api.components.headers != null)
				for (String k : api.components.headers.keySet())
					sb.append("        System.out.println(\"Component headers ").append(escape(k)).append("\");\n");
			if (api.components.securitySchemes != null) {
				for (java.util.Map.Entry<String, ReferenceOr<openapi.SecurityScheme>> entry : api.components.securitySchemes
						.entrySet()) {
					String k = entry.getKey();
					if (entry.getValue() != null) {
						openapi.SecurityScheme sc = entry.getValue().value;
						sb.append("        System.out.println(\"Component securitySchemes ").append(escape(k))
								.append(" type=").append(sc.type != null ? escape(sc.type) : "-").append(" scheme=")
								.append(sc.scheme != null ? escape(sc.scheme) : "-").append(" in=")
								.append(sc.in != null ? escape(sc.in) : "-").append(" name=")
								.append(sc.name != null ? escape(sc.name) : "-").append(" bearerFormat=")
								.append(sc.bearerFormat != null ? escape(sc.bearerFormat) : "-")
								.append(" openIdConnectUrl=")
								.append(sc.openIdConnectUrl != null ? escape(sc.openIdConnectUrl) : "-")
								.append(" oauth2MetadataUrl=")
								.append(sc.oauth2MetadataUrl != null ? escape(sc.oauth2MetadataUrl) : "-")
								.append(" deprecated=")
								.append(sc.deprecated != null && sc.deprecated ? "true" : "false");
						if (sc.description != null) {
							sb.append(" description=\\\"").append(escape(sc.description)).append("\\\"");
						}
						sb.append("\");\n");
						if (sc.flows != null) {
							if (sc.flows.implicit != null) {
								sb.append("        System.out.println(\"Component securitySchemesFlow ")
										.append(escape(k)).append(" implicit ")
										.append(sc.flows.implicit.authorizationUrl != null
												? escape(sc.flows.implicit.authorizationUrl)
												: "-")
										.append(" ").append(escape(sc.flows.implicit.tokenUrl)).append(" ")
										.append(sc.flows.implicit.refreshUrl != null
												? escape(sc.flows.implicit.refreshUrl)
												: "-")
										.append(" ").append(escape(sc.flows.implicit.deviceAuthorizationUrl))
										.append("\");\n");
								if (sc.flows.implicit.scopes != null) {
									for (Map.Entry<String, String> entry2 : sc.flows.implicit.scopes.entrySet()) {
										sb.append("        System.out.println(\"Component securitySchemesFlowScope ")
												.append(escape(k)).append(" implicit ").append(escape(entry2.getKey()))
												.append(" ").append(escape(entry2.getValue())).append("\");\n");
									}
								}
							}
							if (sc.flows.password != null) {
								sb.append("        System.out.println(\"Component securitySchemesFlow ")
										.append(escape(k)).append(" password ")
										.append(escape(sc.flows.password.authorizationUrl)).append(" ")
										.append(sc.flows.password.tokenUrl != null
												? escape(sc.flows.password.tokenUrl)
												: "-")
										.append(" ")
										.append(sc.flows.password.refreshUrl != null
												? escape(sc.flows.password.refreshUrl)
												: "-")
										.append(" ").append(escape(sc.flows.password.deviceAuthorizationUrl))
										.append("\");\n");
								if (sc.flows.password.scopes != null) {
									for (Map.Entry<String, String> entry2 : sc.flows.password.scopes.entrySet()) {
										sb.append("        System.out.println(\"Component securitySchemesFlowScope ")
												.append(escape(k)).append(" password ").append(escape(entry2.getKey()))
												.append(" ").append(escape(entry2.getValue())).append("\");\n");
									}
								}
							}
							if (sc.flows.clientCredentials != null) {
								sb.append("        System.out.println(\"Component securitySchemesFlow ")
										.append(escape(k)).append(" clientCredentials ")
										.append(escape(sc.flows.clientCredentials.authorizationUrl)).append(" ")
										.append(sc.flows.clientCredentials.tokenUrl != null
												? escape(sc.flows.clientCredentials.tokenUrl)
												: "-")
										.append(" ")
										.append(sc.flows.clientCredentials.refreshUrl != null
												? escape(sc.flows.clientCredentials.refreshUrl)
												: "-")
										.append(" ")
										.append(sc.flows.clientCredentials.deviceAuthorizationUrl != null
												? escape(sc.flows.clientCredentials.deviceAuthorizationUrl)
												: "-")
										.append("\");\n");
								if (sc.flows.clientCredentials.scopes != null) {
									for (Map.Entry<String, String> entry2 : sc.flows.clientCredentials.scopes
											.entrySet()) {
										sb.append("        System.out.println(\"Component securitySchemesFlowScope ")
												.append(escape(k)).append(" clientCredentials ")
												.append(escape(entry2.getKey())).append(" ")
												.append(escape(entry2.getValue())).append("\");\n");
									}
								}
							}
							if (sc.flows.authorizationCode != null) {
								sb.append("        System.out.println(\"Component securitySchemesFlow ")
										.append(escape(k)).append(" authorizationCode ")
										.append(sc.flows.authorizationCode.authorizationUrl != null
												? escape(sc.flows.authorizationCode.authorizationUrl)
												: "-")
										.append(" ")
										.append(sc.flows.authorizationCode.tokenUrl != null
												? escape(sc.flows.authorizationCode.tokenUrl)
												: "-")
										.append(" ")
										.append(sc.flows.authorizationCode.refreshUrl != null
												? escape(sc.flows.authorizationCode.refreshUrl)
												: "-")
										.append(" ")
										.append(sc.flows.authorizationCode.deviceAuthorizationUrl != null
												? escape(sc.flows.authorizationCode.deviceAuthorizationUrl)
												: "-")
										.append("\");\n");
								if (sc.flows.authorizationCode.scopes != null) {
									for (Map.Entry<String, String> entry2 : sc.flows.authorizationCode.scopes
											.entrySet()) {
										sb.append("        System.out.println(\"Component securitySchemesFlowScope ")
												.append(escape(k)).append(" authorizationCode ")
												.append(escape(entry2.getKey())).append(" ")
												.append(escape(entry2.getValue())).append("\");\n");
									}
								}
							}
							if (sc.flows.deviceAuthorization != null) {
								sb.append("        System.out.println(\"Component securitySchemesFlow ")
										.append(escape(k)).append(" deviceAuthorization ")
										.append(sc.flows.deviceAuthorization.authorizationUrl != null
												? escape(sc.flows.deviceAuthorization.authorizationUrl)
												: "-")
										.append(" ")
										.append(sc.flows.deviceAuthorization.tokenUrl != null
												? escape(sc.flows.deviceAuthorization.tokenUrl)
												: "-")
										.append(" ")
										.append(sc.flows.deviceAuthorization.refreshUrl != null
												? escape(sc.flows.deviceAuthorization.refreshUrl)
												: "-")
										.append(" ")
										.append(sc.flows.deviceAuthorization.deviceAuthorizationUrl != null
												? escape(sc.flows.deviceAuthorization.deviceAuthorizationUrl)
												: "-")
										.append("\");\n");
								if (sc.flows.deviceAuthorization.scopes != null) {
									for (Map.Entry<String, String> entry2 : sc.flows.deviceAuthorization.scopes
											.entrySet()) {
										sb.append("        System.out.println(\"Component securitySchemesFlowScope ")
												.append(escape(k)).append(" deviceAuthorization ")
												.append(escape(entry2.getKey())).append(" ")
												.append(escape(entry2.getValue())).append("\");\n");
									}
								}
							}
						}
					}
				}
			}
			if (api.components.links != null) {
				for (java.util.Map.Entry<String, ReferenceOr<openapi.Link>> entry : api.components.links.entrySet()) {
					String k = entry.getKey();
					if (entry.getValue() != null) {
						openapi.Link lnk = entry.getValue().value;
						sb.append("        System.out.println(\"Component links ").append(escape(k));
						if (lnk.operationId != null)
							sb.append(" operationId=").append(escape(lnk.operationId));
						if (lnk.operationRef != null)
							sb.append(" operationRef=").append(escape(lnk.operationRef));
						if (lnk.description != null)
							sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
						if (lnk.requestBody != null)
							sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
						if (lnk.server != null && lnk.server.url != null)
							sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
						sb.append("\");\n");
						if (lnk.parameters != null) {
							for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
								sb.append("        System.out.println(\"Component linksParam ").append(escape(k))
										.append(" ").append(escape(pe.getKey())).append(" ")
										.append(escape(pe.getValue().toString())).append("\");\n");
							}
						}
					} else {
						sb.append("        System.out.println(\"Component links ").append(escape(k)).append("\");\n");
					}
				}
			}
			if (api.components.callbacks != null)
				for (String k : api.components.callbacks.keySet())
					sb.append("        System.out.println(\"Component callbacks ").append(escape(k)).append("\");\n");
			if (api.components.pathItems != null)
				for (String k : api.components.pathItems.keySet())
					sb.append("        System.out.println(\"Component pathItems ").append(escape(k)).append("\");\n");
			if (api.components.mediaTypes != null)
				for (String k : api.components.mediaTypes.keySet())
					sb.append("        System.out.println(\"Component mediaTypes ").append(escape(k)).append("\");\n");
		}
	}

	/**
	 * Emits all PathItem operations inside {@code printHelp()}.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param api
	 *            The OpenAPI specification.
	 */
	public static void emitOperations(StringBuilder sb, OpenAPI api) {
		sb.append("        System.out.println(\"Commands:\");\n");
		if (api.paths != null && api.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> pe : api.paths.pathItems.entrySet()) {
				String path = pe.getKey();
				PathItem pi = pe.getValue();
				if (pi.get != null)
					appendOp(sb, "get", path, pi.get);
				if (pi.post != null)
					appendOp(sb, "post", path, pi.post);
				if (pi.put != null)
					appendOp(sb, "put", path, pi.put);
				if (pi.delete != null)
					appendOp(sb, "delete", path, pi.delete);
				if (pi.patch != null)
					appendOp(sb, "patch", path, pi.patch);
				if (pi.options != null)
					appendOp(sb, "options", path, pi.options);
				if (pi.head != null)
					appendOp(sb, "head", path, pi.head);
				if (pi.trace != null)
					appendOp(sb, "trace", path, pi.trace);
				if (pi.query != null)
					appendOp(sb, "query", path, pi.query);
			}
		}
	}

	/**
	 * Appends CLI help output statements describing an OpenAPI operation, its
	 * parameters, request body, responses, headers, links, and encodings.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param method
	 *            The HTTP method (lowercase, e.g. {@code get}).
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 */
	private static void appendOp(StringBuilder sb, String method, String path, Operation op) {
		sb.append("        System.out.println(\"Operation: ").append(method).append(" ").append(escape(path))
				.append("\");\n");
		String tags = op.tags != null ? " (Tags: " + String.join(", ", op.tags) + ")" : "";
		String dep = (op.deprecated != null && op.deprecated) ? "[DEPRECATED] " : "";
		String sum = op.summary != null ? op.summary : "";
		sb.append("        System.out.println(\"Operation Object ").append(dep).append(escape(sum)).append(escape(tags))
				.append("\");\n");
		if (op.operationId != null) {
			sb.append("        System.out.println(\"    OperationId: ").append(escape(op.operationId)).append("\");\n");
		}
		if (op.description != null) {
			sb.append("        System.out.println(\"    Description: ").append(escape(op.description)).append("\");\n");
		}
		if (op.externalDocs != null) {
			sb.append("        System.out.println(\"See also: ").append(escape(op.externalDocs.url)).append(" ")
					.append(escape(op.externalDocs.description == null ? "" : op.externalDocs.description))
					.append("\");\n");
		}
		if (op.callbacks != null) {
			for (String ck : op.callbacks.keySet()) {
				sb.append("        System.out.println(\"Callback: ").append(escape(ck)).append("\");\n");
			}
		}
		if (op.parameters != null) {
			for (ReferenceOr<Parameter> pObj : op.parameters) {
				if (pObj == null)
					continue;
				Parameter p = pObj.value;
				String req = (p.required != null && p.required) ? " (required)" : "";
				String pDep = (p.deprecated != null && p.deprecated) ? " [DEPRECATED]" : "";
				sb.append("        System.out.println(\"    --").append(escape(p.name)).append(req).append(pDep)
						.append(" : ").append(escape(p.description == null ? "" : p.description)).append("\");\n");
				if (p.example != null) {
					sb.append("        System.out.println(\"      Example: ").append(escape(p.example.toString()))
							.append("\");\n");
				}
				if (p.examples != null) {
					for (Map.Entry<String, ReferenceOr<Example>> exEntry : p.examples.entrySet()) {
						String key = exEntry.getKey();
						if (exEntry.getValue() == null)
							continue;
						Example ex = exEntry.getValue().value;
						String exSum = ex.summary != null ? ex.summary : "null";
						String exDesc = ex.description != null ? ex.description : "null";
						String exVal = ex.value != null ? ex.value.toString() : "null";
						sb.append("        System.out.println(\"      Example ").append(escape(key)).append(": ")
								.append(escape(exSum)).append(" - ").append(escape(exDesc)).append(" = ")
								.append(escape(exVal)).append("\");\n");
					}
				}
			}
		}
		if (op.requestBody != null && op.requestBody.value != null) {
			RequestBody rb = op.requestBody.value;
			String rreq = (rb.required != null && rb.required) ? " (required)" : "";
			String rdesc = rb.description != null ? rb.description : "";
			String rct = rb.content != null ? " [Content-Types: " + String.join(", ", rb.content.keySet()) + "]" : "";
			sb.append("        System.out.println(\"    --requestBody").append(rreq).append(": ").append(escape(rdesc))
					.append(escape(rct)).append("\");\n");
			appendMediaTypeEncodings(sb, "RequestBody", "", rb.content);
		}
		if (op.responses != null && op.responses.statusCodes != null) {
			for (Map.Entry<String, ReferenceOr<Response>> reEntry : op.responses.statusCodes.entrySet()) {
				String code = reEntry.getKey();
				if (reEntry.getValue() == null)
					continue;
				Response r = reEntry.getValue().value;
				String rdesc = r.description != null ? r.description : "";
				String rct = r.content != null ? " [Content-Types: " + String.join(", ", r.content.keySet()) + "]" : "";
				sb.append("        System.out.println(\"    Returns ").append(escape(code)).append(": ")
						.append(escape(rdesc)).append(escape(rct)).append("\");\n");
				appendMediaTypeEncodings(sb, "Response", escape(code) + " ", r.content);
				if (r.headers != null) {
					for (Map.Entry<String, ReferenceOr<Header>> heEntry : r.headers.entrySet()) {
						String hName = heEntry.getKey();
						if (heEntry.getValue() == null)
							continue;
						Header h = heEntry.getValue().value;
						String hreq = (h.required != null && h.required) ? " (required)" : "";
						String hDep = (h.deprecated != null && h.deprecated) ? " [DEPRECATED]" : "";
						sb.append("        System.out.println(\"      Header ").append(escape(hName)).append(hreq)
								.append(hDep).append(": ").append(escape(h.description == null ? "" : h.description))
								.append("\");\n");
					}
				}
				if (r.links != null) {
					for (Map.Entry<String, ReferenceOr<openapi.Link>> leEntry : r.links.entrySet()) {
						String lName = leEntry.getKey();
						Link lnk = leEntry.getValue().value;
						sb.append("        System.out.println(\"      Link ").append(escape(lName));
						if (lnk.operationId != null)
							sb.append(" operationId=").append(escape(lnk.operationId));
						if (lnk.operationRef != null)
							sb.append(" operationRef=").append(escape(lnk.operationRef));
						if (lnk.description != null)
							sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
						if (lnk.requestBody != null)
							sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
						if (lnk.server != null && lnk.server.url != null)
							sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
						sb.append("\");\n");
						if (lnk.parameters != null) {
							for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
								sb.append("        System.out.println(\"        LinkParam ").append(escape(lName))
										.append(" ").append(escape(pe.getKey())).append(" ")
										.append(escape(pe.getValue().toString())).append("\");\n");
							}
						}
					}
				}
			}
		}
		if (op.responses != null && op.responses.defaultResponse != null
				&& op.responses.defaultResponse.value != null) {
			Response r = op.responses.defaultResponse.value;
			String rdesc = r.description != null ? r.description : "";
			String rct = r.content != null ? " [Content-Types: " + String.join(", ", r.content.keySet()) + "]" : "";
			sb.append("        System.out.println(\"    Returns default: ").append(escape(rdesc)).append(escape(rct))
					.append("\");\n");
			appendMediaTypeEncodings(sb, "Response", "default ", r.content);
			if (r.headers != null) {
				for (Map.Entry<String, ReferenceOr<Header>> heEntry : r.headers.entrySet()) {
					String hName = heEntry.getKey();
					Header h = heEntry.getValue().value;
					String hreq = (h.required != null && h.required) ? " (required)" : "";
					String hDep = (h.deprecated != null && h.deprecated) ? " [DEPRECATED]" : "";
					sb.append("        System.out.println(\"      Header ").append(escape(hName)).append(hreq)
							.append(hDep).append(": ").append(escape(h.description == null ? "" : h.description))
							.append("\");\n");
				}
			}
			if (r.links != null) {
				for (Map.Entry<String, ReferenceOr<Link>> leEntry : r.links.entrySet()) {
					String lName = leEntry.getKey();
					Link lnk = leEntry.getValue().value;
					sb.append("        System.out.println(\"      Link ").append(escape(lName));
					if (lnk.operationId != null)
						sb.append(" operationId=").append(escape(lnk.operationId));
					if (lnk.operationRef != null)
						sb.append(" operationRef=").append(escape(lnk.operationRef));
					if (lnk.description != null)
						sb.append(" description=\\\"").append(escape(lnk.description)).append("\\\"");
					if (lnk.requestBody != null)
						sb.append(" requestBody=\\\"").append(escape(lnk.requestBody.toString())).append("\\\"");
					if (lnk.server != null && lnk.server.url != null)
						sb.append(" serverUrl=\\\"").append(escape(lnk.server.url)).append("\\\"");
					sb.append("\");\n");
					if (lnk.parameters != null) {
						for (java.util.Map.Entry<String, Object> pe : lnk.parameters.entrySet()) {
							sb.append("        System.out.println(\"        LinkParam ").append(escape(lName))
									.append(" ").append(escape(pe.getKey())).append(" ")
									.append(escape(pe.getValue().toString())).append("\");\n");
						}
					}
				}
			}
		}
	}

	/**
	 * Appends request body or response media type item schemas and encodings so
	 * {@link Parse} can reconstruct them losslessly.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param prefix
	 *            Either {@code "RequestBody"} or {@code "Response"}.
	 * @param codePrefix
	 *            Empty string for request bodies, or {@code "<code> "} for
	 *            responses.
	 * @param contentMap
	 *            The media type map to inspect.
	 */
	private static void appendMediaTypeEncodings(StringBuilder sb, String prefix, String codePrefix,
			Map<String, MediaType> contentMap) {
		if (contentMap == null) {
			return;
		}
		for (Map.Entry<String, MediaType> mEntry : contentMap.entrySet()) {
			MediaType mt = mEntry.getValue();
			if (mt.itemSchema != null && mt.itemSchema.value != null && mt.itemSchema.value.type != null) {
				sb.append("        System.out.println(\"      ").append(prefix).append("ContentItemSchema ")
						.append(codePrefix).append(escape(mEntry.getKey())).append(" ")
						.append(escape(mt.itemSchema.value.type.single)).append("\");\n");
			}
			if (mt.prefixEncoding != null) {
				for (Encoding enc : mt.prefixEncoding) {
					if (enc.contentType != null) {
						sb.append("        System.out.println(\"      ").append(prefix).append("ContentPrefixEncoding ")
								.append(codePrefix).append(escape(mEntry.getKey())).append(" ")
								.append(escape(enc.contentType)).append("\");\n");
					}
				}
			}
			if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
				sb.append("        System.out.println(\"      ").append(prefix).append("ContentItemEncoding ")
						.append(codePrefix).append(escape(mEntry.getKey())).append(" ")
						.append(escape(mt.itemEncoding.contentType)).append("\");\n");
			}
			if (mt.encoding != null) {
				for (Map.Entry<String, Encoding> encEntry : mt.encoding.entrySet()) {
					Encoding enc = encEntry.getValue();
					if (enc.contentType != null) {
						sb.append("        System.out.println(\"      ").append(prefix).append("Encoding ")
								.append(codePrefix).append(escape(mEntry.getKey())).append(" ")
								.append(escape(encEntry.getKey())).append(" ").append(escape(enc.contentType))
								.append("\");\n");
					}
					if (enc.prefixEncoding != null) {
						for (Encoding pEnc : enc.prefixEncoding) {
							if (pEnc.contentType != null) {
								sb.append("        System.out.println(\"      ").append(prefix)
										.append("EncodingPrefixEncoding ").append(codePrefix)
										.append(escape(mEntry.getKey())).append(" ").append(escape(encEntry.getKey()))
										.append(" ").append(escape(pEnc.contentType)).append("\");\n");
							}
						}
					}
					if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
						sb.append("        System.out.println(\"      ").append(prefix).append("EncodingItemEncoding ")
								.append(codePrefix).append(escape(mEntry.getKey())).append(" ")
								.append(escape(encEntry.getKey())).append(" ")
								.append(escape(enc.itemEncoding.contentType)).append("\");\n");
					}
				}
			}
		}
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
	public static boolean parseInfoAndServers(String line, OpenAPI api) {
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
	public static boolean parseComponentLine(String line, OpenAPI api, ParseState state) {
		if (line.startsWith("Component schemas ")) {
			String key = line.substring("Component schemas ".length());
			state.lastParsedSchema = new openapi.Schema();
			api.components.schemas.put(key, ReferenceOr.ofValue(state.lastParsedSchema));
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
				api.components.securitySchemes.put(key, ReferenceOr.ofValue(sc));
			}
			return true;
		} else if (line.startsWith("Component securitySchemesFlowScope ")) {
			Matcher m = Pattern.compile("^Component securitySchemesFlowScope (\\S+) (\\S+) (\\S+) (.*)$").matcher(line);
			if (m.find()) {
				String k = unescape(m.group(1));
				String flowType = unescape(m.group(2));
				String scopeKey = unescape(m.group(3));
				String scopeDesc = unescape(m.group(4));
				ReferenceOr<SecurityScheme> scRefOr = api.components.securitySchemes.get(k);
				SecurityScheme sc = scRefOr != null ? scRefOr.value : null;
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
				ReferenceOr<SecurityScheme> scRefOr = api.components.securitySchemes.get(k);
				SecurityScheme sc = scRefOr != null ? scRefOr.value : null;
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
				ReferenceOr<Link> lnkRefOr = api.components.links.get(parts[0]);
				Link lnk = lnkRefOr != null ? lnkRefOr.value : null;
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
			api.components.links.put(parts[0],
					ReferenceOr.ofValue(parseLinkAttributes(parts.length > 1 ? parts[1] : "")));
			return true;
		} else if (line.startsWith("Component ")) {
			String[] parts = line.split(" ");
			if (parts.length >= 3) {
				String type = parts[1];
				String key = parts[2];
				if (type.equals("responses")) {
					api.components.responses.put(key, ReferenceOr.ofValue(new Response()));
				} else if (type.equals("parameters")) {
					api.components.parameters.put(key, ReferenceOr.ofValue(new Parameter()));
				} else if (type.equals("requestBodies")) {
					api.components.requestBodies.put(key, ReferenceOr.ofValue(new RequestBody()));
				} else if (type.equals("headers")) {
					api.components.headers.put(key, ReferenceOr.ofValue(new Header()));
				} else if (type.equals("callbacks")) {
					api.components.callbacks.put(key, ReferenceOr.ofValue(new Callback()));
				} else if (type.equals("pathItems")) {
					api.components.pathItems.put(key, ReferenceOr.ofValue(new PathItem()));
				} else if (type.equals("mediaTypes")) {
					api.components.mediaTypes.put(key, ReferenceOr.ofValue(new MediaType()));
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
	public static void parseOperationLine(String line, OpenAPI api, ParseState state) {
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
				state.currentOp.callbacks.put(line.substring(10), ReferenceOr.ofValue(new Callback()));
			}
		} else if (line.startsWith("    --requestBody")) {
			state.currentParameter = null;
			if (state.currentOp != null) {
				RequestBody rb = new RequestBody();
				state.currentOp.requestBody = ReferenceOr.ofValue(rb);
				if (line.contains(" (required)")) {
					rb.required = true;
					line = line.replace(" (required)", "");
				}
				line = line.substring(17);
				if (line.startsWith(": "))
					line = line.substring(2);
				Matcher ctMatcher = Pattern.compile(" \\[Content-Types: (.*?)\\]$").matcher(line);
				if (ctMatcher.find()) {
					rb.content = new HashMap<>();
					for (String ct : ctMatcher.group(1).split(", ")) {
						rb.content.put(ct, new MediaType());
					}
					line = line.substring(0, line.length() - ctMatcher.group(0).length());
				}
				if (!line.isEmpty())
					rb.description = line;
			}
		} else if (line.startsWith("      RequestBody")) {
			if (state.currentOp != null && state.currentOp.requestBody != null) {
				RequestBody rb = state.currentOp.requestBody.value;
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
						state.currentParameter.examples.put(key, ReferenceOr.ofValue(ex));
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
				state.currentOp.parameters.add(ReferenceOr.ofValue(p));
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
					state.currentOp.responses.defaultResponse = ReferenceOr.ofValue(state.currentResponse);
				else
					state.currentOp.responses.statusCodes.put(code, ReferenceOr.ofValue(state.currentResponse));
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
						ReferenceOr<Response> rRefOr = code.equals("default")
								? state.currentOp.responses.defaultResponse
								: state.currentOp.responses.statusCodes.get(code);
						Response r = rRefOr != null ? rRefOr.value : null;
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
				state.currentResponse.headers.put(pre, ReferenceOr.ofValue(h));
			}
		} else if (line.startsWith("        LinkParam ")) {
			if (state.currentResponse != null && state.currentResponse.links != null) {
				String[] parts = line.substring(18).split(" ", 3);
				if (parts.length >= 3) {
					ReferenceOr<Link> lnkRefOr = state.currentResponse.links.get(parts[0]);
					Link lnk = lnkRefOr != null ? lnkRefOr.value : null;
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
				state.currentResponse.links.put(parts[0],
						ReferenceOr.ofValue(parseLinkAttributes(parts.length > 1 ? parts[1] : "")));
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
	private static void applyMediaTypeEncoding(Map<String, MediaType> contentMap, String kind, String rest) {
		if (contentMap == null) {
			return;
		}
		if (kind.startsWith("Content")) {
			String[] parts = rest.split(" ", 2);
			if (parts.length > 1 && contentMap.containsKey(parts[0])) {
				MediaType mt = contentMap.get(parts[0]);
				if (kind.equals("ContentItemSchema")) {
					Schema sch = new Schema();
					sch.type = SchemaType.ofSingle(parts[1]);
					mt.itemSchema = ReferenceOr.ofValue(sch);
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
