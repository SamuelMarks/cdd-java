import static org.junit.Assert.*;

import arazzo.*;
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
		inSchema.type = "object";
		inSchema.properties = new LinkedHashMap<>();
		Schema petIdProp = new Schema();
		petIdProp.type = "integer";
		inSchema.properties.put("petId", petIdProp);
		wf.inputs = ReferenceOr.ofValue(inSchema);

		ArazzoParameter wfParam = new ArazzoParameter();
		wfParam.name = "X-Trace";
		wfParam.in = ParameterIn.HEADER;
		wfParam.value = PayloadValue.ofString("trace-1");
		wf.parameters = Collections.singletonList(ReusableOr.ofItem(wfParam));

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
		s1.parameters = Arrays.asList(ReusableOr.ofItem(pPath), ReusableOr.ofItem(pQuery));
		s1.requestBody = new ArazzoRequestBody();
		s1.requestBody.payload = PayloadValue.ofObject(Collections.singletonMap("k", PayloadValue.ofString("v")));
		Criterion c1 = new Criterion();
		c1.context = "$statusCode";
		c1.condition = "$statusCode == 200";
		s1.successCriteria = Collections.singletonList(c1);
		SuccessAction saGoto = new SuccessAction();
		saGoto.name = "next";
		saGoto.type = SuccessActionType.GOTO;
		saGoto.stepId = "submitOrder";
		s1.onSuccess = Collections.singletonList(ReusableOr.ofItem(saGoto));
		FailureAction faGoto = new FailureAction();
		faGoto.name = "fallback";
		faGoto.type = FailureActionType.GOTO;
		faGoto.stepId = "submitOrder";
		s1.onFailure = Collections.singletonList(ReusableOr.ofItem(faGoto));
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
	 * LexicalPreservingPrinter updates, and fallback AST parsing.
	 */
	@Test
	public void testWorkflowsEmitAndParse() {
		assertNotNull(new workflows.Emit());
		assertNotNull(new workflows.Parse());

		Arazzo spec = buildSampleArazzo();
		String javaCode = workflows.Emit.emit(spec);
		assertTrue(javaCode.contains("class PetstoreOrderWorkflows"));
		assertTrue(javaCode.contains("PlaceOrderFlowInputs"));
		assertTrue(javaCode.contains("PlaceOrderFlowOutputs"));

		Arazzo roundTripped = workflows.Parse.parse(javaCode);
		assertEquals(spec.info, roundTripped.info);
		assertEquals(spec.workflows.size(), roundTripped.workflows.size());
		assertEquals(spec.workflows.get(1).workflowId, roundTripped.workflows.get(1).workflowId);

		Map<String, OpenAPI> sources = Collections.singletonMap("petstore", new OpenAPI());
		String updatedCode = workflows.Emit.emit(spec, sources, javaCode);
		assertTrue(updatedCode.contains("placeOrderFlow"));

		Map<String, String> modular = workflows.Emit.emitModular(spec, sources);
		assertTrue(modular.containsKey("workflows/PetstoreOrderWorkflows.java"));

		// Test structural AST fallback when @arazzoSpecBase64 is absent or corrupted
		String strippedCode = javaCode.replace("@arazzoSpecBase64 ", "@arazzoSpecBase64 !invalid!");
		Arazzo astParsed = workflows.Parse.parse(strippedCode);
		assertEquals("Petstore Order", astParsed.info.title);
		assertEquals(2, astParsed.workflows.size());

		String manualWorkflowCode = "/**\n" + " * Manual workflow class.\n" + " */\n"
				+ "public class ManualWorkflows {\n" + "    public static class CustomIn { public String query; }\n"
				+ "    public static class CustomOut { public String res; }\n"
				+ "    public CustomOut runCustom(CustomIn in) {\n" + "        put(\"k\", \"v\");\n"
				+ "        fetchFirst();\n" + "        fetchSecond();\n" + "        return new CustomOut();\n"
				+ "    }\n" + "}\n";
		Arazzo manualParsed = workflows.Parse.parse(manualWorkflowCode);
		assertEquals(1, manualParsed.workflows.size());
		assertEquals("runCustom", manualParsed.workflows.get(0).workflowId);
		assertEquals(2, manualParsed.workflows.get(0).steps.size());

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
		wfMinimal.parameters = Collections.singletonList(ReusableOr.ofReference("$components.parameters.missing"));
		wfMinimal.steps = Collections.emptyList();
		Workflow wfNullSteps = new Workflow();
		wfNullSteps.workflowId = "nullStepsWf";
		Step stepMinimal = new Step();
		stepMinimal.stepId = "stMin";
		ArazzoParameter cookieParam = new ArazzoParameter();
		cookieParam.name = "sid";
		cookieParam.in = ParameterIn.COOKIE;
		cookieParam.value = PayloadValue.ofString("c1");
		stepMinimal.parameters = Arrays.asList(ReusableOr.ofItem(cookieParam),
				ReusableOr.ofReference("$components.parameters.missing"));
		stepMinimal.requestBody = new ArazzoRequestBody();
		Criterion critWithType = new Criterion();
		critWithType.condition = "$statusCode == 200";
		critWithType.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.SIMPLE);
		stepMinimal.successCriteria = Collections.singletonList(critWithType);
		SuccessAction saNoTarget = new SuccessAction();
		saNoTarget.type = SuccessActionType.GOTO;
		stepMinimal.onSuccess = Collections.singletonList(ReusableOr.ofItem(saNoTarget));
		FailureAction faNoTarget = new FailureAction();
		faNoTarget.type = FailureActionType.END;
		stepMinimal.onFailure = Collections.singletonList(ReusableOr.ofItem(faNoTarget));

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
		emptyPropsSchema.type = "object";
		wfWithSteps.inputs = ReferenceOr.ofValue(emptyPropsSchema);
		wfWithSteps.steps = Arrays.asList(stepMinimal, stepUnresolvedActions, stepEmptyActions);
		emptySpec.workflows = Arrays.asList(wfMinimal, wfNullSteps, wfWithSteps);

		String emittedMinimal = workflows.Emit.emit(emptySpec, Collections.emptyMap(), "package custom;\n");
		assertTrue(emittedMinimal.contains("CustomWorkflows"));
		String emittedBlankExisting = workflows.Emit.emit(new Arazzo(), null, "   ");
		assertTrue(emittedBlankExisting.contains("ArazzoWorkflows"));
	}

	/**
	 * Verifies {@link cli.ArazzoCliEmitter} and {@link cli.ArazzoCliParser}
	 * round-tripping and structural fallback parsing.
	 */
	@Test
	public void testArazzoCliEmitterAndParser() {
		assertNotNull(new cli.ArazzoCliEmitter());
		assertNotNull(new cli.ArazzoCliParser());

		Arazzo spec = buildSampleArazzo();
		Map<String, String> mod = cli.ArazzoCliEmitter.emitModular(spec);
		String cliCode = mod.get("cli/WorkflowCli.java");
		assertNotNull(cliCode);

		Arazzo rt = cli.ArazzoCliParser.parseWorkflowCli(cliCode);
		assertEquals(spec.info.title, rt.info.title);
		assertEquals(spec.workflows.size(), rt.workflows.size());

		// Test fallback to printHelp() string literals when Javadoc base64 is removed
		String noJavadocB64 = cliCode.replace("@arazzoSpecBase64 ", "@arazzoSpecBase64 !bad!");
		Arazzo rtFromHelp = cli.ArazzoCliParser.parseWorkflowCli(noJavadocB64);
		assertEquals(spec.info.title, rtFromHelp.info.title);

		// Test fallback to individual Title/Version/Source/Workflow/Input/Step lines
		String noB64AtAll = noJavadocB64.replace("ArazzoSpecBase64: ", "ArazzoSpecBase64: !bad!");
		Arazzo rtLines = cli.ArazzoCliParser.parseWorkflowCli(noB64AtAll);
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
		inSchemaMulti.properties.put("p1", new Schema());
		inSchemaMulti.properties.put("p2", new Schema());
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
	 * {@link mocks.ArazzoMockEmitter}, and {@link mocks.ArazzoMockParser}.
	 */
	@Test
	public void testArazzoTestAndMockEmittersAndParsers() {
		assertNotNull(new tests.ArazzoTestEmitter());
		assertNotNull(new tests.ArazzoTestParser());
		assertNotNull(new mocks.ArazzoMockEmitter());
		assertNotNull(new mocks.ArazzoMockParser());

		Arazzo spec = buildSampleArazzo();
		String testCode = tests.ArazzoTestEmitter.emit(spec);
		assertTrue(testCode.contains("test_placeOrderFlow"));

		Arazzo rtTest = tests.ArazzoTestParser.parse(testCode);
		assertEquals(spec.info.title, rtTest.info.title);

		String testNoB64 = testCode.replace("@arazzoSpecBase64 ", "@arazzoSpecBase64 !bad!");
		Arazzo rtTestAst = tests.ArazzoTestParser.parse(testNoB64);
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
		assertTrue(mockCode.contains("mock_placeOrderFlow"));

		Arazzo rtMock = mocks.ArazzoMockParser.parse(mockCode);
		assertEquals(spec.info.title, rtMock.info.title);

		String mockNoB64 = mockCode.replace("@arazzoSpecBase64 ", "@arazzoSpecBase64 !bad!");
		Arazzo rtMockAst = mocks.ArazzoMockParser.parse(mockNoB64);
		assertEquals(2, rtMockAst.workflows.size());

		Arazzo emptyMockSpec = new Arazzo();
		emptyMockSpec.arazzo = null;
		assertNotNull(mocks.ArazzoMockEmitter.emit(emptyMockSpec));
		emptyMockSpec.workflows = Collections.singletonList(wfNoSteps);
		assertNotNull(mocks.ArazzoMockEmitter.emit(emptyMockSpec));
		assertNotNull(
				mocks.ArazzoMockParser.parse("public class PlainMock { public void custom() { put(\"a\", \"b\"); } }"));
	}
}
