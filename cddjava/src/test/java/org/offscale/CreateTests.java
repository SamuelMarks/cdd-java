package org.offscale;

import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.offscale.Create;
import org.offscale.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateTests {
    private Create create;
    private Create improperFormCreate;

    static private final String COMPONENT_FILE_PATH = "src/main/resources/openAPIComponentCode.txt";
    static private final String ROUTES_FILE_PATH = "src/main/resources/openAPIRoutesCode.txt";

    @Before
    public void init() {
        this.create = new Create("test.yaml");
        this.improperFormCreate = new Create("improperFormOpenAPI.yaml");
    }

    @Test
    public void generateComponentsSuccess() throws IOException {
        Path filePath = Path.of(COMPONENT_FILE_PATH);
        String petComponentCode = Files.readString(filePath);
        ImmutableMap<String, String> generatedComponents = create.generateComponents();
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
