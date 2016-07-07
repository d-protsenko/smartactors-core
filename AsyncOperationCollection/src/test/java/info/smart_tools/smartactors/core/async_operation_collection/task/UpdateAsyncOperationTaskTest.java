package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.wrapper.update.UpdateAsyncOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.update.UpdateItem;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class UpdateAsyncOperationTaskTest {
    private UpdateAsyncOperationTask testTask;
    private IDatabaseTask targetTask;

    @Before
    public void prepare () throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        targetTask = mock(IDatabaseTask.class);
        testTask = new UpdateAsyncOperationTask(targetTask);
    }

    @Test
    public void MustCorrectPrepareQuery() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);

        UpdateAsyncOperationQuery updateAsyncOperationQuery = mock(UpdateAsyncOperationQuery.class);
        Key updateQueryKey = mock(Key.class);
        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenReturn(updateQueryKey);
        when(IOC.resolve(updateQueryKey, query)).thenReturn(updateAsyncOperationQuery);

        UpdateItem updateItem = mock(UpdateItem.class);
        when(updateAsyncOperationQuery.getUpdateItem()).thenReturn(updateItem);

        Key ioBjectKey = mock(Key.class);
        when(Keys.getOrAdd(IObject.class.toString())).thenReturn(ioBjectKey);
        when(IOC.resolve(ioBjectKey, updateAsyncOperationQuery)).thenReturn(query);

        testTask.prepare(query);

        verifyStatic();
        Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
        verifyStatic();
        IOC.resolve(updateQueryKey, query);

        verify(updateAsyncOperationQuery).getUpdateItem();

        verify(updateItem).setIsDone(true);

        verifyStatic();
        Keys.getOrAdd(IObject.class.toString());
        verifyStatic();
        IOC.resolve(ioBjectKey, updateAsyncOperationQuery);

        verify(targetTask).prepare(query);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenKeysThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
            return;
        }
        assertTrue("Must throw exception", false);
    }



    @Test
    public void MustInCorrectPrepareQueryWhenIOCResolveThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        Key updateQueryKey = mock(Key.class);
        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenReturn(updateQueryKey);
        when(IOC.resolve(updateQueryKey, query)).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(updateQueryKey, query);

            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenMessageThrowReadValueException() throws ResolutionException, ReadValueException {
        IObject query = mock(IObject.class);

        UpdateAsyncOperationQuery updateAsyncOperationQuery = mock(UpdateAsyncOperationQuery.class);
        Key updateQueryKey = mock(Key.class);
        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenReturn(updateQueryKey);
        when(IOC.resolve(updateQueryKey, query)).thenReturn(updateAsyncOperationQuery);

        when(updateAsyncOperationQuery.getUpdateItem()).thenThrow(new ReadValueException());

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(updateQueryKey, query);

            verify(updateAsyncOperationQuery).getUpdateItem();
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenUpdateItemThrowChangeValueException() throws ResolutionException, ChangeValueException, ReadValueException {
        IObject query = mock(IObject.class);

        UpdateAsyncOperationQuery updateAsyncOperationQuery = mock(UpdateAsyncOperationQuery.class);
        Key updateQueryKey = mock(Key.class);
        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenReturn(updateQueryKey);
        when(IOC.resolve(updateQueryKey, query)).thenReturn(updateAsyncOperationQuery);

        UpdateItem updateItem = mock(UpdateItem.class);
        when(updateAsyncOperationQuery.getUpdateItem()).thenReturn(updateItem);

        doThrow(new ChangeValueException()).when(updateItem).setIsDone(true);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(updateQueryKey, query);

            verify(updateAsyncOperationQuery).getUpdateItem();

            verify(updateItem).setIsDone(true);
            return;
        }
        assertTrue("Must throw exception", false);

    }

    @Test
    public void MustInCorrectPrepareQueryWhenTargetTaskThrowException() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);

        UpdateAsyncOperationQuery updateAsyncOperationQuery = mock(UpdateAsyncOperationQuery.class);
        Key updateQueryKey = mock(Key.class);
        when(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString())).thenReturn(updateQueryKey);
        when(IOC.resolve(updateQueryKey, query)).thenReturn(updateAsyncOperationQuery);

        UpdateItem updateItem = mock(UpdateItem.class);
        when(updateAsyncOperationQuery.getUpdateItem()).thenReturn(updateItem);

        Key ioBjectKey = mock(Key.class);
        when(Keys.getOrAdd(IObject.class.toString())).thenReturn(ioBjectKey);
        when(IOC.resolve(ioBjectKey, updateAsyncOperationQuery)).thenReturn(query);

        doThrow(new TaskPrepareException("")).when(targetTask).prepare(query);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e){

            verifyStatic();
            Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(updateQueryKey, query);

            verify(updateAsyncOperationQuery).getUpdateItem();

            verify(updateItem).setIsDone(true);

            verifyStatic();
            Keys.getOrAdd(IObject.class.toString());
            verifyStatic();
            IOC.resolve(ioBjectKey, updateAsyncOperationQuery);

            verify(targetTask).prepare(query);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustCorrectSetConnection() throws TaskSetConnectionException {

        StorageConnection connection = mock(StorageConnection.class);

        testTask.setConnection(connection);

        verify(targetTask).setConnection(connection);
    }

    @Test
    public void MustCorrectExecute() throws TaskExecutionException {
        testTask.execute();

        verify(targetTask).execute();
    }
}