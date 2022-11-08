package io.offscale;

import com.github.javafaker.Faker;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Given an OpenAPI Spec, generates code for routes and components and tests
 * when no code previously exists
 */
public class Create {
    private final JSONObject jo;
    private static final Faker faker = new Faker();
    private static final String GET_METHOD_NAME = "run";

    private record Response(Schema schema, String description) {
    }

    public Create(String filePath) {
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
    }

    /**
     * Generates the classes corresponding to the components in the OpenAPI spec
     *
     * @return a map where the keys are the class names and the values are the class code
     */
    public ImmutableMap<String, String> generateComponents() {
        final HashMap<String, String> generatedComponents = new HashMap<>();
        final JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        final List<String> schemas = Lists.newArrayList(joSchemas.keys());
        schemas.forEach((schema) -> generatedComponents
                .put(schema, CreateComponentUtils.generateComponent(joSchemas.getJSONObject(schema), schema, null).code()));
        return ImmutableMap.copyOf(generatedComponents);
    }


    /**
     * @return Map with two key-value pairs: routes and tests.
     * routes is the code for the generated routes interface.
     * tests is the code for generated tests for all routes.
     */
    public ImmutableMap<String, String> generateRoutesAndTests() {
        final HashMap<String, String> routesAndTests = new HashMap<>();
        final JSONObject joPaths = jo.getJSONObject("paths");
        final List<String> paths = Lists.newArrayList(joPaths.keys());
        final ClassOrInterfaceDeclaration routesInterface = new ClassOrInterfaceDeclaration()
                .setInterface(true).setName("Routes");
        final ClassOrInterfaceDeclaration testsClass = new ClassOrInterfaceDeclaration().setName("Tests");
        testsClass.addField("OkHttpClient", "client", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new OkHttpClient()");
        testsClass.addField("Gson", "gson", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new GsonBuilder().create()");
        testsClass.addField("String", "BASE_URL", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("\"" + getBaseURL() + "?\"");
        final MethodDeclaration runMethod = testsClass.addMethod(GET_METHOD_NAME);
        final MethodDeclaration runMethodWithBody = Utils.generateGetRequestMethod();
        runMethod.setParameters(runMethodWithBody.getParameters());
        runMethod.setType(runMethodWithBody.getType());
        final Optional<BlockStmt> body = runMethodWithBody.getBody();
        body.ifPresent(runMethod::setBody);

        for (final String path : paths) {
            for (final String operation : Lists.newArrayList(joPaths.getJSONObject(path).keys())) {
                generateRoute(routesInterface, joPaths.getJSONObject(path).getJSONObject(operation), path, operation);
                generateTest(testsClass, joPaths.getJSONObject(path).getJSONObject(operation));
            }
        }
        routesAndTests.put("routes", routesInterface.toString());
        routesAndTests.put("tests", testsClass.toString());
        return ImmutableMap.copyOf(routesAndTests);
    }

    /**
     * @return the base url for all http requests
     */
    private String getBaseURL() {
        return this.jo.getJSONArray("servers").getJSONObject(0).getString("url");
    }

    /**
     * Given a route, generates a test corresponding the route.
     *
     * @param routesInterface
     * @param joRoute
     */
    private void generateTest(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute) {
        final String classType = generateRouteType(joRoute.getJSONObject("responses")).type();
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId") + "Test");
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr runCall = new MethodCallExpr();
        final StringBuilder getURLParams = new StringBuilder("\"");
        final FieldDeclaration getResponse = new FieldDeclaration();
        final MethodCallExpr assertEqualsCall = new MethodCallExpr();

        methodDeclaration.addAnnotation("Test");

        runCall.setName(GET_METHOD_NAME);
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
    private String generateMockDataForUnrecognizedName(String type) {
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
    private String generateMockDataForType(Schema schema) {
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

    /**
     * generates the route code for a given route in the JSONObject.
     *
     * @param routesInterface
     * @param joRoute
     * @param routeName
     * @param operation       such as GET or POST
     */
    private MethodDeclaration generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeName, String operation) {
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"))
                .removeBody()
                .setJavadocComment(generateJavadocForRoute(joRoute, routeName, operation));
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
    private List<Response> generateRouteParameters(JSONArray joRouteParameters) {
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
    private Schema generateRouteType(JSONObject joRouteResponse) {
        final List<String> responses = Lists.newArrayList(joRouteResponse.keys());
        final Optional<String> response = responses.stream().filter(r -> r.equals("200")).findFirst();
        if (response.isPresent() && joRouteResponse.getJSONObject(response.get()).has("content")) {
            return Schema.parseSchema(joRouteResponse.getJSONObject(response.get()).getJSONObject("content").getJSONObject("application/json").getJSONObject("schema"));
        }
        return new Schema("void");
    }

    /**
     * @param joRoute
     * @param routeName
     * @param operation
     * @return javadoc
     */
    private String generateJavadocForRoute(JSONObject joRoute, String routeName, String operation) {
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
    private String generateJavadocReturn(JSONObject joResponse, String responseName) {
        return joResponse.getString("description") + " (Status Code " + responseName + "), ";
    }
}
