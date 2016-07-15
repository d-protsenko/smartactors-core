package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class, Keys.class })
@SuppressWarnings("unchecked")
public class GeneralSQLOrderWriterTest {
    private SQLOrderWriter orderWriter;

    @Before
    public void setUp() throws ResolutionException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey fieldKey = mock(IKey.class);
        IField field = mock(IField.class);
        IField orderField = mock(IField.class);

        when(Keys.getOrAdd(IField.class.toString())).thenReturn(fieldKey);
        when(IOC.resolve(eq(fieldKey), eq("field"))).thenReturn(field);
        when(IOC.resolve(eq(fieldKey), eq("order"))).thenReturn(orderField);

        orderWriter = SQLOrderWriter.create();
    }

    @Test
    public void should_WritesDefaultORDERIntoQueryStatement() throws Exception {
        ISearchMessage ISearchMessage = mock(ISearchMessage.class);
        IObject orderItem = mock(IObject.class);

        List<IObject> orderByItems = new ArrayList<>(1);
        orderByItems.add(orderItem);

        when(ISearchMessage.getOrderBy()).thenReturn(orderByItems);
        when(orderItem.getValue(anyObject())).thenReturn("testOrderField").thenReturn("testOrderDirection");

        QueryStatement queryStatement = new QueryStatement();
        orderWriter.write(queryStatement, orderByItems);

        assertEquals("ORDER BY(document#>'{testOrderDirection}')ASC,(1)", queryStatement.getBodyWriter().toString());
        verify(orderItem, times(2)).getValue(anyObject());
    }
}
