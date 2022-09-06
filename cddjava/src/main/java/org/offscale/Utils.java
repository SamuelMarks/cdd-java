package org.offscale;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
    * Gets a map between Types in OpenAPI and Types in Java. Going from OpenAPI -> Java.
     */
    public static ImmutableMap<String, String> getOpenAPIToJavaTypes() {
        Map<String, String> openAPIToJavaTypes = new HashMap<>();
        openAPIToJavaTypes.put("string", "String");
        openAPIToJavaTypes.put("int64", "long");
        openAPIToJavaTypes.put("integer", "int");
        openAPIToJavaTypes.put("int32", "int");
        return ImmutableMap.copyOf(openAPIToJavaTypes);
    }
}
