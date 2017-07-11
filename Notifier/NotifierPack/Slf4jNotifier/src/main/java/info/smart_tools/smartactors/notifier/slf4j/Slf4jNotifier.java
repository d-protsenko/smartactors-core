package info.smart_tools.smartactors.notifier.slf4j;

import info.smart_tools.smartactors.notifier.IMessageGenerator;
import info.smart_tools.smartactors.notifier.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifier implementation which uses SLF4J to log the message.
 */
public class Slf4jNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger("info.smart_tools.smartactors.notifier");

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     * @throws Exception if cannot send
     */
    public void send(final IMessageGenerator generator, final Throwable error) throws Exception {
        // TODO: Use Thread.currentThread().getStackTrace() to identify the caller and find appropriate logger?
        // TODO: Find the actor and chain where the message come from?
        if (logger.isInfoEnabled()) {
            String message;
            if (generator == null) {
                message = "[null generator]";
            } else {
                message = generator.getMessage();
                if (message == null) {
                    message = "[null message]";
                }
            }
            logger.info(message, error);
        }
    }

}
