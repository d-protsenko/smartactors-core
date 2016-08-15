package info.smart_tools.smartactors.plugin.in_memory_db_tasks;

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
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.core.in_memory_db_create_collection_task.InMemoryDBCreateCollectionTask;
import info.smart_tools.smartactors.core.in_memory_db_delete_task.InMemoryDBDeleteTask;
import info.smart_tools.smartactors.core.in_memory_db_get_by_id_task.InMemoryGetByIdTask;
import info.smart_tools.smartactors.core.in_memory_db_select_task.InMemoryDBSelectTask;
import info.smart_tools.smartactors.core.in_memory_db_upsert_task.InMemoryDBUpsertTask;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin for register tasks for {@link InMemoryDatabase}
 */
public class PluginInMemoryDBTasks implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public PluginInMemoryDBTasks(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            BootstrapItem item = new BootstrapItem("InMemoryDBTasksPlugin");
            item
                    .after("IOC")
                    .after("IFieldPlugin")
                    .after("InMemoryDatabase")
                    .process(() -> {
                        try {
                            registerCreateCollectionTask();
                            registerUpsertTask();
                            registerGetByIdTask();
                            registerSearchTask();
                            registerDeleteTask();
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("Can't resolve fields for db task.", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("Can't create strategy for db task.", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("Can't register strategy for db task.", e);
                        }
                    });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't get BootstrapItem.", e);
        }
    }

    private void registerUpsertTask() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IField collectionNameField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        IField documentField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "document");
        IOC.register(
                Keys.getOrAdd("db.collection.upsert"),
                //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                String collectionName = String.valueOf(args[1]);
                                IObject document = (IObject) args[2];
                                IDatabaseTask task = new InMemoryDBUpsertTask();

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
    }

    private void registerCreateCollectionTask() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IField collectionNameField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        IOC.register(
                Keys.getOrAdd("db.collection.create"),
                //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                String collectionName = String.valueOf(args[1]);
                                IDatabaseTask task = new InMemoryDBCreateCollectionTask();

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                collectionNameField.out(query, collectionName);

                                task.prepare(query);
                                return task;
                            } catch (Exception e) {
                                throw new RuntimeException("Can't resolve upsert db task.", e);
                            }
                        }
                )
        );
    }

    private void registerGetByIdTask() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IField collectionNameField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        IField idField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "id");
        IField callbackField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "callback");
        IOC.register(
                Keys.getOrAdd("db.collection.getbyid"),
                //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                Object id = args[2];
                                IAction<IObject> callback = (IAction<IObject>) args[3];
                                IDatabaseTask task = new InMemoryGetByIdTask();

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                collectionNameField.out(query, collectionName.toString());
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
    }

    private void registerSearchTask() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IField collectionNameField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        IField criteriaField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "criteria");
        IField callbackField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "callback");
        IOC.register(
                Keys.getOrAdd("db.collection.search"),
                //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                CollectionName collectionName = CollectionName.fromString(String.valueOf(args[1]));
                                IObject criteria = (IObject) args[2];
                                IAction<IObject[]> callback = (IAction<IObject[]>) args[3];
                                IDatabaseTask task = new InMemoryDBSelectTask();

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                collectionNameField.out(query, collectionName.toString());
                                criteriaField.out(query, criteria);
                                callbackField.out(query, callback);

                                task.prepare(query);
                                return task;
                            } catch (Exception e) {
                                throw new RuntimeException("Can't resolve search db task.", e);
                            }
                        }
                )
        );
    }

    private void registerDeleteTask() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IField collectionNameField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        IField documentField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "document");
        IOC.register(
                Keys.getOrAdd("db.collection.delete"),
                //TODO:: use smth like ResolveByNameStrategy, but this caching strategy should call prepare always
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                String collectionName = String.valueOf(args[1]);
                                IObject document = (IObject) args[2];
                                IDatabaseTask task = new InMemoryDBDeleteTask();

                                IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                                collectionNameField.out(query, collectionName);
                                documentField.out(query, document);

                                task.prepare(query);
                                return task;
                            } catch (Exception e) {
                                throw new RuntimeException("Can't resolve delete db task.", e);
                            }
                        }
                )
        );
    }

}
