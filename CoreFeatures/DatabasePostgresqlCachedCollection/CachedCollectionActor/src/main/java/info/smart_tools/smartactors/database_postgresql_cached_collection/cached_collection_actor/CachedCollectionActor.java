package info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database.cached_collection.CachedCollection;
import info.smart_tools.smartactors.database.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.database.cached_collection.task.DeleteFromCachedCollectionTask;
import info.smart_tools.smartactors.database.cached_collection.task.GetItemFromCachedCollectionTask;
import info.smart_tools.smartactors.database.cached_collection.task.UpsertIntoCachedCollectionTask;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor.exception.CachedCollectionException;
import info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor.wrapper.CachedCollectionWrapper;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;

public class CachedCollectionActor {
    public void register(final CachedCollectionWrapper wrapper) throws CachedCollectionException {
        registerCachedCollection(wrapper);
        registerTasks();
    }

    private void registerCachedCollection(final CachedCollectionWrapper wrapper) throws CachedCollectionException {
        try {
            IKey cachedCollectionKey = Keys.getOrAdd(ICachedCollection.class.getCanonicalName());
            IField connectionPoolField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "connectionPool");
            IField collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            IField keyNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "keyName");
            IOC.register(cachedCollectionKey, new ResolveByCompositeNameIOCStrategy(
                    (args) -> {
                        try {
                            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd(wrapper.getConnectionOptionsRegistrationName()));
                            IPool connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
                            IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                            connectionPoolField.out(config, connectionPool);
                            collectionNameField.out(config, wrapper.getCollectionName());
                            keyNameField.out(config, wrapper.getKeyName());

                            return new CachedCollection(config);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
            throw new CachedCollectionException("CreateCachedCollection plugin can't load", e);
        }
    }

    private void registerTasks() throws CachedCollectionException {
        try {
            IField collectionNameField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            IField documentField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "document");
            IField callbackField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "callback");
            IField keyNameField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "keyName");
            IField keyField = IOC.resolve(
                    Keys.getOrAdd(IField.class.getCanonicalName()), "key");


            IOC.register(
                    Keys.getOrAdd("db.cached_collection.upsert"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IObject document = (IObject) args[2];
                                    IDatabaseTask task = new UpsertIntoCachedCollectionTask(connection);

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
                    Keys.getOrAdd("db.cached_collection.delete"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IObject document = (IObject) args[2];
                                    IDatabaseTask task = new DeleteFromCachedCollectionTask(connection);

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
                    Keys.getOrAdd("db.cached_collection.get_item"),
                    //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                    new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    IStorageConnection connection = (IStorageConnection) args[0];
                                    CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                    IDatabaseTask task = new GetItemFromCachedCollectionTask(connection);

                                    IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                                    collectionNameField.out(query, collectionName);
                                    keyNameField.out(query, args[2]);
                                    keyField.out(query, args[3]);
                                    callbackField.out(query, args[4]);

                                    task.prepare(query);
                                    return task;
                                } catch (Exception e) {
                                    throw new RuntimeException("Can't resolve upsert db task.", e);
                                }
                            }
                    )
            );
        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
            throw new CachedCollectionException(e);
        }
    }
}
