package io.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class Utils {
    private static final String GET_METHOD = """
                public class Test {
                    Response get(String url) throws IOException {
                      Request request = new Request.Builder()
                          .url(url)
                          .build();
                                    
                      return client.newCall(request).execute();
                    }
                }
                """;

    private static final String POST_METHOD = """
                public class Test {
                    Response post(String url, String json) throws IOException {
                      RequestBody body = RequestBody.create(json, JSON);
                      Request request = new Request.Builder()
                          .url(url)
                          .post(body)
                          .build();
                      return client.newCall(request).execute();
                    }
                }
                """;
    public static final String GET_METHOD_NAME = "get";
    public static final String POST_METHOD_NAME = "post";
    /**
     * Gets a map between Types in OpenAPI and Types in Java. Going from OpenAPI -> Java.
     */
    public static ImmutableMap<String, String> getOpenAPIToJavaTypes() {
        return ImmutableMap.<String, String>builder()
                .put("string", "String")
                .put("int64", "long")
                .put("integer", "int")
                .put("int32", "int")
                .put("object", "object")
                .put("boolean", "boolean")
                .put("array", "array")
                .build();
    }

    public static String getPrimitivesToClassTypes(String primitive) {
        return switch (primitive) {
            case "int" -> "Integer";
            case "long" -> "Long";
            default -> primitive;
        };
    }

    public static void addDeclarationsToBlock(BlockStmt block, FieldDeclaration... declarations) {
        for (final FieldDeclaration declaration: declarations) {
            block.addStatement(declaration.toString());
        }
    }

    public static void initializeField(FieldDeclaration field, String type, String name, String init) {
        field.addVariable(new VariableDeclarator().setType(type).setName(name).setInitializer(init));
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

    public static MethodDeclaration generateGetRequestMethod() throws AssertionError {
        return generateHttpRequestMethod(GET_METHOD, GET_METHOD_NAME);
    }

    public static MethodDeclaration generatePostRequestMethod() throws AssertionError {
        return generateHttpRequestMethod(POST_METHOD, POST_METHOD_NAME);
    }

    private static MethodDeclaration generateHttpRequestMethod(String method, String methodName) throws AssertionError {
        final Optional<ClassOrInterfaceDeclaration> classObj = StaticJavaParser.parse(method).getClassByName("Test");
        assert(classObj.isPresent());
        return classObj.get().getMethodsByName(methodName).get(0);
    }

    public static String capitalizeFirstLetter(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
