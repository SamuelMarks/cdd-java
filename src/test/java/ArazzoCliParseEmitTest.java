import static org.junit.Assert.*;

import arazzo.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import openapi.ReferenceOr;
import openapi.Schema;
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
	 * round-trip a full {@link Arazzo} specification losslessly without Base64
	 * embedding.
	 */
	@Test
	public void testArazzoCliRoundTrip() {
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.$self = "https://example.com/cli-arazzo.json";
		spec.info = new ArazzoInfo();
		spec.info.title = "CliFlow";
		spec.info.summary = "CLI Workflow Runner";
		spec.info.description = "Runs CLI workflows";
		spec.info.version = "1.1.0";

		SourceDescription sd = new SourceDescription();
		sd.name = "cliApi";
		sd.type = SourceType.OPENAPI;
		sd.url = "./cli-openapi.json";
		spec.sourceDescriptions = Collections.singletonList(sd);

		Workflow wf = new Workflow();
		wf.workflowId = "runCli";
		wf.summary = "Run CLI workflow";
		Schema inSchema = new Schema();
		inSchema.type = openapi.SchemaType.ofSingle("object");
		inSchema.properties = new LinkedHashMap<>();
		Schema argProp = new Schema();
		argProp.type = openapi.SchemaType.ofSingle("string");
		inSchema.properties.put("target", ReferenceOr.ofValue(argProp));
		wf.inputs = ReferenceOr.ofValue(inSchema);

		Step step = new Step();
		step.stepId = "execStep";
		step.operationId = "execOp";
		Criterion crit = new Criterion();
		crit.context = "$statusCode";
		crit.condition = "$statusCode == 200";
		step.successCriteria = Collections.singletonList(crit);
		wf.steps = Collections.singletonList(step);
		wf.outputs = Collections.singletonMap("cliStatus", OutputValue.ofExpression("$response.body#/status"));
		spec.workflows = Collections.singletonList(wf);

		String code = cli.ArazzoCliEmitter.emitWorkflowCli(spec);
		assertFalse(code.contains("arazzoSpecBase64"));
		assertFalse(code.contains("ArazzoSpecBase64"));
		Arazzo parsed = cli.ArazzoCliParser.parseWorkflowCli(code);
		assertEquals(spec, parsed);
	}
}
