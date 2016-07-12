package info.smart_tools.smartactors.core.db_tasks.psql.search;

//@PrepareForTest(IOC.class)
//@RunWith(PowerMockRunner.class)
//@SuppressWarnings("unchecked")
//public class SearchByIdQueryStatementBuilderTest {

//    @Before
//    public void setUp() throws Exception {
//        mockStatic(IOC.class);
//        QueryStatement queryStatement = new QueryStatement();
//        IKey queryStatementKey = mock(IKey.class);
//        when(Keys.getOrAdd(QueryStatement.class.toString())).thenReturn(queryStatementKey);
//        when(IOC.resolve(eq(queryStatementKey))).thenReturn(queryStatement);
//    }
//
//    @Test
//    public void buildQueryStatementTest() throws Exception {
//        String collection = "testCollection";
//        String validationStr = "SELECT * FROM " + collection + " WHERE id=?;";
//
//        QueryStatement queryStatement = QueryStatementBuilder
//                .create()
//                .withCollection(collection)
//                .build();
//
//        assertNotEquals(queryStatement, null);
//        assertEquals(queryStatement.getBodyWriter().toString(), validationStr);
//
//        Field templateSizeField = QueryStatementBuilder.class.getDeclaredField("TEMPLATE_SIZE");
//        templateSizeField.setAccessible(true);
//        int expectedTemplateSize = (int) templateSizeField.get(null) + collection.length();
//        int actualTemplateSize = queryStatement.getBodyWriter().toString().length();
//
//        assertEquals(expectedTemplateSize, actualTemplateSize);
//    }
//
//    @Test(expected = BuildingException.class)
//    public void should_ThrowsException_WithReason_CollectionFieldNotSet() throws BuildingException {
//        QueryStatementBuilder
//                .create()
//                .build();
//    }
//}
