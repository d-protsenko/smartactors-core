package info.smart_tools.smartactors.core.irequest_sender.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.core.irequest_sender.IRequestSender}
 */
public class RequestSenderException extends Exception {
    public RequestSenderException(String message) {
        super(message);
    }

    public RequestSenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestSenderException(Throwable cause) {
        super(cause);
    }
}
