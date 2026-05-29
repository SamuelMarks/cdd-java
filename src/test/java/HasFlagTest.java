import org.junit.Test;
import java.lang.reflect.Method;
import cli.CddCli;

public class HasFlagTest {
	@Test
	public void testHasFlag() throws Exception {
		Method hasFlag = CddCli.class.getDeclaredMethod("hasFlag", String[].class, String.class, String.class,
				String.class);
		hasFlag.setAccessible(true);

		// args has it
		hasFlag.invoke(null, new String[]{"--test"}, "--test", "--test", "ENV");

		// args doesn't have it, env is null
		hasFlag.invoke(null, new String[]{}, "--test", "--test", null);

		// envVar not found (tests null branch of env != null)
		hasFlag.invoke(null, new String[]{}, "--test", "--test", "DOES_NOT_EXIST_XYZ");
	}
}
