package info.smart_tools.smartactors.event_handler.event_handler;

public interface IEvent {

    String getType();

    Integer getLevel();

    String getInitiator();

    String getMessage();

    Object getBody();

    Object getParams();
}
