package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * A complex test for all criteria and operators.
 */
public class PostgresQueryWriterResolverTest {

    private PostgresQueryWriterResolver resolver;
    private StringWriter body;
    private QueryStatement query;

    @Before
    public void setUp() {
        resolver = new PostgresQueryWriterResolver();

        body = new StringWriter();

        query = mock(QueryStatement.class);
        when(query.getBodyWriter()).thenReturn(body);
    }

    private void testAndVerify(String jsonFilter, String expectedBody, int expectedParameterSetters)
            throws InvalidArgumentException, QueryBuildException {
        IObject filter = new DSObject(jsonFilter);
        resolver.resolve(null).write(query, resolver, null, filter);
        assertEquals(expectedBody, body.toString());
        verify(query, times(expectedParameterSetters)).pushParameterSetter(any());
    }

    @Test
    public void testEmptyQuery() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{}",
                "(TRUE)",
                0
        );
    }

    @Test
    @Ignore("Equality test without explicit $eq operator is not supported yet")
    public void testOneFieldEqualityQuery() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"a\": \"b\" }",
                "document#>'{a}'=?",
                1
        );
    }

    @Test
    public void testEqualityQuery() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"a\": { \"$eq\": \"b\" } }",
                "((((document#>'{a}')=to_json(?)::jsonb)))",
                1
        );
    }

    @Test
    public void testInQuery() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"status\": { \"$in\": [ \"P\", \"D\" ] } }",
                "((((document#>'{status}')in(to_json(?)::jsonb,to_json(?)::jsonb))))",
                1
        );
    }

    @Test
    public void testImplicitAndCondition() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"status\": { \"$eq\": \"A\" }, \"age\": { \"$lt\": 30 } }",
                "((((document#>'{status}')=to_json(?)::jsonb))AND(((document#>'{age}')<to_json(?)::jsonb)))",
                2
        );
    }

    @Test
    public void testOrCondition() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"$or\": [ { \"status\": { \"$eq\": \"A\" } }, { \"age\": { \"$lt\": 30 } } ] }",
                "((((((document#>'{status}')=to_json(?)::jsonb)))OR((((document#>'{age}')<to_json(?)::jsonb)))))",
                2
        );
    }

    @Test
    public void testImplicitAndAndOrCondition() throws InvalidArgumentException, QueryBuildException {
        testAndVerify(
                "{ \"status\": { \"$eq\": \"A\" }, \"$or\": [ { \"age\": { \"$lt\": 30 } }, { \"type\": { \"$eq\": 1 } } ] }",
                "((((document#>'{status}')=to_json(?)::jsonb))AND(((((document#>'{age}')<to_json(?)::jsonb)))OR((((document#>'{type}')=to_json(?)::jsonb)))))",
                3
        );
    }

    @Test
    public void testNestedField() throws QueryBuildException, InvalidArgumentException {
        testAndVerify(
                "{ \"favorites.artist\": { \"$eq\": \"Picasso\" } }",
                "((((document#>'{favorites,artist}')=to_json(?)::jsonb)))",
                1
        );
    }

    @Test
    public void testMatchConbination() throws QueryBuildException, InvalidArgumentException {
        testAndVerify(
                "{ \"finished\": { \"$gt\": 15, \"$lt\": 20 } }",
                "((((document#>'{finished}')<to_json(?)::jsonb)AND((document#>'{finished}')>to_json(?)::jsonb)))",
                2
        );
    }

}
