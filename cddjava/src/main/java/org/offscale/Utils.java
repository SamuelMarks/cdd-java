package org.offscale;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
    * Gets a map between Types in OpenAPI and Types in Java. Going from OpenAPI -> Java.
     */
    public static ImmutableMap<String, String> getOpenAPIToJavaTypes() {
        return ImmutableMap.of(
            "string", "String",
            "int64", "long",
            "integer", "int",
            "int32", "int"
        );
    }
}
