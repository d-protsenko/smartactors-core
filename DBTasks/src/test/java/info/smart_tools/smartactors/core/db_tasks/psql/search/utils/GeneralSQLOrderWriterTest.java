package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ IOC.class, Keys.class })
//@SuppressWarnings("unchecked")
//public class GeneralSQLOrderWriterTest {
////    private ISearchQueryStatementWriter orderWriter;
//
//    @Before
//    public void setUp() {
//        mockStatic(IOC.class);
//        mockStatic(Keys.class);
//
////        orderWriter = SQLOrderWriter.create();
//    }
//
//    @Test
//    public void should_WritesORDERClauseIntoQueryStatement() throws Exception {
//        ISearchMessage ISearchMessage = mock(ISearchMessage.class);
//        IObject orderItem = mock(IObject.class);
//
//        List<IObject> orderByItems = new ArrayList<>(1);
//        orderByItems.add(orderItem);
//
//        when(ISearchMessage.getOrderBy()).thenReturn(orderByItems);
//        when(orderItem.getValue(anyObject())).thenReturn("testOrderField").thenReturn("testOrderDirection");
//
//        QueryStatement queryStatement = new QueryStatement();
//        List<ISQLQueryParameterSetter> setters = new LinkedList<>();
////        orderWriter.write(queryStatement, orderByItems, setters);
//
//        assertTrue("ORDER BY(document#>'{testOrderDirection}')ASC,(1)".equals(queryStatement.getBodyWriter().toString()));
//        verify(orderItem, times(2)).getValue(anyObject());
//    }
//}
