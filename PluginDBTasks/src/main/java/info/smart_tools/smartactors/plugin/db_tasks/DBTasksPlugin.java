package info.smart_tools.smartactors.plugin.db_tasks;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_getbyid_task.GetByIdMessage;
import info.smart_tools.smartactors.core.postgres_getbyid_task.PostgresGetByIdTask;
import info.smart_tools.smartactors.core.postgres_upsert_task.PostgresUpsertTask;
import info.smart_tools.smartactors.core.postgres_upsert_task.UpsertMessage;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin with IOC-strategies for database tasks
 */
public class DBTasksPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public DBTasksPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("DBTasksPlugin");
            item
                .after("IOC")
                .after("IFieldPlugin")
                .process(() -> {
                    try {

                        IField collectionNameField = IOC.resolve(
                            Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
                        IField idField = IOC.resolve(
                            Keys.getOrAdd(IField.class.getCanonicalName()), "id");
                        IField documentField = IOC.resolve(
                            Keys.getOrAdd(IField.class.getCanonicalName()), "document");
                        IField callbackField = IOC.resolve(
                            Keys.getOrAdd(IField.class.getCanonicalName()), "callback");

                        //registration upsert task & message
                        IOC.register(
                            Keys.getOrAdd(UpsertMessage.class.getCanonicalName()),
                            new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    IObject message = (IObject) args[0];
                                    return new UpsertMessage() {
                                        @Override
                                        public CollectionName getCollectionName() throws ReadValueException {
                                            try {
                                                return (CollectionName) collectionNameField.in(message);
                                            } catch (Exception e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                        @Override
                                        public IObject getDocument() throws ReadValueException {
                                            try {
                                                return (IObject) documentField.in(message);
                                            } catch (InvalidArgumentException e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                    };
                                }
                            )
                        );
                        IOC.register(
                            Keys.getOrAdd("db.collection.upsert"),
                            //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                            new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    try {
                                        IStorageConnection connection = (IStorageConnection) args[0];
                                        CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                        IObject document = (IObject) args[2];
                                        IDatabaseTask task = new PostgresUpsertTask(connection);

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

                        //registration get_by_id task & message
                        IOC.register(
                            Keys.getOrAdd(GetByIdMessage.class.getCanonicalName()),
                            new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    IObject message = (IObject) args[0];
                                    return new GetByIdMessage() {
                                        @Override
                                        public CollectionName getCollectionName() throws ReadValueException {
                                            try {
                                                return (CollectionName) collectionNameField.in(message);
                                            } catch (Exception e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                        @Override
                                        public Object getId() throws ReadValueException {
                                            try {
                                                return idField.in(message);
                                            } catch (Exception e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                        @Override
                                        public IAction<IObject> getCallback() throws ReadValueException {
                                            try {
                                                return (IAction<IObject>) callbackField.in(message);
                                            } catch (Exception e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                    };
                                }
                            )
                        );
                        IOC.register(
                            Keys.getOrAdd("db.collection.getbyid"),
                            //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                            new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    try {
                                        IStorageConnection connection = (IStorageConnection) args[0];
                                        CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                        Object id = args[2];
                                        IAction<IObject> callback = (IAction<IObject>) args[3];
                                        IDatabaseTask task = new PostgresGetByIdTask(connection);

                                        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                        collectionNameField.out(query, collectionName);
                                        idField.out(query, id);
                                        callbackField.out(query, callback);

                                        task.prepare(query);
                                        return task;
                                    } catch (Exception e) {
                                        throw new RuntimeException("Can't resolve getbyid db task.", e);
                                    }
                                }
                            )
                        );
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("Can't resolve fields for db task.", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("Can't create strategy for db task.", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("Can't register strategy for db task.", e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't get BootstrapItem.", e);
        }
    }
}
