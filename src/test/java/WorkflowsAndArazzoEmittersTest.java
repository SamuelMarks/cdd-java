import static org.junit.Assert.*;

import arazzo.*;
import common.PayloadValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.OpenAPI;
import openapi.ReferenceOr;
import openapi.Schema;
import org.junit.Test;

/**
 * Unit and round-trip tests for {@link workflows.Emit},
 * {@link workflows.Parse}, {@link cli.ArazzoCliEmitter},
 * {@link cli.ArazzoCliParser}, {@link tests.ArazzoTestEmitter},
 * {@link tests.ArazzoTestParser}, {@link mocks.ArazzoMockEmitter}, and
 * {@link mocks.ArazzoMockParser}.
 */
public class WorkflowsAndArazzoEmittersTest {
	/**
	 * Default constructor.
	 */
	public WorkflowsAndArazzoEmittersTest() {
	}

	/**
	 * Builds a rich sample {@link Arazzo} 1.1.0 specification for testing emitters
	 * and parsers.
	 *
	 * @return The sample {@link Arazzo} specification.
	 */
	private static Arazzo buildSampleArazzo() {
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.$self = "https://example.com/arazzo.json";
		spec.info = new ArazzoInfo();
		spec.info.title = "Petstore Order";
		spec.info.summary = "Order workflow summary";
		spec.info.description = "Order workflow description";
		spec.info.version = "1.1.0";

		SourceDescription sd = new SourceDescription();
		sd.name = "petstore";
		sd.type = SourceType.OPENAPI;
		sd.url = "./petstore.json";
		spec.sourceDescriptions = Collections.singletonList(sd);

		Workflow prereq = new Workflow();
		prereq.workflowId = "authFlow";
		Step authStep = new Step();
		authStep.stepId = "login";
		authStep.operationId = "loginUser";
		prereq.steps = Collections.singletonList(authStep);

		Workflow wf = new Workflow();
		wf.workflowId = "placeOrderFlow";
		wf.summary = "Place order";
		wf.description = "Places a pet order";
		wf.dependsOn = Arrays.asList("authFlow", "externalFlow");

		Schema inSchema = new Schema();
		inSchema.type = openapi.SchemaType.ofSingle("object");
		inSchema.properties = new LinkedHashMap<>();
		Schema petIdProp = new Schema();
		petIdProp.type = openapi.SchemaType.ofSingle("integer");
		Schema qtyProp = new Schema();
		qtyProp.type = openapi.SchemaType.ofSingle("integer");
		inSchema.properties.put("petId", ReferenceOr.ofValue(petIdProp));
		inSchema.properties.put("quantity", ReferenceOr.ofValue(qtyProp));
		wf.inputs = ReferenceOr.ofValue(inSchema);

		ArazzoParameter wfParam = new ArazzoParameter();
		wfParam.name = "X-Trace";
		wfParam.in = ParameterIn.HEADER;
		wfParam.value = PayloadValue.ofString("trace-1");
		ArazzoParameter wfParam2 = new ArazzoParameter();
		wfParam2.name = "X-Env";
		wfParam2.in = ParameterIn.HEADER;
		wfParam2.value = PayloadValue.ofString("prod");
		wf.parameters = Arrays.asList(ReusableOr.ofItem(wfParam), ReusableOr.ofItem(wfParam2));

		Step s1 = new Step();
		s1.stepId = "checkPet";
		s1.operationId = "getPetById";
		ArazzoParameter pPath = new ArazzoParameter();
		pPath.name = "petId";
		pPath.in = ParameterIn.PATH;
		pPath.value = PayloadValue.ofString("$inputs.petId");
		ArazzoParameter pQuery = new ArazzoParameter();
		pQuery.name = "mode";
		pQuery.in = ParameterIn.QUERY;
		pQuery.value = PayloadValue.ofNumber(1);
		ArazzoParameter pDouble = new ArazzoParameter();
		pDouble.name = "weight";
		pDouble.in = ParameterIn.QUERY;
		pDouble.value = PayloadValue.ofNumber(1.5);
		s1.parameters = Arrays.asList(ReusableOr.ofItem(pPath), ReusableOr.ofItem(pQuery), ReusableOr.ofItem(pDouble));
		s1.requestBody = new ArazzoRequestBody();
		s1.requestBody.payload = PayloadValue.ofObject(Collections.singletonMap("k", PayloadValue.ofString("v")));
		PayloadReplacement repl1 = new PayloadReplacement();
		repl1.target = "/k";
		repl1.value = PayloadValue.ofString("v2");
		PayloadReplacement repl2 = new PayloadReplacement();
		repl2.target = "/k2";
		repl2.value = PayloadValue.ofString("v3");
		s1.requestBody.replacements = Arrays.asList(repl1, repl2);
		Criterion c1 = new Criterion();
		c1.context = "$statusCode";
		c1.condition = "$statusCode == 200";
		Criterion c2 = new Criterion();
		c2.condition = "$statusCode != 500";
		c2.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.SIMPLE);
		s1.successCriteria = Arrays.asList(c1, c2);
		SuccessAction saGoto = new SuccessAction();
		saGoto.name = "next";
		saGoto.type = SuccessActionType.GOTO;
		saGoto.stepId = "submitOrder";
		SuccessAction saAnon = new SuccessAction();
		saAnon.type = SuccessActionType.END;
		s1.onSuccess = Arrays.asList(ReusableOr.ofItem(saGoto), ReusableOr.ofItem(saAnon));
		FailureAction faGoto = new FailureAction();
		faGoto.name = "fallback";
		faGoto.type = FailureActionType.GOTO;
		faGoto.stepId = "submitOrder";
		FailureAction faAnon = new FailureAction();
		faAnon.type = FailureActionType.END;
		s1.onFailure = Arrays.asList(ReusableOr.ofItem(faGoto), ReusableOr.ofItem(faAnon));
		s1.outputs = new LinkedHashMap<>();
		s1.outputs.put("petStatus", OutputValue.ofExpression("$response.body#/status"));
		Selector sel = new Selector();
		sel.selector = "$.id";
		s1.outputs.put("selectedId", OutputValue.ofSelector(sel));

		Step s2 = new Step();
		s2.stepId = "submitOrder";
		s2.operationId = "placeOrder";
		s2.requestBody = new ArazzoRequestBody();
		s2.requestBody.payload = PayloadValue.ofString("{\"petId\":\"$inputs.petId\"}");
		SuccessAction saEnd = new SuccessAction();
		saEnd.name = "finish";
		saEnd.type = SuccessActionType.END;
		s2.onSuccess = Collections.singletonList(ReusableOr.ofItem(saEnd));

		wf.steps = Arrays.asList(s1, s2);
		wf.outputs = new LinkedHashMap<>();
		wf.outputs.put("finalStatus", OutputValue.ofExpression("$steps.checkPet.outputs.petStatus"));
		wf.outputs.put("finalSelected", OutputValue.ofSelector(sel));

		spec.workflows = Arrays.asList(prereq, wf);
		return spec;
	}

	/**
	 * Verifies {@link workflows.Emit} and {@link workflows.Parse} round-tripping,
	 * LexicalPreservingPrinter updates, and fallback AST parsing without Base64
	 * Javadoc embedding.
	 */
	@Test
	public void testWorkflowsEmitAndParse() throws Exception {
		assertNotNull(new workflows.Emit());
		assertNotNull(new workflows.Parse());

		Arazzo spec = buildSampleArazzo();
		String specJson = arazzo.Emit.toString(spec);
		Arazzo specFromJson = arazzo.Parse.fromString(specJson);
		assertEquals(spec, specFromJson);

		String javaCode = workflows.Emit.emit(spec);
		assertFalse(javaCode.contains("arazzoSpecBase64"));
		assertTrue(javaCode.contains("class PetstoreOrderWorkflows"));
		assertTrue(javaCode.contains("PlaceOrderFlowInputs"));
		assertTrue(javaCode.contains("PlaceOrderFlowOutputs"));

		Arazzo roundTripped = workflows.Parse.parse(javaCode);
		assertEquals(spec, roundTripped);

		Map<String, OpenAPI> sources = Collections.singletonMap("petstore", new OpenAPI());
		String updatedCode = workflows.Emit.emit(spec, sources, javaCode);
		assertTrue(updatedCode.contains("placeOrderFlow"));

		Map<String, String> modular = workflows.Emit.emitModular(spec, sources);
		assertTrue(modular.containsKey("workflows/PetstoreOrderWorkflows.java"));

		// Test structural AST fallback when step Javadoc tags are absent
		String strippedCode = javaCode.replace("@stepMeta ", "@ignoredMeta ").replace("@workflowInput ", "@ignoredIn ")
				.replace("@workflowOutput ", "@ignoredOut ");
		Arazzo astParsed = workflows.Parse.parse(strippedCode);
		assertEquals("Petstore Order", astParsed.info.title);
		assertEquals(2, astParsed.workflows.size());
		assertEquals(2, astParsed.workflows.get(1).steps.size());

		String manualWorkflowCode = "/**\n" + " * Manual workflow class.\n" + " * @sourceDescription malformed\n"
				+ " */\n" + "public class ManualWorkflows {\n"
				+ "    public static class CustomIn { public String query; }\n"
				+ "    public static class CustomOut { public String res; }\n" + "    /**\n"
				+ "     * @workflowInput bad\n" + "     * @workflowParam bad\n" + "     * @stepMeta bad\n"
				+ "     * @stepParam missing in n str:v\n" + "     * @stepParam bad\n"
				+ "     * @stepBody missing str:v\n" + "     * @stepBody bad\n"
				+ "     * @stepReplacement missing /t str:v\n" + "     * @stepReplacement bad\n"
				+ "     * @stepCriterion missing - - c\n" + "     * @stepCriterion bad\n"
				+ "     * @stepOnSuccess missing end - -\n" + "     * @stepOnSuccess bad\n"
				+ "     * @stepOnFailure missing end - -\n" + "     * @stepOnFailure bad\n"
				+ "     * @stepOutput missing k expr v\n" + "     * @stepOutput bad\n" + "     * @workflowOutput bad\n"
				+ "     * @stepMeta sNoBody -\n" + "     * @stepReplacement sNoBody /ptr str:val\n"
				+ "     * @workflowParam query bare rawToken\n" + "     */\n"
				+ "    public CustomOut runCustom(CustomIn in) {\n" + "        put(\"k\", \"v\");\n"
				+ "        fetchFirst();\n" + "        fetchSecond();\n" + "        return new CustomOut();\n"
				+ "    }\n" + "    public void runCallsOnly() {\n" + "        put(\"k\", \"v\");\n"
				+ "        fetchFirst();\n" + "    }\n" + "}\n";
		Arazzo manualParsed = workflows.Parse.parse(manualWorkflowCode);
		assertEquals(2, manualParsed.workflows.size());
		assertEquals("runCustom", manualParsed.workflows.get(0).workflowId);
		assertEquals(1, manualParsed.workflows.get(0).steps.size());
		assertNull(manualParsed.workflows.get(0).steps.get(0).operationId);
		assertEquals(1, manualParsed.workflows.get(1).steps.size());

		// Edge cases for workflows.Emit
		Arazzo emptySpec = new Arazzo();
		emptySpec.arazzo = null;
		assertEquals("ArazzoWorkflows", workflows.Emit.deriveClassName(null));
		assertEquals("ArazzoWorkflows", workflows.Emit.deriveClassName(emptySpec));
		emptySpec.info = new ArazzoInfo();
		assertEquals("ArazzoWorkflows", workflows.Emit.deriveClassName(emptySpec));
		emptySpec.info.title = "!!!";
		assertEquals("ArazzoWorkflows", workflows.Emit.deriveClassName(emptySpec));
		emptySpec.info.title = "CustomWorkflows";
		assertEquals("CustomWorkflows", workflows.Emit.deriveClassName(emptySpec));
		Arazzo noTitleSpec = new Arazzo();
		noTitleSpec.info = new ArazzoInfo();
		assertNotNull(workflows.Emit.emit(noTitleSpec));

		SourceDescription sdNoType = new SourceDescription();
		sdNoType.name = "s";
		sdNoType.url = "./s.json";
		emptySpec.sourceDescriptions = Collections.singletonList(sdNoType);

		Workflow wfMinimal = new Workflow();
		wfMinimal.workflowId = "minWf";
		Schema nullPropsSchema = new Schema();
		nullPropsSchema.type = openapi.SchemaType.ofSingle("object");
		wfMinimal.inputs = ReferenceOr.ofValue(nullPropsSchema);
		ArazzoParameter paramNoIn = new ArazzoParameter();
		paramNoIn.name = "q1";
		paramNoIn.value = PayloadValue.ofString("v1");
		wfMinimal.parameters = Arrays.asList(ReusableOr.ofItem(paramNoIn),
				ReusableOr.ofReference("$components.parameters.missing"));
		wfMinimal.steps = Collections.emptyList();
		Workflow wfNullSteps = new Workflow();
		wfNullSteps.workflowId = "nullStepsWf";
		Step stepMinimal = new Step();
		stepMinimal.stepId = "stMin";
		ArazzoParameter cookieParam = new ArazzoParameter();
		cookieParam.name = "sid";
		cookieParam.in = ParameterIn.COOKIE;
		cookieParam.value = PayloadValue.ofString("c1");
		ArazzoParameter stepParamNoIn = new ArazzoParameter();
		stepParamNoIn.name = "q2";
		stepParamNoIn.value = PayloadValue.ofString("v2");
		stepMinimal.parameters = Arrays.asList(ReusableOr.ofItem(cookieParam), ReusableOr.ofItem(stepParamNoIn),
				ReusableOr.ofReference("$components.parameters.missing"));
		stepMinimal.requestBody = new ArazzoRequestBody();
		Criterion critWithType = new Criterion();
		critWithType.condition = "$statusCode == 200";
		critWithType.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.SIMPLE);
		stepMinimal.successCriteria = Collections.singletonList(critWithType);
		SuccessAction saNoTarget = new SuccessAction();
		saNoTarget.type = SuccessActionType.GOTO;
		SuccessAction saNullType = new SuccessAction();
		stepMinimal.onSuccess = Arrays.asList(ReusableOr.ofItem(saNoTarget), ReusableOr.ofItem(saNullType));
		FailureAction faNoTarget = new FailureAction();
		faNoTarget.type = FailureActionType.END;
		FailureAction faNullType = new FailureAction();
		stepMinimal.onFailure = Arrays.asList(ReusableOr.ofItem(faNoTarget), ReusableOr.ofItem(faNullType));

		Step stepUnresolvedActions = new Step();
		stepUnresolvedActions.stepId = "stUnres";
		stepUnresolvedActions.onSuccess = Collections
				.singletonList(ReusableOr.ofReference("$components.successActions.missing"));
		stepUnresolvedActions.onFailure = Collections
				.singletonList(ReusableOr.ofReference("$components.failureActions.missing"));

		Step stepEmptyActions = new Step();
		stepEmptyActions.stepId = "stEmptyAct";
		stepEmptyActions.onSuccess = Collections.emptyList();
		stepEmptyActions.onFailure = Collections.emptyList();

		Workflow wfWithSteps = new Workflow();
		wfWithSteps.workflowId = "stepsWf";
		Schema emptyPropsSchema = new Schema();
		emptyPropsSchema.type = openapi.SchemaType.ofSingle("object");
		emptyPropsSchema.properties = new LinkedHashMap<>();
		emptyPropsSchema.properties.put("untypedProp", ReferenceOr.ofValue(new Schema()));
		emptyPropsSchema.properties.put("nonSchemaProp", null);
		wfWithSteps.inputs = ReferenceOr.ofValue(emptyPropsSchema);
		wfWithSteps.steps = Arrays.asList(stepMinimal, stepUnresolvedActions, stepEmptyActions);
		emptySpec.workflows = Arrays.asList(wfMinimal, wfNullSteps, wfWithSteps);

		String emittedDirect = workflows.Emit.emit(emptySpec);
		Arazzo parsedDirect = workflows.Parse.parse(emittedDirect);
		assertEquals(3, parsedDirect.workflows.size());

		String emittedMinimal = workflows.Emit.emit(emptySpec, Collections.emptyMap(), "package custom;\n");
		assertTrue(emittedMinimal.contains("CustomWorkflows"));
		String emittedBlankExisting = workflows.Emit.emit(new Arazzo(), null, "   ");
		assertTrue(emittedBlankExisting.contains("ArazzoWorkflows"));
	}

	/**
	 * Verifies {@link cli.ArazzoCliEmitter} and {@link cli.ArazzoCliParser}
	 * round-tripping and structural fallback parsing without Base64 embedding.
	 */
	@Test
	public void testArazzoCliEmitterAndParser() {
		assertNotNull(new cli.ArazzoCliEmitter());
		assertNotNull(new cli.ArazzoCliParser());

		Arazzo spec = buildSampleArazzo();
		Map<String, String> mod = cli.ArazzoCliEmitter.emitModular(spec);
		String cliCode = mod.get("cli/WorkflowCli.java");
		assertNotNull(cliCode);
		assertFalse(cliCode.contains("arazzoSpecBase64"));
		assertFalse(cliCode.contains("ArazzoSpecBase64"));

		Arazzo rt = cli.ArazzoCliParser.parseWorkflowCli(cliCode);
		assertEquals(spec, rt);

		// Test fallback to printHelp() string literals when Javadoc tags are absent
		String noJavadocWf = cliCode.replace("@workflowId ", "@ignoredWf ").replace("@sourceDescription ",
				"@ignoredSd ");
		Arazzo rtLines = cli.ArazzoCliParser.parseWorkflowCli(noJavadocWf);
		assertEquals("Petstore Order", rtLines.info.title);
		assertEquals("1.1.0", rtLines.info.version);
		assertEquals(1, rtLines.sourceDescriptions.size());
		assertEquals(2, rtLines.workflows.size());

		// Test CLI emitter and parser with minimal/empty Arazzo fields
		Arazzo emptyCliSpec = new Arazzo();
		emptyCliSpec.arazzo = null;
		assertNotNull(cli.ArazzoCliEmitter.emitWorkflowCli(emptyCliSpec));
		emptyCliSpec.info = new ArazzoInfo();
		SourceDescription sdNoType = new SourceDescription();
		sdNoType.name = "plainSource";
		sdNoType.url = "./p.json";
		emptyCliSpec.sourceDescriptions = Collections.singletonList(sdNoType);
		Workflow wfEmpty = new Workflow();
		wfEmpty.workflowId = "w1";
		Schema inSchemaMulti = new Schema();
		inSchemaMulti.properties = new LinkedHashMap<>();
		inSchemaMulti.properties.put("p1", ReferenceOr.ofValue(new Schema()));
		inSchemaMulti.properties.put("p2", ReferenceOr.ofValue(new Schema()));
		wfEmpty.inputs = ReferenceOr.ofValue(inSchemaMulti);
		Workflow wfNoProps = new Workflow();
		wfNoProps.workflowId = "w2";
		wfNoProps.inputs = ReferenceOr.ofValue(new Schema());
		emptyCliSpec.workflows = Arrays.asList(wfEmpty, wfNoProps);
		String emptyCliCode = cli.ArazzoCliEmitter.emitWorkflowCli(emptyCliSpec);
		assertNotNull(emptyCliCode);

		String manualCliCode = "public class CustomCli { public static void printHelp() { "
				+ "System.out.println(\"Source: plainSource -> ./p.json\"); " + "System.out.println(\"Workflow: w1\"); "
				+ "System.out.println(\"  Input: --p1\"); " + "System.out.println(\"  Input: --p2\"); " + "} }";
		Arazzo parsedManualCli = cli.ArazzoCliParser.parseWorkflowCli(manualCliCode);
		assertEquals("plainSource", parsedManualCli.sourceDescriptions.get(0).name);
		assertEquals(2, parsedManualCli.workflows.get(0).inputs.value.properties.size());
	}

	/**
	 * Verifies {@link tests.ArazzoTestEmitter}, {@link tests.ArazzoTestParser},
	 * {@link mocks.ArazzoMockEmitter}, and {@link mocks.ArazzoMockParser} without
	 * Base64 Javadoc embedding.
	 */
	@Test
	public void testArazzoTestAndMockEmittersAndParsers() {
		assertNotNull(new tests.ArazzoTestEmitter());
		assertNotNull(new tests.ArazzoTestParser());
		assertNotNull(new mocks.ArazzoMockEmitter());
		assertNotNull(new mocks.ArazzoMockParser());

		Arazzo spec = buildSampleArazzo();
		String testCode = tests.ArazzoTestEmitter.emit(spec);
		assertFalse(testCode.contains("arazzoSpecBase64"));
		assertTrue(testCode.contains("test_placeOrderFlow"));

		Arazzo rtTest = tests.ArazzoTestParser.parse(testCode);
		assertEquals(spec, rtTest);

		String testNoStepMeta = testCode.replace("@stepMeta ", "@ignoredStep ");
		Arazzo rtTestAst = tests.ArazzoTestParser.parse(testNoStepMeta);
		assertEquals(2, rtTestAst.workflows.size());

		Arazzo minTestSpec = new Arazzo();
		minTestSpec.arazzo = null;
		Workflow wfNoSteps = new Workflow();
		wfNoSteps.workflowId = "noSteps";
		Workflow wfOtherCrit = new Workflow();
		wfOtherCrit.workflowId = "otherCrit";
		Step stCrit = new Step();
		stCrit.stepId = "s";
		Criterion cNoCond = new Criterion();
		Criterion c404 = new Criterion();
		c404.condition = "$statusCode == 404";
		stCrit.successCriteria = Arrays.asList(cNoCond, c404);
		wfOtherCrit.steps = Collections.singletonList(stCrit);
		minTestSpec.workflows = Arrays.asList(wfNoSteps, wfOtherCrit);
		assertNotNull(tests.ArazzoTestEmitter.emit(minTestSpec));
		assertNotNull(tests.ArazzoTestParser.parse("public class PlainTest { public void verifySomething() {} }"));

		Map<String, String> mockMod = mocks.ArazzoMockEmitter.emitModular(spec);
		String mockCode = mockMod.values().iterator().next();
		assertFalse(mockCode.contains("arazzoSpecBase64"));
		assertTrue(mockCode.contains("mock_placeOrderFlow"));

		Arazzo rtMock = mocks.ArazzoMockParser.parse(mockCode);
		assertEquals(spec, rtMock);

		String mockNoStepMeta = mockCode.replace("@stepMeta ", "@ignoredStep ");
		Arazzo rtMockAst = mocks.ArazzoMockParser.parse(mockNoStepMeta);
		assertEquals(2, rtMockAst.workflows.size());

		Arazzo emptyMockSpec = new Arazzo();
		emptyMockSpec.arazzo = null;
		assertNotNull(mocks.ArazzoMockEmitter.emit(emptyMockSpec));
		emptyMockSpec.workflows = Collections.singletonList(wfNoSteps);
		assertNotNull(mocks.ArazzoMockEmitter.emit(emptyMockSpec));
		assertNotNull(
				mocks.ArazzoMockParser.parse("public class PlainMock { public void custom() { put(\"a\", \"b\"); } }"));
	}

	/**
	 * Verifies exhaustive round-trip and edge-case coverage of
	 * {@link ArazzoJavadocCodec} across reusable components, workflow actions, step
	 * attributes, JSON selectors, and specification extensions.
	 */
	@Test
	public void testExhaustiveArazzoJavadocCodecRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.$self = "https://example.com/exhaustive.json";
		spec.extensions.put("x-root", PayloadValue.ofString("rVal"));
		spec.info = new ArazzoInfo();
		spec.info.title = "Exhaustive";
		spec.info.summary = "Sum";
		spec.info.description = "Desc";
		spec.info.version = "1.1.0";
		spec.info.extensions.put("x-info", PayloadValue.ofNumber(42));

		SourceDescription sd1 = new SourceDescription();
		sd1.name = "api1";
		sd1.type = SourceType.OPENAPI;
		sd1.url = "./api1.json";
		sd1.extensions.put("x-sd", PayloadValue.ofString("sdVal"));
		SourceDescription sd2 = new SourceDescription();
		sd2.name = "api2";
		sd2.type = SourceType.ARAZZO;
		sd2.url = "./api2.json";
		spec.sourceDescriptions = Arrays.asList(sd1, sd2);

		spec.components = new ArazzoComponents();
		spec.components.inputs = new LinkedHashMap<>();
		Schema compSchema = new Schema();
		compSchema.type = openapi.SchemaType.ofSingle("object");
		compSchema.properties = new LinkedHashMap<>();
		Schema strSchema = new Schema();
		strSchema.type = openapi.SchemaType.ofSingle("string");
		compSchema.properties.put("apiKey", ReferenceOr.ofValue(strSchema));
		spec.components.inputs.put("AuthIn", compSchema);
		spec.components.inputs.put("OtherIn", compSchema);

		spec.components.parameters = new LinkedHashMap<>();
		ArazzoParameter compParam = new ArazzoParameter();
		compParam.name = "X-Key";
		compParam.in = ParameterIn.HEADER;
		compParam.value = PayloadValue.ofString("$inputs.apiKey");
		compParam.extensions.put("x-cp", PayloadValue.ofString("cp"));
		spec.components.parameters.put("sharedKey", compParam);
		spec.components.parameters.put("sharedKey2", compParam);

		spec.components.successActions = new LinkedHashMap<>();
		SuccessAction compSa = new SuccessAction();
		compSa.name = "compDone";
		compSa.type = SuccessActionType.END;
		spec.components.successActions.put("doneAct", compSa);
		spec.components.successActions.put("doneAct2", compSa);

		spec.components.failureActions = new LinkedHashMap<>();
		FailureAction compFa = new FailureAction();
		compFa.name = "compRetry";
		compFa.type = FailureActionType.RETRY;
		compFa.retryAfter = 2.0;
		compFa.retryLimit = 3;
		spec.components.failureActions.put("retryAct", compFa);
		spec.components.failureActions.put("retryAct2", compFa);
		spec.components.extensions.put("x-comp", PayloadValue.ofString("cVal"));

		Workflow wf = new Workflow();
		wf.workflowId = "complexFlow";
		openapi.Reference inRef = new openapi.Reference();
		inRef.ref = "$components.inputs.AuthIn";
		wf.inputs = ReferenceOr.ofRef(inRef);
		wf.extensions.put("x-wf", PayloadValue.ofString("wfVal"));

		ArazzoParameter wfExtParam = new ArazzoParameter();
		wfExtParam.name = "pExt";
		wfExtParam.in = ParameterIn.QUERY;
		wfExtParam.value = PayloadValue.ofString("v");
		wfExtParam.extensions.put("x-p", PayloadValue.ofString("pv"));
		wf.parameters = Arrays.asList(ReusableOr.ofReference("$components.parameters.sharedKey"),
				ReusableOr.ofItem(wfExtParam), new ReusableOr<>(null, null));

		SuccessAction wfSa = new SuccessAction();
		wfSa.name = "wfGoto";
		wfSa.type = SuccessActionType.GOTO;
		wfSa.workflowId = "otherWf";
		wf.successActions = Arrays.asList(ReusableOr.ofReference("$components.successActions.doneAct"),
				ReusableOr.ofItem(wfSa));

		FailureAction wfFa = new FailureAction();
		wfFa.name = "wfFail";
		wfFa.type = FailureActionType.END;
		wf.failureActions = Arrays.asList(ReusableOr.ofReference("$components.failureActions.retryAct"),
				ReusableOr.ofItem(wfFa));

		Step s = new Step();
		s.stepId = "richStep";
		s.description = "Line 1\nLine 2";
		s.operationPath = "{$sourceDescriptions.api1.url}#/paths/~1items/get";
		s.workflowId = "subWf";
		s.channelPath = "/events";
		s.timeout = 1000;
		s.correlationId = "$request.header.X-Corr";
		s.action = StepActionType.SEND;
		s.dependsOn = Arrays.asList("dep1", "dep2");
		s.extensions.put("x-st", PayloadValue.ofString("stVal"));

		ArazzoParameter stExtParam = new ArazzoParameter();
		stExtParam.name = "spExt";
		stExtParam.in = ParameterIn.HEADER;
		stExtParam.value = PayloadValue.ofString("sv");
		stExtParam.extensions.put("x-sp", PayloadValue.ofString("spv"));
		s.parameters = Arrays.asList(ReusableOr.ofReference("$components.parameters.sharedKey"),
				ReusableOr.ofItem(stExtParam), new ReusableOr<>(null, null));

		s.requestBody = new ArazzoRequestBody();
		s.requestBody.contentType = "application/json";
		s.requestBody.payload = PayloadValue.ofString("{\"a\":1}");

		Step sBodyExt = new Step();
		sBodyExt.stepId = "bodyExtStep";
		sBodyExt.requestBody = new ArazzoRequestBody();
		sBodyExt.requestBody.extensions.put("x-rb", PayloadValue.ofString("rbv"));

		Step sReplJson = new Step();
		sReplJson.stepId = "replJsonStep";
		sReplJson.requestBody = new ArazzoRequestBody();
		PayloadReplacement pr1 = new PayloadReplacement();
		pr1.target = "/a";
		pr1.targetSelectorType = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPOINTER);
		pr1.value = PayloadValue.ofString("x");
		PayloadReplacement pr2 = new PayloadReplacement();
		pr2.target = "/b";
		pr2.value = PayloadValue.ofString("y");
		pr2.extensions.put("x-pr", PayloadValue.ofString("prv"));
		sReplJson.requestBody.replacements = Arrays.asList(pr1, pr2);

		Criterion critExt = new Criterion();
		critExt.condition = "$statusCode == 200";
		critExt.extensions.put("x-cr", PayloadValue.ofString("crv"));
		ExpressionType et = new ExpressionType();
		et.type = CriterionEngineType.JSONPATH;
		et.version = "rfc9535";
		Criterion critExpr = new Criterion();
		critExpr.condition = "$.ok == true";
		critExpr.type = CriterionTypeOrExpression.ofExpressionType(et);
		s.successCriteria = Arrays.asList(critExt, critExpr);

		SuccessAction saWf = new SuccessAction();
		saWf.name = "saWf";
		saWf.type = SuccessActionType.GOTO;
		saWf.workflowId = "w2";
		SuccessAction saParams = new SuccessAction();
		saParams.name = "saP";
		saParams.type = SuccessActionType.GOTO;
		saParams.parameters = Collections.singletonList(ReusableOr.ofItem(stExtParam));
		SuccessAction saCrit = new SuccessAction();
		saCrit.name = "saC";
		saCrit.type = SuccessActionType.END;
		saCrit.criteria = Collections.singletonList(critExt);
		SuccessAction saExt = new SuccessAction();
		saExt.name = "saE";
		saExt.type = SuccessActionType.END;
		saExt.extensions.put("x-sa", PayloadValue.ofString("sav"));
		s.onSuccess = Arrays.asList(ReusableOr.ofReference("$components.successActions.doneAct"),
				ReusableOr.ofItem(saWf), ReusableOr.ofItem(saParams), ReusableOr.ofItem(saCrit),
				ReusableOr.ofItem(saExt), new ReusableOr<>(null, null));

		FailureAction faWf = new FailureAction();
		faWf.name = "faWf";
		faWf.type = FailureActionType.GOTO;
		faWf.workflowId = "w2";
		FailureAction faParams = new FailureAction();
		faParams.name = "faP";
		faParams.type = FailureActionType.GOTO;
		faParams.parameters = Collections.singletonList(ReusableOr.ofItem(stExtParam));
		FailureAction faRetryAfter = new FailureAction();
		faRetryAfter.name = "faRA";
		faRetryAfter.type = FailureActionType.RETRY;
		faRetryAfter.retryAfter = 1.0;
		FailureAction faRetryLimit = new FailureAction();
		faRetryLimit.name = "faRL";
		faRetryLimit.type = FailureActionType.RETRY;
		faRetryLimit.retryLimit = 2;
		FailureAction faCrit = new FailureAction();
		faCrit.name = "faC";
		faCrit.type = FailureActionType.END;
		faCrit.criteria = Collections.singletonList(critExt);
		FailureAction faExt = new FailureAction();
		faExt.name = "faE";
		faExt.type = FailureActionType.END;
		faExt.extensions.put("x-fa", PayloadValue.ofString("fav"));
		s.onFailure = Arrays.asList(ReusableOr.ofReference("$components.failureActions.retryAct"),
				ReusableOr.ofItem(faWf), ReusableOr.ofItem(faParams), ReusableOr.ofItem(faRetryAfter),
				ReusableOr.ofItem(faRetryLimit), ReusableOr.ofItem(faCrit), ReusableOr.ofItem(faExt),
				new ReusableOr<>(null, null));

		Selector selCtx = new Selector();
		selCtx.context = "$response.body";
		selCtx.selector = "$.id";
		Selector selType = new Selector();
		selType.selector = "/id";
		selType.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.JSONPOINTER);
		Selector selExt = new Selector();
		selExt.selector = "$.name";
		selExt.extensions.put("x-s", PayloadValue.ofString("sv"));
		s.outputs = new LinkedHashMap<>();
		s.outputs.put("o1", OutputValue.ofSelector(selCtx));
		s.outputs.put("o2", OutputValue.ofSelector(selType));
		s.outputs.put("o3", OutputValue.ofSelector(selExt));

		wf.steps = Arrays.asList(s, sBodyExt, sReplJson);
		wf.outputs = new LinkedHashMap<>();
		wf.outputs.put("wo1", OutputValue.ofSelector(selCtx));
		wf.outputs.put("wo2", OutputValue.ofSelector(selType));
		wf.outputs.put("wo3", OutputValue.ofSelector(selExt));
		spec.workflows = Collections.singletonList(wf);

		String emittedJava = workflows.Emit.emit(spec);
		Arazzo parsedJava = workflows.Parse.parse(emittedJava);
		assertEquals(spec.extensions, parsedJava.extensions);
		assertEquals(spec.info, parsedJava.info);
		assertEquals(spec.sourceDescriptions, parsedJava.sourceDescriptions);
		assertEquals(spec.components, parsedJava.components);
		assertEquals(spec.workflows.get(0).inputs, parsedJava.workflows.get(0).inputs);
		assertEquals(spec.workflows.get(0).successActions, parsedJava.workflows.get(0).successActions);
		assertEquals(spec.workflows.get(0).failureActions, parsedJava.workflows.get(0).failureActions);
		assertEquals(spec.workflows.get(0).extensions, parsedJava.workflows.get(0).extensions);
		assertEquals(spec.workflows.get(0).outputs, parsedJava.workflows.get(0).outputs);
		assertEquals("Line 1 Line 2", parsedJava.workflows.get(0).steps.get(0).description);
		assertEquals(spec.workflows.get(0).steps.get(0).operationPath,
				parsedJava.workflows.get(0).steps.get(0).operationPath);
		assertEquals(spec.workflows.get(0).steps.get(0).workflowId,
				parsedJava.workflows.get(0).steps.get(0).workflowId);
		assertEquals(spec.workflows.get(0).steps.get(0).channelPath,
				parsedJava.workflows.get(0).steps.get(0).channelPath);
		assertEquals(spec.workflows.get(0).steps.get(0).timeout, parsedJava.workflows.get(0).steps.get(0).timeout);
		assertEquals(spec.workflows.get(0).steps.get(0).correlationId,
				parsedJava.workflows.get(0).steps.get(0).correlationId);
		assertEquals(spec.workflows.get(0).steps.get(0).action, parsedJava.workflows.get(0).steps.get(0).action);
		assertEquals(spec.workflows.get(0).steps.get(0).dependsOn, parsedJava.workflows.get(0).steps.get(0).dependsOn);
		assertEquals(spec.workflows.get(0).steps.get(0).extensions,
				parsedJava.workflows.get(0).steps.get(0).extensions);
		assertEquals(spec.workflows.get(0).steps.get(0).requestBody,
				parsedJava.workflows.get(0).steps.get(0).requestBody);
		assertEquals(spec.workflows.get(0).steps.get(0).successCriteria,
				parsedJava.workflows.get(0).steps.get(0).successCriteria);
		assertEquals(spec.workflows.get(0).steps.get(0).outputs, parsedJava.workflows.get(0).steps.get(0).outputs);
		assertEquals(spec.workflows.get(0).steps.get(2).requestBody,
				parsedJava.workflows.get(0).steps.get(2).requestBody);

		// Test malformed/missing step guard branches in ArazzoJavadocCodec
		Arazzo guardSpec = new Arazzo();
		guardSpec.info = new ArazzoInfo();
		ArazzoJavadocCodec.parseSpecJavadoc(
				"@arazzoExt bad\n@infoExt bad\n@sourceDescriptionExt s k str:v\n@sourceDescriptionExt bad\n"
						+ "@componentInput bad\n@componentParameter bad\n@componentSuccessAction bad\n"
						+ "@componentFailureAction bad\n@componentExt bad\n",
				guardSpec);
		guardSpec.sourceDescriptions = new java.util.ArrayList<>(Collections.singletonList(sd1));
		ArazzoJavadocCodec.parseSpecJavadoc("@sourceDescriptionExt missing k str:v\n", guardSpec);

		Workflow guardWf = new Workflow();
		guardWf.steps = new java.util.ArrayList<>();
		ArazzoJavadocCodec.parseWorkflowJavadoc("@workflowExt bad\n"
				+ "@stepDescription missing desc\n@stepDescription bad\n"
				+ "@stepOperationPath missing /p\n@stepOperationPath bad\n"
				+ "@stepWorkflowId missing w\n@stepWorkflowId bad\n"
				+ "@stepChannelPath missing /c\n@stepChannelPath bad\n" + "@stepTimeout missing 10\n@stepTimeout bad\n"
				+ "@stepCorrelationId missing id\n@stepCorrelationId bad\n"
				+ "@stepAction missing send\n@stepAction bad\n" + "@stepDependsOn missing d\n@stepDependsOn bad\n"
				+ "@stepExt missing x-k str:v\n@stepExt bad\n"
				+ "@stepParamJson missing {\"name\":\"p\"}\n@stepParamJson bad\n"
				+ "@stepRequestBodyJson missing {}\n@stepRequestBodyJson bad\n"
				+ "@stepReplacementJson missing {\"target\":\"/a\",\"value\":\"b\"}\n@stepReplacementJson bad\n"
				+ "@stepCriterionJson missing {\"condition\":\"true\"}\n@stepCriterionJson bad\n"
				+ "@stepOnSuccessJson missing {\"type\":\"end\"}\n@stepOnSuccessJson bad\n"
				+ "@stepOnFailureJson missing {\"type\":\"end\"}\n@stepOnFailureJson bad\n"
				+ "@stepMeta s1 -\n@stepReplacementJson s1 {\"target\":\"/a\",\"value\":\"b\"}\n", guardWf);
		assertEquals(1, guardWf.steps.size());
		assertEquals(1, guardWf.steps.get(0).requestBody.replacements.size());

		// Also test empty collections in spec.components
		Arazzo emptyCompSpec = new Arazzo();
		emptyCompSpec.components = new ArazzoComponents();
		StringBuilder emptyCompSb = new StringBuilder();
		ArazzoJavadocCodec.appendSpecJavadoc(emptyCompSb, " * ", emptyCompSpec);
		assertEquals("", emptyCompSb.toString());

		Workflow wfNullInRef = new Workflow();
		wfNullInRef.workflowId = "wNullRef";
		wfNullInRef.inputs = new ReferenceOr<>(null, null);
		StringBuilder wfSb = new StringBuilder();
		ArazzoJavadocCodec.appendWorkflowJavadoc(wfSb, " * ", wfNullInRef, emptyCompSpec);
		assertTrue(wfSb.toString().contains("@workflowId wNullRef"));
	}
}
