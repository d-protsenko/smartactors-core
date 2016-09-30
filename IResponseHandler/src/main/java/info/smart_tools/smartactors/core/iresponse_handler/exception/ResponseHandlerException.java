package info.smart_tools.smartactors.core.iresponse_handler.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.core.iresponse_handler.IResponseHandler}
 */
public class ResponseHandlerException extends Exception {

    public ResponseHandlerException(final String message) {
        super(message);
    }

    public ResponseHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ResponseHandlerException(final Throwable cause) {
        super(cause);
    }
}
