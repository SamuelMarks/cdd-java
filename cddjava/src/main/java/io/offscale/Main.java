package io.offscale;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "cdd", mixinStandardHelpOptions = true, version = "cdd 0.01",
        description = "Converts openapi spec to code")
public final class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "openapi -> java or java -> openapi")
    private String to;
    @CommandLine.Parameters(index = "1", description = "The openapi spec file")
    private String openAPIFilePath;
    private static final String mainPath = "src/main/java/io/offscale"; // this is a simplification for now

    private void writeToFile(String name, String contents, String path) throws IOException {
        File directory = new File(path);
        String pathWithName = path + name + ".java";
        directory.mkdirs();
        File newFile = new File(pathWithName);
        newFile.createNewFile();
        FileWriter myWriter = new FileWriter(pathWithName);
        myWriter.write(contents);
        myWriter.close();
    }

    private void addComponents(ImmutableMap<String, String> components, String path) throws IOException {
        components.forEach((name, code) -> {
            try {
                writeToFile(name, code, path);
            } catch (IOException e) {
                System.err.println("Failed to create/modify " + name + ".java");
            }
        });
    }

    private ImmutableMap<String, String> getComponents(File folder) throws IOException {
        HashMap<String, String> components = new HashMap<>();
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return ImmutableMap.copyOf(components);
        }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                components.put(Utils.getFileNameUntilDot(listOfFiles[i].getName()),
                        FileUtils.readFileToString(listOfFiles[i], "UTF-8"));
            }
        }
        return ImmutableMap.copyOf(components);
    }

    private String getRoutes(String filePath) {
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
                Merge code = new Merge(getComponents(new File(mainPath + "/api/components")),
                        getRoutes(mainPath + "/api/routes.java"),
                        openAPIFilePath);
                addComponents(code.mergeComponents(), mainPath + "/api/components/");
                writeToFile("Routes", code.mergeRoutes(), mainPath + "/api/");
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