package docstrings;

import java.util.HashMap;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;
import openapi.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses docstrings from language source or documentation JSON to an OpenAPI
 * representation.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses documentation JSON (as emitted by {@link Emit#emitDocsJson}) into an
	 * OpenAPI model.
	 *
	 * @param input
	 *            The documentation JSON string or source input.
	 * @return The parsed {@link OpenAPI} object.
	 */
	public static OpenAPI parse(String input) {
		OpenAPI api = new OpenAPI();
		if (input == null || !input.trim().startsWith("{")) {
			return api;
		}
		JSONObject root = new JSONObject(input);
		if (root.has("endpoints")) {
			api.paths = new Paths();
			api.paths.pathItems = new HashMap<>();
			JSONArray endpoints = root.getJSONArray("endpoints");
			for (int i = 0; i < endpoints.length(); i++) {
				JSONObject ep = endpoints.getJSONObject(i);
				String path = ep.optString("path", "/");
				String method = ep.optString("method", "GET").toUpperCase();
				PathItem item = api.paths.pathItems.computeIfAbsent(path, k -> new PathItem());
				Operation op = new Operation();
				if (ep.has("operationId")) {
					op.operationId = ep.getString("operationId");
				}
				item.setOperation(openapi.HttpVerb.fromStringOrDefault(method, openapi.HttpVerb.GET), op);
			}
		}
		return api;
	}
}
