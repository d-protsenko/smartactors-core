package info.smart_tools.smartactors.notifier;

import info.smart_tools.smartactors.notifier.console.ConsoleNotifier;
import info.smart_tools.smartactors.notifier.slf4j.Slf4jNotifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Entry point for system Notifier.
 * Use `Notifier.send()` instead of `System.out.println()` where possible.
 */
public final class Notifier {

    /**
     * List of notifiers in order of preference.
     */
    static List<INotifier> notifiers = new LinkedList<>();
    static {
        addNotifier(new ConsoleNotifier());
        addNotifier(new Slf4jNotifier());
    }

    private Notifier() {
        //private constructor to avoid instantiation
    }

    /**
     * Sends one text message.
     * @param message the message to send
     */
    public static void send(final String message) {
        send(message, null);
    }

    /**
     * Sends a message produced by the generator.
     * The generator is called only when the message can be sent.
     * @param generator the generator to take a message
     */
    public static void send(final IMessageGenerator generator) {
        send(generator, null);
    }

    /**
     * Sends the message and the exception.
     * @param message the message to send
     * @param error the exception where to get the details about an error
     */
    public static void send(final String message, final Throwable error) {
        send(() -> message, error);
    }

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     */
    public static void send(final IMessageGenerator generator, final Throwable error) {
        for (INotifier notifier : notifiers) {
            try {
                notifier.send(generator, error);
                return;
            } catch (Exception e) {
                // silence the exception occurred in notifier to try another notifier
            }
        }
    }

    /**
     * Adds a new notifier to be used as a preferable one.
     * @param notifier a new notifier
     */
    static void addNotifier(final INotifier notifier) {
        notifiers.add(0, notifier);
    }

}
