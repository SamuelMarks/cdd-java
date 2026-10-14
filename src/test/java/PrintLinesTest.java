import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import cli.Emit;
import cli.Parse;
import openapi.*;

/**
 * Unit test verifying CLI help output line formatting and round-trip parsing.
 */
public class PrintLinesTest {
	/**
	 * Emits a CLI class, extracts help output lines, and verifies round-trip
	 * parsing.
	 */
	@Test
	public void printLines() {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.contact = new Contact();
		api.info.contact.email = "e";
		String content = Emit.emitCli(api);
		assertNotNull(content);
		Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{(.*?)\\n    \\}").matcher(content);
		assertTrue(hm.find());
		Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(hm.group(1));
		List<String> lines = new ArrayList<>();
		while (lm.find()) {
			lines.add(lm.group(1));
		}
		assertTrue(lines.contains("SDK CLI"));
		assertTrue(lines.contains("Contact Object email: e"));
		OpenAPI roundTripped = Parse.parse(content);
		assertNotNull(roundTripped.info);
		assertNotNull(roundTripped.info.contact);
		assertEquals("e", roundTripped.info.contact.email);
	}
}
