package info.smart_tools.smartactors.actors.check_user_by_email;

import info.smart_tools.smartactors.actors.check_user_by_email.exception.NotFoundUserException;
import info.smart_tools.smartactors.actors.check_user_by_email.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.check_user_by_email.wrapper.MessageWrapper;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.List;

/**
 * Actor that check user by email
 */
public class CheckUserByEmailActor {
    private ICachedCollection collection;

    /**
     * Constructor
     * @param params the constructor params
     * @throws InvalidArgumentException Throw when can't read some value from message or resolving key or dependency is throw exception
     */
    public CheckUserByEmailActor(final ActorParams params) throws InvalidArgumentException {
        try {
            collection = IOC.resolve(
                    Keys.getOrAdd(ICachedCollection.class.toString()),
                    params.getCollectionName(),
                    params.getCollectionKey());
        } catch (ReadValueException e) {
            throw new InvalidArgumentException("Can't read some of message values", e);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Can't get key or resolve dependency", e);
        }
    }


    /**
     * Try to find user with this email in collection
     * @param message the message
     * @throws NotFoundUserException Throw when user with this email not found
     * @throws TaskExecutionException Throw in all of other reasons
     */
    public void checkUser(final MessageWrapper message) throws NotFoundUserException, TaskExecutionException {
        try {
            List<IObject> users = collection.getItems(message.getEmail());
            if (users.size() != 1) {
                throw new NotFoundUserException("Failed to find user with this email");
            }
            message.setUser(users.get(0));
        } catch (ReadValueException | GetCacheItemException e) {
            throw new TaskExecutionException("Failed to get email from message", e);
        } catch (ChangeValueException e) {
            throw new TaskExecutionException("Failed to set user to message", e);
        }
    }
}