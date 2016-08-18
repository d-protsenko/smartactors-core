package info.smart_tools.smartactors.actors.save_session;

import info.smart_tools.smartactors.actors.save_session.exception.SaveSessionException;
import info.smart_tools.smartactors.actors.save_session.wrapper.SaveSessionMessage;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

public class SaveSessionActor {
    private IPool connectionPool;
    private String collectionName;

    /**
     * default constructor
     * @param params IObject with configuration
     */
    public SaveSessionActor(final IObject params) throws SaveSessionException {
        try {
            IField collectionNameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            collectionName = collectionNameF.in(params);

            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            this.connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
        } catch (ResolutionException | InvalidArgumentException | ReadValueException e) {
            throw new SaveSessionException("Failed to create SaveSessionActor", e);
        }
    }

    /**
     *
     * @param message the actor message
     * @throws SaveSessionException sometimes
     */
    public void saveSession(final SaveSessionMessage message) throws SaveSessionException {
        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            IStorageConnection connection = (IStorageConnection) poolGuard.getObject();

            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.upsert"),
                    connection,
                    collectionName,
                    message.getSession()
            );
            task.execute();
        } catch (PoolGuardException e) {
            throw new SaveSessionException("Cannot get connection from pool.", e);
        } catch (Exception e) {
            throw new SaveSessionException("Error during upsert session", e);
        }
    }
}
