package cli;

import openapi.*;
import java.util.*;
import java.util.regex.*;

/**
 * Parses a CLI application to an OpenAPI model.
 */
public class Parse {

	/**
	 * Default constructor.
	 */
	public Parse() {
	}

	/**
	 * Parses the CLI source code into an OpenAPI object.
	 *
	 * @param existingSource
	 *            The source code string.
	 * @return The parsed OpenAPI object.
	 */
	public static OpenAPI parse(String existingSource) {
		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();
		api.components.responses = new HashMap<>();
		api.components.parameters = new HashMap<>();
		api.components.requestBodies = new HashMap<>();
		api.components.headers = new HashMap<>();
		api.components.links = new HashMap<>();
		api.components.callbacks = new HashMap<>();
		api.components.pathItems = new HashMap<>();
		api.components.securitySchemes = new HashMap<>();
		api.components.mediaTypes = new HashMap<>();
		String helpBody = "";
		Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{(.*?)\\n    \\}")
				.matcher(existingSource);
		if (hm.find()) {
			helpBody = hm.group(1);
		}
		Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(helpBody);
		CliHelpCodec.ParseState state = new CliHelpCodec.ParseState();
		while (lm.find()) {
			String line = CliHelpCodec.unescape(lm.group(1));
			if (CliHelpCodec.parseInfoAndServers(line, api)) {
				continue;
			}
			if (CliHelpCodec.parseComponentLine(line, api, state)) {
				continue;
			}
			CliHelpCodec.parseOperationLine(line, api, state);
		}
		return api;
	}
}
