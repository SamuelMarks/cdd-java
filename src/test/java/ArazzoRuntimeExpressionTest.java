import static org.junit.Assert.*;

import arazzo.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import openapi.OpenAPI;
import openapi.Reference;
import openapi.ReferenceOr;
import openapi.Schema;
import org.junit.Test;

/**
 * Exhaustive unit tests for {@link RuntimeExpression} and
 * {@link ArazzoResolver}.
 */
public class ArazzoRuntimeExpressionTest {
	/**
	 * Default constructor.
	 */
	public ArazzoRuntimeExpressionTest() {
	}

	/**
	 * Verifies evaluation of all 17 Arazzo 1.1.0 runtime expression roots and
	 * embedded template expressions.
	 */
	@Test
	public void testAll17RuntimeExpressions() {
		assertNotNull(new RuntimeExpression());
		RuntimeExpression.Context ctx = new RuntimeExpression.Context();
		ctx.url = "https://api.example.com/v1/pets/42";
		ctx.method = "GET";
		ctx.statusCode = 201;
		ctx.selfUri = "https://api.example.com/arazzo.yaml";
		ctx.requestHeaders.put("Authorization", "Bearer secret");
		ctx.requestQuery.put("filter", "active");
		ctx.requestPath.put("petId", "42");
		ctx.requestBody = "{\"name\":\"Rex\",\"tags\":[\"dog\"]}";
		ctx.responseHeaders.put("X-Rate-Limit", "100");
		ctx.responseBody = "{\"id\":42,\"status\":\"available\",\"items\":[{\"code\":\"A1\"}],\"tilde~key\":{\"slash/key\":\"val\"}}";
		ctx.messageHeaders.put("X-Correlation-Id", "corr-99");
		ctx.messagePayload = "{\"event\":\"created\",\"pet\":{\"id\":42}}";
		ctx.inputs.put("userId", "u-1");
		ctx.outputs.put("finalId", "42");

		Map<String, String> stepMap = new LinkedHashMap<>();
		stepMap.put("token", "tok-abc");
		ctx.steps.put("loginStep", stepMap);

		Map<String, String> wfMap = new LinkedHashMap<>();
		wfMap.put("sessionKey", "sess-xyz");
		ctx.workflows.put("authWf", wfMap);

		Map<String, String> sdMap = new LinkedHashMap<>();
		sdMap.put("url", "https://petstore.swagger.io/v2");
		ctx.sourceDescriptions.put("petstore", sdMap);

		ctx.components.put("parameters.defaultLimit", "50");

		assertEquals("", RuntimeExpression.evaluate(null, ctx));
		assertEquals("literal", RuntimeExpression.evaluate("literal", ctx));
		assertEquals("unclosed {$inputs.userId", RuntimeExpression.evaluate("unclosed {$inputs.userId", ctx));
		assertEquals("https://petstore.swagger.io/v2/users/u-1/pets/42", RuntimeExpression
				.evaluate("{$sourceDescriptions.petstore.url}/users/{$inputs.userId}/pets/{$request.path.petId}", ctx));

		assertEquals("https://api.example.com/v1/pets/42", RuntimeExpression.evaluate("$url", ctx));
		ctx.requestBody = null;
		assertEquals("", RuntimeExpression.evaluate("$request.body", ctx));
		ctx.requestBody = "{\"name\":\"Rex\",\"tags\":[\"dog\"]}";
		assertEquals("GET", RuntimeExpression.evaluate("$method", ctx));
		assertEquals("201", RuntimeExpression.evaluate("$statusCode", ctx));
		assertEquals("https://api.example.com/arazzo.yaml", RuntimeExpression.evaluate("$self", ctx));
		assertEquals("Bearer secret", RuntimeExpression.evaluate("$request.header.Authorization", ctx));
		assertEquals("Bearer secret", RuntimeExpression.evaluate("$request.header.authorization", ctx));
		assertEquals("", RuntimeExpression.evaluate("$request.header.Missing", ctx));
		assertEquals("active", RuntimeExpression.evaluate("$request.query.filter", ctx));
		assertEquals("42", RuntimeExpression.evaluate("$request.path.petId", ctx));
		assertEquals(ctx.requestBody, RuntimeExpression.evaluate("$request.body", ctx));
		assertEquals("Rex", RuntimeExpression.evaluate("$request.body#/name", ctx));
		assertEquals("dog", RuntimeExpression.evaluate("$request.body.tags[0]", ctx));
		assertEquals("", RuntimeExpression.evaluate("$request.body?invalid", ctx));
		assertEquals("100", RuntimeExpression.evaluate("$response.header.x-rate-limit", ctx));
		assertEquals("42", RuntimeExpression.evaluate("$response.body#/id", ctx));
		assertEquals("A1", RuntimeExpression.evaluate("$response.body.items[0].code", ctx));
		assertEquals("val", RuntimeExpression.evaluate("$response.body#/tilde~0key/slash~1key", ctx));
		assertEquals("corr-99", RuntimeExpression.evaluate("$message.header.x-correlation-id", ctx));
		assertEquals("42", RuntimeExpression.evaluate("$message.payload#/pet/id", ctx));
		assertEquals("u-1", RuntimeExpression.evaluate("$inputs.userId", ctx));
		assertEquals("42", RuntimeExpression.evaluate("$outputs.finalId", ctx));
		assertEquals("tok-abc", RuntimeExpression.evaluate("$steps.loginStep.outputs.token", ctx));
		assertEquals("", RuntimeExpression.evaluate("$steps.missingStep.outputs.token", ctx));
		assertEquals("", RuntimeExpression.evaluate("$steps.malformed", ctx));
		assertEquals("sess-xyz", RuntimeExpression.evaluate("$workflows.authWf.outputs.sessionKey", ctx));
		assertEquals("", RuntimeExpression.evaluate("$workflows.missingWf.outputs.sessionKey", ctx));
		assertEquals("", RuntimeExpression.evaluate("$workflows.malformed", ctx));
		assertEquals("https://petstore.swagger.io/v2",
				RuntimeExpression.evaluate("$sourceDescriptions.petstore.url", ctx));
		assertEquals("", RuntimeExpression.evaluate("$sourceDescriptions.missing.url", ctx));
		assertEquals("", RuntimeExpression.evaluate("$sourceDescriptions.nodot", ctx));
		assertEquals("50", RuntimeExpression.evaluate("$components.parameters.defaultLimit", ctx));
		assertEquals("", RuntimeExpression.evaluate("$unknownRoot", ctx));
	}

	/**
	 * Verifies {@link RuntimeExpression#evaluateCriterion},
	 * {@link RuntimeExpression#evaluateSelector},
	 * {@link RuntimeExpression#evaluateOutputValue}, and
	 * {@link RuntimeExpression#applyReplacements}.
	 */
	@Test
	public void testCriteriaSelectorsAndReplacements() {
		RuntimeExpression.Context ctx = new RuntimeExpression.Context();
		ctx.statusCode = 200;
		ctx.responseBody = "{\"id\":10,\"status\":\"available\",\"count\":5,\"items\":[10,20]}";

		assertTrue(RuntimeExpression.evaluateCriterion(null, ctx));
		Criterion emptyCond = new Criterion();
		assertTrue(RuntimeExpression.evaluateCriterion(emptyCond, ctx));

		Criterion simpleEq = new Criterion();
		simpleEq.condition = "$statusCode == 200 && $statusCode != 404";
		assertTrue(RuntimeExpression.evaluateCriterion(simpleEq, ctx));
		simpleEq.condition = "$statusCode != 200";
		assertFalse(RuntimeExpression.evaluateCriterion(simpleEq, ctx));

		Criterion simpleFalseAnd = new Criterion();
		simpleFalseAnd.condition = "$statusCode == 200 && $statusCode == 500";
		assertFalse(RuntimeExpression.evaluateCriterion(simpleFalseAnd, ctx));

		Criterion simpleOr = new Criterion();
		simpleOr.condition = "$statusCode == 400 || $statusCode == 200";
		assertTrue(RuntimeExpression.evaluateCriterion(simpleOr, ctx));

		Criterion simpleFalseOr = new Criterion();
		simpleFalseOr.condition = "$statusCode == 400 || $statusCode == 500";
		assertFalse(RuntimeExpression.evaluateCriterion(simpleFalseOr, ctx));

		Criterion numericCmp = new Criterion();
		numericCmp.context = "$statusCode";
		numericCmp.condition = ">= 200";
		assertTrue(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = ">= 300";
		assertFalse(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "<= 200";
		assertTrue(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "<= 100";
		assertFalse(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "> 199";
		assertTrue(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "> 200";
		assertFalse(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "< 300";
		assertTrue(RuntimeExpression.evaluateCriterion(numericCmp, ctx));
		numericCmp.condition = "< 200";
		assertFalse(RuntimeExpression.evaluateCriterion(numericCmp, ctx));

		Criterion rightExprCmp = new Criterion();
		rightExprCmp.condition = "$statusCode == $statusCode";
		assertTrue(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));
		rightExprCmp.condition = "'";
		assertTrue(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));
		rightExprCmp.condition = "'unclosed";
		assertTrue(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));
		rightExprCmp.condition = "\"unclosed";
		assertTrue(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));
		rightExprCmp.condition = "\"a' == 'a'";
		assertFalse(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));
		rightExprCmp.condition = "'a\" == 'a'";
		assertFalse(RuntimeExpression.evaluateCriterion(rightExprCmp, ctx));

		Criterion lexicalCmp = new Criterion();
		lexicalCmp.condition = "'apple' < 'banana' && 'b' > 'a' && 'b' >= 'b' && 'a' <= 'b'";
		assertTrue(RuntimeExpression.evaluateCriterion(lexicalCmp, ctx));
		lexicalCmp.condition = "\"banana\" < \"apple\"";
		assertFalse(RuntimeExpression.evaluateCriterion(lexicalCmp, ctx));
		lexicalCmp.condition = "'a' >= 'z'";
		assertFalse(RuntimeExpression.evaluateCriterion(lexicalCmp, ctx));
		lexicalCmp.condition = "'z' <= 'a'";
		assertFalse(RuntimeExpression.evaluateCriterion(lexicalCmp, ctx));
		lexicalCmp.condition = "'a' > 'z'";
		assertFalse(RuntimeExpression.evaluateCriterion(lexicalCmp, ctx));

		Criterion bareTruth = new Criterion();
		bareTruth.condition = "$statusCode";
		assertTrue(RuntimeExpression.evaluateCriterion(bareTruth, ctx));
		bareTruth.condition = "false";
		assertFalse(RuntimeExpression.evaluateCriterion(bareTruth, ctx));
		bareTruth.condition = "null";
		assertFalse(RuntimeExpression.evaluateCriterion(bareTruth, ctx));
		bareTruth.condition = "$inputs.missing";
		assertFalse(RuntimeExpression.evaluateCriterion(bareTruth, ctx));

		Criterion regexCrit = new Criterion();
		regexCrit.context = "$response.body#/status";
		regexCrit.condition = "^avail.*";
		regexCrit.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.REGEX);
		assertTrue(RuntimeExpression.evaluateCriterion(regexCrit, ctx));

		Criterion jsonPointerCrit = new Criterion();
		jsonPointerCrit.condition = "/status == 'available'";
		jsonPointerCrit.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPOINTER);
		assertTrue(RuntimeExpression.evaluateCriterion(jsonPointerCrit, ctx));

		Criterion jsonPointerBare = new Criterion();
		jsonPointerBare.context = "$response.body";
		jsonPointerBare.condition = "/id";
		jsonPointerBare.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPOINTER);
		assertTrue(RuntimeExpression.evaluateCriterion(jsonPointerBare, ctx));
		jsonPointerBare.condition = "/missing";
		assertFalse(RuntimeExpression.evaluateCriterion(jsonPointerBare, ctx));

		Criterion jsonPathCrit = new Criterion();
		jsonPathCrit.condition = "$.count >= 5";
		jsonPathCrit.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPATH);
		assertTrue(RuntimeExpression.evaluateCriterion(jsonPathCrit, ctx));

		Criterion jsonPathBare = new Criterion();
		jsonPathBare.context = "$response.body";
		jsonPathBare.condition = "$.missing";
		jsonPathBare.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPATH);
		assertFalse(RuntimeExpression.evaluateCriterion(jsonPathBare, ctx));
		ctx.responseBody = "{\"flag\":false,\"nil\":\"null\"}";
		jsonPathBare.condition = "$.flag";
		assertFalse(RuntimeExpression.evaluateCriterion(jsonPathBare, ctx));
		jsonPathBare.condition = "$.nil";
		assertFalse(RuntimeExpression.evaluateCriterion(jsonPathBare, ctx));
		jsonPointerBare.condition = "/flag";
		assertFalse(RuntimeExpression.evaluateCriterion(jsonPointerBare, ctx));
		assertEquals("null", RuntimeExpression.queryJsonPointer(ctx.responseBody, "#/nil"));
		ctx.responseBody = "{\"id\":10,\"status\":\"available\",\"count\":5,\"items\":[10,20]}";

		ctx.responseBody = "<pet><id>42</id><name>Rex</name></pet>";
		Criterion xpathCrit = new Criterion();
		xpathCrit.condition = "//id";
		xpathCrit.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.XPATH);
		assertTrue(RuntimeExpression.evaluateCriterion(xpathCrit, ctx));
		xpathCrit.context = "$response.body";
		xpathCrit.condition = "//missing";
		assertFalse(RuntimeExpression.evaluateCriterion(xpathCrit, ctx));

		assertEquals("", RuntimeExpression.evaluateOutputValue(null, ctx));
		assertEquals("42", RuntimeExpression.evaluateOutputValue(
				OutputValue.ofSelector(createSelector("$response.body", "//id", CriterionEngineType.XPATH)), ctx));
		ctx.responseBody = "{\"id\":42}";
		assertEquals("42", RuntimeExpression.evaluateOutputValue(
				OutputValue.ofSelector(createSelector(null, "/id", CriterionEngineType.JSONPOINTER)), ctx));
		assertEquals("42",
				RuntimeExpression.evaluateOutputValue(OutputValue.ofSelector(createSelector(null, "$.id", null)), ctx));
		assertEquals("42", RuntimeExpression.evaluateOutputValue(OutputValue.ofExpression("$response.body#/id"), ctx));
		assertEquals("", RuntimeExpression.evaluateSelector(null, ctx));
		assertEquals("", RuntimeExpression.evaluateSelector(new Selector(), ctx));

		assertEquals("", RuntimeExpression.queryJsonPointer(null, "/a"));
		assertEquals("", RuntimeExpression.queryJsonPointer("{}", null));
		assertEquals("{}", RuntimeExpression.queryJsonPointer("{}", ""));
		assertEquals("{}", RuntimeExpression.queryJsonPointer("{}", "/"));
		assertEquals("", RuntimeExpression.queryJsonPointer("not-json", "/a"));
		assertEquals("20", RuntimeExpression.queryJsonPointer("[10, 20]", "1"));
		assertEquals("", RuntimeExpression.queryJsonPointer("[10, null]", "/1"));
		assertEquals("", RuntimeExpression.queryJsonPointer("[10]", "/5"));
		assertEquals("", RuntimeExpression.queryJsonPointer("[10]", "/-1"));
		assertEquals("", RuntimeExpression.queryJsonPointer("[10]", "/nan"));
		assertEquals("", RuntimeExpression.queryJsonPointer("{\"a\":1}", "/a/b"));
		assertEquals("", RuntimeExpression.queryJsonPointer("{\"a\":null}", "/a"));

		assertEquals("", RuntimeExpression.queryJsonPath(null, "$.a"));
		assertEquals("", RuntimeExpression.queryJsonPath("{}", null));
		assertEquals("1", RuntimeExpression.queryJsonPath("{\"a\":1}", "a"));

		assertEquals("", RuntimeExpression.queryXPath(null, "//a"));
		assertEquals("", RuntimeExpression.queryXPath("<a/>", null));
		assertEquals("", RuntimeExpression.queryXPath("<a/>", "///"));

		ctx.inputs.put("newName", "Bella");
		assertEquals("raw", RuntimeExpression.applyReplacements("raw", null, ctx));
		assertEquals("raw", RuntimeExpression.applyReplacements("raw", Collections.emptyList(), ctx));
		PayloadReplacement pr1 = new PayloadReplacement();
		pr1.target = "/pet/name";
		pr1.value = PayloadValue.ofString("$inputs.newName");
		PayloadReplacement pr2 = new PayloadReplacement();
		pr2.target = "$.pet.age";
		pr2.value = PayloadValue.ofNumber(3);
		PayloadReplacement prNullTarget = new PayloadReplacement();
		PayloadReplacement prNullVal = new PayloadReplacement();
		prNullVal.target = "/pet/ignore";
		PayloadReplacement prNoSlash = new PayloadReplacement();
		prNoSlash.target = "relative";
		prNoSlash.value = PayloadValue.ofString("x");
		assertEquals("not-json", RuntimeExpression.applyReplacements("not-json", Arrays.asList(pr1), ctx));
		String replaced = RuntimeExpression.applyReplacements("{\"pet\":\"scalar\"}",
				Arrays.asList(pr1, pr2, prNullTarget, prNullVal, prNoSlash), ctx);
		assertEquals("Bella", RuntimeExpression.queryJsonPointer(replaced, "/pet/name"));
		assertEquals("3", RuntimeExpression.queryJsonPointer(replaced, "/pet/age"));
		String replacedExistingObj = RuntimeExpression.applyReplacements("{\"pet\":{\"existing\":1}}",
				Arrays.asList(pr1), ctx);
		assertEquals("Bella", RuntimeExpression.queryJsonPointer(replacedExistingObj, "/pet/name"));
		String replacedMissingKey = RuntimeExpression.applyReplacements("{}", Arrays.asList(pr1), ctx);
		assertEquals("Bella", RuntimeExpression.queryJsonPointer(replacedMissingKey, "/pet/name"));
		assertEquals("{}", RuntimeExpression.applyReplacements(null, Arrays.asList(prNullTarget), ctx));
	}

	/**
	 * Verifies {@link ArazzoResolver} operation linking, component dereferencing,
	 * and workflow topological sorting.
	 *
	 * @throws Exception
	 *             If temporary file creation fails.
	 */
	@Test
	public void testArazzoResolver() throws Exception {
		assertNotNull(new ArazzoResolver());
		assertTrue(ArazzoResolver.loadOpenApiSources(null, null).isEmpty());

		File tmpDir = Files.createTempDirectory("arazzo_resolver").toFile();
		File oasFile = new File(tmpDir, "petstore.json");
		String oasJson = "{\n" + "  \"openapi\": \"3.2.0\",\n"
				+ "  \"info\": {\"title\": \"Petstore\", \"version\": \"1.0\"},\n" + "  \"paths\": {\n"
				+ "    \"/ sparse\": {\n" + "      \"get\": {\"operationId\": \"sparseGet\"}\n" + "    },\n"
				+ "    \"/pet\": {\n" + "      \"get\": {\"operationId\": \"getPet\"},\n"
				+ "      \"post\": {\"operationId\": \"addPet\"},\n"
				+ "      \"put\": {\"operationId\": \"updatePet\"},\n"
				+ "      \"delete\": {\"operationId\": \"deletePet\"},\n"
				+ "      \"patch\": {\"operationId\": \"patchPet\"},\n"
				+ "      \"options\": {\"operationId\": \"optPet\"},\n"
				+ "      \"head\": {\"operationId\": \"headPet\"},\n"
				+ "      \"trace\": {\"operationId\": \"tracePet\"},\n"
				+ "      \"query\": {\"operationId\": \"queryPet\"}\n" + "    }\n" + "  }\n" + "}";
		Files.writeString(oasFile.toPath(), oasJson, StandardCharsets.UTF_8);

		Arazzo spec = new Arazzo();
		assertTrue(ArazzoResolver.loadOpenApiSources(spec, tmpDir).isEmpty());
		SourceDescription sdLocal = new SourceDescription();
		sdLocal.name = "petstore";
		sdLocal.type = SourceType.OPENAPI;
		sdLocal.url = "petstore.json";
		SourceDescription sdNullUrl = new SourceDescription();
		sdNullUrl.name = "nullUrl";
		sdNullUrl.type = SourceType.OPENAPI;
		SourceDescription sdHttp = new SourceDescription();
		sdHttp.name = "http";
		sdHttp.type = SourceType.OPENAPI;
		sdHttp.url = "http://example.com/oas.json";
		SourceDescription sdRemote = new SourceDescription();
		sdRemote.name = "remote";
		sdRemote.type = SourceType.OPENAPI;
		sdRemote.url = "https://example.com/oas.json";
		SourceDescription sdMissing = new SourceDescription();
		sdMissing.name = "missing";
		sdMissing.type = SourceType.OPENAPI;
		sdMissing.url = "nonexistent.json";
		SourceDescription sdAsync = new SourceDescription();
		sdAsync.name = "async";
		sdAsync.type = SourceType.ASYNCAPI;
		sdAsync.url = "async.yaml";
		spec.sourceDescriptions = Arrays.asList(sdLocal, sdNullUrl, sdHttp, sdRemote, sdMissing, sdAsync);

		Map<String, OpenAPI> loaded = ArazzoResolver.loadOpenApiSources(spec, tmpDir);
		assertEquals(1, loaded.size());
		assertTrue(loaded.containsKey("petstore"));

		assertNull(ArazzoResolver.resolveStepOperation(null, spec, loaded));
		assertNull(ArazzoResolver.resolveStepOperation(new Step(), spec, null));
		assertNull(ArazzoResolver.resolveStepOperation(new Step(), spec, Collections.emptyMap()));
		assertNull(ArazzoResolver.resolveStepOperation(new Step(), spec, loaded));

		String[] ops = {"getPet", "addPet", "updatePet", "deletePet", "patchPet", "optPet", "headPet", "tracePet",
				"queryPet"};
		for (String opId : ops) {
			Step s = new Step();
			s.operationId = opId;
			ArazzoResolver.ResolvedOperation ro = ArazzoResolver.resolveStepOperation(s, spec, loaded);
			assertNotNull(ro);
			assertEquals("petstore", ro.sourceName);
			assertEquals("/pet", ro.path);
		}

		Step prefixedStep = new Step();
		prefixedStep.operationId = "$sourceDescriptions.petstore.addPet";
		assertNotNull(ArazzoResolver.resolveStepOperation(prefixedStep, spec, loaded));

		Step unfoundOpStep = new Step();
		unfoundOpStep.operationId = "nonExistentOperationId";
		assertNull(ArazzoResolver.resolveStepOperation(unfoundOpStep, spec, loaded));

		Step wrongSourceStep = new Step();
		wrongSourceStep.operationId = "$sourceDescriptions.other.addPet";
		assertNull(ArazzoResolver.resolveStepOperation(wrongSourceStep, spec, loaded));

		Step opPathStep = new Step();
		opPathStep.operationPath = "{$sourceDescriptions.petstore.url}#/paths/~1pet/post";
		ArazzoResolver.ResolvedOperation roPath = ArazzoResolver.resolveStepOperation(opPathStep, spec, loaded);
		assertNotNull(roPath);
		assertEquals("POST", roPath.httpMethod);

		Step opPathUnknownVerb = new Step();
		opPathUnknownVerb.operationPath = "#/paths/~1pet/unknown";
		assertNull(ArazzoResolver.resolveStepOperation(opPathUnknownVerb, spec, loaded));

		Step opPathInvalid = new Step();
		opPathInvalid.operationPath = "#/paths/~1missing/unknown";
		assertNull(ArazzoResolver.resolveStepOperation(opPathInvalid, spec, loaded));

		Step opPathNoHash = new Step();
		opPathNoHash.operationPath = "/pet";
		assertNull(ArazzoResolver.resolveStepOperation(opPathNoHash, spec, loaded));

		OpenAPI emptyApi = new OpenAPI();
		OpenAPI nullPathItemsApi = new OpenAPI();
		nullPathItemsApi.paths = new openapi.Paths();
		nullPathItemsApi.paths.pathItems = null;
		Map<String, OpenAPI> emptySources = new LinkedHashMap<>();
		emptySources.put("empty", emptyApi);
		emptySources.put("nullItems", nullPathItemsApi);
		assertNull(ArazzoResolver.resolveStepOperation(prefixedStep, spec, emptySources));
		assertNull(ArazzoResolver.resolveStepOperation(unfoundOpStep, spec, emptySources));
		assertNull(ArazzoResolver.resolveStepOperation(opPathStep, spec, emptySources));

		assertNull(ArazzoResolver.resolveParameter(new ReusableOr<>(null, null), spec));
		assertNull(ArazzoResolver.resolveParameter(ReusableOr.ofReusable(new ReusableObject()), spec));
		assertNull(ArazzoResolver.resolveParameter(ReusableOr.ofReference("$components.parameters.x"), null));
		assertNull(ArazzoResolver.resolveParameter(ReusableOr.ofReference("$components.parameters.x"), new Arazzo()));
		Arazzo specEmptyComp = new Arazzo();
		specEmptyComp.components = new ArazzoComponents();
		assertNull(ArazzoResolver.resolveParameter(ReusableOr.ofReference("$components.parameters.x"), specEmptyComp));
		assertNull(ArazzoResolver.resolveSuccessAction(new ReusableOr<>(null, null), spec));
		assertNull(ArazzoResolver.resolveSuccessAction(ReusableOr.ofReusable(new ReusableObject()), spec));
		assertNull(ArazzoResolver.resolveSuccessAction(ReusableOr.ofReference("$components.successActions.x"), null));
		assertNull(ArazzoResolver.resolveSuccessAction(ReusableOr.ofReference("$components.successActions.x"),
				new Arazzo()));
		assertNull(ArazzoResolver.resolveSuccessAction(ReusableOr.ofReference("$components.successActions.x"),
				specEmptyComp));
		assertNull(ArazzoResolver.resolveFailureAction(new ReusableOr<>(null, null), spec));
		assertNull(ArazzoResolver.resolveFailureAction(ReusableOr.ofReusable(new ReusableObject()), spec));
		assertNull(ArazzoResolver.resolveFailureAction(ReusableOr.ofReference("$components.failureActions.x"), null));
		assertNull(ArazzoResolver.resolveFailureAction(ReusableOr.ofReference("$components.failureActions.x"),
				new Arazzo()));
		assertNull(ArazzoResolver.resolveFailureAction(ReusableOr.ofReference("$components.failureActions.x"),
				specEmptyComp));

		Workflow wfRefCheck = new Workflow();
		Reference rCheck = new Reference();
		rCheck.ref = "$components.inputs.LoginInput";
		wfRefCheck.inputs = ReferenceOr.ofRef(rCheck);
		assertNull(ArazzoResolver.resolveWorkflowInputs(wfRefCheck, null));
		assertNull(ArazzoResolver.resolveWorkflowInputs(wfRefCheck, new Arazzo()));
		assertNull(ArazzoResolver.resolveWorkflowInputs(wfRefCheck, specEmptyComp));
		assertTrue(ArazzoResolver.sortWorkflowsTopologically(new Arazzo()).isEmpty());

		spec.components = new ArazzoComponents();
		spec.components.parameters = new LinkedHashMap<>();
		ArazzoParameter baseParam = new ArazzoParameter();
		baseParam.name = "limit";
		baseParam.in = ParameterIn.QUERY;
		baseParam.value = PayloadValue.ofNumber(10);
		spec.components.parameters.put("limitParam", baseParam);

		assertNull(ArazzoResolver.resolveParameter(null, spec));
		assertEquals(baseParam, ArazzoResolver.resolveParameter(ReusableOr.ofItem(baseParam), spec));
		ReusableObject paramRef = new ReusableObject();
		paramRef.reference = "$components.parameters.limitParam";
		paramRef.value = PayloadValue.ofNumber(25);
		ArazzoParameter resolvedParam = ArazzoResolver.resolveParameter(ReusableOr.ofReusable(paramRef), spec);
		assertNotNull(resolvedParam);
		assertEquals(PayloadValue.ofNumber(25), resolvedParam.value);
		ArazzoParameter resolvedNoOverride = ArazzoResolver
				.resolveParameter(ReusableOr.ofReference("$components.parameters.limitParam"), spec);
		assertEquals(PayloadValue.ofNumber(10), resolvedNoOverride.value);
		assertNull(ArazzoResolver.resolveParameter(ReusableOr.ofReference("$components.parameters.missing"), spec));

		spec.components.successActions = new LinkedHashMap<>();
		SuccessAction sa = new SuccessAction();
		sa.name = "endAct";
		sa.type = SuccessActionType.END;
		spec.components.successActions.put("endAct", sa);
		assertNull(ArazzoResolver.resolveSuccessAction(null, spec));
		assertEquals(sa, ArazzoResolver.resolveSuccessAction(ReusableOr.ofItem(sa), spec));
		assertEquals(sa,
				ArazzoResolver.resolveSuccessAction(ReusableOr.ofReference("$components.successActions.endAct"), spec));
		assertNull(ArazzoResolver.resolveSuccessAction(ReusableOr.ofReference("$components.successActions.missing"),
				spec));

		spec.components.failureActions = new LinkedHashMap<>();
		FailureAction fa = new FailureAction();
		fa.name = "retryAct";
		fa.type = FailureActionType.RETRY;
		spec.components.failureActions.put("retryAct", fa);
		assertNull(ArazzoResolver.resolveFailureAction(null, spec));
		assertEquals(fa, ArazzoResolver.resolveFailureAction(ReusableOr.ofItem(fa), spec));
		assertEquals(fa, ArazzoResolver
				.resolveFailureAction(ReusableOr.ofReference("$components.failureActions.retryAct"), spec));
		assertNull(ArazzoResolver.resolveFailureAction(ReusableOr.ofReference("$components.failureActions.missing"),
				spec));

		spec.components.inputs = new LinkedHashMap<>();
		Schema inSchema = new Schema();
		inSchema.type = "object";
		spec.components.inputs.put("LoginInput", inSchema);
		assertNull(ArazzoResolver.resolveWorkflowInputs(null, spec));
		Workflow wf = new Workflow();
		assertNull(ArazzoResolver.resolveWorkflowInputs(wf, spec));
		wf.inputs = ReferenceOr.ofValue(inSchema);
		assertEquals(inSchema, ArazzoResolver.resolveWorkflowInputs(wf, spec));
		Reference r1 = new Reference();
		r1.ref = "$components.inputs.LoginInput";
		wf.inputs = ReferenceOr.ofRef(r1);
		assertEquals(inSchema, ArazzoResolver.resolveWorkflowInputs(wf, spec));
		Reference r2 = new Reference();
		r2.ref = "#/components/inputs/LoginInput";
		wf.inputs = ReferenceOr.ofRef(r2);
		assertEquals(inSchema, ArazzoResolver.resolveWorkflowInputs(wf, spec));

		assertTrue(ArazzoResolver.sortWorkflowsTopologically(null).isEmpty());
		Workflow wfA = new Workflow();
		wfA.workflowId = "wfA";
		wfA.dependsOn = Arrays.asList("wfB", "externalWf");
		Workflow wfB = new Workflow();
		wfB.workflowId = "wfB";
		spec.workflows = Arrays.asList(wfA, wfB);
		List<Workflow> sorted = ArazzoResolver.sortWorkflowsTopologically(spec);
		assertEquals("wfB", sorted.get(0).workflowId);
		assertEquals("wfA", sorted.get(1).workflowId);

		wfB.dependsOn = Collections.singletonList("wfA");
		try {
			ArazzoResolver.sortWorkflowsTopologically(spec);
			fail("Expected cycle exception");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("Circular workflow dependency"));
		}

		Files.deleteIfExists(oasFile.toPath());
		Files.deleteIfExists(tmpDir.toPath());
	}

	/**
	 * Helper to construct a {@link Selector}.
	 *
	 * @param context
	 *            The context expression.
	 * @param query
	 *            The selector query string.
	 * @param engine
	 *            The optional {@link CriterionEngineType}.
	 * @return The constructed {@link Selector}.
	 */
	private static Selector createSelector(String context, String query, CriterionEngineType engine) {
		Selector s = new Selector();
		s.context = context;
		s.selector = query;
		if (engine != null) {
			s.type = CriterionTypeOrExpression.ofSimple(engine);
		}
		return s;
	}
}
