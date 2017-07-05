package info.smart_tools.smartactors.database_postgresql_plugins.cached_collection_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.database.cached_collection.task.DeleteFromCachedCollectionTask;
import info.smart_tools.smartactors.database.cached_collection.task.GetItemFromCachedCollectionTask;
import info.smart_tools.smartactors.database.cached_collection.task.UpsertIntoCachedCollectionTask;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin with strategies for cached collection db-tasks-facades
 * TODO:: this plugin contains strategies very similar to strategies into DBTasksPlugin
 */
public class CachedCollectionTasksPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap
     */
    public CachedCollectionTasksPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }


    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("CachedCollectionTasksPlugin");

            item
//                    .after("IOC")
//                    .after("datetime_formatter_plugin")
//                    .after("IFieldPlugin")
                    .process(() -> {
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

                                                    IObject query = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

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

                                                    IObject query = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

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

                                                    IObject query = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));
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
                            throw new RuntimeException(e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load CachedCollectionTasksPlugin plugin", e);
        }
    }
}