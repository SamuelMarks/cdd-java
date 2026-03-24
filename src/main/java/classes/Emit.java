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
    public Emit() {}

    /**
     * Emits Java code for schemas.
     * @param model The OpenAPI model.
     * @param existingSource Existing Java code to preserve formatting, or null if new.
     * @return Generated Java source.
     */
    public static String emit(OpenAPI model, String existingSource) {
        if (model.components == null || model.components.schemas == null) {
            return existingSource != null ? existingSource : "";
        }

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
        }

        for (Map.Entry<String, Schema> entry : model.components.schemas.entrySet()) {
            String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
            
            if (className.equals("Emit") || className.equals("Parse")) {
                continue;
            }

            emitClass(cu, className, entry.getValue(), model);
        }

        if (isNew) {
            return cu.toString();
        } else {
            return LexicalPreservingPrinter.print(cu);
        }
    }

    /**
     * Generated JavaDoc.
     * @param cu param doc
     * @param className param doc
     * @param schemaMap param doc
     * @param model param doc
     */
    private static void emitClass(CompilationUnit cu, String className, Schema schemaMap, OpenAPI model) {
        if (schemaMap.enumValues != null) {
            EnumDeclaration enumDecl = cu.getEnumByName(className).orElse(null);
            if (enumDecl == null) {
                enumDecl = cu.addEnum(className);
            } else {
                enumDecl.getEntries().clear();
            }
            List<Object> enumValues = schemaMap.enumValues;
            for (Object val : enumValues) {
                String valStr = String.valueOf(val);
                String safeName = valStr.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
                if (safeName.isEmpty() || Character.isDigit(safeName.charAt(0))) safeName = "_" + safeName;
                enumDecl.addEnumConstant(safeName);
            }
            return;
        }

        ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
        if (classDecl == null) {
            classDecl = cu.addClass(className);
        }

        StringBuilder classDoc = new StringBuilder();
        if (schemaMap.description != null && !schemaMap.description.isEmpty()) {
            classDoc.append(schemaMap.description);
        }
        if (schemaMap.xml != null) {
            XML xmlMap = schemaMap.xml;
            if (xmlMap.name != null) classDoc.append("\n@xmlName ").append(xmlMap.name);
            if (xmlMap.namespace != null) classDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
            if (xmlMap.prefix != null) classDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
            if (xmlMap.attribute != null) classDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
            if (xmlMap.wrapped != null) classDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
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
                    classDoc.append("\n@discriminatorMapping ").append(entry.getKey()).append(" ").append(entry.getValue());
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
                if (Character.isDigit(safePropName.charAt(0))) safePropName = "_" + safePropName;
                if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName) || "class".equals(safePropName)) {
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
                            if (xmlMap.name != null) fieldDoc.append("\n@xmlName ").append(xmlMap.name);
                            if (xmlMap.namespace != null) fieldDoc.append("\n@xmlNamespace ").append(xmlMap.namespace);
                            if (xmlMap.prefix != null) fieldDoc.append("\n@xmlPrefix ").append(xmlMap.prefix);
                            if (xmlMap.attribute != null) fieldDoc.append("\n@xmlAttribute ").append(xmlMap.attribute);
                            if (xmlMap.wrapped != null) fieldDoc.append("\n@xmlWrapped ").append(xmlMap.wrapped);
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
     * @param schemaObj param doc
     * @param model param doc
     * @return return doc
     */
    private static String resolveType(Object schemaObj, OpenAPI model) {
        if (!(schemaObj instanceof Schema)) return "Object";
        Schema schemaMap = (Schema) schemaObj;
        if (schemaMap.$ref != null) {
            return schemaMap.$ref.substring(schemaMap.$ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
        }
        String schemaType = (String) schemaMap.type;
        if ("string".equals(schemaType)) {
            if ("date-time".equals(schemaMap.format)) return "java.time.OffsetDateTime";
            if ("date".equals(schemaMap.format)) return "java.time.LocalDate";
            if ("uuid".equals(schemaMap.format)) return "java.util.UUID";
            if ("binary".equals(schemaMap.format)) return "byte[]";
            return "String";
        } else if ("integer".equals(schemaType)) {
            if ("int64".equals(schemaMap.format)) return "Long";
            return "Integer";
        } else if ("number".equals(schemaType)) {
            if ("float".equals(schemaMap.format)) return "Float";
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
