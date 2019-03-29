package info.smart_tools.smartactors.event_handler.event_handler.exception;

public class EventHandlerException extends Exception {

    public EventHandlerException(final String message) {
        super(message);
    }

    public EventHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EventHandlerException(final Throwable cause) {
        super(cause);
    }
}
