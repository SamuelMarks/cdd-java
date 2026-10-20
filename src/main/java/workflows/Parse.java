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
import arazzo.ReusableOr;
import common.PayloadValue;
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
		arazzo.ArazzoJavadocCodec.parseSpecJavadoc(javadocContent, spec);
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
		arazzo.ArazzoJavadocCodec.parseWorkflowJavadoc(javadocContent, wf);
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
