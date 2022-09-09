package org.offscale;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;

public class Merge {
    private final ImmutableMap<String, String> components;
    private final String routes;

    private final JSONObject jo;

    public Merge(ImmutableMap<String, String> components, String routes, String filePath) {
        this.components = components;
        this.routes = routes;
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
    }

//    public ImmutableMap<String, String> mergeComponents() {
//
//    }
}
