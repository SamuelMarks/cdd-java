package workflows;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.ArazzoParameter;
import arazzo.ArazzoRequestBody;
import arazzo.Criterion;
import arazzo.CriterionEngineType;
import arazzo.CriterionTypeOrExpression;
import arazzo.FailureAction;
import arazzo.FailureActionType;
import arazzo.OutputValue;
import arazzo.ParameterIn;
import arazzo.PayloadReplacement;
import arazzo.PayloadValue;
import arazzo.ReusableOr;
import arazzo.Selector;
import arazzo.SourceDescription;
import arazzo.SourceType;
import arazzo.Step;
import arazzo.SuccessAction;
import arazzo.SuccessActionType;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.ReferenceOr;
import openapi.Schema;
import openapi.TypeMapper;
import org.json.JSONTokener;

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
				parseSpecJavadoc(cls.getJavadocComment().get().getContent(), spec);
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
	 * Parses class-level Arazzo metadata Javadoc tags ({@code @arazzoSelf},
	 * {@code @infoTitle}, {@code @infoSummary}, {@code @infoDescription},
	 * {@code @infoVersion}, and {@code @sourceDescription}) into the target
	 * {@link Arazzo} specification.
	 *
	 * @param javadocContent
	 *            The raw Javadoc comment content.
	 * @param spec
	 *            The target {@link Arazzo} specification to populate.
	 */
	public static void parseSpecJavadoc(String javadocContent, Arazzo spec) {
		for (String rawLine : javadocContent.split("\n")) {
			String line = rawLine.replaceAll("^\\s*\\*\\s*", "").trim();
			if (line.startsWith("@arazzoSelf ")) {
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
				if (parts.length == 3) {
					SourceDescription sd = new SourceDescription();
					sd.name = parts[0];
					sd.type = SourceType.fromString(parts[1]);
					sd.url = parts[2];
					spec.sourceDescriptions.add(sd);
				}
			}
		}
	}

	/**
	 * Parses method-level Arazzo workflow and step metadata Javadoc tags into the
	 * target {@link Workflow}.
	 *
	 * @param javadocContent
	 *            The raw method Javadoc comment content.
	 * @param wf
	 *            The target {@link Workflow} to populate.
	 */
	public static void parseWorkflowJavadoc(String javadocContent, Workflow wf) {
		Map<String, Step> stepMap = new LinkedHashMap<>();
		for (String rawLine : javadocContent.split("\n")) {
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
			} else if (line.startsWith("@workflowInput ")) {
				String[] parts = line.substring("@workflowInput ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					if (wf.inputs == null) {
						Schema s = new Schema();
						s.type = openapi.SchemaType.ofSingle("object");
						s.properties = new LinkedHashMap<>();
						wf.inputs = ReferenceOr.ofValue(s);
					}
					Schema propSchema = new Schema();
					propSchema.type = openapi.SchemaType.ofSingle(parts[1]);
					wf.inputs.value.properties.put(parts[0], ReferenceOr.ofValue(propSchema));
				}
			} else if (line.startsWith("@workflowParam ")) {
				String[] parts = line.substring("@workflowParam ".length()).trim().split("\\s+", 3);
				if (parts.length == 3) {
					if (wf.parameters == null) {
						wf.parameters = new ArrayList<>();
					}
					ArazzoParameter param = new ArazzoParameter();
					param.in = ParameterIn.fromString(parts[0]);
					param.name = parts[1];
					param.value = parsePayloadTag(parts[2]);
					wf.parameters.add(ReusableOr.ofItem(param));
				}
			} else if (line.startsWith("@stepMeta ")) {
				String[] parts = line.substring("@stepMeta ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					Step s = new Step();
					s.stepId = parts[0];
					s.operationId = "-".equals(parts[1]) ? null : parts[1];
					stepMap.put(s.stepId, s);
					wf.steps.add(s);
				}
			} else if (line.startsWith("@stepParam ")) {
				String[] parts = line.substring("@stepParam ".length()).trim().split("\\s+", 4);
				if (parts.length == 4 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.parameters == null) {
						s.parameters = new ArrayList<>();
					}
					ArazzoParameter param = new ArazzoParameter();
					param.in = ParameterIn.fromString(parts[1]);
					param.name = parts[2];
					param.value = parsePayloadTag(parts[3]);
					s.parameters.add(ReusableOr.ofItem(param));
				}
			} else if (line.startsWith("@stepBody ")) {
				String[] parts = line.substring("@stepBody ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					s.requestBody = new ArazzoRequestBody();
					s.requestBody.payload = parsePayloadTag(parts[1]);
				}
			} else if (line.startsWith("@stepReplacement ")) {
				String[] parts = line.substring("@stepReplacement ".length()).trim().split("\\s+", 3);
				if (parts.length == 3 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.requestBody == null) {
						s.requestBody = new ArazzoRequestBody();
					}
					if (s.requestBody.replacements == null) {
						s.requestBody.replacements = new ArrayList<>();
					}
					PayloadReplacement pr = new PayloadReplacement();
					pr.target = parts[1];
					pr.value = parsePayloadTag(parts[2]);
					s.requestBody.replacements.add(pr);
				}
			} else if (line.startsWith("@stepCriterion ")) {
				String[] parts = line.substring("@stepCriterion ".length()).trim().split("\\s+", 4);
				if (parts.length == 4 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.successCriteria == null) {
						s.successCriteria = new ArrayList<>();
					}
					Criterion c = new Criterion();
					c.context = "-".equals(parts[1]) ? null : parts[1];
					if (!"-".equals(parts[2])) {
						c.type = CriterionTypeOrExpression.ofSimple(CriterionEngineType.fromString(parts[2]));
					}
					c.condition = parts[3];
					s.successCriteria.add(c);
				}
			} else if (line.startsWith("@stepOnSuccess ")) {
				String[] parts = line.substring("@stepOnSuccess ".length()).trim().split("\\s+", 4);
				if (parts.length == 4 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.onSuccess == null) {
						s.onSuccess = new ArrayList<>();
					}
					SuccessAction sa = new SuccessAction();
					sa.type = SuccessActionType.fromString(parts[1]);
					sa.name = "-".equals(parts[2]) ? null : parts[2];
					sa.stepId = "-".equals(parts[3]) ? null : parts[3];
					s.onSuccess.add(ReusableOr.ofItem(sa));
				}
			} else if (line.startsWith("@stepOnFailure ")) {
				String[] parts = line.substring("@stepOnFailure ".length()).trim().split("\\s+", 4);
				if (parts.length == 4 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.onFailure == null) {
						s.onFailure = new ArrayList<>();
					}
					FailureAction fa = new FailureAction();
					fa.type = FailureActionType.fromString(parts[1]);
					fa.name = "-".equals(parts[2]) ? null : parts[2];
					fa.stepId = "-".equals(parts[3]) ? null : parts[3];
					s.onFailure.add(ReusableOr.ofItem(fa));
				}
			} else if (line.startsWith("@stepOutput ")) {
				String[] parts = line.substring("@stepOutput ".length()).trim().split("\\s+", 4);
				if (parts.length == 4 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.outputs == null) {
						s.outputs = new LinkedHashMap<>();
					}
					s.outputs.put(parts[1], parseOutputTag(parts[2], parts[3]));
				}
			} else if (line.startsWith("@workflowOutput ")) {
				String[] parts = line.substring("@workflowOutput ".length()).trim().split("\\s+", 3);
				if (parts.length == 3) {
					if (wf.outputs == null) {
						wf.outputs = new LinkedHashMap<>();
					}
					wf.outputs.put(parts[0], parseOutputTag(parts[1], parts[2]));
				}
			}
		}
	}

	/**
	 * Parses a formatted payload tag ({@code str:...}, {@code num:...}, or
	 * {@code json:...}) into a {@link PayloadValue}.
	 *
	 * @param token
	 *            The formatted payload token.
	 * @return The parsed {@link PayloadValue}.
	 */
	private static PayloadValue parsePayloadTag(String token) {
		if (token.startsWith("num:")) {
			String numStr = token.substring(4);
			if (numStr.contains(".")) {
				return PayloadValue.ofNumber(Double.parseDouble(numStr));
			}
			return PayloadValue.ofNumber(Integer.parseInt(numStr));
		}
		if (token.startsWith("json:")) {
			Object raw = new JSONTokener(token.substring(5)).nextValue();
			return arazzo.Parse.parsePayloadValue(raw);
		}
		if (token.startsWith("str:")) {
			return PayloadValue.ofString(token.substring(4));
		}
		return PayloadValue.ofString(token);
	}

	/**
	 * Parses an output tag ({@code selector} or {@code expr}) into an
	 * {@link OutputValue}.
	 *
	 * @param kind
	 *            The output kind ({@code "selector"} or {@code "expr"}).
	 * @param val
	 *            The selector string or runtime expression.
	 * @return The parsed {@link OutputValue}.
	 */
	private static OutputValue parseOutputTag(String kind, String val) {
		if ("selector".equals(kind)) {
			Selector sel = new Selector();
			sel.selector = val;
			return OutputValue.ofSelector(sel);
		}
		return OutputValue.ofExpression(val);
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
			parseWorkflowJavadoc(method.getJavadocComment().get().getContent(), wf);
		}

		if (wf.inputs == null) {
			for (Parameter param : method.getParameters()) {
				String typeName = param.getTypeAsString();
				ClassOrInterfaceDeclaration dto = nestedDtos.get(typeName);
				if (dto != null) {
					Schema inSchema = new Schema();
					inSchema.type = openapi.SchemaType.ofSingle("object");
					inSchema.properties = new LinkedHashMap<>();
					for (FieldDeclaration fd : dto.getFields()) {
						for (VariableDeclarator vd : fd.getVariables()) {
							if ("values".equals(vd.getNameAsString())) {
								continue;
							}
							Schema propSchema = new Schema();
							TypeMapper.toSchema(vd.getType(), propSchema, true);
							inSchema.properties.put(vd.getNameAsString(), ReferenceOr.ofValue(propSchema));
						}
					}
					if (!inSchema.properties.isEmpty()) {
						wf.inputs = ReferenceOr.ofValue(inSchema);
					}
				}
			}
		}

		if (wf.steps.isEmpty()) {
			for (SwitchEntry entry : method.findAll(SwitchEntry.class)) {
				if (entry.getLabels().isNonEmpty()) {
					String stepId = entry.getLabels().get(0).toString().replace("\"", "");
					Step step = new Step();
					step.stepId = stepId;
					step.operationId = stepId;
					wf.steps.add(step);
				}
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

		if (wf.outputs == null) {
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
		}

		return wf;
	}
}
