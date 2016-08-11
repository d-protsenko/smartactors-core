package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class GetItemFromCachedCollectionTaskTest {

    private GetItemFromCachedCollectionTask testTask;
    private IDatabaseTask targetTask;
    private IObject queryForNestedTask;

    @BeforeClass
    public static void initIOC() throws PluginException, ProcessExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        bootstrap.start();
    }

    @Before
    public void prepare() throws Exception {
        targetTask = mock(IDatabaseTask.class);
        IOC.register(Keys.getOrAdd("db.collection.search"), new IResolveDependencyStrategy() {
            @Override
            public IDatabaseTask resolve(Object... args) throws ResolveDependencyStrategyException {
                queryForNestedTask = (IObject) args[2];
                return targetTask;
            }
        });

        IStorageConnection connection = mock(IStorageConnection.class);
        testTask = new GetItemFromCachedCollectionTask(connection);
    }

    @Test
    public void MustCorrectPrepareQueryForSelecting() throws Exception {
        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{ \"keyName\": \"keyName\", \"key\": \"keyValue\" }");

        testTask.prepare(query);

        IField criteriaEqualsIsActiveField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "criteria/isActive/$eq");
        IField criteriaDateToStartDateTimeField = IOC.resolve(
                Keys.getOrAdd(IField.class.getCanonicalName()), "criteria/startDateTime/$date-to");
        assertEquals(true, criteriaEqualsIsActiveField.in(queryForNestedTask));
        assertTrue(criteriaDateToStartDateTimeField.in(queryForNestedTask) instanceof String);
    }

    @Test(expected = TaskPrepareException.class)
    public void MustInCorrectPrepareQueryForSelectingWhenIOCThrowException() throws ResolutionException, TaskPrepareException, RegistrationException {
        IOC.register(Keys.getOrAdd("db.collection.search"), new IResolveDependencyStrategy() {
            @Override
            public IDatabaseTask resolve(Object... args) throws ResolveDependencyStrategyException {
                throw new ResolveDependencyStrategyException("No database task");
            }
        });

        testTask.prepare(mock(IObject.class));
    }

    @Test
    public void MustCorrectExecuteQuery() throws TaskExecutionException, TaskPrepareException, ResolutionException {
        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{ \"keyName\": \"keyName\", \"key\": \"keyValue\" }");
        testTask.prepare(query);

        testTask.execute();

        verify(targetTask).execute();
    }
}