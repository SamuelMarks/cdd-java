package org.offscale;

import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateTests {
    private Create create;
    private Create improperFormCreate;

    static private final String COMPONENT_FILE_PATH = "src/main/resources/OpenAPISpec1/componentCode1.txt";
    static private final String ROUTES_FILE_PATH = "src/main/resources/OpenAPISpec1/routesCode.txt";

    @Before
    public void init() {
        this.create = new Create("OpenAPISpec1/openapi.yaml");
        this.improperFormCreate = new Create("improperFormOpenAPI.yaml");
    }

    @Test
    public void generateComponentsSuccess() throws IOException {
        Path filePath = Path.of(COMPONENT_FILE_PATH);
        String petComponentCode = Files.readString(filePath);
        ImmutableMap<String, String> generatedComponents = create.generateComponents();
//        System.out.println(generatedComponents.get("Pet"));
        assertEquals(generatedComponents.size(), 2);
        assertEquals(generatedComponents.get("Pet"), petComponentCode);
    }

    @Test(expected = JSONException.class)
    public void generateComponentsException() {
        ImmutableMap<String, String> generatedComponents = improperFormCreate.generateComponents();
    }

    @Test
    public void generateRoutesSuccess() throws IOException {
        Path filePath = Path.of(ROUTES_FILE_PATH);
        String routesCode = Files.readString(filePath);
        assertEquals(create.generateRoutes(), routesCode);
    }

}
