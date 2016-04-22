package info.smart_tools.smartactors.core.class_storage.exception;

import info.smart_tools.smartactors.core.class_storage.IClassStorageContainer;

/**
 * Exception for error in {@link IClassStorageContainer} methods
 */
public class ClassStorageException extends Exception {

    /**
     * Default constructor
     */
    private ClassStorageException() {
        super();
    }

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public ClassStorageException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public ClassStorageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public ClassStorageException(final Throwable cause) {
        super(cause);
    }
}