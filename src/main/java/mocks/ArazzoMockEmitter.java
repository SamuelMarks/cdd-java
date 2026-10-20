package mocks;

import arazzo.Arazzo;
import arazzo.ArazzoJavadocCodec;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.LinkedHashMap;
import java.util.Map;
import openapi.TypeMapper;

/**
 * Emits stateful workflow mock servers and simulators from an {@link Arazzo}
 * 1.1.0 specification.
 */
public class ArazzoMockEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoMockEmitter() {
	}

	/**
	 * Emits a stateful workflow mock class from an {@link Arazzo} 1.1.0
	 * specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java source code string.
	 */
	public static String emit(Arazzo spec) {
		String className = workflows.Emit.deriveClassName(spec) + "Mock";
		CompilationUnit cu = new CompilationUnit();
		cu.addImport("java.util.LinkedHashMap");
		cu.addImport("java.util.Map");

		ClassOrInterfaceDeclaration cls = cu.addClass(className, Modifier.Keyword.PUBLIC);
		StringBuilder classDoc = new StringBuilder();
		classDoc.append(" * Stateful workflow mock simulator generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		ArazzoJavadocCodec.appendSpecJavadoc(classDoc, " * ", spec);
		cls.setJavadocComment(classDoc.toString());

		FieldDeclaration stepAttemptsField = cls.addFieldWithInitializer("Map<String, Integer>", "stepAttempts",
				StaticJavaParser.parseExpression("new LinkedHashMap<>()"), Modifier.Keyword.PUBLIC,
				Modifier.Keyword.FINAL);
		stepAttemptsField.setJavadocComment(" Tracks per-step invocation counts for retry simulation. ");

		ConstructorDeclaration ctor = cls.addConstructor(Modifier.Keyword.PUBLIC);
		ctor.setJavadocComment(" Default constructor. ");

		if (spec.workflows != null) {
			for (Workflow wf : spec.workflows) {
				String mName = "mock_" + TypeMapper.sanitizeIdentifier(wf.workflowId);
				MethodDeclaration md = cls.addMethod(mName, Modifier.Keyword.PUBLIC);
				md.setType("Map<String, String>");
				StringBuilder methodDoc = new StringBuilder();
				methodDoc.append(" * Simulates stateful execution of workflow ").append(wf.workflowId).append(".\n");
				ArazzoJavadocCodec.appendWorkflowJavadoc(methodDoc, " * ", wf, spec);
				methodDoc.append(" * @return Map of simulated step responses.\n");
				md.setJavadocComment(methodDoc.toString());

				BlockStmt body = new BlockStmt();
				body.addStatement(
						StaticJavaParser.parseStatement("Map<String, String> responses = new LinkedHashMap<>();"));
				if (wf.steps != null) {
					for (Step s : wf.steps) {
						String stepEsc = TypeMapper.escape(s.stepId);
						body.addStatement(StaticJavaParser.parseStatement("stepAttempts.put(\"" + stepEsc
								+ "\", stepAttempts.getOrDefault(\"" + stepEsc + "\", 0) + 1);"));
						body.addStatement(StaticJavaParser.parseStatement("responses.put(\"" + stepEsc
								+ "\", \"{\\\"status\\\":\\\"ok\\\",\\\"stepId\\\":\\\"" + stepEsc + "\\\"}\");"));
					}
				}
				body.addStatement(StaticJavaParser.parseStatement("return responses;"));
				md.setBody(body);
			}
		}
		return cu.toString();
	}

	/**
	 * Emits a map of modular workflow mock files.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return Map of relative file paths to Java source strings.
	 */
	public static Map<String, String> emitModular(Arazzo spec) {
		Map<String, String> files = new LinkedHashMap<>();
		String className = workflows.Emit.deriveClassName(spec) + "Mock";
		files.put("mocks/" + className + ".java", emit(spec));
		return files;
	}
}
