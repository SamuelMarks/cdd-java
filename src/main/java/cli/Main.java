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
	 * Executes the CLI application with the provided command-line arguments.
	 *
	 * @param args
	 *            Command-line arguments passed to the CLI.
	 */
	public static void main(String[] args) {
		try {
			CddCli.run(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			if ("true".equals(System.getProperty("cdd.test"))) {
				throw new RuntimeException("Exit 1", e);
			}
			exitHandler.accept(1);
		}
	}
}
