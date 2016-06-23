package info.smart_tools.smartactors.core.iobserver;

import info.smart_tools.smartactors.core.iobserver.exception.ObserverExecuteException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ScopeException
 */
public class ObserverExecuteExceptionTest {
    @Test(expected = ObserverExecuteException.class)
    public void checkMessageMethod()
            throws ObserverExecuteException {
        String str = "test";
        ObserverExecuteException exception = new ObserverExecuteException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = ObserverExecuteException.class)
    public void checkCauseMethod()
            throws ObserverExecuteException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        ObserverExecuteException exception = new ObserverExecuteException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = ObserverExecuteException.class)
    public void checkMessageAndCauseMethod()
            throws ObserverExecuteException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        ObserverExecuteException exception = new ObserverExecuteException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
