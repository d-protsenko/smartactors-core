package info.smart_tools.smartactors.base.interfaces.iresolution_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ResolutionStrategyException
 */
public class ResolutionStrategyExceptionTest {

    @Test(expected = ResolutionStrategyException.class)
    public void checkMessageMethod()
            throws ResolutionStrategyException {
        String str = "test";
        ResolutionStrategyException exception = new ResolutionStrategyException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test (expected = ResolutionStrategyException.class)
    public void checkCauseMethod()
            throws ResolutionStrategyException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        ResolutionStrategyException exception = new ResolutionStrategyException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = ResolutionStrategyException.class)
    public void checkMessageAndCauseMethod()
            throws ResolutionStrategyException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        ResolutionStrategyException exception = new ResolutionStrategyException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
