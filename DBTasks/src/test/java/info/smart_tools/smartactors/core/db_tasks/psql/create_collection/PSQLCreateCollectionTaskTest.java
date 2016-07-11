package info.smart_tools.smartactors.core.db_tasks.psql.create_collection;

import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ICollectionName;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.wrappers.create_collection.ICreateCollectionMessage;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.field;

@PrepareForTest({ IOC.class, Keys.class })
@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
public class PSQLCreateCollectionTaskTest {
    private PSQLCreateCollectionTask task = PSQLCreateCollectionTask.create();
    private static JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
    private static IObject message = mock(IObject.class);
    private static ICreateCollectionMessage wrapper = mock(ICreateCollectionMessage.class);
    private static IStorageConnection connection = mock(IStorageConnection.class);

    @BeforeClass
    public static void setUp() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey wrapperKey = mock(IKey.class);
        IKey compiledQueryKey = mock(IKey.class);

        Map<String, String> indexes = new HashMap<>();
        indexes.put("meta.tags", "tags");
        ICollectionName collectionName = mock(ICollectionName.class);

        when(collectionName.toString()).thenReturn("testCollection");
        when(wrapper.getIndexes()).thenReturn(indexes);
        when(wrapper.getCollection()).thenReturn(collectionName);
        when(Keys.getOrAdd(ICompiledQuery.class.toString())).thenReturn(compiledQueryKey);
        when(IOC.resolve(eq(compiledQueryKey), eq(connection), anyObject())).thenReturn(compiledQuery);
        when(connection.getId()).thenReturn("testConnectionId");
        when(Keys.getOrAdd(ICreateCollectionMessage.class.toString())).thenReturn(wrapperKey);
        when(IOC.resolve(eq(wrapperKey), eq(message))).thenReturn(wrapper);
    }

    @Test
    public void should_PrepareQuery() throws Exception {
        task.setConnection(connection);
        task.prepare(message);

        verify(wrapper, times(1)).getCollection();
        verify(wrapper, times(1)).getIndexes();

        verifyStatic(times(1));
        IOC.resolve(eq(Keys.getOrAdd(ICompiledQuery.class.toString())), eq(connection), anyObject());
        verifyStatic(times(1));
        IOC.resolve(eq(Keys.getOrAdd(ICreateCollectionMessage.class.toString())), eq(message));
    }

    @Test(expected = TaskPrepareException.class)
    public void should_ThrowsException_WithReason_InvalidMessage() throws Exception {
        when(wrapper.getCollection()).thenReturn(null);
        when(wrapper.getIndexes()).thenReturn(null);
        task.setConnection(connection);
        task.prepare(message);
    }

    @Test
    public void should_ExecuteQuery() throws Exception {
        field(PSQLCreateCollectionTask.class, "query").set(task, compiledQuery);
        field(PSQLCreateCollectionTask.class, "message").set(task, wrapper);
        task.execute();

        verify(compiledQuery).execute();
    }

    @Test
    public void should_SetConnection() throws Exception {
        when(connection.getId()).thenReturn("testConnectionId");
        IStorageConnection storageConnectionBefore = (IStorageConnection) MemberModifier.field(PSQLCreateCollectionTask.class, "connection").get(task);
        task.setConnection(connection);
        IStorageConnection storageConnectionAfter = (IStorageConnection) MemberModifier.field(PSQLCreateCollectionTask.class, "connection").get(task);

        assertNull(storageConnectionBefore);
        assertNotNull(storageConnectionAfter);
        assertEquals(connection, storageConnectionAfter);
    }

    @After
    public void resetMocks() throws Exception {
        reset(compiledQuery, message, wrapper, connection);
    }
}
