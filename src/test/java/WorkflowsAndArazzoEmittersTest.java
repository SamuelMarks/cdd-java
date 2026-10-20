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
}
