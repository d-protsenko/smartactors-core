package info.smart_tools.smartactors.event_handler.event_handler;

final public class EventHandler {

    private static IEventHandlerContainer container;

    static {
        container = new EventHandlerContainer();
    }

    /**
     * Default private constructor
     */
    private EventHandler(){
    }

    public static void handle(final IEvent event) {
        container.handle(event);
    }

    public static IEventHandlerContainer getEventHandlerContainer() {

        return container;
    }
}
