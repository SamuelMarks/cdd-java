package cli;

import java.util.function.Consumer;

/**
 * CLI Entrypoint.
 */
public class Main {

	/**
	 * Process exit handler, defaulting to {@link System#exit(int)}.
	 */
	public static Consumer<Integer> exitHandler = System::exit;

	/**
	 * Default constructor for Main.
	 */
	public Main() {
	}

	/**
	 * Executes the CLI application with the provided command-line arguments and
	 * {@link CliContext}.
	 *
	 * @param args
	 *            Command-line arguments passed to the CLI.
	 * @param context
	 *            The {@link CliContext} governing exit handling.
	 */
	public static void main(String[] args, CliContext context) {
		try {
			CddCli.run(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			context.exitHandler.accept(1);
		}
	}

	/**
	 * Executes the CLI application with the provided command-line arguments.
	 *
	 * @param args
	 *            Command-line arguments passed to the CLI.
	 */
	public static void main(String[] args) {
		main(args, new CliContext(CddCli.envProvider, CddCli.workspaceDir, exitHandler));
	}
}
