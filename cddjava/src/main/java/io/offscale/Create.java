package io.offscale;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


/**
 * Given an OpenAPI Spec, generates code for routes and components and tests
 * when no code previously exists
 */
public class Create {
    private final JSONObject jo;
    private record Response(Schema schema, String description) { }

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
                .put(schema, GenerateComponentUtils.generateComponent(joSchemas.getJSONObject(schema), schema, null).code()));
        return ImmutableMap.copyOf(generatedComponents);
    }

    public ImmutableMap<String, Schema2> generateSchemas() {
        final HashMap<String, Schema2> generatedSchemas = new HashMap<>();
        final JSONObject joSchemas = jo.getJSONObject("components").getJSONObject("schemas");
        final List<String> schemas = Lists.newArrayList(joSchemas.keys());
        int i = 0;
        while (generatedSchemas.size() < schemas.size()) {
            Schema2 generatedSchema = Schema2.parseSchema(joSchemas.getJSONObject(schemas.get(i)), generatedSchemas, schemas.get(i));
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
        testsClass.addField("OkHttpClient", "client", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new OkHttpClient()");
        testsClass.addField("Gson", "gson", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("new GsonBuilder().create()");
        testsClass.addField("String", "BASE_URL", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL).getVariable(0)
                .setInitializer("\"" + getBaseURL() + "?\"");
        final MethodDeclaration runMethod = testsClass.addMethod(Utils.GET_METHOD_NAME);
        final MethodDeclaration runMethodWithBody = Utils.generateGetRequestMethod();
        runMethod.setParameters(runMethodWithBody.getParameters());
        runMethod.setType(runMethodWithBody.getType());
        final Optional<BlockStmt> body = runMethodWithBody.getBody();
        body.ifPresent(runMethod::setBody);

        for (final String path : paths) {
            for (final String operation : Lists.newArrayList(joPaths.getJSONObject(path).keys())) {
                GenerateRoutesAndTestsUtils.generateRoute(routesInterface, joPaths.getJSONObject(path).getJSONObject(operation), path, operation);
                GenerateRoutesAndTestsUtils.generateTest(testsClass, joPaths.getJSONObject(path).getJSONObject(operation));
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
