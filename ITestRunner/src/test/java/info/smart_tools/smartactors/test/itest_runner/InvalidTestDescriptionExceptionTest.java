package info.smart_tools.smartactors.test.itest_runner;

import info.smart_tools.smartactors.test.itest_runner.exception.InvalidTestDescriptionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link InvalidTestDescriptionException}
 */
public class InvalidTestDescriptionExceptionTest {

    @Test(expected = InvalidTestDescriptionException.class)
    public void checkMessageMethod()
            throws InvalidTestDescriptionException {
        String str = "test";
        InvalidTestDescriptionException exception = new InvalidTestDescriptionException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test (expected = InvalidTestDescriptionException.class)
    public void checkCauseMethod()
            throws InvalidTestDescriptionException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        InvalidTestDescriptionException exception = new InvalidTestDescriptionException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = InvalidTestDescriptionException.class)
    public void checkMessageAndCauseMethod()
            throws InvalidTestDescriptionException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        InvalidTestDescriptionException exception = new InvalidTestDescriptionException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
