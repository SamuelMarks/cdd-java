package classes;

import openapi.OpenAPI;
import openapi.Schema;
import openapi.XML;
import openapi.Discriminator;
import openapi.ExternalDocumentation;
import openapi.Reference;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Parses DTOs from language source to OpenAPI representation using JavaParser.
 */
public class Parse {
	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses Java source files into an OpenAPI model.
	 *
	 * @param sourceCode
	 *            The Java source.
	 * @return The parsed OpenAPI object (specifically populating
	 *         components.schemas).
	 */
	public static OpenAPI parse(String sourceCode) {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.2.0";
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		try {
			CompilationUnit cu = StaticJavaParser.parse(sourceCode);

			// Handle enums
			for (EnumDeclaration enumDecl : cu.findAll(EnumDeclaration.class)) {
				String enumName = enumDecl.getNameAsString();
				Schema schema = new Schema();
				schema.type = "string";

				Optional<JavadocComment> javadoc = enumDecl.getJavadocComment();
				if (javadoc.isPresent()) {
					String cleanDoc = javadoc.get().parse().getDescription().toText().trim();
					if (!cleanDoc.isEmpty()) {
						schema.description = cleanDoc;
					}
				}

				List<Object> enumValues = new ArrayList<>();
				for (EnumConstantDeclaration constDecl : enumDecl.getEntries()) {
					String val = constDecl.getNameAsString();
					for (AnnotationExpr ann : constDecl.getAnnotations()) {
						if (ann.getNameAsString().equals("JsonProperty")) {
							if (ann instanceof SingleMemberAnnotationExpr) {
								val = ((SingleMemberAnnotationExpr) ann).getMemberValue().toString().replace("\"", "");
							}
						}
					}
					enumValues.add(val);
				}
				schema.enumValues = enumValues;
				api.components.schemas.put(enumName, schema);
			}

			for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
				if (classDecl.isInterface()) {
					continue;
				}

				String className = classDecl.getNameAsString();
				if (className.endsWith("Client") || className.endsWith("MockServer")
						|| className.endsWith("IntegrationTest")) {
					continue;
				}

				Schema schema = new Schema();
				schema.type = "object";

				Optional<JavadocComment> classJavadoc = classDecl.getJavadocComment();
				if (classJavadoc.isPresent()) {
					Javadoc parsedDoc = classJavadoc.get().parse();
					String cleanDoc = parsedDoc.getDescription().toText().trim();
					if (!cleanDoc.isEmpty()) {
						schema.description = cleanDoc;
					}
					XML xmlObj = new XML();
					Discriminator discriminatorObj = new Discriminator();
					Map<String, String> mappingMap = new HashMap<>();
					boolean hasXml = false;
					boolean hasDisc = false;
					for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
						String tName = tag.getTagName();
						String tContent = tag.getContent().toText().trim();
						if ("xmlName".equals(tName)) {
							xmlObj.name = tContent;
							hasXml = true;
						} else if ("xmlNamespace".equals(tName)) {
							xmlObj.namespace = tContent;
							hasXml = true;
						} else if ("xmlPrefix".equals(tName)) {
							xmlObj.prefix = tContent;
							hasXml = true;
						} else if ("xmlAttribute".equals(tName)) {
							xmlObj.attribute = Boolean.parseBoolean(tContent);
							hasXml = true;
						} else if ("xmlWrapped".equals(tName)) {
							xmlObj.wrapped = Boolean.parseBoolean(tContent);
							hasXml = true;
						} else if ("discriminatorProperty".equals(tName)) {
							discriminatorObj.propertyName = tContent;
							hasDisc = true;
						} else if ("discriminatorMapping".equals(tName)) {
							int spaceIdx = tContent.indexOf(' ');
							if (spaceIdx > 0) {
								mappingMap.put(tContent.substring(0, spaceIdx),
										tContent.substring(spaceIdx + 1).trim());
								hasDisc = true;
							}
						} else if ("discriminatorDefault".equals(tName)) {
							discriminatorObj.addExtension("defaultMapping", tContent);
							hasDisc = true;
						} else if ("schemaExample".equals(tName))
							schema.example = tContent;
						else if ("schemaExternalDocs".equals(tName)) {
							ExternalDocumentation extDocs = new ExternalDocumentation();
							int spaceIdx = tContent.indexOf(' ');
							if (spaceIdx > 0) {
								extDocs.url = tContent.substring(0, spaceIdx);
								extDocs.description = tContent.substring(spaceIdx + 1).trim();
							} else {
								extDocs.url = tContent;
							}
							schema.externalDocs = extDocs;
						}
					}
					if (hasXml)
						schema.xml = xmlObj;

					if (!mappingMap.isEmpty())
						discriminatorObj.mapping = mappingMap;
					if (hasDisc)
						schema.discriminator = discriminatorObj;
				}

				// Discriminator
				for (AnnotationExpr ann : classDecl.getAnnotations()) {
					if (ann.getNameAsString().equals("JsonTypeInfo")) {
						if (ann instanceof NormalAnnotationExpr) {
							NormalAnnotationExpr nae = (NormalAnnotationExpr) ann;
							Discriminator discriminator = schema.discriminator != null
									? schema.discriminator
									: new Discriminator();
							boolean added = false;
							for (MemberValuePair mvp : nae.getPairs()) {
								if (mvp.getNameAsString().equals("property")) {
									discriminator.propertyName = mvp.getValue().toString().replace("\"", "");
									added = true;
								}
							}
							if (added) {
								schema.discriminator = discriminator;
							}
						}
					}
				}

				// Inheritance
				if (!classDecl.getExtendedTypes().isEmpty()) {
					List<Object> allOf = new ArrayList<>();
					for (ClassOrInterfaceType extType : classDecl.getExtendedTypes()) {
						Schema refSchema = new Schema();
						refSchema.$ref = "#/components/schemas/" + extType.getNameAsString();
						allOf.add(refSchema);
					}
					if (!allOf.isEmpty()) {
						schema.allOf = allOf;
					}
				}

				Map<String, Object> properties = new HashMap<>();
				for (FieldDeclaration fieldDecl : classDecl.getFields()) {
					if (fieldDecl.isPublic() || !fieldDecl.isPrivate()) {
						for (VariableDeclarator varDecl : fieldDecl.getVariables()) {
							Type type = varDecl.getType();
							String name = varDecl.getNameAsString();

							for (AnnotationExpr ann : fieldDecl.getAnnotations()) {
								if (ann.getNameAsString().equals("JsonProperty")) {
									if (ann instanceof SingleMemberAnnotationExpr) {
										name = ((SingleMemberAnnotationExpr) ann).getMemberValue().toString()
												.replace("\"", "");
									}
								}
							}

							Schema propSchema = new Schema();
							Optional<JavadocComment> fieldJavadoc = fieldDecl.getJavadocComment();
							if (fieldJavadoc.isPresent()) {
								Javadoc parsedDoc = fieldJavadoc.get().parse();
								String cleanPropDoc = parsedDoc.getDescription().toText().trim();
								if (!cleanPropDoc.isEmpty()) {
									propSchema.description = cleanPropDoc;
								}
								XML xmlObj = new XML();
								boolean hasXml = false;
								for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
									String tName = tag.getTagName();
									String tContent = tag.getContent().toText().trim();
									if ("xmlName".equals(tName)) {
										xmlObj.name = tContent;
										hasXml = true;
									} else if ("xmlNamespace".equals(tName)) {
										xmlObj.namespace = tContent;
										hasXml = true;
									} else if ("xmlPrefix".equals(tName)) {
										xmlObj.prefix = tContent;
										hasXml = true;
									} else if ("xmlAttribute".equals(tName)) {
										xmlObj.attribute = Boolean.parseBoolean(tContent);
										hasXml = true;
									} else if ("xmlWrapped".equals(tName)) {
										xmlObj.wrapped = Boolean.parseBoolean(tContent);
										hasXml = true;
									} else if ("schemaExample".equals(tName))
										propSchema.example = tContent;
									else if ("schemaExternalDocs".equals(tName)) {
										ExternalDocumentation extDocs = new ExternalDocumentation();
										int spaceIdx = tContent.indexOf(' ');
										if (spaceIdx > 0) {
											extDocs.url = tContent.substring(0, spaceIdx);
											extDocs.description = tContent.substring(spaceIdx + 1).trim();
										} else {
											extDocs.url = tContent;
										}
										propSchema.externalDocs = extDocs;
									}
								}
								if (hasXml)
									propSchema.xml = xmlObj;
							}

							resolveType(type, propSchema);

							properties.put(name, propSchema);
						}
					}
				}
				schema.properties = properties;
				api.components.schemas.put(className, schema);
			}
		} catch (Exception e) {
			// Ignore unparseable code blocks or fragments
		}
		return api;
	}

	/**
	 * Generated JavaDoc.
	 *
	 * @param type
	 *            param doc
	 * @param propSchema
	 *            param doc
	 */
	private static void resolveType(Type type, Schema propSchema) {
		String typeName = type.toString();

		if (type.isClassOrInterfaceType()) {
			ClassOrInterfaceType ciType = type.asClassOrInterfaceType();
			String name = ciType.getNameAsString();
			if (name.equals("String")) {
				propSchema.type = "string";
			} else if (name.equals("Integer")) {
				propSchema.type = "integer";
			} else if (name.equals("Long")) {
				propSchema.type = "integer";
				propSchema.format = "int64";
			} else if (name.equals("Double") || name.equals("Float")) {
				propSchema.type = "number";
				if (name.equals("Float"))
					propSchema.format = "float";
			} else if (name.equals("Boolean")) {
				propSchema.type = "boolean";
			} else if (name.equals("UUID")) {
				propSchema.type = "string";
				propSchema.format = "uuid";
			} else if (name.equals("LocalDate")) {
				propSchema.type = "string";
				propSchema.format = "date";
			} else if (name.equals("OffsetDateTime") || name.equals("ZonedDateTime")) {
				propSchema.type = "string";
				propSchema.format = "date-time";
			} else if (name.equals("List") || name.equals("ArrayList") || name.equals("Set")) {
				propSchema.type = "array";
				Schema items = new Schema();
				if (ciType.getTypeArguments().isPresent() && !ciType.getTypeArguments().get().isEmpty()) {
					resolveType(ciType.getTypeArguments().get().get(0), items);
				} else {
					items.type = "string";
				}
				propSchema.items = items;
			} else if (name.equals("Map") || name.equals("HashMap")) {
				propSchema.type = "object";
				if (ciType.getTypeArguments().isPresent() && ciType.getTypeArguments().get().size() > 1) {
					Schema addProps = new Schema();
					resolveType(ciType.getTypeArguments().get().get(1), addProps);
					propSchema.additionalProperties = addProps;
				}
			} else {
				propSchema.$ref = "#/components/schemas/" + name;
			}
		} else if (type.isArrayType()) {
			String elemType = type.asArrayType().getComponentType().toString();
			if (elemType.equals("byte")) {
				propSchema.type = "string";
				propSchema.format = "binary";
			} else {
				propSchema.type = "array";
				Schema items = new Schema();
				resolveType(type.asArrayType().getComponentType(), items);
				propSchema.items = items;
			}
		} else if (type.isPrimitiveType()) {
			String pType = type.asPrimitiveType().toString();
			if (pType.equals("int")) {
				propSchema.type = "integer";
			} else if (pType.equals("long")) {
				propSchema.type = "integer";
				propSchema.format = "int64";
			} else if (pType.equals("double") || pType.equals("float")) {
				propSchema.type = "number";
			} else if (pType.equals("boolean")) {
				propSchema.type = "boolean";
			}
		}
	}
}
