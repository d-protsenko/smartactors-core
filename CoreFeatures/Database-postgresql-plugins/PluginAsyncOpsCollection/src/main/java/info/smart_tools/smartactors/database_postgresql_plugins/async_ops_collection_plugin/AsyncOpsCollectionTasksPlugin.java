package info.smart_tools.smartactors.database_postgresql_plugins.async_ops_collection_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.DeleteAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.database.async_operation_collection.task.UpdateAsyncOperationTask;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Plugin with strategies for Async ops collection db-tasks-facades
 * TODO:: this plugin contains strategies very similar to strategies into DBTasksPlugin
 */
public class AsyncOpsCollectionTasksPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap
     */
    public AsyncOpsCollectionTasksPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }


    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("AsyncOpsCollectionTasksPlugin");

            item
//                    .after("IOC")
//                    .after("IFieldPlugin")
                    .process(() -> {
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
                            throw new RuntimeException(e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load AsyncOpsCollectionTasksPlugin plugin", e);
        }
    }
}
