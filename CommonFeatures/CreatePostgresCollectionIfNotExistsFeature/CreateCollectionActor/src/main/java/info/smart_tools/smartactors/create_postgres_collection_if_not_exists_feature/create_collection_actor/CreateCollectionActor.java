package info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_actor;

import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_actor.exception.CreateCollectionActorException;
import info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_actor.wrapper.CreateCollectionWrapper;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

public class CreateCollectionActor {
    private final IPool pool;

    public CreateCollectionActor() throws CreateCollectionActorException {
        try {
            ConnectionOptions options = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            pool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), options);
        } catch (ResolutionException e) {
            throw new CreateCollectionActorException("Cannot create actor: " + e.getMessage(), e);
        }
    }

    public void createTable(CreateCollectionWrapper message) throws CreateCollectionActorException {
        try {
            String collectionName = message.getCollectionName();
            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.create-if-not-exists"),
                        guard.getObject(),
                        collectionName
                );
                task.execute();
            } catch (TaskExecutionException | ResolutionException | PoolGuardException e) {
                throw new CreateCollectionActorException(e);
            }
        } catch (ReadValueException e) {
            throw new CreateCollectionActorException(e);
        }
    }
}
