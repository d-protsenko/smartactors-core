package info.smart_tools.smartactors.database.postgresql_async.async_query_actor;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.PostgresConnection;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_plugins.postgres_connection_pool_plugin.PostgresConnectionPoolPlugin;
import info.smart_tools.smartactors.database_postgresql_plugins.postgres_db_tasks_plugin.PostgresDBTasksPlugin;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.text.MessageFormat;

/**
 *
 */
public class SyncSearchBenchmark {
    private final static long QUERY_COUNT = 10;

    private final static String COLLECTION_NAME = "testcollection1";

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new ScopedIOCPlugin(bootstrap).load();
        new PluginScopeProvider(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new PostgresConnectionPoolPlugin(bootstrap).load();
        new PostgresDBTasksPlugin(bootstrap).load();

        bootstrap.start();

        PostgresConnection connection = new PostgresConnection(new ConnectionOptions() {
            @Override
            public String getUrl() throws ReadValueException {
                return "jdbc:postgresql://localhost:5433/postgres";
            }

            @Override
            public String getUsername() throws ReadValueException {
                return "test_user";
            }

            @Override
            public String getPassword() throws ReadValueException {
                return "password";
            }

            @Override
            public Integer getMaxConnections() throws ReadValueException {
                return null;
            }

            @Override
            public void setUrl(String url) throws ChangeValueException { }

            @Override
            public void setUsername(String username) throws ChangeValueException { }

            @Override
            public void setPassword(String password) throws ChangeValueException { }

            @Override
            public void setMaxConnections(Integer maxConnections) throws ChangeValueException { }
        });

        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                ("{\n" +
                "        \"filter\": {\n" +
                "            \"id\": {\n" +
                "                \"$lt\": 40\n" +
                "            }\n" +
                "        },\n" +
                "        \"page\": {\n" +
                "            \"size\": 10,\n" +
                "            \"number\": 4\n" +
                "        },\n" +
                "        \"sort\": []\n" +
                "    }"));

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < QUERY_COUNT; i++) {
            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.search"),
                    connection,
                    CollectionName.fromString(COLLECTION_NAME),
                    query,
                    (IAction<IObject[]>) docs -> {

                    }
            );

            task.execute();
        }

        long endTime = System.currentTimeMillis();

        System.out.println(MessageFormat.format("Spent {0} ms for {1} queries ({2} ms/query).",
                endTime - startTime, QUERY_COUNT, (endTime - startTime) / QUERY_COUNT));
    }
}
