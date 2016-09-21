package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, GetAsyncOperationTask.class})
public class GetAsyncOperationTaskTest {
    private GetAsyncOperationTask testTask;
    private IStorageConnection connection;
    private IDatabaseTask targetTask;

    private IField callbackField;
    private IField equalsField;
    private IField filterField;
    private IField tokenField;
    private IField collectionNameField;

    @Before
    public void prepare () throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);


        connection = mock(IStorageConnection.class);
        targetTask = mock(IDatabaseTask.class);

        callbackField = mock(IField.class);
        filterField = mock(IField.class);
        equalsField = mock(IField.class);
        tokenField = mock(IField.class);
        collectionNameField = mock(IField.class);

        Key fieldKey = mock(Key.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(fieldKey);

        when(IOC.resolve(fieldKey, "callback")).thenReturn(callbackField);
        when(IOC.resolve(fieldKey, "$eq")).thenReturn(equalsField);
        when(IOC.resolve(fieldKey, "filter")).thenReturn(filterField);
        when(IOC.resolve(fieldKey, "token")).thenReturn(tokenField);
        when(IOC.resolve(fieldKey, "collectionName")).thenReturn(collectionNameField);

        testTask = new GetAsyncOperationTask(connection);

        verifyStatic(times(5));
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(fieldKey, "callback");
        verifyStatic();
        IOC.resolve(fieldKey, "$eq");
        verifyStatic();
        IOC.resolve(fieldKey, "filter");
        verifyStatic();
        IOC.resolve(fieldKey, "token");
        verifyStatic();
        IOC.resolve(fieldKey, "collectionName");

    }

    @Test
    public void MustCorrectPrepare() throws ResolutionException, ReadValueException, InvalidArgumentException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);

        IObject queryForNestedTask  = mock(IObject.class);
        IObject filterObject = mock(IObject.class);
        IObject eqKeyObject = mock(IObject.class);

        IKey iObjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);

        when(IOC.resolve(iObjectKey)).thenReturn(queryForNestedTask).thenReturn(filterObject).thenReturn(eqKeyObject);

        String token = "token";
        when(tokenField.in(query)).thenReturn(token);
        String collectionName = "collectionName";
        when(collectionNameField.in(query)).thenReturn(collectionName);
        Object callback = mock(Object.class);
        when(callbackField.in(query)).thenReturn(callback);

        IKey searchTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.search")).thenReturn(searchTaskKey);

        when(IOC.resolve(searchTaskKey, connection, collectionName, queryForNestedTask, callback)).thenReturn(targetTask);

        testTask.prepare(query);

        verifyStatic(times(3));
        Keys.getOrAdd(IObject.class.getCanonicalName());

        verifyStatic(times(3));
        IOC.resolve(iObjectKey);

        verify(tokenField).in(query);

        verify(equalsField).out(eqKeyObject, token);
        verify(tokenField).out(filterObject, eqKeyObject);
        verify(filterField).out(queryForNestedTask, filterObject);

        verifyStatic();
        Keys.getOrAdd("db.collection.search");

        verifyStatic();
        IOC.resolve(searchTaskKey, connection, collectionName, queryForNestedTask, callback);
    }

    @Test
    public void MustInCorrectPrepareWhenKeysGetOrAddThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(IObject.class.getCanonicalName());
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenIOCResolveThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        IKey iObjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);

        when(IOC.resolve(iObjectKey)).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd(IObject.class.getCanonicalName());
            verifyStatic();
            IOC.resolve(iObjectKey);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenFieldInThrowReadValueException() throws ReadValueException, InvalidArgumentException, ResolutionException {
        IObject query = mock(IObject.class);

        IObject queryForNestedTask  = mock(IObject.class);
        IObject filterObject = mock(IObject.class);

        IKey iObjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);

        when(IOC.resolve(iObjectKey)).thenReturn(queryForNestedTask).thenReturn(filterObject);

        when(tokenField.in(query)).thenThrow(new ReadValueException());

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic(times(2));
            Keys.getOrAdd(IObject.class.getCanonicalName());

            verifyStatic(times(2));
            IOC.resolve(iObjectKey);

            verify(tokenField).in(query);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenFieldInThrowInvalidArgumentException() throws ReadValueException, InvalidArgumentException, ResolutionException {
        IObject query = mock(IObject.class);

        IObject queryForNestedTask  = mock(IObject.class);
        IObject filterObject = mock(IObject.class);

        IKey iObjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);

        when(IOC.resolve(iObjectKey)).thenReturn(queryForNestedTask).thenReturn(filterObject);

        when(tokenField.in(query)).thenThrow(new InvalidArgumentException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic(times(2));
            Keys.getOrAdd(IObject.class.getCanonicalName());

            verifyStatic(times(2));
            IOC.resolve(iObjectKey);

            verify(tokenField).in(query);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenFieldOutThrowException() throws ResolutionException, ReadValueException, InvalidArgumentException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);

        IObject queryForNestedTask  = mock(IObject.class);
        IObject filterObject = mock(IObject.class);
        IObject eqKeyObject = mock(IObject.class);

        IKey iObjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);

        when(IOC.resolve(iObjectKey)).thenReturn(queryForNestedTask).thenReturn(filterObject).thenReturn(eqKeyObject);

        String token = "token";
        when(tokenField.in(query)).thenReturn(token);

        doThrow(new ChangeValueException()).when(equalsField).out(eqKeyObject, token);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic(times(3));
            Keys.getOrAdd(IObject.class.getCanonicalName());

            verifyStatic(times(3));
            IOC.resolve(iObjectKey);

            verify(tokenField).in(query);

            verify(equalsField).out(eqKeyObject, token);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustCorrectExecute() throws TaskExecutionException, ChangeValueException, ReadValueException, InvalidArgumentException, TaskPrepareException, ResolutionException {
        MustCorrectPrepare();
        testTask.execute();

        verify(targetTask).execute();
    }

}
