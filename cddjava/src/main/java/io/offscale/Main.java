package io.offscale;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "cdd", mixinStandardHelpOptions = true, version = "cdd 0.01",
        description = "Converts openapi spec to code")
public final class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "openapi -> java or java -> openapi")
    private String to;

    @CommandLine.Parameters(index = "1", description = "The openapi spec file path")
    private String openAPIFilePath;

    @CommandLine.Parameters(index = "2", description = "The filepath to the main package to generate api code")
    private String mainFilePath;
    @CommandLine.Parameters(index = "3", description = "The filepath to the test package to generate api tests")
    private String testFilePath;
    private void addComponents(ImmutableMap<String, String> components, String path) {
        components.forEach((name, code) -> {
            Utils.writeToFile(name, code, path);
        });
    }

    private ImmutableMap<String, String> getComponents(File folder) {
        HashMap<String, String> components = new HashMap<>();
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return ImmutableMap.copyOf(components);
        }

        Arrays.stream(listOfFiles).forEach(file -> {
            if (file.isFile()) {
                try {
                    components.put(Utils.getFileNameUntilDot(file.getName()),
                            FileUtils.readFileToString(file, "UTF-8"));
                } catch (IOException e) {
                    System.err.println("Failed to read file: " + file.getName());
                }
            }
        });

        return ImmutableMap.copyOf(components);
    }

    private String fileToString(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public Integer call() throws Exception {
        switch (to) {
            case "openapi": throw new UnsupportedOperationException("Need to implement");
            case "java" : {
                final String apiPath = mainFilePath + "/api";
                final String testPath = testFilePath + "/api";
                Merge code = new Merge(getComponents(new File(apiPath + "/components")),
                        fileToString(apiPath + "/routes.java"),fileToString(testPath + "/tests.java"),
                        openAPIFilePath);
                if (true) {
                    addComponents(code.mergeComponents(), apiPath + "/components/");
                    Utils.writeToFile("Routes", code.mergeRoutes(), apiPath + "/");
                    Utils.writeToFile("Tests", code.mergeTests(), testPath + "/");
                }
                return 0;
            }
            default: throw new IllegalArgumentException("Only valid values are openapi and java");
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}