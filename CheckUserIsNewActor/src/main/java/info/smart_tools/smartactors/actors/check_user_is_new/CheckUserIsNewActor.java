package info.smart_tools.smartactors.actors.check_user_is_new;

import info.smart_tools.smartactors.actors.check_user_is_new.wrapper.MessageWrapper;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Actor check that this email was not registered before
 */
public class CheckUserIsNewActor {
    private String USER_COLLECTION_NAME = "user_account";
    private IPool connectionPool;

    /**
     * Constructor
     * @param params the actor params
     * @throws InvalidArgumentException Throw when can't read some value from message or resolving key or dependency is throw exception
     */
    public CheckUserIsNewActor(final IObject params) throws InvalidArgumentException {
        try {
            ConnectionOptions connectionOptionsWrapper = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptionsWrapper);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Can't get key or resolve dependency", e);
        }
    }

    /**
     * Check that this email was not registered before
     * @param message the message, contain email
     * @throws Exception Throw always
     */
    public void check(final MessageWrapper message) throws Exception {
        try {
            final List<IObject> items = new LinkedList<>();
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.search"),
                        poolGuard.getObject(),
                        USER_COLLECTION_NAME,
                        IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                                String.format(
                                        "{ " +
                                                "\"filter\": { \"email\": { \"$eq\": \"%s\" } }" +
                                                "}",
                                        message.getEmail())
                        ),
                        (IAction<IObject[]>) docs ->
                                items.addAll(Arrays.asList(docs))

                );

                task.execute();
            } catch (PoolGuardException e) {
                throw new TaskSetConnectionException("Can't get connection from pool.", e);
            }

            if (!items.isEmpty()) {
                throw new TaskExecutionException("User with this email already exists");
            }
        } catch (ReadValueException e) {
            throw new TaskExecutionException("Failed to get email from message", e);
        }
    }
}
