package info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database.async_operation_collection.AsyncOperationCollection;
import info.smart_tools.smartactors.database.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.database.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.DeleteAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.UpdateAsyncOperationTask;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.exception.AsyncOpsCollectionActorException;
import info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.wrapper.AsyncOpsWrapper;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;

public class AsyncOpsCollectionActor {

    void register(final AsyncOpsWrapper wrapper) throws AsyncOpsCollectionActorException {
        registerAsyncOperationCollection(wrapper);
        registerTasks();
    }

    private void registerAsyncOperationCollection(final AsyncOpsWrapper wrapper) throws AsyncOpsCollectionActorException {
        try {
            IKey asyncCollectionKey = Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName());
            IOC.register(asyncCollectionKey, new ResolveByCompositeNameIOCStrategy(
                    (args) -> {
                        try {
                            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd(wrapper.getConnectionOptionsRegistrationName()));
                            IPool connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);

                            return new AsyncOperationCollection(connectionPool, wrapper.getCollectionName());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
            throw new AsyncOpsCollectionActorException("Error during registration strategy for collection.", e);
        }
    }

    private void registerTasks() throws AsyncOpsCollectionActorException {
        try {
            IField collectionNameField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            IField tokenField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "token");
            IField documentField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "document");
            IField expiredTimeField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "expiredTime");
            IField callbackField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "callback");
            IField asyncDataField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "asyncData");

            IOC.register(
                    Keys.getOrAdd("db.async_ops_collection.get"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    String id = (String) args[2];
                                    IDatabaseTask task = new GetAsyncOperationTask(connection);

                                    IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                    collectionNameField.out(query, collectionName);
                                    tokenField.out(query, id);
                                    callbackField.out(query, args[3]);

                                    task.prepare(query);
                                    return task;
                                } catch (Exception e) {
                                    throw new RuntimeException("Can't resolve upsert db task.", e);
                                }
                            }
                    )
            );

            IOC.register(
                    Keys.getOrAdd("db.async_ops_collection.create"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IObject document = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                    asyncDataField.out(document, args[2]);
                                    tokenField.out(document, args[3]);
                                    expiredTimeField.out(document, args[4]);

                                    IDatabaseTask task = new CreateAsyncOperationTask(connection);

                                    IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                    collectionNameField.out(query, collectionName);
                                    documentField.out(query, document);

                                    task.prepare(query);
                                    return task;
                                } catch (Exception e) {
                                    throw new RuntimeException("Can't resolve upsert db task.", e);
                                }
                            }
                    )
            );

            IOC.register(
                    Keys.getOrAdd("db.async_ops_collection.delete"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    //TODO:: write strategy
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IDatabaseTask task = new DeleteAsyncOperationTask(connection);

                                    IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                                    IObject document = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                    tokenField.out(document, args[2]);
                                    documentField.out(query, document);
                                    collectionNameField.out(query, collectionName);

                                    task.prepare(query);
                                    return task;
                                } catch (Exception e) {
                                    throw new RuntimeException("Can't resolve upsert db task.", e);
                                }
                            }
                    )
            );

            IOC.register(
                    Keys.getOrAdd("db.async_ops_collection.complete"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    //TODO:: write strategy
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IObject document = (IObject) args[2];

                                    IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                                    collectionNameField.out(query, collectionName);
                                    documentField.out(query, document);

                                    IDatabaseTask updateTask = new UpdateAsyncOperationTask(connection);
                                    updateTask.prepare(query);
                                    return updateTask;
                                } catch (Exception e) {
                                    throw new RuntimeException("Can't resolve upsert db task.", e);
                                }
                            }
                    )
            );
        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
            throw new AsyncOpsCollectionActorException("Can't register AsyncOps database tasks", e);
        }
    }
}
