package cli;

import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.IsolateThread;
import java.util.List;
import java.util.ArrayList;

/**
 * Entry point for GraalVM WASI.
 */
public class GraalEntryPoint {

    /** Default constructor. */
    public GraalEntryPoint() {}

    /**
     * Executes the `from_openapi` command.
     * @param thread The isolate thread.
     * @return The exit code.
     */
    @CEntryPoint(name = "from_openapi")
    public static int from_openapi(IsolateThread thread) {
        try {
            String argsStr = System.getenv("CDD_ARGS");
            String cmdStr = System.getenv("CDD_COMMAND");
            if (argsStr == null || cmdStr == null) {
                Main.main(new String[0]);
                return 0;
            }
            List<String> argList = new ArrayList<>();
            argList.add(cmdStr);
            for (String arg : argsStr.split(" ")) {
                if (!arg.trim().isEmpty()) argList.add(arg.trim());
            }
            Main.main(argList.toArray(new String[0]));
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }
}
