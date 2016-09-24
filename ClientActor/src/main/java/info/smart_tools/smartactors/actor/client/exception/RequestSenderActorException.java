package info.smart_tools.smartactors.actor.client.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.actor.client.ClientActor}
 */
public class RequestSenderActorException extends Exception {

    public RequestSenderActorException(String message) {
        super(message);
    }

    public RequestSenderActorException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestSenderActorException(Throwable cause) {
        super(cause);
    }
}
