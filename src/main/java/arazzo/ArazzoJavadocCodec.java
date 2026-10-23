package arazzo;

import common.PayloadValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.Reference;
import openapi.ReferenceOr;
import openapi.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Shared Javadoc metadata emitter and parser for Arazzo 1.1.0 class-level and
 * method-level tags across {@code workflows}, {@code cli}, {@code tests}, and
 * {@code mocks}.
 */
public final class ArazzoJavadocCodec {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private ArazzoJavadocCodec() {
	}

	/**
	 * Appends structured class-level Arazzo metadata Javadoc tags
	 * ({@code @arazzoSelf}, {@code @arazzoExt}, {@code @infoTitle},
	 * {@code @infoSummary}, {@code @infoDescription}, {@code @infoVersion},
	 * {@code @infoExt}, {@code @sourceDescription}, {@code @componentInput},
	 * {@code @componentParameter}, {@code @componentSuccessAction},
	 * {@code @componentFailureAction}, and {@code @componentExt}).
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
		if (!spec.extensions.isEmpty()) {
			for (Map.Entry<String, PayloadValue> ext : spec.extensions.entrySet()) {
				sb.append(prefix).append("@arazzoExt ").append(ext.getKey()).append(" ")
						.append(formatPayloadTag(ext.getValue())).append("\n");
			}
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
			if (!spec.info.extensions.isEmpty()) {
				for (Map.Entry<String, PayloadValue> ext : spec.info.extensions.entrySet()) {
					sb.append(prefix).append("@infoExt ").append(ext.getKey()).append(" ")
							.append(formatPayloadTag(ext.getValue())).append("\n");
				}
			}
		}
		if (spec.sourceDescriptions != null) {
			for (SourceDescription sd : spec.sourceDescriptions) {
				sb.append(prefix).append("@sourceDescription ").append(sd.name).append(" ")
						.append(sd.type != null ? sd.type.getValue() : "openapi").append(" ").append(sd.url)
						.append("\n");
				if (!sd.extensions.isEmpty()) {
					for (Map.Entry<String, PayloadValue> ext : sd.extensions.entrySet()) {
						sb.append(prefix).append("@sourceDescriptionExt ").append(sd.name).append(" ")
								.append(ext.getKey()).append(" ").append(formatPayloadTag(ext.getValue())).append("\n");
					}
				}
			}
		}
		if (spec.components != null) {
			if (spec.components.inputs != null) {
				for (Map.Entry<String, Schema> e : spec.components.inputs.entrySet()) {
					sb.append(prefix).append("@componentInput ").append(e.getKey()).append(" ")
							.append(openapi.Emit.serializeSchema(e.getValue()).toString()).append("\n");
				}
			}
			if (spec.components.parameters != null) {
				for (Map.Entry<String, ArazzoParameter> e : spec.components.parameters.entrySet()) {
					sb.append(prefix).append("@componentParameter ").append(e.getKey()).append(" ")
							.append(Emit.serializeParameter(e.getValue()).toString()).append("\n");
				}
			}
			if (spec.components.successActions != null) {
				for (Map.Entry<String, SuccessAction> e : spec.components.successActions.entrySet()) {
					sb.append(prefix).append("@componentSuccessAction ").append(e.getKey()).append(" ")
							.append(Emit.serializeSuccessAction(e.getValue()).toString()).append("\n");
				}
			}
			if (spec.components.failureActions != null) {
				for (Map.Entry<String, FailureAction> e : spec.components.failureActions.entrySet()) {
					sb.append(prefix).append("@componentFailureAction ").append(e.getKey()).append(" ")
							.append(Emit.serializeFailureAction(e.getValue()).toString()).append("\n");
				}
			}
			if (!spec.components.extensions.isEmpty()) {
				for (Map.Entry<String, PayloadValue> ext : spec.components.extensions.entrySet()) {
					sb.append(prefix).append("@componentExt ").append(ext.getKey()).append(" ")
							.append(formatPayloadTag(ext.getValue())).append("\n");
				}
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
		if (wf.inputs != null && wf.inputs.isRef()) {
			sb.append(prefix).append("@workflowInputsRef ").append(wf.inputs.ref.ref).append("\n");
		} else {
			Schema inSchema = ArazzoResolver.resolveWorkflowInputs(wf, spec);
			if (inSchema != null && inSchema.properties != null) {
				for (Map.Entry<String, ReferenceOr<Schema>> prop : inSchema.properties.entrySet()) {
					String pType = "string";
					if (prop.getValue() != null && prop.getValue().value.type != null) {
						pType = String.valueOf(prop.getValue().value.type);
					}
					sb.append(prefix).append("@workflowInput ").append(prop.getKey()).append(" ").append(pType)
							.append("\n");
				}
			}
		}
		if (wf.parameters != null) {
			for (ReusableOr<ArazzoParameter> wp : wf.parameters) {
				if (wp.isReusable()) {
					sb.append(prefix).append("@workflowParamJson ")
							.append(Emit.serializeReusableObject(wp.reusable).toString()).append("\n");
				} else if (wp.isItem()) {
					ArazzoParameter resolved = wp.item;
					if (resolved.extensions.isEmpty()) {
						String inVal = resolved.in != null ? resolved.in.getValue() : "query";
						sb.append(prefix).append("@workflowParam ").append(inVal).append(" ").append(resolved.name)
								.append(" ").append(formatPayloadTag(resolved.value)).append("\n");
					} else {
						sb.append(prefix).append("@workflowParamJson ")
								.append(Emit.serializeParameter(resolved).toString()).append("\n");
					}
				}
			}
		}
		if (wf.successActions != null) {
			for (ReusableOr<SuccessAction> wsa : wf.successActions) {
				JSONObject saJson = wsa.isReusable()
						? Emit.serializeReusableObject(wsa.reusable)
						: Emit.serializeSuccessAction(wsa.item);
				sb.append(prefix).append("@workflowSuccessAction ").append(saJson.toString()).append("\n");
			}
		}
		if (wf.failureActions != null) {
			for (ReusableOr<FailureAction> wfa : wf.failureActions) {
				JSONObject faJson = wfa.isReusable()
						? Emit.serializeReusableObject(wfa.reusable)
						: Emit.serializeFailureAction(wfa.item);
				sb.append(prefix).append("@workflowFailureAction ").append(faJson.toString()).append("\n");
			}
		}
		if (!wf.extensions.isEmpty()) {
			for (Map.Entry<String, PayloadValue> ext : wf.extensions.entrySet()) {
				sb.append(prefix).append("@workflowExt ").append(ext.getKey()).append(" ")
						.append(formatPayloadTag(ext.getValue())).append("\n");
			}
		}
		if (wf.steps != null) {
			for (Step s : wf.steps) {
				String opId = s.operationId != null ? s.operationId : "-";
				sb.append(prefix).append("@stepMeta ").append(s.stepId).append(" ").append(opId).append("\n");
				if (s.description != null) {
					sb.append(prefix).append("@stepDescription ").append(s.stepId).append(" ")
							.append(s.description.replace("\n", " ")).append("\n");
				}
				if (s.operationPath != null) {
					sb.append(prefix).append("@stepOperationPath ").append(s.stepId).append(" ").append(s.operationPath)
							.append("\n");
				}
				if (s.workflowId != null) {
					sb.append(prefix).append("@stepWorkflowId ").append(s.stepId).append(" ").append(s.workflowId)
							.append("\n");
				}
				if (s.channelPath != null) {
					sb.append(prefix).append("@stepChannelPath ").append(s.stepId).append(" ").append(s.channelPath)
							.append("\n");
				}
				if (s.timeout != null) {
					sb.append(prefix).append("@stepTimeout ").append(s.stepId).append(" ").append(s.timeout)
							.append("\n");
				}
				if (s.correlationId != null) {
					sb.append(prefix).append("@stepCorrelationId ").append(s.stepId).append(" ").append(s.correlationId)
							.append("\n");
				}
				if (s.action != null) {
					sb.append(prefix).append("@stepAction ").append(s.stepId).append(" ").append(s.action.getValue())
							.append("\n");
				}
				if (s.dependsOn != null) {
					for (String dep : s.dependsOn) {
						sb.append(prefix).append("@stepDependsOn ").append(s.stepId).append(" ").append(dep)
								.append("\n");
					}
				}
				if (!s.extensions.isEmpty()) {
					for (Map.Entry<String, PayloadValue> ext : s.extensions.entrySet()) {
						sb.append(prefix).append("@stepExt ").append(s.stepId).append(" ").append(ext.getKey())
								.append(" ").append(formatPayloadTag(ext.getValue())).append("\n");
					}
				}
				if (s.parameters != null) {
					for (ReusableOr<ArazzoParameter> sp : s.parameters) {
						if (sp.isReusable()) {
							sb.append(prefix).append("@stepParamJson ").append(s.stepId).append(" ")
									.append(Emit.serializeReusableObject(sp.reusable).toString()).append("\n");
						} else if (sp.isItem()) {
							ArazzoParameter resolved = sp.item;
							if (resolved.extensions.isEmpty()) {
								String inVal = resolved.in != null ? resolved.in.getValue() : "query";
								sb.append(prefix).append("@stepParam ").append(s.stepId).append(" ").append(inVal)
										.append(" ").append(resolved.name).append(" ")
										.append(formatPayloadTag(resolved.value)).append("\n");
							} else {
								sb.append(prefix).append("@stepParamJson ").append(s.stepId).append(" ")
										.append(Emit.serializeParameter(resolved).toString()).append("\n");
							}
						}
					}
				}
				if (s.requestBody != null) {
					if (s.requestBody.contentType != null || !s.requestBody.extensions.isEmpty()) {
						sb.append(prefix).append("@stepRequestBodyJson ").append(s.stepId).append(" ")
								.append(Emit.serializeRequestBody(s.requestBody).toString()).append("\n");
					} else {
						if (s.requestBody.payload != null) {
							sb.append(prefix).append("@stepBody ").append(s.stepId).append(" ")
									.append(formatPayloadTag(s.requestBody.payload)).append("\n");
						}
						if (s.requestBody.replacements != null) {
							for (PayloadReplacement pr : s.requestBody.replacements) {
								if (pr.targetSelectorType == null && pr.extensions.isEmpty()) {
									sb.append(prefix).append("@stepReplacement ").append(s.stepId).append(" ")
											.append(pr.target).append(" ").append(formatPayloadTag(pr.value))
											.append("\n");
								} else {
									sb.append(prefix).append("@stepReplacementJson ").append(s.stepId).append(" ")
											.append(Emit.serializePayloadReplacement(pr).toString()).append("\n");
								}
							}
						}
					}
				}
				if (s.successCriteria != null) {
					for (Criterion c : s.successCriteria) {
						if (c.condition != null && c.extensions.isEmpty()
								&& (c.type == null || !c.type.isExpressionType())) {
							String ctx = c.context != null ? c.context : "-";
							String cType = c.type != null ? c.type.resolveEngineType().getValue() : "-";
							sb.append(prefix).append("@stepCriterion ").append(s.stepId).append(" ").append(ctx)
									.append(" ").append(cType).append(" ").append(c.condition).append("\n");
						} else if (c.condition != null) {
							sb.append(prefix).append("@stepCriterionJson ").append(s.stepId).append(" ")
									.append(Emit.serializeCriterion(c).toString()).append("\n");
						}
					}
				}
				if (s.onSuccess != null) {
					for (ReusableOr<SuccessAction> osa : s.onSuccess) {
						if (osa.isReusable()) {
							sb.append(prefix).append("@stepOnSuccessJson ").append(s.stepId).append(" ")
									.append(Emit.serializeReusableObject(osa.reusable).toString()).append("\n");
						} else if (osa.isItem()) {
							SuccessAction sa = osa.item;
							if (sa.type != null && sa.workflowId == null && sa.parameters == null && sa.criteria == null
									&& sa.extensions.isEmpty()) {
								String name = sa.name != null ? sa.name : "-";
								String target = sa.stepId != null ? sa.stepId : "-";
								sb.append(prefix).append("@stepOnSuccess ").append(s.stepId).append(" ")
										.append(sa.type.getValue()).append(" ").append(name).append(" ").append(target)
										.append("\n");
							} else if (sa.type != null) {
								sb.append(prefix).append("@stepOnSuccessJson ").append(s.stepId).append(" ")
										.append(Emit.serializeSuccessAction(sa).toString()).append("\n");
							}
						}
					}
				}
				if (s.onFailure != null) {
					for (ReusableOr<FailureAction> ofa : s.onFailure) {
						if (ofa.isReusable()) {
							sb.append(prefix).append("@stepOnFailureJson ").append(s.stepId).append(" ")
									.append(Emit.serializeReusableObject(ofa.reusable).toString()).append("\n");
						} else if (ofa.isItem()) {
							FailureAction fa = ofa.item;
							if (fa.type != null && fa.workflowId == null && fa.parameters == null
									&& fa.retryAfter == null && fa.retryLimit == null && fa.criteria == null
									&& fa.extensions.isEmpty()) {
								String name = fa.name != null ? fa.name : "-";
								String target = fa.stepId != null ? fa.stepId : "-";
								sb.append(prefix).append("@stepOnFailure ").append(s.stepId).append(" ")
										.append(fa.type.getValue()).append(" ").append(name).append(" ").append(target)
										.append("\n");
							} else if (fa.type != null) {
								sb.append(prefix).append("@stepOnFailureJson ").append(s.stepId).append(" ")
										.append(Emit.serializeFailureAction(fa).toString()).append("\n");
							}
						}
					}
				}
				if (s.outputs != null) {
					for (Map.Entry<String, OutputValue> so : s.outputs.entrySet()) {
						OutputValue ov = so.getValue();
						if (ov.isSelector()) {
							if (ov.selector.context == null && ov.selector.type == null
									&& ov.selector.extensions.isEmpty()) {
								sb.append(prefix).append("@stepOutput ").append(s.stepId).append(" ")
										.append(so.getKey()).append(" selector ").append(ov.selector.selector)
										.append("\n");
							} else {
								sb.append(prefix).append("@stepOutput ").append(s.stepId).append(" ")
										.append(so.getKey()).append(" selectorJson ")
										.append(Emit.serializeSelector(ov.selector).toString()).append("\n");
							}
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
					if (ov.selector.context == null && ov.selector.type == null && ov.selector.extensions.isEmpty()) {
						sb.append(prefix).append("@workflowOutput ").append(wo.getKey()).append(" selector ")
								.append(ov.selector.selector).append("\n");
					} else {
						sb.append(prefix).append("@workflowOutput ").append(wo.getKey()).append(" selectorJson ")
								.append(Emit.serializeSelector(ov.selector).toString()).append("\n");
					}
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
	public static String formatPayloadTag(PayloadValue pv) {
		if (pv.isString()) {
			return "str:" + pv.stringVal;
		}
		if (pv.isNumber()) {
			return "num:" + pv.numberVal;
		}
		return "json:" + Emit.serializePayloadValue(pv).toString();
	}

	/**
	 * Parses class-level Arazzo metadata Javadoc tags ({@code @arazzoSelf},
	 * {@code @arazzoExt}, {@code @infoTitle}, {@code @infoSummary},
	 * {@code @infoDescription}, {@code @infoVersion}, {@code @infoExt},
	 * {@code @sourceDescription}, {@code @sourceDescriptionExt}, and
	 * {@code @component*}) into the target {@link Arazzo} specification.
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
			} else if (line.startsWith("@arazzoExt ")) {
				String[] parts = line.substring("@arazzoExt ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					spec.extensions.put(parts[0], parsePayloadTag(parts[1]));
				}
			} else if (line.startsWith("@infoTitle ")) {
				spec.info.title = line.substring("@infoTitle ".length()).trim();
			} else if (line.startsWith("@infoSummary ")) {
				spec.info.summary = line.substring("@infoSummary ".length()).trim();
			} else if (line.startsWith("@infoDescription ")) {
				spec.info.description = line.substring("@infoDescription ".length()).trim();
			} else if (line.startsWith("@infoVersion ")) {
				spec.info.version = line.substring("@infoVersion ".length()).trim();
			} else if (line.startsWith("@infoExt ")) {
				String[] parts = line.substring("@infoExt ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					spec.info.extensions.put(parts[0], parsePayloadTag(parts[1]));
				}
			} else if (line.startsWith("@sourceDescription ")) {
				String[] parts = line.substring("@sourceDescription ".length()).trim().split("\\s+", 3);
				if (parts.length == 3) {
					SourceDescription sd = new SourceDescription();
					sd.name = parts[0];
					sd.type = SourceType.fromString(parts[1]);
					sd.url = parts[2];
					spec.sourceDescriptions.add(sd);
				}
			} else if (line.startsWith("@sourceDescriptionExt ")) {
				String[] parts = line.substring("@sourceDescriptionExt ".length()).trim().split("\\s+", 3);
				if (parts.length == 3 && spec.sourceDescriptions != null) {
					for (SourceDescription sd : spec.sourceDescriptions) {
						if (parts[0].equals(sd.name)) {
							sd.extensions.put(parts[1], parsePayloadTag(parts[2]));
						}
					}
				}
			} else if (line.startsWith("@componentInput ")) {
				String[] parts = line.substring("@componentInput ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					ensureComponents(spec);
					if (spec.components.inputs == null) {
						spec.components.inputs = new LinkedHashMap<>();
					}
					spec.components.inputs.put(parts[0], openapi.Parse.parseSchema(new JSONObject(parts[1])));
				}
			} else if (line.startsWith("@componentParameter ")) {
				String[] parts = line.substring("@componentParameter ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					ensureComponents(spec);
					if (spec.components.parameters == null) {
						spec.components.parameters = new LinkedHashMap<>();
					}
					spec.components.parameters.put(parts[0], Parse.parseParameter(new JSONObject(parts[1])));
				}
			} else if (line.startsWith("@componentSuccessAction ")) {
				String[] parts = line.substring("@componentSuccessAction ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					ensureComponents(spec);
					if (spec.components.successActions == null) {
						spec.components.successActions = new LinkedHashMap<>();
					}
					spec.components.successActions.put(parts[0], Parse.parseSuccessAction(new JSONObject(parts[1])));
				}
			} else if (line.startsWith("@componentFailureAction ")) {
				String[] parts = line.substring("@componentFailureAction ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					ensureComponents(spec);
					if (spec.components.failureActions == null) {
						spec.components.failureActions = new LinkedHashMap<>();
					}
					spec.components.failureActions.put(parts[0], Parse.parseFailureAction(new JSONObject(parts[1])));
				}
			} else if (line.startsWith("@componentExt ")) {
				String[] parts = line.substring("@componentExt ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					ensureComponents(spec);
					spec.components.extensions.put(parts[0], parsePayloadTag(parts[1]));
				}
			}
		}
	}

	/**
	 * Ensures {@code spec.components} is initialized.
	 *
	 * @param spec
	 *            The target {@link Arazzo} specification.
	 */
	private static void ensureComponents(Arazzo spec) {
		if (spec.components == null) {
			spec.components = new ArazzoComponents();
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
			} else if (line.startsWith("@workflowInputsRef ")) {
				Reference r = new Reference();
				r.ref = line.substring("@workflowInputsRef ".length()).trim();
				wf.inputs = ReferenceOr.ofRef(r);
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
			} else if (line.startsWith("@workflowParamJson ")) {
				JSONObject obj = new JSONObject(line.substring("@workflowParamJson ".length()).trim());
				if (wf.parameters == null) {
					wf.parameters = new ArrayList<>();
				}
				wf.parameters.add(obj.has("reference")
						? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
						: ReusableOr.ofItem(Parse.parseParameter(obj)));
			} else if (line.startsWith("@workflowSuccessAction ")) {
				JSONObject obj = new JSONObject(line.substring("@workflowSuccessAction ".length()).trim());
				if (wf.successActions == null) {
					wf.successActions = new ArrayList<>();
				}
				wf.successActions.add(obj.has("reference")
						? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
						: ReusableOr.ofItem(Parse.parseSuccessAction(obj)));
			} else if (line.startsWith("@workflowFailureAction ")) {
				JSONObject obj = new JSONObject(line.substring("@workflowFailureAction ".length()).trim());
				if (wf.failureActions == null) {
					wf.failureActions = new ArrayList<>();
				}
				wf.failureActions.add(obj.has("reference")
						? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
						: ReusableOr.ofItem(Parse.parseFailureAction(obj)));
			} else if (line.startsWith("@workflowExt ")) {
				String[] parts = line.substring("@workflowExt ".length()).trim().split("\\s+", 2);
				if (parts.length == 2) {
					wf.extensions.put(parts[0], parsePayloadTag(parts[1]));
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
			} else if (line.startsWith("@stepDescription ")) {
				String[] parts = line.substring("@stepDescription ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).description = parts[1];
				}
			} else if (line.startsWith("@stepOperationPath ")) {
				String[] parts = line.substring("@stepOperationPath ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).operationPath = parts[1];
				}
			} else if (line.startsWith("@stepWorkflowId ")) {
				String[] parts = line.substring("@stepWorkflowId ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).workflowId = parts[1];
				}
			} else if (line.startsWith("@stepChannelPath ")) {
				String[] parts = line.substring("@stepChannelPath ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).channelPath = parts[1];
				}
			} else if (line.startsWith("@stepTimeout ")) {
				String[] parts = line.substring("@stepTimeout ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).timeout = Integer.parseInt(parts[1]);
				}
			} else if (line.startsWith("@stepCorrelationId ")) {
				String[] parts = line.substring("@stepCorrelationId ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).correlationId = parts[1];
				}
			} else if (line.startsWith("@stepAction ")) {
				String[] parts = line.substring("@stepAction ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).action = StepActionType.fromString(parts[1]);
				}
			} else if (line.startsWith("@stepDependsOn ")) {
				String[] parts = line.substring("@stepDependsOn ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.dependsOn == null) {
						s.dependsOn = new ArrayList<>();
					}
					s.dependsOn.add(parts[1]);
				}
			} else if (line.startsWith("@stepExt ")) {
				String[] parts = line.substring("@stepExt ".length()).trim().split("\\s+", 3);
				if (parts.length == 3 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).extensions.put(parts[1], parsePayloadTag(parts[2]));
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
			} else if (line.startsWith("@stepParamJson ")) {
				String[] parts = line.substring("@stepParamJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.parameters == null) {
						s.parameters = new ArrayList<>();
					}
					JSONObject obj = new JSONObject(parts[1]);
					s.parameters.add(obj.has("reference")
							? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
							: ReusableOr.ofItem(Parse.parseParameter(obj)));
				}
			} else if (line.startsWith("@stepRequestBodyJson ")) {
				String[] parts = line.substring("@stepRequestBodyJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					stepMap.get(parts[0]).requestBody = Parse.parseRequestBody(new JSONObject(parts[1]));
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
			} else if (line.startsWith("@stepReplacementJson ")) {
				String[] parts = line.substring("@stepReplacementJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.requestBody == null) {
						s.requestBody = new ArazzoRequestBody();
					}
					if (s.requestBody.replacements == null) {
						s.requestBody.replacements = new ArrayList<>();
					}
					s.requestBody.replacements.add(Parse.parsePayloadReplacement(new JSONObject(parts[1])));
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
			} else if (line.startsWith("@stepCriterionJson ")) {
				String[] parts = line.substring("@stepCriterionJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.successCriteria == null) {
						s.successCriteria = new ArrayList<>();
					}
					s.successCriteria.add(Parse.parseCriterion(new JSONObject(parts[1])));
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
			} else if (line.startsWith("@stepOnSuccessJson ")) {
				String[] parts = line.substring("@stepOnSuccessJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.onSuccess == null) {
						s.onSuccess = new ArrayList<>();
					}
					JSONObject obj = new JSONObject(parts[1]);
					s.onSuccess.add(obj.has("reference")
							? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
							: ReusableOr.ofItem(Parse.parseSuccessAction(obj)));
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
			} else if (line.startsWith("@stepOnFailureJson ")) {
				String[] parts = line.substring("@stepOnFailureJson ".length()).trim().split("\\s+", 2);
				if (parts.length == 2 && stepMap.containsKey(parts[0])) {
					Step s = stepMap.get(parts[0]);
					if (s.onFailure == null) {
						s.onFailure = new ArrayList<>();
					}
					JSONObject obj = new JSONObject(parts[1]);
					s.onFailure.add(obj.has("reference")
							? ReusableOr.ofReusable(Parse.parseReusableObject(obj))
							: ReusableOr.ofItem(Parse.parseFailureAction(obj)));
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
	public static PayloadValue parsePayloadTag(String token) {
		if (token.startsWith("num:")) {
			String numStr = token.substring(4);
			if (numStr.contains(".")) {
				return PayloadValue.ofNumber(Double.parseDouble(numStr));
			}
			return PayloadValue.ofNumber(Integer.parseInt(numStr));
		}
		if (token.startsWith("json:")) {
			Object raw = new JSONTokener(token.substring(5)).nextValue();
			return Parse.parsePayloadValue(raw);
		}
		if (token.startsWith("str:")) {
			return PayloadValue.ofString(token.substring(4));
		}
		return PayloadValue.ofString(token);
	}

	/**
	 * Parses an output tag ({@code selector}, {@code selectorJson}, or
	 * {@code expr}) into an {@link OutputValue}.
	 *
	 * @param kind
	 *            The output kind ({@code "selector"}, {@code "selectorJson"}, or
	 *            {@code "expr"}).
	 * @param val
	 *            The selector string, JSON selector object, or runtime expression.
	 * @return The parsed {@link OutputValue}.
	 */
	public static OutputValue parseOutputTag(String kind, String val) {
		if ("selectorJson".equals(kind)) {
			return OutputValue.ofSelector(Parse.parseSelector(new JSONObject(val)));
		}
		if ("selector".equals(kind)) {
			Selector sel = new Selector();
			sel.selector = val;
			return OutputValue.ofSelector(sel);
		}
		return OutputValue.ofExpression(val);
	}
}
