package org.offscale;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Utils {

    /**
     * Gets a map between Types in OpenAPI and Types in Java. Going from OpenAPI -> Java.
     */
    public static ImmutableMap<String, String> getOpenAPIToJavaTypes() {
        return ImmutableMap.of(
                "string", "String",
                "int64", "long",
                "integer", "int",
                "int32", "int"
        );
    }

    /**
     * @param filePath of yaml file
     * @return JSONObject corresponding to openAPI spec from yaml file
     */
    public static <T> JSONObject getJSONObjectFromFile(String filePath, Class<T> getClass) {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass
                .getClassLoader()
                .getResourceAsStream(filePath);
        Map<String, Object> obj = yaml.load(inputStream);
        return new JSONObject(obj);
    }

    public static String readFileToString(String filePath) {
        try {
            return Files.
                    readString(Path.of(filePath));
        } catch (IOException e) {
            return "";
        }
    }
}
