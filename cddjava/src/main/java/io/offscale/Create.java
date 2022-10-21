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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given an OpenAPI Spec, generates code for routes and components
 * when no code previously exists
 */
public class Create {
    private final JSONObject jo;
    final Faker faker = new Faker();
    private static final String GET_METHOD_METHOD_NAME = "run";
    private static class Schema {
        private String type;
        private String name;
        private String description;
        private final String strictType;

        public Schema() {
            this.type = "object";
            this.strictType = "Object";
        }

        public Schema (final String type) {
            this.type = type;
            this.strictType = type;
        }

        public Schema(String type, String strictType) {
            this.type = type;
            this.strictType = strictType;
        }

        public Schema setName(String name) {
            this.name = name;
            return this;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Schema setDescription(String description) {
            this.description = description;
            return this;
        }
    }

    public Create(String filePath) {
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
    }

    /**
     * Generates the classes corresponding to the components in the OpenAPI spec
     * @return a map where the keys are the class names and the values are the class code
     */
    public ImmutableMap<String, String> generateComponents() {
        final HashMap<String, String> generatedComponents = new HashMap<>();
        final JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        final List<String> schemas = Lists.newArrayList(joSchemas.keys());
        schemas.forEach((schema) -> generatedComponents.put(schema, generateComponent(joSchemas.getJSONObject(schema), schema, null).get("type")));
        return ImmutableMap.copyOf(generatedComponents);
    }

    /**
     *
     * @param joComponent JSONObject for a component in the OpenAPI spec.
     * @param componentName name of the component to generate.
     * @param parentClass the parent class of a given component.
     * @return a Map containing two keys: type and strict type.
     * type will contain the final code generated for the component
     */
    private ImmutableMap<String, String> generateComponent(JSONObject joComponent, String componentName, ClassOrInterfaceDeclaration parentClass) {
        final Schema type = parseSchema(joComponent);
        if (type.type.equalsIgnoreCase("object")) {
            final ClassOrInterfaceDeclaration newClass = new ClassOrInterfaceDeclaration();
            final JSONObject joProperties = joComponent.getJSONObject("properties");
            final List<String> properties = Lists.newArrayList(joProperties.keys());
            newClass.setName(Utils.capitalizeFirstLetter(componentName));
            properties.forEach(property -> {
                ImmutableMap<String, String> propertyType = generateComponent(joProperties.getJSONObject(property), property, newClass);
                FieldDeclaration field = newClass.addField(propertyType.get("type"), property);
                field.setJavadocComment("Type of " + propertyType.get("strictType"));
            });
            if (parentClass == null) {
                return ImmutableMap.of("type", newClass.toString());
            }

            parentClass.addMember(newClass);
            return ImmutableMap.of("type", newClass.getNameAsString(), "strictType", newClass.getNameAsString());
        } else if (type.type.equals("array")) {
            if (parentClass == null) {
                final ClassOrInterfaceDeclaration newClass = new ClassOrInterfaceDeclaration();
                final String arrayType = generateComponent(joComponent.getJSONObject("items"), "ArrayType", newClass).get("type");
                newClass.setName(Utils.capitalizeFirstLetter(componentName));
                newClass.addField(arrayType + "[]", componentName + "Array");
                return ImmutableMap.of("type", newClass.toString());
            }
            final String arrayType = generateComponent(joComponent.getJSONObject("items"), "ArrayType", parentClass).get("type");
            return ImmutableMap.of("type", arrayType + "[]", "strictType", arrayType + "[]");
        } else {
            return ImmutableMap.of("type", parseSchema(joComponent).type, "strictType", parseSchema(joComponent).strictType);
        }
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
        testsClass.addField("OkHttpClient", "client",Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new OkHttpClient()");
        final MethodDeclaration runMethod = testsClass.addMethod(GET_METHOD_METHOD_NAME);
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
     * @param routesInterface
     * @param joRoute
     */
    private void generateTest(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute) {
        final String classType = generateRouteType(joRoute.getJSONObject("responses")).type;
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId") + "Test");
        methodDeclaration.addAnnotation("Test");
        final StringBuilder getURL = new StringBuilder("\"" + getBaseURL() + "?");
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters"))
                    .forEach(parameter -> getURL.append(generateMockDataForType(parameter)));
        }
        getURL.append("\"");
        final BlockStmt methodBody = new BlockStmt();
        final MethodCallExpr runCall = new MethodCallExpr();
        runCall.setName(GET_METHOD_METHOD_NAME);
        runCall.addArgument(getURL.toString());
        final FieldDeclaration getResponse = new FieldDeclaration();
        if (!classType.equals("void")) {
            final FieldDeclaration gson = new FieldDeclaration();
            final FieldDeclaration parsedResponse = new FieldDeclaration();

            Utils.initializeField(getResponse, "String", "getResponse", runCall + ".body().string()");
            Utils.initializeField(gson, "Gson", "gson", "new GsonBuilder().create()");
            Utils.initializeField(parsedResponse, Utils.getPrimitivesToClassTypes(classType),
                    "response", "gson.fromJson(getResponse, " + classType + ".class)");
            Utils.addDeclarationsToBlock(methodBody, getResponse, gson, parsedResponse);
        } else {
            Utils.initializeField(getResponse, "int", "statusCode", runCall + ".code()");
            final MethodCallExpr assertEqualsCall = new MethodCallExpr();
            assertEqualsCall.setName("assertEquals");
            assertEqualsCall.addArgument("statusCode");
            assertEqualsCall.addArgument("200");
            Utils.addDeclarationsToBlock(methodBody, getResponse);
            methodBody.addStatement(assertEqualsCall);
        }
        methodDeclaration.setBody(methodBody);
    }

    /**
     * Some parameter names are common such as firstname and can be
     * generated more precisely. This method handles parameters that
     * aren't common.
     * @param type of the parameter to generate mock data for
     * @return the mock data for the given parameter
     */
    private String generateMockDataForUnrecognizedName(String type) {
        return switch (type) {
            case "String" -> faker.food().fruit();
            case "long", "int" -> String.valueOf(faker.number().numberBetween(1, 100));
            case "boolean" -> "true";
            default -> "UNKNOWN";
        };
    }

    /**
     * @param schema for which to generate the mock data
     * @return the mock data for the given schema.
     */
    private String generateMockDataForType(Schema schema) {
        final String parameter = schema.name + "=";
        return switch (schema.name) {
            case "name" -> parameter + faker.name().name();
            case "fullname" -> parameter + faker.name().fullName();
            case "firstname" -> parameter + faker.name().firstName();
            case "lastname" -> parameter + faker.name().lastName();
            case "address" -> parameter + faker.address().fullAddress();
            default -> parameter +
                    generateMockDataForUnrecognizedName(schema.type);
        };
    }

    /**
     * generates the route code for a given route in the JSONObject.
     * @param routesInterface
     * @param joRoute
     * @param routeName
     * @param operation such as GET or POST
     */
    private MethodDeclaration generateRoute(ClassOrInterfaceDeclaration routesInterface, JSONObject joRoute, String routeName, String operation) {
        final MethodDeclaration methodDeclaration = routesInterface.addMethod(joRoute.getString("operationId"))
                .removeBody()
                .setJavadocComment(generateJavadocForRoute(joRoute, routeName, operation));
        final NormalAnnotationExpr expr = methodDeclaration.addAndGetAnnotation(operation.toUpperCase());
        expr.addPair("path", "\"" + routeName + "\"");
        final String routeType = generateRouteType(joRoute.getJSONObject("responses")).type;
        if (routeType.equals("void")) {
            methodDeclaration.setType("void");
        } else {
            methodDeclaration.setType("Call<" + routeType + ">");
        }
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(param -> methodDeclaration.addParameter(param.type, param.name));
        }
        return methodDeclaration;
    }

    /**
     *
     * @param joRouteParameters the openAPI representation of the route parameters
     * @return a List of route parameters
     */
    private List<Schema> generateRouteParameters(JSONArray joRouteParameters) {
        final List<Schema> routeParameters = new ArrayList<>();
        for (int i = 0; i < joRouteParameters.length(); i++) {
            final JSONObject joParameter = joRouteParameters.getJSONObject(i);
            final Schema parameter = parseSchema(joParameter.getJSONObject("schema"))
                    .setName(joParameter.getString("name"))
                    .setDescription(joParameter.getString("description"));
            routeParameters.add(parameter);
        }
        return routeParameters;
    }

    /**
     * Generates the return type of a route
     * @param joRouteResponse the OpenAPI representation of the route response.
     * @return the type of the route
     */
    private Schema generateRouteType(JSONObject joRouteResponse) {
        final List<String> responses = Lists.newArrayList(joRouteResponse.keys());
        final Optional<String> response = responses.stream().filter(r -> r.equals("200")).findFirst();
        if (response.isPresent() && joRouteResponse.getJSONObject(response.get()).has("content")) {
            return parseSchema(joRouteResponse.getJSONObject(response.get()).getJSONObject("content").getJSONObject("application/json").getJSONObject("schema"));
        }
        return new Schema("void");
    }

    /**
     *
     * @param joSchema which is essentially a type
     * @return a Parameter with type information
     */
    private Schema parseSchema(JSONObject joSchema) {
        if (joSchema.has("$ref")) {
            return new Schema(parseSchemaRef(joSchema.getString("$ref")));
        }

        else if (joSchema.has("format")) {
            return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("format")), joSchema.getString("format"));
        }

        else if (!joSchema.has("type")) {
            return new Schema();
        }

        return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("type"));
    }


    /**
     * Uses regex to parse out the component name in the reference.
     * @param ref of a schema, maps to a component
     * @return the component name
     */
    private String parseSchemaRef(String ref) {
        final Pattern pattern = Pattern.compile("#/components/schemas/(\\w+)");
        final Matcher matcher = pattern.matcher(ref);
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
        final JSONObject joResponses = joRoute.getJSONObject("responses");
        final List<String> responses = Lists.newArrayList(joResponses.keys());
        final StringBuilder javaDocForRoute = new StringBuilder(joRoute.getString("summary") + "\n");
        if (joRoute.has("parameters")) {
            generateRouteParameters(joRoute.getJSONArray("parameters")).forEach(parameter -> {
                final String param = "@param " + parameter.name + " of type " + parameter.strictType + ". " + parameter.description + ". \n";
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
