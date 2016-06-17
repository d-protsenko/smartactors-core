package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.IObjectIterator;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class, Keys.class })
@SuppressWarnings("unchecked")
public class PSQLSearchTaskTest {
    private IDatabaseTask searchTask;

    @Before
    public void setUp() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);
        searchTask = PSQLSearchTask.create();
    }

    @Test
    public void should_PrepareAndExecuteSearchTask() throws Exception {
        IObject message = mock(IObject.class);
        ISearchQuery queryMessage = mock(ISearchQuery.class);
        when(queryMessage.getCollectionName()).thenReturn("testCollection");
        when(queryMessage.countOrderBy()).thenReturn(0);

        IObject objectFirstExec = mock(IObject.class);
        IObject objectSecondExec = mock(IObject.class);
        IObject objectThirdExec = mock(IObject.class);
        IFieldName fieldName = mock(IFieldName.class);
        when(fieldName.toString()).thenReturn("testFN");

        when(objectFirstExec.getValue(fieldName)).thenReturn("first exec");
        when(objectSecondExec.getValue(fieldName)).thenReturn("second exec");
        when(objectThirdExec.getValue(fieldName)).thenReturn("third exec");

        when(queryMessage.getSearchResult(0)).thenReturn(objectFirstExec);
        when(queryMessage.getSearchResult(1)).thenReturn(objectFirstExec);
        when(queryMessage.getSearchResult(2)).thenReturn(objectSecondExec);
        when(queryMessage.getSearchResult(3)).thenReturn(objectSecondExec);
        when(queryMessage.getSearchResult(4)).thenReturn(objectThirdExec);
        when(queryMessage.getSearchResult(5)).thenReturn(objectThirdExec);

        IObject criteria = mock(IObject.class);
        IObjectIterator iterator = mock(IObjectIterator.class);
        when(iterator.next()).thenReturn(false);
        when(criteria.iterator()).thenReturn(iterator);

        when(queryMessage.getCriteria()).thenReturn(criteria);
        when(queryMessage.getPageNumber()).thenReturn(1);
        when(queryMessage.getPageSize()).thenReturn(2);

        IKey wrapperKey = mock(IKey.class);
        when(Keys.getOrAdd(ISearchQuery.class.toString())).thenReturn(wrapperKey);
        when(IOC.resolve(eq(wrapperKey), eq(message))).thenReturn(queryMessage);

        StorageConnection connection = mock(StorageConnection.class);
        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
        when(connection.compileQuery(anyObject())).thenReturn(compiledQuery);

        searchTask.setConnection(connection);
        searchTask.prepare(message);

        verify(queryMessage, times(1)).getCollectionName();
        verify(queryMessage, times(1)).getCriteria();
        verify(queryMessage, times(1)).countOrderBy();
        verify(connection, times(1)).compileQuery(anyObject());
        verifyStatic(times(1));
        IOC.resolve(eq(wrapperKey), eq(message));

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        String json = "testJson";
        when(resultSet.getString(eq("document"))).thenReturn(json);
        when(resultSet.getLong(eq("id"))).thenReturn(123L);

        IKey iObjectKey = mock(IKey.class);
        IKey fieldNameKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.toString())).thenReturn(iObjectKey);
        when(Keys.getOrAdd(IFieldName.class.toString())).thenReturn(fieldNameKey);

        IObject convertJson = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey), eq(json))).thenReturn(convertJson);

        IFieldName idFN = mock(IFieldName.class);
        when(IOC.resolve(eq(fieldNameKey), eq("id"))).thenReturn(idFN);

        searchTask.execute();

        verify(compiledQuery, times(1)).getPreparedStatement();
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(2)).next();
        verify(resultSet, times(1)).getString(eq("document"));
        verify(resultSet, times(1)).getLong(eq("id"));
        verifyStatic(times(1));
        IOC.resolve(eq(iObjectKey), eq(json));
        verifyStatic(times(1));
        IOC.resolve(eq(fieldNameKey), eq("id"));

        assertEquals(queryMessage.getSearchResult(0).getValue(fieldName), "first exec");
        assertEquals(queryMessage.getSearchResult(1).getValue(fieldName), "first exec");
    }
}
