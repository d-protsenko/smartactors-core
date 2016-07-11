package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, UpdateAsyncOperationTask.class})
public class UpdateAsyncOperationTaskTest {
    private UpdateAsyncOperationTask testTask;
    private IDatabaseTask targetTask;

    private IField updateIObjectField;
    private IField doneFlagField;

    @Before
    public void prepare () throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        updateIObjectField = mock(IField.class);
        doneFlagField = mock(IField.class);

        Key fieldKey = mock(Key.class);
        when(Keys.getOrAdd(IField.class.toString())).thenReturn(fieldKey);

        when(IOC.resolve(fieldKey, "updateItem")).thenReturn(updateIObjectField);

        when(IOC.resolve(fieldKey, "done")).thenReturn(doneFlagField);

        targetTask = mock(IDatabaseTask.class);

        testTask = new UpdateAsyncOperationTask(targetTask);

        verifyStatic(times(2));
        Keys.getOrAdd(IField.class.toString());

        verifyStatic();
        IOC.resolve(fieldKey, "updateItem");

        verifyStatic();
        IOC.resolve(fieldKey, "done");
    }

    @Test
    public void MustCorrectPrepareQuery() throws ReadValueException, InvalidArgumentException, TaskPrepareException, ChangeValueException {
        IObject query = mock(IObject.class);

        IObject updateItem = mock(IObject.class);

        when(updateIObjectField.in(query)).thenReturn(updateItem);

        testTask.prepare(query);

        verify(updateIObjectField).in(query);

        verify(doneFlagField).out(updateItem, true);

        verify(targetTask).prepare(query);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenMessageThrowReadValueException() throws ReadValueException, InvalidArgumentException {
        IObject query = mock(IObject.class);

        when(updateIObjectField.in(query)).thenThrow(new ReadValueException());

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verify(updateIObjectField).in(query);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectPrepareQueryWhenUpdateItemThrowChangeValueException() throws ReadValueException, InvalidArgumentException, ChangeValueException {
        IObject query = mock(IObject.class);

        IObject updateItem = mock(IObject.class);

        when(updateIObjectField.in(query)).thenReturn(updateItem);

        doThrow(new ChangeValueException()).when(doneFlagField).out(updateItem, true);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verify(updateIObjectField).in(query);

            verify(doneFlagField).out(updateItem, true);

            return;
        }
        assertTrue("Must throw exception", false);

    }

    @Test
    public void MustInCorrectPrepareQueryWhenTargetTaskThrowException() throws ReadValueException, InvalidArgumentException, ChangeValueException, TaskPrepareException {
        IObject query = mock(IObject.class);

        IObject updateItem = mock(IObject.class);

        when(updateIObjectField.in(query)).thenReturn(updateItem);

        doThrow(new TaskPrepareException("")).when(targetTask).prepare(query);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verify(updateIObjectField).in(query);

            verify(doneFlagField).out(updateItem, true);

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