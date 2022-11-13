package io.offscale;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateTests {
    private Create create;
    private Create improperFormCreate;
    static private final String PET_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode1.txt";
    static private final String PETS_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode4.txt";
    static private final String DOG_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode3.txt";
    static private final String ROUTES_FILE_PATH = "src/main/resources/OpenAPISpec1/routesCode.txt";

    @Before
    public void init() {
        this.create = new Create("OpenAPISpec1/openapi_simple.yaml");
        this.improperFormCreate = new Create("improperFormOpenAPI.yaml");
    }

    @Test
    public void generateComponentsSuccess() throws IOException {
        final String petComponentCode = Files.readString(Path.of(PET_COMPONENT_FILE_PATH));
        final String petsComponentCode = Files.readString(Path.of(PETS_COMPONENT_FILE_PATH));
        final String dogComponentCode = Files.readString(Path.of(DOG_COMPONENT_FILE_PATH));
        final ImmutableMap<String, String> generatedComponents = create.generateComponents();
        assertEquals(generatedComponents.size(), 4);
        assertEquals(generatedComponents.get("Pet"), petComponentCode);
        assertEquals(generatedComponents.get("Pets"), petsComponentCode);
        assertEquals(generatedComponents.get("Dog"), dogComponentCode);
    }

    @Test
    public void generateSchemas2Test() {
        final ImmutableMap<String, Schema2> generatedComponents = create.generateSchemas();
        Schema2 schema = generatedComponents.get("Error");
        System.out.println(schema.properties().get("message").strictType());
    }

    @Test(expected = JSONException.class)
    public void generateComponentsException() {
        final ImmutableMap<String, String> generatedComponents = improperFormCreate.generateComponents();
    }

    @Test
    public void generateRoutesSuccess() throws IOException {
        final Path filePath = Path.of(ROUTES_FILE_PATH);
        final String routesCode = Files.readString(filePath);
        assertEquals(create.generateRoutesAndTests().get("routes"), routesCode);
    }

    @Test
    public void generateTestsSuccess() {
        final String testClass = create.generateRoutesAndTests().get("tests");
        System.out.println(testClass);
        assertThat(testClass, containsString("createPetsTest()"));
        assertThat(testClass, containsString("listPetsTest()"));
        assertThat(testClass, containsString("showDogByIdTest()"));
    }

//    Response run(String url, OkHttpClient client) throws IOException {
//        final Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            response.headers().forEach(System.out::println);
//            System.out.println(response.code());
//            return response;
//        }
//    }
//    @Test
//    public void testEndPoints() throws IOException {
//        /*final URL url = new URL("http://www.android.com/");*/
//        final OkHttpClient client = new OkHttpClient();
//        System.out.println(run("https://petstore.swagger.io/v2/pet/findByStatus?status=available", client));
//
//        final String jsonString = """
//                10
//                """;
//        final Gson gson = new GsonBuilder().create();
//
//        final Integer user = gson.fromJson(jsonString, Integer.class);
//
//        System.out.println(user);
//    }

}
