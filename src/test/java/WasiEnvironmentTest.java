import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;

public class WasiEnvironmentTest {

    public static void run() throws Exception {
        System.out.println("Running WASI Environment Simulation Test...");

        // Setup virtual root
        File tempDir = Files.createTempDirectory("cdd-wasi-test").toFile();
        try {
            // Setup /spec.json inside virtual root
            File specJson = new File(tempDir, "spec.json");
            try (FileOutputStream fos = new FileOutputStream(specJson)) {
                String minimalSpec = "{\"openapi\":\"3.2.0\",\"info\":{\"title\":\"Test API\",\"version\":\"1.0\"},\"paths\":{},\"components\":{\"schemas\":{\"MySchema\":{\"type\":\"object\",\"properties\":{\"myProp\":{\"type\":\"string\"}}}}}}";
                fos.write(minimalSpec.getBytes("UTF-8"));
            }

            // Setup /out directory inside virtual root
            File outDir = new File(tempDir, "out");
            outDir.mkdirs();

            // Prepare process builder
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");

            ProcessBuilder pb = new ProcessBuilder(
                javaBin, "-cp", "target/classes:lib/*",
                "cli.Main",
                "from_openapi",
                "to_sdk",
                "-i",
                "/spec.json",
                "-o",
                "/out"
            );

            // Mock environment variables
            Map<String, String> env = pb.environment();
            env.put("CDD_COMMAND", "from_openapi");
            env.put("INPUT", "/spec.json");
            env.put("OUTPUT_DIR", "out");
            // Set the virtual root so cli.Main resolves /spec.json -> tempDir/spec.json
            env.put("CDD_WASI_VIRTUAL_ROOT", tempDir.getAbsolutePath());

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output
            byte[] outBytes = process.getInputStream().readAllBytes();
            String output = new String(outBytes, "UTF-8");

            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("WASI Simulation process failed with exit code: " + exitCode);
                System.err.println("Output:\n" + output);
                throw new RuntimeException("WASI test exited with non-zero code");
            }

            // Validate outputs
            File generatedSdk = new File(new File(new File(outDir, "src"), "main"), new File("java", "Sdk.java").getPath());
            if (!generatedSdk.exists() || generatedSdk.length() == 0) {
                System.err.println("Output:\n" + output);
                throw new RuntimeException("Expected generated file /out/src/main/java/Sdk.java not found or is empty");
            }

            System.out.println("WASI Environment Simulation Test passed.");
        } finally {
            // Clean up
            deleteDir(tempDir);
        }
    }

    private static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDir(file);
                }
            }
        }
        dir.delete();
    }
}
