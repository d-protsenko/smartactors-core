package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test for SQL statements.
 */
public class PostgresSchemaTest {

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
    public void testNextId() throws QueryBuildException {
        PostgresSchema.nextId(statement, collection);
        assertEquals("SELECT nextval('test_collection_id_seq') AS id", body.toString());
    }

    @Test
    public void testInsert() throws QueryBuildException {
        PostgresSchema.insert(statement, collection);
        assertEquals("INSERT INTO test_collection (id, document) " +
                "VALUES (?, ?::jsonb)", body.toString());
    }

    @Test
    public void testUpdate() throws QueryBuildException {
        PostgresSchema.update(statement, collection);
        assertEquals("UPDATE test_collection AS tab " +
                "SET document = docs.document " +
                "FROM (VALUES (?, ?::jsonb)) AS docs (id, document) " +
                "WHERE tab.id = docs.id", body.toString());
    }

    @Test
    public void testGetById() throws QueryBuildException {
        PostgresSchema.getById(statement, collection);
        assertEquals("SELECT document FROM test_collection " +
                "WHERE id = ?", body.toString());
    }

    @Test
    public void testSearch() throws InvalidArgumentException, QueryBuildException {
        IObject criteria = new DSObject("{ \"filter\": { \"a\": { \"$eq\": \"b\" } } }");
        PostgresSchema.search(statement, collection, criteria);
        assertEquals("SELECT document FROM test_collection " +
                "WHERE ((((document#>'{a}')=to_json(?)::jsonb)))", body.toString());
        verify(statement, times(1)).pushParameterSetter(any());
    }

    @Test
    public void testSearchWithPaging() throws InvalidArgumentException, QueryBuildException {
        IObject criteria = new DSObject("{ \"filter\": { \"a\": { \"$eq\": \"b\" } }," +
                " \"page\": { \"size\": 10, \"number\": 3 } }");
        PostgresSchema.search(statement, collection, criteria);
        assertEquals("SELECT document FROM test_collection " +
                "WHERE ((((document#>'{a}')=to_json(?)::jsonb))) " +
                "LIMIT(?)OFFSET(?)", body.toString());
        verify(statement, times(2)).pushParameterSetter(any());
    }

    @Test
    public void testSearchWithSorting() throws InvalidArgumentException, QueryBuildException {
        IObject criteria = new DSObject("{ \"filter\": { \"a\": { \"$eq\": \"b\" } }," +
                " \"sort\": [ { \"a\": \"desc\" } ] }");
        PostgresSchema.search(statement, collection, criteria);
        assertEquals("SELECT document FROM test_collection " +
                "WHERE ((((document#>'{a}')=to_json(?)::jsonb))) " +
                "ORDER BY(document#>'{a}')DESC", body.toString());
        verify(statement, times(1)).pushParameterSetter(any());
    }

    @Test
    public void testSearchWithPagingAndSorting() throws InvalidArgumentException, QueryBuildException {
        IObject criteria = new DSObject("{ \"filter\": { \"a\": { \"$eq\": \"b\" } }," +
                " \"page\": { \"size\": 10, \"number\": 3 }, " +
                " \"sort\": [ { \"a\": \"desc\" } ] }");
        PostgresSchema.search(statement, collection, criteria);
        assertEquals("SELECT document FROM test_collection " +
                "WHERE ((((document#>'{a}')=to_json(?)::jsonb))) " +
                "ORDER BY(document#>'{a}')DESC " +
                "LIMIT(?)OFFSET(?)",
                body.toString());
        verify(statement, times(2)).pushParameterSetter(any());
    }

    @Test
    public void testCreate() throws QueryBuildException {
        PostgresSchema.create(statement, collection, null);
        assertEquals("CREATE TABLE test_collection (id bigserial PRIMARY KEY, document jsonb NOT NULL);\n", body.toString());
    }

    @Test
    public void testCreateWithIndexes() throws QueryBuildException, InvalidArgumentException {
        IObject indexes = new DSObject("{ \"a\": \"ordered\", \"b\": { \"fulltext\": \"english\" } }");
        PostgresSchema.create(statement, collection, indexes);
        assertEquals("CREATE TABLE test_collection (id bigserial PRIMARY KEY, document jsonb NOT NULL);\n" +
                "CREATE INDEX ON test_collection USING BTREE ((document#>'{a}'));\n" +
                "CREATE INDEX ON test_collection USING GIN ((to_tsvector('english',(document#>'{b}')::text)));\n", body.toString());
    }

}
