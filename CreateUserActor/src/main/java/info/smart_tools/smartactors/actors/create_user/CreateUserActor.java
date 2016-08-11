package info.smart_tools.smartactors.actors.create_user;

import info.smart_tools.smartactors.actors.create_user.wrapper.MessageWrapper;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

/**
 * Actor for creating user
 */
public class CreateUserActor {
    private IPool connectionPool;
    private String COLLECTION_NAME = "user_account";

    /**
     * Constructor
     * @param params the actors params
     * @throws InvalidArgumentException Throw when can't read some value from message or resolving key or dependency is throw exception
     */
    public CreateUserActor(final IObject params) throws InvalidArgumentException {
        try {
            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            this.connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Can't get key or resolve dependency", e);
        }
    }

    /**
     * Create a new user in collection
     * @param message the message
     * @throws TaskExecutionException Throw when can't get user or upsert his
     */
    public void create(final MessageWrapper message) throws TaskExecutionException {
        try {
            IObject user = message.getUser();

            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                ITask searchTask = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        poolGuard.getObject(),
                        COLLECTION_NAME,
                        user
                );
                searchTask.execute();
            }
        } catch (PoolGuardException e) {
            throw new TaskExecutionException("Failed to get connection", e);
        } catch (ResolutionException e) {
            throw new TaskExecutionException("Failed to resolve upsert task", e);
        } catch (ReadValueException e) {
            throw new TaskExecutionException("Failed to get user object from message", e);
        }
    }
}
