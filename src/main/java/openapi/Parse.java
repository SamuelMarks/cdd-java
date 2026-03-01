package openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 * Parses OpenAPI descriptions.
 */
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse OpenAPI description from string.
     * @param content The JSON string.
     * @return OpenAPI object.
     * @throws IOException If parsing fails.
     */
    public static OpenAPI fromString(String content) throws IOException {
        return mapper.readValue(content, OpenAPI.class);
    }

    /**
     * Parse OpenAPI description from file.
     * @param file The file.
     * @return OpenAPI object.
     * @throws IOException If parsing fails.
     */
    public static OpenAPI fromFile(File file) throws IOException {
        return mapper.readValue(file, OpenAPI.class);
    }
}
