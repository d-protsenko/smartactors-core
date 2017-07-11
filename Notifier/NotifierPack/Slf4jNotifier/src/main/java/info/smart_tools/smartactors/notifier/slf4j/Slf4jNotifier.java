package info.smart_tools.smartactors.notifier.slf4j;

import info.smart_tools.smartactors.notifier.IMessageGenerator;
import info.smart_tools.smartactors.notifier.INotifier;

/**
 * Notifier implementation which uses SLF4J to log the message.
 */
public class Slf4jNotifier implements INotifier {

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    public void send(final IMessageGenerator generator, final Throwable error) throws Exception {

    }

}
