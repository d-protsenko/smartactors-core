package info.smart_tools.smartactors.on_feature_loading_service_starter.on_feature_loading_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by sevenbits on 11/25/16.
 */
public class OnFeatureLoadingSectionProcessingStrategyTest {

    private IQueue<ITask> taskQueue;
    private IChainStorage chainStorage;
    private IResolveDependencyStrategy sequenceResolveStrategy;
    private IResolveDependencyStrategy chainMapIdResolveStrategy;
    private IMessageProcessingSequence sequence;
    private IMessageProcessor messageProcessor;
    private int stackDepth;
    private IObject context;

    @Before
    public void init()
            throws Exception {
        this.taskQueue = mock(IQueue.class);
        this.chainStorage = mock(IChainStorage.class);
        this.context = mock(IObject.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );

        IKey iFieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());

        IOC.register(iFieldNameKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
        );

        IKey taskQueueKey = Keys.getOrAdd("task_queue");
        IOC.register(taskQueueKey,
                new SingletonStrategy(this.taskQueue));

        IKey chainStorageKey = Keys.getOrAdd(IChainStorage.class.getCanonicalName());
        IOC.register(chainStorageKey,
                new SingletonStrategy(this.chainStorage));

        IKey messageProcessorKey = Keys.getOrAdd(IMessageProcessor.class.getCanonicalName());
        this.messageProcessor = mock(IMessageProcessor.class);
        IOC.register(messageProcessorKey, new SingletonStrategy(this.messageProcessor));

        IKey stackDepthKey = Keys.getOrAdd("default_stack_depth");
        this.stackDepth = 5;
        IOC.register(stackDepthKey, new SingletonStrategy(this.stackDepth));

        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new SingletonStrategy(this.context)
        );
    }

    @Test
    public void checkGetSectionNameMethod()
            throws Exception {
        ISectionStrategy strategy = new OnFeatureLoadingSectionProcessingStrategy();
        assertEquals(strategy.getSectionName(), new FieldName("onFeatureLoading"));
    }

    @Test
    public void checkOnLoadConfigMethod()
            throws Exception {
        ISectionStrategy strategy = new OnFeatureLoadingSectionProcessingStrategy();

        IObject config = new DSObject("{\"onFeatureLoading\": [\n" +
                "    {\n" +
                "      \"chain\": \"chain1\",\n" +
                "      \"messages\": [\n" +
                "        {\n" +
                "          \"key\": \"value1\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"chain\": \"chain2\",\n" +
                "      \"messages\": [\n" +
                "        {\n" +
                "          \"key\": \"value2\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"key\": \"value3\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]}");
        List<IObject> onFeatureLoadingConfigSection = (List<IObject>) config.getValue(new FieldName("onFeatureLoading"));
        IObject message11 = ((List<IObject>)onFeatureLoadingConfigSection.get(0).getValue(new FieldName("messages"))).get(0);
        IObject message21 = ((List<IObject>)onFeatureLoadingConfigSection.get(1).getValue(new FieldName("messages"))).get(0);
        IObject message22 = ((List<IObject>)onFeatureLoadingConfigSection.get(1).getValue(new FieldName("messages"))).get(1);

        this.chainMapIdResolveStrategy = mock(IResolveDependencyStrategy.class);
        Object mapId1 = mock(Object.class);
        Object mapId2 = mock(Object.class);
        IReceiverChain chain1 = mock(IReceiverChain.class);
        IReceiverChain chain2 = mock(IReceiverChain.class);
        when(this.chainMapIdResolveStrategy.resolve("chain1")).thenReturn(mapId1);
        when(this.chainMapIdResolveStrategy.resolve("chain2")).thenReturn(mapId2);

        IKey chainIdFromMapNameKey = Keys.getOrAdd("chain_id_from_map_name");
        IOC.register(chainIdFromMapNameKey,
                this.chainMapIdResolveStrategy);

        when(this.chainStorage.resolve(mapId1)).thenReturn(chain1);
        when(this.chainStorage.resolve(mapId2)).thenReturn(chain2);

        this.sequenceResolveStrategy = mock(IResolveDependencyStrategy.class);
        IKey sequenceKey = Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName());
        this.sequence = mock(IMessageProcessingSequence.class);
        IOC.register(sequenceKey, this.sequenceResolveStrategy);
        when(this.sequenceResolveStrategy.resolve(this.stackDepth, chain1)).thenReturn(sequence);
        when(this.sequenceResolveStrategy.resolve(this.stackDepth, chain2)).thenReturn(sequence);

        strategy.onLoadConfig(config);

        verify(this.chainMapIdResolveStrategy, times(1)).resolve("chain1");
        verify(this.chainMapIdResolveStrategy, times(1)).resolve("chain2");

        verify(this.chainStorage, times(1)).resolve(mapId1);
        verify(this.chainStorage, times(1)).resolve(mapId2);

        verify(this.sequenceResolveStrategy, times(1)).resolve(this.stackDepth, chain1);
        verify(this.sequenceResolveStrategy, times(2)).resolve(this.stackDepth, chain2);

        verify(this.messageProcessor, times(1)).process(message11, this.context);
        verify(this.messageProcessor, times(1)).process(message21, this.context);
        verify(this.messageProcessor, times(1)).process(message22, this.context);
    }

    @Test (expected = ConfigurationProcessingException.class)
    public void checkConfigurationProcessingExceptionOnWrongConfig()
            throws Exception {
        ISectionStrategy strategy = new OnFeatureLoadingSectionProcessingStrategy();
        IObject config = new DSObject("{\"onFeatureLoading\": [\n" +
                "    {\n" +
                "      \"chain\": \"unknownChain\",\n" +
                "      \"messages\": [\n" +
                "        {\n" +
                "          \"key\": \"value\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }]}");
        strategy.onLoadConfig(config);
        fail();
    }

}
