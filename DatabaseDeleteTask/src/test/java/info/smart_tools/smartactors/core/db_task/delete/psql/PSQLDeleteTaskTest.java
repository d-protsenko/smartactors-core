package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.delete.DBDeleteTask;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.DeletionQuery;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.PreparedStatement;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ IOC.class })
@SuppressWarnings("unchecked")
public class PSQLDeleteTaskTest {
    private String collectionName = "testCollection";

    @Before
    public void setUp() throws ResolutionException {
        mockStatic(IOC.class);
    }

    @Test
    public void should_PrepareAndExecuteDeletionQuery() throws Exception {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        DBDeleteTask deleteTask = PSQLDeleteTask.create(connectionPool, collectionName);

        DeletionQuery message = mock(DeletionQuery.class);
        when(message.getCollectionName()).thenReturn(collectionName);
        when(message.countDocumentIds()).thenReturn(3);
        when(message.getDocumentIds(0)).thenReturn(0L);
        when(message.getDocumentIds(1)).thenReturn(1L);
        when(message.getDocumentIds(2)).thenReturn(2L);

        IKey keyWrapper = mock(IKey.class);
        when(IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class)).thenReturn(keyWrapper);
        when(IOC.resolve(eq(keyWrapper), anyObject())).thenReturn(message);

        IObject deletionQuery = mock(IObject.class);
        deleteTask.prepare(deletionQuery);

        verify(message, times(2)).countDocumentIds();

        verifyStatic(times(1));
        IOC.resolve(eq(keyWrapper), eq(deletionQuery));

        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
        PreparedStatement prepareStatement = mock(PreparedStatement.class);
        StorageConnection storageConnection = mock(StorageConnection.class);

        when(connectionPool.getConnection()).thenReturn(storageConnection);
        when(storageConnection.compileQuery(any())).thenReturn(compiledQuery);
        when(compiledQuery.getPreparedStatement()).thenReturn(prepareStatement);
        when(prepareStatement.executeUpdate()).thenReturn(3);

        deleteTask.execute();

        verify(compiledQuery, times(1)).getPreparedStatement();
        verify(prepareStatement, times(1)).executeUpdate();
        verify(storageConnection, times(1)).commit();
        verify(storageConnection, times(1)).compileQuery(anyObject());
        verify(connectionPool, times(1)).returnConnection(storageConnection);
    }

    @Test(expected = TaskPrepareException.class)
    public void should_ThrowsTaskPrepareException() throws Exception {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        DBDeleteTask deleteTask = PSQLDeleteTask.create(connectionPool, collectionName);

        DeletionQuery message = mock(DeletionQuery.class);
        when(message.countDocumentIds()).thenReturn(0);

        IKey keyWrapper = mock(IKey.class);
        when(IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class)).thenReturn(keyWrapper);
        when(IOC.resolve(eq(keyWrapper), anyObject())).thenReturn(message);

        IObject deletionQuery = mock(IObject.class);
        deleteTask.prepare(deletionQuery);
    }

    @Test(expected = TaskExecutionException.class)
    public void should_ThrowsTaskExecutionException() throws Exception {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        DBDeleteTask deleteTask = PSQLDeleteTask.create(connectionPool, collectionName);

        JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
        PreparedStatement prepareStatement = mock(PreparedStatement.class);
        StorageConnection storageConnection = mock(StorageConnection.class);

        when(connectionPool.getConnection()).thenReturn(storageConnection);
        when(storageConnection.compileQuery(any())).thenReturn(compiledQuery);
        when(compiledQuery.getPreparedStatement()).thenReturn(prepareStatement);
        when(prepareStatement.executeUpdate()).thenReturn(3);

        deleteTask.execute();
    }
}


