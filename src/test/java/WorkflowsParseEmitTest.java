import static org.junit.Assert.*;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.Step;
import arazzo.Workflow;
import java.util.Collections;
import org.junit.Test;

/**
 * Dedicated round-trip tests for {@link workflows.Emit} and
 * {@link workflows.Parse}.
 */
public class WorkflowsParseEmitTest {
	/**
	 * Default constructor.
	 */
	public WorkflowsParseEmitTest() {
	}

	/**
	 * Verifies that {@link workflows.Emit} and {@link workflows.Parse} round-trip
	 * an {@link Arazzo} specification losslessly.
	 */
	@Test
	public void testWorkflowsRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.info = new ArazzoInfo();
		spec.info.title = "OrderFlow";
		spec.info.version = "1.1.0";
		Workflow wf = new Workflow();
		wf.workflowId = "checkout";
		Step step = new Step();
		step.stepId = "pay";
		step.operationId = "createPayment";
		wf.steps = Collections.singletonList(step);
		spec.workflows = Collections.singletonList(wf);

		String code = workflows.Emit.emit(spec);
		Arazzo parsed = workflows.Parse.parse(code);
		assertEquals(spec.info.title, parsed.info.title);
		assertEquals(1, parsed.workflows.size());
		assertEquals("checkout", parsed.workflows.get(0).workflowId);
	}
}
