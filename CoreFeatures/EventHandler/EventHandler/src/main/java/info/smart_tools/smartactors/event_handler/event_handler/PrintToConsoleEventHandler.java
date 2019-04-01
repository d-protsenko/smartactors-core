package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IEventHandler} which output data of {@link IEvent} to system console.
 */
public class PrintToConsoleEventHandler implements IEventHandler, IExtendedEventHandler {

    private String eventHandlerKey;
    private Map<String, IAction<IEvent>> executors = new HashMap<String, IAction<IEvent>>() {{
        put(
                Exception.class.getCanonicalName(), (event) -> {
                    ((Exception) event.getBody()).printStackTrace();
                }
        );
    }};

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToConsoleEventHandler}
     */
    public PrintToConsoleEventHandler(final String eventHandlerKey) {
        this.eventHandlerKey = eventHandlerKey;
    }

    @Override
    public void handle(final IEvent event) throws EventHandlerException {
        if (event == null) {
            return;
        }
        String type = event.getType();
        IAction<IEvent> exec = executors.getOrDefault(event.getType(), (et) -> {
            System.out.println(event.toString());
        });
        try {
            exec.execute(event);
        } catch (Exception e) {
            throw new EventHandlerException(
                    String.format("Event handler action '%s' throws exception.", type),
                    e
            );
        }
    }

    @Override
    public String getEventHandlerKey() {

        return this.eventHandlerKey;
    }

    @Override
    public void addExecutor(final Object eventType, final Object executor)
            throws ExtendedEventHandlerException {
        String castedEventType = castEventType(eventType);
        IAction<IEvent> castedExecutor = castExecutor(executor);

        executors.put(castedEventType, castedExecutor);
    }

    @Override
    public Object removeExecutor(final Object eventType) throws ExtendedEventHandlerException {
        String castedEventType = castEventType(eventType);

        return executors.remove(castedEventType);
    }

    private String castEventType(final Object eventType)
            throws ExtendedEventHandlerException {
        try {
            return  (String) eventType;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast event type to String.");
        }
    }

    @SuppressWarnings("unchecked")
    private IAction<IEvent> castExecutor(final Object executor)
            throws ExtendedEventHandlerException {
        try {
            return  (IAction<IEvent>) executor;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast executor to IAction<IEvent>.");
        }
    }
}
