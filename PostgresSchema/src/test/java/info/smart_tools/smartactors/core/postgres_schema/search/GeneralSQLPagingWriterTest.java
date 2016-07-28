package info.smart_tools.smartactors.core.postgres_schema.search;

//public class GeneralSQLPagingWriterTest {
//    private ISearchQueryWriter pagingWriter;
//
//    @Before
//    public void setUp() {
//        pagingWriter = GeneralSQLPagingWriter.create();
//    }
//
//    @Test
//    public void should_WritesPAGINGClauseIntoQueryStatement() throws QueryBuildException {
//        ISearchQuery ISearchQuery = mock(ISearchQuery.class);
//        QueryStatement queryStatement = new QueryStatement();
//        List<SQLQueryParameterSetter> setters = new LinkedList<>();
//
//        pagingWriter.write(queryStatement, ISearchQuery, setters);
//        assertTrue("LIMIT(?)OFFSET(?)".equals(queryStatement.getBodyWriter().toString()));
//        assertEquals(setters.size(), 1);
//    }
//}
