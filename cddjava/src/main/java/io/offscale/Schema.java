package io.offscale;

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
}
