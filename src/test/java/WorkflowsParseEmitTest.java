import static org.junit.Assert.*;

import arazzo.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import openapi.ReferenceOr;
import openapi.Schema;
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
	 * Verifies that {@link workflows.Emit} and {@link workflows.Parse} round-trip a
	 * rich {@link Arazzo} specification losslessly without Base64 embedding.
	 */
	@Test
	public void testWorkflowsRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.$self = "https://example.com/workflows.json";
		spec.info = new ArazzoInfo();
		spec.info.title = "OrderFlow";
		spec.info.summary = "Checkout orchestration";
		spec.info.description = "Full payment workflow";
		spec.info.version = "1.1.0";

		SourceDescription sd = new SourceDescription();
		sd.name = "payments";
		sd.type = SourceType.OPENAPI;
		sd.url = "./payments.json";
		spec.sourceDescriptions = Collections.singletonList(sd);

		Workflow wf = new Workflow();
		wf.workflowId = "checkout";
		wf.summary = "Execute checkout";
		Schema inSchema = new Schema();
		inSchema.type = openapi.SchemaType.ofSingle("object");
		inSchema.properties = new LinkedHashMap<>();
		Schema amountProp = new Schema();
		amountProp.type = openapi.SchemaType.ofSingle("integer");
		inSchema.properties.put("amount", ReferenceOr.ofValue(amountProp));
		wf.inputs = ReferenceOr.ofValue(inSchema);

		Step step = new Step();
		step.stepId = "pay";
		step.operationId = "createPayment";
		ArazzoParameter param = new ArazzoParameter();
		param.in = ParameterIn.HEADER;
		param.name = "X-Idempotency-Key";
		param.value = PayloadValue.ofString("key-123");
		step.parameters = Collections.singletonList(ReusableOr.ofItem(param));
		step.requestBody = new ArazzoRequestBody();
		step.requestBody.payload = PayloadValue
				.ofObject(Collections.singletonMap("amount", PayloadValue.ofString("$inputs.amount")));
		Criterion crit = new Criterion();
		crit.context = "$statusCode";
		crit.condition = "$statusCode == 200";
		step.successCriteria = Collections.singletonList(crit);
		step.outputs = Collections.singletonMap("txId", OutputValue.ofExpression("$response.body#/transactionId"));

		wf.steps = Collections.singletonList(step);
		wf.outputs = Collections.singletonMap("receiptId", OutputValue.ofExpression("$steps.pay.outputs.txId"));
		spec.workflows = Collections.singletonList(wf);

		String code = workflows.Emit.emit(spec);
		assertFalse(code.contains("arazzoSpecBase64"));
		Arazzo parsed = workflows.Parse.parse(code);
		assertEquals(spec, parsed);
	}
}
