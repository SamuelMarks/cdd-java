package cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

/**
 * Project scaffolding, file I/O, and WASI virtual path resolution utilities for
 * {@link CddCli}.
 */
public class ProjectScaffolder {

	/**
	 * Default constructor.
	 */
	public ProjectScaffolder() {
	}

	/**
	 * Resolves a file path against the WASI virtual root if configured.
	 *
	 * @param path
	 *            The file path to resolve.
	 * @param envProvider
	 *            The environment variable lookup function.
	 * @return The resolved {@link File} instance.
	 */
	public static File resolveWasiPath(String path, Function<String, String> envProvider) {
		String vfsRoot = envProvider.apply("CDD_WASI_VIRTUAL_ROOT");
		if (vfsRoot != null && path.startsWith("/")) {
			return new File(vfsRoot, path.substring(1));
		}
		return new File(path);
	}

	/**
	 * Writes string content to a UTF-8 file, creating parent directories if
	 * necessary.
	 *
	 * @param file
	 *            The target file to write.
	 * @param content
	 *            The UTF-8 string content to write.
	 * @throws IOException
	 *             If writing fails.
	 */
	public static void writeOutput(File file, String content) throws IOException {
		File absParent = file.getAbsoluteFile().getParentFile();
		absParent.mkdirs();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Reads the entire contents of a file as a UTF-8 string.
	 *
	 * @param file
	 *            The file to read.
	 * @return The file contents as a UTF-8 string.
	 * @throws IOException
	 *             If reading fails.
	 */
	public static String readFile(File file) throws IOException {
		return Files.readString(file.toPath(), StandardCharsets.UTF_8);
	}

	/**
	 * Recursively collects all {@code .java} files within a directory or single
	 * file.
	 *
	 * @param dir
	 *            The root directory or file to inspect.
	 * @param result
	 *            The list to populate with discovered Java files.
	 */
	public static void findJavaFiles(File dir, List<File> result) {
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java")) {
				result.add(dir);
			}
		} else {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files)
					findJavaFiles(f, result);
			}
		}
	}

	/**
	 * Generates the default Maven {@code pom.xml} scaffolding content for generated
	 * projects.
	 *
	 * @return The XML content for {@code pom.xml}.
	 */
	public static String generatePomXml() {
		return String.join("\n", "<?xml version='1.0' encoding='UTF-8'?>".replace('\'', '"'),
				"<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>"
						.replace('\'', '"'),
				"    <modelVersion>4.0.0</modelVersion>", "    <groupId>com.example</groupId>",
				"    <artifactId>generated-sdk</artifactId>", "    <version>0.0.4</version>", "    <properties>",
				"        <maven.compiler.source>11</maven.compiler.source>",
				"        <maven.compiler.target>11</maven.compiler.target>", "    </properties>", "    <dependencies>",
				"        <dependency>", "            <groupId>io.javalin</groupId>",
				"            <artifactId>javalin</artifactId>", "            <version>5.6.3</version>",
				"        </dependency>", "        <dependency>", "            <groupId>org.hibernate.orm</groupId>",
				"            <artifactId>hibernate-core</artifactId>", "            <version>6.4.4.Final</version>",
				"        </dependency>", "        <dependency>", "            <groupId>org.postgresql</groupId>",
				"            <artifactId>postgresql</artifactId>", "            <version>42.7.2</version>",
				"        </dependency>", "        <dependency>",
				"            <groupId>com.fasterxml.jackson.core</groupId>",
				"            <artifactId>jackson-databind</artifactId>", "            <version>2.15.2</version>",
				"        </dependency>", "        <dependency>",
				"            <groupId>com.fasterxml.jackson.datatype</groupId>",
				"            <artifactId>jackson-datatype-jsr310</artifactId>", "            <version>2.15.2</version>",
				"        </dependency>", "        <dependency>", "            <groupId>net.datafaker</groupId>",
				"            <artifactId>datafaker</artifactId>", "            <version>2.0.2</version>",
				"        </dependency>", "        <dependency>", "            <groupId>junit</groupId>",
				"            <artifactId>junit</artifactId>", "            <version>4.13.2</version>",
				"            <scope>test</scope>", "        </dependency>", "    </dependencies>", "</project>");
	}

	/**
	 * Generates the default {@code README.md} content for generated server and SDK
	 * projects, documenting the decoupled CDD server modes and administrative
	 * webhook trigger endpoints.
	 *
	 * @return The Markdown content for {@code README.md}.
	 */
	public static String generateServerReadme() {
		return String.join("\n", "# Generated CDD Project", "", "## Server Modes", "",
				"- `start` (No DB configured): **Stub Mode**. Server runs using traditional scaffolds, endpoints return `NotImplementedError` or empty bodies.",
				"- `start` (With `DATABASE_URL`): **Production Mode**. Uses actual ORM interactions against a real database.",
				"- `start --ephemeral`: **Sandbox Mode**. Uses actual ORM interactions against a fresh, throwaway database.",
				"- `start --ephemeral --seed`: **Full Mock Mode**. Ephemeral database, automatically populated with a localized fake data graph.",
				"", "## Advanced Mock Capabilities & Administrative Endpoints", "",
				"- `--strict-validation`: Enables strict OpenAPI request validation middleware (`400 Bad Request` on constraint failure).",
				"- `--enforce-auth`: Enforces authentication middleware (`401 Unauthorized` / `403 Forbidden`).",
				"- `--start-auth-server`: Starts integrated Identity Provider endpoints (`POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`).",
				"- `POST /_mock/trigger-webhook/{webhook_name}`: Administrative endpoint to dispatch outgoing webhook payloads (`{\"targetUrl\": \"https://...\", \"payload\": {...}}`) to a registered target URL.",
				"");
	}

	/**
	 * Writes the Maven {@code pom.xml} and {@code README.md} scaffolding files into
	 * the specified directory.
	 *
	 * @param dir
	 *            The target project directory.
	 * @throws IOException
	 *             If writing scaffolding files fails.
	 */
	public static void generateScaffolding(File dir) throws IOException {
		writeOutput(new File(dir, "pom.xml"), generatePomXml());
		writeOutput(new File(dir, "README.md"), generateServerReadme());
	}

	/**
	 * Generates the default GitHub Actions CI workflow YAML content.
	 *
	 * @return The YAML content for {@code .github/workflows/ci.yml}.
	 */
	public static String generateGithubActionsCi() {
		return String.join("\n", "name: CI", "on: [push, pull_request]", "jobs:", "  build:",
				"    runs-on: ubuntu-latest", "    steps:", "    - uses: actions/checkout@v6", "    - name: Set up JDK",
				"      uses: actions/setup-java@v3", "      with:", "        java-version: '11'",
				"        distribution: 'temurin'", "    - name: Build with Maven", "      run: mvn clean install", "");
	}

	/**
	 * Writes the GitHub Actions CI workflow into {@code .github/workflows/ci.yml}
	 * inside the target directory.
	 *
	 * @param dir
	 *            The target project directory.
	 * @throws IOException
	 *             If writing the workflow file fails.
	 */
	public static void generateGithubActions(File dir) throws IOException {
		File ghDir = new File(dir, ".github/workflows");
		ghDir.mkdirs();
		writeOutput(new File(ghDir, "ci.yml"), generateGithubActionsCi());
	}
}
