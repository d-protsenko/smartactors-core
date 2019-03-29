package info.smart_tools.smartactors.event_handler.event_handler.exception;

public class ExtendedEventHandlerException extends Exception {

    public ExtendedEventHandlerException(final String message) {
        super(message);
    }

    public ExtendedEventHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExtendedEventHandlerException(final Throwable cause) {
        super(cause);
    }
}
