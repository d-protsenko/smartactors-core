package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.exception;

/**
 *
 */
public class ConnectionListenerException extends Exception {
    /**
     * The constructor.
     *
     * @param message       the message
     * @param cause         the cause
     */
    public ConnectionListenerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor.
     *
     * @param message       the message
     */
    public ConnectionListenerException(final String message) {
        super(message);
    }

    /**
     * The constructor.
     *
     * @param cause         the cause
     */
    public ConnectionListenerException(final Throwable cause) {
        super(cause);
    }
}
