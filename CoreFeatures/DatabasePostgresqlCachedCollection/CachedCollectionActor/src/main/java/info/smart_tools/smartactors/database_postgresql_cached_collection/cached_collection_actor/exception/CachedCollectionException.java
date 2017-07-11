package info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor.exception;

public class CachedCollectionException extends Exception {
    public CachedCollectionException() {
    }

    public CachedCollectionException(String s) {
        super(s);
    }

    public CachedCollectionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CachedCollectionException(Throwable throwable) {
        super(throwable);
    }
}
