package arazzo;

import common.PayloadValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.ReferenceOr;
import openapi.Schema;
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
		if (wf.parameters != null) {
			for (ReusableOr<ArazzoParameter> wp : wf.parameters) {
				ArazzoParameter resolved = ArazzoResolver.resolveParameter(wp, spec);
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
						ArazzoParameter resolved = ArazzoResolver.resolveParameter(sp, spec);
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
						SuccessAction sa = ArazzoResolver.resolveSuccessAction(osa, spec);
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
						FailureAction fa = ArazzoResolver.resolveFailureAction(ofa, spec);
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
	 * Parses an output tag ({@code selector} or {@code expr}) into an
	 * {@link OutputValue}.
	 *
	 * @param kind
	 *            The output kind ({@code "selector"} or {@code "expr"}).
	 * @param val
	 *            The selector string or runtime expression.
	 * @return The parsed {@link OutputValue}.
	 */
	public static OutputValue parseOutputTag(String kind, String val) {
		if ("selector".equals(kind)) {
			Selector sel = new Selector();
			sel.selector = val;
			return OutputValue.ofSelector(sel);
		}
		return OutputValue.ofExpression(val);
	}
}
