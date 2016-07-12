package info.smart_tools.smartactors.core.db_tasks.psql.create_collection;

//@PrepareForTest(IOC.class)
//@RunWith(PowerMockRunner.class)
//@SuppressWarnings("unchecked")
//public class QueryStatementBuilderTest {
//
//    @BeforeClass
//    public static void setUp() throws Exception {
//        mockStatic(IOC.class);
//
//        QueryStatement queryStatement = new QueryStatement();
//        IKey queryStatementKey = mock(IKey.class);
//        when(Keys.getOrAdd(QueryStatement.class.toString())).thenReturn(queryStatementKey);
//        when(IOC.resolve(eq(queryStatementKey))).thenReturn(queryStatement);
//    }
//
//    @Test
//    public void buildQueryStatementTest() throws Exception {
//        String collection = "testCollection";
//        Map<String, String> indexes = new HashMap<>(1);
//        String validationStr = "CREATE TABLE " + collection +
//                " (id BIGSERIAL PRIMARY KEY, document JSONB NOT NULL);\n";
//
//        QueryStatement queryStatement = QueryStatementBuilder
//                .create()
//                .withCollection(collection)
//                .withIndexes(indexes)
//                .build();
//
//        assertNotEquals(queryStatement, null);
//        assertEquals(queryStatement.getBodyWriter().toString(), validationStr);
//    }
//
//    @Test
//    public void checkSizeCreateCollectionQueryTest() throws Exception {
//        String collection = "testCollection";
//        QueryStatementBuilder builder = QueryStatementBuilder
//                .create()
//                .withCollection(collection);
//
//        Field templateSizeField = QueryStatementBuilder.class.getDeclaredField("TEMPLATE_SIZE");
//        templateSizeField.setAccessible(true);
//        int expectedTemplateSize = (int) templateSizeField.get(null) + collection.length();
//
//        Field queryField = QueryStatementBuilder.class.getDeclaredField("createCollectionQuery");
//        queryField.setAccessible(true);
//        int actualTemplateSize = queryField.get(builder).toString().length();
//
//        assertEquals(expectedTemplateSize, actualTemplateSize);
//    }
//
//    @Test(expected = QueryBuildException.class)
//    public void should_ThrowsException_WithReason_CollectionFieldNotSet() throws QueryBuildException {
//        QueryStatementBuilder
//                .create()
//                .withIndexes(Collections.emptyMap())
//                .build();
//    }
//
//    @Test(expected = QueryBuildException.class)
//    public void should_ThrowsException_WithReason_IndexesFieldNotSet() throws QueryBuildException {
//        QueryStatementBuilder
//                .create()
//                .withCollection("collection")
//                .build();
//    }
//}
