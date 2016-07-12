package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class, Keys.class })
@SuppressWarnings("unchecked")
public class GeneralSQLOrderWriterTest {
//    private ISearchQueryStatementWriter orderWriter;

    @Before
    public void setUp() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

//        orderWriter = SQLOrderWriter.create();
    }

    @Test
    public void should_WritesORDERClauseIntoQueryStatement() throws Exception {
        ISearchMessage ISearchMessage = mock(ISearchMessage.class);
        IObject orderItem = mock(IObject.class);

        List<IObject> orderByItems = new ArrayList<>(1);
        orderByItems.add(orderItem);

        when(ISearchMessage.getOrderBy()).thenReturn(orderByItems);
        when(orderItem.getValue(anyObject())).thenReturn("testOrderField").thenReturn("testOrderDirection");

        QueryStatement queryStatement = new QueryStatement();
        List<ISQLQueryParameterSetter> setters = new LinkedList<>();
//        orderWriter.write(queryStatement, orderByItems, setters);

        assertTrue("ORDER BY(document#>'{testOrderDirection}')ASC,(1)".equals(queryStatement.getBodyWriter().toString()));
        verify(orderItem, times(2)).getValue(anyObject());
    }
}
