package info.smart_tools.smartactors.core.icookies_extractor.exceptions;

public class CookieSettingException extends Exception {
    public CookieSettingException() {
        super();
    }

    public CookieSettingException(final String message) {
        super(message);
    }

    public CookieSettingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CookieSettingException(final Throwable cause) {
        super(cause);
    }

    public CookieSettingException(final String message, final Throwable cause, final boolean enableSuppression,
                                  final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
