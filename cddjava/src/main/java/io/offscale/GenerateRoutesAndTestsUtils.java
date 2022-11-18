package io.offscale;

import com.github.javafaker.Faker;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateRoutesAndTestsUtils {
    private static final Faker faker = new Faker();
    private static record Parameter(Schema schema, String name, String description) { }

    private static ImmutableMap<String, Schema> components;

    public static void setComponents(ImmutableMap<String, Schema> generatedComponents) {
        components = generatedComponents;
    }

    /**
     * generates the route code for a given route in the JSONObject.
     *
     * @param routesInterface
     * @param joRoute
     * @param routeName
     * @param operation such as GET or POST
     */
    public static MethodDeclaration generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeName, String operation) {
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"))
                .removeBody()
                .setJavadocComment(generateJavadocForRoute(joRoute));
        final NormalAnnotationExpr expr = methodDeclaration.addAndGetAnnotation(operation.toUpperCase());
        expr.addPair("path", "\"" + routeName + "\"");
        final String routeType = generateRouteType(joRoute.getJSONObject("responses")).type();
        if (routeType.equals("void")) {
            methodDeclaration.setType("void");
        } else {
            methodDeclaration.setType("Call<" + routeType + ">");
        }
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(param -> methodDeclaration.addParameter(param.schema.type(), param.name()));
        }
        return methodDeclaration;
    }

    /**
     * @param joRouteParameters the openAPI representation of the route parameters
     * @return a List of route parameters
     */
    private static List<Parameter> generateRouteParameters(JSONArray joRouteParameters) {
        final List<Parameter> routeParameters = new ArrayList<>();
        for (int i = 0; i < joRouteParameters.length(); i++) {
            final JSONObject joParameter = joRouteParameters.getJSONObject(i);
            final Parameter parameter = new Parameter(Schema.parseSchema(joParameter.getJSONObject("schema"), components, ""),
                    joParameter.getString("name"), joParameter.getString("description"));
            routeParameters.add(parameter);
        }
        return routeParameters;
    }

    /**
     * Generates the return type of a route
     *
     * @param joRouteResponse the OpenAPI representation of the route response.
     * @return the type of the route
     */
    private static Schema generateRouteType(JSONObject joRouteResponse) {
        final List<String> responses = Lists.newArrayList(joRouteResponse.keys());
        final Optional<String> response = responses.stream().filter(r -> r.equals("200")).findFirst();
        if (response.isPresent() && joRouteResponse.getJSONObject(response.get()).has("content")) {
            JSONObject joRouteResponseSchema = joRouteResponse.getJSONObject(response.get()).getJSONObject("content").getJSONObject("application/json").getJSONObject("schema");
            return Schema.parseSchema(joRouteResponseSchema, components, "" );
        }

        return new Schema("void");
    }

    /**
     * @param joRoute JSON Object of route
     * @return javadoc for given route
     */
    private static String generateJavadocForRoute(JSONObject joRoute) {
        final JSONObject joResponses = joRoute.getJSONObject("responses");
        final List<String> responses = Lists.newArrayList(joResponses.keys());
        final StringBuilder javaDocForRoute = new StringBuilder(joRoute.getString("summary") + "\n");
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(parameter -> {
                final String param = "@param " + parameter.name() + " of type " + parameter.schema.strictType() + ". " + parameter.description + ". \n";
                javaDocForRoute.append(param);
            });
        }
        javaDocForRoute.append("@return ");
        responses.forEach((response) -> javaDocForRoute.append(generateJavadocReturn(joResponses.getJSONObject(response), response)));

        return javaDocForRoute.toString();
    }

    /**
     * @param joResponse
     * @param responseName
     * @return the javadoc return statement
     */
    private static String generateJavadocReturn(JSONObject joResponse, String responseName) {
        return joResponse.getString("description") + " (Status Code " + responseName + "), ";
    }

    /**
     * Given a route, generates a test corresponding the route.
     *
     * @param routesInterface
     * @param joRoute
     */
    public static void generateTest(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String path, String operation) {
        final String responseType = generateRouteType(joRoute.getJSONObject("responses")).type();
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId") + "Test");

        methodDeclaration.addAnnotation("Test");

        switch (operation) {
            case "get":
                methodDeclaration.setBody(generateGetRequestTest(joRoute, generateURL(joRoute, path), responseType));
                break;
            case "post":
                methodDeclaration.setBody(generatePostRequestTest(joRoute, generateURL(joRoute, path), responseType));
                break;
            default: assert false;
        }
    }

    private static BlockStmt generateGetRequestTest(JSONObject joRoute, String url, String responseType) {
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr getCall = new MethodCallExpr();
        final FieldDeclaration getResponse = new FieldDeclaration();
        final FieldDeclaration parsedResponse = new FieldDeclaration();

        getCall.setName(Utils.GET_METHOD_NAME);
        getCall.addArgument(url);

        return handleTestResponse(responseType, methodBody, getCall, getResponse, parsedResponse);
    }

    private static BlockStmt generatePostRequestTest(JSONObject joRoute, String url, String responseType) {
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr postCall = new MethodCallExpr();
        final FieldDeclaration postResponse = new FieldDeclaration();
        final FieldDeclaration requestBodyData = new FieldDeclaration();
        final FieldDeclaration parsedResponse = new FieldDeclaration();
        Utils.initializeField(requestBodyData, "String", "requestBody", generateRequestBody(joRoute));
        Utils.addDeclarationsToBlock(methodBody, requestBodyData);

        postCall.setName(Utils.POST_METHOD_NAME);
        postCall.addArgument(url);
        postCall.addArgument("requestBody");

        return handleTestResponse(responseType, methodBody, postCall, postResponse, parsedResponse);
    }

    private static BlockStmt handleTestResponse(String responseType, BlockStmt methodBody, MethodCallExpr postCall, FieldDeclaration postResponse, FieldDeclaration parsedResponse) {
        Utils.initializeField(postResponse, "Response", "response", postCall.toString());
        Utils.addDeclarationsToBlock(methodBody, postResponse);
        methodBody.addStatement(generateAssertEquals());

        if (!responseType.equals("void")) {
            Utils.initializeField(parsedResponse, Utils.getPrimitivesToClassTypes(responseType),
                    "response", "gson.fromJson(response.body().string(), " + responseType + ".class)");
            Utils.addDeclarationsToBlock(methodBody, parsedResponse);
        }

        return methodBody;
    }

    private static String generateURL(JSONObject joRoute, String path) {
        final StringBuilder url = new StringBuilder("BASE_URL");
        final StringBuilder getURLParams = new StringBuilder("\"");
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters"))
                    .forEach(parameter -> getURLParams.append(parameter.name() + "=" + generateMockDataForType(parameter)));
            getURLParams.append("\"");
            url.append(" + " + getURLParams.toString());
        }

        return url.toString();
    }

    private static MethodCallExpr generateAssertEquals() {
        final MethodCallExpr assertEqualsCall = new MethodCallExpr();
        assertEqualsCall.setName("assertEquals");
        assertEqualsCall.addArgument("response.code()");
        assertEqualsCall.addArgument("200");

        return assertEqualsCall;
    }

    /**
     * @param joRoute to generate the request body from.
     * @return A string with either the request body or an empty string if there is no request body.
     */
    private static String generateRequestBody(JSONObject joRoute) {
        if (joRoute.has("requestBody")) {
            JSONObject joSchema = joRoute.getJSONObject("requestBody").getJSONObject("content")
                    .getJSONObject("application/json").getJSONObject("schema");
            return "\"" + convertSchemaToJSON(Schema.parseSchema(joSchema, components, ""), "") + "\"";
        }

        return "\"\"";
    }

    /**
     * @param schema object to convert into json
     * @param name of the schema object to convert to json. Useful for common names such as "address" or "fullname"
     * @return the json as a string.
     */
    private static String convertSchemaToJSON(Schema schema, String name) {
        if (schema.isObject()) {
            StringBuilder json = new StringBuilder();
            json.append('{');
            int i = 0;
            for (String key: schema.properties().keySet()) {
                if (i == schema.properties().size()-1) {
                    json.append(key + ": " + convertSchemaToJSON(schema.properties().get(key), key));
                } else {
                    json.append(key + ": " + convertSchemaToJSON(schema.properties().get(key), key) + ", ");
                }
                i++;
            }
            json.append('}');
            return json.toString();
        }

        if (schema.isArray()) {
            StringBuilder json = new StringBuilder();
            json.append('[');
            json.append(convertSchemaToJSON(schema.arrayOfType(), ""));
            json.append(']');
            return json.toString();
        }

        if (schema.type().equals("String")) {
            return "\\\"" + generateMockDataForType(new Parameter(schema, name, "")) + "\\\"";
        }

        return generateMockDataForType(new Parameter(schema, name, ""));
    }

    /**
     * @param parameter for which to generate the mock data
     * @return the mock data for the given schema.
     */
    private static String generateMockDataForType(Parameter parameter) {
        return switch (parameter.name()) {
            case "name" -> faker.name().name();
            case "fullname" -> faker.name().fullName();
            case "firstname" -> faker.name().firstName();
            case "lastname" -> faker.name().lastName();
            case "address" -> faker.address().fullAddress();
            default -> generateMockDataForUnrecognizedName(parameter.schema().type());
        };
    }

    /**
     * Some parameter names are common such as firstname and can be
     * generated more precisely. This method handles parameters that
     * aren't common.
     *
     * @param type of the parameter for which to generate mock data
     * @return the mock data for the given parameter
     */
    private static String generateMockDataForUnrecognizedName(String type) {
        return switch (type) {
            case "String" -> faker.food().fruit();
            case "long", "int" -> String.valueOf(faker.number().numberBetween(1, 100));
            case "boolean" -> Boolean.toString(faker.bool().bool());
            default -> throw new IllegalArgumentException("type wasn't one of the expected values.");
        };
    }
}
