package info.smart_tools.smartactors.notifier.exceptionformatter;

import info.smart_tools.smartactors.notifier.IMessageGenerator;

/**
 * Generates the notification message from the original message and the exception.
 * It prints all causes of the exception line by line,
 * but not the whole stacktrace.
 */
public class ExceptionFormatter implements IMessageGenerator {

    private final IMessageGenerator generator;
    private final Throwable error;

    /**
     * Creates the formatter for the message and the error.
     * @param message the message to prepend the error
     * @param error the error to print line by line
     */
    public ExceptionFormatter(final String message, final Throwable error) {
        this(() -> message, error);
    }

    /**
     * Creates the formatter for the message generator and the error.
     * @param generator the message to prepend the error
     * @param error the error to print line by line
     */
    public ExceptionFormatter(final IMessageGenerator generator, final Throwable error) {
        this.generator = generator;
        this.error = error;
    }

    @Override
    public String getMessage() {
        StringBuilder result = new StringBuilder(generator.getMessage());
        Throwable cause = error;
        while (cause != null) {     // TODO: avoid infinite loop when cause is repeated
            result.append("\n\t");
            result.append(cause.toString());
            cause = cause.getCause();
        }
        result.append("\n");
        return result.toString();
    }

}
