package io.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schema2 {
    private final String type;
    private final String strictType;
    private final ImmutableMap<String, Schema2> properties;

    private final Schema2 arrayOfType;

    public Schema2(String type, String strictType) {
        this.type = type;
        this.strictType = strictType;
        this.properties = null;
        this.arrayOfType = null;
    }

    public Schema2(String type, String strictType, Map<String, Schema2> properties) {
        this.type = type;
        this.strictType = strictType;
        this.properties = ImmutableMap.copyOf(properties);
        this.arrayOfType = null;
    }

    public Schema2(String type, String strictType, Schema2 arrayOfType) {
        this.type = type;
        this.strictType = strictType;
        this.properties = null;
        this.arrayOfType = arrayOfType;
    }


    public String type() {
        return this.type;
    }

    public String strictType() {
        return this.strictType;
    }

    public ImmutableMap<String, Schema2> properties() {
        return properties;
    }

    private String toCodeAux(Schema2 schema, ClassOrInterfaceDeclaration parentClass) {
        if (schema.isObject()) {
            final ClassOrInterfaceDeclaration newClass = new ClassOrInterfaceDeclaration();
            schema.properties.forEach((name, propSchema) -> {
                toCodeAux(propSchema, newClass);
                newClass.addField(propSchema.type, name).setJavadocComment("Type of " + propSchema.strictType);
            });
            newClass.setName(schema.type);
            if (parentClass != null) {
                parentClass.addMember(newClass);
            }
            return newClass.toString();
        }

        if (schema.isArray()) {
            toCodeAux(schema.arrayOfType, parentClass);
        }

        return schema.type;
    }

    public String toCode() {
        assert this.isObject();
        return toCodeAux(this, null);
    }

    public static Schema2 parseSchema(JSONObject joSchema, HashMap<String, Schema2> schemas, String type) {
        if (joSchema.has("type") && joSchema.get("type").equals("object") || joSchema.has("properties")) {
            assert (!type.isEmpty());
            HashMap<String, Schema2> schemaProperties = new HashMap<>();
            final List<String> propertyKeys = Lists.newArrayList(joSchema.getJSONObject("properties").keys());
            propertyKeys.forEach(key -> {
                        schemaProperties.put(key, parseSchema(
                                joSchema.getJSONObject("properties").getJSONObject(key),
                                schemas,
                                Utils.capitalizeFirstLetter(key)));
                    }
            );

            if (isNullValue(schemaProperties)) {
                return null;
            }

            return new Schema2(type, type, schemaProperties);
        }

        if (joSchema.has("type") && joSchema.get("type").equals("array")) {
            Schema2 itemsSchema = parseSchema(joSchema.getJSONObject("items"), schemas, Utils.capitalizeFirstLetter(type));
            if (itemsSchema == null) {
                return null;
            }
            return new Schema2(itemsSchema.type + "[]", "array", itemsSchema);
        }

        if (joSchema.has("type") && joSchema.has("format")) {
            return new Schema2(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("format"));
        }

        if (joSchema.has("type") && !joSchema.has("format")) {
            return new Schema2(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("type"));
        }

        assert (joSchema.has("$ref"));
        if (schemas.containsKey(parseSchemaRef(joSchema.getString("$ref")))) {
            return schemas.get(parseSchemaRef(joSchema.getString("$ref")));
        }

        return null;
    }

    private boolean isObject() {
        return this.properties != null;
    }

    private boolean isArray() {
        return this.arrayOfType != null;
    }

    private static boolean isNullValue(HashMap<String, Schema2> map) {
        for (Schema2 value : map.values()) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    private static String parseSchemaRef(String ref) {
        final Pattern pattern = Pattern.compile("#/components/schemas/(\\w+)");
        final Matcher matcher = pattern.matcher(ref);
        matcher.find();
        return matcher.group(1);
    }
}
