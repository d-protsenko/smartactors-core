package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IActionTwoArgs;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Class-registrar. Register instance of {@link PrintToFileEventHandler} to the {@link EventHandler}
 */
public final class EventHandlerWithPrintToFileEventHandler {

    private EventHandlerWithPrintToFileEventHandler() {
    }

    static {
        IEventHandlerContainer container = EventHandler.getEventHandlerContainer();
        ((IExtendedEventHandlerContainer) container).register(
                new PrintToFileEventHandler(
                        "fileLogger",
                        null,
                        (event, writer) -> writer.println(event.getBody().toString()),
                        new HashMap<Object, Object>() {{
                            put(
                                    Exception.class.getCanonicalName(),
                                    (IActionTwoArgs<IEvent, PrintWriter>) (event, writer) -> ((Exception) event.getBody()).printStackTrace(writer)
                            );
                        }}
                )
        );
    }
}
