package info.smart_tools.smartactors.core.examples.db_collection;

import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_database_plugin.PluginInMemoryDatabase;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_db_tasks_plugin.PluginInMemoryDBTasks;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin.PluginIOCSimpleContainer;
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

            IFieldName idField = IOC.resolve(
                    Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), collection + "ID");

            IObject document = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
            IFieldName textField = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "text");
            IFieldName intField = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "int");
            document.setValue(textField, "initial value");
            document.setValue(intField, 1);

            ConnectionOptions connectionOptions = new TestConnectionOptions();
            IPool pool = IOC.resolve(Keys.getKeyByName("DatabaseConnectionPool"), connectionOptions);

            CollectionOperations.createCollection(pool, collection);

            CollectionOperations.insertDocument(pool, collection, document);

            document.setValue(textField, "new updated value");
            CollectionOperations.upsertDocument(pool, collection, document);

            CollectionOperations.getDocumentById(pool, collection, document.getValue(idField));

            CollectionOperations.searchDocumentByIntField(pool, collection);

            CollectionOperations.countByInt(pool, collection);

            CollectionOperations.searchDocumentByTextField(pool, collection);

            CollectionOperations.deleteDocument(pool, collection, document);

            CollectionOperations.countByInt(pool, collection);

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
