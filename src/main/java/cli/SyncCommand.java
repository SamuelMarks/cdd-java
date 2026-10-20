package cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import openapi.OpenAPI;

/**
 * Bidirectional source synchronization and AST extraction utilities for
 * {@link CddCli}.
 */
public class SyncCommand {

	/**
	 * Default constructor.
	 */
	public SyncCommand() {
	}

	/**
	 * Synchronizes Java source files in {@code dir} against the extracted OpenAPI
	 * or Arazzo specification, writing updates either in place or to
	 * {@code outputDir}.
	 *
	 * @param dir
	 *            The input source directory.
	 * @param outputDir
	 *            The optional output directory path, or {@code null} to update in
	 *            place.
	 * @param truth
	 *            The optional source-of-truth filter.
	 * @throws Exception
	 *             If reading, parsing, or writing files fails.
	 */
	public static void syncDirectory(File dir, String outputDir, String truth) throws Exception {
		OpenAPI fullApi = extractOpenAPI(dir, truth);
		List<File> javaFiles = new ArrayList<>();
		ProjectScaffolder.findJavaFiles(dir, javaFiles);
		for (File jf : javaFiles) {
			String source = ProjectScaffolder.readFile(jf);
			String newSource = source;
			String absPath = jf.getAbsolutePath().replace('\\', '/');
			if (absPath.contains("/classes/")) {
				newSource = classes.Emit.emit(fullApi, source);
			} else if (absPath.contains("/orm/")) {
				newSource = orm.Emit.emit(fullApi, source);
			} else if (absPath.contains("/routes/")) {
				newSource = routes.Emit.emit(fullApi, source);
			} else if (absPath.contains("/mocks/")) {
				newSource = mocks.Emit.emit(fullApi, source);
			} else if (absPath.contains("/tests/")) {
				newSource = tests.Emit.emit(fullApi, source);
			} else if (absPath.contains("/functions/")) {
				newSource = functions.Emit.emit(fullApi, source);
			} else if (absPath.contains("/cli/")) {
				newSource = cli.Emit.emitCli(fullApi);
			} else if (absPath.contains("/workflows/")) {
				arazzo.Arazzo arazzoSpec = extractArazzo(dir);
				newSource = workflows.Emit.emit(arazzoSpec, null, source);
			}
			File targetFile = jf;
			if (outputDir != null) {
				String relativePath = jf.getAbsolutePath().substring(dir.getAbsolutePath().length());
				if (relativePath.startsWith(File.separator)) {
					relativePath = relativePath.substring(1);
				}
				targetFile = new File(ProjectScaffolder.resolveWasiPath(outputDir, CddCli.envProvider), relativePath);
			}
			if (!newSource.equals(source) || outputDir != null) {
				ProjectScaffolder.writeOutput(targetFile, newSource);
				System.out.println("Updated: " + targetFile.getAbsolutePath());
			}
		}
		System.out.println("Sync complete.");
	}

	/**
	 * Extracts an aggregated OpenAPI specification by parsing all Java files in a
	 * directory or file path.
	 *
	 * @param dir
	 *            The root directory or single Java file to scan.
	 * @return The extracted {@link OpenAPI} model.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	public static OpenAPI extractOpenAPI(File dir) throws Exception {
		return extractOpenAPI(dir, null);
	}

	/**
	 * Extracts an aggregated OpenAPI specification by parsing source files in a
	 * directory or file path, optionally prioritizing a source of truth.
	 *
	 * @param dir
	 *            The root directory or single file to scan.
	 * @param truth
	 *            Optional source of truth filter (e.g. {@code "openapi"},
	 *            {@code "orm"}, {@code "classes"}, {@code "routes"}, or
	 *            {@code null} for all).
	 * @return The extracted {@link OpenAPI} model.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	public static OpenAPI extractOpenAPI(File dir, String truth) throws Exception {
		if ("openapi".equalsIgnoreCase(truth)) {
			File specFile = new File(dir, "openapi.json");
			if (specFile.exists()) {
				return openapi.Parse.fromFile(specFile);
			}
		}
		List<File> javaFiles = new ArrayList<>();
		ProjectScaffolder.findJavaFiles(dir, javaFiles);
		OpenAPI fullApi = new OpenAPI();
		fullApi.openapi = "3.2.0";
		fullApi.info = new openapi.Info();
		fullApi.info.title = "Extracted API";
		fullApi.info.version = "0.0.4";
		fullApi.paths = new openapi.Paths();
		fullApi.paths.pathItems = new java.util.HashMap<>();
		fullApi.components = new openapi.Components();
		fullApi.components.schemas = new java.util.HashMap<>();
		for (File jf : javaFiles) {
			String source = ProjectScaffolder.readFile(jf);
			if (!"orm".equalsIgnoreCase(truth) && !"classes".equalsIgnoreCase(truth)) {
				OpenAPI apiPaths = routes.Parse.parse(source);
				fullApi.paths.pathItems.putAll(apiPaths.paths.pathItems);
				OpenAPI mockPaths = mocks.Parse.parse(source);
				for (Map.Entry<String, openapi.PathItem> entry : mockPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
				OpenAPI cliPaths = cli.Parse.parse(source);
				if (cliPaths.paths != null) {
					for (Map.Entry<String, openapi.PathItem> entry : cliPaths.paths.pathItems.entrySet()) {
						fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
				OpenAPI testsPaths = tests.Parse.parse(source);
				for (Map.Entry<String, openapi.PathItem> entry : testsPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
				OpenAPI fnPaths = functions.Parse.parse(source);
				for (Map.Entry<String, openapi.PathItem> entry : fnPaths.paths.pathItems.entrySet()) {
					fullApi.paths.pathItems.putIfAbsent(entry.getKey(), entry.getValue());
				}
			}
			if (!"orm".equalsIgnoreCase(truth)) {
				OpenAPI apiClasses = classes.Parse.parse(source);
				fullApi.components.schemas.putAll(apiClasses.components.schemas);
			}
			if (!"classes".equalsIgnoreCase(truth)) {
				OpenAPI ormClasses = orm.Parse.parse(source);
				fullApi.components.schemas.putAll(ormClasses.components.schemas);
			}
		}
		return fullApi;
	}

	/**
	 * Extracts an aggregated Arazzo 1.1.0 specification from a Java file or
	 * directory containing workflow orchestrators, CLI runners, E2E tests, or
	 * mocks.
	 *
	 * @param dir
	 *            The root directory or single file to scan.
	 * @return The extracted {@link arazzo.Arazzo} specification.
	 * @throws Exception
	 *             If reading or parsing files fails.
	 */
	public static arazzo.Arazzo extractArazzo(File dir) throws Exception {
		File arazzoFile = new File(dir, "arazzo.json");
		if (arazzoFile.isFile()) {
			return arazzo.Parse.fromFile(arazzoFile);
		}
		List<File> javaFiles = new ArrayList<>();
		ProjectScaffolder.findJavaFiles(dir, javaFiles);
		for (File jf : javaFiles) {
			String source = ProjectScaffolder.readFile(jf);
			String name = jf.getName();
			if (name.equals("WorkflowCli.java")) {
				return cli.ArazzoCliParser.parseWorkflowCli(source);
			}
			if (name.contains("IntegrationTest")) {
				return tests.ArazzoTestParser.parse(source);
			}
			if (name.contains("Mock")) {
				return mocks.ArazzoMockParser.parse(source);
			}
			return workflows.Parse.parse(source);
		}
		arazzo.Arazzo empty = new arazzo.Arazzo();
		empty.info = new arazzo.ArazzoInfo();
		empty.info.title = "Extracted Workflow";
		empty.info.version = "1.0.0";
		empty.sourceDescriptions = new ArrayList<>();
		empty.workflows = new ArrayList<>();
		return empty;
	}
}
