package io.offscale;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateTests {
    private Create create;
    static private final String PET_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode1.txt";
    static private final String UNNAMED_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode2.txt";
    static private final String DOG_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode3.txt";
    static private final String ROUTES_FILE_PATH = "src/main/resources/OpenAPISpec1/routesCode.txt";

    @Before
    public void init() {
        this.create = new Create("OpenAPISpec1/openapi.yaml");
    }

    @Test
    public void generateComponentsSuccess() throws IOException {
        final String petComponentCode = Files.readString(Path.of(PET_COMPONENT_FILE_PATH));
        final String dogComponentCode = Files.readString(Path.of(DOG_COMPONENT_FILE_PATH));
        final ImmutableMap<String, String> generatedComponents = create.generateComponents();
        for (String key: generatedComponents.keySet()) {
            System.out.println(generatedComponents.get(key));
        }
        assertEquals(3,generatedComponents.size());
        assertEquals(generatedComponents.get("Pet"), petComponentCode);
        assertEquals(generatedComponents.get("Dog"), dogComponentCode);
    }

    @Test(expected = AssertionError.class)
    public void generateComponentsException() {
        Create improperFormCreate = new Create("improperFormOpenAPI.yaml");
        improperFormCreate.generateComponents();
    }

    @Test
    public void generateRoutesSuccess() throws IOException {
        final Path filePath = Path.of(ROUTES_FILE_PATH);
        final String routesCode = Files.readString(filePath);
        System.out.println(routesCode);
        assertEquals(create.generateRoutesAndTests().routes(), routesCode);
    }

    @Test
    public void generateTestsSuccess() {
        final String testClass = create.generateRoutesAndTests().tests();
        System.out.println(testClass);
        assertThat(testClass, containsString("createPetsTest()"));
        assertThat(testClass, containsString("listPetsTest()"));
        assertThat(testClass, containsString("showDogByIdTest()"));
    }

    @Test
    public void generateUnnamedComponentsSuccess() throws IOException {
        final String unnamedComponentCode = Files.readString(Path.of(UNNAMED_COMPONENT_FILE_PATH));
        assertEquals(create.generateRoutesAndTests().schemas().get("Petsgetinfobyidget"), unnamedComponentCode);
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
