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
    public Create(String filePath) {
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
    }

    /**
     * Generates the classes corresponding to the components in the OpenAPI spec
     *
     * @return a map where the keys are the class names and the values are the class code
     */
    public ImmutableMap<String, String> generateComponents() {
        final ImmutableMap<String, Schema> generatedSchemas = generateSchemas();
        final HashMap<String, String> generatedComponents = new HashMap<>();
        generatedSchemas.forEach((name, schema) -> generatedComponents.put(name, schema.toCode()));
        return ImmutableMap.copyOf(generatedComponents);
    }

    public ImmutableMap<String, Schema> generateSchemas() {
        final HashMap<String, Schema> generatedSchemas = new HashMap<>();
        final JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        final List<String> schemas = Lists.newArrayList(joSchemas.keys());
        int i = 0;
        while (generatedSchemas.size() < schemas.size()) {
            Schema generatedSchema = Schema.parseSchema(joSchemas.getJSONObject(schemas.get(i)), generatedSchemas, schemas.get(i));
            if (generatedSchema != null) {
                generatedSchemas.put(schemas.get(i), generatedSchema);
            }

            i = (i + 1) % schemas.size();
        }

        return ImmutableMap.copyOf(generatedSchemas);
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
        final MethodDeclaration runMethod = testsClass.addMethod(Utils.GET_METHOD_NAME);
        final MethodDeclaration runMethodWithBody = Utils.generateGetRequestMethod();

        final MethodDeclaration postMethod = testsClass.addMethod(Utils.POST_METHOD_NAME);
        final MethodDeclaration postMethodWithBody = Utils.generatePostRequestMethod();

        testsClass.addField("OkHttpClient", "client", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new OkHttpClient()");
        testsClass.addField("Gson", "gson", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new GsonBuilder().create()");
        testsClass.addField("String", "BASE_URL", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("\"" + getBaseURL() + "?\"");
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
                GenerateRoutesAndTestsUtils.generateRoute(routesInterface, joPaths.getJSONObject(path).getJSONObject(operation), path, operation);
                GenerateRoutesAndTestsUtils.generateTest(testsClass, joPaths.getJSONObject(path).getJSONObject(operation), path, operation);
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
}
