package classes;

import openapi.OpenAPI;
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

/**Parses DTOs from language source to OpenAPI representation using JavaParser.*/
public class Parse {
    /**
     * Default constructor.
     */
    public Parse() {}

    /**
     * Parses Java source files into an OpenAPI model.
     * @param sourceCode The Java source.
     * @return The parsed OpenAPI object (specifically populating components.schemas).
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
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "string");
                
                Optional<JavadocComment> javadoc = enumDecl.getJavadocComment();
                if (javadoc.isPresent()) {
                    String cleanDoc = javadoc.get().parse().getDescription().toText().trim();
                    if (!cleanDoc.isEmpty()) {
                        schema.put("description", cleanDoc);
                    }
                }
                
                List<String> enumValues = new ArrayList<>();
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
                schema.put("enum", enumValues);
                api.components.schemas.put(enumName, schema);
            }
            
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (classDecl.isInterface()) {
                    continue;
                }
                
                String className = classDecl.getNameAsString();
                if (className.endsWith("Client") || className.endsWith("MockServer") || className.endsWith("IntegrationTest")) {
                    continue;
                }

                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                
                Optional<JavadocComment> classJavadoc = classDecl.getJavadocComment();
                if (classJavadoc.isPresent()) {
                    Javadoc parsedDoc = classJavadoc.get().parse();
                    String cleanDoc = parsedDoc.getDescription().toText().trim();
                    if (!cleanDoc.isEmpty()) {
                        schema.put("description", cleanDoc);
                    }
                    Map<String, Object> xmlMap = new HashMap<>();
                    Map<String, Object> discriminatorMap = new HashMap<>();
                    Map<String, String> mappingMap = new HashMap<>();
                    for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
                        String tName = tag.getTagName();
                        String tContent = tag.getContent().toText().trim();
                        if ("xmlName".equals(tName)) xmlMap.put("name", tContent);
                        else if ("xmlNamespace".equals(tName)) xmlMap.put("namespace", tContent);
                        else if ("xmlPrefix".equals(tName)) xmlMap.put("prefix", tContent);
                        else if ("xmlAttribute".equals(tName)) xmlMap.put("attribute", Boolean.parseBoolean(tContent));
                        else if ("xmlWrapped".equals(tName)) xmlMap.put("wrapped", Boolean.parseBoolean(tContent));
                        else if ("discriminatorProperty".equals(tName)) discriminatorMap.put("propertyName", tContent);
                        else if ("discriminatorMapping".equals(tName)) {
                            int spaceIdx = tContent.indexOf(' ');
                            if (spaceIdx > 0) {
                                mappingMap.put(tContent.substring(0, spaceIdx), tContent.substring(spaceIdx + 1).trim());
                            }
                        }
                        else if ("discriminatorDefault".equals(tName)) discriminatorMap.put("defaultMapping", tContent);
                        else if ("schemaExample".equals(tName)) schema.put("example", tContent);
                        else if ("schemaExternalDocs".equals(tName)) {
                            Map<String, Object> extDocs = new HashMap<>();
                            int spaceIdx = tContent.indexOf(' ');
                            if (spaceIdx > 0) {
                                extDocs.put("url", tContent.substring(0, spaceIdx));
                                extDocs.put("description", tContent.substring(spaceIdx + 1).trim());
                            } else {
                                extDocs.put("url", tContent);
                            }
                            schema.put("externalDocs", extDocs);
                        }
                    }
                    if (!xmlMap.isEmpty()) schema.put("xml", xmlMap);
                    
                    if (!mappingMap.isEmpty()) discriminatorMap.put("mapping", mappingMap);
                    if (!discriminatorMap.isEmpty()) schema.put("discriminator", discriminatorMap);
                }
                
                // Discriminator
                for (AnnotationExpr ann : classDecl.getAnnotations()) {
                    if (ann.getNameAsString().equals("JsonTypeInfo")) {
                        if (ann instanceof NormalAnnotationExpr) {
                            NormalAnnotationExpr nae = (NormalAnnotationExpr) ann;
                            Map<String, Object> discriminator = (Map<String, Object>) schema.getOrDefault("discriminator", new HashMap<>());
                            for (MemberValuePair mvp : nae.getPairs()) {
                                if (mvp.getNameAsString().equals("property")) {
                                    discriminator.put("propertyName", mvp.getValue().toString().replace("\"", ""));
                                }
                            }
                            if (!discriminator.isEmpty()) {
                                schema.put("discriminator", discriminator);
                            }
                        }
                    }
                }
                
                // Inheritance
                if (!classDecl.getExtendedTypes().isEmpty()) {
                    List<Object> allOf = new ArrayList<>();
                    for (ClassOrInterfaceType extType : classDecl.getExtendedTypes()) {
                        Map<String, Object> ref = new HashMap<>();
                        ref.put("$ref", "#/components/schemas/" + extType.getNameAsString());
                        allOf.add(ref);
                    }
                    if (!allOf.isEmpty()) {
                        schema.put("allOf", allOf);
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
                                        name = ((SingleMemberAnnotationExpr) ann).getMemberValue().toString().replace("\"", "");
                                    }
                                }
                            }

                            Map<String, Object> propSchema = new HashMap<>();
                            Optional<JavadocComment> fieldJavadoc = fieldDecl.getJavadocComment();
                            if (fieldJavadoc.isPresent()) {
                                Javadoc parsedDoc = fieldJavadoc.get().parse();
                                String cleanPropDoc = parsedDoc.getDescription().toText().trim();
                                if (!cleanPropDoc.isEmpty()) {
                                    propSchema.put("description", cleanPropDoc);
                                }
                                Map<String, Object> xmlMap = new HashMap<>();
                                for (JavadocBlockTag tag : parsedDoc.getBlockTags()) {
                                    String tName = tag.getTagName();
                                    String tContent = tag.getContent().toText().trim();
                                    if ("xmlName".equals(tName)) xmlMap.put("name", tContent);
                                    else if ("xmlNamespace".equals(tName)) xmlMap.put("namespace", tContent);
                                    else if ("xmlPrefix".equals(tName)) xmlMap.put("prefix", tContent);
                                    else if ("xmlAttribute".equals(tName)) xmlMap.put("attribute", Boolean.parseBoolean(tContent));
                                    else if ("xmlWrapped".equals(tName)) xmlMap.put("wrapped", Boolean.parseBoolean(tContent));
                                    else if ("schemaExample".equals(tName)) propSchema.put("example", tContent);
                                    else if ("schemaExternalDocs".equals(tName)) {
                                        Map<String, Object> extDocs = new HashMap<>();
                                        int spaceIdx = tContent.indexOf(' ');
                                        if (spaceIdx > 0) {
                                            extDocs.put("url", tContent.substring(0, spaceIdx));
                                            extDocs.put("description", tContent.substring(spaceIdx + 1).trim());
                                        } else {
                                            extDocs.put("url", tContent);
                                        }
                                        propSchema.put("externalDocs", extDocs);
                                    }
                                }
                                if (!xmlMap.isEmpty()) propSchema.put("xml", xmlMap);
                            }
                            
                            resolveType(type, propSchema);

                            properties.put(name, propSchema);
                        }
                    }
                }
                schema.put("properties", properties);
                api.components.schemas.put(className, schema);
            }
        } catch (Exception e) {
            // Ignore unparseable code blocks or fragments
        }
        return api;
    }
    
    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     * @param type param doc
     * @param propSchema param doc
     */
    private static void resolveType(Type type, Map<String, Object> propSchema) {
        String typeName = type.toString();
        
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType ciType = type.asClassOrInterfaceType();
            String name = ciType.getNameAsString();
            if (name.equals("String")) {
                propSchema.put("type", "string");
            } else if (name.equals("Integer")) {
                propSchema.put("type", "integer");
            } else if (name.equals("Long")) {
                propSchema.put("type", "integer");
                propSchema.put("format", "int64");
            } else if (name.equals("Double") || name.equals("Float")) {
                propSchema.put("type", "number");
                if (name.equals("Float")) propSchema.put("format", "float");
            } else if (name.equals("Boolean")) {
                propSchema.put("type", "boolean");
            } else if (name.equals("UUID")) {
                propSchema.put("type", "string");
                propSchema.put("format", "uuid");
            } else if (name.equals("LocalDate")) {
                propSchema.put("type", "string");
                propSchema.put("format", "date");
            } else if (name.equals("OffsetDateTime") || name.equals("ZonedDateTime")) {
                propSchema.put("type", "string");
                propSchema.put("format", "date-time");
            } else if (name.equals("List") || name.equals("ArrayList") || name.equals("Set")) {
                propSchema.put("type", "array");
                Map<String, Object> items = new HashMap<>();
                if (ciType.getTypeArguments().isPresent() && !ciType.getTypeArguments().get().isEmpty()) {
                    resolveType(ciType.getTypeArguments().get().get(0), items);
                } else {
                    items.put("type", "string");
                }
                propSchema.put("items", items);
            } else if (name.equals("Map") || name.equals("HashMap")) {
                propSchema.put("type", "object");
                if (ciType.getTypeArguments().isPresent() && ciType.getTypeArguments().get().size() > 1) {
                    Map<String, Object> addProps = new HashMap<>();
                    resolveType(ciType.getTypeArguments().get().get(1), addProps);
                    propSchema.put("additionalProperties", addProps);
                }
            } else {
                propSchema.put("$ref", "#/components/schemas/" + name);
            }
        } else if (type.isArrayType()) {
            String elemType = type.asArrayType().getComponentType().toString();
            if (elemType.equals("byte")) {
                propSchema.put("type", "string");
                propSchema.put("format", "binary");
            } else {
                propSchema.put("type", "array");
                Map<String, Object> items = new HashMap<>();
                resolveType(type.asArrayType().getComponentType(), items);
                propSchema.put("items", items);
            }
        } else if (type.isPrimitiveType()) {
            String pType = type.asPrimitiveType().toString();
            if (pType.equals("int")) {
                propSchema.put("type", "integer");
            } else if (pType.equals("long")) {
                propSchema.put("type", "integer");
                propSchema.put("format", "int64");
            } else if (pType.equals("double") || pType.equals("float")) {
                propSchema.put("type", "number");
            } else if (pType.equals("boolean")) {
                propSchema.put("type", "boolean");
            }
        } else {
            propSchema.put("type", "object");
        }
    }
}

