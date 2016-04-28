package info.smart_tools.smartactors.core.iresolve_dependency_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for StrategyFactoryException
 */
public class StrategyFactoryExceptionTest {

    @Test(expected = StrategyFactoryException.class)
    public void checkMessageMethod()
            throws StrategyFactoryException {
        String str = "test";
        StrategyFactoryException exception = new StrategyFactoryException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test (expected = StrategyFactoryException.class)
    public void checkCauseMethod()
            throws StrategyFactoryException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        StrategyFactoryException exception = new StrategyFactoryException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = StrategyFactoryException.class)
    public void checkMessageAndCauseMethod()
            throws StrategyFactoryException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        StrategyFactoryException exception = new StrategyFactoryException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
