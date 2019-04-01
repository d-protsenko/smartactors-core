package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

/**
 * The interface for extending instances of {@link IEventHandler}. An implementation of this
 * interface provide an ability to add and remove any actions to processing specific types of
 * {@link IEvent}.
 */
public interface IExtendedEventHandler {

    /**
     * registers given executor with given event key to the instance of {@link EventHandler}
     * @param eventType the event type
     * @param executor the executor
     * @throws ExtendedEventHandlerException should be thrown if any errors were occurring
     */
    void addExecutor(Object eventType, Object executor) throws ExtendedEventHandlerException;

    /**
     * unregisters an executor by given event key from the instance of {@link EventHandler}
     * @param eventType the event type
     * @return unregistered executor
     * @throws ExtendedEventHandlerException should be thrown if any errors were occurring
     */
    Object removeExecutor(Object eventType) throws ExtendedEventHandlerException;
}
