package io.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Given existing code and an openAPI spec, updates code based on openAP spec.
 */
public class Merge {
    private final ImmutableMap<String, String> components;
    private final String routes;

    private final Create create;

    public Merge(ImmutableMap<String, String> components, String routes, String filePath) {
        this.components = components;
        this.routes = routes;
        this.create = new Create(filePath);
    }

    /**
     * Updates the Components code with the openAPI components.
     * When there is a conflict, uses openAPI as the source of truth.
     *
     * @return a map where the keys are the component names and the values
     * are the component code.
     */
    public ImmutableMap<String, String> mergeComponents() {
        final ImmutableMap<String, String> openAPIComponents = create.generateComponents();
        final Map<String, String> mergedComponents = new HashMap<>();
        openAPIComponents.forEach((key, value) -> mergedComponents.put(key, mergeComponent(value, key)));
        return ImmutableMap.copyOf(mergedComponents);
    }

    /**
     * @param componentCode the openAPI component code
     * @param componentName the openAPI component name
     * @return the merged code for a component
     */
    public String mergeComponent(String componentCode, String componentName) {
        if (!this.components.containsKey(componentName)) {
            return componentCode;
        }

        final CompilationUnit openAPIComponent = StaticJavaParser.parse(componentCode);
        final Optional<ClassOrInterfaceDeclaration> openAPIComponentClass = openAPIComponent.getClassByName(componentName);
        final CompilationUnit javaCodeComponent = StaticJavaParser.parse(this.components.get(componentName));
        final Optional<ClassOrInterfaceDeclaration> javaCodeComponentClass = javaCodeComponent.getClassByName(componentName);
        if (javaCodeComponentClass.isPresent() && openAPIComponentClass.isPresent()) {
            openAPIComponentClass.get().getFields().forEach(field -> {
                final VariableDeclarator varDeclarator = field.getVariable(0);
                if (javaCodeComponentClass.get().getFieldByName(varDeclarator.getNameAsString()).isPresent()) {
                    javaCodeComponentClass.get().getFieldByName(varDeclarator.getNameAsString()).get().remove();
                }
                final ClassOrInterfaceDeclaration componentClass = javaCodeComponentClass.get();
                componentClass.addField(varDeclarator.getTypeAsString(), varDeclarator.getNameAsString())
                        .setJavadocComment(field.getJavadocComment().get());
            });
            javaCodeComponentClass.get().getFields().forEach(field -> {
                if (openAPIComponentClass.get().getFieldByName(field.getVariable(0).getNameAsString()).isEmpty()) {
                    field.remove();
                }
            });

            if (openAPIComponentClass.get().getJavadocComment().isPresent()) {
                javaCodeComponentClass.get().setJavadocComment(openAPIComponentClass.get().getJavadocComment().get());
            }
        }
        return javaCodeComponent.toString();
    }

    /**
     * @return merged routes between existing code and openAPI spec.
     */
    public String mergeRoutes() {
        final String openAPIRoutes = create.generateRoutesAndTests().routes();
        final CompilationUnit cuOpenAPIRoutes = StaticJavaParser.parse(openAPIRoutes);
        final CompilationUnit cuJavaCodeRoutes = StaticJavaParser.parse(this.routes);

        if (cuJavaCodeRoutes.getInterfaceByName("Routes").isEmpty()
                || cuOpenAPIRoutes.getInterfaceByName("Routes").isEmpty()) {
            throw new IllegalArgumentException("Couldn't find Routes interface in openAPI Spec or Java code"); // perhaps change this to a different exceptions
        }

        ClassOrInterfaceDeclaration cuJavaCodeRoutesInterface = cuJavaCodeRoutes.getInterfaceByName("Routes").get();
        ClassOrInterfaceDeclaration cuOpenAPIRoutesInterface = cuOpenAPIRoutes.getInterfaceByName("Routes").get();
        cuOpenAPIRoutesInterface.getMethods().forEach(method -> {
            if (!cuJavaCodeRoutesInterface.getMethodsByName(method.getNameAsString()).isEmpty()) {
                cuJavaCodeRoutesInterface.getMethodsByName(method.getNameAsString()).get(0).remove();
            }
            cuJavaCodeRoutesInterface.addMethod(method.getNameAsString())
                    .setModifiers(method.getModifiers())
                    .setType(method.getType())
                    .setParameters(method.getParameters())
                    .setJavadocComment(method.getJavadocComment().get())
                    .removeBody();
        });

        return cuJavaCodeRoutes.toString();
    }
}
