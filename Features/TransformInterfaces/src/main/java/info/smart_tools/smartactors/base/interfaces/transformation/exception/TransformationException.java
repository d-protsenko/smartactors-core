package info.smart_tools.smartactors.base.interfaces.transformation.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.base.interfaces.transformation.ITransformable} methods
 */

public class TransformationException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public TransformationException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public TransformationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public TransformationException(final Throwable cause) {
        super(cause);
    }
}
