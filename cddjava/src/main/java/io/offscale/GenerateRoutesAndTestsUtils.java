package io.offscale;

import com.github.javafaker.Faker;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateRoutesAndTestsUtils {
    private static final Faker faker = new Faker();
    private static record Response(Schema schema, String description) { }

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
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(param -> methodDeclaration.addParameter(param.schema.type(), param.schema.name()));
        }
        return methodDeclaration;
    }

    /**
     * @param joRouteParameters the openAPI representation of the route parameters
     * @return a List of route parameters
     */
    private static List<Response> generateRouteParameters(JSONArray joRouteParameters) {
        final List<Response> routeParameters = new ArrayList<>();
        for (int i = 0; i < joRouteParameters.length(); i++) {
            final JSONObject joParameter = joRouteParameters.getJSONObject(i);
            final Schema schema = new Schema(Schema.parseSchema(joParameter.getJSONObject("schema")), joParameter.getString("name"));
            final Response response = new Response(schema, joParameter.getString("description"));
            routeParameters.add(response);
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
            return Schema.parseSchema(joRouteResponse.getJSONObject(response.get()).getJSONObject("content").getJSONObject("application/json").getJSONObject("schema"));
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
                final String param = "@param " + parameter.schema.name() + " of type " + parameter.schema.strictType() + ". " + parameter.description + ". \n";
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
    public static void generateTest(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute) {
        final String classType = generateRouteType(joRoute.getJSONObject("responses")).type();
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId") + "Test");
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr runCall = new MethodCallExpr();
        final StringBuilder getURLParams = new StringBuilder("\"");
        final FieldDeclaration getResponse = new FieldDeclaration();
        final MethodCallExpr assertEqualsCall = new MethodCallExpr();

        methodDeclaration.addAnnotation("Test");

        runCall.setName(Utils.GET_METHOD_NAME);
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters"))
                    .forEach(parameter -> getURLParams.append(generateMockDataForType(parameter.schema)));
            getURLParams.append("\"");
            runCall.addArgument("BASE_URL + " + getURLParams);
        } else {
            runCall.addArgument("BASE_URL");
        }

        Utils.initializeField(getResponse, "Response", "getResponse", runCall.toString());
        Utils.addDeclarationsToBlock(methodBody, getResponse);

        assertEqualsCall.setName("assertEquals");
        assertEqualsCall.addArgument("getResponse.code()");
        assertEqualsCall.addArgument("200");
        methodBody.addStatement(assertEqualsCall);

        if (!classType.equals("void")) {
            final FieldDeclaration parsedResponse = new FieldDeclaration();

            Utils.initializeField(parsedResponse, Utils.getPrimitivesToClassTypes(classType),
                    "response", "gson.fromJson(getResponse.body().string(), " + classType + ".class)");
            Utils.addDeclarationsToBlock(methodBody, parsedResponse);
        }
        methodDeclaration.setBody(methodBody);
    }

    /**
     * Some parameter names are common such as firstname and can be
     * generated more precisely. This method handles parameters that
     * aren't common.
     *
     * @param type of the parameter to generate mock data for
     * @return the mock data for the given parameter
     */
    private static String generateMockDataForUnrecognizedName(String type) {
        return switch (type) {
            case "String" -> faker.food().fruit();
            case "long", "int" -> String.valueOf(faker.number().numberBetween(1, 100));
            case "boolean" -> faker.bool().toString();
            default -> "UNKNOWN";
        };
    }

    /**
     * @param schema for which to generate the mock data
     * @return the mock data for the given schema.
     */
    private static String generateMockDataForType(Schema schema) {
        final String parameter = schema.name() + "=";
        return switch (schema.name()) {
            case "name" -> parameter + faker.name().name();
            case "fullname" -> parameter + faker.name().fullName();
            case "firstname" -> parameter + faker.name().firstName();
            case "lastname" -> parameter + faker.name().lastName();
            case "address" -> parameter + faker.address().fullAddress();
            default -> parameter +
                    generateMockDataForUnrecognizedName(schema.type());
        };
    }
}
