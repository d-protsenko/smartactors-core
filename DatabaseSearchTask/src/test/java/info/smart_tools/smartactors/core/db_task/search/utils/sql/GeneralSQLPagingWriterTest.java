package info.smart_tools.smartactors.core.db_task.search.utils.sql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_task.search.utils.ISearchQueryWriter;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class GeneralSQLPagingWriterTest {
    private ISearchQueryWriter pagingWriter;

    @Before
    public void setUp() {
        pagingWriter = GeneralSQLPagingWriter.create();
    }

    @Test
    public void should_WritesPAGINGClauseIntoQueryStatement() throws QueryBuildException {
        ISearchMessage ISearchMessage = mock(ISearchMessage.class);
        QueryStatement queryStatement = new QueryStatement();
        List<SQLQueryParameterSetter> setters = new LinkedList<>();

        pagingWriter.write(queryStatement, ISearchMessage, setters);
        assertTrue("LIMIT(?)OFFSET(?)".equals(queryStatement.getBodyWriter().toString()));
        assertEquals(setters.size(), 1);
    }
}
