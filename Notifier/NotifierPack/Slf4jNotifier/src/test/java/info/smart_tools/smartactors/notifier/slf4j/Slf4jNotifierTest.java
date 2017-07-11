package info.smart_tools.smartactors.notifier.slf4j;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Test for Slf4jNotifier.
 */
public class Slf4jNotifierTest {

    private Slf4jNotifier notifier;
    private Appender mockAppender;

    @Before
    public void setUp() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
                LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);
        notifier = new Slf4jNotifier();
    }

    private ILoggingEvent getLoggingEvent() {
        ArgumentCaptor<LoggingEvent> captor = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(mockAppender).doAppend(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testNullMessage() throws Exception {
        notifier.send(() -> null, null);
        ILoggingEvent event = getLoggingEvent();
        assertEquals("[null message]", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    public void testNullGenerator() throws Exception {
        notifier.send(null, null);
        ILoggingEvent event = getLoggingEvent();
        assertEquals("[null generator]", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    public void testOnlyMessage() throws Exception {
        notifier.send(() -> "a message", null);
        ILoggingEvent event = getLoggingEvent();
        assertEquals("a message", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    public void testSimpleException() throws Exception {
        notifier.send(() -> "a message",
                new Exception("an exception"));
        ILoggingEvent event = getLoggingEvent();
        assertEquals("a message", event.getFormattedMessage());
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        assertEquals("java.lang.Exception", throwableProxy.getClassName());
        assertEquals("an exception", throwableProxy.getMessage());
    }

    @Test
    public void testNullMessageWithException() throws Exception {
        notifier.send(() -> null,
                new Exception("an exception"));
        ILoggingEvent event = getLoggingEvent();
        assertEquals("[null message]", event.getFormattedMessage());
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        assertEquals("java.lang.Exception", throwableProxy.getClassName());
        assertEquals("an exception", throwableProxy.getMessage());
    }

}
