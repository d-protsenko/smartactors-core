package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectsFromCachedCollectionParameters;
import info.smart_tools.smartactors.core.cached_collection.wrapper.SearchCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.cglib.core.Local;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;


@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, LocalDateTime.class})
public class GetObjectFromCachedCollectionTaskTest {

    private GetObjectFromCachedCollectionTask testTask;
    private IDatabaseTask targetTask;

    private String collectionName = "collectionName";
    private String key = "key";

    private Key keyForKeyStorage;
    private Key fieldNameKey;

    private IFieldName eqFieldName;
    private IFieldName dateToFieldName;
    private IFieldName keyFieldName;

    @Before
    public void prepareTaskAndOthers() throws ResolutionException, ReadValueException, ChangeValueException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        GetObjectsFromCachedCollectionParameters params = mock(GetObjectsFromCachedCollectionParameters.class);

        targetTask = mock(IDatabaseTask.class);

        when(params.getKey()).thenReturn(key);
        when(params.getCollectionName()).thenReturn(collectionName);
        when(params.getTask()).thenReturn(targetTask);

        keyForKeyStorage = mock(Key.class);
        fieldNameKey = mock(Key.class);

        eqFieldName = mock(IFieldName.class);
        dateToFieldName = mock(IFieldName.class);
        keyFieldName = mock(IFieldName.class);

        when(IOC.getKeyForKeyStorage()).thenReturn(keyForKeyStorage);
        when(IOC.resolve(keyForKeyStorage, IFieldName.class.toString())).thenReturn(fieldNameKey);

        when(IOC.resolve(fieldNameKey, "$eq")).thenReturn(eqFieldName);
        when(IOC.resolve(fieldNameKey, "$date-to")).thenReturn(dateToFieldName);
        when(IOC.resolve(fieldNameKey, key)).thenReturn(keyFieldName);

        testTask = new GetObjectFromCachedCollectionTask(params);
    }

    @Test
    public void MustCorrectPrepareQueryForSelecting() throws ResolutionException, TaskPrepareException, ReadValueException, ChangeValueException {
        Key iobjectKey = mock(Key.class);
        when(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString())).thenReturn(iobjectKey);

        IObject testIObject = mock(IObject.class);
        when(IOC.resolve(iobjectKey)).thenReturn(testIObject);

        Key searchCachedCollectionKey = mock(Key.class);
        SearchCachedCollectionQuery criteriaQuery = mock(SearchCachedCollectionQuery.class);

        when(Keys.getOrAdd(SearchCachedCollectionQuery.class.toString())).thenReturn(searchCachedCollectionKey);
        when(IOC.resolve(searchCachedCollectionKey, testIObject)).thenReturn(criteriaQuery);

        IObject query = mock(IObject.class);
        String key = "keyasd";
        when(query.getValue(keyFieldName)).thenReturn(key);

        ISearchQuery iSearchQuery = mock(ISearchQuery.class);
        Key iSearchQueryKey = mock(Key.class);

        when(Keys.getOrAdd(ISearchQuery.class.toString())).thenReturn(iSearchQueryKey);
        when(IOC.resolve(iSearchQueryKey, query)).thenReturn(iSearchQuery);

        testTask.prepare(query);

        verify(query).getValue(keyFieldName);
        verify(testIObject).setValue(eqFieldName, key);
        verify(testIObject).setValue(eq(keyFieldName), eq(testIObject));

        verify(testIObject).setValue(eqFieldName, true);
        verify(criteriaQuery).setIsActive(testIObject);

        verify(testIObject).setValue(eq(dateToFieldName), any());// FIXME: 6/21/16 must test time
        verify(criteriaQuery).setIsActive(testIObject);

        verify(iSearchQuery).setCollectionName(collectionName);
        verify(iSearchQuery).setPageNumber(0);
        verify(iSearchQuery).setPageSize(any());// FIXME: 6/21/16 hardcode count must be fixed
        verify(iSearchQuery).setCriteria(testIObject);

        verify(targetTask).prepare(query);
    }

    @Test
    public void MustCorrectExecuteQuery() throws TaskExecutionException {
        testTask.execute();

        verify(targetTask).execute();
    }

    @Test
    public void MostCorrectlySetConnectionToNestedTask() throws TaskSetConnectionException {

        StorageConnection connection = mock(StorageConnection.class);

        testTask.setConnection(connection);

        verify(targetTask).setConnection(eq(connection));
    }

}