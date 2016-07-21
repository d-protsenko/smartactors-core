package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.AsyncDocument;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.CreateOperationQuery;
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
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class CreateAsyncOperationTaskTest {

    private CreateAsyncOperationTask testTask;
    private IDatabaseTask targetTask;

    @Before
    public void prepare () throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        targetTask = mock(IDatabaseTask.class);
        testTask = new CreateAsyncOperationTask(targetTask);
    }

    @Test
    public void MustCorrectPrepareQuery() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);
        IObject documentIObject = mock(IObject.class);

        CreateOperationQuery createOperationQuery = mock(CreateOperationQuery.class);
        Key createOperationKey = mock(Key.class);
        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenReturn(createOperationKey);
        when(IOC.resolve(createOperationKey, query)).thenReturn(createOperationQuery);

        AsyncDocument document = mock(AsyncDocument.class);
        Key documentKey = mock(Key.class);
        when(Keys.getOrAdd(AsyncDocument.class.toString())).thenReturn(documentKey);
        when(IOC.resolve(documentKey)).thenReturn(document);

        when(createOperationQuery.getIObject()).thenReturn(query);
        when(document.getIObject()).thenReturn(documentIObject);

        IObject asyncData = mock(IObject.class);
        String token = "testToken";
        String expiredTime = "testTime";

        when(createOperationQuery.getAsyncData()).thenReturn(asyncData);
        when(createOperationQuery.getExpiredTime()).thenReturn(expiredTime);
        when(createOperationQuery.getToken()).thenReturn(token);

        testTask.prepare(query);

        verifyStatic();
        Keys.getOrAdd(CreateOperationQuery.class.toString());
        verifyStatic();
        IOC.resolve(createOperationKey, query);

        verifyStatic();
        Keys.getOrAdd(AsyncDocument.class.toString());
        verifyStatic();
        IOC.resolve(documentKey);

        verify(createOperationQuery).getAsyncData();
        verify(document).setAsyncData(asyncData);

        verify(document).setDoneFlag(false);

        verify(createOperationQuery).getToken();
        verify(document).setToken(token);

        verify(createOperationQuery).getExpiredTime();
        verify(document).setExpiredTime(expiredTime);

        verify(document).getIObject();
        verify(createOperationQuery).setDocument(documentIObject);

        verify(createOperationQuery).getIObject();
        verify(targetTask).prepare(query);
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

    @Test
    public void MustInCorrectPrepareQueryWhenKeysThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(CreateOperationQuery.class.toString());
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenIOCThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        Key createOperationKey = mock(Key.class);
        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenReturn(createOperationKey);
        when(IOC.resolve(createOperationKey, query)).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(CreateOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(createOperationKey, query);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenQueryWrapperThrowReadValueException() throws ResolutionException, ReadValueException {
        IObject query = mock(IObject.class);

        CreateOperationQuery createOperationQuery = mock(CreateOperationQuery.class);
        Key createOperationKey = mock(Key.class);
        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenReturn(createOperationKey);
        when(IOC.resolve(createOperationKey, query)).thenReturn(createOperationQuery);

        AsyncDocument document = mock(AsyncDocument.class);
        Key documentKey = mock(Key.class);
        when(Keys.getOrAdd(AsyncDocument.class.toString())).thenReturn(documentKey);
        when(IOC.resolve(documentKey)).thenReturn(document);

        when(createOperationQuery.getAsyncData()).thenThrow(new ReadValueException());

        try {
            testTask.prepare(query);

        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(CreateOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(createOperationKey, query);

            verifyStatic();
            Keys.getOrAdd(AsyncDocument.class.toString());
            verifyStatic();
            IOC.resolve(documentKey);

            verify(createOperationQuery).getAsyncData();
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenDocumentThrowChangeValueException() throws ResolutionException, ReadValueException, ChangeValueException {
        IObject query = mock(IObject.class);

        CreateOperationQuery createOperationQuery = mock(CreateOperationQuery.class);
        Key createOperationKey = mock(Key.class);
        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenReturn(createOperationKey);
        when(IOC.resolve(createOperationKey, query)).thenReturn(createOperationQuery);

        AsyncDocument document = mock(AsyncDocument.class);
        Key documentKey = mock(Key.class);
        when(Keys.getOrAdd(AsyncDocument.class.toString())).thenReturn(documentKey);
        when(IOC.resolve(documentKey)).thenReturn(document);

        IObject asyncData = mock(IObject.class);

        when(createOperationQuery.getAsyncData()).thenReturn(asyncData);

        doThrow(new ChangeValueException()).when(document).setAsyncData(asyncData);

        try {
            testTask.prepare(query);

        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(CreateOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(createOperationKey, query);

            verifyStatic();
            Keys.getOrAdd(AsyncDocument.class.toString());
            verifyStatic();
            IOC.resolve(documentKey);

            verify(createOperationQuery).getAsyncData();
            verify(document).setAsyncData(asyncData);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenTargetTaskThrowException() throws ResolutionException, ReadValueException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);
        IObject documentIObject = mock(IObject.class);

        CreateOperationQuery createOperationQuery = mock(CreateOperationQuery.class);
        Key createOperationKey = mock(Key.class);
        when(Keys.getOrAdd(CreateOperationQuery.class.toString())).thenReturn(createOperationKey);
        when(IOC.resolve(createOperationKey, query)).thenReturn(createOperationQuery);

        AsyncDocument document = mock(AsyncDocument.class);
        Key documentKey = mock(Key.class);
        when(Keys.getOrAdd(AsyncDocument.class.toString())).thenReturn(documentKey);
        when(IOC.resolve(documentKey)).thenReturn(document);

        when(createOperationQuery.getIObject()).thenReturn(query);
        when(document.getIObject()).thenReturn(documentIObject);

        IObject asyncData = mock(IObject.class);
        String token = "testToken";
        String expiredTime = "testTime";

        when(createOperationQuery.getAsyncData()).thenReturn(asyncData);
        when(createOperationQuery.getExpiredTime()).thenReturn(expiredTime);
        when(createOperationQuery.getToken()).thenReturn(token);

        doThrow(new TaskPrepareException("")).when(targetTask).prepare(query);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(CreateOperationQuery.class.toString());
            verifyStatic();
            IOC.resolve(createOperationKey, query);

            verifyStatic();
            Keys.getOrAdd(AsyncDocument.class.toString());
            verifyStatic();
            IOC.resolve(documentKey);

            verify(createOperationQuery).getAsyncData();
            verify(document).setAsyncData(asyncData);

            verify(document).setDoneFlag(false);

            verify(createOperationQuery).getToken();
            verify(document).setToken(token);

            verify(createOperationQuery).getExpiredTime();
            verify(document).setExpiredTime(expiredTime);

            verify(document).getIObject();
            verify(createOperationQuery).setDocument(documentIObject);

            verify(createOperationQuery).getIObject();
            verify(targetTask).prepare(query);
            return;
        }
        assertTrue("Must throw exception", false);
    }
}