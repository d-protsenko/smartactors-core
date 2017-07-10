package info.smart_tools.smartactors.notifier;

/**
 * The interface for notifier implementation
 */
public interface INotifier {

    /**
     * Sends the message and the exception.
     * @param message the message to send
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    void send(String message, Throwable error) throws Exception;

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    void send(IMessageGenerator generator, Throwable error) throws Exception;

}
