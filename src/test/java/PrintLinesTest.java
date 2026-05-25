import org.junit.Test;
import java.util.regex.*;
import cli.Emit;
import openapi.*;

public class PrintLinesTest {
	@Test
	public void printLines() {
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.contact = new Contact();
		api.info.contact.email = "e";
		String content = Emit.emitCli(api);
		Matcher hm = Pattern.compile("(?s)private static void printHelp\\(\\) \\{(.*?)\\n    \\}").matcher(content);
		hm.find();
		Matcher lm = Pattern.compile("System\\.out\\.println\\(\"((?:[^\"]|\\\\\")*)\"\\);").matcher(hm.group(1));
		while (lm.find()) {
			System.out.println("LINE: " + lm.group(1));
		}
	}
}
