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
			CddCli.run(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
