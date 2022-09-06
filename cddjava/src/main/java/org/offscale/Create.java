package org.offscale;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Given an OpenAPI Spec, generates code corresponding to the schemas and paths
 */
public class Create {
    private final JSONObject jo;
    private static final ImmutableMap<String, String> OPEN_API_TO_JAVA = Utils.getOpenAPIToJavaTypes();

    public Create(String filePath) {
        this.jo = getJSONObjectFromFile(filePath);
        generateClasses();
        generateRoutes();
    }

    /**
     * Given a filePath to a yaml file, returns a corresponding JSONObject
     *
     * @param filePath of yaml file
     */
    public JSONObject getJSONObjectFromFile(String filePath) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("test.yaml");
        Map<String, Object> obj = yaml.load(inputStream);
        return new JSONObject(obj);
    }

    /**
     * Generates the classes corresponding to the schemas in the JSON Object
     */
    public ImmutableList<String> generateClasses() {
        List<String> generatedClasses = new ArrayList<>();
        JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        List<String> schemas = Lists.newArrayList(joSchemas.keys());
        schemas.forEach((schema) -> generatedClasses.add(generateClass(joSchemas.getJSONObject(schema), schema)));
        return ImmutableList.copyOf(generatedClasses);
    }

    /**
     *
     * @return Routes interface which contains all the routes in the JSONObject
     */
    public String generateRoutes() {
        JSONObject joPaths = jo.getJSONObject("paths");
        List<String> paths = Lists.newArrayList(joPaths.keys());
        ClassOrInterfaceDeclaration myInterface = new ClassOrInterfaceDeclaration();
        myInterface.setInterface(true);
        myInterface.setName("Routes");
        for (String path : paths) {
            for (String operation : Lists.newArrayList(joPaths.getJSONObject(path).keys())) {
                generateRoute(myInterface, joPaths.getJSONObject(path).getJSONObject(operation), path, operation);
            }
        }
        System.out.println(myInterface);
        return myInterface.toString();
    }

    private void generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeName, String operation) {
        MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"));
        methodDeclaration.setJavadocComment(generateJavadocForRoute(joRoute, routeName, operation));
        NormalAnnotationExpr expr = methodDeclaration.addAndGetAnnotation(operation.toUpperCase());
        expr.addPair("path", "\"" + routeName + "\"");
        methodDeclaration.removeBody();
        methodDeclaration.setType(generateRouteType(joRoute.getJSONObject("responses")));
    }

    private String generateRouteType(JSONObject joRouteResponse) {
        StringBuilder type = new StringBuilder();
        List<String> responses = Lists.newArrayList(joRouteResponse.keys());
        responses.forEach(response -> {
            if (!response.equals("default")) {
                if (joRouteResponse.getJSONObject(response).has("content")) {

                } else {
                    type.append("void");
                }
            }
        });
        return type.toString();
    }

    /**
     *
     * @param joRoute
     * @param routeName
     * @param operation
     * @return javadoc
     */
    private String generateJavadocForRoute(JSONObject joRoute, String routeName, String operation) {
        JSONObject joResponses = joRoute.getJSONObject("responses");
        List<String> responses = Lists.newArrayList(joResponses.keys());
        StringBuilder javaDocForRoute = new StringBuilder(joRoute.getString("summary") + "\n");
        if (joRoute.has("parameters")) {
            for (int i = 0; i < joRoute.getJSONArray("parameters").length(); i++) {
                String param = "@param " + joRoute.getJSONArray("parameters").getJSONObject(i).getString("name") + "\n";
                javaDocForRoute.append(param);
            }
        }
        javaDocForRoute.append("@return ");
        responses.forEach((response) -> javaDocForRoute.append(generateJavadocReturn(joResponses.getJSONObject(response), response)));

        return javaDocForRoute.toString();
    }

    private String generateJavadocReturn(JSONObject joResponse, String responseName) {
        return joResponse.getString("description") + " (Status Code " + responseName + "), ";
    }

    /**
     * Given a JSON Object and the name of a class, generate a new class with properties defined in the JSON Object
     */
    private String generateClass(JSONObject joClass, String className) {
        JSONObject joProperties = joClass.getJSONObject("properties");
        List<String> properties = Lists.newArrayList(joProperties.keys());
        ClassOrInterfaceDeclaration myClass = new ClassOrInterfaceDeclaration();

        myClass.setName(className);
        // TODO: handle int64 and int32 differently
        properties.forEach((property) -> myClass.addField(OPEN_API_TO_JAVA.get(joProperties.getJSONObject(property).getString("type")), property));
        myClass.setJavadocComment("Howdy, gents?\n@param name of child");
        System.out.println(myClass);
        return myClass.toString();
    }


}
