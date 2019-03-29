package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

import java.util.HashMap;
import java.util.Map;

public class PrintToConsoleEventHandler implements IEventHandler, IExtendedEventHandler {

    private String name;
    private Map<String, IAction<IEvent>> executors = new HashMap<String, IAction<IEvent>>() {{
        put(
                Exception.class.getCanonicalName(), (event) -> {
                    ((Exception) event.getBody()).printStackTrace();
                }
        );
    }};

    public PrintToConsoleEventHandler(final String name) {
        this.name = name;
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
    public String getName() {
        return this.name;
    }

    @Override
    public void addExecutor(final Object key, final Object executor)
            throws ExtendedEventHandlerException {
        String castedKey = castKey(key);
        IAction<IEvent> castedExecutor = castExecutor(executor);

        executors.put(castedKey, castedExecutor);
    }

    @Override
    public Object removeExecutor(final Object key) throws ExtendedEventHandlerException {
        String castedKey = castKey(key);

        return executors.remove(castedKey);
    }

    private String castKey(final Object key)
            throws ExtendedEventHandlerException {
        try {
            return  (String) key;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast key to String.");
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
