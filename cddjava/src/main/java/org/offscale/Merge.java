package org.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Merge {
    private final ImmutableMap<String, String> components;
    private final String routes;

    private Create create;
    private final String filePath;

    public Merge(ImmutableMap<String, String> components, String routes, String filePath) {
        this.components = components;
        this.routes = routes;
        this.filePath = filePath;
        this.create = new Create(filePath);
    }

    // TODO: Still working on this, but taking a break to write tests for Create
    public ImmutableMap<String, String> mergeComponents() {
        ImmutableMap<String, String> openAPIComponents = create.generateComponents();
        Map<String, String> mergedComponents = new HashMap<>();
        openAPIComponents.entrySet().forEach(entry -> mergedComponents.put(entry.getKey(), mergeComponent(entry.getValue(), entry.getKey())));
        CompilationUnit cuComponent = StaticJavaParser.parse(this.components.get(""));

        return ImmutableMap.copyOf(mergedComponents);
    }

    public String mergeComponent(String componentCode, String componentName) {
        if (!this.components.containsKey(componentName)) {
            return componentCode;
        }

        CompilationUnit openAPIComponent = StaticJavaParser.parse(componentCode);
        Optional<ClassOrInterfaceDeclaration> openAPIComponentClass = openAPIComponent.getClassByName(componentName);
        CompilationUnit javaCodeComponent = StaticJavaParser.parse(this.components.get(this.components.get(componentName)));
        Optional<ClassOrInterfaceDeclaration> javaCodeComponentClass = javaCodeComponent.getClassByName(componentName);
        if (javaCodeComponentClass.isPresent() && openAPIComponentClass.isPresent()) {
            openAPIComponentClass.get().getFields().forEach(field -> {
                VariableDeclarator varDeclarator = field.getVariable(0);
                System.out.println(varDeclarator.getNameAsString());
                if (javaCodeComponentClass.get().getFieldByName(varDeclarator.getNameAsString()).isPresent()) {
                    javaCodeComponentClass.get().getFieldByName(varDeclarator.getNameAsString()).get().remove();
                }
                javaCodeComponentClass.get().addField(varDeclarator.getTypeAsString(), varDeclarator.getNameAsString())
                        .setJavadocComment(field.getJavadocComment().get());
            });
        }
        return javaCodeComponent.toString();
    }

    public String mergeRoutes() {
        String openAPIRoutes = create.generateRoutes();
        CompilationUnit cuOpenAPIRoutes = StaticJavaParser.parse(openAPIRoutes);
        CompilationUnit cuJavaCodeRoutes = StaticJavaParser.parse(this.routes);
        ClassOrInterfaceDeclaration javaCodeInterface = cuOpenAPIRoutes.getInterfaceByName("routes").get();
        cuOpenAPIRoutes.getInterfaceByName("routes").get().getMethods().forEach(method -> {
            if (!javaCodeInterface.getMethodsByName(method.getNameAsString()).isEmpty()) {
                javaCodeInterface.getMethodsByName(method.getNameAsString()).get(0).remove();
            }
            javaCodeInterface.addField(method.getTypeAsString(), method.getNameAsString()).setJavadocComment(method.getJavadocComment().get());
        });

        return cuJavaCodeRoutes.toString();
    }
}
