package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.HttpEndpoint;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_bus.MessageBus;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageBusSectionProcessingStrategyTest {

    private IQueue<ITask> taskQueue;
    private Object mapId;
    private IChainStorage chainStorage;
    private IReceiverChain receiverChain;

    @Before
    public void setUp()
            throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        taskQueue = mock(IQueue.class);
        mapId = mock(Object.class);
        receiverChain = mock(IReceiverChain.class);
        chainStorage = mock(IChainStorage.class);
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

        IKey chainIdFromMapNameKey = Keys.getOrAdd("chain_id_from_map_name");
        IOC.register(chainIdFromMapNameKey,
                new SingletonStrategy(mapId));

        IKey taskQueueKey = Keys.getOrAdd("task_queue");
        IOC.register(taskQueueKey,
                new SingletonStrategy(taskQueue));

        IKey chainStorageKey = Keys.getOrAdd(IChainStorage.class.getCanonicalName());
        IOC.register(chainStorageKey,
                new SingletonStrategy(chainStorage));


    }

    @Test
    public void testLoadingConfig()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessingSequence.class.getCanonicalName()),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessor.class.getCanonicalName()),
                messageProcessorStrategy
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            return new DSObject();
                        }
                )
        );

        when(chainStorage.resolve(mapId)).thenReturn(receiverChain);
        DSObject config = new DSObject("\n" +
                "     {\n" +
                "         \"messageBus\": \n" +
                "             {\n" +
                "                 \"routingChain\": \"mainChain\",\n" +
                "                 \"stackDepth\": 5\n" +
                "             }\n" +
                "         \n" +
                "     }");
        MessageBusSectionProcessingStrategy strategy = new MessageBusSectionProcessingStrategy();
        strategy.onLoadConfig(config);

        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(5, this.receiverChain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.taskQueue, sequence)).thenReturn(processor);
        IObject result = new DSObject();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                result.setValue(new FieldName("message"), invocationOnMock.getArguments()[0]);
                result.setValue(new FieldName("context"), invocationOnMock.getArguments()[1]);
                return null;
            }
        }).when(processor).process(any(IObject.class), any(IObject.class));

        IMessageBusHandler handler = (IMessageBusHandler) ScopeProvider.getCurrentScope().getValue(MessageBus.getMessageBusKey());
        assertNotNull(handler);
        Object replyToChainName = mock(Object.class);
        IObject message = mock(IObject.class);
        handler.handleForReply(message, replyToChainName);
        assertSame(result.getValue(new FieldName("message")), message);
        IObject resultContext = (IObject) result.getValue(new FieldName("context"));
        List<IAction> actions = (List<IAction>) resultContext.getValue(new FieldName("finalActions"));
        assertEquals(actions.size(), 1);
        IAction<IObject> action = actions.get(0);
        IObject env = new DSObject();
        IObject messageForReply = new DSObject();
        env.setValue(new FieldName("message"), messageForReply);
        env.setValue(new FieldName("context"), resultContext);
        action.execute(env);
        verify(processor, times(1)).process(eq(messageForReply), any(IObject.class));
    }
}
