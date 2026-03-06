package orm;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.Map;

/**
 * Emits ORM entities using Hibernate/JPA annotations.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits Java code for JPA entities.
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
            cu.addImport("jakarta.persistence.Entity");
            cu.addImport("jakarta.persistence.Table");
            cu.addImport("jakarta.persistence.Column");
            cu.addImport("jakarta.persistence.Id");
            cu.addImport("jakarta.persistence.GeneratedValue");
            cu.addImport("jakarta.persistence.GenerationType");
            cu.addImport("jakarta.persistence.OneToMany");
            cu.addImport("jakarta.persistence.ManyToOne");
            cu.addImport("java.util.List");
            cu.addImport("java.util.Map");
        }

        for (Map.Entry<String, Object> entry : model.components.schemas.entrySet()) {
            String className = entry.getKey().replaceAll("[^a-zA-Z0-9_]", "");
            if (className.equals("Emit") || className.equals("Parse")) {
                continue;
            }

            if (entry.getValue() instanceof Map) {
                Map<String, Object> schemaMap = (Map<String, Object>) entry.getValue();
                if (!schemaMap.containsKey("enum")) {
                    emitEntity(cu, className, schemaMap, model);
                }
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
     * @param cu param doc
     * @param className param doc
     * @param schemaMap param doc
     * @param model param doc
     */
    private static void emitEntity(CompilationUnit cu, String className, Map<String, Object> schemaMap, OpenAPI model) {
        ClassOrInterfaceDeclaration classDecl = cu.getClassByName(className).orElse(null);
        if (classDecl == null) {
            classDecl = cu.addClass(className);
        }

        if (!classDecl.getAnnotationByName("Entity").isPresent()) {
            classDecl.addAnnotation("Entity");
        }
        if (!classDecl.getAnnotationByName("Table").isPresent()) {
            NormalAnnotationExpr tableAnn = new NormalAnnotationExpr();
            tableAnn.setName("Table");
            tableAnn.addPair("name", "\"" + className.toLowerCase() + "s\"");
            classDecl.addAnnotation(tableAnn);
        }

        Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
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
                    
                    if (propName.equalsIgnoreCase("id")) {
                        fd.addAnnotation("Id");
                        NormalAnnotationExpr genAnn = new NormalAnnotationExpr();
                        genAnn.setName("GeneratedValue");
                        genAnn.addPair("strategy", new NameExpr("GenerationType.IDENTITY"));
                        fd.addAnnotation(genAnn);
                    } else if (type.startsWith("List<")) {
                        fd.addAnnotation("OneToMany");
                    } else {
                        NormalAnnotationExpr colAnn = new NormalAnnotationExpr();
                        colAnn.setName("Column");
                        colAnn.addPair("name", "\"" + propName + "\"");
                        fd.addAnnotation(colAnn);
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