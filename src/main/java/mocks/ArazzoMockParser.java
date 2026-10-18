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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

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
		for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (cls.getJavadocComment().isPresent()) {
				String doc = cls.getJavadocComment().get().getContent();
				for (String rawLine : doc.split("\n")) {
					String line = rawLine.replaceAll("^\\s*\\*\\s*", "").trim();
					if (line.startsWith("@arazzoSpecBase64 ")) {
						String b64 = line.substring("@arazzoSpecBase64 ".length()).trim();
						try {
							String json = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
							return arazzo.Parse.fromString(json);
						} catch (IOException | IllegalArgumentException ignored) {
							// Fallback to structural mock AST extraction below
						}
					}
				}
			}
		}

		Arazzo spec = new Arazzo();
		spec.arazzo = "1.1.0";
		spec.info = new ArazzoInfo();
		spec.info.title = "Extracted Mock Workflow";
		spec.info.version = "1.0.0";
		SourceDescription sd = new SourceDescription();
		sd.name = "default";
		sd.type = SourceType.OPENAPI;
		sd.url = "./openapi.json";
		spec.sourceDescriptions = new ArrayList<>(Collections.singletonList(sd));
		spec.workflows = new ArrayList<>();

		for (MethodDeclaration m : cu.findAll(MethodDeclaration.class)) {
			String mName = m.getNameAsString();
			Workflow wf = new Workflow();
			wf.workflowId = mName.startsWith("mock_") ? mName.substring(5) : mName;
			wf.steps = new ArrayList<>();
			for (MethodCallExpr call : m.findAll(MethodCallExpr.class)) {
				if ("put".equals(call.getNameAsString()) && call.getScope().isPresent()
						&& "responses".equals(call.getScope().get().toString())) {
					Step s = new Step();
					s.stepId = call.getArgument(0).asStringLiteralExpr().asString();
					s.operationId = s.stepId;
					wf.steps.add(s);
				}
			}
			spec.workflows.add(wf);
		}
		return spec;
	}
}
