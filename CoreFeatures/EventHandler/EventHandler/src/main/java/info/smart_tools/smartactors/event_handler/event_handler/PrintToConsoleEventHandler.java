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
    private IAction<IEvent> defaultExecutor;
    private Map<String, IAction<IEvent>> executors = new HashMap<>();

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToConsoleEventHandler}
     * @param defaultExecutor the default executor for event processing
     */
    public PrintToConsoleEventHandler(final String eventHandlerKey, final IAction<IEvent> defaultExecutor) {
        this.eventHandlerKey = eventHandlerKey;
        this.defaultExecutor = defaultExecutor;
    }

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToConsoleEventHandler}
     * @param defaultExecutor the default executor for event processing
     * @param executors initialization map of executors
     */
    public PrintToConsoleEventHandler(
            final String eventHandlerKey,
            final IAction<IEvent> defaultExecutor,
            final Map<Object, Object> executors
            ) {
        this.eventHandlerKey = eventHandlerKey;
        this.defaultExecutor = defaultExecutor;

        executors.forEach((type, executor) -> {
            try {
                this.addExecutor(type, executor);
            } catch (Exception e) {
                throw new RuntimeException(
                        "PrintToConsoleEventHandler: One of the executors cannot be casted to a specified type", e
                );
            }
        });
    }


    @Override
    public void handle(final IEvent event)
            throws EventHandlerException {
        if (event == null) {
            return;
        }
        String type = event.getBody().getClass().getCanonicalName();
        IAction<IEvent> exec = executors.getOrDefault(type, defaultExecutor);
        try {
            exec.execute(event);
        } catch (Exception e) {
            throw new EventHandlerException(
                    String.format("PrintToConsoleEventHandler: Event handler action '%s' throws exception.", type),
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
            throw new ExtendedEventHandlerException(
                    "PrintToConsoleEventHandler: Could not cast event type to String.", e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private IAction<IEvent> castExecutor(final Object executor)
            throws ExtendedEventHandlerException {
        try {
            return  (IAction<IEvent>) executor;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException(
                    "PrintToConsoleEventHandler: Could not cast executor to IAction<IEvent>.", e
            );
        }
    }
}
