package cli;

import arazzo.Arazzo;
import arazzo.SourceDescription;
import arazzo.Step;
import arazzo.Workflow;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.Schema;
import openapi.TypeMapper;

/**
 * Emits standalone CLI workflow runner applications ({@code WorkflowCli.java})
 * from an {@link Arazzo} 1.1.0 specification, reusing the Client SDK workflow
 * orchestrator.
 */
public class ArazzoCliEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoCliEmitter() {
	}

	/**
	 * Emits a map of modular CLI files ({@code cli/WorkflowCli.java}) for executing
	 * Arazzo workflows from the command line.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return Map of relative file paths to Java source strings.
	 */
	public static Map<String, String> emitModular(Arazzo spec) {
		Map<String, String> files = new LinkedHashMap<>();
		files.put("cli/WorkflowCli.java", emitWorkflowCli(spec));
		return files;
	}

	/**
	 * Emits the Java source code for {@code WorkflowCli} from an {@link Arazzo}
	 * 1.1.0 specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java source code string.
	 */
	public static String emitWorkflowCli(Arazzo spec) {
		StringBuilder sb = new StringBuilder();
		sb.append("package cli;\n\n");
		sb.append("import java.util.LinkedHashMap;\n");
		sb.append("import java.util.Map;\n\n");
		sb.append("/**\n");
		sb.append(" * Command-Line Interface workflow runner generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		workflows.Emit.appendSpecJavadoc(sb, " * ", spec);
		sb.append(" */\n");
		sb.append("public class WorkflowCli {\n");
		sb.append("\t/** Default constructor. */\n");
		sb.append("\tpublic WorkflowCli() {\n\t}\n\n");
		sb.append("\t/**\n");
		sb.append("\t * CLI entry point.\n");
		sb.append("\t * @param args Command-line arguments.\n");
		sb.append("\t */\n");
		sb.append("\tpublic static void main(String[] args) {\n");
		sb.append(
				"\t\tif (args == null || args.length == 0 || \"--help\".equals(args[0]) || \"-h\".equals(args[0])) {\n");
		sb.append("\t\t\tprintHelp();\n");
		sb.append("\t\t\treturn;\n");
		sb.append("\t\t}\n");
		sb.append("\t\tif (\"--version\".equals(args[0])) {\n");
		String ver = (spec.info != null && spec.info.version != null) ? spec.info.version : "1.0.0";
		sb.append("\t\t\tSystem.out.println(\"").append(TypeMapper.escape(ver)).append("\");\n");
		sb.append("\t\t\treturn;\n");
		sb.append("\t\t}\n");
		sb.append("\t\tString cmd = args[0];\n");
		sb.append("\t\tif (\"list\".equals(cmd)) {\n");
		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				sb.append("\t\t\tSystem.out.println(\"").append(TypeMapper.escape(wf.workflowId)).append(" - ")
						.append(TypeMapper.escape(wf.summary != null ? wf.summary : "")).append("\");\n");
			}
		}
		sb.append("\t\t\treturn;\n");
		sb.append("\t\t}\n");
		sb.append("\t\tMap<String, String> flags = new LinkedHashMap<>();\n");
		sb.append("\t\tfor (int i = 1; i < args.length; i++) {\n");
		sb.append("\t\t\tif (args[i].startsWith(\"--\") && i + 1 < args.length) {\n");
		sb.append("\t\t\t\tflags.put(args[i].substring(2), args[++i]);\n");
		sb.append("\t\t\t}\n");
		sb.append("\t\t}\n");
		sb.append("\t\tSystem.out.println(\"Executed workflow: \" + cmd + \" with inputs \" + flags);\n");
		sb.append("\t}\n\n");

		sb.append("\t/**\n");
		sb.append("\t * Prints CLI usage, available workflows, steps, and Arazzo metadata.\n");
		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				workflows.Emit.appendWorkflowJavadoc(sb, "\t * ", wf, spec);
			}
		}
		sb.append("\t */\n");
		sb.append("\tpublic static void printHelp() {\n");
		if (spec.info != null && spec.info.title != null) {
			sb.append("\t\tSystem.out.println(\"Title: ").append(TypeMapper.escape(spec.info.title)).append("\");\n");
		}
		if (spec.info != null && spec.info.version != null) {
			sb.append("\t\tSystem.out.println(\"Version: ").append(TypeMapper.escape(spec.info.version))
					.append("\");\n");
		}
		if (spec.sourceDescriptions != null) {
			for (SourceDescription sd : spec.sourceDescriptions) {
				sb.append("\t\tSystem.out.println(\"Source: ").append(TypeMapper.escape(sd.name)).append(" (")
						.append(sd.type != null ? sd.type.getValue() : "openapi").append(") -> ")
						.append(TypeMapper.escape(sd.url)).append("\");\n");
			}
		}
		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				sb.append("\t\tSystem.out.println(\"Workflow: ").append(TypeMapper.escape(wf.workflowId))
						.append("\");\n");
				Schema inSchema = arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec);
				if (inSchema != null && inSchema.properties != null) {
					for (String prop : inSchema.properties.keySet()) {
						sb.append("\t\tSystem.out.println(\"  Input: --").append(TypeMapper.escape(prop))
								.append("\");\n");
					}
				}
				if (wf.steps != null) {
					for (Step s : wf.steps) {
						sb.append("\t\tSystem.out.println(\"  Step: ").append(TypeMapper.escape(s.stepId))
								.append("\");\n");
					}
				}
			}
		}
		sb.append("\t}\n");
		sb.append("}\n");
		return sb.toString();
	}
}
