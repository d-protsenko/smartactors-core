package info.smart_tools.smartactors.checkpoint.checkpoint_actor;

import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.checkpoint.interfaces.IRecoverStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerActionInitializationException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Tst for {@link CheckpointSchedulerAction}.
 */
public class CheckpointSchedulerActionTest extends PluginsLoadingTestBase {
    private IRecoverStrategy recoverStrategy;
    private ISchedulerEntry entryMock;
    private IObject entryState;
    private String entryId;
    private IMessageBusHandler messageBusHandlerMock;
    private Object chainId = new Object();

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        recoverStrategy = mock(IRecoverStrategy.class);
        when(recoverStrategy.chooseRecoveryChain(any())).thenReturn(chainId);
        IOC.register(Keys.getOrAdd("the recover strategy"), new SingletonStrategy(recoverStrategy));

        entryMock = mock(ISchedulerEntry.class);
        entryState = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        entryId = UUID.randomUUID().toString();
        when(entryMock.getState()).thenReturn(entryState);
        when(entryMock.getId()).thenReturn(entryId);

        messageBusHandlerMock = mock(IMessageBusHandler.class);
        ScopeProvider.getCurrentScope().setValue(MessageBus.getMessageBusKey(), messageBusHandlerMock);
    }

    @Test(expected = SchedulerActionInitializationException.class)
    public void Should_throwWhenInitializationArgumentsDoNotContainMessage()
            throws Exception {
        CheckpointSchedulerAction action = new CheckpointSchedulerAction();

        IObject args = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'recover':{'strategy':'the recover strategy'},'message':null}".replace('\'','"'));

        action.init(entryMock, args);
    }

    @Test
    public void Should_initializeRecoverStrategyAndCopyFieldsToEntryStateOnInitialization()
            throws Exception {
        CheckpointSchedulerAction action = new CheckpointSchedulerAction();

        IObject args = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                ("{'recover':{'strategy':'the recover strategy'}," +
                        "'message':{'a':'1'}," +
                        "'responsibleCheckpointId':'rCP'," +
                        "'prevCheckpointId':'prCP'," +
                        "'prevCheckpointEntryId':'pcpEi'}").replace('\'','"'));

        action.init(entryMock, args);

        verify(recoverStrategy).init(same(entryState), any());
        assertEquals("rCP", entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "responsibleCheckpointId")));
        assertEquals("prCP", entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "prevCheckpointId")));
        assertEquals("pcpEi", entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "prevCheckpointEntryId")));
        assertEquals("the recover strategy", entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "recoverStrategy")));
        assertEquals("1", ((IObject) entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message")))
            .getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "a")));
    }

    @Test
    public void Should_sendCloneOfMessageFillingCheckpointStatusOnExecution()
            throws Exception {
        IObject entryState = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                ("{'recoverStrategy':'the recover strategy'," +
                        "'message':{'a':'1'}," +
                        "'responsibleCheckpointId':'rCP'," +
                        "'prevCheckpointId':'prCP'," +
                        "'prevCheckpointEntryId':'pcpEi'}").replace('\'','"'));

        when(entryMock.getState()).thenReturn(entryState);

        CheckpointSchedulerAction action = new CheckpointSchedulerAction();

        action.execute(entryMock);

        ArgumentCaptor<IObject> mc = ArgumentCaptor.forClass(IObject.class);

        verify(messageBusHandlerMock).handle(mc.capture(), same(chainId));

        IObject sent = mc.getValue();
        IObject sentCPS = (IObject) sent.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "checkpointStatus"));

        assertEquals("1", sent.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "a")));

        assertEquals(entryId, sentCPS.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "checkpointEntryId")));
        assertEquals("rCP", sentCPS.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "responsibleCheckpointId")));
        assertEquals("prCP", sentCPS.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "prevCheckpointId")));
        assertEquals("pcpEi", sentCPS.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "prevCheckpointEntryId")));

        assertNotSame(sent, entryState.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message")));
    }
}
