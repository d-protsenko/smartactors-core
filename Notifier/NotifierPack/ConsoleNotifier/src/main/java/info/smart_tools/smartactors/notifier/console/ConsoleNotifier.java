package info.smart_tools.smartactors.notifier.console;

import info.smart_tools.smartactors.notifier.IMessageGenerator;
import info.smart_tools.smartactors.notifier.INotifier;

/**
 * Notifier implementation which just prints to System.err.
 */
public class ConsoleNotifier implements INotifier {

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    public void send(final IMessageGenerator generator, final Throwable error) throws Exception {

    }

}
