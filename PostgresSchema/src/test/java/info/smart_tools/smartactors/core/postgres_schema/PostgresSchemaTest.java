package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for SQL statements.
 */
public class PostgresSchemaTest {

    private CollectionName collection;
    private QueryStatement statement;
    private StringWriter body;

    @Before
    public void setUp() throws QueryBuildException {
        collection = CollectionName.fromString("test_collection");
        body = new StringWriter();
        statement = mock(QueryStatement.class);
        when(statement.getBodyWriter()).thenReturn(body);
    }

    @Test
    public void testInsert() throws QueryBuildException {
        PostgresSchema.insert(statement, collection);
        assertEquals("INSERT INTO test_collection (document) " +
                "VALUES (?::jsonb) " +
                "RETURNING id AS id", body.toString());
    }

    @Test
    public void testUpdate() throws QueryBuildException {
        PostgresSchema.update(statement, collection);
        assertEquals("UPDATE test_collection AS tab " +
                "SET tab.document = docs.document " +
                "FROM (VALUES (?, ?::jsonb)) AS docs (id, document) " +
                "WHERE tab.id = docs.id", body.toString());
    }

}
