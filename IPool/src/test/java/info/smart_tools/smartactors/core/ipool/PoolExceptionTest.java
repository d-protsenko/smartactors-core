package info.smart_tools.smartactors.core.ipool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import info.smart_tools.smartactors.core.ipool.exception.PoolException;

/**
 * Tests for PoolException
 */
public class PoolExceptionTest {
    @Test(expected = PoolException.class)
    public void checkMessageMethod() throws PoolException {
        String str = "test";
        PoolException exception = new PoolException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = PoolException.class)
    public void checkCauseMethod()
            throws PoolException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        PoolException exception = new PoolException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test(expected = PoolException.class)
    public void checkMessageAndCauseMethod()
            throws PoolException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        PoolException exception = new PoolException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
