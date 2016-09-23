package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.plugin.datetime_formatter_strategy.PluginDateTimeFormatter;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import info.smart_tools.smartactors.plugin.postgres_db_tasks.PostgresDBTasksPlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

public class GetItemFromCachedCollectionTaskTest {

    private GetItemFromCachedCollectionTask testTask;

    @BeforeClass
    public static void prepareIOC() throws PluginException, ProcessExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        new PostgresDBTasksPlugin(bootstrap).load();
        new PluginDateTimeFormatter(bootstrap).load();
        bootstrap.start();
    }

    @Before
    public void setUp() throws Exception {

        IStorageConnection connection = mock(IStorageConnection.class);
        testTask = new GetItemFromCachedCollectionTask(connection);
    }

    @Test
    public void MustCorrectPrepareQueryForSelecting() throws Exception {

        IObject query = spy(new DSObject("{" +
            "\"keyName\": \"name\"," +
            " \"key\": \"keyValue\"," +
            " \"collectionName\": \"collection\"" +
            "}"
        ));

        testTask.prepare(query);

        FieldName keyNameFN = new FieldName("keyName");
        FieldName keyValueFN = new FieldName("key");
        FieldName collectionNameFN = new FieldName("collectionName");

        verify(query).getValue(eq(keyNameFN));
        verify(query).getValue(eq(keyValueFN));
        verify(query).getValue(eq(collectionNameFN));
    }

    @Test(expected = TaskPrepareException.class)
    public void MustInCorrectPrepareQueryForSelectingWhenIOCThrowException() throws ResolutionException, TaskPrepareException, ReadValueException, ChangeValueException {

        testTask.prepare(null);
    }
}