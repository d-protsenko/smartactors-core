package info.smart_tools.smartactors.core.db_task.search.utils.sql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_task.search.utils.SearchQueryWriter;
import info.smart_tools.smartactors.core.db_task.search.wrappers.SearchQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class GeneralSQLPagingWriterTest {
    private SearchQueryWriter pagingWriter;

    @Before
    public void setUp() {
        pagingWriter = GeneralSQLPagingWriter.create();
    }

    @Test
    public void should_WritesPAGINGClauseIntoQueryStatement() throws QueryBuildException {
        SearchQuery searchQuery = mock(SearchQuery.class);
        QueryStatement queryStatement = new QueryStatement();
        pagingWriter.write(queryStatement, searchQuery);
        assertTrue("LIMIT(?)OFFSET(?)".equals(queryStatement.getBodyWriter().toString()));
    }
}
