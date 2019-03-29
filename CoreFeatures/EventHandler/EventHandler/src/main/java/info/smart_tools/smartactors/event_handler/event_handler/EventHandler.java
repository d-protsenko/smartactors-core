package info.smart_tools.smartactors.event_handler.event_handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final public class EventHandler {

    /**
     * Default private constructor
     */
    private EventHandler(){
    }

    private static LinkedList<IEventHandler> handlers;

    static {
        handlers = new LinkedList<IEventHandler>(
                Arrays.asList(
                        new PrintToFileEventHandler("fileLogger"),
                        new PrintToConsoleEventHandler("consoleLogger")
                )
        );
    }

    public static void handle(final IEvent event) {
        List<IEvent> events = new ArrayList<>();
        events.add(event);
        for (IEventHandler handler : handlers) {
            try {
                for (IEvent e: events) {
                    handler.handle(e);
                }
                break;
            } catch (Exception e) {
                events = new ArrayList<>();
                events.add(event);
                events.add(
                    Event
                        .builder()
                        .message(
                            String.format(
                                "Exception on executing 'handle' method of '%s' event handler.", handler.getName()
                            )
                        )
                        .body(e)
                        .build()
                );
            }
        }
    }

    public static void register(final IEventHandler handler) {
        if (handler != null) {
            handlers.push(handler);
        }
    }

    public static IEventHandler unregister() {
        if (handlers.size() > 0) {
            return handlers.pollFirst();
        }

        return null;
    }
}
