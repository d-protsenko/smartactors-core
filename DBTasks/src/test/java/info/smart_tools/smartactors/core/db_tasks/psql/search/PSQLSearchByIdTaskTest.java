package info.smart_tools.smartactors.core.db_tasks.psql.search;

//@PrepareForTest(IOC.class)
//@RunWith(PowerMockRunner.class)
//@SuppressWarnings("unchecked")
//public class PSQLSearchByIdTaskTest {
//
//    private PSQLSearchByIdTask task;
//    private JDBCCompiledQuery compiledQuery;
//    private IStorageConnection connection;
//
//    @Before
//    public void setUp() throws StorageException, IllegalAccessException {
//        compiledQuery = mock(JDBCCompiledQuery.class);
//        task = new PSQLSearchByIdTask();
//    }
//
//    @Test
//    public void ShouldPrepareQuery()
//            throws TaskPrepareException, ResolutionException, ReadValueException, ChangeValueException, StorageException, PoolTakeException, TaskSetConnectionException {
//
//        IObject createCollectionMessage = mock(IObject.class);
//        ISearchByIdMessage message = mock(ISearchByIdMessage.class);
//        IPreparedQuery preparedQuery = new QueryStatement();
//        initDataForPrepare(preparedQuery, message, createCollectionMessage);
//        Map<String, String> indexes = new HashMap<>();
//        indexes.put("meta.tags", "tags");
//        when(message.getDocumentId()).thenReturn("123");
//        IStorageConnection connection = mock(IStorageConnection.class);
//        when(connection.compileQuery(any(IPreparedQuery.class))).thenReturn(compiledQuery);
//        when(connection.getId()).thenReturn("testConnectionId");
//
//        IKey compiledQueryKey = mock(IKey.class);
//        when(Keys.getOrAdd(ICompiledQuery.class.toString())).thenReturn(compiledQueryKey);
//        when(IOC.resolve(eq(compiledQueryKey), eq(connection), anyObject())).thenReturn(compiledQuery);
//
//        task.setConnection(connection);
//        task.prepare(createCollectionMessage);
//
//        PowerMockito.verifyStatic();
//        IOC.resolve(any(IKey.class), eq(connection), any(IQueryStatementFactory.class));
//    }
//
//    @Test(expected = TaskExecutionException.class)
//    public void ShouldExecuteQueryAndThrowTaskException() throws Exception {
//
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        field(PSQLSearchByIdTask.class, "query").set(task, compiledQuery);
//        task.execute();
//
//        verify(compiledQuery).executeQuery();
//        verify(preparedStatement).execute();
//    }
//
//    @Test
//    public void ShouldSetConnection() throws Exception {
//
//        IStorageConnection storageConnectionBefore = (IStorageConnection) MemberModifier.field(PSQLSearchByIdTask.class, "connection").get(task);
//        connection = mock(IStorageConnection.class);
//        when(connection.getId()).thenReturn("testConnectionId");
//        task.setConnection(connection);
//        IStorageConnection storageConnectionAfter = (IStorageConnection) MemberModifier.field(PSQLSearchByIdTask.class, "connection").get(task);
//
//        assertNull(storageConnectionBefore);
//        assertNotNull(storageConnectionAfter);
//        assertEquals(connection, storageConnectionAfter);
//    }
//
//    private void initDataForPrepare(IPreparedQuery preparedQuery, ISearchByIdMessage message, IObject createCollectionMessage)
//            throws ResolutionException, ReadValueException, ChangeValueException {
//
//        mockStatic(IOC.class);
//
//        IKey key1 = mock(IKey.class);
//        IKey keyQuery = mock(IKey.class);
//        IKey keyMessage = mock(IKey.class);
//        IKey keyFieldPath = mock(IKey.class);
//        when(IOC.getKeyForKeyStorage()).thenReturn(key1);
//        when(IOC.resolve(eq(key1), eq(QueryStatement.class.toString()))).thenReturn(keyQuery);
//        when(IOC.resolve(eq(key1), eq(ISearchByIdMessage.class.toString()))).thenReturn(keyMessage);
//        when(IOC.resolve(eq(key1), eq(FieldPath.class.toString()))).thenReturn(keyFieldPath);
//
//
//        FieldPath fieldPath = mock(FieldPath.class);
//        when(IOC.resolve(eq(keyQuery))).thenReturn(preparedQuery);
//        when(IOC.resolve(eq(keyMessage), eq(createCollectionMessage))).thenReturn(message);
//        when(IOC.resolve(eq(keyFieldPath), anyString())).thenReturn(fieldPath);
//        when(fieldPath.getSQLRepresentation()).thenReturn("");
//
//        CollectionName collectionName = mock(CollectionName.class);
//        when(collectionName.toString()).thenReturn("collection");
//        when(message.getCollection()).thenReturn(collectionName);
//    }
//}