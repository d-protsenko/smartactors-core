package info.smart_tools.smartactors.base.interfaces.transformation;

import info.smart_tools.smartactors.base.interfaces.transformation.exception.TransformationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransformationExceptionTest {

    @Test(expected = TransformationException.class)
    public void checkMessageMethod()
            throws TransformationException {
        String str = "test";
        TransformationException exception = new TransformationException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = TransformationException.class)
    public void checkCauseMethod()
            throws TransformationException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        TransformationException exception = new TransformationException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = TransformationException.class)
    public void checkMessageAndCauseMethod()
            throws TransformationException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        TransformationException exception = new TransformationException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
