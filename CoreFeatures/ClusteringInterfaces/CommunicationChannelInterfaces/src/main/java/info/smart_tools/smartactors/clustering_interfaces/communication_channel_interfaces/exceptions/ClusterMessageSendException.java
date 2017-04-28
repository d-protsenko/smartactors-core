package info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.ICommunicationChannel
 * communication channel} methods sending messages to cluster members when any error occurs sending a message.
 */
public class ClusterMessageSendException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public ClusterMessageSendException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public ClusterMessageSendException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public ClusterMessageSendException(final Throwable cause) {
        super(cause);
    }
}
