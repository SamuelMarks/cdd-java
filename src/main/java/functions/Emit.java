package functions;

import openapi.OpenAPI;
import openapi.Operation;
import openapi.Parameter;
import openapi.PathItem;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;

/**
 * Emits functions to language source preserving lexical layout.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Java code for standalone functions.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		boolean hasPaths = model != null && model.paths != null && model.paths.pathItems != null
				&& !model.paths.pathItems.isEmpty();
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			CompilationUnit cu = common.AstMergeUtils.parseLexicalPreserving(existingSource);
			if (hasPaths) {
				java.util.List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
				if (!classes.isEmpty()) {
					populateHelperMethods(classes.get(0), model);
				}
			}
			return LexicalPreservingPrinter.print(cu);
		} else {
			if (hasPaths) {
				CompilationUnit cu = new CompilationUnit();
				ClassOrInterfaceDeclaration cls = cu.addClass("HelperFunctions").setModifier(Modifier.Keyword.PUBLIC,
						true);
				cls.setJavadocComment("Helper Functions Generated.");
				populateHelperMethods(cls, model);
				return "// Helper Functions Generated\n" + cu.toString();
			}
			return "// Helper Functions Generated\n";
		}
	}

	/**
	 * Populates a helper class with typed operation methods and Javadoc metadata.
	 *
	 * @param cls
	 *            The target {@link ClassOrInterfaceDeclaration}.
	 * @param model
	 *            The {@link OpenAPI} model.
	 */
	private static void populateHelperMethods(ClassOrInterfaceDeclaration cls, OpenAPI model) {
		for (Map.Entry<String, PathItem> entry : model.paths.pathItems.entrySet()) {
			String path = entry.getKey();
			PathItem pi = entry.getValue();
			for (Map.Entry<openapi.HttpVerb, Operation> opEntry : pi.getOperationsMap().entrySet()) {
				emitOpMethod(cls, opEntry.getKey().name(), path, opEntry.getValue());
			}
		}
	}

	/**
	 * Emits a single helper method for an OpenAPI {@link Operation} if non-null and
	 * not already present.
	 *
	 * @param cls
	 *            The target {@link ClassOrInterfaceDeclaration}.
	 * @param verb
	 *            The HTTP method verb.
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation}, or {@code null}.
	 */
	private static void emitOpMethod(ClassOrInterfaceDeclaration cls, String verb, String path, Operation op) {
		String methodName = (op.operationId != null && !op.operationId.isEmpty())
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (verb.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		if (common.AstMergeUtils.hasMethod(cls, methodName)) {
			return;
		}
		MethodDeclaration md = cls.addMethod(methodName, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		md.setType("String");
		StringBuilder doc = new StringBuilder();
		if (op.description != null && !op.description.isEmpty()) {
			doc.append(op.description.replace('\n', ' ')).append("\n");
		}
		doc.append("@httpMethod ").append(verb).append("\n");
		doc.append("@httpPath ").append(path).append("\n");
		if (op.parameters != null) {
			for (openapi.ReferenceOr<Parameter> po : op.parameters) {
				if (po != null) {
					Parameter p = po.value;
					if (p.name != null) {
						String safeName = p.name.replaceAll("[^a-zA-Z0-9_]", "");
						if (!safeName.isEmpty()) {
							md.addParameter("String", safeName);
							String inLoc = p.in != null ? p.in : "query";
							doc.append("@httpParam ").append(inLoc).append(" ").append(p.name).append("\n");
						}
					}
				}
			}
		}
		if (op.requestBody != null) {
			md.addParameter("String", "requestBody");
			doc.append("@httpRequestBody true\n");
		}
		md.setJavadocComment(doc.toString());
		md.setBody(StaticJavaParser.parseBlock("{ return \"" + verb + " " + path + "\"; }"));
	}
}
