package orm;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Parses JPA/Hibernate entities to OpenAPI representation.
 */
public class Parse {
    /** Default constructor. */
    public Parse() {}

    /**
     * Parses Java source files into an OpenAPI model.
     * @param sourceCode The Java source.
     * @return The parsed OpenAPI object.
     */
    public static OpenAPI parse(String sourceCode) {
        OpenAPI api = new OpenAPI();
        api.openapi = "3.2.0";
        api.components = new openapi.Components();
        api.components.schemas = new HashMap<>();

        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (classDecl.isInterface() || !classDecl.getAnnotationByName("Entity").isPresent()) {
                    continue;
                }
                
                String className = classDecl.getNameAsString();
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                
                // Add table name as extension if available
                classDecl.getAnnotationByName("Table").ifPresent(ann -> {
                    if (ann instanceof NormalAnnotationExpr) {
                        for (MemberValuePair mvp : ((NormalAnnotationExpr) ann).getPairs()) {
                            if (mvp.getNameAsString().equals("name")) {
                                schema.put("x-table-name", mvp.getValue().toString().replace("\"", ""));
                            }
                        }
                    }
                });

                Map<String, Object> properties = new HashMap<>();
                for (FieldDeclaration fieldDecl : classDecl.getFields()) {
                    for (VariableDeclarator varDecl : fieldDecl.getVariables()) {
                        Type type = varDecl.getType();
                        String name = varDecl.getNameAsString();
                        
                        // Override name if @Column(name="...") is present
                        for (AnnotationExpr ann : fieldDecl.getAnnotations()) {
                            if (ann.getNameAsString().equals("Column")) {
                                if (ann instanceof NormalAnnotationExpr) {
                                    for (MemberValuePair mvp : ((NormalAnnotationExpr) ann).getPairs()) {
                                        if (mvp.getNameAsString().equals("name")) {
                                            name = mvp.getValue().toString().replace("\"", "");
                                        }
                                    }
                                }
                            }
                        }

                        Map<String, Object> propSchema = new HashMap<>();
                        resolveType(type, propSchema);
                        
                        if (fieldDecl.getAnnotationByName("Id").isPresent()) {
                            propSchema.put("x-primary-key", true);
                        }
                        
                        properties.put(name, propSchema);
                    }
                }
                schema.put("properties", properties);
                api.components.schemas.put(className, schema);
            }
        } catch (Exception e) {
            // Ignore unparseable blocks
        }
        return api;
    }

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