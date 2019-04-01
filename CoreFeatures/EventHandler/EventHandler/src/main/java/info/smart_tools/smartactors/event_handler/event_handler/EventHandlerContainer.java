package info.smart_tools.smartactors.event_handler.event_handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EventHandlerContainer implements IEventHandlerContainer, IExtendedEventHandlerContainer {

    private LinkedList<IEventHandler> handlers = new LinkedList<IEventHandler>(
            Arrays.asList(
                    new PrintToFileEventHandler("fileLogger"),
                    new PrintToConsoleEventHandler("consoleLogger")
            )
    );

    @Override
    public void handle(final IEvent event) {
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
                                "Exception on executing 'handle' method of '%s' event handler.",
                                    handler.getEventHandlerKey()
                            )
                        )
                        .body(e)
                        .build()
                );
            }
        }
    }

    @Override
    public void register(final IEventHandler eventHandler) {
        if (eventHandler != null) {
            handlers.push(eventHandler);
        }
    }

    @Override
    public IEventHandler unregister(final String eventHandlerKey) {
        if (null != eventHandlerKey) {
            handlers.removeIf((IEventHandler handler) -> handler.getEventHandlerKey().equals(eventHandlerKey));
        } else {
            if (handlers.size() > 0) {
                return handlers.pollFirst();
            }
        }

        return null;
    }
}
