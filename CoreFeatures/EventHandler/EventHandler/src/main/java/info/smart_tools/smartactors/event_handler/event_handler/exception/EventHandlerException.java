package info.smart_tools.smartactors.event_handler.event_handler.exception;

import info.smart_tools.smartactors.event_handler.event_handler.IEvent;

/**
 * The checked exception that should be thrown if execution of an implementation
 * of the method {@link info.smart_tools.smartactors.event_handler.event_handler.IEventHandler#handle(IEvent)}
 * will be unexpectedly terminated by any reason
 */
public class EventHandlerException extends Exception {

    /**
     * Constructor with a specific error message as the argument
     * @param message the specific error message
     */
    public EventHandlerException(final String message) {
        super(message);
    }

    /**
     * Constructor with a specific error message and a specific cause as arguments
     * @param message the specific error message
     * @param cause the specific cause
     */
    public EventHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with a specific cause as the argument
     * @param cause the specific cause
     */
    public EventHandlerException(final Throwable cause) {
        super(cause);
    }
}
