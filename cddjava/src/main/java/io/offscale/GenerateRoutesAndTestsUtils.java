package io.offscale;

import com.github.javafaker.Faker;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

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
    public static MethodDeclaration generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeType, String routeName, String operation) {
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"))
                .removeBody()
                .setJavadocComment(generateJavadocForRoute(joRoute));
        final NormalAnnotationExpr expr = methodDeclaration.addAndGetAnnotation(operation.toUpperCase());
        expr.addPair("path", "\"" + routeName + "\"");
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
    public static Schema generateRouteType(JSONObject joRouteResponse, String path, String operation) {
        if (joRouteResponse.has("200")) {
            JSONObject successResponse = joRouteResponse.getJSONObject("200");
            assert successResponse.has("content");
            return Schema.parseSchema(
                    successResponse.getJSONObject("content").getJSONObject("application/json").getJSONObject("schema"),
                    components,
                    Utils.capitalizeFirstLetter((path + operation).replaceAll("[^a-zA-Z0-9]", "")));
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
        final StringBuilder javaDocForRoute = new StringBuilder();
        if (joRoute.has("summary")) {
            javaDocForRoute.append(joRoute.getString("summary") + "\n");
        }

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
        if (joResponse.has("description")) {
            return joResponse.getString("description") + " (Status Code " + responseName + "), ";
        }
        return "(Status Code " + responseName + "), ";
    }

    /**
     * Given a route, generates a test corresponding the route.
     * @param routesInterface
     * @param joRoute
     */
    public static void generateTest(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String responseType, String path, String operation) {
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId") + "Test");

        methodDeclaration.addAnnotation("Test");

        switch (operation) {
            case "get":
                methodDeclaration.setBody(generateGetRequestTest(generateURL(joRoute, path), responseType));
                break;
            case "post":
                methodDeclaration.setBody(generatePostRequestTest(joRoute, generateURL(joRoute, path), responseType));
                break;
            default: assert false;
        }
    }

    /**
     * @param url to generate the get request test
     * @param responseType of the get request
     * @return code representing the generated test
     */
    private static BlockStmt generateGetRequestTest(String url, String responseType) {
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr getCall = new MethodCallExpr();
        final FieldDeclaration getResponse = new FieldDeclaration();
        final FieldDeclaration parsedResponse = new FieldDeclaration();

        getCall.setName(Utils.GET_METHOD_NAME);
        getCall.addArgument(url);

        return handleTestResponse(responseType, methodBody, getCall, getResponse, parsedResponse);
    }

    /**
     * @param joRoute to generate the post request test
     * @param url to generate the post request test
     * @param responseType of the post of the post request
     * @return code representing the generated test.
     */
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

    /**
     *
     * @param responseType of the api call
     * @param methodBody for which to add the code
     * @param requestCall http call for the test
     * @param response response of the requestCall
     * @param parsedResponse to check if the response is parsable into the expected type.
     * @return the code to check if the response has 200 status code and has a response of the right form.
     */
    private static BlockStmt handleTestResponse(String responseType, BlockStmt methodBody, MethodCallExpr requestCall, FieldDeclaration response, FieldDeclaration parsedResponse) {
        Utils.initializeField(response, "Response", "response", requestCall.toString());
        Utils.addDeclarationsToBlock(methodBody, response);
        methodBody.addStatement(generateAssertEquals());

        if (!responseType.equals("void")) {
            MethodCallExpr parseResponse = new MethodCallExpr("gson.fromJson").addArgument("response.body().string()").addArgument(responseType + ".class");
            parseResponse.addOrphanComment(new BlockComment("Testing if the response can be parsed into the expected type"));
            methodBody.addOrphanComment(new BlockComment("Testing if the response can be parsed into the expected type"));
            methodBody.addStatement(parseResponse);
        }

        return methodBody;
    }

    /**
     * @param joRoute from which to generate the URL
     * @param path of the api call
     * @return the generated url with all necessary parameters
     */
    private static String generateURL(JSONObject joRoute, String path) {
        final StringBuilder url = new StringBuilder("BASE_URL");
        final StringBuilder getURLParams = new StringBuilder();
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters"))
                    .forEach(parameter -> getURLParams.append(parameter.name() + "=" + generateMockDataForType(parameter)));
            url.append(" + " + "\"" + path.substring(0, indexOfParams(path)) + "?" + getURLParams + "\"");
        }

        return url.toString();
    }

    /**
     *
     * @param path of the api call
     * @return the index of the first '{' if there is one.
     * If there isn't than return the string length. The logic
     * behind this is that we want the api path without the params
     * because those will be added after.
     */
    private static int indexOfParams(String path) {
        if (path.indexOf('{') != -1) {
            return path.indexOf('{');
        }

        return path.length();
    }

    /**
     * Small method to generate the assertEquals(200, response.code())
     * @return the assertEquals method call.
     */
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
