package info.smart_tools.smartactors.notifier;

import info.smart_tools.smartactors.notifier.slf4j.Slf4jNotifier;

/**
 * Entry point for system Notifier.
 * Use `Notifier.send()` instead `System.out.println()` where possible.
 */
public final class Notifier {

    private static INotifier[] notifiers = {
            new Slf4jNotifier()
    };

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
        
    }

    /**
     * Sends a message produced by the generator and the exception.
     * @param generator the generator to take a message
     * @param error the exception where to get the details about an error
     */
    public static void send(final IMessageGenerator generator, final Throwable error) {

    }

}
