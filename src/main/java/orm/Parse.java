package orm;

import openapi.OpenAPI;
import openapi.Schema;
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
                Schema schema = new Schema();
                schema.type = "object";
                
                classDecl.getAnnotationByName("Table").ifPresent(ann -> {
                    if (ann instanceof NormalAnnotationExpr) {
                        for (MemberValuePair mvp : ((NormalAnnotationExpr) ann).getPairs()) {
                            if (mvp.getNameAsString().equals("name")) {
                                schema.addExtension("x-table-name", mvp.getValue().toString().replace("\"", ""));
                            }
                        }
                    }
                });

                Map<String, Object> properties = new HashMap<>();
                for (FieldDeclaration fieldDecl : classDecl.getFields()) {
                    for (VariableDeclarator varDecl : fieldDecl.getVariables()) {
                        Type type = varDecl.getType();
                        String name = varDecl.getNameAsString();
                        
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

                        Schema propSchema = new Schema();
                        resolveType(type, propSchema);
                        
                        if (fieldDecl.getAnnotationByName("Id").isPresent()) {
                            propSchema.addExtension("x-primary-key", true);
                        }
                        
                        properties.put(name, propSchema);
                    }
                }
                schema.properties = properties;
                api.components.schemas.put(className, schema);
            }
        } catch (Exception e) {
        }
        return api;
    }

    /**
     * Generated JavaDoc.
     * @param type param doc
     * @param propSchema param doc
     */
    private static void resolveType(Type type, Schema propSchema) {
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
                if (name.equals("Float")) propSchema.format = "float";
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
        } else {
            propSchema.type = "object";
        }
    }
}
