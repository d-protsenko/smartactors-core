package info.smart_tools.smartactors.core.cached_collection.exception;

public class GetCacheItemException extends Exception {

    public GetCacheItemException() {
    }

    public GetCacheItemException(final String message) {
        super(message);
    }

    public GetCacheItemException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public GetCacheItemException(final Throwable cause) {
        super(cause);
    }
}
