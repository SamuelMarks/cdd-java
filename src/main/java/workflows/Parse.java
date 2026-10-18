package workflows;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.OutputValue;
import arazzo.SourceDescription;
import arazzo.SourceType;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.SwitchEntry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.ReferenceOr;
import openapi.Schema;
import openapi.TypeMapper;

/**
 * Parses Java workflow orchestrator classes and AST structures back into an
 * {@link Arazzo} 1.1.0 specification.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses Java workflow orchestrator source code into an {@link Arazzo} 1.1.0
	 * specification.
	 *
	 * @param code
	 *            The Java source code of a workflow orchestrator class.
	 * @return The extracted {@link Arazzo} 1.1.0 specification.
	 */
	public static Arazzo parse(String code) {
		CompilationUnit cu = StaticJavaParser.parse(code);
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "Extracted Workflow";
		spec.info.version = "1.0.0";
		spec.sourceDescriptions = new ArrayList<>();
		spec.workflows = new ArrayList<>();

		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.isNestedType()) {
				continue;
			}
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
							// Fallback to structural AST extraction below
						}
					} else if (line.startsWith("@arazzoSelf ")) {
						spec.$self = line.substring("@arazzoSelf ".length()).trim();
					} else if (line.startsWith("@infoTitle ")) {
						spec.info.title = line.substring("@infoTitle ".length()).trim();
					} else if (line.startsWith("@infoSummary ")) {
						spec.info.summary = line.substring("@infoSummary ".length()).trim();
					} else if (line.startsWith("@infoDescription ")) {
						spec.info.description = line.substring("@infoDescription ".length()).trim();
					} else if (line.startsWith("@infoVersion ")) {
						spec.info.version = line.substring("@infoVersion ".length()).trim();
					} else if (line.startsWith("@sourceDescription ")) {
						String[] parts = line.substring("@sourceDescription ".length()).trim().split("\\s+", 3);
						SourceDescription sd = new SourceDescription();
						sd.name = parts[0];
						sd.type = SourceType.fromString(parts[1]);
						sd.url = parts[2];
						spec.sourceDescriptions.add(sd);
					}
				}
			}

			Map<String, ClassOrInterfaceDeclaration> nestedDtos = new LinkedHashMap<>();
			for (ClassOrInterfaceDeclaration inner : cls.findAll(ClassOrInterfaceDeclaration.class)) {
				if (inner != cls) {
					nestedDtos.put(inner.getNameAsString(), inner);
				}
			}

			for (MethodDeclaration method : cls.getMethods()) {
				Workflow wf = parseWorkflowMethod(method, nestedDtos);
				spec.workflows.add(wf);
			}
		}

		if (spec.sourceDescriptions.isEmpty()) {
			SourceDescription defaultSd = new SourceDescription();
			defaultSd.name = "default";
			defaultSd.type = SourceType.OPENAPI;
			defaultSd.url = "./openapi.json";
			spec.sourceDescriptions.add(defaultSd);
		}
		return spec;
	}

	/**
	 * Extracts a {@link Workflow} from a Java orchestrator
	 * {@link MethodDeclaration} and its associated input/output DTO declarations.
	 *
	 * @param method
	 *            The workflow orchestrator method declaration.
	 * @param nestedDtos
	 *            Map of inner DTO class names to declarations.
	 * @return The extracted {@link Workflow}.
	 */
	private static Workflow parseWorkflowMethod(MethodDeclaration method,
			Map<String, ClassOrInterfaceDeclaration> nestedDtos) {
		Workflow wf = new Workflow();
		wf.workflowId = method.getNameAsString();
		wf.steps = new ArrayList<>();

		if (method.getJavadocComment().isPresent()) {
			String doc = method.getJavadocComment().get().getContent();
			for (String rawLine : doc.split("\n")) {
				String line = rawLine.replaceAll("^\\s*\\*\\s*", "").trim();
				if (line.startsWith("@workflowId ")) {
					wf.workflowId = line.substring("@workflowId ".length()).trim();
				} else if (line.startsWith("@workflowSummary ")) {
					wf.summary = line.substring("@workflowSummary ".length()).trim();
				} else if (line.startsWith("@workflowDescription ")) {
					wf.description = line.substring("@workflowDescription ".length()).trim();
				} else if (line.startsWith("@dependsOn ")) {
					if (wf.dependsOn == null) {
						wf.dependsOn = new ArrayList<>();
					}
					wf.dependsOn.add(line.substring("@dependsOn ".length()).trim());
				}
			}
		}

		for (Parameter param : method.getParameters()) {
			String typeName = param.getTypeAsString();
			ClassOrInterfaceDeclaration dto = nestedDtos.get(typeName);
			if (dto != null) {
				Schema inSchema = new Schema();
				inSchema.type = "object";
				inSchema.properties = new LinkedHashMap<>();
				for (FieldDeclaration fd : dto.getFields()) {
					for (VariableDeclarator vd : fd.getVariables()) {
						if ("values".equals(vd.getNameAsString())) {
							continue;
						}
						Schema propSchema = new Schema();
						TypeMapper.toSchema(vd.getType(), propSchema, true);
						inSchema.properties.put(vd.getNameAsString(), propSchema);
					}
				}
				if (!inSchema.properties.isEmpty()) {
					wf.inputs = ReferenceOr.ofValue(inSchema);
				}
			}
		}

		for (SwitchEntry entry : method.findAll(SwitchEntry.class)) {
			if (entry.getLabels().isNonEmpty()) {
				String stepId = entry.getLabels().get(0).toString().replace("\"", "");
				Step step = new Step();
				step.stepId = stepId;
				step.operationId = stepId;
				wf.steps.add(step);
			}
		}

		if (wf.steps.isEmpty()) {
			java.util.Set<String> ignoredCalls = new java.util.HashSet<>(java.util.Arrays.asList("put", "putAll",
					"evaluate", "evaluateCriterion", "queryJsonPath", "valueOf", "fromString", "ofSimple"));
			for (MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
				String name = call.getNameAsString();
				if (!ignoredCalls.contains(name)) {
					Step step = new Step();
					step.stepId = name;
					step.operationId = name;
					wf.steps.add(step);
				}
			}
		}

		String retType = method.getTypeAsString();
		ClassOrInterfaceDeclaration outDto = nestedDtos.get(retType);
		if (outDto != null) {
			wf.outputs = new LinkedHashMap<>();
			for (FieldDeclaration fd : outDto.getFields()) {
				for (VariableDeclarator vd : fd.getVariables()) {
					if (!"values".equals(vd.getNameAsString())) {
						wf.outputs.put(vd.getNameAsString(),
								OutputValue.ofExpression("$outputs." + vd.getNameAsString()));
					}
				}
			}
			if (wf.outputs.isEmpty()) {
				wf.outputs = null;
			}
		}

		return wf;
	}
}
