package info.smart_tools.smartactors.core.db_tasks.psql.delete;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ IOC.class })
//@SuppressWarnings("unchecked")
//public class PSQLDeleteTaskTest {
////    private CollectionName collectionName;
////    IDeleteByIdMessage queryMessage;
////    JDBCCompiledQuery compiledQuery;
////
////    @Before
////    public void setUp() throws Exception {
////        queryMessage = mock(IDeleteByIdMessage.class);
////        CollectionName collectionName = mock(CollectionName.class);
////
////        when(collectionName.toString()).thenReturn("testCollection");
////        when(queryMessage.getCollection()).thenReturn(collectionName);
////        when(queryMessage.getDocumentId()).thenReturn(1L);
////
////        compiledQuery = mock(JDBCCompiledQuery.class);
////
////        mockStatic(IOC.class);
////    }
////
////    @Test
////    public void should_PrepareDeletionQueryTest() throws Exception {
////        IStorageConnection connection = mock(IStorageConnection.class);
////        when(connection.getId()).thenReturn("testConnectionId");
////        DBDeleteTaskExecutor deleteTask = PSQLDeleteByIdTask.create();
////        deleteTask.setConnection(connection);
////
////        IKey wrapperKey = mock(IKey.class);
////        IKey queryKey = mock(IKey.class);
////        IKey key = mock(IKey.class);
////        when(Keys.getOrAdd(QueryKey.class.toString())).thenReturn(key);
////        when(IOC.resolve(eq(key), eq("testConnectionId"), eq(PSQLDeleteByIdTask.class.toString()), eq("testCollection")))
////                .thenReturn(key);
////
////        when(Keys.getOrAdd(IDeleteByIdMessage.class.toString())).thenReturn(wrapperKey);
////        when(Keys.getOrAdd(ICompiledQuery.class.toString() + "USED_CACHE")).thenReturn(queryKey);
////        when(IOC.resolve(eq(wrapperKey), anyObject())).thenReturn(queryMessage);
////        when(IOC.resolve(eq(queryKey), eq(key), eq(connection), anyObject()))
////                .thenReturn(compiledQuery);
////
////        IObject deletionQuery = mock(IObject.class);
////        deleteTask.prepare(deletionQuery);
////
////        verifyStatic(times(1));
////        IOC.resolve(eq(wrapperKey), eq(deletionQuery));
////
////        Field[] fields = fields(PSQLDeleteByIdTask.class);
////        assertEquals(getValue(fields, deleteTask, "query"), compiledQuery);
////        assertEquals(getValue(fields, deleteTask, "message"), queryMessage);
////    }
////
////    @Test
////    public void should_ExecuteDeletionQueryTest() throws Exception {
////        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
////        DBDeleteTaskExecutor deleteTask = PSQLDeleteByIdTask.create();
////        PreparedStatement prepareStatement = mock(PreparedStatement.class);
////
////        when(compiledQuery.executeUpdate()).thenReturn(1);
////        when(prepareStatement.executeUpdate()).thenReturn(1);
////        field(PSQLDeleteByIdTask.class, "query").set(deleteTask, compiledQuery);
////        field(PSQLDeleteByIdTask.class, "message").set(deleteTask, queryMessage);
////
////        deleteTask.execute();
////
////        verify(compiledQuery).executeUpdate();
////    }
////
////    @Test(expected = TaskExecutionException.class)
////    public void should_ThrowsException_WithReason_Of_TaskDidNotPreparedBeforeExecute() throws Exception {
////        IStorageConnection connection = mock(IStorageConnection.class);
////        when(connection.getId()).thenReturn("testConnectionId");
////
////        DBDeleteTaskExecutor deleteTask = PSQLDeleteByIdTask.create();
////        deleteTask.setConnection(connection);
////        deleteTask.execute();
////    }
////
////    @Test(expected = TaskExecutionException.class)
////    public void should_ThrowsException_WithReason_Of_WrongCountOfDocumentsIsDeleted() throws Exception {
////        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
////        DBDeleteTaskExecutor deleteTask = PSQLDeleteByIdTask.create();
////
////        when(compiledQuery.executeUpdate()).thenReturn(2);
////        field(PSQLDeleteByIdTask.class, "query").set(deleteTask, compiledQuery);
////        field(PSQLDeleteByIdTask.class, "message").set(deleteTask, queryMessage);
////
////        deleteTask.execute();
////    }
////
////    private Object getValue(final Field[] fields, final Object obj, final String name) throws IllegalAccessException {
////        for (Field field : fields) {
////            if (field.getName().equals(name)) {
////                return field.get(obj);
////            }
////        }
////
////        return null;
////    }
//}

