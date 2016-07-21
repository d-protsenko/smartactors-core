package info.smart_tools.smartactors.core.iheaders_extractor.exceptions;

public class HeadersSetterException extends Exception {
    public HeadersSetterException() {
        super();
    }

    public HeadersSetterException(String message) {
        super(message);
    }

    public HeadersSetterException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeadersSetterException(Throwable cause) {
        super(cause);
    }

    public HeadersSetterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
