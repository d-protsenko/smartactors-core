package info.smart_tools.smartactors.actors.create_user;

import info.smart_tools.smartactors.actors.create_user.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.create_user.wrapper.MessageWrapper;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Actor for creating user
 */
public class CreateUserActor {
    private ICachedCollection collection;

    /**
     * Constructor
     * @param params the actors params
     * @throws InvalidArgumentException
     */
    public CreateUserActor(final ActorParams params) throws InvalidArgumentException {
        try {
            collection = IOC.resolve(
                    Keys.getOrAdd(ICachedCollection.class.toString()),
                    params.getCollectionName(),
                    params.getCollectionKey());
        } catch (Exception e) {
            throw new InvalidArgumentException("Failed to initialize collection", e);
        }
    }

    /**
     * Create a new user in collection
     * @param message the message
     * @throws TaskExecutionException
     */
    public void create(final MessageWrapper message) throws TaskExecutionException {
        try {
            IObject user = message.getUser();
            collection.upsert(user);
        } catch (ReadValueException | UpsertCacheItemException e) {
            throw new TaskExecutionException("Failed to create new user", e);
        }
    }
}
