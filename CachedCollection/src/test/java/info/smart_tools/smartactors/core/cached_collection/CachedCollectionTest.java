package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionParameters;
import info.smart_tools.smartactors.core.cached_collection.wrapper.DeleteFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class CachedCollectionTest {

    private ICachedCollection collection;
    private StorageConnection connection;

    @Before
    public void setUp() throws ReadValueException, ChangeValueException, InvalidArgumentException, PoolTakeException {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IPool connectionPool = mock(IPool.class);
        connection = mock(StorageConnection.class);
        when(connectionPool.take()).thenReturn(connection);
        CachedCollectionConfig config = mock(CachedCollectionConfig.class);
        when(config.getConnectionPool()).thenReturn(connectionPool);
        collection = new CachedCollection(config);
    }

    @Test
    public void ShouldDeleteObject()
        throws DeleteCacheItemException, ResolutionException, TaskSetConnectionException, IllegalAccessException, ReadValueException,
                ChangeValueException, TaskPrepareException, TaskExecutionException {

        IKey keyConnection = mock(IKey.class);
        when(Keys.getOrAdd(StorageConnection.class.toString())).thenReturn(keyConnection);
        when(IOC.resolve(keyConnection, connection)).thenReturn(connection);
        IDatabaseTask deleteTask = mock(IDatabaseTask.class);
        IObject query = mock(IObject.class);
        CachedCollectionParameters parameters = mock(CachedCollectionParameters.class);
        when(parameters.getQuery()).thenReturn(query);
        when(parameters.getTask()).thenReturn(deleteTask);

        DeleteFromCachedCollectionQuery message = mock(DeleteFromCachedCollectionQuery.class);
        when(message.getKey()).thenReturn("key");
        IKey keyMessage = mock(IKey.class);
        when(Keys.getOrAdd(DeleteFromCachedCollectionQuery.class.toString())).thenReturn(keyMessage);
        when(IOC.resolve(keyMessage, query)).thenReturn(message);

        collection.delete(query);

        verify(deleteTask).setConnection(eq(connection));
        verify(deleteTask).prepare(eq(query));
        verify(deleteTask).execute();
    }

    @Test
    public void ShouldUpsertObject()
        throws ResolutionException, TaskSetConnectionException, IllegalAccessException, ReadValueException,
        ChangeValueException, TaskPrepareException, TaskExecutionException, UpsertCacheItemException {

        IKey keyConnection = mock(IKey.class);
        when(Keys.getOrAdd(StorageConnection.class.toString())).thenReturn(keyConnection);
        when(IOC.resolve(keyConnection, connection)).thenReturn(connection);
        IDatabaseTask upsertTask = mock(IDatabaseTask.class);
        IObject query = mock(IObject.class);
        CachedCollectionParameters parameters = mock(CachedCollectionParameters.class);
        when(parameters.getQuery()).thenReturn(query);
        when(parameters.getTask()).thenReturn(upsertTask);

        UpsertIntoCachedCollectionQuery message = mock(UpsertIntoCachedCollectionQuery.class);
        when(message.getKey()).thenReturn("key");
        IKey keyMessage = mock(IKey.class);
        when(Keys.getOrAdd(UpsertIntoCachedCollectionQuery.class.toString())).thenReturn(keyMessage);
        when(IOC.resolve(keyMessage, query)).thenReturn(message);

        collection.upsert(query);

        verify(upsertTask).setConnection(eq(connection));
        verify(upsertTask).prepare(eq(query));
        verify(upsertTask).execute();
    }

    @Test
    public void ShouldReadObject()
        throws ResolutionException, TaskSetConnectionException, IllegalAccessException, ReadValueException,
        ChangeValueException, TaskPrepareException, TaskExecutionException, GetCacheItemException {

        IKey keyConnection = mock(IKey.class);
        when(Keys.getOrAdd(StorageConnection.class.toString())).thenReturn(keyConnection);
        when(IOC.resolve(keyConnection, connection)).thenReturn(connection);
        IDatabaseTask readTask = mock(IDatabaseTask.class);
        IObject query = mock(IObject.class);
        CachedCollectionParameters parameters = mock(CachedCollectionParameters.class);
        when(parameters.getQuery()).thenReturn(query);
        when(parameters.getTask()).thenReturn(readTask);

        GetObjectFromCachedCollectionQuery message = mock(GetObjectFromCachedCollectionQuery.class);
        when(message.getKey()).thenReturn("key");
        IObject searchResult = mock(IObject.class);
        when(message.getSearchResult()).thenReturn(Stream.of(searchResult));
        IKey keyMessage = mock(IKey.class);
        when(Keys.getOrAdd(GetObjectFromCachedCollectionQuery.class.toString())).thenReturn(keyMessage);
        when(IOC.resolve(keyMessage, query)).thenReturn(message);

        List<IObject> items = collection.getItems(query);

        verify(readTask).setConnection(eq(connection));
        verify(readTask).prepare(eq(query));
        verify(readTask).execute();
        assertEquals(items.get(0), searchResult);
    }
}
