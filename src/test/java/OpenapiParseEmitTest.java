package openapi;

import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class OpenapiParseEmitTest {

    @Test
    public void testParseConstructors() {
        assertNotNull(new Parse());
        assertNotNull(new Emit());
    }

    @Test
    public void testParseFromStringFull() throws Exception {
        String json = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"swagger\": \"2.0\",\n" +
                "  \"host\": \"localhost\",\n" +
                "  \"basePath\": \"/v1\",\n" +
                "  \"schemes\": [\"http\", \"https\"],\n" +
                "  \"consumes\": [\"application/json\"],\n" +
                "  \"produces\": [\"application/json\"],\n" +
                "  \"info\": {\n" +
                "    \"title\": \"API\",\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"description\": \"My API\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"x-ignore\": {},\n" +
                "    \"/path\": {\n" +
                "      \"get\": {\n" +
                "        \"operationId\": \"getOp\",\n" +
                "        \"summary\": \"sum\",\n" +
                "        \"description\": \"desc\",\n" +
                "        \"parameters\": [\n" +
                "          {\n" +
                "            \"name\": \"param1\",\n" +
                "            \"in\": \"query\",\n" +
                "            \"description\": \"pdesc\",\n" +
                "            \"required\": true,\n" +
                "            \"schema\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"$ref\": \"#/def/schema\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"requestBody\": {\n" +
                "          \"description\": \"rbdesc\",\n" +
                "          \"required\": true,\n" +
                "          \"content\": {\n" +
                "            \"application/json\": {\n" +
                "              \"schema\": {\n" +
                "                \"type\": \"string\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"200desc\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"post\": {},\n" +
                "      \"put\": {},\n" +
                "      \"delete\": {},\n" +
                "      \"patch\": {},\n" +
                "      \"parameters\": []\n" +
                "    },\n" +
                "    \"/empty\": {}\n" +
                "  },\n" +
                "  \"definitions\": {\n" +
                "    \"Def\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"format\": \"date\",\n" +
                "      \"description\": \"def desc\",\n" +
                "      \"$ref\": \"#/ref\",\n" +
                "      \"items\": { \"type\": \"string\" },\n" +
                "      \"properties\": {\n" +
                "        \"prop1\": { \"type\": \"string\" }\n" +
                "      }\n" +
                "    },\n" +
                "    \"EmptyDef\": {}\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {\n" +
                "      \"Comp\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        OpenAPI api = Parse.fromString(json);
        assertNotNull(api);
        assertEquals("3.0.0", api.openapi);
        assertEquals("2.0", api.swagger);
        assertEquals("localhost", api.host);

        // Convert back to string
        String emitted = Emit.toString(api);
        assertTrue(emitted.contains("\"openapi\": \"3.0.0\""));
    }

    @Test
    public void testParseEmptyAndNulls() throws Exception {
        String json = "{\n" +
                "  \"info\": {\n" +
                "    \"description\": null\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/empty_op\": {\n" +
                "      \"get\": {\n" +
                "        \"requestBody\": {\n" +
                "          \"content\": {\n" +
                "            \"text/plain\": {}\n" +
                "          }\n" +
                "        },\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"content\": {\n" +
                "              \"text/plain\": {}\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"parameters\": [{}]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"components\": {}\n" +
                "}";
        OpenAPI api = Parse.fromString(json);
        assertNotNull(api.info);
        assertNull(api.info.description);

        String emitted = Emit.toString(api);
        assertFalse(emitted.contains("\"description\""));
    }

    @Test
    public void testEmitCoverage() throws Exception {
        OpenAPI api = new OpenAPI();
        api.schemes = new ArrayList<>();
        api.consumes = new ArrayList<>();
        api.produces = new ArrayList<>();
        
        api.definitions = new HashMap<>();
        api.definitions.put("nullDef", null);
        
        api.components = new Components();
        api.components.schemas = new HashMap<>();
        api.components.schemas.put("nullSchema", null);

        api.paths = new Paths();
        api.paths.pathItems = new HashMap<>();
        PathItem pi = new PathItem();
        api.paths.pathItems.put("/p", pi);

        Operation op = new Operation();
        pi.get = op;

        op.requestBody = "Not a request body";
        op.parameters = new ArrayList<>();
        op.parameters.add("Not a parameter");
        
        op.responses = new Responses();
        op.responses.statusCodes = new HashMap<>();
        op.responses.statusCodes.put("200", "Not a response");

        Response validResponse = new Response();
        validResponse.content = new HashMap<>();
        MediaType badMt = new MediaType();
        badMt.schema = "Not a schema";
        validResponse.content.put("application/json", badMt);
        op.responses.statusCodes.put("201", validResponse);

        RequestBody rb = new RequestBody();
        rb.content = new HashMap<>();
        rb.content.put("application/json", badMt);
        op.requestBody = rb;

        Schema badSchema = new Schema();
        badSchema.items = "Not a schema";
        badSchema.properties = new HashMap<>();
        badSchema.properties.put("prop", "Not a schema");

        api.definitions.put("badSchema", badSchema);

        String emitted = Emit.toString(api);
        assertNotNull(emitted);
    }

    @Test(expected = IOException.class)
    public void testParseException() throws Exception {
        Parse.fromString("{ malformed json");
    }

    @Test
    public void testFileOperations() throws Exception {
        File tempFile = File.createTempFile("openapi-test", ".json");
        tempFile.deleteOnExit();

        OpenAPI api = new OpenAPI();
        api.openapi = "3.1.0";

        Emit.toFile(api, tempFile);
        
        OpenAPI parsed = Parse.fromFile(tempFile);
        assertEquals("3.1.0", parsed.openapi);
    }
}
