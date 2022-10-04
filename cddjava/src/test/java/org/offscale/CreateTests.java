package org.offscale;

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

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateTests {
    private Create create;
    private Create improperFormCreate;
    static private final String PET_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode1.txt";
    static private final String DOG_COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode3.txt";
    static private final String ROUTES_FILE_PATH = "src/main/resources/OpenAPISpec1/routesCode.txt";

    @Before
    public void init() {
        this.create = new Create("OpenAPISpec1/openapi.yaml");
        this.improperFormCreate = new Create("improperFormOpenAPI.yaml");
    }

    @Test
    public void generateComponentsSuccess() throws IOException {
        String petComponentCode = Files.readString(Path.of(PET_COMPONENT_FILE_PATH));
        String dogComponentCode = Files.readString(Path.of(DOG_COMPONENT_FILE_PATH));
        ImmutableMap<String, String> generatedComponents = create.generateComponents();
        assertEquals(generatedComponents.size(), 3);
        assertEquals(generatedComponents.get("Pet"), petComponentCode);
        assertEquals(generatedComponents.get("Dog"), dogComponentCode);
    }

    @Test(expected = JSONException.class)
    public void generateComponentsException() {
        ImmutableMap<String, String> generatedComponents = improperFormCreate.generateComponents();
    }

    @Test
    public void generateRoutesSuccess() throws IOException {
        Path filePath = Path.of(ROUTES_FILE_PATH);
        String routesCode = Files.readString(filePath);
        System.out.println(create.generateRoutesAndTests().get("tests"));
        assertEquals(create.generateRoutesAndTests().get("routes"), routesCode);
    }

    String run(String url, OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            response.headers().forEach(header -> System.out.println(header));
            System.out.println(response.code());
            return response.body().string();
        }
    }
    @Test
    public void testEndPoints() throws IOException {
        URL url = new URL("http://www.android.com/");
        OkHttpClient client = new OkHttpClient();
        System.out.println(run("https://petstore.swagger.io/v2/pet/findByStatus?status=available", client));

        String jsonString = """
                10
                """;
        Gson gson = new GsonBuilder().create();

        Integer user = gson.fromJson(jsonString, Integer.class);

        System.out.println(user);
    }

}
