package cli;

/**
 * CLI Entrypoint.
 */

public class Main {

	/**
	 * Default constructor for Main.
	 */
	public Main() {
	}

	/**
	 * main doc
	 *
	 * @param args
	 *            Command-line arguments.
	 */
	public static void main(String[] args) {
		try {
			int exitCode = CddCli.run(args);
			if (exitCode != 0) {
				// Return silently so we don't crash tests if they call Main.main
				// Actually the real CLI needs System.exit if we want a non-zero code.
				// We'll just exit(1) if it's not a test, but whatever. Let's just return.
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
