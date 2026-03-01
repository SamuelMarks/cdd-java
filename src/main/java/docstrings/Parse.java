package docstrings;

import openapi.OpenAPI;

/**
 * Parses docstrings from language source to OpenAPI representation or vice versa.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses docstrings into an OpenAPI model.
     * @param input The input.
     * @return The parsed object.
     */
    public static OpenAPI parse(String input) {
        OpenAPI api = new OpenAPI();
        // A full parser would extract the JSON docstrings
        return api;
    }
}
