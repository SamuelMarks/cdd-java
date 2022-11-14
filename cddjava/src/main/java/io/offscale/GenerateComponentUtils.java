package io.offscale;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateComponentUtils {

    /**
     * @param joComponent   JSONObject for a component in the OpenAPI spec.
     * @param componentName name of the component to generate.
     * @param parentClass   the parent class of a given component.
     * @return a Schema object containing the code of the component.
     */
//    public static Schema generateComponent(JSONObject joComponent, String componentName, ClassOrInterfaceDeclaration parentClass) {
//        final Schema schema = parseSchema(joComponent);
//
//        // schema type is object which means it will have properties.
//        if (schema.type().equals("object")) {
//            final ClassOrInterfaceDeclaration newClass = new ClassOrInterfaceDeclaration();
//            final JSONObject joProperties = joComponent.getJSONObject("properties");
//            final List<String> properties = Lists.newArrayList(joProperties.keys());
//            newClass.setName(Utils.capitalizeFirstLetter(componentName));
//            properties.forEach(property -> {
//                Schema propertyType = generateComponent(joProperties.getJSONObject(property), property, newClass);
//                FieldDeclaration field = newClass.addField(propertyType.type(), property);
//                field.setJavadocComment("Type of " + propertyType.strictType());
//            });
//
//            if (parentClass != null) {
//                parentClass.addMember(newClass);
//            }
//            return new Schema(newClass.getNameAsString(), schema.name(), newClass.toString());
//        } else if (schema.type().equals("array")) {
//            if (parentClass == null) {
//                final ClassOrInterfaceDeclaration newClass = new ClassOrInterfaceDeclaration();
//                final String arrayType = generateComponent(joComponent.getJSONObject("items"), "ArrayType", newClass).type();
//                newClass.setName(Utils.capitalizeFirstLetter(componentName));
//                newClass.addField(arrayType + "[]", componentName + "Array");
//                return new Schema(newClass.getNameAsString(), schema.name(), newClass.toString());
//            }
//
//            final String arrayType = generateComponent(joComponent.getJSONObject("items"), "ArrayType", parentClass).type();
//            return new Schema(arrayType + "[]", schema.name(), schema.code());
//        } else {
//            return schema;
//        }
//    }
//
//    /**
//     * @param joSchema which is essentially a type
//     * @return a Parameter with type information
//     */
//    private static Schema parseSchema(JSONObject joSchema) {
//        if (joSchema.has("$ref")) {
//            return new Schema(parseSchemaRef(joSchema.getString("$ref")));
//        } else if (joSchema.has("format")) {
//            return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("format")), joSchema.getString("format"));
//        } else if (!joSchema.has("type")) {
//            return new Schema();
//        }
//
//        return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("type"));
//    }
//
//    /**
//     * Uses regex to parse out the component name in the reference.
//     *
//     * @param ref of a schema, maps to a component
//     * @return the component name
//     */
//    private static String parseSchemaRef(String ref) {
//        final Pattern pattern = Pattern.compile("#/components/schemas/(\\w+)");
//        final Matcher matcher = pattern.matcher(ref);
//        matcher.find();
//        return matcher.group(1);
//    }
}
