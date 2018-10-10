package info.smart_tools.smartactors.version_management.version_manager.exception;


/**
 * Exception for {@link info.smart_tools.smartactors.version_management.version_manager.VersionManager}
 */
public class VersionManagerException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public VersionManagerException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public VersionManagerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public VersionManagerException(final Throwable cause) {
        super(cause);
    }
}
