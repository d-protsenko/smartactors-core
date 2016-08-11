package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
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
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, CreateAsyncOperationTask.class})
public class DeleteAsyncOperationTaskTest {

    private DeleteAsyncOperationTask testTask;
    private IDatabaseTask targetTask;
    private IStorageConnection connection;

    private IField documentField;
    private IField collectionNameField;

    @Before
    public void prepare () throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        connection = mock(IStorageConnection.class);
        targetTask = mock(IDatabaseTask.class);

        documentField = mock(IField.class);
        collectionNameField = mock(IField.class);

        Key fieldKey = mock(Key.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(fieldKey);

        when(IOC.resolve(fieldKey, "document")).thenReturn(documentField);
        when(IOC.resolve(fieldKey, "collectionName")).thenReturn(collectionNameField);

        testTask = new DeleteAsyncOperationTask(connection);

        verifyStatic(times(2));
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(fieldKey, "document");

        verifyStatic();
        IOC.resolve(fieldKey, "collectionName");
    }

    @Test
    public void MustCorrectPrepare() throws ResolutionException, ReadValueException, InvalidArgumentException, TaskPrepareException {
        IObject query = mock(IObject.class);

        String collectionName = "examplecn";
        Object document = mock(Object.class);

        when(collectionNameField.in(query)).thenReturn(collectionName);
        when(documentField.in(query)).thenReturn(document);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.delete")).thenReturn(upsertTaskKey);

        when(IOC.resolve(upsertTaskKey, connection, collectionName, document)).thenReturn(targetTask);

        testTask.prepare(query);

        verifyStatic();
        Keys.getOrAdd("db.collection.delete");

        verify(collectionNameField).in(query);
        verify(documentField).in(query);

        verifyStatic();
        IOC.resolve(upsertTaskKey, connection, collectionName, document);
    }

    @Test
    public void MustInCorrectPrepareWhenKeysGetOrAddThrowException() throws ResolutionException {
        IObject query = mock(IObject.class);

        when(Keys.getOrAdd("db.collection.delete")).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd("db.collection.delete");
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenFieldInThrowReadValueException() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IObject query = mock(IObject.class);

        when(collectionNameField.in(query)).thenThrow(new ReadValueException());

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.delete")).thenReturn(upsertTaskKey);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd("db.collection.delete");

            verify(collectionNameField).in(query);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenFieldInThrowInvlidArgumentException() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IObject query = mock(IObject.class);

        when(collectionNameField.in(query)).thenThrow(new InvalidArgumentException(""));

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.delete")).thenReturn(upsertTaskKey);

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {

            verifyStatic();
            Keys.getOrAdd("db.collection.delete");

            verify(collectionNameField).in(query);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectPrepareWhenIOCResolveThrowException() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IObject query = mock(IObject.class);

        String collectionName = "examplecn";
        Object document = mock(Object.class);

        when(collectionNameField.in(query)).thenReturn(collectionName);
        when(documentField.in(query)).thenReturn(document);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.delete")).thenReturn(upsertTaskKey);

        when(IOC.resolve(upsertTaskKey, connection, collectionName, document)).thenThrow(new ResolutionException(""));

        try {
            testTask.prepare(query);
        } catch (TaskPrepareException e) {
            verifyStatic();
            Keys.getOrAdd("db.collection.delete");

            verify(collectionNameField).in(query);
            verify(documentField).in(query);

            verifyStatic();
            IOC.resolve(upsertTaskKey, connection, collectionName, document);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustCorrectExecute() throws TaskExecutionException, ReadValueException, InvalidArgumentException, TaskPrepareException, ResolutionException {
        MustCorrectPrepare();
        testTask.execute();

        verify(targetTask).execute();
    }
}
