package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

public interface IExtendedEventHandler {

    void addExecutor(Object key, Object executor) throws ExtendedEventHandlerException;

    Object removeExecutor(Object key) throws ExtendedEventHandlerException;
}
