package io.offscale;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MergeTests {

    private static String readFileToString(String filePath) {
        try {
            return Files.
                    readString(Path.of(filePath));
        } catch (IOException e) {
            return "";
        }
    }
    private final Map<String, String> openAPISpec1Components = Map.of(
            "Pet", readFileToString("src/main/resources/OpenAPISpec1/componentCode1.txt")
    );
    private final String openAPISpec1Routes = "src/main/resources/OpenAPISpec1/routesCode.txt";

    @Test
    public void mergeComponents_addNewComponent() {
        final Merge merge = new Merge(ImmutableMap.copyOf(openAPISpec1Components), openAPISpec1Routes, "OpenAPISpec1/openapi.yaml");
        final ImmutableMap<String, String> mergedComponents = merge.mergeComponents();
        assertEquals(mergedComponents.size(), 2);
        assertEquals(mergedComponents.get("Pet"), readFileToString("src/main/resources/OpenAPISpec1/componentCode1.txt"));
        assertEquals(mergedComponents.get("Error"), readFileToString("src/main/resources/OpenAPISpec1/componentCode2.txt"));
    }

    @Test
    public void mergeComponents_addAndDeleteField() {
        final Merge merge = new Merge(ImmutableMap.copyOf(openAPISpec1Components), openAPISpec1Routes, "OpenAPISpec2/openapi.yaml");
        final ImmutableMap<String, String> mergedComponents = merge.mergeComponents();
        assertEquals(mergedComponents.size(), 2);
        assertEquals(mergedComponents.get("Pet"), readFileToString("src/main/resources/OpenAPISpec2/componentCode1.txt"));
        final ClassOrInterfaceDeclaration testsClass = new ClassOrInterfaceDeclaration().setName("Tests");
        testsClass.addField("OkHttpClient", "client").getVariable(0).setInitializer("new OkHttpClient()");
        System.out.println(testsClass);
    }

    @Test
    public void mergeRoutes_addAndDeleteRoute() {
        final Merge merge = new Merge(ImmutableMap.copyOf(openAPISpec1Components), readFileToString(openAPISpec1Routes) , "OpenAPISpec2/openapi.yaml");
        assertEquals(merge.mergeRoutes(), readFileToString("src/main/resources/OpenAPISpec2/routesCode.txt"));
    }


}
