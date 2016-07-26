package info.smart_tools.smartactors.core.ssl_context_provider.exceptions;

/**
 * Exception for {@link info.smart_tools.smartactors.core.ssl_context_provider.SSLContextProvider}
 */
public class SSLContextProviderException extends Exception {
    /**
     * Constructor with specific error message
     * @param message specific error message
     */
    public SSLContextProviderException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public SSLContextProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as arguments
     * @param cause specific cause
     */
    public SSLContextProviderException(final Throwable cause) {
        super(cause);
    }
}
