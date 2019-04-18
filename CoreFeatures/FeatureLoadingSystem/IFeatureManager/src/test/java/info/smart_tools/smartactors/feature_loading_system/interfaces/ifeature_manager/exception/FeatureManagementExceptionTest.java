package info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link FeatureManagementException}.
 */
public class FeatureManagementExceptionTest {
    @Test(expected = FeatureManagementException.class)
    public void checkMessageMethod()
            throws FeatureManagementException {
        String str = "test";
        FeatureManagementException exception = new FeatureManagementException(str);
        assertEquals(exception.getMessage(), str);
        throw exception;
    }

    @Test(expected = FeatureManagementException.class)
    public void checkCauseMethod()
            throws FeatureManagementException {
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        FeatureManagementException exception = new FeatureManagementException(cause);
        assertEquals(cause, exception.getCause());
        throw exception;
    }

    @Test (expected = FeatureManagementException.class)
    public void checkMessageAndCauseMethod()
            throws FeatureManagementException {
        String str = "test";
        String internalMessage = "Internal message";
        Throwable cause = new Throwable(internalMessage);
        FeatureManagementException exception = new FeatureManagementException(str, cause);
        assertEquals(exception.getMessage(), str);
        assertEquals(exception.getCause(), cause);
        throw exception;
    }
}
