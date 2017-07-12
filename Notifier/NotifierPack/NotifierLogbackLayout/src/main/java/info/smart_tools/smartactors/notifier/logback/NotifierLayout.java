package info.smart_tools.smartactors.notifier.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.LayoutBase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

/**
 * A log message layout for Logback, specific to the Notifier.
 * This class depends on logback-classic,
 * you should avoid to use it when you have another slf4j backend.
 */
public class NotifierLayout extends LayoutBase<ILoggingEvent> {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .withLocale(Locale.US)
                    .withZone(ZoneId.systemDefault());

    @Override
    public String doLayout(final ILoggingEvent event) {
        StringBuilder result = new StringBuilder();
        TIMESTAMP_FORMAT.formatTo(Instant.ofEpochMilli(event.getTimeStamp()), result);
        result.append(" [");
        result.append(event.getThreadName());
        result.append("] ");
        String message = event.getFormattedMessage();
        if (message == null) {
            result.append("[null message]");
        } else {
            result.append(message);
        }
        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            Set<IThrowableProxy> visitedExceptions = Collections.newSetFromMap(new IdentityHashMap<>());
            IThrowableProxy cause = throwable;
            while (cause != null) {
                if (visitedExceptions.contains(cause)) {
                    result.append("\n\t[circular reference]: ");
                    result.append(cause.getClassName());
                    result.append(": ");
                    result.append(cause.getMessage());
                    break;
                }
                visitedExceptions.add(cause);
                result.append("\n\t");
                result.append(cause.getClassName());
                result.append(": ");
                result.append(cause.getMessage());
                cause = cause.getCause();
            }
        }
        return result.toString();
    }

}
