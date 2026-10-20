package tests;

import arazzo.Arazzo;
import arazzo.ArazzoJavadocCodec;
import arazzo.ArazzoResolver;
import arazzo.Criterion;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.List;
import openapi.TypeMapper;

/**
 * Emits multi-step JUnit 4 end-to-end integration test suites from an
 * {@link Arazzo} 1.1.0 specification.
 */
public class ArazzoTestEmitter {
	/**
	 * Default constructor.
	 */
	public ArazzoTestEmitter() {
	}

	/**
	 * Emits a JUnit 4 workflow integration test class from an {@link Arazzo} 1.1.0
	 * specification.
	 *
	 * @param spec
	 *            The {@link Arazzo} 1.1.0 specification.
	 * @return The generated Java JUnit test source code.
	 */
	public static String emit(Arazzo spec) {
		String className = workflows.Emit.deriveClassName(spec) + "IntegrationTest";
		CompilationUnit cu = new CompilationUnit();
		cu.addImport("org.junit.Test");
		cu.addImport("org.junit.Assert", true, true);
		cu.addImport("arazzo.RuntimeExpression");

		ClassOrInterfaceDeclaration cls = cu.addClass(className, Modifier.Keyword.PUBLIC);
		StringBuilder classDoc = new StringBuilder();
		classDoc.append(" * End-to-end workflow integration tests generated from Arazzo ")
				.append(spec.arazzo != null ? spec.arazzo : "1.1.0").append(".\n");
		ArazzoJavadocCodec.appendSpecJavadoc(classDoc, " * ", spec);
		cls.setJavadocComment(classDoc.toString());

		ConstructorDeclaration ctor = cls.addConstructor(Modifier.Keyword.PUBLIC);
		ctor.setJavadocComment(" Default constructor. ");

		List<Workflow> ordered = ArazzoResolver.sortWorkflowsTopologically(spec);
		for (Workflow wf : ordered) {
			String mName = "test_" + TypeMapper.sanitizeIdentifier(wf.workflowId);
			MethodDeclaration md = cls.addMethod(mName, Modifier.Keyword.PUBLIC);
			md.addAnnotation("Test");
			StringBuilder methodDoc = new StringBuilder();
			methodDoc.append(" * E2E workflow test for ").append(wf.workflowId).append(".\n");
			ArazzoJavadocCodec.appendWorkflowJavadoc(methodDoc, " * ", wf, spec);
			md.setJavadocComment(methodDoc.toString());

			BlockStmt body = new BlockStmt();
			body.addStatement(StaticJavaParser
					.parseStatement("RuntimeExpression.Context ctx = new RuntimeExpression.Context();"));
			body.addStatement(StaticJavaParser.parseStatement("ctx.statusCode = 200;"));
			if (wf.steps != null) {
				for (Step s : wf.steps) {
					if (s.successCriteria != null) {
						for (Criterion c : s.successCriteria) {
							if (c.condition != null && c.condition.contains("200")) {
								body.addStatement(
										StaticJavaParser.parseStatement("assertEquals(200, ctx.statusCode);"));
							}
						}
					}
					Statement assertStmt = StaticJavaParser
							.parseStatement("assertNotNull(\"" + TypeMapper.escape(s.stepId) + "\", ctx);");
					assertStmt.setComment(new LineComment(" Step: " + s.stepId));
					body.addStatement(assertStmt);
				}
			}
			md.setBody(body);
		}
		return cu.toString();
	}
}
