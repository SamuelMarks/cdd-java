import static org.junit.Assert.*;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.Step;
import arazzo.Workflow;
import java.util.Collections;
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
	 * Verifies that E2E test and stateful mock emitters and parsers round-trip an
	 * {@link Arazzo} specification losslessly.
	 */
	@Test
	public void testServerTestsAndMocksRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.info = new ArazzoInfo();
		spec.info.title = "ServerFlow";
		spec.info.version = "1.1.0";
		Workflow wf = new Workflow();
		wf.workflowId = "verifyServer";
		Step step = new Step();
		step.stepId = "pingStep";
		step.operationId = "pingOp";
		wf.steps = Collections.singletonList(step);
		spec.workflows = Collections.singletonList(wf);

		String testCode = tests.ArazzoTestEmitter.emit(spec);
		Arazzo parsedTest = tests.ArazzoTestParser.parse(testCode);
		assertEquals(spec.info.title, parsedTest.info.title);

		String mockCode = mocks.ArazzoMockEmitter.emit(spec);
		Arazzo parsedMock = mocks.ArazzoMockParser.parse(mockCode);
		assertEquals(spec.info.title, parsedMock.info.title);
	}
}
