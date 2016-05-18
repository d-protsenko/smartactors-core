package info.smart_tools.smartactors.core.iplugin;

import info.smart_tools.smartactors.core.iplugin.exception.PluginLoadingException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for PluginLoadingException
 */
public class PluginLoadingExceptionTest {
    @Test(expected = PluginLoadingException.class)
    public void checkMessageMethod()
            throws PluginLoadingException {
        String str = "test";
        PluginLoadingException exception = new PluginLoadingException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = PluginLoadingException.class)
    public void checkCauseMethod()
            throws PluginLoadingException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        PluginLoadingException exception = new PluginLoadingException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = PluginLoadingException.class)
    public void checkMessageAndCauseMethod()
            throws PluginLoadingException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        PluginLoadingException exception = new PluginLoadingException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
