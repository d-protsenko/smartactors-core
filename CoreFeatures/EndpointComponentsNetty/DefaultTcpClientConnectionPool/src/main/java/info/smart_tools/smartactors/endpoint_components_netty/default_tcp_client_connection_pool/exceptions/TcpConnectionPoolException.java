package info.smart_tools.smartactors.endpoint_components_netty.default_tcp_client_connection_pool.exceptions;

public class TcpConnectionPoolException extends Exception {
    /**
     * The constructor.
     *
     * @param message exception message
     * @param cause   cause
     */
    public TcpConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }
}