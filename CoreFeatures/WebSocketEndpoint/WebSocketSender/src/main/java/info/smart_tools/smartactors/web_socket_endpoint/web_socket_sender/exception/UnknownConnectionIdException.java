package info.smart_tools.smartactors.web_socket_endpoint.web_socket_sender.exception;

/**
 * Exception thrown by object sending message to web-socket connection when a connection identifier is no longer registered (this usually
 * means that a connection is closed).
 */
public class UnknownConnectionIdException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public UnknownConnectionIdException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public UnknownConnectionIdException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public UnknownConnectionIdException(final Throwable cause) {
        super(cause);
    }
}
