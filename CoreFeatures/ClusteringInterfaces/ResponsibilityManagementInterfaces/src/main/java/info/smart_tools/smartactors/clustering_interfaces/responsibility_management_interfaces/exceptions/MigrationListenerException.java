package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.exceptions;

/**
 * Exception thrown by {@link
 * info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.IIncomingMigrationListener incomming} or
 * {@link info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.IOutgoingMigrationListener outgoing}
 * migration listener when error occurs.
 */
public class MigrationListenerException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public MigrationListenerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public MigrationListenerException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public MigrationListenerException(final Throwable cause) {
        super(cause);
    }
}
