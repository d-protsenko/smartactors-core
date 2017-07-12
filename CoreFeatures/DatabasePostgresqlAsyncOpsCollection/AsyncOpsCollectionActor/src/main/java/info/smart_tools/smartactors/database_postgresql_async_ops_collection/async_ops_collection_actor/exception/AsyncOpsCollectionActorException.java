package info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.exception;

public class AsyncOpsCollectionActorException extends Exception {
    public AsyncOpsCollectionActorException() {
    }

    public AsyncOpsCollectionActorException(String s) {
        super(s);
    }

    public AsyncOpsCollectionActorException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AsyncOpsCollectionActorException(Throwable throwable) {
        super(throwable);
    }
}
