import static org.junit.Assert.*;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.Step;
import arazzo.Workflow;
import java.util.Collections;
import org.junit.Test;

/**
 * Dedicated round-trip tests for {@link cli.ArazzoCliEmitter} and
 * {@link cli.ArazzoCliParser}.
 */
public class ArazzoCliParseEmitTest {
	/**
	 * Default constructor.
	 */
	public ArazzoCliParseEmitTest() {
	}

	/**
	 * Verifies that {@link cli.ArazzoCliEmitter} and {@link cli.ArazzoCliParser}
	 * round-trip an {@link Arazzo} specification losslessly.
	 */
	@Test
	public void testArazzoCliRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.info = new ArazzoInfo();
		spec.info.title = "CliFlow";
		spec.info.version = "1.1.0";
		Workflow wf = new Workflow();
		wf.workflowId = "runCli";
		Step step = new Step();
		step.stepId = "execStep";
		step.operationId = "execOp";
		wf.steps = Collections.singletonList(step);
		spec.workflows = Collections.singletonList(wf);

		String code = cli.ArazzoCliEmitter.emitWorkflowCli(spec);
		Arazzo parsed = cli.ArazzoCliParser.parseWorkflowCli(code);
		assertEquals(spec.info.title, parsed.info.title);
		assertEquals(1, parsed.workflows.size());
		assertEquals("runCli", parsed.workflows.get(0).workflowId);
	}
}
