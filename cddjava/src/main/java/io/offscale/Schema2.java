package io.offscale;

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

    public Schema2(String type, String strictType, Map<String,Schema2> properties) {
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

    public static Schema2 parseSchema(JSONObject joSchema, HashMap<String, Schema2> schemas, String type) {
        if (joSchema.has("type") && joSchema.get("type").equals("object")) {
            assert(!type.isEmpty());
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
            return new Schema2("array", "array", parseSchema(joSchema.getJSONObject("items"), schemas, Utils.capitalizeFirstLetter(type)));
        }

        if (joSchema.has("type") && joSchema.has("format")) {
            return new Schema2(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("format"));
        }

        if (joSchema.has("type") && !joSchema.has("format")) {
            return new Schema2(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("type"));
        }

        assert(joSchema.has("$ref"));
        if (schemas.containsKey(parseSchemaRef(joSchema.getString("$ref")))) {
            return schemas.get(parseSchemaRef(joSchema.getString("$ref")));
        }

        return null;
    }

    private static boolean isNullValue(HashMap<String, Schema2> map) {
        for (Schema2 value: map.values()) {
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
