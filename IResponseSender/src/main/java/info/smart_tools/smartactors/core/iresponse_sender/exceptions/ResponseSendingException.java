package info.smart_tools.smartactors.core.iresponse_sender.exceptions;


public class ResponseSendingException extends Exception {
    public ResponseSendingException() {
    }

    public ResponseSendingException(final String message) {
        super(message);
    }

    public ResponseSendingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ResponseSendingException(final Throwable cause) {
        super(cause);
    }

    public ResponseSendingException(final String message, final Throwable cause,
                                    final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
