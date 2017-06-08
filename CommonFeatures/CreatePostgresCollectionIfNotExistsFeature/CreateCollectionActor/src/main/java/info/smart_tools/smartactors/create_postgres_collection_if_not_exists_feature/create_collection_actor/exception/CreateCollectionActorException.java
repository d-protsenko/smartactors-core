package info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_actor.exception;

public class CreateCollectionActorException extends Exception {

    public CreateCollectionActorException(final String message) {
        super(message);
    }

    public CreateCollectionActorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CreateCollectionActorException(final Throwable cause) {
        super(cause);
    }
}
