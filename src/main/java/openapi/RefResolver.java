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
	 * Fetched JSON objects mapped by URI string.
	 */
	public Map<String, JSONObject> jsonCache = new HashMap<>();

	/**
	 * Counter for naming conflicts.
	 */
	private int nameCounter = 1;

	/**
	 * Default constructor.
	 */
	public RefResolver() {
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
			String uriStr = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString();
			if (cache.containsKey(uriStr)) {
				return cache.get(uriStr);
			}
			String content;
			if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
				HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
				HttpRequest req = HttpRequest.newBuilder(new URI(uriStr)).GET().build();
				HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
				if (res.statusCode() >= 400) {
					throw new IOException("Failed to fetch " + uriStr + ": HTTP " + res.statusCode());
				}
				content = res.body();
			} else {
				Path path = Paths.get(uri.getPath());
				content = Files.readString(path);
			}
			cache.put(uriStr, content);
			return content;
		} catch (Exception e) {
			throw new IOException("Failed to resolve reference: " + ref, e);
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
			part = part.replace("~1", "/").replace("~0", "~");
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
		if (!root.has("components")) {
			root.put("components", new JSONObject());
		}
		JSONObject components = root.getJSONObject("components");
		if (!components.has("schemas")) {
			components.put("schemas", new JSONObject());
		}
		traverseAndBundle(root, baseUri, root);
	}

	private void traverseAndBundle(Object current, String currentBaseUri, JSONObject rootDocument) throws IOException {
		if (current instanceof JSONObject) {
			JSONObject obj = (JSONObject) current;
			if (obj.has("$ref")) {
				String ref = obj.getString("$ref");
				if (!ref.startsWith("#")) {
					// External reference
					try {
						URI resolvedUri = new URI(currentBaseUri).resolve(ref);
						String fetchUri = new URI(resolvedUri.getScheme(), resolvedUri.getSchemeSpecificPart(), null)
								.toString();

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
							// Recursively bundle the fetched document before extracting
							traverseAndBundle(fetchedObj, fetchUri, rootDocument);
						}

						String fragment = resolvedUri.getFragment();
						Object resolvedComponent = fragment != null
								? resolvePointer(fetchedObj, "/" + fragment)
								: fetchedObj;

						// Generate a safe name for the bundled component
						String componentName = extractNameFromRef(ref);
						JSONObject schemas = rootDocument.getJSONObject("components").getJSONObject("schemas");

						while (schemas.has(componentName)) {
							// If it's already exactly the same object, we don't need to rename
							if (schemas.get(componentName).toString().equals(resolvedComponent.toString())) {
								break;
							}
							componentName = componentName + "_" + (nameCounter++);
						}

						schemas.put(componentName, resolvedComponent);

						// Rewrite the $ref to point to the local bundled component
						obj.put("$ref", "#/components/schemas/" + componentName);
					} catch (Exception e) {
						throw new IOException("Failed to bundle reference: " + ref, e);
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
