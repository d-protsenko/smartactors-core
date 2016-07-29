package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
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
    public static void initIOC() throws ScopeProviderException, InvalidArgumentException, ResolutionException, RegistrationException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (args) -> {
                            try {
                                return new Key((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName(String.valueOf(args[0]));
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
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

}
