package mocks;

import arazzo.Arazzo;
import arazzo.ArazzoInfo;
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

/**
 * Parses stateful workflow mock classes back into an {@link Arazzo} 1.1.0
 * specification.
 */
public class ArazzoMockParser {
	/**
	 * Default constructor.
	 */
	public ArazzoMockParser() {
	}

	/**
	 * Extracts an {@link Arazzo} 1.1.0 specification from a stateful workflow mock
	 * Java source string.
	 *
	 * @param code
	 *            The Java source code of the workflow mock class.
	 * @return The extracted {@link Arazzo} 1.1.0 specification.
	 */
	public static Arazzo parse(String code) {
		CompilationUnit cu = StaticJavaParser.parse(code);
		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "Extracted Mock Workflow";
		spec.info.version = "1.0.0";
		spec.sourceDescriptions = new ArrayList<>();
		spec.workflows = new ArrayList<>();

		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.getJavadocComment().isPresent()) {
				arazzo.ArazzoJavadocCodec.parseSpecJavadoc(cls.getJavadocComment().get().getContent(), spec);
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
			wf.workflowId = mName.startsWith("mock_") ? mName.substring(5) : mName;
			wf.steps = new ArrayList<>();
			if (m.getJavadocComment().isPresent()) {
				arazzo.ArazzoJavadocCodec.parseWorkflowJavadoc(m.getJavadocComment().get().getContent(), wf);
			}
			if (wf.steps.isEmpty()) {
				for (MethodCallExpr call : m.findAll(MethodCallExpr.class)) {
					if ("put".equals(call.getNameAsString()) && call.getScope().isPresent()
							&& "responses".equals(call.getScope().get().toString())) {
						Step s = new Step();
						s.stepId = call.getArgument(0).asStringLiteralExpr().asString();
						s.operationId = s.stepId;
						wf.steps.add(s);
					}
				}
			}
			spec.workflows.add(wf);
		}
		return spec;
	}
}
