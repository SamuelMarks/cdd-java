import static org.junit.Assert.*;

import cli.CddCli;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for CLI flag and argument resolution helpers in {@link CddCli}.
 */
public class HasFlagTest {
	/**
	 * Default constructor.
	 */
	public HasFlagTest() {
	}

	/**
	 * Verifies {@link CddCli#hasFlag}, {@link CddCli#getArg}, and
	 * {@link CddCli#findJavaFiles} behavior directly without reflection.
	 */
	@Test
	public void testHasFlag() {
		assertTrue(CddCli.hasFlag(new String[]{"other", "-t"}, "-t", "--test", "ENV"));
		assertTrue(CddCli.hasFlag(new String[]{"other", "--test"}, "-t", "--test", "ENV"));
		assertFalse(CddCli.hasFlag(new String[]{"other"}, "-t", "--test", null));
		assertFalse(CddCli.hasFlag(new String[]{}, "--test", "--test", null));
		assertFalse(CddCli.hasFlag(new String[]{}, "--test", "--test", "DOES_NOT_EXIST_XYZ"));

		assertEquals("val", CddCli.getArg(new String[]{"-t", "val"}, "-t", "--test", null));
		assertEquals("val2", CddCli.getArg(new String[]{"--test", "val2"}, "-t", "--test", null));
		assertNull(CddCli.getArg(new String[]{}, "-t", "--test", null));
		assertNull(CddCli.getArg(new String[]{}, "-t", "--test", "DOES_NOT_EXIST_XYZ"));

		List<File> list = new ArrayList<>();
		CddCli.findJavaFiles(new File("pom.xml"), list);
		assertTrue(list.isEmpty());

		assertNotNull(new cli.CliArguments(null));
		cli.CliArguments parsed = cli.CliArguments.parse(new String[]{"from_openapi", "-i", "in.json"});
		assertEquals("from_openapi", parsed.command());
		assertEquals("to_sdk", parsed.subCommand("to_sdk"));
		assertEquals("in.json", parsed.getArg("-i", "--input", null));
		assertFalse(parsed.hasFlag("-h", "--help", null));
		assertNull(new cli.CliArguments(null).command());
		assertEquals("to_sdk", new cli.CliArguments(new String[]{"from_openapi"}).subCommand("to_sdk"));

		assertNotNull(new cli.ProjectScaffolder());
		assertNotNull(new cli.SyncCommand());
		assertNotNull(new cli.McpJsonRpcServer());
		assertNotNull(new cli.CliHelpCodec());
		assertNotNull(new routes.RouteJavadocEmitter());
		assertNotNull(new routes.RouteJavadocParser());
		assertTrue(cli.ProjectScaffolder.generatePomXml().contains("<project"));
		assertTrue(cli.ProjectScaffolder.generateGithubActionsCi().contains("name: CI"));
		assertTrue(cli.McpJsonRpcServer.buildMcpToolsList("1").contains("cdd_generate"));
	}
}
