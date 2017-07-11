package info.smart_tools.smartactors.notifier.console;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Test for ConsoleNotifier.
 */
public class ConsoleNotifierTest {

    private ByteArrayOutputStream buffer;
    private ConsoleNotifier notifier;

    @Before
    public void setUp() {
        buffer = new ByteArrayOutputStream();
        ConsoleNotifier.console = new PrintStream(buffer);
        notifier = new ConsoleNotifier();
    }

    @Test
    public void testCorrectOutput() throws Exception {
        notifier.send(() -> "a message", new Exception("an exception"));
        assertEquals("a message\n" +
                "\tjava.lang.Exception: an exception\n",
                buffer.toString());
    }

}
