package classes;

import openapi.OpenAPI;
import openapi.Schema;
import openapi.ExternalDocumentation;
import openapi.XML;
import openapi.Discriminator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Map;
import java.util.List;

/**
 * Emits DTOs to language source while preserving whitespace and comments.
 */
public class Emit {
	/** Default constructor. */
	public Emit() {
	}

	/**
	 * Emits Java code for schemas.
	 *
	 * @param model
	 *            The OpenAPI model.
	 * @param existingSource
	 *            Existing Java code to preserve formatting, or null if new.
	 * @return Generated Java source.
	 */
	public static String emit(OpenAPI model, String existingSource) {
		CompilationUnit cu;
		boolean isNew = false;
		if (existingSource != null && !existingSource.trim().isEmpty()) {
			cu = StaticJavaParser.parse(existingSource);
			LexicalPreservingPrinter.setup(cu);
		} else {
			cu = new CompilationUnit();
			isNew = true;
			cu.addImport("com.fasterxml.jackson.annotation.JsonInclude");
			cu.addImport("com.fasterxml.jackson.annotation.JsonProperty");
			cu.addImport("com.fasterxml.jackson.annotation.JsonTypeInfo");
			cu.addImport("com.fasterxml.jackson.annotation.JsonSubTypes");
			cu.addImport("com.fasterxml.jackson.annotation.JsonValue");
			cu.addImport("java.util.List");
			cu.addImport("java.util.Map");
			cu.addImport("java.net.http.HttpClient");
			cu.addImport("java.net.http.HttpRequest");
			cu.addImport("java.net.http.HttpResponse");
			cu.addImport("java.net.URI");
		}

		String title = (model.info != null && model.info.title != null)
				? model.info.title.replaceAll("[^a-zA-Z0-9]", "")
				: "Api";
		if (title.isEmpty())
			title = "Api";
		String clientClass = title + "Client";

		ClassOrInterfaceDeclaration clientDecl = cu.getClassByName(clientClass).orElse(null);
		if (clientDecl == null) {
			clientDecl = cu.addClass(clientClass);
			clientDecl.setModifier(Modifier.Keyword.PUBLIC, false);
			clientDecl.addField("String", "baseUrl", Modifier.Keyword.PRIVATE);
			clientDecl.addField("HttpClient", "httpClient", Modifier.Keyword.PRIVATE);

			clientDecl.addConstructor(Modifier.Keyword.PUBLIC).addParameter("String", "baseUrl")
					.setBody(StaticJavaParser
							.parseBlock("{ this.baseUrl = baseUrl; this.httpClient = HttpClient.newHttpClient(); }"));

			if (model.paths != null && model.paths.pathItems != null) {
				for (Map.Entry<String, openapi.PathItem> entry : model.paths.pathItems.entrySet()) {
					String path = entry.getKey();
					openapi.PathItem pi = entry.getValue();
					if (pi.get != null)
						emitClientMethod(clientDecl, "GET", path, pi.get);
					if (pi.post != null)
						emitClientMethod(clientDecl, "POST", path, pi.post);
					if (pi.put != null)
						emitClientMethod(clientDecl, "PUT", path, pi.put);
					if (pi.delete != null)
						emitClientMethod(clientDecl, "DELETE", path, pi.delete);
				}
			}
		}

		if (model.components != null && model.components.schemas != null) {
			for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
				String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
				if (className.equals("Emit") || className.equals("Parse")) {
					continue;
				}
				emitClass(cu, className, entry.getValue(), model);
			}
		}

		if (isNew) {
			return cu.toString();
		} else {
			return LexicalPreservingPrinter.print(cu);
		}
	}

	private static void emitClientMethod(ClassOrInterfaceDeclaration classDecl, String method, String path,
			openapi.Operation op) {
		String methodName = op.operationId != null
				? op.operationId.replaceAll("[^a-zA-Z0-9_]", "")
				: (method.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
		if (methodName.isEmpty())
			methodName = "method";

		com.github.javaparser.ast.body.MethodDeclaration md = classDecl.addMethod(methodName, Modifier.Keyword.PUBLIC);
		md.setType("HttpResponse<String>");
		md.addThrownException(StaticJavaParser.parseClassOrInterfaceType("Exception"));

		String resolvedPath = path;
		if (op.parameters != null) {
			int pIdx = 0;
			for (Object po : op.parameters) {
				openapi.Parameter p = (openapi.Parameter) po;
				String pName = p.name != null ? p.name.replaceAll("[^a-zA-Z0-9_]", "") : ("p" + pIdx);
				if (pName.isEmpty())
					pName = "param";
				md.addParameter("String", pName);
				if ("path".equals(p.in)) {
					resolvedPath = resolvedPath.replace("{" + p.name + "}", "\" + " + pName + " + \"");
				} else if ("query".equals(p.in)) {
					if (!resolvedPath.contains("?")) {
						resolvedPath += "?" + p.name + "=\" + " + pName + " + \"";
					} else {
						resolvedPath += "&" + p.name + "=\" + " + pName + " + \"";
					}
				}
				pIdx++;
			}
		}

		StringBuilder body = new StringBuilder();
		body.append("{\n");
		body.append("  HttpRequest.Builder builder = HttpRequest.newBuilder()\n");
		body.append("      .uri(URI.create(this.baseUrl + \"").append(resolvedPath).append("\"));\n");
		body.append("  builder.header(\"Accept\", \"application/json\");\n");

		boolean hasBody = (op.requestBody != null);
		if (hasBody) {
			md.addParameter("String", "requestBody");
			body.append("  builder.header(\"Content-Type\", \"application/json\");\n");
			body.append("  builder.method(\"").append(method)
					.append("\", HttpRequest.BodyPublishers.ofString(requestBody));\n");
		} else {
			body.append("  builder.method(\"").append(method).append("\", HttpRequest.BodyPublishers.noBody());\n");
		}
		body.append("  return this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());\n");
		body.append("}\n");

		md.setBody(StaticJavaParser.parseBlock(body.toString()));
	}

	/**
	 * Generated JavaDoc.
	 *
	 * @param cu
	 *            param doc
	 * @param className
	 *            param doc
	 * @param schemaMap
	 *            param doc
	 * @param model
	 *            param doc
	 */
	private static void emitClass(CompilationUnit cu, String className, Schema schemaMap, OpenAPI model) {
		if (schemaMap.enumValues != null) {
			EnumDeclaration enumDecl = cu.getEnumByName(className).orElse(null);
			if (enumDecl == null) {
				enumDecl = cu.addEnum(className);
				enumDecl.setModifier(Modifier.Keyword.PUBLIC, false);
			} else {
				enumDecl.getEntries().clear();
			}
			List<Object> enumValues = schemaMap.enumValues;
			for (Object val : enumValues) {
				String valStr = String.valueOf(val);
				String safeName = valStr.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
				if (safeName.isEmpty() || Character.isDigit(safeName.charAt(0)))
					safeName = "_" + safeName;
				enumDecl.addEnumConstant(safeName);
			}
			return;
		}

		ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
		if (classDecl == null) {
			classDecl = cu.addClass(className);
			classDecl.setModifier(Modifier.Keyword.PUBLIC, false);
		}

		StringBuilder classDoc = new StringBuilder();
		if (schemaMap.description != null && !schemaMap.description.isEmpty()) {
			classDoc.append(schemaMap.description);
		}
		if (schemaMap.xml != null) {
			XML xmlMap = schemaMap.xml;
			if (xmlMap.name != null)
				classDoc.append("\n@xmlName ").append(xmlMap.name);
			if (xmlMap.namespace != null)
				classDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
			if (xmlMap.prefix != null)
				classDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
			if (xmlMap.attribute != null)
				classDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
			if (xmlMap.wrapped != null)
				classDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
		}
		if (schemaMap.externalDocs != null) {
			ExternalDocumentation extDocsMap = schemaMap.externalDocs;
			if (extDocsMap.url != null) {
				classDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
				if (extDocsMap.description != null) {
					classDoc.append(" ").append(extDocsMap.description);
				}
			}
		}
		if (schemaMap.example != null) {
			classDoc.append("\n@schemaExample ").append(schemaMap.example);
		}
		if (schemaMap.discriminator != null) {
			Discriminator discMap = schemaMap.discriminator;
			if (discMap.propertyName != null) {
				classDoc.append("\n@discriminatorProperty ").append(discMap.propertyName);
			}
			if (discMap.mapping != null) {
				for (Map.Entry<String, String> entry : discMap.mapping.entrySet()) {
					classDoc.append("\n@discriminatorMapping ").append(entry.getKey()).append(" ")
							.append(entry.getValue());
				}
			}
			if (discMap.extensions != null && discMap.extensions.containsKey("defaultMapping")) {
				classDoc.append("\n@discriminatorDefault ").append(discMap.extensions.get("defaultMapping"));
			}
		}
		if (classDoc.length() > 0) {
			classDecl.setJavadocComment(classDoc.toString().trim());
		}

		Map<String, Object> properties = schemaMap.properties;
		if (properties != null) {
			for (Map.Entry<String, Object> prop : properties.entrySet()) {
				String propName = prop.getKey();
				String safePropName = propName.replaceAll("[^a-zA-Z0-9_]", "_");
				if (Character.isDigit(safePropName.charAt(0)))
					safePropName = "_" + safePropName;
				if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName)
						|| "class".equals(safePropName)) {
					safePropName += "Value";
				}

				String type = resolveType(prop.getValue(), model);

				if (!classDecl.getFieldByName(safePropName).isPresent()) {
					FieldDeclaration fd = classDecl.addField(type, safePropName, Modifier.Keyword.PUBLIC);
					fd.addAnnotation(StaticJavaParser.parseAnnotation("@JsonProperty(\"" + propName + "\")"));

					if (prop.getValue() instanceof Schema) {
						Schema propMap = (Schema) prop.getValue();
						StringBuilder fieldDoc = new StringBuilder();
						if (propMap.description != null && !propMap.description.isEmpty()) {
							fieldDoc.append(propMap.description);
						}
						if (propMap.externalDocs != null) {
							ExternalDocumentation extDocsMap = propMap.externalDocs;
							if (extDocsMap.url != null) {
								fieldDoc.append("\n@schemaExternalDocs ").append(extDocsMap.url);
								if (extDocsMap.description != null) {
									fieldDoc.append(" ").append(extDocsMap.description);
								}
							}
						}
						if (propMap.example != null) {
							fieldDoc.append("\n@schemaExample ").append(propMap.example);
						}
						if (propMap.xml != null) {
							XML xmlMap = propMap.xml;
							if (xmlMap.name != null)
								fieldDoc.append("\n@xmlName ").append(xmlMap.name);
							if (xmlMap.namespace != null)
								fieldDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
							if (xmlMap.prefix != null)
								fieldDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
							if (xmlMap.attribute != null)
								fieldDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
							if (xmlMap.wrapped != null)
								fieldDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
						}
						if (fieldDoc.length() > 0) {
							fd.setJavadocComment(fieldDoc.toString().trim());
						}
					}
				}
			}
		}
	}

	/**
	 * Generated JavaDoc.
	 *
	 * @param schemaObj
	 *            param doc
	 * @param model
	 *            param doc
	 * @return return doc
	 */
	private static String resolveType(Object schemaObj, OpenAPI model) {
		if (!(schemaObj instanceof Schema))
			return "Object";
		Schema schemaMap = (Schema) schemaObj;

		String schemaType = (String) schemaMap.type;
		if ("string".equals(schemaType)) {
			if ("date-time".equals(schemaMap.format))
				return "java.time.OffsetDateTime";
			if ("date".equals(schemaMap.format))
				return "java.time.LocalDate";
			if ("uuid".equals(schemaMap.format))
				return "java.util.UUID";
			if ("binary".equals(schemaMap.format))
				return "byte[]";
			return "String";
		} else if ("integer".equals(schemaType)) {
			if ("int64".equals(schemaMap.format))
				return "Long";
			return "Integer";
		} else if ("number".equals(schemaType)) {
			if ("float".equals(schemaMap.format))
				return "Float";
			return "Double";
		} else if ("boolean".equals(schemaType)) {
			return "Boolean";
		} else if ("array".equals(schemaType)) {
			String innerType = resolveType(schemaMap.items, model);
			return "List<" + innerType + ">";
		} else if ("object".equals(schemaType) || schemaMap.additionalProperties != null) {
			if (schemaMap.additionalProperties instanceof Schema) {
				String innerType = resolveType(schemaMap.additionalProperties, model);
				return "Map<String, " + innerType + ">";
			}
			return "Map<String, Object>";
		}
		return "Object";
	}
}
