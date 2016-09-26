package info.smart_tools.smartactors.test.test_environment_handler.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.test.itest_runner.ITestRunner} method
 */
public class InvalidTestDescriptionException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public InvalidTestDescriptionException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public InvalidTestDescriptionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public InvalidTestDescriptionException(final Throwable cause) {
        super(cause);
    }
}
