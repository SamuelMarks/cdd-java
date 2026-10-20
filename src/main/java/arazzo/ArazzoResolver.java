package arazzo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;
import openapi.Schema;

/**
 * Resolves and links Arazzo 1.1.0 {@link SourceDescription} documents,
 * {@code $components.*} reusable references, {@link Step} targets
 * ({@code operationId} and {@code operationPath}), and {@code dependsOn}
 * topological execution order.
 */
public class ArazzoResolver {
	/**
	 * Default constructor.
	 */
	public ArazzoResolver() {
	}

	/**
	 * Holds a resolved OpenAPI operation target for an Arazzo {@link Step}.
	 */
	public static class ResolvedOperation {
		/** The source description name. */
		public final String sourceName;
		/** The HTTP method verb (uppercase, e.g. {@code GET}, {@code POST}). */
		public final String httpMethod;
		/** The URL path template (e.g. {@code /pet/{petId}}). */
		public final String path;
		/** The resolved OpenAPI {@link Operation}. */
		public final Operation operation;

		/**
		 * Constructs a {@code ResolvedOperation}.
		 *
		 * @param sourceName
		 *            The source description name.
		 * @param httpMethod
		 *            The HTTP method verb.
		 * @param path
		 *            The URL path template.
		 * @param operation
		 *            The OpenAPI {@link Operation}.
		 */
		public ResolvedOperation(String sourceName, String httpMethod, String path, Operation operation) {
			this.sourceName = sourceName;
			this.httpMethod = httpMethod;
			this.path = path;
			this.operation = operation;
		}
	}

	/**
	 * Loads all local {@code openapi} {@link SourceDescription} files referenced by
	 * an {@link Arazzo} document relative to {@code baseDir}.
	 *
	 * @param spec
	 *            The {@link Arazzo} specification.
	 * @param baseDir
	 *            The directory against which relative source URLs are resolved.
	 * @return A map of {@code sourceDescription.name} to parsed {@link OpenAPI}
	 *         models.
	 * @throws IOException
	 *             If a referenced local OpenAPI file cannot be parsed.
	 */
	public static Map<String, OpenAPI> loadOpenApiSources(Arazzo spec, File baseDir) throws IOException {
		Map<String, OpenAPI> sources = new LinkedHashMap<>();
		if (spec == null || spec.sourceDescriptions == null) {
			return sources;
		}
		for (SourceDescription sd : spec.sourceDescriptions) {
			if (sd.type == SourceType.OPENAPI && sd.url != null && !sd.url.startsWith("http://")
					&& !sd.url.startsWith("https://")) {
				File f = new File(baseDir, sd.url);
				if (f.isFile()) {
					sources.put(sd.name, openapi.Parse.fromFile(f));
				}
			}
		}
		return sources;
	}

	/**
	 * Resolves a {@link Step}'s {@code operationId} or {@code operationPath}
	 * against a map of loaded {@link OpenAPI} source specifications.
	 *
	 * @param step
	 *            The {@link Step} to resolve.
	 * @param spec
	 *            The enclosing {@link Arazzo} specification.
	 * @param openApiSources
	 *            Map of source description names to {@link OpenAPI} models.
	 * @return The {@link ResolvedOperation}, or {@code null} if not found.
	 */
	public static ResolvedOperation resolveStepOperation(Step step, Arazzo spec, Map<String, OpenAPI> openApiSources) {
		if (step == null || openApiSources == null || openApiSources.isEmpty()) {
			return null;
		}
		if (step.operationId != null) {
			String targetOpId = step.operationId;
			String preferredSource = null;
			if (targetOpId.startsWith("$sourceDescriptions.")) {
				String rest = targetOpId.substring("$sourceDescriptions.".length());
				int dot = rest.indexOf('.');
				preferredSource = rest.substring(0, dot);
				targetOpId = rest.substring(dot + 1);
			}
			for (Map.Entry<String, OpenAPI> entry : openApiSources.entrySet()) {
				if (preferredSource != null && !preferredSource.equals(entry.getKey())) {
					continue;
				}
				ResolvedOperation found = findOperationById(entry.getKey(), entry.getValue(), targetOpId);
				if (found != null) {
					return found;
				}
			}
		}
		if (step.operationPath != null && step.operationPath.contains("#/paths/")) {
			int hashIdx = step.operationPath.indexOf("#/paths/");
			String pointer = step.operationPath.substring(hashIdx + "#/paths/".length());
			int lastSlash = pointer.lastIndexOf('/');
			String rawPath = common.JsonPointerUtils.unescapePointerToken(pointer.substring(0, lastSlash));
			String verb = pointer.substring(lastSlash + 1).toUpperCase();
			for (Map.Entry<String, OpenAPI> entry : openApiSources.entrySet()) {
				OpenAPI api = entry.getValue();
				if (api.paths != null && api.paths.pathItems != null) {
					PathItem pi = api.paths.pathItems.get(rawPath);
					if (pi != null) {
						Operation op = getOperationByVerb(pi, verb);
						if (op != null) {
							return new ResolvedOperation(entry.getKey(), verb, rawPath, op);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Dereferences a {@link ReusableOr} parameter against
	 * {@code spec.components.parameters}, applying any overridden {@code value}.
	 *
	 * @param paramOrRef
	 *            The inline or reusable parameter wrapper.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @return The resolved {@link ArazzoParameter}, or {@code null} if
	 *         unresolvable.
	 */
	public static ArazzoParameter resolveParameter(ReusableOr<ArazzoParameter> paramOrRef, Arazzo spec) {
		if (paramOrRef == null) {
			return null;
		}
		if (paramOrRef.isItem()) {
			return paramOrRef.item;
		}
		ReusableObject r = paramOrRef.reusable;
		if (r != null && r.reference != null && spec != null && spec.components != null
				&& spec.components.parameters != null) {
			String key = r.reference.substring(r.reference.lastIndexOf('.') + 1);
			ArazzoParameter base = spec.components.parameters.get(key);
			if (base != null) {
				ArazzoParameter copy = new ArazzoParameter();
				copy.name = base.name;
				copy.in = base.in;
				copy.value = r.value != null ? r.value : base.value;
				copy.extensions.putAll(base.extensions);
				return copy;
			}
		}
		return null;
	}

	/**
	 * Dereferences a {@link ReusableOr} success action against
	 * {@code spec.components.successActions}.
	 *
	 * @param actionOrRef
	 *            The inline or reusable success action wrapper.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @return The resolved {@link SuccessAction}, or {@code null} if unresolvable.
	 */
	public static SuccessAction resolveSuccessAction(ReusableOr<SuccessAction> actionOrRef, Arazzo spec) {
		if (actionOrRef == null) {
			return null;
		}
		if (actionOrRef.isItem()) {
			return actionOrRef.item;
		}
		ReusableObject r = actionOrRef.reusable;
		if (r != null && r.reference != null && spec != null && spec.components != null
				&& spec.components.successActions != null) {
			String key = r.reference.substring(r.reference.lastIndexOf('.') + 1);
			return spec.components.successActions.get(key);
		}
		return null;
	}

	/**
	 * Dereferences a {@link ReusableOr} failure action against
	 * {@code spec.components.failureActions}.
	 *
	 * @param actionOrRef
	 *            The inline or reusable failure action wrapper.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @return The resolved {@link FailureAction}, or {@code null} if unresolvable.
	 */
	public static FailureAction resolveFailureAction(ReusableOr<FailureAction> actionOrRef, Arazzo spec) {
		if (actionOrRef == null) {
			return null;
		}
		if (actionOrRef.isItem()) {
			return actionOrRef.item;
		}
		ReusableObject r = actionOrRef.reusable;
		if (r != null && r.reference != null && spec != null && spec.components != null
				&& spec.components.failureActions != null) {
			String key = r.reference.substring(r.reference.lastIndexOf('.') + 1);
			return spec.components.failureActions.get(key);
		}
		return null;
	}

	/**
	 * Resolves a workflow's input {@link Schema}, dereferencing
	 * {@code $components.inputs.<name>} or {@code #/components/inputs/<name>} if
	 * referenced.
	 *
	 * @param wf
	 *            The {@link Workflow}.
	 * @param spec
	 *            The root {@link Arazzo} specification.
	 * @return The resolved input {@link Schema}, or {@code null} if none.
	 */
	public static Schema resolveWorkflowInputs(Workflow wf, Arazzo spec) {
		if (wf == null || wf.inputs == null) {
			return null;
		}
		if (wf.inputs.isValue()) {
			return wf.inputs.value;
		}
		String ref = wf.inputs.ref.ref;
		if (spec != null && spec.components != null && spec.components.inputs != null) {
			String key = ref.contains("/")
					? ref.substring(ref.lastIndexOf('/') + 1)
					: ref.substring(ref.lastIndexOf('.') + 1);
			return spec.components.inputs.get(key);
		}
		return null;
	}

	/**
	 * Topologically orders workflows in an {@link Arazzo} specification according
	 * to their {@code dependsOn} declarations, throwing a
	 * {@link openapi.CddException} if a dependency cycle is detected.
	 *
	 * @param spec
	 *            The {@link Arazzo} specification.
	 * @return The topologically sorted list of {@link Workflow} objects.
	 */
	public static List<Workflow> sortWorkflowsTopologically(Arazzo spec) {
		List<Workflow> ordered = new ArrayList<>();
		if (spec == null || spec.workflows == null) {
			return ordered;
		}
		Map<String, Workflow> byId = new LinkedHashMap<>();
		for (Workflow wf : spec.workflows) {
			byId.put(wf.workflowId, wf);
		}
		Set<String> visited = new HashSet<>();
		List<String> visitingStack = new ArrayList<>();
		for (Workflow wf : spec.workflows) {
			visitWorkflow(wf, byId, visited, visitingStack, ordered);
		}
		return ordered;
	}

	/**
	 * Recursive depth-first topological helper for sorting workflows.
	 *
	 * @param wf
	 *            The current {@link Workflow}.
	 * @param byId
	 *            Map of all workflows by {@code workflowId}.
	 * @param visited
	 *            Set of permanently visited workflow IDs.
	 * @param visitingStack
	 *            Active recursion stack of workflow IDs for cycle path reporting.
	 * @param ordered
	 *            Output list of topologically sorted workflows.
	 */
	private static void visitWorkflow(Workflow wf, Map<String, Workflow> byId, Set<String> visited,
			List<String> visitingStack, List<Workflow> ordered) {
		if (visited.contains(wf.workflowId)) {
			return;
		}
		int cycleStart = visitingStack.indexOf(wf.workflowId);
		if (cycleStart != -1) {
			List<String> cyclePath = new ArrayList<>(visitingStack.subList(cycleStart, visitingStack.size()));
			cyclePath.add(wf.workflowId);
			throw new openapi.CddException(openapi.CddException.ErrorCode.VALIDATION_ERROR,
					"Circular workflow dependency detected: " + String.join(" -> ", cyclePath));
		}
		visitingStack.add(wf.workflowId);
		if (wf.dependsOn != null) {
			for (String depId : wf.dependsOn) {
				Workflow dep = byId.get(depId);
				if (dep != null) {
					visitWorkflow(dep, byId, visited, visitingStack, ordered);
				}
			}
		}
		visitingStack.remove(visitingStack.size() - 1);
		visited.add(wf.workflowId);
		ordered.add(wf);
	}

	/**
	 * Searches an {@link OpenAPI} model for an operation matching {@code opId}.
	 *
	 * @param sourceName
	 *            The source description name.
	 * @param api
	 *            The {@link OpenAPI} model.
	 * @param opId
	 *            The target {@code operationId}.
	 * @return The matching {@link ResolvedOperation}, or {@code null}.
	 */
	private static ResolvedOperation findOperationById(String sourceName, OpenAPI api, String opId) {
		if (api.paths == null || api.paths.pathItems == null) {
			return null;
		}
		String[] verbs = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE", "QUERY"};
		for (Map.Entry<String, PathItem> e : api.paths.pathItems.entrySet()) {
			PathItem pi = e.getValue();
			for (String verb : verbs) {
				Operation op = getOperationByVerb(pi, verb);
				if (op != null && opId.equals(op.operationId)) {
					return new ResolvedOperation(sourceName, verb, e.getKey(), op);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the {@link Operation} from a {@link PathItem} for the given HTTP
	 * verb.
	 *
	 * @param pi
	 *            The {@link PathItem}.
	 * @param verb
	 *            The uppercase HTTP verb.
	 * @return The corresponding {@link Operation}, or {@code null}.
	 */
	private static Operation getOperationByVerb(PathItem pi, String verb) {
		return pi.getOperation(verb);
	}
}
