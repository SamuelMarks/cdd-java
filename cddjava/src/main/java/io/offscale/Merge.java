package io.offscale;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.collect.ImmutableMap;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Given existing code and an openAPI spec, updates code based on openAP spec.
 */
public class Merge {
    private final ImmutableMap<String, String> components;
    private final String routes;
    private final String tests;

    private final Create create;

    public Merge(ImmutableMap<String, String> components,
                 String routes, String tests, String openAPIFilePath) {
        this.components = components;
        this.routes = routes;
        this.tests = tests;
        this.create = new Create(openAPIFilePath);
    }

    /**
     * Updates the Components code with the openAPI components.
     * When there is a conflict, uses openAPI as the source of truth.
     *
     * @return a map where the keys are the component names and the values
     * are the component code.
     */
    public ImmutableMap<String, String> mergeComponents() {
        final ImmutableMap<String, String> openAPIComponents =
                combineMaps(create.generateComponents(), create.generateRoutesAndTests().schemas());
        final Map<String, String> mergedComponents = new HashMap<>();
        openAPIComponents.forEach((key, value) -> mergedComponents.put(key, mergeComponent(value, key)));
        return ImmutableMap.copyOf(mergedComponents);
    }

    private ImmutableMap<String, String> combineMaps(Map<String, String> m1, Map<String, String> m2) {
        HashMap<String, String> map = new HashMap<>();
        map.putAll(m1);
        map.putAll(m2);
        return ImmutableMap.copyOf(map);
    }

    /**
     * @param componentCode the openAPI component code
     * @param componentName the openAPI component name
     * @return the merged code for a component
     */
    private String mergeComponent(String componentCode, String componentName) {
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

        //Routes doesn't exist yet
        if (cuJavaCodeRoutes.getInterfaceByName("Routes").isEmpty()) {
            return openAPIRoutes;
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

    public String mergeTests() {
        final String openAPITests = create.generateRoutesAndTests().tests();
        final CompilationUnit cuOpenAPITests = StaticJavaParser.parse(openAPITests);
        final CompilationUnit cuJavaCodeTests = StaticJavaParser.parse(this.tests);

        //Tests doesn't exist yet
        if (cuJavaCodeTests.getClassByName("Tests").isEmpty()) {
            return openAPITests;
        }

        ClassOrInterfaceDeclaration cuJavaCodeTestsClass = cuJavaCodeTests.getInterfaceByName("Tests").get();
        ClassOrInterfaceDeclaration cuOpenAPITestsClass = cuOpenAPITests.getInterfaceByName("Tests").get();
        cuOpenAPITestsClass.getMethods().forEach(method -> {
            if (!cuJavaCodeTestsClass.getMethodsByName(method.getNameAsString()).isEmpty()) {
                cuJavaCodeTestsClass.getMethodsByName(method.getNameAsString()).get(0).remove();
            }
            cuJavaCodeTestsClass.addMethod(method.getNameAsString())
                    .setModifiers(method.getModifiers())
                    .setType(method.getType())
                    .setParameters(method.getParameters())
                    .setJavadocComment(method.getJavadocComment().get());
        });

        return cuJavaCodeTests.toString();
    }
}
