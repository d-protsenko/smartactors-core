package info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver_interfaces.exceptions;

import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver_interfaces.IChildDeletionCheckStrategy;

/**
 * Exception thrown by {@link
 * IChildDeletionCheckStrategy} when it cannot perform
 * check because of some error.
 */
public class DeletionCheckException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public DeletionCheckException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public DeletionCheckException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public DeletionCheckException(final Throwable cause) {
        super(cause);
    }
}
