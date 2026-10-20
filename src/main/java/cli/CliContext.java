package cli;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Encapsulates runtime configuration for {@link CddCli} and {@link Main},
 * including environment variable resolution, workspace directory, and process
 * exit handling.
 */
public class CliContext {
	/** Environment variable lookup function. */
	public final Function<String, String> envProvider;
	/** Default workspace directory for in-memory and MCP AST extraction. */
	public final File workspaceDir;
	/** Process exit handler invoked on fatal CLI errors. */
	public final Consumer<Integer> exitHandler;

	/**
	 * Constructs a {@code CliContext} with explicit configuration.
	 *
	 * @param envProvider
	 *            Environment variable lookup function.
	 * @param workspaceDir
	 *            Workspace directory.
	 * @param exitHandler
	 *            Process exit handler.
	 */
	public CliContext(Function<String, String> envProvider, File workspaceDir, Consumer<Integer> exitHandler) {
		this.envProvider = envProvider;
		this.workspaceDir = workspaceDir;
		this.exitHandler = exitHandler;
	}

	/**
	 * Creates a default {@code CliContext} bound to {@link System#getenv(String)},
	 * {@code src/main/java}, and {@link System#exit(int)}.
	 *
	 * @return The default {@link CliContext}.
	 */
	public static CliContext createDefault() {
		return new CliContext(System::getenv, new File("src/main/java"), System::exit);
	}
}
