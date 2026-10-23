package routes;

import java.util.List;
import java.util.Map;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.Parameter;
import openapi.PathItem;

/**
 * Serializes OpenAPI route, operation, parameter, requestBody, and response
 * metadata into Javadoc tags for {@link Emit}.
 */
public class RouteJavadocEmitter {

	/**
	 * Default constructor.
	 */
	public RouteJavadocEmitter() {
	}

	/**
	 * Checks whether a string is non-null and non-empty.
	 *
	 * @param s
	 *            The string to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	public static boolean isNonEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	/**
	 * Builds the class-level Javadoc comment block for a generated client class.
	 *
	 * @param title
	 *            The sanitized client title.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The formatted class-level Javadoc comment string.
	 */
	public static String buildClassJavadoc(String title, OpenAPI model) {
		StringBuilder classDoc = new StringBuilder();
		classDoc.append("/**\n * Auto-generated client for ").append(title).append(".\n");
		if (model.openapi != null)
			classDoc.append(" * @openapiVersion ").append(model.openapi).append("\n");
		if (model.$self != null)
			classDoc.append(" * @openapiSelf ").append(model.$self).append("\n");
		if (model.jsonSchemaDialect != null)
			classDoc.append(" * @jsonSchemaDialect ").append(model.jsonSchemaDialect).append("\n");
		appendInfoClassJavadoc(classDoc, model.info);
		appendTagsAndServersClassJavadoc(classDoc, model);
		appendComponentsClassJavadoc(classDoc, model.components);
		classDoc.append(" */\n");
		return classDoc.toString();
	}

	/**
	 * Appends Info, Contact, and License Javadoc tags to the class Javadoc builder.
	 *
	 * @param classDoc
	 *            The target {@link StringBuilder}.
	 * @param info
	 *            The {@link openapi.Info} object (may be {@code null}).
	 */
	private static void appendInfoClassJavadoc(StringBuilder classDoc, openapi.Info info) {
		if (info == null) {
			return;
		}
		if (isNonEmpty(info.title))
			classDoc.append(" * @title ").append(info.title.replace("\n", " ")).append("\n");
		if (isNonEmpty(info.version))
			classDoc.append(" * @version ").append(info.version.replace("\n", " ")).append("\n");
		if (isNonEmpty(info.summary))
			classDoc.append(" * @summary ").append(info.summary.replace("\n", " ")).append("\n");
		if (isNonEmpty(info.description))
			classDoc.append(" * @description ").append(info.description.replace("\n", " ")).append("\n");
		if (isNonEmpty(info.termsOfService))
			classDoc.append(" * @termsOfService ").append(info.termsOfService).append("\n");
		if (info.contact != null) {
			if (isNonEmpty(info.contact.name))
				classDoc.append(" * @contactName ").append(info.contact.name).append("\n");
			if (isNonEmpty(info.contact.email))
				classDoc.append(" * @contactEmail ").append(info.contact.email).append("\n");
			if (isNonEmpty(info.contact.url))
				classDoc.append(" * @contactUrl ").append(info.contact.url).append("\n");
		}
		if (info.license != null) {
			if (isNonEmpty(info.license.name))
				classDoc.append(" * @licenseName ").append(info.license.name).append("\n");
			if (isNonEmpty(info.license.identifier))
				classDoc.append(" * @licenseIdentifier ").append(info.license.identifier).append("\n");
			if (isNonEmpty(info.license.url))
				classDoc.append(" * @licenseUrl ").append(info.license.url).append("\n");
		}
	}

	/**
	 * Appends global {@code @globalTag}, {@code @server*}, and path-level
	 * {@code @pathServer}/{@code @pathParameter} Javadoc tags to the class Javadoc
	 * builder.
	 *
	 * @param classDoc
	 *            The target {@link StringBuilder}.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 */
	private static void appendTagsAndServersClassJavadoc(StringBuilder classDoc, OpenAPI model) {
		if (model.tags != null) {
			for (openapi.Tag t : model.tags) {
				if (isNonEmpty(t.name)) {
					classDoc.append(" * @globalTag ").append(t.name);
					if (isNonEmpty(t.summary))
						classDoc.append(" summary=").append(t.summary.replace(" ", "_"));
					if (isNonEmpty(t.description))
						classDoc.append(" description=").append(t.description.replace(" ", "_"));
					if (t.externalDocs != null && isNonEmpty(t.externalDocs.url))
						classDoc.append(" externalDocsUrl=").append(t.externalDocs.url);
					if (isNonEmpty(t.parent))
						classDoc.append(" parent=").append(t.parent);
					if (isNonEmpty(t.kind))
						classDoc.append(" kind=").append(t.kind);
					classDoc.append("\n");
				}
			}
		}
		if (model.servers != null) {
			for (openapi.Server s : model.servers) {
				if (isNonEmpty(s.url)) {
					classDoc.append(" * @server ").append(s.url);
					if (isNonEmpty(s.description))
						classDoc.append(" ").append(s.description.replace("\n", " "));
					classDoc.append("\n");
					if (isNonEmpty(s.name)) {
						classDoc.append(" * @serverName ").append(s.name).append("\n");
						if (s.variables != null) {
							for (Map.Entry<String, openapi.ServerVariable> vEntry : s.variables.entrySet()) {
								openapi.ServerVariable sv = vEntry.getValue();
								String defVal = isNonEmpty(sv.defaultValue) ? sv.defaultValue : "-";
								classDoc.append(" * @serverVariable ").append(s.name).append(" ")
										.append(vEntry.getKey()).append(" ").append(defVal);
								if (isNonEmpty(sv.description))
									classDoc.append(" ").append(sv.description.replace("\n", " "));
								classDoc.append("\n");
								if (sv.enumValues != null && !sv.enumValues.isEmpty()) {
									classDoc.append(" * @serverVariableEnum ").append(s.name).append(" ")
											.append(vEntry.getKey()).append(" ").append(String.join(",", sv.enumValues))
											.append("\n");
								}
							}
						}
					}
				}
			}
		}
		if (model.paths != null && model.paths.pathItems != null) {
			for (Map.Entry<String, PathItem> pEntry : model.paths.pathItems.entrySet()) {
				String path = pEntry.getKey();
				PathItem pi = pEntry.getValue();
				if (pi.servers != null) {
					for (openapi.Server ps : pi.servers) {
						if (isNonEmpty(ps.url)) {
							classDoc.append(" * @pathServer ").append(path).append(" ").append(ps.url);
							if (isNonEmpty(ps.description))
								classDoc.append(" ").append(ps.description.replace("\n", " "));
							classDoc.append("\n");
						}
					}
				}
				if (pi.parameters != null) {
					for (openapi.ReferenceOr<Parameter> ppRef : pi.parameters) {
						Parameter pp = ppRef.value;
						if (pp != null) {
							String pName = isNonEmpty(pp.name) ? pp.name : "-";
							String pIn = isNonEmpty(pp.in) ? pp.in : "-";
							classDoc.append(" * @pathParameter ").append(path).append(" ").append(pName).append(" ")
									.append(pIn);
							if (isNonEmpty(pp.description))
								classDoc.append(" ").append(pp.description.replace("\n", " "));
							classDoc.append("\n");
						}
					}
				}
			}
		}
	}

	/**
	 * Appends component-level Javadoc tags ({@code @securityScheme*},
	 * {@code @component*}) to the class Javadoc builder.
	 *
	 * @param classDoc
	 *            The target {@link StringBuilder}.
	 * @param comp
	 *            The {@link openapi.Components} object (may be {@code null}).
	 */
	private static void appendComponentsClassJavadoc(StringBuilder classDoc, openapi.Components comp) {
		if (comp == null) {
			return;
		}
		if (comp.securitySchemes != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.SecurityScheme>> scEntry : comp.securitySchemes
					.entrySet()) {
				String sName = scEntry.getKey();
				openapi.SecurityScheme sc = scEntry.getValue().value;
				if (sc != null) {
					classDoc.append(" * @securityScheme ").append(sName).append(" type=")
							.append(isNonEmpty(sc.type) ? sc.type : "-").append(" scheme=")
							.append(isNonEmpty(sc.scheme) ? sc.scheme : "-").append(" in=")
							.append(isNonEmpty(sc.in) ? sc.in : "-").append(" name=")
							.append(isNonEmpty(sc.name) ? sc.name : "-").append(" bearerFormat=")
							.append(isNonEmpty(sc.bearerFormat) ? sc.bearerFormat : "-").append(" openIdConnectUrl=")
							.append(isNonEmpty(sc.openIdConnectUrl) ? sc.openIdConnectUrl : "-").append("\n");
					if (sc.flows != null) {
						appendOAuthFlowTag(classDoc, sName, "implicit", sc.flows.implicit);
						appendOAuthFlowTag(classDoc, sName, "password", sc.flows.password);
						appendOAuthFlowTag(classDoc, sName, "clientCredentials", sc.flows.clientCredentials);
						appendOAuthFlowTag(classDoc, sName, "authorizationCode", sc.flows.authorizationCode);
						appendOAuthFlowTag(classDoc, sName, "deviceAuthorization", sc.flows.deviceAuthorization);
					}
				}
			}
		}
		if (comp.responses != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Response>> rEntry : comp.responses.entrySet()) {
				openapi.Response r = rEntry.getValue().value;
				if (r != null) {
					classDoc.append(" * @componentResponse ").append(rEntry.getKey());
					if (isNonEmpty(r.description))
						classDoc.append(" ").append(r.description.replace("\n", " "));
					classDoc.append("\n");
				}
			}
		}
		if (comp.parameters != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Parameter>> pEntry : comp.parameters.entrySet()) {
				Parameter p = pEntry.getValue().value;
				if (p != null) {
					classDoc.append(" * @componentParameter ").append(pEntry.getKey()).append(" ")
							.append(isNonEmpty(p.name) ? p.name : "-").append(" ")
							.append(isNonEmpty(p.in) ? p.in : "-");
					if (isNonEmpty(p.description))
						classDoc.append(" ").append(p.description.replace("\n", " "));
					classDoc.append("\n");
				}
			}
		}
		if (comp.requestBodies != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.RequestBody>> rbEntry : comp.requestBodies.entrySet()) {
				openapi.RequestBody rb = rbEntry.getValue().value;
				if (rb != null) {
					classDoc.append(" * @componentRequestBody ").append(rbEntry.getKey());
					if (isNonEmpty(rb.description))
						classDoc.append(" ").append(rb.description.replace("\n", " "));
					classDoc.append("\n");
				}
			}
		}
		if (comp.headers != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Header>> hEntry : comp.headers.entrySet()) {
				openapi.Header h = hEntry.getValue().value;
				if (h != null) {
					classDoc.append(" * @componentHeader ").append(hEntry.getKey());
					if (isNonEmpty(h.description))
						classDoc.append(" ").append(h.description.replace("\n", " "));
					classDoc.append("\n");
				}
			}
		}
		if (comp.links != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Link>> lEntry : comp.links.entrySet()) {
				String lKey = lEntry.getKey();
				openapi.Link l = lEntry.getValue().value;
				if (l != null) {
					classDoc.append(" * @componentLink ").append(lKey);
					if (isNonEmpty(l.operationId))
						classDoc.append(" ").append(l.operationId);
					classDoc.append("\n");
					if (isNonEmpty(l.operationId))
						classDoc.append(" * @componentLinkOpId ").append(lKey).append(" ").append(l.operationId)
								.append("\n");
					if (isNonEmpty(l.operationRef))
						classDoc.append(" * @componentLinkOpRef ").append(lKey).append(" ").append(l.operationRef)
								.append("\n");
					if (isNonEmpty(l.description))
						classDoc.append(" * @componentLinkDesc ").append(lKey).append(" ")
								.append(l.description.replace("\n", " ")).append("\n");
					if (l.server != null && isNonEmpty(l.server.url))
						classDoc.append(" * @componentLinkServer ").append(lKey).append(" ").append(l.server.url)
								.append("\n");
					if (l.requestBody != null)
						classDoc.append(" * @componentLinkRequestBody ").append(lKey).append(" ")
								.append(l.requestBody.toString()).append("\n");
					if (l.parameters != null) {
						for (Map.Entry<String, common.PayloadValue> pe : l.parameters.entrySet()) {
							classDoc.append(" * @componentLinkParam ").append(lKey).append(" ").append(pe.getKey())
									.append(" ").append(pe.getValue().toString()).append("\n");
						}
					}
				}
			}
		}
		if (comp.callbacks != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Callback>> cbEntry : comp.callbacks.entrySet()) {
				openapi.Callback cb = cbEntry.getValue().value;
				if (cb != null && cb.pathItems != null) {
					for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
						for (openapi.HttpVerb v : piEntry.getValue().getOperationsMap().keySet()) {
							classDoc.append(" * @componentCallback ").append(cbEntry.getKey()).append(" ")
									.append(piEntry.getKey()).append(" ").append(v.name()).append("\n");
						}
					}
				}
			}
		}
		if (comp.pathItems != null) {
			for (Map.Entry<String, openapi.ReferenceOr<PathItem>> piEntry : comp.pathItems.entrySet()) {
				PathItem pi = piEntry.getValue().value;
				if (pi != null) {
					classDoc.append(" * @componentPathItem ").append(piEntry.getKey());
					if (isNonEmpty(pi.description))
						classDoc.append(" ").append(pi.description.replace("\n", " "));
					classDoc.append("\n");
				}
			}
		}
		if (comp.mediaTypes != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.MediaType>> mtEntry : comp.mediaTypes.entrySet()) {
				openapi.MediaType mt = mtEntry.getValue().value;
				if (mt != null) {
					String sType = (mt.schema != null && mt.schema.value != null && mt.schema.value.type != null)
							? mt.schema.value.type.single
							: "-";
					classDoc.append(" * @componentMediaType ").append(mtEntry.getKey()).append(" ").append(sType)
							.append("\n");
				}
			}
		}
	}

	/**
	 * Appends {@code @securitySchemeFlow} and {@code @securitySchemeFlowScope}
	 * Javadoc tags for a single OAuth flow.
	 *
	 * @param classDoc
	 *            The target {@link StringBuilder}.
	 * @param sName
	 *            The security scheme key.
	 * @param flowType
	 *            The flow type name.
	 * @param flow
	 *            The {@link openapi.OAuthFlow} instance (may be {@code null}).
	 */
	private static void appendOAuthFlowTag(StringBuilder classDoc, String sName, String flowType,
			openapi.OAuthFlow flow) {
		if (flow == null) {
			return;
		}
		String refresh = isNonEmpty(flow.refreshUrl) ? flow.refreshUrl : "-";
		if ("implicit".equals(flowType)) {
			String auth = isNonEmpty(flow.authorizationUrl) ? flow.authorizationUrl : "-";
			classDoc.append(" * @securitySchemeFlow implicit ").append(sName).append(" ").append(auth).append(" ")
					.append(refresh).append("\n");
		} else if ("password".equals(flowType) || "clientCredentials".equals(flowType)) {
			String token = isNonEmpty(flow.tokenUrl) ? flow.tokenUrl : "-";
			classDoc.append(" * @securitySchemeFlow ").append(flowType).append(" ").append(sName).append(" ")
					.append(token).append(" ").append(refresh).append("\n");
		} else if ("authorizationCode".equals(flowType)) {
			String auth = isNonEmpty(flow.authorizationUrl) ? flow.authorizationUrl : "-";
			String token = isNonEmpty(flow.tokenUrl) ? flow.tokenUrl : "-";
			classDoc.append(" * @securitySchemeFlow authorizationCode ").append(sName).append(" ").append(auth)
					.append(" ").append(token).append(" ").append(refresh).append("\n");
		} else {
			String dev = isNonEmpty(flow.deviceAuthorizationUrl) ? flow.deviceAuthorizationUrl : "-";
			String token = isNonEmpty(flow.tokenUrl) ? flow.tokenUrl : "-";
			classDoc.append(" * @securitySchemeFlow deviceAuthorization ").append(sName).append(" ").append(dev)
					.append(" ").append(token).append(" ").append(refresh).append("\n");
		}
		if (flow.scopes != null) {
			for (Map.Entry<String, String> scEntry : flow.scopes.entrySet()) {
				String desc = isNonEmpty(scEntry.getValue()) ? scEntry.getValue() : "-";
				classDoc.append(" * @securitySchemeFlowScope ").append(sName).append(" ").append(flowType).append(" ")
						.append(scEntry.getKey()).append(" ").append(desc).append("\n");
			}
		}
	}

	/**
	 * Appends operation-level Javadoc tags ({@code @callback}, {@code @pathRef},
	 * {@code @pathSummary}, {@code @pathDescription}, {@code @tag},
	 * {@code @externalDocs}, {@code @deprecated}, {@code @operationServer},
	 * {@code @operationSecurity}, {@code @param*}, {@code @requestBody*},
	 * {@code @response*}, {@code @return}, and {@code @throws}) to a method Javadoc
	 * builder.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 * @param item
	 *            The parent {@link PathItem}.
	 */
	public static void appendOperationJavadocTags(StringBuilder docBuilder, String path, Operation op, PathItem item) {
		if (op.tags != null) {
			for (String t : op.tags) {
				if (isNonEmpty(t))
					docBuilder.append(" * @tag ").append(t).append("\n");
			}
		}
		if (op.externalDocs != null && isNonEmpty(op.externalDocs.url)) {
			docBuilder.append(" * @externalDocs ").append(op.externalDocs.url);
			if (isNonEmpty(op.externalDocs.description))
				docBuilder.append(" ").append(op.externalDocs.description.replace("\n", " "));
			docBuilder.append("\n");
		}
		if (Boolean.TRUE.equals(op.deprecated)) {
			docBuilder.append(" * @deprecated true\n");
		}
		if (op.servers != null) {
			for (openapi.Server s : op.servers) {
				if (isNonEmpty(s.url)) {
					docBuilder.append(" * @operationServer ").append(s.url);
					if (isNonEmpty(s.description))
						docBuilder.append(" ").append(s.description.replace("\n", " "));
					docBuilder.append("\n");
				}
			}
		}
		if (op.security != null) {
			for (openapi.SecurityRequirement sr : op.security) {
				if (sr.requirements != null) {
					for (Map.Entry<String, List<String>> reqEntry : sr.requirements.entrySet()) {
						docBuilder.append(" * @operationSecurity ").append(reqEntry.getKey());
						if (reqEntry.getValue() != null && !reqEntry.getValue().isEmpty()) {
							docBuilder.append(" ").append(String.join(",", reqEntry.getValue()));
						}
						docBuilder.append("\n");
					}
				}
			}
		}
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> pRef : op.parameters) {
				appendParameterJavadocTags(docBuilder, pRef.value);
			}
		}
		if (op.callbacks != null && !op.callbacks.isEmpty()) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Callback>> cbEntry : op.callbacks.entrySet()) {
				openapi.Callback cb = cbEntry.getValue().value;
				for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
					String method = "POST";
					if (item.get != null)
						method = "GET";
					else if (item.put != null)
						method = "PUT";
					else if (item.delete != null)
						method = "DELETE";
					else if (item.patch != null)
						method = "PATCH";
					else if (item.query != null)
						method = "QUERY";
					docBuilder.append(" * @callback ").append(cbEntry.getKey()).append(" ").append(piEntry.getKey())
							.append(" ").append(method).append("\n");
				}
			}
		}

		if (isNonEmpty(item.ref)) {
			docBuilder.append(" * @pathRef ").append(path).append(" ").append(item.ref).append("\n");
		}
		if (isNonEmpty(item.summary)) {
			docBuilder.append(" * @pathSummary ").append(path).append(" ").append(item.summary.replace("\n", " "))
					.append("\n");
		}
		if (isNonEmpty(item.description)) {
			docBuilder.append(" * @pathDescription ").append(path).append(" ")
					.append(item.description.replace("\n", " ")).append("\n");
		}

		if (op.requestBody != null && op.requestBody.value != null) {
			openapi.RequestBody rb = op.requestBody.value;
			if (isNonEmpty(rb.description))
				docBuilder.append(" * @requestBody ").append(rb.description.replace("\n", " ")).append("\n");
			if (Boolean.TRUE.equals(rb.required))
				docBuilder.append(" * @requestBodyRequired true\n");
			appendMediaTypeJavadocTags(docBuilder, "requestBody", "", rb.content);
		}

		if (op.responses != null && op.responses.statusCodes != null) {
			for (java.util.Map.Entry<String, openapi.ReferenceOr<openapi.Response>> rEntry : op.responses.statusCodes
					.entrySet()) {
				String statusCode = rEntry.getKey();
				openapi.Response r = rEntry.getValue().value;
				if (r != null) {
					if (isNonEmpty(r.description))
						docBuilder.append(" * @response ").append(statusCode).append(" ")
								.append(r.description.replace("\n", " ")).append("\n");
					appendResponseDetailsJavadocTags(docBuilder, statusCode, r);
					appendMediaTypeJavadocTags(docBuilder, "response", statusCode + " ", r.content);
				}
			}
		}

		if (op.responses != null && op.responses.defaultResponse != null) {
			openapi.Response r = op.responses.defaultResponse.value;
			if (r != null) {
				if (isNonEmpty(r.description))
					docBuilder.append(" * @responseDefault ").append(r.description.replace("\n", " ")).append("\n");
				appendResponseDetailsJavadocTags(docBuilder, "default", r);
				appendMediaTypeJavadocTags(docBuilder, "response", "default ", r.content);
			}
		}

		docBuilder.append(" * @return HttpResponse\n");
		docBuilder.append(" * @throws IOException on error\n");
		docBuilder.append(" * @throws InterruptedException on error\n");
		docBuilder.append(" */\n");
	}

	/**
	 * Appends parameter-level Javadoc tags ({@code @requiredParam},
	 * {@code @deprecatedParam}, {@code @paramAllowEmptyValue}, {@code @paramStyle},
	 * {@code @paramExplode}, {@code @paramAllowReserved}, {@code @paramSchema},
	 * {@code @paramContent}, {@code @paramExample}, and {@code @paramExamples}) for
	 * a single {@link Parameter}.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param p
	 *            The {@link Parameter} to serialize.
	 */
	private static void appendParameterJavadocTags(StringBuilder docBuilder, Parameter p) {
		if (p == null || !isNonEmpty(p.name)) {
			return;
		}
		String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
		if (safeName.isEmpty()) {
			return;
		}
		if (Boolean.TRUE.equals(p.required))
			docBuilder.append(" * @requiredParam ").append(safeName).append("\n");
		if (Boolean.TRUE.equals(p.deprecated))
			docBuilder.append(" * @deprecatedParam ").append(safeName).append("\n");
		if (Boolean.TRUE.equals(p.allowEmptyValue))
			docBuilder.append(" * @paramAllowEmptyValue ").append(safeName).append("\n");
		if (isNonEmpty(p.style))
			docBuilder.append(" * @paramStyle ").append(safeName).append(" ").append(p.style).append("\n");
		if (Boolean.TRUE.equals(p.explode))
			docBuilder.append(" * @paramExplode ").append(safeName).append("\n");
		if (Boolean.TRUE.equals(p.allowReserved))
			docBuilder.append(" * @paramAllowReserved ").append(safeName).append("\n");
		if (p.schema != null && p.schema.value != null && p.schema.value.type != null)
			docBuilder.append(" * @paramSchema ").append(safeName).append(" ").append(p.schema.value.type.single)
					.append("\n");
		if (p.content != null) {
			for (String ct : p.content.keySet()) {
				docBuilder.append(" * @paramContent ").append(safeName).append(" ").append(ct).append("\n");
			}
		}
		if (p.example != null)
			docBuilder.append(" * @paramExample ").append(safeName).append(" ").append(p.example).append("\n");
		if (p.examples != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Example>> exEntry : p.examples.entrySet()) {
				openapi.Example ex = exEntry.getValue().value;
				if (ex != null) {
					String exSum = ex.summary != null ? ex.summary : "";
					String exDesc = ex.description != null ? ex.description : "";
					String exVal = ex.value != null ? ex.value.toString() : "";
					docBuilder.append(" * @paramExamples ").append(safeName).append(" ").append(exEntry.getKey())
							.append(" ").append(exSum).append("|").append(exDesc).append("|").append(exVal)
							.append("\n");
				}
			}
		}
	}

	/**
	 * Appends response header ({@code @responseHeader*}) and response link
	 * ({@code @responseLink*}) Javadoc tags for a single {@link openapi.Response}.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param statusCode
	 *            The status code key or {@code "default"}.
	 * @param r
	 *            The {@link openapi.Response} to serialize.
	 */
	private static void appendResponseDetailsJavadocTags(StringBuilder docBuilder, String statusCode,
			openapi.Response r) {
		if (r.headers != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Header>> hEntry : r.headers.entrySet()) {
				String hName = hEntry.getKey();
				openapi.Header h = hEntry.getValue().value;
				if (h != null) {
					if (isNonEmpty(h.description))
						docBuilder.append(" * @responseHeader ").append(statusCode).append(" ").append(hName)
								.append(" ").append(h.description.replace("\n", " ")).append("\n");
					if (Boolean.TRUE.equals(h.required))
						docBuilder.append(" * @responseHeaderRequired ").append(statusCode).append(" ").append(hName)
								.append("\n");
					if (Boolean.TRUE.equals(h.deprecated))
						docBuilder.append(" * @responseHeaderDeprecated ").append(statusCode).append(" ").append(hName)
								.append("\n");
					if (isNonEmpty(h.style))
						docBuilder.append(" * @responseHeaderStyle ").append(statusCode).append(" ").append(hName)
								.append(" ").append(h.style).append("\n");
					if (Boolean.TRUE.equals(h.explode))
						docBuilder.append(" * @responseHeaderExplode ").append(statusCode).append(" ").append(hName)
								.append("\n");
					if (h.schema != null && h.schema.value != null && h.schema.value.type != null)
						docBuilder.append(" * @responseHeaderSchema ").append(statusCode).append(" ").append(hName)
								.append(" ").append(h.schema.value.type.single).append("\n");
					if (h.example != null)
						docBuilder.append(" * @responseHeaderExample ").append(statusCode).append(" ").append(hName)
								.append(" ").append(h.example).append("\n");
					if (h.content != null) {
						for (String ct : h.content.keySet()) {
							docBuilder.append(" * @responseHeaderContent ").append(statusCode).append(" ").append(hName)
									.append(" ").append(ct).append("\n");
						}
					}
					if (h.examples != null) {
						for (Map.Entry<String, openapi.ReferenceOr<openapi.Example>> exEntry : h.examples.entrySet()) {
							openapi.Example ex = exEntry.getValue().value;
							if (ex != null) {
								String exSum = ex.summary != null ? ex.summary : "";
								String exDesc = ex.description != null ? ex.description : "";
								String exVal = ex.value != null ? ex.value.toString() : "";
								docBuilder.append(" * @responseHeaderExamples ").append(statusCode).append(" ")
										.append(hName).append(" ").append(exEntry.getKey()).append(" ").append(exSum)
										.append("|").append(exDesc).append("|").append(exVal).append("\n");
							}
						}
					}
				}
			}
		}
		if (r.links != null) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Link>> lEntry : r.links.entrySet()) {
				String lName = lEntry.getKey();
				openapi.Link l = lEntry.getValue().value;
				if (l != null) {
					if (isNonEmpty(l.operationId))
						docBuilder.append(" * @responseLinkOpId ").append(statusCode).append(" ").append(lName)
								.append(" ").append(l.operationId).append("\n");
					if (isNonEmpty(l.operationRef))
						docBuilder.append(" * @responseLinkOpRef ").append(statusCode).append(" ").append(lName)
								.append(" ").append(l.operationRef).append("\n");
					if (isNonEmpty(l.description))
						docBuilder.append(" * @responseLinkDesc ").append(statusCode).append(" ").append(lName)
								.append(" ").append(l.description.replace("\n", " ")).append("\n");
					if (l.server != null && isNonEmpty(l.server.url))
						docBuilder.append(" * @responseLinkServer ").append(statusCode).append(" ").append(lName)
								.append(" ").append(l.server.url).append("\n");
					if (l.requestBody != null)
						docBuilder.append(" * @responseLinkRequestBody ").append(statusCode).append(" ").append(lName)
								.append(" ").append(l.requestBody.toString()).append("\n");
					if (l.parameters != null) {
						for (Map.Entry<String, common.PayloadValue> pe : l.parameters.entrySet()) {
							docBuilder.append(" * @responseLinkParam ").append(statusCode).append(" ").append(lName)
									.append(" ").append(pe.getKey()).append(" ").append(pe.getValue().toString())
									.append("\n");
						}
					}
				}
			}
		}
	}

	/**
	 * Appends Javadoc tags describing a map of {@link openapi.MediaType} entries
	 * for either a request body or a response.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param tagPrefix
	 *            Either {@code "requestBody"} or {@code "response"}.
	 * @param statusPrefix
	 *            Empty string for request bodies, or {@code "<statusCode> "} for
	 *            responses.
	 * @param contentMap
	 *            The media type map to serialize into Javadoc tags.
	 */
	public static void appendMediaTypeJavadocTags(StringBuilder docBuilder, String tagPrefix, String statusPrefix,
			Map<String, openapi.MediaType> contentMap) {
		if (contentMap == null) {
			return;
		}
		for (java.util.Map.Entry<String, openapi.MediaType> mEntry : contentMap.entrySet()) {
			String mtStr = mEntry.getKey();
			openapi.MediaType mt = mEntry.getValue();
			docBuilder.append(" * @").append(tagPrefix).append("Content ").append(statusPrefix).append(mtStr)
					.append("\n");
			if (mt.schema != null && mt.schema.value != null && mt.schema.value.type != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentSchema ").append(statusPrefix).append(mtStr)
						.append(" ").append(mt.schema.value.type.single).append("\n");
			}
			if (mt.itemSchema != null && mt.itemSchema.value != null && mt.itemSchema.value.type != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentItemSchema ").append(statusPrefix)
						.append(mtStr).append(" ").append(mt.itemSchema.value.type.single).append("\n");
			}
			if (mt.prefixEncoding != null) {
				for (openapi.Encoding enc : mt.prefixEncoding) {
					if (enc.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("ContentPrefixEncoding ")
								.append(statusPrefix).append(mtStr).append(" ").append(enc.contentType).append("\n");
					}
				}
			}
			if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentItemEncoding ").append(statusPrefix)
						.append(mtStr).append(" ").append(mt.itemEncoding.contentType).append("\n");
			}
			if (mt.encoding != null) {
				for (java.util.Map.Entry<String, openapi.Encoding> encEntry : mt.encoding.entrySet()) {
					String encKey = encEntry.getKey();
					openapi.Encoding enc = encEntry.getValue();
					if (enc.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("Encoding ").append(statusPrefix)
								.append(mtStr).append(" ").append(encKey).append(" ").append(enc.contentType)
								.append("\n");
					}
					if (enc.prefixEncoding != null) {
						for (openapi.Encoding pEnc : enc.prefixEncoding) {
							if (pEnc.contentType != null) {
								docBuilder.append(" * @").append(tagPrefix).append("EncodingPrefixEncoding ")
										.append(statusPrefix).append(mtStr).append(" ").append(encKey).append(" ")
										.append(pEnc.contentType).append("\n");
							}
						}
					}
					if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("EncodingItemEncoding ").append(statusPrefix)
								.append(mtStr).append(" ").append(encKey).append(" ")
								.append(enc.itemEncoding.contentType).append("\n");
					}
				}
			}
			if (mt.example != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentExample ").append(statusPrefix).append(mtStr)
						.append(" ").append(mt.example).append("\n");
			}
			if (mt.examples != null) {
				for (java.util.Map.Entry<String, openapi.ReferenceOr<openapi.Example>> exEntry : mt.examples
						.entrySet()) {
					openapi.Example ex = exEntry.getValue().value;
					String exValue = ex.value != null ? ex.value.toString() : "";
					String exSummary = ex.summary != null ? ex.summary : "";
					String exDesc = ex.description != null ? ex.description : "";
					docBuilder.append(" * @").append(tagPrefix).append("ContentExamples ").append(statusPrefix)
							.append(mtStr).append(" ").append(exEntry.getKey()).append(" ")
							.append(common.JavadocMetadataCodec.encodeUnderscoreToken(exSummary)).append("|")
							.append(common.JavadocMetadataCodec.encodeUnderscoreToken(exDesc)).append("|")
							.append(exValue).append("\n");
				}
			}
		}
	}
}
