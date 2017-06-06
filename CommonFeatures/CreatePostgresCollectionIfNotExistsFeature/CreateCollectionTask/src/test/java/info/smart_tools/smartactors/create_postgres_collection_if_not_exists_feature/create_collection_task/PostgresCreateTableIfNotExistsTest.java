package info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_task;

import info.smart_tools.smartactors.database.database_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostgresCreateTableIfNotExistsTest {
    private CollectionName collection;
    private QueryStatement statement;
    private StringWriter body;

    @BeforeClass
    public static void initIOC() throws PluginException, ProcessExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        bootstrap.start();
    }

    @Before
    public void setUp() throws QueryBuildException {
        collection = CollectionName.fromString("test_collection");
        body = new StringWriter();
        statement = mock(QueryStatement.class);
        when(statement.getBodyWriter()).thenReturn(body);
    }

    @Test
    public void testCreateIfNotExists() throws QueryBuildException {
        CreateTableIfNotExistsSchema.createIfNotExists(statement, collection, null);
        assertEquals(
                "CREATE OR REPLACE FUNCTION parse_timestamp_immutable(source jsonb) RETURNS timestamptz AS $$ " +
                        "BEGIN RETURN source::text::timestamptz; END; " +
                        "$$ LANGUAGE 'plpgsql' IMMUTABLE;\n" +
                        "CREATE OR REPLACE FUNCTION bigint_to_jsonb_immutable(source bigint) RETURNS jsonb AS $$ " +
                        "BEGIN RETURN to_json(source)::jsonb; END; " +
                        "$$ LANGUAGE 'plpgsql' IMMUTABLE;\n" +
                        "CREATE TABLE IF NOT EXISTS test_collection (document jsonb NOT NULL);\n" +
                        "do\n" +
                        "$$\n" +
                        "begin\n" +
                        "if not exists (\n" +
                        "select indexname\n" +
                        "    from pg_indexes\n" +
                        "    where tablename = 'test_collection'\n" +
                        "        and indexname = 'test_collection_pkey'\n" +
                        ")\n" +
                        "then\n" +
                        "    CREATE UNIQUE INDEX test_collection_pkey ON test_collection USING BTREE ((document#>'{test_collectionID}'));\n" +
                        "end if;\n" +
                        "end \n" +
                        "$$;",
                body.toString());
    }
}
