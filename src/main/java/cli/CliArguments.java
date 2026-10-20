package cli;

import java.util.function.Function;

/**
 * Strongly typed command-line arguments wrapper for {@link CddCli}.
 */
public class CliArguments {

	/** The raw command-line arguments array. */
	private final String[] args;

	/**
	 * Constructs a new {@link CliArguments} instance for the given argument array.
	 *
	 * @param args
	 *            The raw command-line arguments.
	 */
	public CliArguments(String[] args) {
		this.args = args != null ? args.clone() : new String[0];
	}

	/**
	 * Parses a raw command-line argument array into a {@link CliArguments}
	 * instance.
	 *
	 * @param args
	 *            The raw command-line arguments.
	 * @return The parsed {@link CliArguments} instance.
	 */
	public static CliArguments parse(String[] args) {
		return new CliArguments(args);
	}

	/**
	 * Returns the primary command (first argument), or {@code null} if empty.
	 *
	 * @return The primary command string, or {@code null}.
	 */
	public String command() {
		return args.length > 0 ? args[0] : null;
	}

	/**
	 * Returns the subcommand at index 1 if it does not start with {@code "-"}, or
	 * {@code defaultSubCommand} otherwise.
	 *
	 * @param defaultSubCommand
	 *            The fallback subcommand name.
	 * @return The resolved subcommand string.
	 */
	public String subCommand(String defaultSubCommand) {
		if (args.length > 1 && !args[1].startsWith("-")) {
			return args[1];
		}
		return defaultSubCommand;
	}

	/**
	 * Retrieves a command-line option value by short flag, long flag, or fallback
	 * environment variable.
	 *
	 * @param shortFlag
	 *            The short flag (e.g. {@code -i}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --input}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @return The resolved argument value, or {@code null} if not present.
	 */
	public String getArg(String shortFlag, String longFlag, String envVar) {
		return getArg(this.args, shortFlag, longFlag, envVar, CddCli.envProvider);
	}

	/**
	 * Checks whether a boolean flag is enabled via command-line arguments or
	 * environment variable.
	 *
	 * @param shortFlag
	 *            The short flag (e.g. {@code -h}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --help}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @return {@code true} if the flag is set; {@code false} otherwise.
	 */
	public boolean hasFlag(String shortFlag, String longFlag, String envVar) {
		return hasFlag(this.args, shortFlag, longFlag, envVar, CddCli.envProvider);
	}

	/**
	 * Static helper to retrieve a command-line option value by short flag, long
	 * flag, or fallback environment variable.
	 *
	 * @param args
	 *            The command-line arguments array.
	 * @param shortFlag
	 *            The short flag (e.g. {@code -i}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --input}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @param envProvider
	 *            The environment lookup function.
	 * @return The resolved argument value, or {@code null} if not present.
	 */
	public static String getArg(String[] args, String shortFlag, String longFlag, String envVar,
			Function<String, String> envProvider) {
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(shortFlag) || args[i].equals(longFlag)) {
				return args[i + 1];
			}
		}
		if (envVar != null) {
			return envProvider.apply(envVar);
		}
		return null;
	}

	/**
	 * Static helper to check whether a boolean flag is enabled via command-line
	 * arguments or environment variable.
	 *
	 * @param args
	 *            The command-line arguments array.
	 * @param shortFlag
	 *            The short flag (e.g. {@code -h}).
	 * @param longFlag
	 *            The long flag (e.g. {@code --help}).
	 * @param envVar
	 *            The fallback environment variable name, or {@code null}.
	 * @param envProvider
	 *            The environment lookup function.
	 * @return {@code true} if the flag is set; {@code false} otherwise.
	 */
	public static boolean hasFlag(String[] args, String shortFlag, String longFlag, String envVar,
			Function<String, String> envProvider) {
		for (String arg : args) {
			if (arg.equals(shortFlag) || arg.equals(longFlag))
				return true;
		}
		if (envVar != null) {
			String env = envProvider.apply(envVar);
			return "true".equalsIgnoreCase(env) || "1".equals(env);
		}
		return false;
	}
}
