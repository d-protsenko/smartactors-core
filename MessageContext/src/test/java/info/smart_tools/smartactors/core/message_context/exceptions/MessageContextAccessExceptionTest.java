package info.smart_tools.smartactors.core.message_context.exceptions;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link MessageContextAccessException}.
 */
public class MessageContextAccessExceptionTest {
    @Test(expected = MessageContextAccessException.class)
    public void checkMessageMethod()
            throws MessageContextAccessException {
        String str = "test";
        MessageContextAccessException exception = new MessageContextAccessException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = MessageContextAccessException.class)
    public void checkCauseMethod()
            throws MessageContextAccessException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        MessageContextAccessException exception = new MessageContextAccessException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = MessageContextAccessException.class)
    public void checkMessageAndCauseMethod()
            throws MessageContextAccessException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        MessageContextAccessException exception = new MessageContextAccessException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}