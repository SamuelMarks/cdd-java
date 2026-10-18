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
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.getJavadocComment().isPresent()) {
				String doc = cls.getJavadocComment().get().getContent();
				for (String rawLine : doc.split("\n")) {
					String line = rawLine.replaceAll("^\\s*\\*\\s*", "").trim();
					if (line.startsWith("@arazzoSpecBase64 ")) {
						String b64 = line.substring("@arazzoSpecBase64 ".length()).trim();
						try {
							String json = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
							return arazzo.Parse.fromString(json);
						} catch (IOException | IllegalArgumentException ignored) {
							// Fallback to printHelp string literals
						}
					}
				}
			}
		}

		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "CLI Workflow";
		spec.info.version = "1.0.0";
		spec.sourceDescriptions = new ArrayList<>();
		spec.workflows = new ArrayList<>();

		Workflow currentWf = null;
		for (StringLiteralExpr lit : cu.findAll(StringLiteralExpr.class)) {
			String val = lit.asString();
			if (val.startsWith("ArazzoSpecBase64: ")) {
				String b64 = val.substring("ArazzoSpecBase64: ".length()).trim();
				try {
					String json = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
					return arazzo.Parse.fromString(json);
				} catch (IOException | IllegalArgumentException ignored) {
					// Continue parsing lines
				}
			} else if (val.startsWith("Title: ")) {
				spec.info.title = val.substring("Title: ".length()).trim();
			} else if (val.startsWith("Version: ")) {
				spec.info.version = val.substring("Version: ".length()).trim();
			} else if (val.startsWith("Source: ")) {
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
			} else if (val.startsWith("Workflow: ")) {
				currentWf = new Workflow();
				currentWf.workflowId = val.substring("Workflow: ".length()).trim();
				currentWf.steps = new ArrayList<>();
				spec.workflows.add(currentWf);
			} else if (val.startsWith("  Input: --")) {
				String propName = val.substring("  Input: --".length()).trim();
				if (currentWf.inputs == null) {
					Schema s = new Schema();
					s.type = "object";
					s.properties = new LinkedHashMap<>();
					currentWf.inputs = ReferenceOr.ofValue(s);
				}
				Schema propSchema = new Schema();
				propSchema.type = "string";
				currentWf.inputs.value.properties.put(propName, propSchema);
			} else if (val.startsWith("  Step: ")) {
				Step s = new Step();
				s.stepId = val.substring("  Step: ".length()).trim();
				s.operationId = s.stepId;
				currentWf.steps.add(s);
			}
		}
		return spec;
	}
}
