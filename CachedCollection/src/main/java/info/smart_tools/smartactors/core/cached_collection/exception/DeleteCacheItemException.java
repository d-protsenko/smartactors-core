package info.smart_tools.smartactors.core.cached_collection.exception;

public class DeleteCacheItemException extends Exception {

    public DeleteCacheItemException() {
    }

    public DeleteCacheItemException(final String message) {
        super(message);
    }

    public DeleteCacheItemException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DeleteCacheItemException(final Throwable cause) {
        super(cause);
    }
}
