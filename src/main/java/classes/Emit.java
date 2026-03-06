package classes;

import openapi.OpenAPI;
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

        for (Map.Entry<String, Object> entry : model.components.schemas.entrySet()) {
            String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
            
            // Avoid modifying internal classes during sync!
            if (className.equals("Emit") || className.equals("Parse")) {
                continue;
            }

            if (entry.getValue() instanceof Map) {
                Map<String, Object> schemaMap = (Map<String, Object>) entry.getValue();
                emitClass(cu, className, schemaMap, model);
            }
        }

        if (isNew) {
            return cu.toString();
        } else {
            return LexicalPreservingPrinter.print(cu);
        }
    }

    /**
     * Generated JavaDoc.
     */
    /**
     * Generated JavaDoc.
     * @param cu param doc
     * @param className param doc
     * @param schemaMap param doc
     * @param model param doc
     */
    private static void emitClass(CompilationUnit cu, String className, Map<String, Object> schemaMap, OpenAPI model) {
        if (schemaMap.containsKey("enum")) {
            EnumDeclaration enumDecl = cu.getEnumByName(className).orElse(null);
            if (enumDecl == null) {
                enumDecl = cu.addEnum(className);
            } else {
                enumDecl.getEntries().clear();
            }
            List<Object> enumValues = (List<Object>) schemaMap.get("enum");
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
        String classDescription = (String) schemaMap.get("description");
        if (classDescription != null && !classDescription.isEmpty()) {
            classDoc.append(classDescription);
        }
        if (schemaMap.containsKey("xml") && schemaMap.get("xml") instanceof Map) {
            Map<String, Object> xmlMap = (Map<String, Object>) schemaMap.get("xml");
            if (xmlMap.containsKey("name")) classDoc.append("\n@xmlName ").append(xmlMap.get("name"));
            if (xmlMap.containsKey("namespace")) classDoc.append("\n@xmlNamespace ").append(xmlMap.get("namespace"));
            if (xmlMap.containsKey("prefix")) classDoc.append("\n@xmlPrefix ").append(xmlMap.get("prefix"));
            if (xmlMap.containsKey("attribute")) classDoc.append("\n@xmlAttribute ").append(xmlMap.get("attribute"));
            if (xmlMap.containsKey("wrapped")) classDoc.append("\n@xmlWrapped ").append(xmlMap.get("wrapped"));
        }
        if (schemaMap.containsKey("externalDocs") && schemaMap.get("externalDocs") instanceof Map) {
            Map<String, Object> extDocsMap = (Map<String, Object>) schemaMap.get("externalDocs");
            if (extDocsMap.containsKey("url")) {
                classDoc.append("\n@schemaExternalDocs ").append(extDocsMap.get("url"));
                if (extDocsMap.containsKey("description")) {
                    classDoc.append(" ").append(extDocsMap.get("description"));
                }
            }
        }
        if (schemaMap.containsKey("example")) {
            classDoc.append("\n@schemaExample ").append(schemaMap.get("example"));
        }
        if (schemaMap.containsKey("discriminator") && schemaMap.get("discriminator") instanceof Map) {
            Map<String, Object> discMap = (Map<String, Object>) schemaMap.get("discriminator");
            if (discMap.containsKey("propertyName")) {
                classDoc.append("\n@discriminatorProperty ").append(discMap.get("propertyName"));
            }
            if (discMap.containsKey("mapping") && discMap.get("mapping") instanceof Map) {
                Map<String, String> mapping = (Map<String, String>) discMap.get("mapping");
                for (Map.Entry<String, String> entry : mapping.entrySet()) {
                    classDoc.append("\n@discriminatorMapping ").append(entry.getKey()).append(" ").append(entry.getValue());
                }
            }
            if (discMap.containsKey("defaultMapping")) {
                classDoc.append("\n@discriminatorDefault ").append(discMap.get("defaultMapping"));
            }
        }
        if (classDoc.length() > 0) {
            classDecl.setJavadocComment(classDoc.toString().trim());
        }

        Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> prop : properties.entrySet()) {
                String propName = prop.getKey();
                // Map restricted java keywords like 'enum', 'default', 'const'
                String safePropName = propName.replaceAll("[^a-zA-Z0-9_]", "_");
                if (Character.isDigit(safePropName.charAt(0))) safePropName = "_" + safePropName;
                if ("enum".equals(safePropName) || "default".equals(safePropName) || "const".equals(safePropName) || "class".equals(safePropName)) {
                    safePropName += "Value";
                }
                
                String type = resolveType(prop.getValue(), model);

                if (!classDecl.getFieldByName(safePropName).isPresent()) {
                    FieldDeclaration fd = classDecl.addField(type, safePropName, Modifier.Keyword.PUBLIC);
                    fd.addAnnotation(StaticJavaParser.parseAnnotation("@JsonProperty(\"" + propName + "\")"));
                    
                    if (prop.getValue() instanceof Map) {
                        Map<String, Object> propMap = (Map<String, Object>) prop.getValue();
                        StringBuilder fieldDoc = new StringBuilder();
                        String propDescription = (String) propMap.get("description");
                        if (propDescription != null && !propDescription.isEmpty()) {
                            fieldDoc.append(propDescription);
                        }
                        if (propMap.containsKey("externalDocs") && propMap.get("externalDocs") instanceof Map) {
                            Map<String, Object> extDocsMap = (Map<String, Object>) propMap.get("externalDocs");
                            if (extDocsMap.containsKey("url")) {
                                fieldDoc.append("\n@schemaExternalDocs ").append(extDocsMap.get("url"));
                                if (extDocsMap.containsKey("description")) {
                                    fieldDoc.append(" ").append(extDocsMap.get("description"));
                                }
                            }
                        }
                        if (propMap.containsKey("example")) {
                            fieldDoc.append("\n@schemaExample ").append(propMap.get("example"));
                        }
                        if (propMap.containsKey("xml") && propMap.get("xml") instanceof Map) {
                            Map<String, Object> xmlMap = (Map<String, Object>) propMap.get("xml");
                            if (xmlMap.containsKey("name")) fieldDoc.append("\n@xmlName ").append(xmlMap.get("name"));
                            if (xmlMap.containsKey("namespace")) fieldDoc.append("\n@xmlNamespace ").append(xmlMap.get("namespace"));
                            if (xmlMap.containsKey("prefix")) fieldDoc.append("\n@xmlPrefix ").append(xmlMap.get("prefix"));
                            if (xmlMap.containsKey("attribute")) fieldDoc.append("\n@xmlAttribute ").append(xmlMap.get("attribute"));
                            if (xmlMap.containsKey("wrapped")) fieldDoc.append("\n@xmlWrapped ").append(xmlMap.get("wrapped"));
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
        if (!(schemaObj instanceof Map)) return "Object";
        Map<String, Object> schemaMap = (Map<String, Object>) schemaObj;
        if (schemaMap.containsKey("$ref")) {
            String ref = (String) schemaMap.get("$ref");
            return ref.substring(ref.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9_]", "");
        }
        String schemaType = (String) schemaMap.get("type");
        if ("string".equals(schemaType)) {
            String format = (String) schemaMap.get("format");
            if ("date-time".equals(format)) return "java.time.OffsetDateTime";
            if ("date".equals(format)) return "java.time.LocalDate";
            if ("uuid".equals(format)) return "java.util.UUID";
            if ("binary".equals(format)) return "byte[]";
            return "String";
        } else if ("integer".equals(schemaType)) {
            if ("int64".equals(schemaMap.get("format"))) return "Long";
            return "Integer";
        } else if ("number".equals(schemaType)) {
            if ("float".equals(schemaMap.get("format"))) return "Float";
            return "Double";
        } else if ("boolean".equals(schemaType)) {
            return "Boolean";
        } else if ("array".equals(schemaType)) {
            Object itemsObj = schemaMap.get("items");
            String innerType = resolveType(itemsObj, model);
            return "List<" + innerType + ">";
        } else if ("object".equals(schemaType) || schemaMap.containsKey("additionalProperties")) {
            Object addProps = schemaMap.get("additionalProperties");
            if (addProps instanceof Map) {
                String innerType = resolveType(addProps, model);
                return "Map<String, " + innerType + ">";
            }
            return "Map<String, Object>";
        }
        return "Object";
    }
}
