package cli;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.SourceDescription;
import arazzo.SourceType;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import openapi.ReferenceOr;
import openapi.Schema;

/**
 * Parses CLI workflow runner source code ({@code WorkflowCli.java}) back into a
 * strongly typed {@link Arazzo} 1.1.0 specification.
 */
public class ArazzoCliParser {
	/**
	 * Default constructor.
	 */
	public ArazzoCliParser() {
	}

	/**
	 * Extracts an {@link Arazzo} 1.1.0 specification from a CLI workflow runner
	 * Java source string.
	 *
	 * @param code
	 *            The Java source code of {@code WorkflowCli}.
	 * @return The extracted {@link Arazzo} 1.1.0 specification.
	 */
	public static Arazzo parseWorkflowCli(String code) {
		CompilationUnit cu = StaticJavaParser.parse(code);
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "CLI Workflow";
		spec.info.version = "1.0.0";
		spec.sourceDescriptions = new ArrayList<>();
		spec.workflows = new ArrayList<>();

		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.getJavadocComment().isPresent()) {
				workflows.Parse.parseSpecJavadoc(cls.getJavadocComment().get().getContent(), spec);
			}
		}

		for (MethodDeclaration m : cu.findAll(MethodDeclaration.class)) {
			if ("printHelp".equals(m.getNameAsString()) && m.getJavadocComment().isPresent()) {
				String doc = m.getJavadocComment().get().getContent();
				StringBuilder currentBlock = null;
				for (String rawLine : doc.split("\n")) {
					String line = rawLine.replaceAll("^\\s*\\*\\s*", "").trim();
					if (line.startsWith("@workflowId ")) {
						if (currentBlock != null) {
							Workflow wf = new Workflow();
							wf.steps = new ArrayList<>();
							workflows.Parse.parseWorkflowJavadoc(currentBlock.toString(), wf);
							spec.workflows.add(wf);
						}
						currentBlock = new StringBuilder();
					}
					if (currentBlock != null) {
						currentBlock.append(line).append("\n");
					}
				}
				if (currentBlock != null) {
					Workflow wf = new Workflow();
					wf.steps = new ArrayList<>();
					workflows.Parse.parseWorkflowJavadoc(currentBlock.toString(), wf);
					spec.workflows.add(wf);
				}
			}
		}

		boolean hadJavadocWorkflows = !spec.workflows.isEmpty();
		boolean hadJavadocSources = !spec.sourceDescriptions.isEmpty();
		Workflow currentWf = null;
		for (StringLiteralExpr lit : cu.findAll(StringLiteralExpr.class)) {
			String val = lit.asString();
			if (val.startsWith("Title: ")) {
				spec.info.title = val.substring("Title: ".length()).trim();
			} else if (val.startsWith("Version: ")) {
				spec.info.version = val.substring("Version: ".length()).trim();
			} else if (val.startsWith("Source: ") && !hadJavadocSources) {
				String rest = val.substring("Source: ".length()).trim();
				int arrow = rest.indexOf(" -> ");
				String left = rest.substring(0, arrow).trim();
				String url = rest.substring(arrow + 4).trim();
				SourceDescription sd = new SourceDescription();
				sd.url = url;
				sd.type = SourceType.OPENAPI;
				if (left.contains("(")) {
					int open = left.indexOf('(');
					sd.name = left.substring(0, open).trim();
					sd.type = SourceType.fromString(left.substring(open + 1, left.length() - 1).trim());
				} else {
					sd.name = left;
				}
				spec.sourceDescriptions.add(sd);
			} else if (!hadJavadocWorkflows) {
				if (val.startsWith("Workflow: ")) {
					currentWf = new Workflow();
					currentWf.workflowId = val.substring("Workflow: ".length()).trim();
					currentWf.steps = new ArrayList<>();
					spec.workflows.add(currentWf);
				} else if (val.startsWith("  Input: --")) {
					String propName = val.substring("  Input: --".length()).trim();
					if (currentWf.inputs == null) {
						Schema s = new Schema();
						s.type = openapi.SchemaType.ofSingle("object");
						s.properties = new LinkedHashMap<>();
						currentWf.inputs = ReferenceOr.ofValue(s);
					}
					Schema propSchema = new Schema();
					propSchema.type = openapi.SchemaType.ofSingle("string");
					currentWf.inputs.value.properties.put(propName, ReferenceOr.ofValue(propSchema));
				} else if (val.startsWith("  Step: ")) {
					Step s = new Step();
					s.stepId = val.substring("  Step: ".length()).trim();
					s.operationId = s.stepId;
					currentWf.steps.add(s);
				}
			}
		}
		return spec;
	}
}
