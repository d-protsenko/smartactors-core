package info.smart_tools.smartactors.actors.check_user_is_new;

import info.smart_tools.smartactors.actors.check_user_is_new.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.check_user_is_new.wrapper.MessageWrapper;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import java.util.List;

/**
 * Actor check that this email was not registered before
 */
public class CheckUserIsNewActor {
    private CachedCollection collection;

    /**
     * Constructor
     * @param params the actor params
     * @throws InvalidArgumentException
     */
    public CheckUserIsNewActor(final ActorParams params) throws InvalidArgumentException {
        try {
            collection = IOC.resolve(Keys.getOrAdd(CachedCollection.class.toString()), params.getCollectionName());
        } catch (Exception e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Check that this email was not registered before
     * @param message the message, contain email
     * @throws Exception
     */
    public void check(final MessageWrapper message) throws Exception {
        try {
            List<IObject> users = collection.getItems(message.getEmail());
            if (!users.isEmpty()) {
                throw new TaskExecutionException("User with this email already exists");
            }
        } catch (ReadValueException | GetCacheItemException e) {
            throw new TaskExecutionException("Failed to get email from message", e);
        }
    }
}
