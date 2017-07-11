package info.smart_tools.smartactors.notifier;

import info.smart_tools.smartactors.notifier.console.ConsoleNotifier;
import info.smart_tools.smartactors.notifier.console.ConsoleNotifierHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Test for Notifier service.
 */
public class NotifierConsoleTest {

    private ByteArrayOutputStream buffer;

    @Before
    public void setUp() {
        buffer = new ByteArrayOutputStream();
        ConsoleNotifierHelper.setConsoleStream(new PrintStream(buffer));
        ConsoleNotifier notifier = new ConsoleNotifier();
        Notifier.notifiers = new ArrayList<INotifier>();
        Notifier.addNotifier(notifier);
    }

    @Test
    public void testMessage() {
        Notifier.send("a message");
        assertEquals("a message\n", buffer.toString());
    }

    @Test
    public void testMessageGenerator() {
        Notifier.send(() -> "a message");
        assertEquals("a message\n", buffer.toString());
    }

    @Test
    public void testMessageWithException() {
        Notifier.send("a message", new Exception("an exception"));
        assertEquals("a message\n" +
                "\tjava.lang.Exception: an exception\n", buffer.toString());
    }

    @Test
    public void testMessageGeneratorWithException() {
        Notifier.send(() -> "a message", new Exception("an exception"));
        assertEquals("a message\n" +
                "\tjava.lang.Exception: an exception\n", buffer.toString());
    }

}
