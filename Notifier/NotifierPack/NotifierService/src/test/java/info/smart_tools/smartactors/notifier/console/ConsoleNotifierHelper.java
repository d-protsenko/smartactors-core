package info.smart_tools.smartactors.notifier.console;

import java.io.PrintStream;

/**
 * A helper class to override ConsoleNotifier internals.
 */
public class ConsoleNotifierHelper {

    public static void setConsoleStream(final PrintStream stream) {
        ConsoleNotifier.console = stream;
    }

}
