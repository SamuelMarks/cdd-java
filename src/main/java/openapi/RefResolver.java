package openapi;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Resolves external references in OpenAPI specifications.
 */
public class RefResolver {

	/**
	 * Cache of fetched URIs to prevent circular dependencies.
	 */
	public Map<String, String> cache = new HashMap<>();

	/**
	 * Valid OpenAPI component categories for reference bundling.
	 */
	public static final java.util.Set<String> VALID_COMPONENT_CATEGORIES = new java.util.HashSet<>(
			java.util.Arrays.asList("schemas", "responses", "parameters", "examples", "requestBodies", "headers",
					"securitySchemes", "links", "callbacks", "pathItems", "mediaTypes"));

	/**
	 * Fetched JSON objects mapped by URI string.
	 */
	public Map<String, JSONObject> jsonCache = new HashMap<>();

	/**
	 * Cache mapping canonical resolved URI+fragment strings to bundled component
	 * names.
	 */
	public Map<String, String> bundledKeyCache = new HashMap<>();

	/**
	 * Counter for naming conflicts.
	 */
	private int nameCounter = 1;

	/**
	 * Shared HTTP client for fetching remote OpenAPI references.
	 */
	private static volatile HttpClient httpClient;

	/**
	 * Injected reference fetcher strategy.
	 */
	private final ReferenceFetcher fetcher;

	/**
	 * Returns the shared {@link HttpClient} instance, initializing it lazily on
	 * first HTTP/HTTPS request.
	 *
	 * @return The shared {@link HttpClient} instance.
	 */
	private static HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		}
		return httpClient;
	}

	/**
	 * Default reference fetcher supporting HTTP/HTTPS and local filesystem URIs.
	 *
	 * @param noFragmentUri
	 *            The target {@link URI} without fragment.
	 * @return The fetched specification string.
	 * @throws Exception
	 *             If fetching fails.
	 */
	public static String defaultFetch(URI noFragmentUri) throws Exception {
		String uriStr = noFragmentUri.toString();
		String scheme = noFragmentUri.getScheme();
		if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
			HttpRequest req = HttpRequest.newBuilder(noFragmentUri).GET().build();
			HttpResponse<String> res = getHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
			if (res.statusCode() >= 400) {
				throw new CddException(CddException.ErrorCode.RESOLUTION_ERROR,
						"Failed to fetch " + uriStr + ": HTTP " + res.statusCode());
			}
			return res.body();
		}
		Path path = "file".equalsIgnoreCase(scheme) ? Paths.get(noFragmentUri) : Paths.get(noFragmentUri.getPath());
		return Files.readString(path);
	}

	/**
	 * Default constructor using the standard HTTP and local filesystem
	 * {@link ReferenceFetcher}.
	 */
	public RefResolver() {
		this(RefResolver::defaultFetch);
	}

	/**
	 * Constructs a {@code RefResolver} with a custom {@link ReferenceFetcher}.
	 *
	 * @param fetcher
	 *            The {@link ReferenceFetcher} strategy.
	 */
	public RefResolver(ReferenceFetcher fetcher) {
		this.fetcher = fetcher;
	}

	/**
	 * Fetch a remote or local reference.
	 *
	 * @param ref
	 *            The reference URI (e.g. "http://..." or "./file.yaml").
	 * @param baseUri
	 *            The base URI to resolve relative paths against.
	 * @return The fetched content as a String.
	 * @throws IOException
	 *             If fetching fails.
	 */
	public String fetch(String ref, String baseUri) throws IOException {
		try {
			URI uri = new URI(baseUri).resolve(ref);
			// Strip fragment for fetching
			URI noFragmentUri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
			String uriStr = noFragmentUri.toString();
			if (cache.containsKey(uriStr)) {
				return cache.get(uriStr);
			}
			String content = fetcher.fetch(noFragmentUri);
			cache.put(uriStr, content);
			return content;
		} catch (Exception e) {
			throw new CddException(CddException.ErrorCode.RESOLUTION_ERROR, "Failed to resolve reference: " + ref, e);
		}
	}

	/**
	 * Resolves a JSON pointer against a JSON object.
	 *
	 * @param obj
	 *            The root object.
	 * @param pointer
	 *            The JSON pointer (e.g., "/components/schemas/Pet").
	 * @return The resolved object.
	 */
	public Object resolvePointer(JSONObject obj, String pointer) {
		if (pointer == null || pointer.isEmpty() || pointer.equals("/")) {
			return obj;
		}
		String[] parts = pointer.split("/");
		Object current = obj;
		for (String part : parts) {
			if (part.isEmpty())
				continue;
			part = common.JsonPointerUtils.unescapePointerToken(part);
			if (current instanceof JSONObject) {
				current = ((JSONObject) current).opt(part);
			} else if (current instanceof JSONArray) {
				try {
					current = ((JSONArray) current).opt(Integer.parseInt(part));
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		}
		return current;
	}

	/**
	 * Recursively bundles all external references in the root object.
	 *
	 * @param root
	 *            The root OpenAPI object.
	 * @param baseUri
	 *            The base URI of the root object.
	 * @throws IOException
	 *             If bundling fails.
	 */
	public void bundle(JSONObject root, String baseUri) throws IOException {
		traverseAndBundle(root, baseUri, root);
	}

	/**
	 * Recursively traverses a JSON node and bundles external {@code $ref} targets
	 * into the appropriate {@code components/*} section.
	 *
	 * @param current
	 *            The current {@link JSONObject} or {@link JSONArray} node being
	 *            traversed.
	 * @param currentBaseUri
	 *            The base URI of the document containing {@code current}.
	 * @param rootDocument
	 *            The root OpenAPI {@link JSONObject} receiving bundled components.
	 * @throws IOException
	 *             If fetching or resolving an external reference fails.
	 */
	private void traverseAndBundle(Object current, String currentBaseUri, JSONObject rootDocument) throws IOException {
		if (current instanceof JSONObject) {
			JSONObject obj = (JSONObject) current;
			if (obj.has("$ref")) {
				String ref = obj.getString("$ref");
				if (!ref.startsWith("#")) {
					try {
						URI resolvedUri = new URI(currentBaseUri).resolve(ref);
						String fetchUri = new URI(resolvedUri.getScheme(), resolvedUri.getSchemeSpecificPart(), null)
								.toString();
						String fragment = resolvedUri.getFragment();
						String canonicalKey = fetchUri + (fragment != null ? "#" + fragment : "");

						String category = determineComponentCategory(fragment);
						if (!rootDocument.has("components")) {
							rootDocument.put("components", new JSONObject());
						}
						JSONObject components = rootDocument.getJSONObject("components");
						if (!components.has(category)) {
							components.put(category, new JSONObject());
						}
						JSONObject targetBucket = components.getJSONObject(category);

						if (bundledKeyCache.containsKey(canonicalKey)) {
							obj.put("$ref", "#/components/" + category + "/" + bundledKeyCache.get(canonicalKey));
							return;
						}

						String componentName = extractNameFromRef(ref);
						while (targetBucket.has(componentName)) {
							componentName = componentName + "_" + (nameCounter++);
						}
						bundledKeyCache.put(canonicalKey, componentName);

						JSONObject fetchedObj;
						if (jsonCache.containsKey(fetchUri)) {
							fetchedObj = jsonCache.get(fetchUri);
						} else {
							String content = fetch(ref, currentBaseUri);
							String trimmed = content.trim();
							if (trimmed.startsWith("{")) {
								fetchedObj = new JSONObject(trimmed);
							} else {
								org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
								Map<String, Object> map = yaml.load(content);
								fetchedObj = new JSONObject(map);
							}
							jsonCache.put(fetchUri, fetchedObj);
							traverseAndBundle(fetchedObj, fetchUri, rootDocument);
						}

						Object resolvedComponent = fragment != null
								? resolvePointer(fetchedObj, "/" + fragment)
								: fetchedObj;

						String finalName = componentName;
						for (String existingKey : targetBucket.keySet()) {
							if (targetBucket.get(existingKey).toString().equals(resolvedComponent.toString())) {
								finalName = existingKey;
								break;
							}
						}
						bundledKeyCache.put(canonicalKey, finalName);
						targetBucket.put(finalName, resolvedComponent);
						obj.put("$ref", "#/components/" + category + "/" + finalName);
					} catch (Exception e) {
						throw new CddException(CddException.ErrorCode.RESOLUTION_ERROR,
								"Failed to bundle reference: " + ref, e);
					}
				}
			} else {
				Iterator<String> keys = obj.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					traverseAndBundle(obj.get(key), currentBaseUri, rootDocument);
				}
			}
		} else if (current instanceof JSONArray) {
			JSONArray arr = (JSONArray) current;
			for (int i = 0; i < arr.length(); i++) {
				traverseAndBundle(arr.get(i), currentBaseUri, rootDocument);
			}
		}
	}

	/**
	 * Determines the OpenAPI {@code components} bucket name from a JSON pointer
	 * fragment.
	 *
	 * @param fragment
	 *            The URI fragment (e.g. {@code /components/parameters/Param1}), or
	 *            {@code null}.
	 * @return The target component category name (defaults to {@code "schemas"}).
	 */
	private String determineComponentCategory(String fragment) {
		String normalized = fragment == null ? "" : (fragment.startsWith("/") ? fragment.substring(1) : fragment);
		String[] parts = normalized.split("/");
		if (parts.length >= 2 && "components".equals(parts[0]) && VALID_COMPONENT_CATEGORIES.contains(parts[1])) {
			return parts[1];
		}
		return "schemas";
	}

	/**
	 * Extracts a sanitized component name from a {@code $ref} URI or pointer
	 * string.
	 *
	 * @param ref
	 *            The reference string.
	 * @return A sanitized component identifier.
	 */
	private String extractNameFromRef(String ref) {
		String[] parts = ref.split("/");
		String name = parts[parts.length - 1];
		if (name.contains(".")) {
			name = name.substring(0, name.lastIndexOf('.'));
		}
		name = name.replaceAll("[^a-zA-Z0-9.-]", "_");
		return name;
	}
}
