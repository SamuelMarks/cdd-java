package openapi;

import common.PayloadValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Emits OpenAPI 3.2.0 and Swagger 2.0 descriptions from the {@link OpenAPI}
 * AST.
 */
public class Emit {

	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits OpenAPI description to string.
	 *
	 * @param api
	 *            The OpenAPI object.
	 * @return JSON string.
	 */
	public static String toString(OpenAPI api) {
		JSONObject root = new JSONObject();
		putIfNotNull(root, "openapi", api.openapi);
		putIfNotNull(root, "swagger", api.swagger);
		putIfNotNull(root, "$self", api.$self);
		putIfNotNull(root, "jsonSchemaDialect", api.jsonSchemaDialect);
		putIfNotNull(root, "host", api.host);
		putIfNotNull(root, "basePath", api.basePath);
		putNonEmptyList(root, "schemes", api.schemes);
		putNonEmptyList(root, "consumes", api.consumes);
		putNonEmptyList(root, "produces", api.produces);
		if (api.info != null) {
			root.put("info", serializeInfo(api.info));
		}
		if (api.externalDocs != null) {
			root.put("externalDocs", serializeExternalDocs(api.externalDocs));
		}
		if (isNonEmpty(api.servers)) {
			root.put("servers", serializeServers(api.servers));
		}
		if (isNonEmpty(api.security)) {
			root.put("security", serializeSecurity(api.security));
		}
		if (isNonEmpty(api.tags)) {
			root.put("tags", serializeTags(api.tags));
		}
		if (api.paths != null) {
			JSONObject pathsObj = serializePaths(api.paths);
			if (!pathsObj.isEmpty()) {
				root.put("paths", pathsObj);
			}
		}
		if (isNonEmpty(api.webhooks)) {
			JSONObject whObj = new JSONObject();
			for (Map.Entry<String, PathItem> e : api.webhooks.entrySet()) {
				whObj.put(e.getKey(), serializePathItem(e.getValue()));
			}
			root.put("webhooks", whObj);
		}
		if (api.definitions != null) {
			JSONObject defsObj = new JSONObject();
			for (Map.Entry<String, Schema> e : api.definitions.entrySet()) {
				if (e.getValue() != null) {
					defsObj.put(e.getKey(), serializeSchema(e.getValue()));
				}
			}
			root.put("definitions", defsObj);
		}
		if (isNonEmpty(api.parameters)) {
			JSONObject pMap = new JSONObject();
			for (Map.Entry<String, Parameter> e : api.parameters.entrySet()) {
				pMap.put(e.getKey(), serializeParameter(e.getValue()));
			}
			root.put("parameters", pMap);
		}
		if (isNonEmpty(api.responses)) {
			JSONObject rMap = new JSONObject();
			for (Map.Entry<String, Response> e : api.responses.entrySet()) {
				rMap.put(e.getKey(), serializeResponse(e.getValue()));
			}
			root.put("responses", rMap);
		}
		if (isNonEmpty(api.securityDefinitions)) {
			JSONObject sdMap = new JSONObject();
			for (Map.Entry<String, SecurityScheme> e : api.securityDefinitions.entrySet()) {
				sdMap.put(e.getKey(), serializeSecurityScheme(e.getValue()));
			}
			root.put("securityDefinitions", sdMap);
		}
		if (api.components != null) {
			JSONObject compObj = serializeComponents(api.components);
			if (!compObj.isEmpty()) {
				root.put("components", compObj);
			}
		}
		putExtensions(root, api.extensions);
		return root.toString(2);
	}

	/**
	 * Checks whether a collection is non-null and non-empty.
	 *
	 * @param c
	 *            The collection to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	private static boolean isNonEmpty(java.util.Collection<?> c) {
		return c != null && !c.isEmpty();
	}

	/**
	 * Checks whether a map is non-null and non-empty.
	 *
	 * @param m
	 *            The map to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	private static boolean isNonEmpty(Map<?, ?> m) {
		return m != null && !m.isEmpty();
	}

	/**
	 * Puts a key-value pair into a JSONObject if the value is non-null.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param val
	 *            The property value.
	 */
	private static void putIfNotNull(JSONObject obj, String key, Object val) {
		if (val != null) {
			obj.put(key, val);
		}
	}

	/**
	 * Puts a non-empty list into a JSONObject as a JSONArray.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param list
	 *            The list to serialize.
	 */
	private static void putNonEmptyList(JSONObject obj, String key, List<?> list) {
		if (isNonEmpty(list)) {
			obj.put(key, new JSONArray(list));
		}
	}

	/**
	 * Puts a {@link PayloadValue} into a JSONObject if non-null.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param pv
	 *            The {@link PayloadValue}.
	 */
	private static void putPayloadValue(JSONObject obj, String key, PayloadValue pv) {
		if (pv != null) {
			obj.put(key, PayloadValue.toJsonValue(pv));
		}
	}

	/**
	 * Puts a non-empty list of {@link PayloadValue} items into a JSONObject as a
	 * JSONArray.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param list
	 *            The list of {@link PayloadValue} items.
	 */
	private static void putPayloadValueList(JSONObject obj, String key, List<PayloadValue> list) {
		if (isNonEmpty(list)) {
			JSONArray arr = new JSONArray();
			for (PayloadValue item : list) {
				arr.put(PayloadValue.toJsonValue(item));
			}
			obj.put(key, arr);
		}
	}

	/**
	 * Puts an {@link ExclusiveBound} into a JSONObject if non-null.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param eb
	 *            The {@link ExclusiveBound}.
	 */
	private static void putExclusiveBound(JSONObject obj, String key, ExclusiveBound eb) {
		if (eb != null) {
			obj.put(key, eb.isBoolean() ? eb.flag : eb.bound);
		}
	}

	/**
	 * Puts an {@link AdditionalProperties} constraint into a JSONObject if
	 * non-null.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param key
	 *            The property name.
	 * @param ap
	 *            The {@link AdditionalProperties}.
	 */
	private static void putAdditionalProperties(JSONObject obj, String key, AdditionalProperties ap) {
		if (ap != null) {
			if (ap.isBoolean()) {
				obj.put(key, ap.allowed);
			} else {
				obj.put(key, serializeRefOr(ap.schema, Emit::serializeSchema));
			}
		}
	}

	/**
	 * Serializes a {@link Reference} object to JSON.
	 *
	 * @param r
	 *            The {@link Reference}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeReference(Reference r) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "$ref", r.ref);
		putIfNotNull(obj, "summary", r.summary);
		putIfNotNull(obj, "description", r.description);
		putExtensions(obj, r.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link ReferenceOr} wrapper to a {@link JSONObject}.
	 *
	 * @param <T>
	 *            The wrapped element type.
	 * @param refOr
	 *            The {@link ReferenceOr} wrapper.
	 * @param serializer
	 *            The inline serializer function.
	 * @return The serialized {@link JSONObject}.
	 */
	private static <T> JSONObject serializeRefOr(ReferenceOr<T> refOr, Function<T, JSONObject> serializer) {
		if (refOr.isRef()) {
			return serializeReference(refOr.ref);
		}
		return serializer.apply(refOr.value);
	}

	/**
	 * Serializes a map of {@link ReferenceOr} wrappers to a {@link JSONObject}.
	 *
	 * @param <T>
	 *            The wrapped element type.
	 * @param map
	 *            The map of {@link ReferenceOr} wrappers.
	 * @param serializer
	 *            The inline serializer function.
	 * @return The serialized {@link JSONObject}.
	 */
	private static <T> JSONObject serializeRefOrMap(Map<String, ReferenceOr<T>> map,
			Function<T, JSONObject> serializer) {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, ReferenceOr<T>> e : map.entrySet()) {
			if (e.getValue() != null) {
				obj.put(e.getKey(), serializeRefOr(e.getValue(), serializer));
			}
		}
		return obj;
	}

	/**
	 * Serializes a list of {@link ReferenceOr} wrappers to a {@link JSONArray}.
	 *
	 * @param <T>
	 *            The wrapped element type.
	 * @param list
	 *            The list of {@link ReferenceOr} wrappers.
	 * @param serializer
	 *            The inline serializer function.
	 * @return The serialized {@link JSONArray}.
	 */
	private static <T> JSONArray serializeRefOrList(List<ReferenceOr<T>> list, Function<T, JSONObject> serializer) {
		JSONArray arr = new JSONArray();
		for (ReferenceOr<T> item : list) {
			arr.put(serializeRefOr(item, serializer));
		}
		return arr;
	}

	/**
	 * Copies all specification extensions from an extensions map into a target
	 * JSONObject.
	 *
	 * @param obj
	 *            The target JSONObject.
	 * @param extensions
	 *            The extensions map.
	 */
	private static void putExtensions(JSONObject obj, Map<String, PayloadValue> extensions) {
		for (Map.Entry<String, PayloadValue> e : extensions.entrySet()) {
			obj.put(e.getKey(), PayloadValue.toJsonValue(e.getValue()));
		}
	}

	/**
	 * Serializes an {@link Info} object to JSON.
	 *
	 * @param info
	 *            The {@link Info} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeInfo(Info info) {
		JSONObject infoObj = new JSONObject();
		putIfNotNull(infoObj, "title", info.title);
		putIfNotNull(infoObj, "summary", info.summary);
		putIfNotNull(infoObj, "description", info.description);
		putIfNotNull(infoObj, "termsOfService", info.termsOfService);
		putIfNotNull(infoObj, "version", info.version);
		if (info.contact != null) {
			JSONObject cObj = new JSONObject();
			putIfNotNull(cObj, "name", info.contact.name);
			putIfNotNull(cObj, "url", info.contact.url);
			putIfNotNull(cObj, "email", info.contact.email);
			putExtensions(cObj, info.contact.extensions);
			infoObj.put("contact", cObj);
		}
		if (info.license != null) {
			JSONObject lObj = new JSONObject();
			putIfNotNull(lObj, "name", info.license.name);
			putIfNotNull(lObj, "identifier", info.license.identifier);
			putIfNotNull(lObj, "url", info.license.url);
			putExtensions(lObj, info.license.extensions);
			infoObj.put("license", lObj);
		}
		putExtensions(infoObj, info.extensions);
		return infoObj;
	}

	/**
	 * Serializes an {@link ExternalDocumentation} object to JSON.
	 *
	 * @param ed
	 *            The {@link ExternalDocumentation} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeExternalDocs(ExternalDocumentation ed) {
		JSONObject edObj = new JSONObject();
		putIfNotNull(edObj, "description", ed.description);
		putIfNotNull(edObj, "url", ed.url);
		putExtensions(edObj, ed.extensions);
		return edObj;
	}

	/**
	 * Serializes a list of {@link Server} objects to a JSONArray.
	 *
	 * @param servers
	 *            The list of servers.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeServers(List<Server> servers) {
		JSONArray arr = new JSONArray();
		for (Server s : servers) {
			arr.put(serializeServer(s));
		}
		return arr;
	}

	/**
	 * Serializes a single {@link Server} to JSON.
	 *
	 * @param s
	 *            The {@link Server} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeServer(Server s) {
		JSONObject sObj = new JSONObject();
		putIfNotNull(sObj, "url", s.url);
		putIfNotNull(sObj, "description", s.description);
		putIfNotNull(sObj, "name", s.name);
		if (isNonEmpty(s.variables)) {
			JSONObject varsObj = new JSONObject();
			for (Map.Entry<String, ServerVariable> ve : s.variables.entrySet()) {
				ServerVariable sv = ve.getValue();
				JSONObject vo = new JSONObject();
				putIfNotNull(vo, "default", sv.defaultValue);
				putIfNotNull(vo, "description", sv.description);
				putNonEmptyList(vo, "enum", sv.enumValues);
				putExtensions(vo, sv.extensions);
				varsObj.put(ve.getKey(), vo);
			}
			sObj.put("variables", varsObj);
		}
		putExtensions(sObj, s.extensions);
		return sObj;
	}

	/**
	 * Serializes a list of {@link SecurityRequirement} objects to a JSONArray.
	 *
	 * @param security
	 *            The security requirements list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeSecurity(List<SecurityRequirement> security) {
		JSONArray arr = new JSONArray();
		for (SecurityRequirement sr : security) {
			JSONObject srObj = new JSONObject();
			for (Map.Entry<String, List<String>> e : sr.requirements.entrySet()) {
				srObj.put(e.getKey(), new JSONArray(e.getValue()));
			}
			arr.put(srObj);
		}
		return arr;
	}

	/**
	 * Serializes a list of {@link Tag} objects to a JSONArray.
	 *
	 * @param tags
	 *            The list of tags.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeTags(List<Tag> tags) {
		JSONArray arr = new JSONArray();
		for (Tag t : tags) {
			JSONObject tObj = new JSONObject();
			putIfNotNull(tObj, "name", t.name);
			putIfNotNull(tObj, "summary", t.summary);
			putIfNotNull(tObj, "description", t.description);
			putIfNotNull(tObj, "parent", t.parent);
			putIfNotNull(tObj, "kind", t.kind);
			if (t.externalDocs != null) {
				tObj.put("externalDocs", serializeExternalDocs(t.externalDocs));
			}
			putExtensions(tObj, t.extensions);
			arr.put(tObj);
		}
		return arr;
	}

	/**
	 * Serializes a {@link Paths} container to JSON.
	 *
	 * @param paths
	 *            The {@link Paths} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializePaths(Paths paths) {
		JSONObject pathsObj = new JSONObject();
		if (paths.pathItems != null) {
			for (Map.Entry<String, PathItem> e : paths.pathItems.entrySet()) {
				pathsObj.put(e.getKey(), serializePathItem(e.getValue()));
			}
		}
		putExtensions(pathsObj, paths.extensions);
		return pathsObj;
	}

	/**
	 * Serializes a {@link PathItem} to JSON.
	 *
	 * @param pi
	 *            The {@link PathItem} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializePathItem(PathItem pi) {
		JSONObject piObj = new JSONObject();
		putIfNotNull(piObj, "$ref", pi.ref);
		putIfNotNull(piObj, "summary", pi.summary);
		putIfNotNull(piObj, "description", pi.description);
		if (pi.get != null)
			piObj.put("get", serializeOperation(pi.get));
		if (pi.put != null)
			piObj.put("put", serializeOperation(pi.put));
		if (pi.post != null)
			piObj.put("post", serializeOperation(pi.post));
		if (pi.delete != null)
			piObj.put("delete", serializeOperation(pi.delete));
		if (pi.options != null)
			piObj.put("options", serializeOperation(pi.options));
		if (pi.head != null)
			piObj.put("head", serializeOperation(pi.head));
		if (pi.patch != null)
			piObj.put("patch", serializeOperation(pi.patch));
		if (pi.trace != null)
			piObj.put("trace", serializeOperation(pi.trace));
		if (pi.query != null)
			piObj.put("query", serializeOperation(pi.query));
		if (isNonEmpty(pi.additionalOperations)) {
			JSONObject addOps = new JSONObject();
			for (Map.Entry<String, Operation> e : pi.additionalOperations.entrySet()) {
				addOps.put(e.getKey(), serializeOperation(e.getValue()));
			}
			piObj.put("additionalOperations", addOps);
		}
		if (isNonEmpty(pi.servers)) {
			piObj.put("servers", serializeServers(pi.servers));
		}
		if (isNonEmpty(pi.parameters)) {
			piObj.put("parameters", serializeRefOrList(pi.parameters, Emit::serializeParameter));
		}
		putExtensions(piObj, pi.extensions);
		return piObj;
	}

	/**
	 * Serializes an OpenAPI {@link Operation} into its JSON representation.
	 *
	 * @param op
	 *            The {@link Operation} to serialize.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeOperation(Operation op) {
		JSONObject obj = new JSONObject();
		putNonEmptyList(obj, "tags", op.tags);
		putIfNotNull(obj, "summary", op.summary);
		putIfNotNull(obj, "description", op.description);
		putIfNotNull(obj, "operationId", op.operationId);
		putNonEmptyList(obj, "consumes", op.consumes);
		putNonEmptyList(obj, "produces", op.produces);
		putNonEmptyList(obj, "schemes", op.schemes);
		putIfNotNull(obj, "deprecated", op.deprecated);
		if (op.externalDocs != null) {
			obj.put("externalDocs", serializeExternalDocs(op.externalDocs));
		}
		if (isNonEmpty(op.parameters)) {
			obj.put("parameters", serializeRefOrList(op.parameters, Emit::serializeParameter));
		}
		if (op.requestBody != null) {
			obj.put("requestBody", serializeRefOr(op.requestBody, Emit::serializeRequestBody));
		}
		if (op.responses != null) {
			JSONObject respObj = serializeResponses(op.responses);
			if (!respObj.isEmpty()) {
				obj.put("responses", respObj);
			}
		}
		if (isNonEmpty(op.callbacks)) {
			obj.put("callbacks", serializeRefOrMap(op.callbacks, Emit::serializeCallback));
		}
		if (isNonEmpty(op.security)) {
			obj.put("security", serializeSecurity(op.security));
		}
		if (isNonEmpty(op.servers)) {
			obj.put("servers", serializeServers(op.servers));
		}
		putExtensions(obj, op.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link RequestBody} to JSON.
	 *
	 * @param rb
	 *            The {@link RequestBody}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeRequestBody(RequestBody rb) {
		JSONObject rbObj = new JSONObject();
		putIfNotNull(rbObj, "description", rb.description);
		putIfNotNull(rbObj, "required", rb.required);
		if (rb.content != null) {
			rbObj.put("content", serializeMediaTypeMap(rb.content));
		}
		putExtensions(rbObj, rb.extensions);
		return rbObj;
	}

	/**
	 * Serializes a map of {@link MediaType} objects to JSON.
	 *
	 * @param content
	 *            The media type map.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeMediaTypeMap(Map<String, MediaType> content) {
		JSONObject contentObj = new JSONObject();
		for (Map.Entry<String, MediaType> me : content.entrySet()) {
			contentObj.put(me.getKey(), serializeMediaType(me.getValue()));
		}
		return contentObj;
	}

	/**
	 * Serializes a single {@link MediaType} to JSON.
	 *
	 * @param mt
	 *            The {@link MediaType}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeMediaType(MediaType mt) {
		JSONObject mtObj = new JSONObject();
		if (mt.schema != null) {
			mtObj.put("schema", serializeRefOr(mt.schema, Emit::serializeSchema));
		}
		if (mt.itemSchema != null) {
			mtObj.put("itemSchema", serializeRefOr(mt.itemSchema, Emit::serializeSchema));
		}
		putPayloadValue(mtObj, "example", mt.example);
		if (isNonEmpty(mt.examples)) {
			mtObj.put("examples", serializeRefOrMap(mt.examples, Emit::serializeExample));
		}
		if (isNonEmpty(mt.encoding)) {
			mtObj.put("encoding", serializeEncodingMap(mt.encoding));
		}
		if (isNonEmpty(mt.prefixEncoding)) {
			mtObj.put("prefixEncoding", serializeEncodingList(mt.prefixEncoding));
		}
		if (mt.itemEncoding != null) {
			mtObj.put("itemEncoding", serializeEncoding(mt.itemEncoding));
		}
		putExtensions(mtObj, mt.extensions);
		return mtObj;
	}

	/**
	 * Serializes a map of {@link Encoding} objects to JSON.
	 *
	 * @param map
	 *            The encoding map.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeEncodingMap(Map<String, Encoding> map) {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, Encoding> e : map.entrySet()) {
			obj.put(e.getKey(), serializeEncoding(e.getValue()));
		}
		return obj;
	}

	/**
	 * Serializes a list of {@link Encoding} objects to a JSONArray.
	 *
	 * @param list
	 *            The encoding list.
	 * @return The serialized {@link JSONArray}.
	 */
	private static JSONArray serializeEncodingList(List<Encoding> list) {
		JSONArray arr = new JSONArray();
		for (Encoding enc : list) {
			arr.put(serializeEncoding(enc));
		}
		return arr;
	}

	/**
	 * Serializes a single {@link Encoding} object to JSON.
	 *
	 * @param enc
	 *            The {@link Encoding}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeEncoding(Encoding enc) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "contentType", enc.contentType);
		putIfNotNull(obj, "style", enc.style);
		putIfNotNull(obj, "explode", enc.explode);
		putIfNotNull(obj, "allowReserved", enc.allowReserved);
		if (isNonEmpty(enc.headers)) {
			obj.put("headers", serializeRefOrMap(enc.headers, Emit::serializeHeader));
		}
		if (isNonEmpty(enc.encoding)) {
			obj.put("encoding", serializeEncodingMap(enc.encoding));
		}
		if (isNonEmpty(enc.prefixEncoding)) {
			obj.put("prefixEncoding", serializeEncodingList(enc.prefixEncoding));
		}
		if (enc.itemEncoding != null) {
			obj.put("itemEncoding", serializeEncoding(enc.itemEncoding));
		}
		putExtensions(obj, enc.extensions);
		return obj;
	}

	/**
	 * Serializes a {@link Responses} container to JSON.
	 *
	 * @param responses
	 *            The {@link Responses} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeResponses(Responses responses) {
		JSONObject responsesObj = new JSONObject();
		if (responses.defaultResponse != null) {
			responsesObj.put("default", serializeRefOr(responses.defaultResponse, Emit::serializeResponse));
		}
		if (responses.statusCodes != null) {
			for (Map.Entry<String, ReferenceOr<Response>> re : responses.statusCodes.entrySet()) {
				if (re.getValue() != null) {
					responsesObj.put(re.getKey(), serializeRefOr(re.getValue(), Emit::serializeResponse));
				}
			}
		}
		putExtensions(responsesObj, responses.extensions);
		return responsesObj;
	}

	/**
	 * Serializes a single {@link Response} to JSON.
	 *
	 * @param r
	 *            The {@link Response}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeResponse(Response r) {
		JSONObject rObj = new JSONObject();
		putIfNotNull(rObj, "summary", r.summary);
		putIfNotNull(rObj, "description", r.description);
		if (r.schema != null) {
			rObj.put("schema", serializeRefOr(r.schema, Emit::serializeSchema));
		}
		if (isNonEmpty(r.headers)) {
			rObj.put("headers", serializeRefOrMap(r.headers, Emit::serializeHeader));
		}
		if (isNonEmpty(r.examples)) {
			rObj.put("examples", serializeRefOrMap(r.examples, Emit::serializeExample));
		}
		if (r.content != null) {
			rObj.put("content", serializeMediaTypeMap(r.content));
		}
		if (isNonEmpty(r.links)) {
			rObj.put("links", serializeRefOrMap(r.links, Emit::serializeLink));
		}
		putExtensions(rObj, r.extensions);
		return rObj;
	}

	/**
	 * Serializes a single {@link Parameter} to JSON.
	 *
	 * @param p
	 *            The {@link Parameter}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeParameter(Parameter p) {
		JSONObject pObj = new JSONObject();
		putIfNotNull(pObj, "name", p.name);
		putIfNotNull(pObj, "in", p.in);
		putIfNotNull(pObj, "description", p.description);
		putIfNotNull(pObj, "required", p.required);
		putIfNotNull(pObj, "deprecated", p.deprecated);
		putIfNotNull(pObj, "allowEmptyValue", p.allowEmptyValue);
		putIfNotNull(pObj, "style", p.style);
		putIfNotNull(pObj, "explode", p.explode);
		putIfNotNull(pObj, "allowReserved", p.allowReserved);
		putIfNotNull(pObj, "type", p.type);
		putIfNotNull(pObj, "format", p.format);
		if (p.items != null) {
			pObj.put("items", serializeItems(p.items));
		}
		putIfNotNull(pObj, "collectionFormat", p.collectionFormat);
		putPayloadValue(pObj, "default", p.defaultValue);
		putIfNotNull(pObj, "maximum", p.maximum);
		putIfNotNull(pObj, "exclusiveMaximum", p.exclusiveMaximum);
		putIfNotNull(pObj, "minimum", p.minimum);
		putIfNotNull(pObj, "exclusiveMinimum", p.exclusiveMinimum);
		putIfNotNull(pObj, "maxLength", p.maxLength);
		putIfNotNull(pObj, "minLength", p.minLength);
		putIfNotNull(pObj, "pattern", p.pattern);
		putIfNotNull(pObj, "maxItems", p.maxItems);
		putIfNotNull(pObj, "minItems", p.minItems);
		putIfNotNull(pObj, "uniqueItems", p.uniqueItems);
		putPayloadValueList(pObj, "enum", p.enumValues);
		putIfNotNull(pObj, "multipleOf", p.multipleOf);
		if (p.schema != null) {
			pObj.put("schema", serializeRefOr(p.schema, Emit::serializeSchema));
		}
		putPayloadValue(pObj, "example", p.example);
		if (isNonEmpty(p.examples)) {
			pObj.put("examples", serializeRefOrMap(p.examples, Emit::serializeExample));
		}
		if (isNonEmpty(p.content)) {
			pObj.put("content", serializeMediaTypeMap(p.content));
		}
		putExtensions(pObj, p.extensions);
		return pObj;
	}

	/**
	 * Serializes a single {@link Header} to JSON.
	 *
	 * @param h
	 *            The {@link Header}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeHeader(Header h) {
		JSONObject hObj = new JSONObject();
		putIfNotNull(hObj, "description", h.description);
		putIfNotNull(hObj, "required", h.required);
		putIfNotNull(hObj, "deprecated", h.deprecated);
		putIfNotNull(hObj, "allowEmptyValue", h.allowEmptyValue);
		putIfNotNull(hObj, "style", h.style);
		putIfNotNull(hObj, "explode", h.explode);
		putIfNotNull(hObj, "allowReserved", h.allowReserved);
		putIfNotNull(hObj, "type", h.type);
		putIfNotNull(hObj, "format", h.format);
		if (h.items != null) {
			hObj.put("items", serializeItems(h.items));
		}
		putIfNotNull(hObj, "collectionFormat", h.collectionFormat);
		putPayloadValue(hObj, "default", h.defaultValue);
		putIfNotNull(hObj, "maximum", h.maximum);
		putIfNotNull(hObj, "exclusiveMaximum", h.exclusiveMaximum);
		putIfNotNull(hObj, "minimum", h.minimum);
		putIfNotNull(hObj, "exclusiveMinimum", h.exclusiveMinimum);
		putIfNotNull(hObj, "maxLength", h.maxLength);
		putIfNotNull(hObj, "minLength", h.minLength);
		putIfNotNull(hObj, "pattern", h.pattern);
		putIfNotNull(hObj, "maxItems", h.maxItems);
		putIfNotNull(hObj, "minItems", h.minItems);
		putIfNotNull(hObj, "uniqueItems", h.uniqueItems);
		putPayloadValueList(hObj, "enum", h.enumValues);
		putIfNotNull(hObj, "multipleOf", h.multipleOf);
		if (h.schema != null) {
			hObj.put("schema", serializeRefOr(h.schema, Emit::serializeSchema));
		}
		putPayloadValue(hObj, "example", h.example);
		if (isNonEmpty(h.examples)) {
			hObj.put("examples", serializeRefOrMap(h.examples, Emit::serializeExample));
		}
		if (isNonEmpty(h.content)) {
			hObj.put("content", serializeMediaTypeMap(h.content));
		}
		putExtensions(hObj, h.extensions);
		return hObj;
	}

	/**
	 * Serializes a Swagger 2.0 {@link Items} object to JSON.
	 *
	 * @param items
	 *            The {@link Items} object.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeItems(Items items) {
		JSONObject iObj = new JSONObject();
		putIfNotNull(iObj, "type", items.type);
		putIfNotNull(iObj, "format", items.format);
		if (items.items != null) {
			iObj.put("items", serializeItems(items.items));
		}
		putIfNotNull(iObj, "collectionFormat", items.collectionFormat);
		putPayloadValue(iObj, "default", items.defaultValue);
		putIfNotNull(iObj, "maximum", items.maximum);
		putIfNotNull(iObj, "exclusiveMaximum", items.exclusiveMaximum);
		putIfNotNull(iObj, "minimum", items.minimum);
		putIfNotNull(iObj, "exclusiveMinimum", items.exclusiveMinimum);
		putIfNotNull(iObj, "maxLength", items.maxLength);
		putIfNotNull(iObj, "minLength", items.minLength);
		putIfNotNull(iObj, "pattern", items.pattern);
		putIfNotNull(iObj, "maxItems", items.maxItems);
		putIfNotNull(iObj, "minItems", items.minItems);
		putIfNotNull(iObj, "uniqueItems", items.uniqueItems);
		putPayloadValueList(iObj, "enum", items.enumValues);
		putIfNotNull(iObj, "multipleOf", items.multipleOf);
		putExtensions(iObj, items.extensions);
		return iObj;
	}

	/**
	 * Serializes an {@link Example} to JSON.
	 *
	 * @param ex
	 *            The {@link Example}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeExample(Example ex) {
		JSONObject exObj = new JSONObject();
		putIfNotNull(exObj, "summary", ex.summary);
		putIfNotNull(exObj, "description", ex.description);
		putPayloadValue(exObj, "value", ex.value);
		putIfNotNull(exObj, "externalValue", ex.externalValue);
		putPayloadValue(exObj, "dataValue", ex.dataValue);
		putIfNotNull(exObj, "serializedValue", ex.serializedValue);
		putExtensions(exObj, ex.extensions);
		return exObj;
	}

	/**
	 * Serializes a {@link Link} to JSON.
	 *
	 * @param l
	 *            The {@link Link}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeLink(Link l) {
		JSONObject lObj = new JSONObject();
		putIfNotNull(lObj, "operationRef", l.operationRef);
		putIfNotNull(lObj, "operationId", l.operationId);
		putIfNotNull(lObj, "description", l.description);
		putPayloadValue(lObj, "requestBody", l.requestBody);
		if (isNonEmpty(l.parameters)) {
			JSONObject pMapObj = new JSONObject();
			for (Map.Entry<String, PayloadValue> e : l.parameters.entrySet()) {
				pMapObj.put(e.getKey(), PayloadValue.toJsonValue(e.getValue()));
			}
			lObj.put("parameters", pMapObj);
		}
		if (l.server != null) {
			lObj.put("server", serializeServer(l.server));
		}
		putExtensions(lObj, l.extensions);
		return lObj;
	}

	/**
	 * Serializes a {@link Callback} to JSON.
	 *
	 * @param cb
	 *            The {@link Callback}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeCallback(Callback cb) {
		JSONObject cbObj = new JSONObject();
		for (Map.Entry<String, PathItem> e : cb.pathItems.entrySet()) {
			cbObj.put(e.getKey(), serializePathItem(e.getValue()));
		}
		putExtensions(cbObj, cb.extensions);
		return cbObj;
	}

	/**
	 * Serializes a {@link SecurityScheme} to JSON.
	 *
	 * @param sc
	 *            The {@link SecurityScheme}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeSecurityScheme(SecurityScheme sc) {
		JSONObject scObj = new JSONObject();
		putIfNotNull(scObj, "type", sc.type);
		putIfNotNull(scObj, "description", sc.description);
		putIfNotNull(scObj, "name", sc.name);
		putIfNotNull(scObj, "in", sc.in);
		putIfNotNull(scObj, "scheme", sc.scheme);
		putIfNotNull(scObj, "bearerFormat", sc.bearerFormat);
		putIfNotNull(scObj, "flow", sc.flow);
		putIfNotNull(scObj, "authorizationUrl", sc.authorizationUrl);
		putIfNotNull(scObj, "tokenUrl", sc.tokenUrl);
		putIfNotNull(scObj, "openIdConnectUrl", sc.openIdConnectUrl);
		putIfNotNull(scObj, "oauth2MetadataUrl", sc.oauth2MetadataUrl);
		putIfNotNull(scObj, "deprecated", sc.deprecated);
		if (isNonEmpty(sc.scopes)) {
			scObj.put("scopes", new JSONObject(sc.scopes));
		}
		if (sc.flows != null) {
			JSONObject fObj = new JSONObject();
			if (sc.flows.implicit != null)
				fObj.put("implicit", serializeOAuthFlow(sc.flows.implicit));
			if (sc.flows.password != null)
				fObj.put("password", serializeOAuthFlow(sc.flows.password));
			if (sc.flows.clientCredentials != null)
				fObj.put("clientCredentials", serializeOAuthFlow(sc.flows.clientCredentials));
			if (sc.flows.authorizationCode != null)
				fObj.put("authorizationCode", serializeOAuthFlow(sc.flows.authorizationCode));
			if (sc.flows.deviceAuthorization != null)
				fObj.put("deviceAuthorization", serializeOAuthFlow(sc.flows.deviceAuthorization));
			putExtensions(fObj, sc.flows.extensions);
			scObj.put("flows", fObj);
		}
		putExtensions(scObj, sc.extensions);
		return scObj;
	}

	/**
	 * Serializes an {@link OAuthFlow} to JSON.
	 *
	 * @param f
	 *            The {@link OAuthFlow}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeOAuthFlow(OAuthFlow f) {
		JSONObject fObj = new JSONObject();
		putIfNotNull(fObj, "authorizationUrl", f.authorizationUrl);
		putIfNotNull(fObj, "tokenUrl", f.tokenUrl);
		putIfNotNull(fObj, "refreshUrl", f.refreshUrl);
		putIfNotNull(fObj, "deviceAuthorizationUrl", f.deviceAuthorizationUrl);
		if (f.scopes != null) {
			fObj.put("scopes", new JSONObject(f.scopes));
		}
		putExtensions(fObj, f.extensions);
		return fObj;
	}

	/**
	 * Serializes a {@link Components} container to JSON.
	 *
	 * @param comp
	 *            The {@link Components}.
	 * @return The serialized {@link JSONObject}.
	 */
	private static JSONObject serializeComponents(Components comp) {
		JSONObject compObj = new JSONObject();
		if (comp.schemas != null) {
			compObj.put("schemas", serializeRefOrMap(comp.schemas, Emit::serializeSchema));
		}
		if (isNonEmpty(comp.responses)) {
			compObj.put("responses", serializeRefOrMap(comp.responses, Emit::serializeResponse));
		}
		if (isNonEmpty(comp.parameters)) {
			compObj.put("parameters", serializeRefOrMap(comp.parameters, Emit::serializeParameter));
		}
		if (isNonEmpty(comp.examples)) {
			compObj.put("examples", serializeRefOrMap(comp.examples, Emit::serializeExample));
		}
		if (isNonEmpty(comp.requestBodies)) {
			compObj.put("requestBodies", serializeRefOrMap(comp.requestBodies, Emit::serializeRequestBody));
		}
		if (isNonEmpty(comp.headers)) {
			compObj.put("headers", serializeRefOrMap(comp.headers, Emit::serializeHeader));
		}
		if (isNonEmpty(comp.securitySchemes)) {
			compObj.put("securitySchemes", serializeRefOrMap(comp.securitySchemes, Emit::serializeSecurityScheme));
		}
		if (isNonEmpty(comp.links)) {
			compObj.put("links", serializeRefOrMap(comp.links, Emit::serializeLink));
		}
		if (isNonEmpty(comp.callbacks)) {
			compObj.put("callbacks", serializeRefOrMap(comp.callbacks, Emit::serializeCallback));
		}
		if (isNonEmpty(comp.pathItems)) {
			compObj.put("pathItems", serializeRefOrMap(comp.pathItems, Emit::serializePathItem));
		}
		if (isNonEmpty(comp.mediaTypes)) {
			compObj.put("mediaTypes", serializeRefOrMap(comp.mediaTypes, Emit::serializeMediaType));
		}
		putExtensions(compObj, comp.extensions);
		return compObj;
	}

	/**
	 * Serializes an OpenAPI {@link Schema} into its JSON representation.
	 *
	 * @param s
	 *            The {@link Schema} to serialize.
	 * @return The serialized {@link JSONObject}.
	 */
	public static JSONObject serializeSchema(Schema s) {
		JSONObject obj = new JSONObject();
		putIfNotNull(obj, "$ref", s.$ref);
		if (s.type != null) {
			if (s.type.isMultiType()) {
				obj.put("type", new JSONArray(s.type.types));
			} else {
				putIfNotNull(obj, "type", s.type.single);
			}
		}
		putIfNotNull(obj, "title", s.title);
		putIfNotNull(obj, "format", s.format);
		putIfNotNull(obj, "description", s.description);
		putIfNotNull(obj, "multipleOf", s.multipleOf);
		putIfNotNull(obj, "maximum", s.maximum);
		putExclusiveBound(obj, "exclusiveMaximum", s.exclusiveMaximum);
		putIfNotNull(obj, "minimum", s.minimum);
		putExclusiveBound(obj, "exclusiveMinimum", s.exclusiveMinimum);
		putIfNotNull(obj, "maxLength", s.maxLength);
		putIfNotNull(obj, "minLength", s.minLength);
		putIfNotNull(obj, "pattern", s.pattern);
		putIfNotNull(obj, "maxItems", s.maxItems);
		putIfNotNull(obj, "minItems", s.minItems);
		putIfNotNull(obj, "uniqueItems", s.uniqueItems);
		putIfNotNull(obj, "maxProperties", s.maxProperties);
		putIfNotNull(obj, "minProperties", s.minProperties);
		putNonEmptyList(obj, "required", s.required);
		putPayloadValueList(obj, "enum", s.enumValues);
		if (isNonEmpty(s.allOf))
			obj.put("allOf", serializeRefOrList(s.allOf, Emit::serializeSchema));
		if (isNonEmpty(s.anyOf))
			obj.put("anyOf", serializeRefOrList(s.anyOf, Emit::serializeSchema));
		if (isNonEmpty(s.oneOf))
			obj.put("oneOf", serializeRefOrList(s.oneOf, Emit::serializeSchema));
		if (s.not != null)
			obj.put("not", serializeRefOr(s.not, Emit::serializeSchema));
		if (s.items != null) {
			obj.put("items", serializeRefOr(s.items, Emit::serializeSchema));
		}
		if (s.properties != null) {
			obj.put("properties", serializeRefOrMap(s.properties, Emit::serializeSchema));
		}
		putAdditionalProperties(obj, "additionalProperties", s.additionalProperties);
		putPayloadValue(obj, "default", s.defaultValue);
		putPayloadValue(obj, "const", s.constValue);
		putIfNotNull(obj, "readOnly", s.readOnly);
		putIfNotNull(obj, "writeOnly", s.writeOnly);
		putIfNotNull(obj, "deprecated", s.deprecated);
		putPayloadValue(obj, "example", s.example);
		putPayloadValueList(obj, "examples", s.examples);
		if (s.externalDocs != null) {
			obj.put("externalDocs", serializeExternalDocs(s.externalDocs));
		}
		if (s.xml != null) {
			JSONObject xObj = new JSONObject();
			putIfNotNull(xObj, "name", s.xml.name);
			putIfNotNull(xObj, "namespace", s.xml.namespace);
			putIfNotNull(xObj, "prefix", s.xml.prefix);
			putIfNotNull(xObj, "attribute", s.xml.attribute);
			putIfNotNull(xObj, "wrapped", s.xml.wrapped);
			putIfNotNull(xObj, "nodeType", s.xml.nodeType);
			putExtensions(xObj, s.xml.extensions);
			obj.put("xml", xObj);
		}
		if (s.discriminator != null) {
			JSONObject dObj = new JSONObject();
			putIfNotNull(dObj, "propertyName", s.discriminator.propertyName);
			putIfNotNull(dObj, "defaultMapping", s.discriminator.defaultMapping);
			if (isNonEmpty(s.discriminator.mapping)) {
				dObj.put("mapping", new JSONObject(s.discriminator.mapping));
			}
			putExtensions(dObj, s.discriminator.extensions);
			obj.put("discriminator", dObj);
		}
		putIfNotNull(obj, "$id", s.id);
		putIfNotNull(obj, "$schema", s.schema);
		putIfNotNull(obj, "$anchor", s.anchor);
		putIfNotNull(obj, "$dynamicAnchor", s.dynamicAnchor);
		if (isNonEmpty(s.vocabulary)) {
			obj.put("$vocabulary", new JSONObject(s.vocabulary));
		}
		if (isNonEmpty(s.defs)) {
			obj.put("$defs", serializeRefOrMap(s.defs, Emit::serializeSchema));
		}
		if (isNonEmpty(s.dependentRequired)) {
			JSONObject drObj = new JSONObject();
			for (Map.Entry<String, List<String>> e : s.dependentRequired.entrySet()) {
				drObj.put(e.getKey(), new JSONArray(e.getValue()));
			}
			obj.put("dependentRequired", drObj);
		}
		if (isNonEmpty(s.dependentSchemas)) {
			obj.put("dependentSchemas", serializeRefOrMap(s.dependentSchemas, Emit::serializeSchema));
		}
		if (s.propertyNames != null) {
			obj.put("propertyNames", serializeRefOr(s.propertyNames, Emit::serializeSchema));
		}
		if (isNonEmpty(s.patternProperties)) {
			obj.put("patternProperties", serializeRefOrMap(s.patternProperties, Emit::serializeSchema));
		}
		putAdditionalProperties(obj, "unevaluatedProperties", s.unevaluatedProperties);
		putAdditionalProperties(obj, "unevaluatedItems", s.unevaluatedItems);
		if (isNonEmpty(s.prefixItems)) {
			obj.put("prefixItems", serializeRefOrList(s.prefixItems, Emit::serializeSchema));
		}
		if (s.contains != null) {
			obj.put("contains", serializeRefOr(s.contains, Emit::serializeSchema));
		}
		putIfNotNull(obj, "minContains", s.minContains);
		putIfNotNull(obj, "maxContains", s.maxContains);
		putExtensions(obj, s.extensions);
		return obj;
	}

	/**
	 * Emits OpenAPI description to file.
	 *
	 * @param api
	 *            The OpenAPI object.
	 * @param file
	 *            The file.
	 * @throws IOException
	 *             If emitting fails.
	 */
	public static void toFile(OpenAPI api, File file) throws IOException {
		String data = toString(api);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data.getBytes(StandardCharsets.UTF_8));
		}
	}
}
