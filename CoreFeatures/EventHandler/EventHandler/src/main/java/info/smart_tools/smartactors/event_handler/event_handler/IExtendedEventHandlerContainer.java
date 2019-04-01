package info.smart_tools.smartactors.event_handler.event_handler;

public interface IExtendedEventHandlerContainer {

    void register(IEventHandler eventHandler);

    IEventHandler unregister(String eventHandlerKey);
}
