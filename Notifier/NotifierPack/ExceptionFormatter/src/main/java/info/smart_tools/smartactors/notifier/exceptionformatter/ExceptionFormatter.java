package info.smart_tools.smartactors.notifier.exceptionformatter;

import info.smart_tools.smartactors.notifier.IMessageGenerator;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Generates the notification message from the original message and the exception.
 * It prints all causes of the exception line by line,
 * but not the whole stacktrace.
 */
public class ExceptionFormatter implements IMessageGenerator {

    private final IMessageGenerator generator;
    private final Throwable error;

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
        StringBuilder result = new StringBuilder();
        if (generator == null) {
            result.append("[null generator]");
        } else {
            String message = generator.getMessage();
            if (message == null) {
                result.append("[null message]");
            } else {
                result.append(message);
            }
        }
        if (error != null) {
            Set<Throwable> visitedExceptions = Collections.newSetFromMap(new IdentityHashMap<>());
            Throwable cause = error;
            while (cause != null) {
                if (visitedExceptions.contains(cause)) {
                    result.append("\n\t[circular reference]: ");
                    result.append(cause.toString());
                    break;
                }
                visitedExceptions.add(cause);
                result.append("\n\t");
                result.append(cause.toString());
                cause = cause.getCause();
            }
        }
        result.append("\n");
        return result.toString();
    }

}
