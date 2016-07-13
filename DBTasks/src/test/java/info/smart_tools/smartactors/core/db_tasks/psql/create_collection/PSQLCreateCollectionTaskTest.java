package info.smart_tools.smartactors.core.db_tasks.psql.create_collection;

import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ICollectionName;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield.IField;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({ IOC.class, Keys.class })
@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
public class PSQLCreateCollectionTaskTest {
    private PSQLCreateCollectionTask task = PSQLCreateCollectionTask.create();
    private static JDBCCompiledQuery compiledQuery = mock(JDBCCompiledQuery.class);
    private static IObject message = mock(IObject.class);
    private static IStorageConnection connection = mock(IStorageConnection.class);
    private static IField collectionField;
    private static IField indexesField;

    @BeforeClass
    public static void setUp() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey fieldKey = mock(IKey.class);

        collectionField = mock(IField.class);
        indexesField = mock(IField.class);

        Map<String, String> indexes = new HashMap<>();
        indexes.put("meta.tags", "tags");
        ICollectionName collectionName = mock(ICollectionName.class);

        when(connection.compileQuery(any())).thenReturn(compiledQuery);

        when(Keys.getOrAdd(IField.class.toString())).thenReturn(fieldKey);
        when(collectionName.toString()).thenReturn("testCollection");
        when(collectionField.in(message)).thenReturn(collectionName);
        when(indexesField.in(message)).thenReturn(indexes);
        when(connection.getId()).thenReturn("testConnectionId");
        when(IOC.resolve(eq(fieldKey), eq("collection"))).thenReturn(collectionField);
        when(IOC.resolve(eq(fieldKey), eq("indexes"))).thenReturn(indexesField);
    }

    @Test
    public void should_PrepareQuery() throws Exception {
        task.setConnection(connection);
        task.prepare(message);

        verify(collectionField, times(1)).in(message);
        verify(indexesField, times(1)).in(message);
        verify(connection).compileQuery(any());
    }

    @Test(expected = TaskPrepareException.class)
    public void should_ThrowsException_WithReason_InvalidMessage() throws Exception {
        when(collectionField.in(message)).thenReturn(null);
        when(indexesField.in(message)).thenReturn(null);
        task.setConnection(connection);
        task.prepare(message);
    }

    @Test
    public void should_ExecuteQuery() throws Exception {
        field(PSQLCreateCollectionTask.class, "query").set(task, compiledQuery);
        field(PSQLCreateCollectionTask.class, "message").set(task, message);
        field(PSQLCreateCollectionTask.class, "executable").set(task, true);
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
        reset(compiledQuery, message, collectionField, indexesField, connection);
    }
}
