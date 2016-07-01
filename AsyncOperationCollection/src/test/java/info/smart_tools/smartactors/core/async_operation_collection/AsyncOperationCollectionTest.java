package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.get_item.GetAsyncOperationQuery;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, CollectionName.class, PoolGuard.class})
public class AsyncOperationCollectionTest {
    private AsyncOperationCollection testCollection;
    private IPool pool;
    private CollectionName collectionName;

    @Before
    public void prepare () throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException, QueryBuildException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);
        mockStatic(CollectionName.class);
        mockStatic(PoolGuard.class);

        pool = mock(IPool.class);

        collectionName = mock(CollectionName.class);

        when(CollectionName.fromString("async_operation")).thenReturn(collectionName);

        testCollection = new AsyncOperationCollection(pool, "async_operation");
    }

    @Test
    public void MustCorrectGetAsyncOperationWhenFirstGetItemNotNull() throws
            Exception {

        String token = "token";

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withAnyArguments().thenReturn(poolGuard);
//        whenNew(PoolGuard.class).withArguments(pool).thenReturn(poolGuard);

        GetAsyncOperationTask getAsyncOperationTask = mock(GetAsyncOperationTask.class);
        Key getAsyncOperationTaskKey = mock(Key.class);
        when(Keys.getOrAdd(GetAsyncOperationTask.class.toString())).thenReturn(getAsyncOperationTaskKey);
        when(IOC.resolve(getAsyncOperationTaskKey)).thenReturn(getAsyncOperationTask);

        GetAsyncOperationQuery getAsyncOperationQuery = mock(GetAsyncOperationQuery.class);
        Key getAsyncOperationQueryKey = mock(Key.class);
        when(Keys.getOrAdd(GetAsyncOperationQuery.class.toString())).thenReturn(getAsyncOperationQueryKey);
        when(IOC.resolve(getAsyncOperationQueryKey)).thenReturn(getAsyncOperationQuery);

        IObject wrapped = mock(IObject.class);
        when(getAsyncOperationQuery.wrapped()).thenReturn(wrapped);

        StorageConnection connection = mock(StorageConnection.class);
        when(poolGuard.getObject()).thenReturn(connection);
        Key connectionKey = mock(Key.class);
        when(Keys.getOrAdd(StorageConnection.class.toString())).thenReturn(connectionKey);
        when(IOC.resolve(connectionKey, connection)).thenReturn(connection);

        List<IObject> results = new ArrayList<>();
        results.add(mock(IObject.class));
        when(getAsyncOperationQuery.getSearchResult()).thenReturn(results);

        assertTrue("Must return true value", testCollection.getAsyncOperation(token) == results.get(0));

        verify(getAsyncOperationQuery).setCollectionName(collectionName);
        verify(getAsyncOperationQuery).setToken(token);
        verify(poolGuard).getObject();
        verify(getAsyncOperationTask).setConnection(connection);
        verify(getAsyncOperationTask).prepare(wrapped);
        verify(getAsyncOperationTask).execute();

        verify(getAsyncOperationQuery).getSearchResult();
//        verify(poolGuard).close();
    }


}