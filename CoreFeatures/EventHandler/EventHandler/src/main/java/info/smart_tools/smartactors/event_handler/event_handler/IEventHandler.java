package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;

public interface IEventHandler {

    void handle(IEvent event) throws EventHandlerException;

    String getEventHandlerKey();
}
