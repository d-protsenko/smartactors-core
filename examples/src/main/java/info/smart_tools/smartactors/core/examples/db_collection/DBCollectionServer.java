package info.smart_tools.smartactors.core.examples.db_collection;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
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
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import info.smart_tools.smartactors.plugin.postges_connection_pool.PostgresConnectionPoolPlugin;
import info.smart_tools.smartactors.plugin.postgres_db_tasks.PostgresDBTasksPlugin;

/**
 * Sample server which works with DB collection.
 */
public class DBCollectionServer implements IServer {

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            Bootstrap bootstrap = new Bootstrap();
            new PluginIOCSimpleContainer(bootstrap).load();
            new PluginIOCKeys(bootstrap).load();
            new IFieldNamePlugin(bootstrap).load();
            new IFieldPlugin(bootstrap).load();
            new PostgresDBTasksPlugin(bootstrap).load();
            new PluginDSObject(bootstrap).load();
            new PostgresConnectionPoolPlugin(bootstrap).load();
            bootstrap.start();
        } catch (Throwable e) {
            throw new ServerInitializeException("Server initialization failed", e);
        }
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
                        "test",
                        new DSObject(String.format("{ \"filter\": { \"%s\": { \"$eq\": \"new value\" } } }", testField.toString())),
                        (IAction<IObject[]>) docs -> {
                            try {
                                for (IObject doc : docs) {
                                    System.out.println("Found by " + testField);
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
        IServer server = new DBCollectionServer();
        server.initialize();
        server.start();
    }

}
