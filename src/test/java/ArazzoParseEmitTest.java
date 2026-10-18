import static org.junit.Assert.*;

import arazzo.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import openapi.ReferenceOr;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Exhaustive unit and round-trip tests for {@link arazzo.Parse} and
 * {@link arazzo.Emit} across JSON and YAML formats.
 */
public class ArazzoParseEmitTest {
	/**
	 * Default constructor.
	 */
	public ArazzoParseEmitTest() {
	}

	/**
	 * Verifies full JSON and YAML round-trip serialization and parsing across all
	 * Arazzo 1.1.0 objects, components, selectors, replacements, actions, and
	 * extensions.
	 *
	 * @throws Exception
	 *             If parsing or file I/O fails.
	 */
	@Test
	public void testFullArazzoJsonAndYamlRoundTrip() throws Exception {
		assertNotNull(new arazzo.Parse());
		assertNotNull(new arazzo.Emit());

		String fullJson = "{\n" + "  \"arazzo\": \"1.1.0\",\n" + "  \"$self\": \"https://example.com/arazzo.json\",\n"
				+ "  \"x-root-ext\": {\"k\": \"v\"},\n" + "  \"info\": {\n" + "    \"title\": \"Petstore Workflows\",\n"
				+ "    \"summary\": \"Workflow suite summary\",\n"
				+ "    \"description\": \"Detailed markdown description\",\n" + "    \"version\": \"1.1.0\",\n"
				+ "    \"x-info-ext\": [1, 2]\n" + "  },\n" + "  \"sourceDescriptions\": [\n"
				+ "    {\"name\": \"petstore\", \"url\": \"./petstore.json\", \"type\": \"openapi\", \"x-sd\": \"yes\"},\n"
				+ "    {\"name\": \"events\", \"url\": \"./events.yaml\", \"type\": \"asyncapi\"}\n" + "  ],\n"
				+ "  \"components\": {\n" + "    \"inputs\": {\n"
				+ "      \"UserAuth\": {\"type\": \"object\", \"properties\": {\"token\": {\"type\": \"string\"}}}\n"
				+ "    },\n" + "    \"parameters\": {\n"
				+ "      \"sharedParam\": {\"name\": \"api_key\", \"in\": \"header\", \"value\": \"$inputs.token\", \"x-p\": 1}\n"
				+ "    },\n" + "    \"successActions\": {\n"
				+ "      \"sharedEnd\": {\"name\": \"done\", \"type\": \"end\", \"x-sa\": true}\n" + "    },\n"
				+ "    \"failureActions\": {\n"
				+ "      \"sharedRetry\": {\"name\": \"retryIt\", \"type\": \"retry\", \"retryAfter\": 1.5, \"retryLimit\": 3, \"x-fa\": \"ok\"}\n"
				+ "    },\n" + "    \"x-comp\": \"v\"\n" + "  },\n" + "  \"workflows\": [\n" + "    {\n"
				+ "      \"workflowId\": \"adoptPet\",\n" + "      \"summary\": \"Adopt a pet\",\n"
				+ "      \"description\": \"Full pet adoption flow\",\n" + "      \"inputs\": {\n"
				+ "        \"type\": \"object\",\n"
				+ "        \"properties\": {\"petId\": {\"type\": \"integer\"}, \"notes\": {\"type\": \"string\"}}\n"
				+ "      },\n" + "      \"dependsOn\": [\"initSession\"],\n" + "      \"parameters\": [\n"
				+ "        {\"reference\": \"$components.parameters.sharedParam\", \"value\": \"overrideVal\"},\n"
				+ "        {\"name\": \"traceId\", \"in\": \"header\", \"value\": \"123\"}\n" + "      ],\n"
				+ "      \"successActions\": [\n"
				+ "        {\"reference\": \"$components.successActions.sharedEnd\"},\n"
				+ "        {\"name\": \"gotoNext\", \"type\": \"goto\", \"workflowId\": \"notifyUser\", \"parameters\": [{\"name\": \"p\", \"in\": \"query\", \"value\": 1}], \"criteria\": [{\"condition\": \"$statusCode == 200\"}]}\n"
				+ "      ],\n" + "      \"failureActions\": [\n"
				+ "        {\"reference\": \"$components.failureActions.sharedRetry\"},\n"
				+ "        {\"name\": \"fallbackStep\", \"type\": \"goto\", \"stepId\": \"recoverStep\", \"parameters\": [{\"name\": \"err\", \"in\": \"query\", \"value\": true}], \"criteria\": [{\"condition\": \"$statusCode == 500\"}]}\n"
				+ "      ],\n" + "      \"steps\": [\n" + "        {\n" + "          \"stepId\": \"fetchPet\",\n"
				+ "          \"description\": \"Fetch pet by ID\",\n" + "          \"operationId\": \"getPetById\",\n"
				+ "          \"timeout\": 5000,\n" + "          \"correlationId\": \"$request.header.X-Corr\",\n"
				+ "          \"action\": \"send\",\n" + "          \"dependsOn\": [\"preStep\"],\n"
				+ "          \"parameters\": [\n"
				+ "            {\"name\": \"petId\", \"in\": \"path\", \"value\": \"$inputs.petId\"},\n"
				+ "            {\"reference\": \"$components.parameters.sharedParam\"}\n" + "          ],\n"
				+ "          \"requestBody\": {\n" + "            \"contentType\": \"application/json\",\n"
				+ "            \"payload\": {\"nested\": {\"flag\": true, \"count\": 2, \"empty\": null, \"arr\": [\"a\", 1]}},\n"
				+ "            \"replacements\": [\n"
				+ "              {\"target\": \"/nested/flag\", \"targetSelectorType\": \"jsonpointer\", \"value\": false, \"x-pr\": 1},\n"
				+ "              {\"target\": \"$.nested.count\", \"targetSelectorType\": {\"type\": \"jsonpath\", \"version\": \"rfc9535\", \"x-et\": 1}, \"value\": 10}\n"
				+ "            ],\n" + "            \"x-rb\": \"yes\"\n" + "          },\n"
				+ "          \"successCriteria\": [\n"
				+ "            {\"context\": \"$statusCode\", \"condition\": \"$statusCode == 200\", \"type\": \"simple\", \"x-c\": 1},\n"
				+ "            {\"context\": \"$response.body\", \"condition\": \"$.id != null\", \"type\": {\"type\": \"jsonpath\", \"version\": \"rfc9535\"}}\n"
				+ "          ],\n" + "          \"onSuccess\": [\n"
				+ "            {\"name\": \"stepDone\", \"type\": \"goto\", \"stepId\": \"confirmStep\"},\n"
				+ "            {\"reference\": \"$components.successActions.sharedEnd\"}\n" + "          ],\n"
				+ "          \"onFailure\": [\n"
				+ "            {\"name\": \"stepFail\", \"type\": \"end\", \"workflowId\": \"errWf\"},\n"
				+ "            {\"reference\": \"$components.failureActions.sharedRetry\"}\n" + "          ],\n"
				+ "          \"outputs\": {\n" + "            \"petName\": \"$response.body#/name\",\n"
				+ "            \"petTag\": {\"context\": \"$response.body\", \"selector\": \"$.tag\", \"type\": \"jsonpath\", \"x-sel\": 1}\n"
				+ "          },\n" + "          \"x-step\": \"ok\"\n" + "        }\n" + "      ],\n"
				+ "      \"outputs\": {\n" + "        \"adoptedPetName\": \"$steps.fetchPet.outputs.petName\",\n"
				+ "        \"structuredOut\": {\"context\": \"$response.body\", \"selector\": \"/id\", \"type\": {\"type\": \"jsonpointer\", \"version\": \"rfc6901\"}}\n"
				+ "      },\n" + "      \"x-wf\": \"yes\"\n" + "    },\n" + "    {\n"
				+ "      \"workflowId\": \"refInputWf\",\n"
				+ "      \"inputs\": {\"$ref\": \"$components.inputs.UserAuth\"},\n"
				+ "      \"steps\": [{\"stepId\": \"s1\", \"operationPath\": \"{$sourceDescriptions.petstore.url}#/paths/~1pet/post\"}]\n"
				+ "    },\n" + "    {\n" + "      \"workflowId\": \"refInputWf2\",\n"
				+ "      \"inputs\": {\"reference\": \"$components.inputs.UserAuth\"},\n"
				+ "      \"steps\": [{\"stepId\": \"s2\", \"channelPath\": \"/pets/events\", \"workflowId\": \"adoptPet\"}]\n"
				+ "    }\n" + "  ]\n" + "}";

		Arazzo parsed = arazzo.Parse.fromString(fullJson);
		assertEquals("1.1.0", parsed.arazzo);
		assertEquals("https://example.com/arazzo.json", parsed.$self);
		assertEquals("Petstore Workflows", parsed.info.title);
		assertEquals(2, parsed.sourceDescriptions.size());
		assertEquals(3, parsed.workflows.size());

		String emittedJson = arazzo.Emit.toString(parsed);
		Arazzo reparsedJson = arazzo.Parse.fromString(emittedJson);
		assertEquals(parsed.info, reparsedJson.info);
		assertEquals(parsed.sourceDescriptions, reparsedJson.sourceDescriptions);
		assertEquals(parsed.components, reparsedJson.components);
		assertEquals(parsed.workflows.get(0), reparsedJson.workflows.get(0));

		String emittedYaml = arazzo.Emit.toYaml(parsed);
		Arazzo reparsedYaml = arazzo.Parse.fromString(emittedYaml);
		assertEquals(parsed.info, reparsedYaml.info);
		assertEquals(parsed.workflows.get(0).workflowId, reparsedYaml.workflows.get(0).workflowId);

		File tmpJson = File.createTempFile("arazzo_test", ".json");
		File tmpYaml = File.createTempFile("arazzo_test", ".yaml");
		File tmpYml = File.createTempFile("arazzo_test", ".yml");
		try {
			arazzo.Emit.toFile(parsed, tmpJson);
			arazzo.Emit.toFile(parsed, tmpYaml);
			arazzo.Emit.toFile(parsed, tmpYml);
			assertEquals(parsed.info, arazzo.Parse.fromFile(tmpJson).info);
			assertEquals(parsed.info, arazzo.Parse.fromFile(tmpYaml).info);
			assertEquals(parsed.info, arazzo.Parse.fromFile(tmpYml).info);
		} finally {
			Files.deleteIfExists(tmpJson.toPath());
			Files.deleteIfExists(tmpYaml.toPath());
			Files.deleteIfExists(tmpYml.toPath());
		}
	}

	/**
	 * Verifies edge cases and empty/partial branches in {@link arazzo.Parse} and
	 * {@link arazzo.Emit}.
	 *
	 * @throws Exception
	 *             If an unexpected exception occurs.
	 */
	@Test
	public void testParseAndEmitEdgeCases() throws Exception {
		Arazzo minimal = arazzo.Parse.fromJson(new JSONObject("{\"components\":{}}"));
		minimal.arazzo = null;
		assertTrue(arazzo.Parse.parsePayloadValue(null).isNull());
		String json = arazzo.Emit.toString(minimal);
		assertEquals("{}", json.trim());

		SourceDescription sdNoType = new SourceDescription();
		sdNoType.name = "s";
		minimal.sourceDescriptions = Collections.singletonList(sdNoType);

		Workflow wfEdge = new Workflow();
		wfEdge.workflowId = "w";
		wfEdge.inputs = new ReferenceOr<>(null, null);
		wfEdge.parameters = Collections.singletonList(new ReusableOr<>(null, null));
		wfEdge.successActions = Collections.singletonList(new ReusableOr<>(null, null));
		wfEdge.failureActions = Collections.singletonList(new ReusableOr<>(null, null));

		Step stepEdge = new Step();
		stepEdge.stepId = "st";
		ArazzoParameter paramNoIn = new ArazzoParameter();
		paramNoIn.name = "p";
		stepEdge.parameters = Collections.singletonList(ReusableOr.ofItem(paramNoIn));
		stepEdge.requestBody = new ArazzoRequestBody();
		Criterion critEmptyType = new Criterion();
		critEmptyType.condition = "true";
		critEmptyType.type = new CriterionTypeOrExpression(null, null);
		Criterion critExprNoType = new Criterion();
		critExprNoType.condition = "true";
		critExprNoType.type = CriterionTypeOrExpression.ofExpressionType(new ExpressionType());
		stepEdge.successCriteria = Arrays.asList(critEmptyType, critExprNoType);

		SuccessAction saNoType = new SuccessAction();
		saNoType.name = "sa";
		stepEdge.onSuccess = Collections.singletonList(ReusableOr.ofItem(saNoType));

		FailureAction faNoType = new FailureAction();
		faNoType.name = "fa";
		stepEdge.onFailure = Collections.singletonList(ReusableOr.ofItem(faNoType));

		Selector selNoType = new Selector();
		selNoType.context = "$response.body";
		selNoType.selector = "/id";
		stepEdge.outputs = new LinkedHashMap<>();
		stepEdge.outputs.put("k1", OutputValue.ofSelector(selNoType));
		stepEdge.outputs.put("k2", new OutputValue(null, null));

		PayloadReplacement prNoVal = new PayloadReplacement();
		prNoVal.target = "/a";
		stepEdge.requestBody.replacements = Collections.singletonList(prNoVal);

		wfEdge.steps = Collections.singletonList(stepEdge);
		minimal.workflows = Collections.singletonList(wfEdge);

		String edgeJson = arazzo.Emit.toString(minimal);
		assertNotNull(arazzo.Parse.fromString(edgeJson));

		JSONObject refWithExtraKey = new JSONObject();
		refWithExtraKey.put("$ref", "$components.inputs.UserAuth");
		refWithExtraKey.put("description", "override");
		JSONObject wfObj = new JSONObject();
		wfObj.put("workflowId", "wfExtra");
		wfObj.put("inputs", refWithExtraKey);
		Workflow parsedWf = arazzo.Parse.parseWorkflow(wfObj);
		assertTrue(parsedWf.inputs.isValue());

		try {
			arazzo.Parse.fromString(":::invalid yaml:\n\t- [unclosed");
			fail("Expected IOException on malformed input");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("Failed to parse Arazzo"));
		}
	}
}
