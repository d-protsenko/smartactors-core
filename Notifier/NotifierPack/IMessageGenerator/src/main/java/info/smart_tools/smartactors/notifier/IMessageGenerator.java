package info.smart_tools.smartactors.notifier;

/**
 * Functional interface for a message generator
 */
@FunctionalInterface
public interface IMessageGenerator {

    /**
     * Generates a message for the notifier
     * @return the message to notify
     */
    String getMessage();

}
