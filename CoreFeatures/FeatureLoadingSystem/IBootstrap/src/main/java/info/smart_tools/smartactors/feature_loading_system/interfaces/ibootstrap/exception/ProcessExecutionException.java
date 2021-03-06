package info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.ibootstrap.IBootstrapItem} process method
 */
public class ProcessExecutionException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public ProcessExecutionException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public ProcessExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public ProcessExecutionException(final Throwable cause) {
        super(cause);
    }
}
