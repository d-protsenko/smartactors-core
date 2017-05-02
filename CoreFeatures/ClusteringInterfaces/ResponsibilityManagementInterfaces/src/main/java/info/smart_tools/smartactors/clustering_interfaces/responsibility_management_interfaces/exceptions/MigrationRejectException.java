package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.IResponsible
 * responsible} object when responsibility migration is required but the object is not ready to perform it.
 */
public class MigrationRejectException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public MigrationRejectException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public MigrationRejectException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public MigrationRejectException(final Throwable cause) {
        super(cause);
    }
}
