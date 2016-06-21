package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDateTime;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class UpsertIntoCachedCollectionTaskTest {

    private UpsertIntoCachedCollectionTask task;
    private IDatabaseTask upsertTask;
    private static IKey iocKey;

    @BeforeClass
    public static void setUpBefore() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        iocKey = mock(IKey.class);
        when(Keys.getOrAdd(anyString())).thenReturn(iocKey);

    }

    @Before
    public void setUp() throws ReadValueException, ChangeValueException {

        upsertTask = mock(IDatabaseTask.class);
        UpsertIntoCachedCollectionConfig cachedCollectionConfig = mock(UpsertIntoCachedCollectionConfig.class);
        when(cachedCollectionConfig.getKey()).thenReturn("key");
        when(cachedCollectionConfig.getUpsertTask()).thenReturn(upsertTask);

        task = new UpsertIntoCachedCollectionTask(cachedCollectionConfig);



    }

    @Test
    public void ShouldSetConnectionToNestedTask() throws TaskSetConnectionException {

        StorageConnection connection = mock(StorageConnection.class);
        task.setConnection(connection);
        verify(upsertTask).setConnection(eq(connection));
    }

    @Test
    public void ShouldExecuteNestedTask() throws TaskExecutionException {

        task.execute();
        verify(upsertTask).execute();
    }

    @Test
    public void ShouldPrepareUpsertQuery() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {

        IObject rawQuery = mock(IObject.class);
        UpsertIntoCachedCollectionQuery query = mock(UpsertIntoCachedCollectionQuery.class);
        IFieldName fieldName = mock(IFieldName.class);
        when(IOC.resolve(eq(iocKey), any(IObject.class))).thenReturn(query);
        when(IOC.resolve(iocKey, "key")).thenReturn(fieldName);
        when(rawQuery.getValue(eq(fieldName))).thenReturn("keyField");
        when(IOC.resolve(iocKey, "keyField")).thenReturn("keyValue");

        IObject wrapped = mock(IObject.class);
        when(query.wrapped()).thenReturn(wrapped);

        task.prepare(rawQuery);

        verify(query).setStartDateTime(any(LocalDateTime.class));
        verify(upsertTask).prepare(eq(wrapped));
    }

    @Test(expected = TaskPrepareException.class)
    public void ShouldThrowException_When_KeyValueIsNull() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {

        IObject rawQuery = mock(IObject.class);
        UpsertIntoCachedCollectionQuery query = mock(UpsertIntoCachedCollectionQuery.class);
        IFieldName fieldName = mock(IFieldName.class);
        when(IOC.resolve(eq(iocKey), any(IObject.class))).thenReturn(query);
        when(IOC.resolve(iocKey, "key")).thenReturn(fieldName);
        when(rawQuery.getValue(eq(fieldName))).thenReturn("keyField");
        when(IOC.resolve(iocKey, "keyField")).thenReturn(null);

        task.prepare(rawQuery);
    }
}
