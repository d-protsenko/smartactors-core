package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class UpsertIntoCachedCollectionTaskTest {

    private UpsertIntoCachedCollectionTask task;
    private IDatabaseTask upsertTask;

    private IField startDateTimeField;
    private IField collectionNameField;
    private IField documentField;
    private IStorageConnection connection;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        startDateTimeField = mock(IField.class);
        collectionNameField = mock(IField.class);
        documentField = mock(IField.class);
        IKey keyField = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(keyField);
        when(IOC.resolve(keyField, "document/startDateTime")).thenReturn(startDateTimeField);
        when(IOC.resolve(keyField, "document")).thenReturn(documentField);
        when(IOC.resolve(keyField, "collectionName")).thenReturn(collectionNameField);

        upsertTask = mock(IDatabaseTask.class);
        connection = mock(IStorageConnection.class);
        task = new UpsertIntoCachedCollectionTask(connection);
    }

    @Test
    public void ShouldPrepareUpsertQuery() throws Exception {

        IObject query = mock(IObject.class);
        IObject doc = mock(IObject.class);
        when(collectionNameField.in(query)).thenReturn("collectionName");
        when(documentField.in(query)).thenReturn(doc);

        when(startDateTimeField.in(query)).thenReturn(null);
        task.prepare(query);

        verifyStatic();
        IOC.resolve(
            Keys.getOrAdd("db.collection.upsert"),
            connection,
            "collectionName",
            doc
        );
    }

    @Test(expected = TaskPrepareException.class)
    public void ShouldThrowException_When_ResolutionExceptionIsThrown() throws Exception {

        IObject rawQuery = mock(IObject.class);
        when(startDateTimeField.in(rawQuery)).thenThrow(ChangeValueException.class);

        task.prepare(rawQuery);
    }
}
