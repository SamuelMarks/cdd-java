package openapi;

import common.PayloadValue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses OpenAPI 3.2.0 and Swagger 2.0 descriptions into the {@link OpenAPI}
 * AST.
 */
public class Parse {

	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parse OpenAPI description from string.
	 *
	 * @param content
	 *            The JSON or YAML string.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromString(String content) throws IOException {
		return fromString(content, new File(".").toURI().toString());
	}

	/**
	 * Parse OpenAPI description from string with a base URI for resolving
	 * references.
	 *
	 * @param content
	 *            The JSON/YAML string.
	 * @param baseUri
	 *            The base URI.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromString(String content, String baseUri) throws IOException {
		try {
			String trimmed = content.trim();
			JSONObject root;
			if (trimmed.startsWith("{")) {
				root = new JSONObject(trimmed);
			} else {
				org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
				Map<String, Object> map = yaml.load(content);
				root = new JSONObject(map);
			}
			RefResolver resolver = new RefResolver();
			resolver.bundle(root, baseUri);
			return fromJson(root);
		} catch (Exception e) {
			throw new IOException("Failed to parse OpenAPI: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse OpenAPI description from JSONObject.
	 *
	 * @param root
	 *            The root JSONObject.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromJson(JSONObject root) throws IOException {
		try {
			OpenAPI api = new OpenAPI();
			api.openapi = optStr(root, "openapi");
			api.swagger = optStr(root, "swagger");
			api.$self = optStr(root, "$self");
			api.jsonSchemaDialect = optStr(root, "jsonSchemaDialect");
			api.host = optStr(root, "host");
			api.basePath = optStr(root, "basePath");
			api.schemes = optStrList(root, "schemes");
			api.consumes = optStrList(root, "consumes");
			api.produces = optStrList(root, "produces");
			api.servers = parseServers(root);
			if (root.has("info")) {
				api.info = parseInfo(root.getJSONObject("info"));
			}
			if (root.has("externalDocs")) {
				api.externalDocs = parseExternalDocs(root.getJSONObject("externalDocs"));
			}
			if (root.has("security")) {
				api.security = parseSecurityRequirements(root.getJSONArray("security"));
			}
			if (root.has("tags")) {
				api.tags = parseTags(root.getJSONArray("tags"));
			}
			if (root.has("paths")) {
				api.paths = parsePaths(root.getJSONObject("paths"));
			}
			if (root.has("webhooks")) {
				api.webhooks = parsePathItemMap(root.getJSONObject("webhooks"));
			}
			if (root.has("components")) {
				api.components = parseComponents(root.getJSONObject("components"));
			}
			if (root.has("definitions")) {
				api.definitions = parseSchemaMap(root.getJSONObject("definitions"));
				if (api.components == null) {
					api.components = new Components();
					api.components.schemas = new HashMap<>();
				} else if (api.components.schemas == null) {
					api.components.schemas = new HashMap<>();
				}
				for (Map.Entry<String, Schema> defEntry : api.definitions.entrySet()) {
					api.components.schemas.put(defEntry.getKey(), parseSchemaFromRefOrValue(defEntry.getValue()));
				}
			}
			if (root.has("parameters")) {
				api.parameters = parseTypedParameterMap(root.getJSONObject("parameters"));
			}
			if (root.has("responses")) {
				api.responses = parseTypedResponseMap(root.getJSONObject("responses"));
			}
			if (root.has("securityDefinitions")) {
				api.securityDefinitions = parseTypedSecuritySchemeMap(root.getJSONObject("securityDefinitions"));
			}
			copyExtensions(root, api.extensions);
			return api;
		} catch (Exception e) {
			throw new IOException("Failed to parse OpenAPI: " + e.getMessage(), e);
		}
	}

	/**
	 * Converts a parsed {@link Schema} into a {@link ReferenceOr} wrapper (wrapping
	 * as a {@link Reference} if only {@code $ref} is populated, or as an inline
	 * {@link Schema} value otherwise).
	 *
	 * @param s
	 *            The parsed {@link Schema}.
	 * @return The corresponding {@link ReferenceOr} wrapper.
	 */
	private static ReferenceOr<Schema> parseSchemaFromRefOrValue(Schema s) {
		if (s.$ref != null) {
			Reference r = new Reference();
			r.ref = s.$ref;
			r.summary = s.title;
			r.description = s.description;
			return ReferenceOr.ofRef(r);
		}
		return ReferenceOr.ofValue(s);
	}

	/**
	 * Parses a {@link Reference} object from JSON.
	 *
	 * @param obj
	 *            The JSON object containing {@code $ref}.
	 * @return The parsed {@link Reference}.
	 */
	private static Reference parseReference(JSONObject obj) {
		Reference r = new Reference();
		r.ref = optStr(obj, "$ref");
		r.summary = optStr(obj, "summary");
		r.description = optStr(obj, "description");
		copyExtensions(obj, r.extensions);
		return r;
	}

	/**
	 * Parses a {@link JSONObject} into a {@link ReferenceOr} wrapper using the
	 * provided inline parser when {@code $ref} is not present.
	 *
	 * @param <T>
	 *            The target OpenAPI element type.
	 * @param obj
	 *            The JSON object to parse.
	 * @param parser
	 *            The inline parser function.
	 * @return The parsed {@link ReferenceOr} wrapper.
	 */
	private static <T> ReferenceOr<T> parseRefOr(JSONObject obj, Function<JSONObject, T> parser) {
		if (!obj.isNull("$ref")) {
			return ReferenceOr.ofRef(parseReference(obj));
		}
		return ReferenceOr.ofValue(parser.apply(obj));
	}

	/**
	 * Parses a map of {@link ReferenceOr} elements from a {@link JSONObject}.
	 *
	 * @param <T>
	 *            The target OpenAPI element type.
	 * @param mapObj
	 *            The JSON object mapping keys to element definitions or references.
	 * @param parser
	 *            The inline parser function.
	 * @return The parsed map of {@link ReferenceOr} wrappers.
	 */
	private static <T> Map<String, ReferenceOr<T>> parseRefOrMap(JSONObject mapObj, Function<JSONObject, T> parser) {
		Map<String, ReferenceOr<T>> map = new HashMap<>();
		for (String k : mapObj.keySet()) {
			map.put(k, parseRefOr(mapObj.getJSONObject(k), parser));
		}
		return map;
	}

	/**
	 * Parses a list of {@link ReferenceOr} elements from a {@link JSONArray}.
	 *
	 * @param <T>
	 *            The target OpenAPI element type.
	 * @param arr
	 *            The JSON array containing element definitions or references.
	 * @param parser
	 *            The inline parser function.
	 * @return The parsed list of {@link ReferenceOr} wrappers.
	 */
	private static <T> List<ReferenceOr<T>> parseRefOrList(JSONArray arr, Function<JSONObject, T> parser) {
		List<ReferenceOr<T>> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(parseRefOr(arr.getJSONObject(i), parser));
		}
		return list;
	}

	/**
	 * Reads a string property if present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The string value or {@code null}.
	 */
	private static String optStr(JSONObject obj, String key) {
		return !obj.isNull(key) ? obj.getString(key) : null;
	}

	/**
	 * Reads a boolean property if present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The boolean value or {@code null}.
	 */
	private static Boolean optBool(JSONObject obj, String key) {
		return !obj.isNull(key) ? obj.getBoolean(key) : null;
	}

	/**
	 * Reads an integer property if present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The integer value or {@code null}.
	 */
	private static Integer optInt(JSONObject obj, String key) {
		return !obj.isNull(key) ? obj.getInt(key) : null;
	}

	/**
	 * Reads a number property if present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The number value or {@code null}.
	 */
	private static Number optNum(JSONObject obj, String key) {
		return !obj.isNull(key) ? obj.getNumber(key) : null;
	}

	/**
	 * Reads a {@link PayloadValue} property if present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The parsed {@link PayloadValue} or {@code null}.
	 */
	private static PayloadValue optPayloadVal(JSONObject obj, String key) {
		return !obj.isNull(key) ? PayloadValue.fromJsonValue(obj.get(key)) : null;
	}

	/**
	 * Reads a list of {@link PayloadValue} items from a JSONObject property if
	 * present.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The parsed list of {@link PayloadValue} items or {@code null}.
	 */
	private static List<PayloadValue> optPayloadValList(JSONObject obj, String key) {
		if (obj.isNull(key)) {
			return null;
		}
		JSONArray arr = obj.getJSONArray(key);
		List<PayloadValue> res = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			res.add(PayloadValue.fromJsonValue(arr.get(i)));
		}
		return res;
	}

	/**
	 * Reads an {@link ExclusiveBound} property (boolean or number) if present and
	 * non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The parsed {@link ExclusiveBound} or {@code null}.
	 */
	private static ExclusiveBound optExclusiveBound(JSONObject obj, String key) {
		if (obj.isNull(key)) {
			return null;
		}
		Object raw = obj.get(key);
		if (raw instanceof Boolean) {
			return ExclusiveBound.ofBoolean((Boolean) raw);
		}
		return ExclusiveBound.ofNumber(obj.getNumber(key));
	}

	/**
	 * Reads an {@link AdditionalProperties} constraint (boolean or schema/ref) if
	 * present and non-null.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The parsed {@link AdditionalProperties} or {@code null}.
	 */
	private static AdditionalProperties optAdditionalProperties(JSONObject obj, String key) {
		if (obj.isNull(key)) {
			return null;
		}
		Object raw = obj.get(key);
		if (raw instanceof JSONObject) {
			return AdditionalProperties.ofRefOrSchema(parseRefOr((JSONObject) raw, Parse::parseSchema));
		}
		return AdditionalProperties.ofBoolean(obj.getBoolean(key));
	}

	/**
	 * Reads a list of strings from a JSONObject property if present.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param key
	 *            The property name.
	 * @return The string list or {@code null}.
	 */
	private static List<String> optStrList(JSONObject obj, String key) {
		if (obj.isNull(key)) {
			return null;
		}
		JSONArray arr = obj.getJSONArray(key);
		List<String> res = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			res.add(arr.getString(i));
		}
		return res;
	}

	/**
	 * Copies all {@code x-*} specification extensions from a JSONObject into a
	 * target map.
	 *
	 * @param obj
	 *            The source JSONObject.
	 * @param target
	 *            The target extensions map.
	 */
	private static void copyExtensions(JSONObject obj, Map<String, PayloadValue> target) {
		for (String k : obj.keySet()) {
			if (k.startsWith("x-")) {
				target.put(k, PayloadValue.fromJsonValue(obj.get(k)));
			}
		}
	}

	/**
	 * Parses an {@link Info} object from JSON.
	 *
	 * @param infoObj
	 *            The JSON object.
	 * @return The parsed {@link Info}.
	 */
	private static Info parseInfo(JSONObject infoObj) {
		Info info = new Info();
		info.title = optStr(infoObj, "title");
		info.summary = optStr(infoObj, "summary");
		info.description = optStr(infoObj, "description");
		info.termsOfService = optStr(infoObj, "termsOfService");
		info.version = optStr(infoObj, "version");
		if (infoObj.has("contact")) {
			JSONObject cObj = infoObj.getJSONObject("contact");
			Contact c = new Contact();
			c.name = optStr(cObj, "name");
			c.url = optStr(cObj, "url");
			c.email = optStr(cObj, "email");
			copyExtensions(cObj, c.extensions);
			info.contact = c;
		}
		if (infoObj.has("license")) {
			JSONObject lObj = infoObj.getJSONObject("license");
			License l = new License();
			l.name = optStr(lObj, "name");
			l.identifier = optStr(lObj, "identifier");
			l.url = optStr(lObj, "url");
			copyExtensions(lObj, l.extensions);
			info.license = l;
		}
		copyExtensions(infoObj, info.extensions);
		return info;
	}

	/**
	 * Parses an {@link ExternalDocumentation} object from JSON.
	 *
	 * @param edObj
	 *            The JSON object.
	 * @return The parsed {@link ExternalDocumentation}.
	 */
	private static ExternalDocumentation parseExternalDocs(JSONObject edObj) {
		ExternalDocumentation ed = new ExternalDocumentation();
		ed.description = optStr(edObj, "description");
		ed.url = optStr(edObj, "url");
		copyExtensions(edObj, ed.extensions);
		return ed;
	}

	/**
	 * Parses a {@code servers} array if present on the parent JSONObject.
	 *
	 * @param parent
	 *            The parent JSONObject.
	 * @return The list of {@link Server} objects or {@code null}.
	 */
	private static List<Server> parseServers(JSONObject parent) {
		if (!parent.has("servers")) {
			return null;
		}
		JSONArray arr = parent.getJSONArray("servers");
		List<Server> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(parseServer(arr.getJSONObject(i)));
		}
		return list;
	}

	/**
	 * Parses a single {@link Server} object from JSON.
	 *
	 * @param sObj
	 *            The JSON object.
	 * @return The parsed {@link Server}.
	 */
	private static Server parseServer(JSONObject sObj) {
		Server s = new Server();
		s.url = optStr(sObj, "url");
		s.description = optStr(sObj, "description");
		s.name = optStr(sObj, "name");
		if (sObj.has("variables")) {
			s.variables = new HashMap<>();
			JSONObject varsObj = sObj.getJSONObject("variables");
			for (String vk : varsObj.keySet()) {
				JSONObject vo = varsObj.getJSONObject(vk);
				ServerVariable sv = new ServerVariable();
				sv.defaultValue = optStr(vo, "default");
				sv.description = optStr(vo, "description");
				sv.enumValues = optStrList(vo, "enum");
				copyExtensions(vo, sv.extensions);
				s.variables.put(vk, sv);
			}
		}
		copyExtensions(sObj, s.extensions);
		return s;
	}

	/**
	 * Parses a JSON array of {@link SecurityRequirement} objects.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The list of security requirements.
	 */
	private static List<SecurityRequirement> parseSecurityRequirements(JSONArray arr) {
		List<SecurityRequirement> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject srObj = arr.getJSONObject(i);
			SecurityRequirement sr = new SecurityRequirement();
			for (String k : srObj.keySet()) {
				sr.addRequirement(k, optStrList(srObj, k));
			}
			list.add(sr);
		}
		return list;
	}

	/**
	 * Parses a JSON array of {@link Tag} objects.
	 *
	 * @param arr
	 *            The JSON array.
	 * @return The list of tags.
	 */
	private static List<Tag> parseTags(JSONArray arr) {
		List<Tag> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject tObj = arr.getJSONObject(i);
			Tag t = new Tag();
			t.name = optStr(tObj, "name");
			t.summary = optStr(tObj, "summary");
			t.description = optStr(tObj, "description");
			t.parent = optStr(tObj, "parent");
			t.kind = optStr(tObj, "kind");
			if (tObj.has("externalDocs")) {
				t.externalDocs = parseExternalDocs(tObj.getJSONObject("externalDocs"));
			}
			copyExtensions(tObj, t.extensions);
			list.add(t);
		}
		return list;
	}

	/**
	 * Parses a {@link Paths} object from JSON.
	 *
	 * @param pathsObj
	 *            The JSON object.
	 * @return The parsed {@link Paths}.
	 */
	private static Paths parsePaths(JSONObject pathsObj) {
		Paths paths = new Paths();
		paths.pathItems = new HashMap<>();
		for (String pathKey : pathsObj.keySet()) {
			if (pathKey.startsWith("x-")) {
				paths.extensions.put(pathKey, PayloadValue.fromJsonValue(pathsObj.get(pathKey)));
			} else {
				paths.pathItems.put(pathKey, parsePathItem(pathsObj.getJSONObject(pathKey)));
			}
		}
		return paths;
	}

	/**
	 * Parses a map of {@link PathItem} objects from JSON.
	 *
	 * @param mapObj
	 *            The JSON object.
	 * @return The parsed map of path items.
	 */
	private static Map<String, PathItem> parsePathItemMap(JSONObject mapObj) {
		Map<String, PathItem> map = new HashMap<>();
		for (String k : mapObj.keySet()) {
			map.put(k, parsePathItem(mapObj.getJSONObject(k)));
		}
		return map;
	}

	/**
	 * Parses a {@link PathItem} from JSON.
	 *
	 * @param piObj
	 *            The JSON object.
	 * @return The parsed {@link PathItem}.
	 */
	private static PathItem parsePathItem(JSONObject piObj) {
		PathItem pi = new PathItem();
		pi.ref = optStr(piObj, "$ref");
		if (pi.ref == null) {
			pi.ref = optStr(piObj, "ref");
		}
		pi.summary = optStr(piObj, "summary");
		pi.description = optStr(piObj, "description");
		if (piObj.has("get"))
			pi.get = parseOperation(piObj.getJSONObject("get"));
		if (piObj.has("put"))
			pi.put = parseOperation(piObj.getJSONObject("put"));
		if (piObj.has("post"))
			pi.post = parseOperation(piObj.getJSONObject("post"));
		if (piObj.has("delete"))
			pi.delete = parseOperation(piObj.getJSONObject("delete"));
		if (piObj.has("options"))
			pi.options = parseOperation(piObj.getJSONObject("options"));
		if (piObj.has("head"))
			pi.head = parseOperation(piObj.getJSONObject("head"));
		if (piObj.has("patch"))
			pi.patch = parseOperation(piObj.getJSONObject("patch"));
		if (piObj.has("trace"))
			pi.trace = parseOperation(piObj.getJSONObject("trace"));
		if (piObj.has("query"))
			pi.query = parseOperation(piObj.getJSONObject("query"));
		if (piObj.has("additionalOperations")) {
			pi.additionalOperations = new HashMap<>();
			JSONObject addOps = piObj.getJSONObject("additionalOperations");
			for (String method : addOps.keySet()) {
				pi.additionalOperations.put(method, parseOperation(addOps.getJSONObject(method)));
			}
		}
		pi.servers = parseServers(piObj);
		if (piObj.has("parameters")) {
			pi.parameters = parseRefOrList(piObj.getJSONArray("parameters"), Parse::parseParameter);
		}
		copyExtensions(piObj, pi.extensions);
		return pi;
	}

	/**
	 * Parses a JSON object into an OpenAPI {@link Operation} instance.
	 *
	 * @param obj
	 *            The JSON object representing the operation.
	 * @return The parsed {@link Operation} instance.
	 */
	private static Operation parseOperation(JSONObject obj) {
		Operation op = new Operation();
		op.tags = optStrList(obj, "tags");
		op.summary = optStr(obj, "summary");
		op.description = optStr(obj, "description");
		op.operationId = optStr(obj, "operationId");
		op.consumes = optStrList(obj, "consumes");
		op.produces = optStrList(obj, "produces");
		op.schemes = optStrList(obj, "schemes");
		op.deprecated = optBool(obj, "deprecated");
		if (obj.has("externalDocs")) {
			op.externalDocs = parseExternalDocs(obj.getJSONObject("externalDocs"));
		}
		if (obj.has("parameters")) {
			op.parameters = parseRefOrList(obj.getJSONArray("parameters"), Parse::parseParameter);
		}
		if (obj.has("requestBody")) {
			op.requestBody = parseRefOr(obj.getJSONObject("requestBody"), Parse::parseRequestBody);
		}
		if (obj.has("responses")) {
			op.responses = parseResponses(obj.getJSONObject("responses"));
		}
		if (obj.has("callbacks")) {
			op.callbacks = parseRefOrMap(obj.getJSONObject("callbacks"), Parse::parseCallback);
		}
		if (obj.has("security")) {
			op.security = parseSecurityRequirements(obj.getJSONArray("security"));
		}
		op.servers = parseServers(obj);
		copyExtensions(obj, op.extensions);
		return op;
	}

	/**
	 * Parses a {@link RequestBody} from JSON.
	 *
	 * @param rbObj
	 *            The JSON object.
	 * @return The parsed {@link RequestBody}.
	 */
	private static RequestBody parseRequestBody(JSONObject rbObj) {
		RequestBody reqBody = new RequestBody();
		reqBody.description = optStr(rbObj, "description");
		reqBody.required = optBool(rbObj, "required");
		if (rbObj.has("content")) {
			reqBody.content = parseMediaTypeMap(rbObj.getJSONObject("content"));
		}
		copyExtensions(rbObj, reqBody.extensions);
		return reqBody;
	}

	/**
	 * Parses a map of {@link MediaType} objects from JSON.
	 *
	 * @param contentObj
	 *            The JSON object.
	 * @return The parsed media type map.
	 */
	private static Map<String, MediaType> parseMediaTypeMap(JSONObject contentObj) {
		Map<String, MediaType> map = new HashMap<>();
		for (String cKey : contentObj.keySet()) {
			map.put(cKey, parseMediaType(contentObj.getJSONObject(cKey)));
		}
		return map;
	}

	/**
	 * Parses a single {@link MediaType} from JSON.
	 *
	 * @param mtObj
	 *            The JSON object.
	 * @return The parsed {@link MediaType}.
	 */
	private static MediaType parseMediaType(JSONObject mtObj) {
		MediaType mt = new MediaType();
		if (mtObj.has("schema")) {
			mt.schema = parseRefOr(mtObj.getJSONObject("schema"), Parse::parseSchema);
		}
		if (mtObj.has("itemSchema")) {
			mt.itemSchema = parseRefOr(mtObj.getJSONObject("itemSchema"), Parse::parseSchema);
		}
		mt.example = optPayloadVal(mtObj, "example");
		if (mtObj.has("examples")) {
			mt.examples = parseRefOrMap(mtObj.getJSONObject("examples"), Parse::parseExample);
		}
		if (mtObj.has("encoding")) {
			mt.encoding = parseEncodingMap(mtObj.getJSONObject("encoding"));
		}
		if (mtObj.has("prefixEncoding")) {
			mt.prefixEncoding = parseEncodingList(mtObj.getJSONArray("prefixEncoding"));
		}
		if (mtObj.has("itemEncoding")) {
			mt.itemEncoding = parseEncoding(mtObj.getJSONObject("itemEncoding"));
		}
		copyExtensions(mtObj, mt.extensions);
		return mt;
	}

	/**
	 * Parses a map of {@link Encoding} objects from JSON.
	 *
	 * @param encMapObj
	 *            The JSON object.
	 * @return The parsed map.
	 */
	private static Map<String, Encoding> parseEncodingMap(JSONObject encMapObj) {
		Map<String, Encoding> map = new HashMap<>();
		for (String k : encMapObj.keySet()) {
			map.put(k, parseEncoding(encMapObj.getJSONObject(k)));
		}
		return map;
	}

	/**
	 * Parses a list of {@link Encoding} objects from a JSONArray.
	 *
	 * @param arr
	 *            The JSONArray.
	 * @return The parsed list.
	 */
	private static List<Encoding> parseEncodingList(JSONArray arr) {
		List<Encoding> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(parseEncoding(arr.getJSONObject(i)));
		}
		return list;
	}

	/**
	 * Parses a single {@link Encoding} object from JSON.
	 *
	 * @param eObj
	 *            The JSON object.
	 * @return The parsed {@link Encoding}.
	 */
	private static Encoding parseEncoding(JSONObject eObj) {
		Encoding enc = new Encoding();
		enc.contentType = optStr(eObj, "contentType");
		enc.style = optStr(eObj, "style");
		enc.explode = optBool(eObj, "explode");
		enc.allowReserved = optBool(eObj, "allowReserved");
		if (eObj.has("headers")) {
			enc.headers = parseRefOrMap(eObj.getJSONObject("headers"), Parse::parseHeader);
		}
		if (eObj.has("encoding")) {
			enc.encoding = parseEncodingMap(eObj.getJSONObject("encoding"));
		}
		if (eObj.has("prefixEncoding")) {
			enc.prefixEncoding = parseEncodingList(eObj.getJSONArray("prefixEncoding"));
		}
		if (eObj.has("itemEncoding")) {
			enc.itemEncoding = parseEncoding(eObj.getJSONObject("itemEncoding"));
		}
		copyExtensions(eObj, enc.extensions);
		return enc;
	}

	/**
	 * Parses a {@link Responses} container from JSON.
	 *
	 * @param respObj
	 *            The JSON object.
	 * @return The parsed {@link Responses}.
	 */
	private static Responses parseResponses(JSONObject respObj) {
		Responses responses = new Responses();
		responses.statusCodes = new HashMap<>();
		for (String rKey : respObj.keySet()) {
			if (rKey.startsWith("x-")) {
				responses.extensions.put(rKey, PayloadValue.fromJsonValue(respObj.get(rKey)));
			} else if ("default".equals(rKey)) {
				responses.defaultResponse = parseRefOr(respObj.getJSONObject(rKey), Parse::parseResponse);
			} else {
				responses.statusCodes.put(rKey, parseRefOr(respObj.getJSONObject(rKey), Parse::parseResponse));
			}
		}
		return responses;
	}

	/**
	 * Parses a single {@link Response} from JSON.
	 *
	 * @param ro
	 *            The JSON object.
	 * @return The parsed {@link Response}.
	 */
	private static Response parseResponse(JSONObject ro) {
		Response r = new Response();
		r.summary = optStr(ro, "summary");
		r.description = optStr(ro, "description");
		if (ro.has("schema")) {
			r.schema = parseRefOr(ro.getJSONObject("schema"), Parse::parseSchema);
		}
		if (ro.has("headers")) {
			r.headers = parseRefOrMap(ro.getJSONObject("headers"), Parse::parseHeader);
		}
		if (ro.has("examples")) {
			r.examples = parseRefOrMap(ro.getJSONObject("examples"), Parse::parseExample);
		}
		if (ro.has("content")) {
			r.content = parseMediaTypeMap(ro.getJSONObject("content"));
		}
		if (ro.has("links")) {
			r.links = parseRefOrMap(ro.getJSONObject("links"), Parse::parseLink);
		}
		copyExtensions(ro, r.extensions);
		return r;
	}

	/**
	 * Parses a single {@link Parameter} from JSON.
	 *
	 * @param pObj
	 *            The JSON object.
	 * @return The parsed {@link Parameter}.
	 */
	private static Parameter parseParameter(JSONObject pObj) {
		Parameter p = new Parameter();
		p.name = optStr(pObj, "name");
		p.in = optStr(pObj, "in");
		p.description = optStr(pObj, "description");
		p.required = optBool(pObj, "required");
		p.deprecated = optBool(pObj, "deprecated");
		p.allowEmptyValue = optBool(pObj, "allowEmptyValue");
		p.style = optStr(pObj, "style");
		p.explode = optBool(pObj, "explode");
		p.allowReserved = optBool(pObj, "allowReserved");
		p.type = optStr(pObj, "type");
		p.format = optStr(pObj, "format");
		if (pObj.has("items")) {
			p.items = parseItems(pObj.getJSONObject("items"));
		}
		p.collectionFormat = optStr(pObj, "collectionFormat");
		p.defaultValue = optPayloadVal(pObj, "default");
		p.maximum = optNum(pObj, "maximum");
		p.exclusiveMaximum = optBool(pObj, "exclusiveMaximum");
		p.minimum = optNum(pObj, "minimum");
		p.exclusiveMinimum = optBool(pObj, "exclusiveMinimum");
		p.maxLength = optInt(pObj, "maxLength");
		p.minLength = optInt(pObj, "minLength");
		p.pattern = optStr(pObj, "pattern");
		p.maxItems = optInt(pObj, "maxItems");
		p.minItems = optInt(pObj, "minItems");
		p.uniqueItems = optBool(pObj, "uniqueItems");
		p.enumValues = optPayloadValList(pObj, "enum");
		p.multipleOf = optNum(pObj, "multipleOf");
		if (pObj.has("schema")) {
			p.schema = parseRefOr(pObj.getJSONObject("schema"), Parse::parseSchema);
		}
		p.example = optPayloadVal(pObj, "example");
		if (pObj.has("examples")) {
			p.examples = parseRefOrMap(pObj.getJSONObject("examples"), Parse::parseExample);
		}
		if (pObj.has("content")) {
			p.content = parseMediaTypeMap(pObj.getJSONObject("content"));
		}
		copyExtensions(pObj, p.extensions);
		return p;
	}

	/**
	 * Parses a single {@link Header} from JSON.
	 *
	 * @param hObj
	 *            The JSON object.
	 * @return The parsed {@link Header}.
	 */
	private static Header parseHeader(JSONObject hObj) {
		Header h = new Header();
		h.description = optStr(hObj, "description");
		h.required = optBool(hObj, "required");
		h.deprecated = optBool(hObj, "deprecated");
		h.allowEmptyValue = optBool(hObj, "allowEmptyValue");
		h.style = optStr(hObj, "style");
		h.explode = optBool(hObj, "explode");
		h.allowReserved = optBool(hObj, "allowReserved");
		h.type = optStr(hObj, "type");
		h.format = optStr(hObj, "format");
		if (hObj.has("items")) {
			h.items = parseItems(hObj.getJSONObject("items"));
		}
		h.collectionFormat = optStr(hObj, "collectionFormat");
		h.defaultValue = optPayloadVal(hObj, "default");
		h.maximum = optNum(hObj, "maximum");
		h.exclusiveMaximum = optBool(hObj, "exclusiveMaximum");
		h.minimum = optNum(hObj, "minimum");
		h.exclusiveMinimum = optBool(hObj, "exclusiveMinimum");
		h.maxLength = optInt(hObj, "maxLength");
		h.minLength = optInt(hObj, "minLength");
		h.pattern = optStr(hObj, "pattern");
		h.maxItems = optInt(hObj, "maxItems");
		h.minItems = optInt(hObj, "minItems");
		h.uniqueItems = optBool(hObj, "uniqueItems");
		h.enumValues = optPayloadValList(hObj, "enum");
		h.multipleOf = optNum(hObj, "multipleOf");
		if (hObj.has("schema")) {
			h.schema = parseRefOr(hObj.getJSONObject("schema"), Parse::parseSchema);
		}
		h.example = optPayloadVal(hObj, "example");
		if (hObj.has("examples")) {
			h.examples = parseRefOrMap(hObj.getJSONObject("examples"), Parse::parseExample);
		}
		if (hObj.has("content")) {
			h.content = parseMediaTypeMap(hObj.getJSONObject("content"));
		}
		copyExtensions(hObj, h.extensions);
		return h;
	}

	/**
	 * Parses a Swagger 2.0 {@link Items} object from JSON.
	 *
	 * @param iObj
	 *            The JSON object.
	 * @return The parsed {@link Items}.
	 */
	private static Items parseItems(JSONObject iObj) {
		Items items = new Items();
		items.type = optStr(iObj, "type");
		items.format = optStr(iObj, "format");
		if (iObj.has("items")) {
			items.items = parseItems(iObj.getJSONObject("items"));
		}
		items.collectionFormat = optStr(iObj, "collectionFormat");
		items.defaultValue = optPayloadVal(iObj, "default");
		items.maximum = optNum(iObj, "maximum");
		items.exclusiveMaximum = optBool(iObj, "exclusiveMaximum");
		items.minimum = optNum(iObj, "minimum");
		items.exclusiveMinimum = optBool(iObj, "exclusiveMinimum");
		items.maxLength = optInt(iObj, "maxLength");
		items.minLength = optInt(iObj, "minLength");
		items.pattern = optStr(iObj, "pattern");
		items.maxItems = optInt(iObj, "maxItems");
		items.minItems = optInt(iObj, "minItems");
		items.uniqueItems = optBool(iObj, "uniqueItems");
		items.enumValues = optPayloadValList(iObj, "enum");
		items.multipleOf = optNum(iObj, "multipleOf");
		copyExtensions(iObj, items.extensions);
		return items;
	}

	/**
	 * Parses an {@link Example} object from JSON.
	 *
	 * @param exObj
	 *            The JSON object.
	 * @return The parsed {@link Example}.
	 */
	private static Example parseExample(JSONObject exObj) {
		Example ex = new Example();
		ex.summary = optStr(exObj, "summary");
		ex.description = optStr(exObj, "description");
		ex.value = optPayloadVal(exObj, "value");
		ex.externalValue = optStr(exObj, "externalValue");
		ex.dataValue = optPayloadVal(exObj, "dataValue");
		ex.serializedValue = optStr(exObj, "serializedValue");
		copyExtensions(exObj, ex.extensions);
		return ex;
	}

	/**
	 * Parses a {@link Link} object from JSON.
	 *
	 * @param lObj
	 *            The JSON object.
	 * @return The parsed {@link Link}.
	 */
	private static Link parseLink(JSONObject lObj) {
		Link l = new Link();
		l.operationRef = optStr(lObj, "operationRef");
		l.operationId = optStr(lObj, "operationId");
		l.description = optStr(lObj, "description");
		l.requestBody = optPayloadVal(lObj, "requestBody");
		if (lObj.has("parameters")) {
			JSONObject pMapObj = lObj.getJSONObject("parameters");
			l.parameters = new HashMap<>();
			for (String k : pMapObj.keySet()) {
				l.parameters.put(k, PayloadValue.fromJsonValue(pMapObj.get(k)));
			}
		}
		if (lObj.has("server")) {
			l.server = parseServer(lObj.getJSONObject("server"));
		}
		copyExtensions(lObj, l.extensions);
		return l;
	}

	/**
	 * Parses a {@link Callback} object from JSON.
	 *
	 * @param cbObj
	 *            The JSON object.
	 * @return The parsed {@link Callback}.
	 */
	private static Callback parseCallback(JSONObject cbObj) {
		Callback cb = new Callback();
		for (String k : cbObj.keySet()) {
			if (k.startsWith("x-")) {
				cb.extensions.put(k, PayloadValue.fromJsonValue(cbObj.get(k)));
			} else {
				cb.pathItems.put(k, parsePathItem(cbObj.getJSONObject(k)));
			}
		}
		return cb;
	}

	/**
	 * Parses a {@link SecurityScheme} from JSON.
	 *
	 * @param scObj
	 *            The JSON object.
	 * @return The parsed {@link SecurityScheme}.
	 */
	private static SecurityScheme parseSecurityScheme(JSONObject scObj) {
		SecurityScheme sc = new SecurityScheme();
		sc.type = optStr(scObj, "type");
		sc.description = optStr(scObj, "description");
		sc.name = optStr(scObj, "name");
		sc.in = optStr(scObj, "in");
		sc.scheme = optStr(scObj, "scheme");
		sc.bearerFormat = optStr(scObj, "bearerFormat");
		sc.flow = optStr(scObj, "flow");
		sc.authorizationUrl = optStr(scObj, "authorizationUrl");
		sc.tokenUrl = optStr(scObj, "tokenUrl");
		sc.openIdConnectUrl = optStr(scObj, "openIdConnectUrl");
		sc.oauth2MetadataUrl = optStr(scObj, "oauth2MetadataUrl");
		sc.deprecated = optBool(scObj, "deprecated");
		if (scObj.has("scopes")) {
			sc.scopes = parseStringMap(scObj.getJSONObject("scopes"));
		}
		if (scObj.has("flows")) {
			JSONObject fObj = scObj.getJSONObject("flows");
			OAuthFlows flows = new OAuthFlows();
			if (fObj.has("implicit"))
				flows.implicit = parseOAuthFlow(fObj.getJSONObject("implicit"));
			if (fObj.has("password"))
				flows.password = parseOAuthFlow(fObj.getJSONObject("password"));
			if (fObj.has("clientCredentials"))
				flows.clientCredentials = parseOAuthFlow(fObj.getJSONObject("clientCredentials"));
			if (fObj.has("authorizationCode"))
				flows.authorizationCode = parseOAuthFlow(fObj.getJSONObject("authorizationCode"));
			if (fObj.has("deviceAuthorization"))
				flows.deviceAuthorization = parseOAuthFlow(fObj.getJSONObject("deviceAuthorization"));
			copyExtensions(fObj, flows.extensions);
			sc.flows = flows;
		}
		copyExtensions(scObj, sc.extensions);
		return sc;
	}

	/**
	 * Parses an {@link OAuthFlow} from JSON.
	 *
	 * @param flowObj
	 *            The JSON object.
	 * @return The parsed {@link OAuthFlow}.
	 */
	private static OAuthFlow parseOAuthFlow(JSONObject flowObj) {
		OAuthFlow f = new OAuthFlow();
		f.authorizationUrl = optStr(flowObj, "authorizationUrl");
		f.tokenUrl = optStr(flowObj, "tokenUrl");
		f.refreshUrl = optStr(flowObj, "refreshUrl");
		f.deviceAuthorizationUrl = optStr(flowObj, "deviceAuthorizationUrl");
		if (flowObj.has("scopes")) {
			f.scopes = parseStringMap(flowObj.getJSONObject("scopes"));
		}
		copyExtensions(flowObj, f.extensions);
		return f;
	}

	/**
	 * Parses a string-to-string map from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed string map.
	 */
	private static Map<String, String> parseStringMap(JSONObject obj) {
		Map<String, String> map = new HashMap<>();
		for (String k : obj.keySet()) {
			map.put(k, obj.getString(k));
		}
		return map;
	}

	/**
	 * Parses a {@link Components} object from JSON.
	 *
	 * @param compObj
	 *            The JSON object.
	 * @return The parsed {@link Components}.
	 */
	private static Components parseComponents(JSONObject compObj) {
		Components comp = new Components();
		if (compObj.has("schemas")) {
			comp.schemas = parseRefOrMap(compObj.getJSONObject("schemas"), Parse::parseSchema);
		}
		if (compObj.has("responses")) {
			comp.responses = parseRefOrMap(compObj.getJSONObject("responses"), Parse::parseResponse);
		}
		if (compObj.has("parameters")) {
			comp.parameters = parseRefOrMap(compObj.getJSONObject("parameters"), Parse::parseParameter);
		}
		if (compObj.has("examples")) {
			comp.examples = parseRefOrMap(compObj.getJSONObject("examples"), Parse::parseExample);
		}
		if (compObj.has("requestBodies")) {
			comp.requestBodies = parseRefOrMap(compObj.getJSONObject("requestBodies"), Parse::parseRequestBody);
		}
		if (compObj.has("headers")) {
			comp.headers = parseRefOrMap(compObj.getJSONObject("headers"), Parse::parseHeader);
		}
		if (compObj.has("securitySchemes")) {
			comp.securitySchemes = parseRefOrMap(compObj.getJSONObject("securitySchemes"), Parse::parseSecurityScheme);
		}
		if (compObj.has("links")) {
			comp.links = parseRefOrMap(compObj.getJSONObject("links"), Parse::parseLink);
		}
		if (compObj.has("callbacks")) {
			comp.callbacks = parseRefOrMap(compObj.getJSONObject("callbacks"), Parse::parseCallback);
		}
		if (compObj.has("pathItems")) {
			JSONObject piMapObj = compObj.getJSONObject("pathItems");
			comp.pathItems = new HashMap<>();
			for (String piKey : piMapObj.keySet()) {
				comp.pathItems.put(piKey, ReferenceOr.ofValue(parsePathItem(piMapObj.getJSONObject(piKey))));
			}
		}
		if (compObj.has("mediaTypes")) {
			comp.mediaTypes = parseRefOrMap(compObj.getJSONObject("mediaTypes"), Parse::parseMediaType);
		}
		copyExtensions(compObj, comp.extensions);
		return comp;
	}

	/**
	 * Parses a map of {@link Schema} objects from JSON.
	 *
	 * @param schemasObj
	 *            The JSON object.
	 * @return The parsed schema map.
	 */
	private static Map<String, Schema> parseSchemaMap(JSONObject schemasObj) {
		Map<String, Schema> map = new HashMap<>();
		for (String sKey : schemasObj.keySet()) {
			map.put(sKey, parseSchema(schemasObj.getJSONObject(sKey)));
		}
		return map;
	}

	/**
	 * Parses a map of {@link Parameter} objects from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed parameter map.
	 */
	private static Map<String, Parameter> parseTypedParameterMap(JSONObject obj) {
		Map<String, Parameter> map = new HashMap<>();
		for (String k : obj.keySet()) {
			map.put(k, parseParameter(obj.getJSONObject(k)));
		}
		return map;
	}

	/**
	 * Parses a map of {@link Response} objects from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed response map.
	 */
	private static Map<String, Response> parseTypedResponseMap(JSONObject obj) {
		Map<String, Response> map = new HashMap<>();
		for (String k : obj.keySet()) {
			map.put(k, parseResponse(obj.getJSONObject(k)));
		}
		return map;
	}

	/**
	 * Parses a map of {@link SecurityScheme} objects from JSON.
	 *
	 * @param obj
	 *            The JSON object.
	 * @return The parsed security scheme map.
	 */
	private static Map<String, SecurityScheme> parseTypedSecuritySchemeMap(JSONObject obj) {
		Map<String, SecurityScheme> map = new HashMap<>();
		for (String k : obj.keySet()) {
			map.put(k, parseSecurityScheme(obj.getJSONObject(k)));
		}
		return map;
	}

	/**
	 * Parses a JSON object into an OpenAPI {@link Schema} instance.
	 *
	 * @param sObj
	 *            The JSON object representing the schema.
	 * @return The parsed {@link Schema} instance.
	 */
	public static Schema parseSchema(JSONObject sObj) {
		Schema s = new Schema();
		s.$ref = optStr(sObj, "$ref");
		if (!sObj.isNull("type")) {
			Object tRaw = sObj.get("type");
			if (tRaw instanceof JSONArray) {
				s.type = SchemaType.ofMultiple(optStrList(sObj, "type"));
			} else {
				s.type = SchemaType.ofSingle(sObj.getString("type"));
			}
		}
		s.title = optStr(sObj, "title");
		s.format = optStr(sObj, "format");
		s.description = optStr(sObj, "description");
		s.multipleOf = optNum(sObj, "multipleOf");
		s.maximum = optNum(sObj, "maximum");
		s.exclusiveMaximum = optExclusiveBound(sObj, "exclusiveMaximum");
		s.minimum = optNum(sObj, "minimum");
		s.exclusiveMinimum = optExclusiveBound(sObj, "exclusiveMinimum");
		s.maxLength = optInt(sObj, "maxLength");
		s.minLength = optInt(sObj, "minLength");
		s.pattern = optStr(sObj, "pattern");
		s.maxItems = optInt(sObj, "maxItems");
		s.minItems = optInt(sObj, "minItems");
		s.uniqueItems = optBool(sObj, "uniqueItems");
		s.maxProperties = optInt(sObj, "maxProperties");
		s.minProperties = optInt(sObj, "minProperties");
		s.required = optStrList(sObj, "required");
		s.enumValues = optPayloadValList(sObj, "enum");
		if (sObj.has("allOf"))
			s.allOf = parseRefOrList(sObj.getJSONArray("allOf"), Parse::parseSchema);
		if (sObj.has("anyOf"))
			s.anyOf = parseRefOrList(sObj.getJSONArray("anyOf"), Parse::parseSchema);
		if (sObj.has("oneOf"))
			s.oneOf = parseRefOrList(sObj.getJSONArray("oneOf"), Parse::parseSchema);
		if (sObj.has("not"))
			s.not = parseRefOr(sObj.getJSONObject("not"), Parse::parseSchema);
		if (sObj.has("items")) {
			s.items = parseRefOr(sObj.getJSONObject("items"), Parse::parseSchema);
		}
		if (sObj.has("properties")) {
			s.properties = parseRefOrMap(sObj.getJSONObject("properties"), Parse::parseSchema);
		}
		s.additionalProperties = optAdditionalProperties(sObj, "additionalProperties");
		s.defaultValue = optPayloadVal(sObj, "default");
		s.constValue = optPayloadVal(sObj, "const");
		s.readOnly = optBool(sObj, "readOnly");
		s.writeOnly = optBool(sObj, "writeOnly");
		s.deprecated = optBool(sObj, "deprecated");
		s.example = optPayloadVal(sObj, "example");
		s.examples = optPayloadValList(sObj, "examples");
		if (sObj.has("externalDocs")) {
			s.externalDocs = parseExternalDocs(sObj.getJSONObject("externalDocs"));
		}
		if (sObj.has("xml")) {
			JSONObject xObj = sObj.getJSONObject("xml");
			XML xml = new XML();
			xml.name = optStr(xObj, "name");
			xml.namespace = optStr(xObj, "namespace");
			xml.prefix = optStr(xObj, "prefix");
			xml.attribute = optBool(xObj, "attribute");
			xml.wrapped = optBool(xObj, "wrapped");
			xml.nodeType = optStr(xObj, "nodeType");
			copyExtensions(xObj, xml.extensions);
			s.xml = xml;
		}
		if (sObj.has("discriminator")) {
			JSONObject dObj = sObj.getJSONObject("discriminator");
			Discriminator d = new Discriminator();
			d.propertyName = optStr(dObj, "propertyName");
			d.defaultMapping = optStr(dObj, "defaultMapping");
			if (dObj.has("mapping")) {
				d.mapping = parseStringMap(dObj.getJSONObject("mapping"));
			}
			copyExtensions(dObj, d.extensions);
			s.discriminator = d;
		}
		s.id = optStr(sObj, "$id");
		s.schema = optStr(sObj, "$schema");
		s.anchor = optStr(sObj, "$anchor");
		s.dynamicAnchor = optStr(sObj, "$dynamicAnchor");
		if (sObj.has("$vocabulary")) {
			s.vocabulary = new HashMap<>();
			JSONObject vocObj = sObj.getJSONObject("$vocabulary");
			for (String vk : vocObj.keySet()) {
				s.vocabulary.put(vk, vocObj.getBoolean(vk));
			}
		}
		if (sObj.has("$defs")) {
			s.defs = parseRefOrMap(sObj.getJSONObject("$defs"), Parse::parseSchema);
		}
		if (sObj.has("dependentRequired")) {
			s.dependentRequired = new HashMap<>();
			JSONObject drObj = sObj.getJSONObject("dependentRequired");
			for (String dk : drObj.keySet()) {
				s.dependentRequired.put(dk, optStrList(drObj, dk));
			}
		}
		if (sObj.has("dependentSchemas")) {
			s.dependentSchemas = parseRefOrMap(sObj.getJSONObject("dependentSchemas"), Parse::parseSchema);
		}
		if (sObj.has("propertyNames")) {
			s.propertyNames = parseRefOr(sObj.getJSONObject("propertyNames"), Parse::parseSchema);
		}
		if (sObj.has("patternProperties")) {
			s.patternProperties = parseRefOrMap(sObj.getJSONObject("patternProperties"), Parse::parseSchema);
		}
		s.unevaluatedProperties = optAdditionalProperties(sObj, "unevaluatedProperties");
		s.unevaluatedItems = optAdditionalProperties(sObj, "unevaluatedItems");
		if (sObj.has("prefixItems")) {
			s.prefixItems = parseRefOrList(sObj.getJSONArray("prefixItems"), Parse::parseSchema);
		}
		if (sObj.has("contains")) {
			s.contains = parseRefOr(sObj.getJSONObject("contains"), Parse::parseSchema);
		}
		s.minContains = optInt(sObj, "minContains");
		s.maxContains = optInt(sObj, "maxContains");
		copyExtensions(sObj, s.extensions);
		return s;
	}

	/**
	 * Parse OpenAPI description from file.
	 *
	 * @param file
	 *            The file.
	 * @return OpenAPI object.
	 * @throws IOException
	 *             If parsing fails.
	 */
	public static OpenAPI fromFile(File file) throws IOException {
		String content = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
		return fromString(content, file.toURI().toString());
	}
}
