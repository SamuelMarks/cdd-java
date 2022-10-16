package org.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * Gets a map between Types in OpenAPI and Types in Java. Going from OpenAPI -> Java.
     */
    public static ImmutableMap<String, String> getOpenAPIToJavaTypes() {
        HashMap<String, String> openAPIToJavaTypes = new HashMap<>();
        openAPIToJavaTypes.put("string", "String");
        openAPIToJavaTypes.put("int64", "long");
        openAPIToJavaTypes.put("integer", "int");
        openAPIToJavaTypes.put("int32", "int");
        openAPIToJavaTypes.put("object", "Object");
        openAPIToJavaTypes.put("boolean", "boolean");
        openAPIToJavaTypes.put("array", "array");

        return ImmutableMap.copyOf(openAPIToJavaTypes);
    }

    public static String getPrimitivesToClassTypes(String primitive) {
        switch (primitive) {
            case "int": return "Integer";
            case "long": return "Long";
            default: return primitive;
        }
    }

    public static void addDeclarationsToBlock(BlockStmt block, FieldDeclaration... declarations) {
        for (FieldDeclaration declaration: declarations) {
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

    public static MethodDeclaration generateGetRequestMethod() {
        String method = """
                public class Test {
                    Response run(String url) throws IOException {
                      Request request = new Request.Builder()
                          .url(url)
                          .build();
                                    
                      return client.newCall(request).execute();
                    }
                }
                """;
        return StaticJavaParser.parse(method).getClassByName("Test").get()
                .getMethodsByName("run").get(0);
    }

    public static String capitalizeFirstLetter(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
