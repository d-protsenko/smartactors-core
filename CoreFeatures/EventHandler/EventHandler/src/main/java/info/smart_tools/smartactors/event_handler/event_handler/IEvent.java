package info.smart_tools.smartactors.event_handler.event_handler;

/**
 * The interface for wrapping any system events(messages, exceptions, etc) to a uniform form
 */
public interface IEvent {

    /**
     * returns the event type
     * @return the event type
     */
    String getType();

    /**
     * returns the event level
     * @return the event level
     */
    Integer getLevel();

    /**
     * returns the event initiator
     * @return the event initiator
     */
    String getInitiator();

    /**
     * returns the event message
     * @return the event message
     */
    String getMessage();

    /**
     * return the event in original form
     * @return the original event
     */
    Object getBody();

    /**
     * returns parameters which can be useful for event processing
     * @return the original parameters
     */
    Object getParams();
}
