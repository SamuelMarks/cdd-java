package org.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Merge {
    private final ImmutableMap<String, String> components;
    private final String routes;

    private final JSONObject jo;

    private final String filePath;

    public Merge(ImmutableMap<String, String> components, String routes, String filePath) {
        this.components = components;
        this.routes = routes;
        this.jo = Utils.getJSONObjectFromFile(filePath, this.getClass());
        this.filePath = filePath;
    }

    // TODO: Still working on this, but taking a break to write tests for Create
//    public ImmutableMap<String, String> mergeComponents() {
//        Create create = new Create(filePath);
//        ImmutableMap<String, String> openAPIComponents = create.generateComponents();
//        Map<String, String> mergedComponents = new HashMap<>();
//        openAPIComponents.entrySet().forEach(entry -> );
//        CompilationUnit cuComponent = StaticJavaParser.parse(this.components.get(""));
//    }


}
