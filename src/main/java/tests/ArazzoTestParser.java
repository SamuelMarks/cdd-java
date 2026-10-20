package tests;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
import arazzo.Criterion;
import arazzo.SourceDescription;
import arazzo.SourceType;
import arazzo.Step;
import arazzo.Workflow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parses multi-step JUnit 4 workflow integration test classes back into an
 * {@link Arazzo} 1.1.0 specification.
 */
public class ArazzoTestParser {
	/**
	 * Default constructor.
	 */
	public ArazzoTestParser() {
	}

	/**
	 * Extracts an {@link Arazzo} 1.1.0 specification from a JUnit workflow
	 * integration test source string.
	 *
	 * @param code
	 *            The Java source code of the JUnit workflow test class.
	 * @return The extracted {@link Arazzo} 1.1.0 specification.
	 */
	public static Arazzo parse(String code) {
		CompilationUnit cu = StaticJavaParser.parse(code);
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "Extracted Test Workflow";
		spec.info.version = "1.0.0";
		spec.sourceDescriptions = new ArrayList<>();
		spec.workflows = new ArrayList<>();

		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.getJavadocComment().isPresent()) {
				workflows.Parse.parseSpecJavadoc(cls.getJavadocComment().get().getContent(), spec);
			}
		}

		if (spec.sourceDescriptions.isEmpty()) {
			SourceDescription sd = new SourceDescription();
			sd.name = "default";
			sd.type = SourceType.OPENAPI;
			sd.url = "./openapi.json";
			spec.sourceDescriptions.add(sd);
		}

		for (MethodDeclaration m : cu.findAll(MethodDeclaration.class)) {
			String mName = m.getNameAsString();
			Workflow wf = new Workflow();
			wf.workflowId = mName.startsWith("test_") ? mName.substring(5) : mName;
			wf.steps = new ArrayList<>();
			if (m.getJavadocComment().isPresent()) {
				workflows.Parse.parseWorkflowJavadoc(m.getJavadocComment().get().getContent(), wf);
			}
			if (wf.steps.isEmpty()) {
				for (MethodCallExpr call : m.findAll(MethodCallExpr.class)) {
					if ("assertNotNull".equals(call.getNameAsString())) {
						Step s = new Step();
						s.stepId = call.getArgument(0).asStringLiteralExpr().asString();
						s.operationId = s.stepId;
						Criterion c = new Criterion();
						c.condition = "$statusCode == 200";
						s.successCriteria = new ArrayList<>(Collections.singletonList(c));
						wf.steps.add(s);
					}
				}
			}
			spec.workflows.add(wf);
		}
		return spec;
	}
}
