package openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

/**
 * Emits OpenAPI descriptions.
 */
public class Emit {
    /**
     * Default constructor.
     */
    public Emit() {}

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     */
    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Emits OpenAPI description to string.
     * @param openapi The OpenAPI object.
     * @return JSON string.
     * @throws JsonProcessingException If emitting fails.
     */
    public static String toString(OpenAPI openapi) throws JsonProcessingException {
        return mapper.writeValueAsString(openapi);
    }

    /**
     * Emits OpenAPI description to file.
     * @param openapi The OpenAPI object.
     * @param file The file.
     * @throws IOException If emitting fails.
     */
    public static void toFile(OpenAPI openapi, File file) throws IOException {
        mapper.writeValue(file, openapi);
    }
}
