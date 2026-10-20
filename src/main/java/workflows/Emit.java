package workflows;

import arazzo.Arazzo;
import arazzo.ArazzoParameter;
import arazzo.Criterion;
import arazzo.FailureAction;
import arazzo.OutputValue;
import arazzo.PayloadReplacement;
import arazzo.PayloadValue;
import arazzo.ReusableOr;
import arazzo.SourceDescription;
import arazzo.Step;
import arazzo.SuccessAction;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.OpenAPI;
import openapi.Schema;
import openapi.TypeMapper;

/**
 * Emits strongly typed Java Client SDK workflow orchestrator classes from an
 * {@link Arazzo} 1.1.0 specification.
 */
public class Emit {
	/**
	 * Default constructor.
	 */
	public Emit() {
	}

	/**
	 * Emits a strongly typed Java workflow orchestrator class from an
	 * {@link Arazzo} specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java source code string.
	 */
	public static String emit(Arazzo spec) {
		return emit(spec, null, null);
	}

	/**
	 * Emits a strongly typed Java workflow orchestrator class from an
	 * {@link Arazzo} specification and optional linked {@link OpenAPI} sources,
	 * preserving formatting via {@link LexicalPreservingPrinter} when updating
	 * existing source code.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @param openApiSources
	 *            Optional map of {@code sourceDescription.name} to {@link OpenAPI}
	 *            models.
	 * @param existingSource
	 *            Optional existing Java source code to update in-place.
	 * @return The generated or updated Java source code string.
	 */
	public static String emit(Arazzo spec, Map<String, OpenAPI> openApiSources, String existingSource) {
		CompilationUnit cu;
		ClassOrInterfaceDeclaration classDecl;
		boolean usingLpp = false;

		String className = deriveClassName(spec);
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = StaticJavaParser.parse(existingSource);
			LexicalPreservingPrinter.setup(cu);
			usingLpp = true;
			classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class).orElse(null);
			if (classDecl == null) {
				classDecl = cu.addClass(className);
			}
		} else {
			cu = new CompilationUnit();
			cu.addImport("java.util.LinkedHashMap");
			cu.addImport("java.util.Map");
			cu.addImport("java.util.List");
			cu.addImport("java.util.ArrayList");
			cu.addImport("arazzo.Arazzo");
			cu.addImport("arazzo.RuntimeExpression");
			classDecl = cu.addClass(className).setPublic(true);
		}

		StringBuilder classDoc = new StringBuilder("\n * Workflow orchestrator generated from Arazzo "
				+ (spec.arazzo != null ? spec.arazzo : "1.1.0") + ".\n");
		appendSpecJavadoc(classDoc, " * ", spec);
		if (spec.sourceDescriptions != null) {
			for (SourceDescription sd : spec.sourceDescriptions) {
				String fieldName = TypeMapper.sanitizeIdentifier(sd.name) + "Url";
				if (!classDecl.getFieldByName(fieldName).isPresent()) {
					FieldDeclaration fd = classDecl.addFieldWithInitializer("String", fieldName,
							StaticJavaParser.parseExpression("\"" + TypeMapper.escape(sd.url) + "\""),
							Modifier.Keyword.PUBLIC);
					fd.setJavadocComment(" Base URL or spec URI for source description " + sd.name + ". ");
				}
			}
		}
		classDoc.append(" ");
		classDecl.setJavadocComment(classDoc.toString());

		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				String wfMethodName = TypeMapper.sanitizeIdentifier(wf.workflowId);
				String inputClassName = capitalize(wfMethodName) + "Inputs";
				String outputClassName = capitalize(wfMethodName) + "Outputs";

				emitInputDto(classDecl, inputClassName, arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec),
						openApiSources);
				emitOutputDto(classDecl, outputClassName, wf.outputs);

				if (!hasMethod(classDecl, wfMethodName)) {
					emitWorkflowMethod(classDecl, wf, wfMethodName, inputClassName, outputClassName, spec);
				}
			}
		}

		return usingLpp ? LexicalPreservingPrinter.print(cu) : cu.toString();
	}

	/**
	 * Appends structured class-level Arazzo metadata Javadoc tags
	 * ({@code @arazzoSelf}, {@code @infoTitle}, {@code @infoSummary},
	 * {@code @infoDescription}, {@code @infoVersion}, and
	 * {@code @sourceDescription}).
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param prefix
	 *            The line prefix (e.g. {@code " * "}).
	 * @param spec
	 *            The {@link Arazzo} specification.
	 */
	public static void appendSpecJavadoc(StringBuilder sb, String prefix, Arazzo spec) {
		if (spec.$self != null) {
			sb.append(prefix).append("@arazzoSelf ").append(spec.$self).append("\n");
		}
		if (spec.info != null) {
			if (spec.info.title != null) {
				sb.append(prefix).append("@infoTitle ").append(spec.info.title).append("\n");
			}
			if (spec.info.summary != null) {
				sb.append(prefix).append("@infoSummary ").append(spec.info.summary).append("\n");
			}
			if (spec.info.description != null) {
				sb.append(prefix).append("@infoDescription ").append(spec.info.description.replace("\n", " "))
						.append("\n");
			}
			if (spec.info.version != null) {
				sb.append(prefix).append("@infoVersion ").append(spec.info.version).append("\n");
			}
		}
		if (spec.sourceDescriptions != null) {
			for (SourceDescription sd : spec.sourceDescriptions) {
				sb.append(prefix).append("@sourceDescription ").append(sd.name).append(" ")
						.append(sd.type != null ? sd.type.getValue() : "openapi").append(" ").append(sd.url)
						.append("\n");
			}
		}
	}

	/**
	 * Appends structured method-level Arazzo workflow and step metadata Javadoc
	 * tags.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param prefix
	 *            The line prefix (e.g. {@code " * "} or {@code "\t * "}).
	 * @param wf
	 *            The {@link Workflow} model.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 */
	public static void appendWorkflowJavadoc(StringBuilder sb, String prefix, Workflow wf, Arazzo spec) {
		sb.append(prefix).append("@workflowId ").append(wf.workflowId).append("\n");
		if (wf.summary != null) {
			sb.append(prefix).append("@workflowSummary ").append(wf.summary).append("\n");
		}
		if (wf.description != null) {
			sb.append(prefix).append("@workflowDescription ").append(wf.description.replace("\n", " ")).append("\n");
		}
		if (wf.dependsOn != null) {
			for (String dep : wf.dependsOn) {
				sb.append(prefix).append("@dependsOn ").append(dep).append("\n");
			}
		}
		Schema inSchema = arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec);
		if (inSchema != null && inSchema.properties != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> prop : inSchema.properties.entrySet()) {
				String pType = "string";
				if (prop.getValue() != null && prop.getValue().value.type != null) {
					pType = String.valueOf(prop.getValue().value.type);
				}
				sb.append(prefix).append("@workflowInput ").append(prop.getKey()).append(" ").append(pType)
						.append("\n");
			}
		}
		if (wf.parameters != null) {
			for (ReusableOr<ArazzoParameter> wp : wf.parameters) {
				ArazzoParameter resolved = arazzo.ArazzoResolver.resolveParameter(wp, spec);
				if (resolved != null) {
					String inVal = resolved.in != null ? resolved.in.getValue() : "query";
					sb.append(prefix).append("@workflowParam ").append(inVal).append(" ").append(resolved.name)
							.append(" ").append(formatPayloadTag(resolved.value)).append("\n");
				}
			}
		}
		if (wf.steps != null) {
			for (Step s : wf.steps) {
				String opId = s.operationId != null ? s.operationId : "-";
				sb.append(prefix).append("@stepMeta ").append(s.stepId).append(" ").append(opId).append("\n");
				if (s.parameters != null) {
					for (ReusableOr<ArazzoParameter> sp : s.parameters) {
						ArazzoParameter resolved = arazzo.ArazzoResolver.resolveParameter(sp, spec);
						if (resolved != null) {
							String inVal = resolved.in != null ? resolved.in.getValue() : "query";
							sb.append(prefix).append("@stepParam ").append(s.stepId).append(" ").append(inVal)
									.append(" ").append(resolved.name).append(" ")
									.append(formatPayloadTag(resolved.value)).append("\n");
						}
					}
				}
				if (s.requestBody != null && s.requestBody.payload != null) {
					sb.append(prefix).append("@stepBody ").append(s.stepId).append(" ")
							.append(formatPayloadTag(s.requestBody.payload)).append("\n");
				}
				if (s.requestBody != null && s.requestBody.replacements != null) {
					for (PayloadReplacement pr : s.requestBody.replacements) {
						sb.append(prefix).append("@stepReplacement ").append(s.stepId).append(" ").append(pr.target)
								.append(" ").append(formatPayloadTag(pr.value)).append("\n");
					}
				}
				if (s.successCriteria != null) {
					for (Criterion c : s.successCriteria) {
						if (c.condition != null) {
							String ctx = c.context != null ? c.context : "-";
							String cType = c.type != null ? c.type.resolveEngineType().getValue() : "-";
							sb.append(prefix).append("@stepCriterion ").append(s.stepId).append(" ").append(ctx)
									.append(" ").append(cType).append(" ").append(c.condition).append("\n");
						}
					}
				}
				if (s.onSuccess != null) {
					for (ReusableOr<SuccessAction> osa : s.onSuccess) {
						SuccessAction sa = arazzo.ArazzoResolver.resolveSuccessAction(osa, spec);
						if (sa != null && sa.type != null) {
							String name = sa.name != null ? sa.name : "-";
							String target = sa.stepId != null ? sa.stepId : "-";
							sb.append(prefix).append("@stepOnSuccess ").append(s.stepId).append(" ")
									.append(sa.type.getValue()).append(" ").append(name).append(" ").append(target)
									.append("\n");
						}
					}
				}
				if (s.onFailure != null) {
					for (ReusableOr<FailureAction> ofa : s.onFailure) {
						FailureAction fa = arazzo.ArazzoResolver.resolveFailureAction(ofa, spec);
						if (fa != null && fa.type != null) {
							String name = fa.name != null ? fa.name : "-";
							String target = fa.stepId != null ? fa.stepId : "-";
							sb.append(prefix).append("@stepOnFailure ").append(s.stepId).append(" ")
									.append(fa.type.getValue()).append(" ").append(name).append(" ").append(target)
									.append("\n");
						}
					}
				}
				if (s.outputs != null) {
					for (Map.Entry<String, OutputValue> so : s.outputs.entrySet()) {
						OutputValue ov = so.getValue();
						if (ov.isSelector()) {
							sb.append(prefix).append("@stepOutput ").append(s.stepId).append(" ").append(so.getKey())
									.append(" selector ").append(ov.selector.selector).append("\n");
						} else {
							sb.append(prefix).append("@stepOutput ").append(s.stepId).append(" ").append(so.getKey())
									.append(" expr ").append(ov.expression).append("\n");
						}
					}
				}
			}
		}
		if (wf.outputs != null) {
			for (Map.Entry<String, OutputValue> wo : wf.outputs.entrySet()) {
				OutputValue ov = wo.getValue();
				if (ov.isSelector()) {
					sb.append(prefix).append("@workflowOutput ").append(wo.getKey()).append(" selector ")
							.append(ov.selector.selector).append("\n");
				} else {
					sb.append(prefix).append("@workflowOutput ").append(wo.getKey()).append(" expr ")
							.append(ov.expression).append("\n");
				}
			}
		}
	}

	/**
	 * Formats a {@link PayloadValue} as a typed Javadoc token ({@code str:...},
	 * {@code num:...}, or {@code json:...}).
	 *
	 * @param pv
	 *            The {@link PayloadValue} to format.
	 * @return The formatted string token.
	 */
	private static String formatPayloadTag(PayloadValue pv) {
		if (pv.isString()) {
			return "str:" + pv.stringVal;
		}
		if (pv.isNumber()) {
			return "num:" + pv.numberVal;
		}
		return "json:" + arazzo.Emit.serializePayloadValue(pv).toString();
	}

	/**
	 * Emits a map of modular workflow files ({@code workflows/<ClassName>.java})
	 * from an {@link Arazzo} specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} specification.
	 * @param openApiSources
	 *            Optional map of linked {@link OpenAPI} sources.
	 * @return Map of relative file paths to Java source strings.
	 */
	public static Map<String, String> emitModular(Arazzo spec, Map<String, OpenAPI> openApiSources) {
		Map<String, String> files = new LinkedHashMap<>();
		String className = deriveClassName(spec);
		files.put("workflows/" + className + ".java", emit(spec, openApiSources, null));
		return files;
	}

	/**
	 * Emits an inner input DTO class for a workflow's JSON Schema inputs.
	 *
	 * @param parentClass
	 *            The enclosing orchestrator class declaration.
	 * @param dtoName
	 *            The inner DTO class name.
	 * @param inputSchema
	 *            The resolved input {@link Schema}.
	 * @param openApiSources
	 *            Optional linked {@link OpenAPI} models for type resolution.
	 */
	private static void emitInputDto(ClassOrInterfaceDeclaration parentClass, String dtoName, Schema inputSchema,
			Map<String, OpenAPI> openApiSources) {
		for (ClassOrInterfaceDeclaration inner : parentClass.findAll(ClassOrInterfaceDeclaration.class)) {
			if (inner != parentClass && inner.getNameAsString().equals(dtoName)) {
				return;
			}
		}
		ClassOrInterfaceDeclaration dto = new ClassOrInterfaceDeclaration();
		dto.setName(dtoName);
		dto.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		dto.setJavadocComment(" Strongly typed workflow inputs for " + dtoName + ". ");
		OpenAPI firstApi = (openApiSources != null && !openApiSources.isEmpty())
				? openApiSources.values().iterator().next()
				: new OpenAPI();
		if (inputSchema != null && inputSchema.properties != null) {
			for (Map.Entry<String, openapi.ReferenceOr<Schema>> prop : inputSchema.properties.entrySet()) {
				String fieldName = TypeMapper.sanitizeIdentifier(prop.getKey());
				String javaType = TypeMapper.toJavaType(prop.getValue(), firstApi, false);
				FieldDeclaration fd = dto.addField(javaType, fieldName, Modifier.Keyword.PUBLIC);
				fd.setJavadocComment(" Workflow input parameter " + fieldName + ". ");
			}
		}
		FieldDeclaration rawMap = dto.addFieldWithInitializer("Map<String, String>", "values",
				StaticJavaParser.parseExpression("new LinkedHashMap<>()"), Modifier.Keyword.PUBLIC);
		rawMap.setJavadocComment(" Dynamic key-value input bindings. ");
		parentClass.addMember(dto);
	}

	/**
	 * Emits an inner output DTO class for a workflow's declared outputs.
	 *
	 * @param parentClass
	 *            The enclosing orchestrator class declaration.
	 * @param dtoName
	 *            The inner DTO class name.
	 * @param outputs
	 *            The workflow's declared outputs map.
	 */
	private static void emitOutputDto(ClassOrInterfaceDeclaration parentClass, String dtoName,
			Map<String, OutputValue> outputs) {
		for (ClassOrInterfaceDeclaration inner : parentClass.findAll(ClassOrInterfaceDeclaration.class)) {
			if (inner != parentClass && inner.getNameAsString().equals(dtoName)) {
				return;
			}
		}
		ClassOrInterfaceDeclaration dto = new ClassOrInterfaceDeclaration();
		dto.setName(dtoName);
		dto.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		dto.setJavadocComment(" Strongly typed workflow outputs for " + dtoName + ". ");
		if (outputs != null) {
			for (String outKey : outputs.keySet()) {
				String fieldName = TypeMapper.sanitizeIdentifier(outKey);
				FieldDeclaration fd = dto.addField("String", fieldName, Modifier.Keyword.PUBLIC);
				fd.setJavadocComment(" Extracted workflow output " + fieldName + ". ");
			}
		}
		FieldDeclaration rawMap = dto.addFieldWithInitializer("Map<String, String>", "values",
				StaticJavaParser.parseExpression("new LinkedHashMap<>()"), Modifier.Keyword.PUBLIC);
		rawMap.setJavadocComment(" Dynamic key-value output bindings. ");
		parentClass.addMember(dto);
	}

	/**
	 * Emits an executable workflow orchestration method on the target class.
	 *
	 * @param classDecl
	 *            The orchestrator class declaration.
	 * @param wf
	 *            The {@link Workflow} model.
	 * @param wfMethodName
	 *            The sanitized Java method name.
	 * @param inputClassName
	 *            The input DTO class name.
	 * @param outputClassName
	 *            The output DTO class name.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 */
	private static void emitWorkflowMethod(ClassOrInterfaceDeclaration classDecl, Workflow wf, String wfMethodName,
			String inputClassName, String outputClassName, Arazzo spec) {
		StringBuilder sb = new StringBuilder();
		sb.append("/**\n");
		sb.append(" * Executes workflow ").append(wf.workflowId).append(".\n");
		appendWorkflowJavadoc(sb, " * ", wf, spec);
		sb.append(" * @param inputs Strongly typed workflow inputs.\n");
		sb.append(" * @param ctx Runtime execution context.\n");
		sb.append(" * @return Strongly typed workflow outputs.\n");
		sb.append(" */\n");
		sb.append("public ").append(outputClassName).append(" ").append(wfMethodName).append("(").append(inputClassName)
				.append(" inputs, RuntimeExpression.Context ctx) {\n");
		sb.append("    if (ctx == null) { ctx = new RuntimeExpression.Context(); }\n");
		sb.append("    if (inputs != null && inputs.values != null) { ctx.inputs.putAll(inputs.values); }\n");

		Schema inSchema = arazzo.ArazzoResolver.resolveWorkflowInputs(wf, spec);
		if (inSchema != null && inSchema.properties != null) {
			for (String propName : inSchema.properties.keySet()) {
				String fName = TypeMapper.sanitizeIdentifier(propName);
				sb.append("    if (inputs != null && inputs.").append(fName).append(" != null) { ctx.inputs.put(\"")
						.append(TypeMapper.escape(propName)).append("\", String.valueOf(inputs.").append(fName)
						.append(")); }\n");
			}
		}

		if (wf.dependsOn != null) {
			for (String dep : wf.dependsOn) {
				String depMethod = TypeMapper.sanitizeIdentifier(dep);
				String depIn = capitalize(depMethod) + "Inputs";
				String depOut = capitalize(depMethod) + "Outputs";
				if (hasWorkflow(spec, dep)) {
					sb.append("    ").append(depOut).append(" dep_").append(depMethod).append(" = ").append(depMethod)
							.append("(new ").append(depIn).append("(), ctx);\n");
					sb.append("    ctx.workflows.put(\"").append(TypeMapper.escape(dep)).append("\", dep_")
							.append(depMethod).append(".values);\n");
				}
			}
		}

		if (wf.parameters != null) {
			for (ReusableOr<ArazzoParameter> wp : wf.parameters) {
				ArazzoParameter resolved = arazzo.ArazzoResolver.resolveParameter(wp, spec);
				if (resolved != null) {
					emitParamBinding(sb, "    ", resolved);
				}
			}
		}

		if (wf.steps != null && !wf.steps.isEmpty()) {
			String firstStep = wf.steps.get(0).stepId;
			sb.append("    String currentStep = \"").append(TypeMapper.escape(firstStep)).append("\";\n");
			sb.append("    int stepGuard = 0;\n");
			sb.append("    while (currentStep != null && stepGuard++ < 100) {\n");
			sb.append("        switch (currentStep) {\n");
			for (int i = 0; i < wf.steps.size(); i++) {
				Step step = wf.steps.get(i);
				String nextStep = (i + 1 < wf.steps.size()) ? wf.steps.get(i + 1).stepId : null;
				emitStepCase(sb, step, nextStep, spec);
			}
			sb.append("            default:\n");
			sb.append("                currentStep = null;\n");
			sb.append("                break;\n");
			sb.append("        }\n");
			sb.append("    }\n");
		}

		sb.append("    ").append(outputClassName).append(" out = new ").append(outputClassName).append("();\n");
		if (wf.outputs != null) {
			for (Map.Entry<String, OutputValue> oe : wf.outputs.entrySet()) {
				String outKey = oe.getKey();
				String fName = TypeMapper.sanitizeIdentifier(outKey);
				OutputValue ov = oe.getValue();
				if (ov.isSelector()) {
					sb.append("    out.").append(fName)
							.append(" = RuntimeExpression.queryJsonPath(ctx.responseBody, \"")
							.append(TypeMapper.escape(ov.selector.selector)).append("\");\n");
				} else {
					sb.append("    out.").append(fName).append(" = RuntimeExpression.evaluate(\"")
							.append(TypeMapper.escape(ov.expression)).append("\", ctx);\n");
				}
				sb.append("    out.values.put(\"").append(TypeMapper.escape(outKey)).append("\", out.").append(fName)
						.append(");\n");
				sb.append("    ctx.outputs.put(\"").append(TypeMapper.escape(outKey)).append("\", out.").append(fName)
						.append(");\n");
			}
		}
		sb.append("    ctx.workflows.put(\"").append(TypeMapper.escape(wf.workflowId)).append("\", out.values);\n");
		sb.append("    return out;\n");
		sb.append("}\n");

		classDecl.addMember(StaticJavaParser.parseBodyDeclaration(sb.toString()));
	}

	/**
	 * Appends a {@code switch} case block for a single {@link Step}.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param step
	 *            The {@link Step} model.
	 * @param nextStep
	 *            The next sequential {@code stepId}, or {@code null} if terminal.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 */
	private static void emitStepCase(StringBuilder sb, Step step, String nextStep, Arazzo spec) {
		sb.append("            case \"").append(TypeMapper.escape(step.stepId)).append("\": {\n");
		if (step.parameters != null) {
			for (ReusableOr<ArazzoParameter> sp : step.parameters) {
				ArazzoParameter resolved = arazzo.ArazzoResolver.resolveParameter(sp, spec);
				if (resolved != null) {
					emitParamBinding(sb, "                ", resolved);
				}
			}
		}
		if (step.requestBody != null && step.requestBody.payload != null) {
			String payloadStr = step.requestBody.payload.isString()
					? step.requestBody.payload.stringVal
					: arazzo.Emit.serializePayloadValue(step.requestBody.payload).toString();
			sb.append("                ctx.requestBody = RuntimeExpression.evaluate(\"")
					.append(TypeMapper.escape(payloadStr)).append("\", ctx);\n");
		}
		sb.append("                boolean stepPassed = true;\n");
		if (step.successCriteria != null) {
			for (Criterion c : step.successCriteria) {
				String condEsc = TypeMapper.escape(c.condition);
				String ctxEsc = c.context != null ? TypeMapper.escape(c.context) : "";
				String typeStr = c.type != null ? c.type.resolveEngineType().getValue() : "simple";
				sb.append("                {\n");
				sb.append("                    arazzo.Criterion crit = new arazzo.Criterion();\n");
				if (!ctxEsc.isEmpty()) {
					sb.append("                    crit.context = \"").append(ctxEsc).append("\";\n");
				}
				sb.append("                    crit.condition = \"").append(condEsc).append("\";\n");
				sb.append("                    crit.type = arazzo.CriterionTypeOrExpression.ofSimple("
						+ "arazzo.CriterionEngineType.fromString(\"").append(typeStr).append("\"));\n");
				sb.append("                    if (!RuntimeExpression.evaluateCriterion(crit, ctx)) {\n");
				sb.append("                        stepPassed = false;\n");
				sb.append("                    }\n");
				sb.append("                }\n");
			}
		}

		sb.append("                Map<String, String> stepOut = new LinkedHashMap<>();\n");
		if (step.outputs != null) {
			for (Map.Entry<String, OutputValue> so : step.outputs.entrySet()) {
				OutputValue ov = so.getValue();
				if (ov.isSelector()) {
					sb.append("                stepOut.put(\"").append(TypeMapper.escape(so.getKey()))
							.append("\", RuntimeExpression.queryJsonPath(ctx.responseBody, \"")
							.append(TypeMapper.escape(ov.selector.selector)).append("\"));\n");
				} else {
					sb.append("                stepOut.put(\"").append(TypeMapper.escape(so.getKey()))
							.append("\", RuntimeExpression.evaluate(\"").append(TypeMapper.escape(ov.expression))
							.append("\", ctx));\n");
				}
			}
		}
		sb.append("                ctx.steps.put(\"").append(TypeMapper.escape(step.stepId)).append("\", stepOut);\n");

		String defaultNext = nextStep != null ? "\"" + TypeMapper.escape(nextStep) + "\"" : "null";
		String onSuccessTarget = defaultNext;
		if (step.onSuccess != null && !step.onSuccess.isEmpty()) {
			SuccessAction sa = arazzo.ArazzoResolver.resolveSuccessAction(step.onSuccess.get(0), spec);
			if (sa != null) {
				if (sa.type == arazzo.SuccessActionType.END) {
					onSuccessTarget = "null";
				} else if (sa.stepId != null) {
					onSuccessTarget = "\"" + TypeMapper.escape(sa.stepId) + "\"";
				}
			}
		}
		String onFailureTarget = "null";
		if (step.onFailure != null && !step.onFailure.isEmpty()) {
			FailureAction fa = arazzo.ArazzoResolver.resolveFailureAction(step.onFailure.get(0), spec);
			if (fa != null && fa.stepId != null) {
				onFailureTarget = "\"" + TypeMapper.escape(fa.stepId) + "\"";
			}
		}

		sb.append("                currentStep = stepPassed ? ").append(onSuccessTarget).append(" : ")
				.append(onFailureTarget).append(";\n");
		sb.append("                break;\n");
		sb.append("            }\n");
	}

	/**
	 * Appends a parameter binding statement to the generated method body.
	 *
	 * @param sb
	 *            The target {@link StringBuilder}.
	 * @param indent
	 *            The indentation prefix.
	 * @param param
	 *            The resolved {@link ArazzoParameter}.
	 */
	private static void emitParamBinding(StringBuilder sb, String indent, ArazzoParameter param) {
		String valStr = param.value.isString() ? param.value.stringVal : String.valueOf(param.value);
		String targetMap = "ctx.requestQuery";
		if (param.in == arazzo.ParameterIn.PATH) {
			targetMap = "ctx.requestPath";
		} else if (param.in == arazzo.ParameterIn.HEADER || param.in == arazzo.ParameterIn.COOKIE) {
			targetMap = "ctx.requestHeaders";
		}
		sb.append(indent).append(targetMap).append(".put(\"").append(TypeMapper.escape(param.name))
				.append("\", RuntimeExpression.evaluate(\"").append(TypeMapper.escape(valStr)).append("\", ctx));\n");
	}

	/**
	 * Checks whether the given {@link Arazzo} specification defines a workflow with
	 * {@code workflowId}.
	 *
	 * @param spec
	 *            The {@link Arazzo} specification.
	 * @param workflowId
	 *            The target workflow identifier.
	 * @return {@code true} if present.
	 */
	private static boolean hasWorkflow(Arazzo spec, String workflowId) {
		for (Workflow wf : spec.workflows) {
			if (workflowId.equals(wf.workflowId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Derives a valid PascalCase Java class name from the {@link Arazzo} info
	 * title.
	 *
	 * @param spec
	 *            The {@link Arazzo} specification.
	 * @return The derived class name ending with {@code Workflows}.
	 */
	public static String deriveClassName(Arazzo spec) {
		if (spec != null && spec.info != null && spec.info.title != null) {
			String clean = TypeMapper.sanitizeAlphanumeric(spec.info.title);
			if (!clean.isEmpty()) {
				return capitalize(clean) + (clean.endsWith("Workflows") ? "" : "Workflows");
			}
		}
		return "ArazzoWorkflows";
	}

	/**
	 * Capitalizes the first character of a non-empty string.
	 *
	 * @param str
	 *            The input string.
	 * @return The capitalized string.
	 */
	private static String capitalize(String str) {
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * Checks whether a class declaration already contains a method with the given
	 * name.
	 *
	 * @param classDecl
	 *            The class declaration.
	 * @param name
	 *            The method name.
	 * @return {@code true} if present.
	 */
	private static boolean hasMethod(ClassOrInterfaceDeclaration classDecl, String name) {
		for (MethodDeclaration m : classDecl.getMethods()) {
			if (m.getNameAsString().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
