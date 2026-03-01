package functions;

import openapi.OpenAPI;

/**
 * Parses functions from language source to OpenAPI representation or vice versa.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses functions to OpenAPI.
     * @param input The input.
     * @return The parsed object.
     */
    public static OpenAPI parse(String input) {
        OpenAPI api = new OpenAPI();
        return api;
    }
}
