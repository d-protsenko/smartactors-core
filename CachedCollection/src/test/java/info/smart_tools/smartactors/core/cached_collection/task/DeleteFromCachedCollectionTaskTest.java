package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionConfig;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class DeleteFromCachedCollectionTaskTest {

    private DeleteFromCachedCollectionTask task;
    private IDatabaseTask upsertTask;

    @Before
    public void setUp() throws ReadValueException, ChangeValueException {

        upsertTask = mock(IDatabaseTask.class);
        task = new DeleteFromCachedCollectionTask(upsertTask);
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
}
