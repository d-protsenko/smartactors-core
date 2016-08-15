package info.smart_tools.smartactors.core.examples.db_collection;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.in_memory_database.PluginInMemoryDatabase;
import info.smart_tools.smartactors.plugin.in_memory_db_tasks.PluginInMemoryDBTasks;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import info.smart_tools.smartactors.plugin.null_connection_pool.NullConnectionPoolPlugin;

/**
 * Sample server which works with DB collection.
 */
public class InMemoryDBCollectionServer implements IServer {

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            Bootstrap bootstrap = new Bootstrap();
            new PluginIOCSimpleContainer(bootstrap).load();
            new PluginIOCKeys(bootstrap).load();
            new IFieldNamePlugin(bootstrap).load();
            new IFieldPlugin(bootstrap).load();
            new PluginDSObject(bootstrap).load();
            new NullConnectionPoolPlugin(bootstrap).load();
            new PluginInMemoryDatabase(bootstrap).load();
            new PluginInMemoryDBTasks(bootstrap).load();
            bootstrap.start();
        } catch (Throwable e) {
            throw new ServerInitializeException("Server initialization failed", e);
        }
    }

    @Override
    public void start() throws ServerExecutionException {
        try {
            CollectionName collection = CollectionName.fromString(
                    "test_" + Long.toHexString(Double.doubleToLongBits(Math.random())));

            IObject document = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            IFieldName idField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), collection + "ID");
            IFieldName textField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), "text");
            document.setValue(textField, "initial value");
            IFieldName intField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), "int");
            document.setValue(intField, 1);

            IPool pool = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"));

            IObject createOptions = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                    "{ \"fulltext\": \"text\", \"language\": \"english\" }");
            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.create"),
                        guard.getObject(),
                        collection,
                        createOptions
                );
                task.execute();
            }
            System.out.println("Created " + collection);

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        guard.getObject(),
                        collection,
                        document
                );
                task.execute();
            }
            System.out.println("Inserted");
            System.out.println((String) document.serialize());

            document.setValue(textField, "new updated value");
            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        guard.getObject(),
                        collection,
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
                        collection,
                        document.getValue(idField),
                        (IAction<IObject>) doc -> {
                            try {
                                System.out.println("Found by id");
                                System.out.println((String) doc.serialize());
                            } catch (SerializeException e) {
                                throw new ActionExecuteException(e);
                            }
                        }
                );
                task.execute();
            }

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.search"),
                        guard.getObject(),
                        collection,
                        new DSObject(String.format(
                                "{ \"filter\": { \"%1$s\": { \"$eq\": 1 } } }",
                                intField.toString())),
                        (IAction<IObject[]>) docs -> {
                            try {
                                for (IObject doc : docs) {
                                    System.out.println("Found by " + intField);
                                    System.out.println((String) doc.serialize());
                                }
                            } catch (SerializeException e) {
                                throw new ActionExecuteException(e);
                            }
                        }
                );
                task.execute();
            }

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.search"),
                        guard.getObject(),
                        collection,
                        new DSObject(String.format(
                                "{ " +
                                        "\"filter\": { \"%1$s\": { \"$fulltext\": \"value\" } }," +
                                        "\"page\": { \"size\": 2, \"number\": 1 }," +
                                        "\"sort\": [ { \"%1$s\": \"asc\" } ]" +
                                "}",
                                textField.toString())),
                        (IAction<IObject[]>) docs -> {
                            try {
                                for (IObject doc : docs) {
                                    System.out.println("Found by " + textField);
                                    System.out.println((String) doc.serialize());
                                }
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
        IServer server = new InMemoryDBCollectionServer();
        server.initialize();
        server.start();
    }

}
