package org.offscale;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given an OpenAPI Spec, generates code for routes and components
 * when no code previously exists
 */
public class Create {
    private final JSONObject jo;
    private static final ImmutableMap<String, String> OPEN_API_TO_JAVA = Utils.getOpenAPIToJavaTypes();

    private class Parameter {
        private String type;
        private String name;

        private String description;

        private String strictType;

        public Parameter (String type) {
            this.type = type;
        }

        public Parameter(String type, String strictType) {
            this.type = type;
            this.strictType = strictType;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public Create(String filePath) {
        this.jo = getJSONObjectFromFile(filePath);
    }



    /**
     * @param filePath of yaml file
     * @return JSONObject corresponding to openAPI spec from yaml file
     */
    public JSONObject getJSONObjectFromFile(String filePath) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filePath);
        Map<String, Object> obj = yaml.load(inputStream);
        return new JSONObject(obj);
    }

    /**
     * Generates the classes corresponding to the components in the OpenAPI spec
     * @return a map where the keys are the class names and the values are the class code
     */
    public ImmutableMap<String, String> generateComponents() {
        HashMap<String, String> generatedComponents = new HashMap<>();
        JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        List<String> schemas = Lists.newArrayList(joSchemas.keys());
        schemas.forEach((schema) -> generatedComponents.put(schema, generateComponent(joSchemas.getJSONObject(schema), schema)));
        return ImmutableMap.copyOf(generatedComponents);
    }

    /**
     * Generates code for a component.
     * @param joComponent JSONObject for a component in the OpenAPI spec.
     * @param componentName name of the component to generate.
     * @return a String containing the generated code for a component.
     */
    private String generateComponent(JSONObject joComponent, String componentName) {
        JSONObject joProperties = joComponent.getJSONObject("properties");
        List<String> properties = Lists.newArrayList(joProperties.keys());
        ClassOrInterfaceDeclaration myComponent = new ClassOrInterfaceDeclaration();

        myComponent.setName(componentName);
        properties.forEach((property) -> myComponent.addField(OPEN_API_TO_JAVA.get(joProperties.getJSONObject(property).getString("type")), property));
        return myComponent.toString();
    }

    /**
     * @return Routes interface containing routes from OpenAPI Spec
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

    /**
     *
     * @param routesInterface
     * @param joRoute
     * @param routeName
     * @param operation
     */
    private void generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeName, String operation) {
        MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"));
        methodDeclaration.setJavadocComment(generateJavadocForRoute(joRoute, routeName, operation));
        NormalAnnotationExpr expr = methodDeclaration.addAndGetAnnotation(operation.toUpperCase());
        expr.addPair("path", "\"" + routeName + "\"");
        methodDeclaration.removeBody();
        methodDeclaration.setType(generateRouteType(joRoute.getJSONObject("responses")));
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(param -> methodDeclaration.addParameter(param.type, param.name));
        }
    }

    /**
     *
     * @param joRouteParameters the openAPI representation of the route parameters
     * @return a List of route parameters
     */
    private List<Parameter> generateRouteParameters(JSONArray joRouteParameters) {
        List<Parameter> routeParameters = new ArrayList<>();
        for (int i = 0; i < joRouteParameters.length(); i++) {
            JSONObject joParameter = joRouteParameters.getJSONObject(i);
            Parameter parameter = parseSchema(joParameter.getJSONObject("schema"));
            parameter.setName(joParameter.getString("name"));
            parameter.setDescription(joParameter.getString("description"));
            routeParameters.add(parameter);
        }
        return routeParameters;
    }

    /**
     * Generates the return type of a route
     * @param joRouteResponse the OpenAPI representation of the route response.
     * @return the type of the route
     */
    private String generateRouteType(JSONObject joRouteResponse) {
        StringBuilder type = new StringBuilder();
        List<String> responses = Lists.newArrayList(joRouteResponse.keys());
        responses.forEach(response -> {
            if (!response.equals("default")) {
                if (joRouteResponse.getJSONObject(response).has("content")) {
                    Parameter parameter = parseSchema(joRouteResponse.getJSONObject(response).getJSONObject("content").getJSONObject("application/json").getJSONObject("schema"));
                    parameter.setType("Call<" + parameter.type + ">");
                    type.append(parameter.type);
                }
            }
        });
        if (type.isEmpty()) {
            return "void";
        } else {
            return type.toString();
        }
    }

    /**
     *
     * @param schema which is essentially a type
     * @return a Parameter with type information
     */
    private Parameter parseSchema(JSONObject schema) {
        if (schema.has("$ref")) {
            return new Parameter(parseSchemaRef(schema.getString("$ref")));
        }

        if (schema.has("format")) {
            return new Parameter(Utils.getOpenAPIToJavaTypes().get(schema.get("format")), schema.getString("format"));
        }

        return new Parameter(Utils.getOpenAPIToJavaTypes().get(schema.get("type")), schema.getString("type"));
    }

    /**
     * Uses regex to parse out the component name in the reference.
     * @param ref of a schema, maps to a component
     * @return the component name
     */
    private String parseSchemaRef(String ref) {
        Pattern pattern = Pattern.compile("#/components/schemas/(\\w+)");
        Matcher matcher = pattern.matcher(ref);
        matcher.find();
        return matcher.group(1);
    }

    /**
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
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(parameter -> {
                String param = "@param " + parameter.name + " of type " + parameter.strictType + ". " + parameter.description + ". \n";
                javaDocForRoute.append(param);
            });
        }
        javaDocForRoute.append("@return ");
        responses.forEach((response) -> javaDocForRoute.append(generateJavadocReturn(joResponses.getJSONObject(response), response)));

        return javaDocForRoute.toString();
    }

    private String generateJavadocReturn(JSONObject joResponse, String responseName) {
        return joResponse.getString("description") + " (Status Code " + responseName + "), ";
    }
}
