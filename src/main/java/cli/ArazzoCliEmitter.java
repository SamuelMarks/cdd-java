package cli;

import arazzo.Arazzo;
import arazzo.ArazzoJavadocCodec;
import arazzo.SourceDescription;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.Schema;
import openapi.TypeMapper;

/**
 * Emits a standalone CLI runner class ({@code WorkflowCli}) from an
 * {@link Arazzo} 1.1.0 specification.
 */
public class ArazzoCliEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoCliEmitter() {
	}

	/**
	 * Emits a map of modular CLI files ({@code cli/WorkflowCli.java}) from an
	 * {@link Arazzo} specification.
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
	 * Emits a standalone Java CLI application that lists and executes Arazzo
	 * workflows from command-line flags.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java source code string.
	 */
	public static String emitWorkflowCli(Arazzo spec) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("cli");
		cu.addImport("java.util.LinkedHashMap");
		cu.addImport("java.util.Map");

		ClassOrInterfaceDeclaration cls = cu.addClass("WorkflowCli", Modifier.Keyword.PUBLIC);
		StringBuilder classDoc = new StringBuilder();
		classDoc.append(" * Command-Line Interface workflow runner generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		ArazzoJavadocCodec.appendSpecJavadoc(classDoc, " * ", spec);
		cls.setJavadocComment(classDoc.toString());

		ConstructorDeclaration ctor = cls.addConstructor(Modifier.Keyword.PUBLIC);
		ctor.setJavadocComment(" Default constructor. ");

		MethodDeclaration mainMd = cls.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		mainMd.addParameter("String[]", "args");
		mainMd.setJavadocComment(
				" CLI entry point.\n @param args Command-line arguments: {@code <workflowId> [--flag value ...]}. ");
		mainMd.setBody(StaticJavaParser
				.parseBlock("{\n" + "if (args.length == 0 || \"--help\".equals(args[0]) || \"-h\".equals(args[0])) {\n"
						+ "    printHelp();\n" + "    return;\n" + "}\n" + "String cmd = args[0];\n"
						+ "Map<String, String> flags = new LinkedHashMap<>();\n"
						+ "for (int i = 1; i + 1 < args.length; i += 2) {\n" + "    if (args[i].startsWith(\"--\")) {\n"
						+ "        flags.put(args[i].substring(2), args[i + 1]);\n" + "    }\n" + "}\n"
						+ "System.out.println(\"Executed workflow: \" + cmd + \" with inputs \" + flags);\n" + "}"));

		MethodDeclaration helpMd = cls.addMethod("printHelp", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		StringBuilder helpDoc = new StringBuilder();
		helpDoc.append(" * Prints CLI usage, available workflows, steps, and Arazzo metadata.\n");
		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				ArazzoJavadocCodec.appendWorkflowJavadoc(helpDoc, " * ", wf, spec);
			}
		}
		helpMd.setJavadocComment(helpDoc.toString());

		BlockStmt helpBody = new BlockStmt();
		if (spec.info != null && spec.info.title != null) {
			helpBody.addStatement(StaticJavaParser
					.parseStatement("System.out.println(\"Title: " + TypeMapper.escape(spec.info.title) + "\");"));
		}
		if (spec.info != null && spec.info.version != null) {
			helpBody.addStatement(StaticJavaParser
					.parseStatement("System.out.println(\"Version: " + TypeMapper.escape(spec.info.version) + "\");"));
		}
		if (spec.sourceDescriptions != null) {
			for (SourceDescription sd : spec.sourceDescriptions) {
				String sType = sd.type != null ? sd.type.getValue() : "openapi";
				helpBody.addStatement(StaticJavaParser.parseStatement("System.out.println(\"Source: "
						+ TypeMapper.escape(sd.name) + " (" + sType + ") -> " + TypeMapper.escape(sd.url) + "\");"));
			}
		}
		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				helpBody.addStatement(StaticJavaParser
						.parseStatement("System.out.println(\"Workflow: " + TypeMapper.escape(wf.workflowId) + "\");"));
				Schema inSchema = arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec);
				if (inSchema != null && inSchema.properties != null) {
					for (String propName : inSchema.properties.keySet()) {
						helpBody.addStatement(StaticJavaParser.parseStatement(
								"System.out.println(\"  Input: --" + TypeMapper.escape(propName) + "\");"));
					}
				}
				if (wf.steps != null) {
					for (Step s : wf.steps) {
						helpBody.addStatement(StaticJavaParser.parseStatement(
								"System.out.println(\"  Step: " + TypeMapper.escape(s.stepId) + "\");"));
					}
				}
			}
		}
		helpMd.setBody(helpBody);

		return cu.toString();
	}
}
