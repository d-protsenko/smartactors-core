package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneralSQLPagingWriterTest {
    private ISearchQueryStatementWriter<int[]> pagingWriter;

    @Before
    public void setUp() {
        pagingWriter = GeneralSQLPagingWriter.create();
    }

    @Test
    public void should_WritesPAGINGClauseIntoQueryStatement() throws QueryBuildException {
        int[] paging = new int[2];
        QueryStatement queryStatement = new QueryStatement();
        List<ISQLQueryParameterSetter> setters = new LinkedList<>();

        pagingWriter.write(queryStatement, paging, setters);
        assertTrue("LIMIT(?)OFFSET(?)".equals(queryStatement.getBodyWriter().toString()));
        assertEquals(setters.size(), 1);
    }
}
