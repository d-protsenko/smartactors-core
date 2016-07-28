package info.smart_tools.smartactors.core.postgres_schema.search;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ IOC.class, Keys.class })
//@SuppressWarnings("unchecked")
//public class GeneralSQLOrderWriterTest {
//    private ISearchQueryWriter orderWriter;
//
//    @Before
//    public void setUp() {
//        mockStatic(IOC.class);
//        mockStatic(Keys.class);
//
//        orderWriter = GeneralSQLOrderWriter.create();
//    }
//
//    @Test
//    public void should_WritesORDERClauseIntoQueryStatement() throws Exception {
//        ISearchQuery ISearchQuery = mock(ISearchQuery.class);
//        IObject orderItem = mock(IObject.class);
//
//        FieldPath fieldPath = mock(FieldPath.class);
//        String sortDirection = "testSQLStrDirection";
//
//        when(ISearchQuery.countOrderBy()).thenReturn(1);
//        when(ISearchQuery.getOrderBy(0)).thenReturn(orderItem);
//        when(orderItem.getValue(anyObject())).thenReturn("testOrderField").thenReturn("testOrderDirection");
//
//        when(fieldPath.toSQL()).thenReturn("testSQLStrField");
//
//        IKey fieldPathKey = mock(IKey.class);
//        IKey strKey = mock(IKey.class);
//
//        when(Keys.getOrAdd(PostgresFieldPath.class.toString())).thenReturn(fieldPathKey);
//        when(Keys.getOrAdd(String.class.toString())).thenReturn(strKey);
//
//        when(IOC.resolve(eq(fieldPathKey), anyString())).thenReturn(fieldPath).thenReturn(sortDirection);
//
//        QueryStatement queryStatement = new QueryStatement();
//        List<SQLQueryParameterSetter> setters = new LinkedList<>();
//        orderWriter.write(queryStatement, ISearchQuery, setters);
//
//        assertTrue("ORDER BY(testSQLStrField)ASC,(1)".equals(queryStatement.getBodyWriter().toString()));
//
//        verify(orderItem, times(2)).getValue(anyObject());
//        verify(fieldPath, times(1)).toSQL();
//        verifyStatic(times(1));
//        Keys.getOrAdd(PostgresFieldPath.class.toString());
//        verifyStatic(times(1));
//        Keys.getOrAdd(String.class.toString());
//        verifyStatic(times(1));
//        IOC.resolve(eq(fieldPathKey), anyObject());
//        verifyStatic(times(1));
//        IOC.resolve(eq(strKey), anyObject());
//    }
//}
