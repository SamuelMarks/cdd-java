package mocks;

import arazzo.Arazzo;
import arazzo.Step;
import arazzo.Workflow;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Emits stateful workflow mock servers and simulators from an {@link Arazzo}
 * 1.1.0 specification.
 */
public class ArazzoMockEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoMockEmitter() {
	}

	/**
	 * Emits a stateful workflow mock class from an {@link Arazzo} 1.1.0
	 * specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java source code string.
	 */
	public static String emit(Arazzo spec) {
		String encodedSpec = Base64.getEncoder()
				.encodeToString(arazzo.Emit.toString(spec).getBytes(StandardCharsets.UTF_8));
		String className = workflows.Emit.deriveClassName(spec) + "Mock";
		StringBuilder sb = new StringBuilder();
		sb.append("import java.util.LinkedHashMap;\n");
		sb.append("import java.util.Map;\n\n");
		sb.append("/**\n");
		sb.append(" * Stateful workflow mock simulator generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		sb.append(" * @arazzoSpecBase64 ").append(encodedSpec).append("\n");
		sb.append(" */\n");
		sb.append("public class ").append(className).append(" {\n");
		sb.append("\t/** Tracks per-step invocation counts for retry simulation. */\n");
		sb.append("\tpublic final Map<String, Integer> stepAttempts = new LinkedHashMap<>();\n\n");
		sb.append("\t/** Default constructor. */\n");
		sb.append("\tpublic ").append(className).append("() {\n\t}\n\n");

		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				String mName = "mock_" + TypeMapper.sanitizeIdentifier(wf.workflowId);
				sb.append("\t/**\n");
				sb.append("\t * Simulates stateful execution of workflow ").append(wf.workflowId).append(".\n");
				sb.append("\t * @return Map of simulated step responses.\n");
				sb.append("\t */\n");
				sb.append("\tpublic Map<String, String> ").append(mName).append("() {\n");
				sb.append("\t\tMap<String, String> responses = new LinkedHashMap<>();\n");
				if (wf.steps != null) {
					for (Step s : wf.steps) {
						String stepEsc = TypeMapper.escape(s.stepId);
						sb.append("\t\tstepAttempts.put(\"").append(stepEsc).append("\", stepAttempts.getOrDefault(\"")
								.append(stepEsc).append("\", 0) + 1);\n");
						sb.append("\t\tresponses.put(\"").append(stepEsc)
								.append("\", \"{\\\"status\\\":\\\"ok\\\",\\\"stepId\\\":\\\"").append(stepEsc)
								.append("\\\"}\");\n");
					}
				}
				sb.append("\t\treturn responses;\n");
				sb.append("\t}\n\n");
			}
		}
		sb.append("}\n");
		return sb.toString();
	}

	/**
	 * Emits a map of modular workflow mock files.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return Map of relative file paths to Java source strings.
	 */
	public static Map<String, String> emitModular(Arazzo spec) {
		Map<String, String> files = new LinkedHashMap<>();
		String className = workflows.Emit.deriveClassName(spec) + "Mock";
		files.put("mocks/" + className + ".java", emit(spec));
		return files;
	}
}
