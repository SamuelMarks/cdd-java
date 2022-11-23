package io.offscale;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;

/**
 * Given an OpenAPI Spec, generates code for routes and components and tests
 * when no code previously exists
 */
public class Create {
    private final JSONObject jo;
    private final ImmutableMap<String, Schema> schemas;
    public Create(String filePath) {
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
        this.schemas = generateSchemas();
    }

    public record RoutesAndTests(String routes, String tests, ImmutableMap<String, String> schemas) {}

    /**
     * Generates the classes corresponding to the components in the OpenAPI spec
     *
     * @return a map where the keys are the class names and the values are the class code
     */
    public ImmutableMap<String, String> generateComponents() {
        final ImmutableMap<String, Schema> generatedSchemas = generateSchemas();
        final HashMap<String, String> generatedComponents = new HashMap<>();
        generatedSchemas.forEach((name, schema) -> {
            if (schema.isObject()) {
                generatedComponents.put(name, schema.toCode());
            }
        });
        return ImmutableMap.copyOf(generatedComponents);
    }

    public ImmutableMap<String, Schema> generateSchemas() {
        final HashMap<String, Schema> generatedSchemas = new HashMap<>();
        final JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        final List<String> schemas = Lists.newArrayList(joSchemas.keys());
        for (int i = 0; generatedSchemas.size() < schemas.size(); i = (i + 1) % schemas.size()) {
            Schema generatedSchema = Schema.parseSchema(joSchemas.getJSONObject(schemas.get(i)), generatedSchemas, schemas.get(i));
            if (generatedSchema != null) {
                generatedSchemas.put(schemas.get(i), generatedSchema);
            }
        }

        return ImmutableMap.copyOf(generatedSchemas);
    }

    /**
     * @return generated routes, tests, and new classes to be created
     */
    public RoutesAndTests generateRoutesAndTests() {
        final JSONObject joPaths = jo.getJSONObject("paths");
        final List<String> paths = Lists.newArrayList(joPaths.keys());
        final HashMap<String, String> addedSchemas = new HashMap<>();
        final ClassOrInterfaceDeclaration routesInterface = new ClassOrInterfaceDeclaration()
                .setInterface(true).setName("Routes");
        final ClassOrInterfaceDeclaration testsClass = new ClassOrInterfaceDeclaration().setName("Tests");
        final MethodDeclaration runMethod = testsClass.addMethod(Utils.GET_METHOD_NAME);
        final MethodDeclaration runMethodWithBody = Utils.generateGetRequestMethod();

        final MethodDeclaration postMethod = testsClass.addMethod(Utils.POST_METHOD_NAME);
        final MethodDeclaration postMethodWithBody = Utils.generatePostRequestMethod();

        testsClass.addField("OkHttpClient", "client", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new OkHttpClient()");
        testsClass.addField("Gson", "gson", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new GsonBuilder().create()");
        testsClass.addField("String", "BASE_URL", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("\"" + getBaseURL() + "\"");
        testsClass.addField("MediaType", "JSON", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("MediaType.get(\"application/json; charset=utf-8\")");

        runMethod.setParameters(runMethodWithBody.getParameters());
        runMethod.setType(runMethodWithBody.getType());
        runMethodWithBody.getBody().ifPresent(runMethod::setBody);

        postMethod.setParameters(postMethodWithBody.getParameters());
        postMethod.setType(postMethodWithBody.getType());
        postMethodWithBody.getBody().ifPresent(postMethod::setBody);

        GenerateRoutesAndTestsUtils.setComponents(generateSchemas());

        for (final String path : paths) {
            for (final String operation : Lists.newArrayList(joPaths.getJSONObject(path).keys())) {
                final JSONObject joRoute = joPaths.getJSONObject(path).getJSONObject(operation);
                Schema routeResponseSchema = GenerateRoutesAndTestsUtils.generateRouteType(joRoute.getJSONObject("responses"), path, operation);
                GenerateRoutesAndTestsUtils.generateRoute(routesInterface, joRoute, routeResponseSchema.type(), path, operation);
                GenerateRoutesAndTestsUtils.generateTest(testsClass, joRoute, routeResponseSchema.type(), path, operation);
                if (routeResponseSchema.isObject() && !schemas.containsKey(routeResponseSchema.type())) {
                    addedSchemas.put(routeResponseSchema.type(), routeResponseSchema.toCode());
                }
            }
        }

        return new RoutesAndTests(routesInterface.toString(), testsClass.toString(), ImmutableMap.copyOf(addedSchemas));
    }

    /**
     * @return the base url for all http requests
     */
    private String getBaseURL() {
        return this.jo.getJSONArray("servers").getJSONObject(0).getString("url");
    }
}
