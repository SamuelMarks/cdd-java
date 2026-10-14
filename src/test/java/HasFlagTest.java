import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import cli.CddCli;

/**
 * Unit tests for CLI flag and argument resolution helpers in {@link CddCli}.
 */
public class HasFlagTest {
	/**
	 * Verifies {@code hasFlag}, {@code getArg}, and {@code findJavaFiles} behavior.
	 *
	 * @throws Exception
	 *             If reflective invocation fails.
	 */
	@Test
	public void testHasFlag() throws Exception {
		Method hasFlag = CddCli.class.getDeclaredMethod("hasFlag", String[].class, String.class, String.class,
				String.class);
		hasFlag.setAccessible(true);

		// args has it (both shortFlag and longFlag and non-matching arg)
		assertTrue((Boolean) hasFlag.invoke(null, new String[]{"other", "-t"}, "-t", "--test", "ENV"));
		assertTrue((Boolean) hasFlag.invoke(null, new String[]{"other", "--test"}, "-t", "--test", "ENV"));
		assertFalse((Boolean) hasFlag.invoke(null, new String[]{"other"}, "-t", "--test", null));

		// args doesn't have it, env is null
		assertFalse((Boolean) hasFlag.invoke(null, new String[]{}, "--test", "--test", null));

		// envVar not found (tests null branch of env != null)
		assertFalse((Boolean) hasFlag.invoke(null, new String[]{}, "--test", "--test", "DOES_NOT_EXIST_XYZ"));

		Method getArg = CddCli.class.getDeclaredMethod("getArg", String[].class, String.class, String.class,
				String.class);
		getArg.setAccessible(true);
		assertEquals("val", getArg.invoke(null, new String[]{"-t", "val"}, "-t", "--test", null));
		assertEquals("val2", getArg.invoke(null, new String[]{"--test", "val2"}, "-t", "--test", null));
		assertNull(getArg.invoke(null, new String[]{}, "-t", "--test", null));
		assertNull(getArg.invoke(null, new String[]{}, "-t", "--test", "DOES_NOT_EXIST_XYZ"));

		Method findJavaFiles = CddCli.class.getDeclaredMethod("findJavaFiles", java.io.File.class,
				java.util.List.class);
		findJavaFiles.setAccessible(true);
		java.util.List<java.io.File> list = new java.util.ArrayList<>();
		findJavaFiles.invoke(null, new java.io.File("pom.xml"), list);
		assertTrue(list.isEmpty());
	}
}
