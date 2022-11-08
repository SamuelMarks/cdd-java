package io.offscale;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Schema {
    private final String type;
    private final String strictType;
    private final String name;
    private final String code;

    public Schema() {
        this.type = "object";
        this.strictType = "object";
        this.name = "";
        this.code = "";
    }

    public Schema(final String type) {
        this.type = type;
        this.strictType = type;
        this.name = "";
        this.code = "";
    }

    public Schema(String type, String strictType) {
        this.type = type;
        this.strictType = strictType;
        this.name = "";
        this.code = "";
    }

    public Schema(String type, String name, String code) {
        this.type = type;
        this.strictType = type;
        this.name = name;
        this.code = code;
    }

    public Schema(Schema schema, String name) {
        this.type = schema.type;
        this.strictType = schema.strictType;
        this.name = name;
        this.code = schema.code;
    }

    public String type() {
        return this.type;
    }

    public String strictType() {
        return this.strictType;
    }

    public String name() {
        return this.name;
    }

    public String code() {
        return this.code;
    }

    /**
     * @param joSchema which is essentially a type
     * @return a Parameter with type information
     */
    public static Schema parseSchema(JSONObject joSchema) {
        if (joSchema.has("$ref")) {
            return new Schema(parseSchemaRef(joSchema.getString("$ref")));
        } else if (joSchema.has("format")) {
            return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("format")), joSchema.getString("format"));
        } else if (!joSchema.has("type")) {
            return new Schema();
        }

        return new Schema(Utils.getOpenAPIToJavaTypes().get(joSchema.get("type")), joSchema.getString("type"));
    }


    /**
     * Uses regex to parse out the component name in the reference.
     *
     * @param ref of a schema, maps to a component
     * @return the component name
     */
    private static String parseSchemaRef(String ref) {
        final Pattern pattern = Pattern.compile("#/components/schemas/(\\w+)");
        final Matcher matcher = pattern.matcher(ref);
        matcher.find();
        return matcher.group(1);
    }
}
