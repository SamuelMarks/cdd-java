package io.offscale;

import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "cdd", mixinStandardHelpOptions = true, version = "cdd 0.01",
        description = "Converts openapi spec to code")
public final class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "openapi -> java or java -> openapi")
    private String to;
    @CommandLine.Parameters(index = "1", description = "The openapi spec file")
    private String filePath;

    private void createFileWithName(String name) throws IOException {
        try {
            File newFile = new File(name);
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public Integer call() throws Exception {
        switch (to) {
            case "openapi": throw new UnsupportedOperationException("Need to implement");
            case "java" : {
                final Create create = new Create(filePath);
                System.out.println(create.generateRoutesAndTests().routes());
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