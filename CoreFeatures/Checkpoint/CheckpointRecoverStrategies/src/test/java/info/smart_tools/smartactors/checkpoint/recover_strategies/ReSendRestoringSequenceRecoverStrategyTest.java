package info.smart_tools.smartactors.checkpoint.recover_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ReSendRestoringSequenceRecoverStrategy}.
 */
public class ReSendRestoringSequenceRecoverStrategyTest extends PluginsLoadingTestBase {

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Test
    public void Should_makeDumpOfMessageProcessingSequenceOnInitialization()
            throws Exception {
        IObject stateMock = mock(IObject.class);
        IObject argsMock = mock(IObject.class);
        IObject dumpMock = mock(IObject.class);
        IMessageProcessor processorMock = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequenceMock = mock(IMessageProcessingSequence.class);
        IResolveDependencyStrategy makeDumpStrategy = mock(IResolveDependencyStrategy.class);

        IOC.register(Keys.resolveByName("make dump"), makeDumpStrategy);

        when(makeDumpStrategy.resolve(same(sequenceMock), same(argsMock))).thenReturn(dumpMock);

        when(processorMock.getSequence()).thenReturn(sequenceMock);

        new ReSendRestoringSequenceRecoverStrategy().init(stateMock, argsMock, processorMock);

        verify(stateMock).setValue(eq(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "sequenceDump")), same(dumpMock));
    }

    @Test
    public void Should_recoverMessageProcessingSequence()
            throws Exception {
        IObject state = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'responsibleCheckpointId':'rcid'," +
                        "'entryId':'eid'," +
                        "'prevCheckpointEntryId':'pceid'," +
                        "'prevCheckpointId':'pcid'," +
                        "'sequenceDump':'This is a sequence dump. Trust me I am IObject.'," +
                        "'message':{'is-a-message':true}" +
                        "}").replace('\'','"'));

        IResolveDependencyStrategy recoverSequenceStrategy = mock(IResolveDependencyStrategy.class);
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IMessageProcessingSequence sequenceMock = mock(IMessageProcessingSequence.class);
        IMessageProcessor processorMock = mock(IMessageProcessor.class);
        Object taskQueue = new Object();

        IOC.register(Keys.resolveByName("recover message processing sequence"), recoverSequenceStrategy);
        IOC.register(Keys.resolveByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"), messageProcessorStrategy);
        IOC.register(Keys.resolveByName("task_queue"), new SingletonStrategy(taskQueue));

        when(recoverSequenceStrategy.resolve(eq("This is a sequence dump. Trust me I am IObject."))).thenReturn(sequenceMock);
        when(messageProcessorStrategy.resolve(same(taskQueue), same(sequenceMock))).thenReturn(processorMock);

        new ReSendRestoringSequenceRecoverStrategy().reSend(state);

        ArgumentCaptor<IObject> mc = ArgumentCaptor.forClass(IObject.class);

        verify(processorMock).process(mc.capture(), any());

        assertNotNull(mc.getValue());
        assertEquals(true, mc.getValue().getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "is-a-message")));

        IObject checkpointStatus =
                (IObject) mc.getValue().getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpointStatus"));

        assertNotNull(checkpointStatus);
        assertEquals("rcid",
                checkpointStatus.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responsibleCheckpointId")));
        assertEquals("eid",
                checkpointStatus.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpointEntryId")));
        assertEquals("pceid",
                checkpointStatus.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "prevCheckpointEntryId")));
        assertEquals("pcid",
                checkpointStatus.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "prevCheckpointId")));
    }
}
