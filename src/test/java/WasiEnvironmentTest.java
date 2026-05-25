import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;

public class WasiEnvironmentTest {

	public static void run() throws Exception {
		System.out.println("Running WASI Environment Simulation Test...");

		// 1. Create a temporary directory to act as the virtual root
		File virtualRoot = Files.createTempDirectory("wasi_root").toFile();
		try {
			// 2. Create the spec.json inside the virtual root
			File specFile = new File(virtualRoot, "spec.json");
			String specJson = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Wasi API\",\"version\":\"1.0\"},\"paths\":{}}";
			try (FileOutputStream fos = new FileOutputStream(specFile)) {
				fos.write(specJson.getBytes("UTF-8"));
			}

			// 3. Create an output directory inside the virtual root
			File outDir = new File(virtualRoot, "out");
			outDir.mkdirs();

			// 4. Set up the ProcessBuilder
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");

			java.util.List<String> command = new java.util.ArrayList<>();
			command.add(javaBin);
			command.add("-cp");
			command.add(classpath);

			// Pass jacoco agent if present
			for (String arg : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
				if (arg.startsWith("-javaagent:") && arg.contains("jacoco")) {
					command.add(arg);
				}
			}

			command.add("cli.Main");
			command.add("from_openapi");
			command.add("to_sdk");
			command.add("-i");
			command.add("/spec.json");
			command.add("-o");
			command.add("/out");

			ProcessBuilder pb = new ProcessBuilder(command);

			// 5. Inject the simulated environment variable
			Map<String, String> env = pb.environment();
			env.put("CDD_WASI_VIRTUAL_ROOT", virtualRoot.getAbsolutePath());

			// 6. Run the process
			Process process = pb.start();

			// 7. Wait for completion and check the result
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				Scanner s = new Scanner(process.getErrorStream()).useDelimiter("\\A");
				String error = s.hasNext() ? s.next() : "";
				System.err.println("WASI Simulation process failed with exit code: " + exitCode);
				System.err.println("Output:\n" + error);
				throw new Exception("WASI Process Failed");
			}

			// 8. Verify the output was created in the virtual root
			File expectedOutput = new File(outDir, "src/main/java/Sdk.java");
			if (!expectedOutput.exists()) {
				throw new Exception("Expected output file not found in virtual root: " + expectedOutput);
			}

			System.out.println("WASI Environment Simulation Test passed.");

		} finally {
			// Cleanup
			deleteDir(virtualRoot);
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
