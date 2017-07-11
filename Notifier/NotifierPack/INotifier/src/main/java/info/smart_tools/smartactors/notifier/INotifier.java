package info.smart_tools.smartactors.notifier;

/**
 * The interface for notifier implementation
 */
public interface INotifier {

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    void send(IMessageGenerator generator, Throwable error) throws Exception;

}
