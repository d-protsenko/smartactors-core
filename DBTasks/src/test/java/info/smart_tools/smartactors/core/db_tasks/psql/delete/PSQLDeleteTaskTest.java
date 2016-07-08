package info.smart_tools.smartactors.core.db_tasks.psql.delete;

import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_tasks.commons.DBDeleteTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.delete.IDeleteMessage;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class })
@SuppressWarnings("unchecked")
public class PSQLDeleteTaskTest {
    private CollectionName collectionName;
    IDeleteMessage queryMessage;
    JDBCCompiledQuery compiledQuery;

    @Before
    public void setUp() throws Exception {
        queryMessage = mock(IDeleteMessage.class);
        CollectionName collectionName = mock(CollectionName.class);

        when(collectionName.toString()).thenReturn("testCollection");
        when(queryMessage.getCollection()).thenReturn(collectionName);
        when(queryMessage.getDocumentId()).thenReturn(1L);

        compiledQuery = mock(JDBCCompiledQuery.class);

        mockStatic(IOC.class);
    }

    @Test
    public void should_PrepareDeletionQueryTest() throws Exception {
        IStorageConnection connection = mock(IStorageConnection.class);
        when(connection.getId()).thenReturn("testConnectionId");
        DBDeleteTask deleteTask = PSQLDeleteTask.create();
        deleteTask.setStorageConnection(connection);

        IKey wrapperKey = mock(IKey.class);
        IKey queryKey = mock(IKey.class);
        IKey key = mock(IKey.class);
        when(Keys.getOrAdd(QueryKey.class.toString())).thenReturn(key);
        when(IOC.resolve(eq(key), eq("testConnectionId"), eq(PSQLDeleteTask.class.toString()), eq("testCollection")))
                .thenReturn(key);

        when(Keys.getOrAdd(IDeleteMessage.class.toString())).thenReturn(wrapperKey);
        when(Keys.getOrAdd(ICompiledQuery.class.toString() + "USED_CACHE")).thenReturn(queryKey);
        when(IOC.resolve(eq(wrapperKey), anyObject())).thenReturn(queryMessage);
        when(IOC.resolve(eq(queryKey), eq(key), eq(connection), anyObject()))
                .thenReturn(compiledQuery);

        IObject deletionQuery = mock(IObject.class);
        deleteTask.prepare(deletionQuery);

        verifyStatic(times(1));
        IOC.resolve(eq(wrapperKey), eq(deletionQuery));

        Field[] fields = fields(PSQLDeleteTask.class);
        assertEquals(getValue(fields, deleteTask, "query"), compiledQuery);
        assertEquals(getValue(fields, deleteTask, "message"), queryMessage);
    }

    @Test
    public void should_ExecuteDeletionQueryTest() throws Exception {
        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
        DBDeleteTask deleteTask = PSQLDeleteTask.create();
        PreparedStatement prepareStatement = mock(PreparedStatement.class);

        when(compiledQuery.executeUpdate()).thenReturn(1);
        when(prepareStatement.executeUpdate()).thenReturn(1);
        field(PSQLDeleteTask.class, "query").set(deleteTask, compiledQuery);
        field(PSQLDeleteTask.class, "message").set(deleteTask, queryMessage);

        deleteTask.execute();

        verify(compiledQuery).executeUpdate();
    }

    @Test(expected = TaskExecutionException.class)
    public void should_ThrowsException_WithReason_Of_TaskDidNotPreparedBeforeExecute() throws Exception {
        IStorageConnection connection = mock(IStorageConnection.class);
        when(connection.getId()).thenReturn("testConnectionId");

        DBDeleteTask deleteTask = PSQLDeleteTask.create();
        deleteTask.setStorageConnection(connection);
        deleteTask.execute();
    }

    @Test(expected = TaskExecutionException.class)
    public void should_ThrowsException_WithReason_Of_WrongCountOfDocumentsIsDeleted() throws Exception {
        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
        DBDeleteTask deleteTask = PSQLDeleteTask.create();

        when(compiledQuery.executeUpdate()).thenReturn(2);
        field(PSQLDeleteTask.class, "query").set(deleteTask, compiledQuery);
        field(PSQLDeleteTask.class, "message").set(deleteTask, queryMessage);

        deleteTask.execute();
    }

    private Object getValue(final Field[] fields, final Object obj, final String name) throws IllegalAccessException {
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field.get(obj);
            }
        }

        return null;
    }
}

