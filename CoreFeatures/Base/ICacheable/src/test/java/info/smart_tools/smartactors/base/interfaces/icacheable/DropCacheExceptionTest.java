package info.smart_tools.smartactors.base.interfaces.icacheable;

import info.smart_tools.smartactors.base.interfaces.icacheable.exception.DropCacheException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DropCacheExceptionTest {

    @Test(expected = DropCacheException.class)
    public void checkMessageMethod()
            throws DropCacheException {
        String str = "test";
        DropCacheException exception = new DropCacheException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = DropCacheException.class)
    public void checkCauseMethod()
            throws DropCacheException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        DropCacheException exception = new DropCacheException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = DropCacheException.class)
    public void checkMessageAndCauseMethod()
            throws DropCacheException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        DropCacheException exception = new DropCacheException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
