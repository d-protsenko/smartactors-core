package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class, Keys.class })
@SuppressWarnings("unchecked")
public class PSQLSearchTaskTest {
    private IStorageConnection connection;
    private IObject message;
    private ISearchMessage queryMessage;
    private JDBCCompiledQuery compiledQuery;

    private IKey wrapperKey;
    private IKey cachedQueryKey;

//    @Before
//    public void setUp() throws Exception {
//        mockStatic(IOC.class);
//        mockStatic(Keys.class);
//
//        connection = mock(IStorageConnection.class);
//        message = mock(IObject.class);
//        queryMessage = mock(ISearchMessage.class);
//        ICollectionName collectionName = mock(ICollectionName.class);
//        when(collectionName.toString()).thenReturn("testCollection");
//        when(queryMessage.getCollection()).thenReturn(collectionName);
//        when(queryMessage.getOrderBy()).thenReturn(Collections.emptyList());
//        compiledQuery = mock(JDBCCompiledQuery.class);
//        when(connection.compileQuery(anyObject())).thenReturn(compiledQuery);
//
//        IObject criteria = mock(IObject.class);
//        Iterator iterator = mock(Iterator.class);
//        when(iterator.hasNext()).thenReturn(false);
//        when(criteria.iterator()).thenReturn(iterator);
//
//        when(queryMessage.getCriteria()).thenReturn(criteria);
//        when(queryMessage.getPageNumber()).thenReturn(1);
//        when(queryMessage.getPageSize()).thenReturn(2);
//
//        wrapperKey = mock(IKey.class);
//        when(Keys.getOrAdd(ISearchMessage.class.toString())).thenReturn(wrapperKey);
//        when(IOC.resolve(eq(wrapperKey), eq(message))).thenReturn(queryMessage);
//
//        bufferedQuery = mock(ICachedQuery.class);
//        when(bufferedQuery.getCompiledQuery()).thenReturn(compiledQuery);
//        when(bufferedQuery.getParametersSetters()).thenReturn(new ArrayList<>());
//
//        cachedQueryKey = mock(IKey.class);
//        when(Keys.getOrAdd(ICachedQuery.class.toString())).thenReturn(cachedQueryKey);
//        when(IOC.resolve(eq(cachedQueryKey), eq(compiledQuery), anyObject())).thenReturn(bufferedQuery);
//    }
//
//    @Test
//    public void prepareSearchQueryTest_WhenBufferedQueryIsNotExist() throws Exception {
//        DBSearchTask searchTask = PSQLSearchTask.create();
//        searchTask.setConnection(connection);
//        when(queryMessage.getCachedQuery()).thenReturn(Optional.empty());
//        prepareSearchQueryTest(searchTask);
//    }
//
//    @Test
//    public void prepareSearchQueryTest_WhenBufferedQueryIsExist() throws Exception {
//        DBSearchTask searchTask = PSQLSearchTask.create();
//        searchTask.setConnection(connection);
//        when(queryMessage.getCachedQuery()).thenReturn(Optional.of(bufferedQuery));
//        prepareSearchQueryTest(searchTask);
//    }
//
//    @Test
//    public void executeSearchQueryTest() throws Exception {
//        DBSearchTask searchTask = prepareSearchTask();
//
//        String json = "testJSON";
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        ResultSet resultSet = mock(ResultSet.class);
//        when(preparedStatement.executeQuery()).thenReturn(resultSet);
//        when(compiledQuery.execute()).thenReturn(true);
//        when(resultSet.next()).thenReturn(true).thenReturn(false);
//        when(resultSet.getString(eq("document"))).thenReturn(json);
//        when(resultSet.getLong(eq("id"))).thenReturn(123L);
//
//        IKey iObjectKey = mock(IKey.class);
//        IKey fieldNameKey = mock(IKey.class);
//        when(Keys.getOrAdd(IObject.class.toString())).thenReturn(iObjectKey);
//        when(Keys.getOrAdd(IFieldName.class.toString())).thenReturn(fieldNameKey);
//
//        IObject searchResultObj = mock(IObject.class);
//        when(IOC.resolve(eq(iObjectKey), eq(json))).thenReturn(searchResultObj);
//
//        IFieldName idFN = mock(IFieldName.class);
//        when(IOC.resolve(eq(fieldNameKey), eq("id"))).thenReturn(idFN);
//
//        searchTask.execute();
//
//        verify(compiledQuery, times(1)).execute();
//        verify(preparedStatement, times(1)).executeQuery();
//        verify(resultSet, times(2)).next();
//        verify(resultSet, times(1)).getString(eq("document"));
//        verify(resultSet, times(1)).getLong(eq("id"));
//        verify(queryMessage).setSearchResult(Collections.singletonList(searchResultObj));
//
//        verifyStatic(times(1));
//        IOC.resolve(eq(iObjectKey), eq(json));
//        verifyStatic(times(1));
//        IOC.resolve(eq(fieldNameKey), eq("id"));
//    }
//
//    private void prepareSearchQueryTest(DBSearchTask searchTask) throws Exception {
//        searchTask.setConnection(connection);
//        searchTask.prepare(message);
//
//        verify(queryMessage, times(1)).getCollection();
//        verify(queryMessage, times(1)).getCriteria();
//        verify(queryMessage, times(1)).getOrderBy();
//        verify(queryMessage, times(1)).setCachedQuery(eq(bufferedQuery));
//
//        verifyStatic(times(1));
//        IOC.resolve(eq(wrapperKey), eq(message));
//        verifyStatic(times(1));
//        IOC.resolve(eq(cachedQueryKey), eq(compiledQuery), anyObject());
//
//        Field connectionField = searchTask.getClass().getDeclaredField("connection");
//        connectionField.setAccessible(true);
//        assertEquals(connectionField.get(searchTask), connection);
//
//        Field queryField = searchTask.getClass().getDeclaredField("query");
//        queryField.setAccessible(true);
//        assertEquals(queryField.get(searchTask), compiledQuery);
//
//        Field messageField = searchTask.getClass().getDeclaredField("message");
//        messageField.setAccessible(true);
//        assertEquals(messageField.get(searchTask), queryMessage);
//    }
//
//    private DBSearchTask prepareSearchTask() throws Exception {
//        DBSearchTask searchTask = PSQLSearchTask.create();
//        searchTask.setConnection(connection);
//
//        field(PSQLSearchTask.class, "query").set(searchTask, compiledQuery);
//        field(PSQLSearchTask.class, "message").set(searchTask, queryMessage);
//
//        return searchTask;
//    }
}
