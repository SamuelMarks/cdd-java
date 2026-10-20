import static org.junit.Assert.*;

import arazzo.*;
import common.PayloadValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import openapi.ReferenceOr;
import openapi.Schema;
import org.junit.Test;

/**
 * Dedicated round-trip tests for {@link tests.ArazzoTestEmitter},
 * {@link tests.ArazzoTestParser}, {@link mocks.ArazzoMockEmitter}, and
 * {@link mocks.ArazzoMockParser}.
 */
public class ArazzoServerTestsMockTest {
	/**
	 * Default constructor.
	 */
	public ArazzoServerTestsMockTest() {
	}

	/**
	 * Verifies that E2E test and stateful mock emitters and parsers round-trip a
	 * rich {@link Arazzo} specification losslessly without Base64 embedding.
	 */
	@Test
	public void testServerTestsAndMocksRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.$self = "https://example.com/server-arazzo.json";
		spec.info = new ArazzoInfo();
		spec.info.title = "ServerFlow";
		spec.info.summary = "Server verification suite";
		spec.info.description = "Verifies multi-step server endpoints";
		spec.info.version = "1.1.0";

		SourceDescription sd = new SourceDescription();
		sd.name = "api";
		sd.type = SourceType.OPENAPI;
		sd.url = "./openapi.json";
		spec.sourceDescriptions = Collections.singletonList(sd);

		Workflow wf = new Workflow();
		wf.workflowId = "verifyServer";
		wf.summary = "Verify ping and status";
		wf.description = "Executes pingStep and checks outputs";
		wf.dependsOn = Collections.singletonList("initServer");

		Schema inputs = new Schema();
		inputs.type = openapi.SchemaType.ofSingle("object");
		inputs.properties = new LinkedHashMap<>();
		Schema hostProp = new Schema();
		hostProp.type = openapi.SchemaType.ofSingle("string");
		inputs.properties.put("host", ReferenceOr.ofValue(hostProp));
		wf.inputs = ReferenceOr.ofValue(inputs);

		Step step = new Step();
		step.stepId = "pingStep";
		step.operationId = "pingOp";
		ArazzoParameter param = new ArazzoParameter();
		param.in = ParameterIn.QUERY;
		param.name = "echo";
		param.value = PayloadValue.ofString("hello");
		step.parameters = Collections.singletonList(ReusableOr.ofItem(param));
		Criterion crit = new Criterion();
		crit.context = "$statusCode";
		crit.condition = "$statusCode == 200";
		step.successCriteria = Collections.singletonList(crit);
		step.outputs = Collections.singletonMap("pong", OutputValue.ofExpression("$response.body#/pong"));

		wf.steps = Collections.singletonList(step);
		wf.outputs = Collections.singletonMap("resultPong", OutputValue.ofExpression("$steps.pingStep.outputs.pong"));
		spec.workflows = Arrays.asList(wf);

		String testCode = tests.ArazzoTestEmitter.emit(spec);
		assertFalse(testCode.contains("arazzoSpecBase64"));
		Arazzo parsedTest = tests.ArazzoTestParser.parse(testCode);
		assertEquals(spec, parsedTest);

		String mockCode = mocks.ArazzoMockEmitter.emit(spec);
		assertFalse(mockCode.contains("arazzoSpecBase64"));
		Arazzo parsedMock = mocks.ArazzoMockParser.parse(mockCode);
		assertEquals(spec, parsedMock);
	}
}
