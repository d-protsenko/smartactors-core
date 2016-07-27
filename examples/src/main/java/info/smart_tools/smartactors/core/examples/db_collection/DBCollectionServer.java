package info.smart_tools.smartactors.core.examples.db_collection;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool.Pool;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.postgres_connection.PostgresConnection;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.core.postgres_getbyid_task.GetByIdMessage;
import info.smart_tools.smartactors.core.postgres_getbyid_task.PostgresGetByIdTask;
import info.smart_tools.smartactors.core.postgres_upsert_task.PostgresUpsertTask;
import info.smart_tools.smartactors.core.postgres_upsert_task.UpsertMessage;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

/**
 * Sample server which works with DB collection.
 */
public class DBCollectionServer implements IServer {

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            initScope();
            initIOC();
            initDBPool();
            initDBStrategies();
        } catch (Throwable e) {
            throw new ServerInitializeException("Server initialization failed", e);
        }
    }

    private void initScope() throws ScopeProviderException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
    }

    private void initIOC() throws RegistrationException, InvalidArgumentException, ResolutionException {
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (args) -> {
                            try {
                                return new Key((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName(String.valueOf(args[0]));
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                Keys.getOrAdd(IObject.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                switch (args.length) {
                                    case 0: return new DSObject();
                                    case 1: return new DSObject(String.valueOf(args[0]));
                                    default: throw new InvalidArgumentException("cannot instantiate DSObject");
                                }
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    private void initDBPool() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(
                Keys.getOrAdd("PostgresConnectionPool"),
                new CreateNewInstanceStrategy(
                    (args) -> {
                        ConnectionOptions connectionOptions = (ConnectionOptions) args[0];
                        try {
                            return new Pool(connectionOptions.getMaxConnections(), () -> {
                                try {
                                    return new PostgresConnection(connectionOptions);
                                } catch (StorageException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (ReadValueException e) {
                            throw new RuntimeException("Can't create PostgresConnectionPool", e);
                        }
                    }
                )
        );
    }

    private void initDBStrategies() throws RegistrationException, ResolutionException, InvalidArgumentException {
        IFieldName collectionNameField = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName");
        IFieldName idField = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
        IFieldName documentField = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()), "document");
        IFieldName callbackField = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()), "callback");

        IOC.register(
                Keys.getOrAdd(UpsertMessage.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            IObject message = (IObject) args[0];
                            return new UpsertMessage() {
                                @Override
                                public CollectionName getCollectionName() throws ReadValueException {
                                    try {
                                        return (CollectionName) message.getValue(collectionNameField);
                                    } catch (Exception e) {
                                        throw new ReadValueException(e);
                                    }
                                }
                                @Override
                                public IObject getDocument() throws ReadValueException {
                                    try {
                                        return (IObject) message.getValue(documentField);
                                    } catch (InvalidArgumentException e) {
                                        throw new ReadValueException(e);
                                    }
                                }
                            };
                        }
                )
        );
        IOC.register(
                Keys.getOrAdd(GetByIdMessage.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            IObject message = (IObject) args[0];
                            return new GetByIdMessage() {
                                @Override
                                public CollectionName getCollectionName() throws ReadValueException {
                                    try {
                                        return (CollectionName) message.getValue(collectionNameField);
                                    } catch (Exception e) {
                                        throw new ReadValueException(e);
                                    }
                                }
                                @Override
                                public Object getId() throws ReadValueException {
                                    try {
                                        return message.getValue(idField);
                                    } catch (Exception e) {
                                        throw new ReadValueException(e);
                                    }
                                }
                                @Override
                                public IAction<IObject> getCallback() throws ReadValueException {
                                    try {
                                        return (IAction<IObject>) message.getValue(callbackField);
                                    } catch (Exception e) {
                                        throw new ReadValueException(e);
                                    }
                                }
                            };
                        }
                )
        );

        IOC.register(
                Keys.getOrAdd("db.collection.upsert"),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                IStorageConnection connection = (IStorageConnection) args[0];
                                CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                IObject document = (IObject) args[2];
                                IDatabaseTask task = new PostgresUpsertTask(connection);    // TODO: cache tasks

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                query.setValue(collectionNameField, collectionName);
                                query.setValue(documentField, document);

                                task.prepare(query);    // TODO: reuse cached tasks
                                return task;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                Keys.getOrAdd("db.collection.getbyid"),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                IStorageConnection connection = (IStorageConnection) args[0];
                                CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                Object id = args[2];
                                IAction<IObject> callback = (IAction<IObject>) args[3];
                                IDatabaseTask task = new PostgresGetByIdTask(connection);    // TODO: cache tasks

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                query.setValue(collectionNameField, collectionName);
                                query.setValue(idField, id);
                                query.setValue(callbackField, callback);

                                task.prepare(query);    // TODO: reuse cached tasks
                                return task;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    @Override
    public void start() throws ServerExecutionException {
        try {
            IObject document = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            IFieldName idField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), "testID");
            IFieldName testField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), "test");
            document.setValue(testField, "value");

            ConnectionOptions options = new TestConnectionOptions();
            IPool pool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), options);
            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        guard.getObject(),
                        "test",
                        document
                );
                task.execute();
            }
            System.out.println("Inserted");
            System.out.println((String) document.serialize());

            document.setValue(testField, "new value");
            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        guard.getObject(),
                        "test",
                        document
                );
                task.execute();
            }
            System.out.println("Updated");
            System.out.println((String) document.serialize());

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.getbyid"),
                        guard.getObject(),
                        "test",
                        document.getValue(idField),
                        (IAction<IObject>) doc -> {
                            try {
                                System.out.println("Found");
                                System.out.println((String) doc.serialize());
                            } catch (SerializeException e) {
                                throw new ActionExecuteException(e);
                            }
                        }
                );
                task.execute();
            }
        } catch (Exception e) {
            throw new ServerExecutionException(e);
        }
    }

    /**
     * Runs the server
     * @param args ignored
     * @throws ServerInitializeException when the server initialization failed
     * @throws ServerExecutionException when the server execution failed
     */
    public static void main(final String[] args) throws ServerInitializeException, ServerExecutionException {
        IServer server = new DBCollectionServer();
        server.initialize();
        server.start();
    }

}
