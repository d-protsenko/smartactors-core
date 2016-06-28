package info.smart_tools.smartactors.core.cached_collection.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.cached_collection.ICachedCollection} upsert method
 */
public class UpsertCacheItemException extends Exception {

    public UpsertCacheItemException() {
    }

    public UpsertCacheItemException(final String message) {
        super(message);
    }

    public UpsertCacheItemException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UpsertCacheItemException(final Throwable cause) {
        super(cause);
    }
}
