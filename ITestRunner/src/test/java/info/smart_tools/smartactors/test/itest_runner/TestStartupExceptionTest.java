package info.smart_tools.smartactors.test.itest_runner;

import info.smart_tools.smartactors.test.itest_runner.exception.TestStartupException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TestStartupException}
 */
public class TestStartupExceptionTest {

    @Test(expected = TestStartupException.class)
    public void checkMessageMethod()
            throws TestStartupException {
        String str = "test";
        TestStartupException exception = new TestStartupException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test (expected = TestStartupException.class)
    public void checkCauseMethod()
            throws TestStartupException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        TestStartupException exception = new TestStartupException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = TestStartupException.class)
    public void checkMessageAndCauseMethod()
            throws TestStartupException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        TestStartupException exception = new TestStartupException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
