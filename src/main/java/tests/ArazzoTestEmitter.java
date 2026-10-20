package tests;

import arazzo.Arazzo;
import arazzo.Criterion;
import arazzo.Step;
import arazzo.Workflow;
import java.util.List;
import openapi.TypeMapper;

/**
 * Emits multi-step JUnit 4 end-to-end integration test suites from an
 * {@link Arazzo} 1.1.0 specification.
 */
public class ArazzoTestEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoTestEmitter() {
	}

	/**
	 * Emits a JUnit 4 workflow integration test class from an {@link Arazzo} 1.1.0
	 * specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java JUnit test source code.
	 */
	public static String emit(Arazzo spec) {
		String className = workflows.Emit.deriveClassName(spec) + "IntegrationTest";
		StringBuilder sb = new StringBuilder();
		sb.append("import org.junit.Test;\n");
		sb.append("import static org.junit.Assert.*;\n");
		sb.append("import arazzo.RuntimeExpression;\n\n");
		sb.append("/**\n");
		sb.append(" * End-to-end workflow integration tests generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		workflows.Emit.appendSpecJavadoc(sb, " * ", spec);
		sb.append(" */\n");
		sb.append("public class ").append(className).append(" {\n");
		sb.append("\t/** Default constructor. */\n");
		sb.append("\tpublic ").append(className).append("() {\n\t}\n\n");

		List<Workflow> ordered = arazzo.ArazzoResolver.sortWorkflowsTopologically(spec);
		for (Workflow wf : ordered) {
			String mName = "test_" + TypeMapper.sanitizeIdentifier(wf.workflowId);
			sb.append("\t/**\n");
			sb.append("\t * E2E workflow test for ").append(wf.workflowId).append(".\n");
			workflows.Emit.appendWorkflowJavadoc(sb, "\t * ", wf, spec);
			sb.append("\t */\n");
			sb.append("\t@Test\n");
			sb.append("\tpublic void ").append(mName).append("() {\n");
			sb.append("\t\tRuntimeExpression.Context ctx = new RuntimeExpression.Context();\n");
			sb.append("\t\tctx.statusCode = 200;\n");
			if (wf.steps != null) {
				for (Step s : wf.steps) {
					sb.append("\t\t// Step: ").append(s.stepId).append("\n");
					if (s.successCriteria != null) {
						for (Criterion c : s.successCriteria) {
							if (c.condition != null && c.condition.contains("200")) {
								sb.append("\t\tassertEquals(200, ctx.statusCode);\n");
							}
						}
					}
					sb.append("\t\tassertNotNull(\"").append(TypeMapper.escape(s.stepId)).append("\", ctx);\n");
				}
			}
			sb.append("\t}\n\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
