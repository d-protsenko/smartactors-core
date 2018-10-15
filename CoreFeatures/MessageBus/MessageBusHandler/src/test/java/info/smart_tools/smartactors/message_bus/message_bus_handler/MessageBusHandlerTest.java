package info.smart_tools.smartactors.message_bus.message_bus_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.exception.MessageBusHandlerException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MessageBusHandler}.
 */
public class MessageBusHandlerTest {

    private IStrategyContainer container = new StrategyContainer();
    private IQueue queue = mock(IQueue.class);
    private IReceiverChain chain = mock(IReceiverChain.class);
    private IResponseStrategy nullResponseStrategy;
    private IResponseStrategy mbResponseStrategy;

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyByNameResolveStrategy(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException("exception", e);
                            }
                        }
                )
        );

        nullResponseStrategy = mock(IResponseStrategy.class);
        mbResponseStrategy = mock(IResponseStrategy.class);

        IOC.register(Keys.getOrAdd("null response strategy"), new SingletonStrategy(nullResponseStrategy));
        IOC.register(Keys.getOrAdd("message bus response strategy"), new SingletonStrategy(mbResponseStrategy));
    }

    @Test
    public void checkCreation()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        assertNotNull(handler);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullQueue() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(null, 1, this.chain, mock(IAction.class));
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnWrongStackDepth() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, -1, this.chain, mock(IAction.class));
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullReceiverChain() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, null, mock(IAction.class));
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullFinalAction() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, null);
        fail();
    }

    @Test
    public void checkMessageHandle()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                messageProcessorStrategy
        );
        IResolveDependencyStrategy iobjectStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.iobject.IObject"),
                iobjectStrategy
        );
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(1, this.chain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.queue, sequence)).thenReturn(processor);
        IObject context = mock(IObject.class);
        when(iobjectStrategy.resolve()).thenReturn(context);
        IObject message = mock(IObject.class);

        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handle(message);
        verify(context, times(1)).setValue(eq(new FieldName("responseStrategy")), same(nullResponseStrategy));
        verify(sequenceStrategy, times(1)).resolve(1, this.chain);
        verify(messageProcessorStrategy, times(1)).resolve(this.queue, sequence);
        verify(iobjectStrategy, times(1)).resolve();
        verify(processor, times(1)).process(message, context);
    }

    @Test (expected = MessageBusHandlerException.class)
    public void checkMessageBusHandlerExceptionOnErrorInHandle()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handle(null);
        fail();
    }

    @Test
    public void checkMessageHandleWithSpecificChain()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                messageProcessorStrategy
        );
        IResolveDependencyStrategy iobjectStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.iobject.IObject"),
                iobjectStrategy
        );
        IResolveDependencyStrategy chainIdStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "chain_id_from_map_name"),
                chainIdStrategy
        );
        IResolveDependencyStrategy chainStorageStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), IChainStorage.class.getCanonicalName()),
                chainStorageStrategy
        );
        Object chainId = mock(Object.class);
        Object chainName = mock(Object.class);
        IChainStorage storage = mock(IChainStorage.class);
        IReceiverChain chain = mock(IReceiverChain.class);
        when(chainIdStrategy.resolve(chainName)).thenReturn(chainId);
        when(chainStorageStrategy.resolve()).thenReturn(storage);
        when(storage.resolve(chainId)).thenReturn(chain);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(1, chain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.queue, sequence)).thenReturn(processor);
        IObject context = mock(IObject.class);
        when(iobjectStrategy.resolve()).thenReturn(context);
        IObject message = mock(IObject.class);

        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handle(message, chainName);
        verify(sequenceStrategy, times(1)).resolve(1, chain);
        verify(messageProcessorStrategy, times(1)).resolve(this.queue, sequence);
        verify(iobjectStrategy, times(1)).resolve();
        verify(processor, times(1)).process(message, context);
        verify(chainIdStrategy, times(1)).resolve(chainName);
        verify(chainStorageStrategy, times(1)).resolve();
        verify(storage, times(1)).resolve(chainId);
    }

    @Test (expected = MessageBusHandlerException.class)
    public void checkMessageBusHandlerExceptionOnErrorInHandleWithSpecificChain()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handle(null, null);
        fail();
    }

    @Test
    public void checkMessageHandleWithReply()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                messageProcessorStrategy
        );
        IResolveDependencyStrategy iobjectStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.iobject.IObject"),
                iobjectStrategy
        );
        IResolveDependencyStrategy chainIdStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "chain_id_from_map_name"),
                chainIdStrategy
        );
        IResolveDependencyStrategy chainStorageStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), IChainStorage.class.getCanonicalName()),
                chainStorageStrategy
        );
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(1, this.chain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.queue, sequence)).thenReturn(processor);
        IObject context = mock(IObject.class);
        when(iobjectStrategy.resolve()).thenReturn(context);
        IObject message = mock(IObject.class);
        IAction finalAction = mock(IAction.class);
        Object replyToChainName = mock(Object.class);

        Object replyToChainId = mock(Object.class);
        IChainStorage storage = mock(IChainStorage.class);
        IReceiverChain replyToChain = mock(IReceiverChain.class);
        when(chainIdStrategy.resolve(replyToChainName)).thenReturn(replyToChainId);
        when(chainStorageStrategy.resolve()).thenReturn(storage);
        when(storage.resolve(replyToChainId)).thenReturn(replyToChain);

        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, finalAction);
        handler.handleForReply(message, replyToChainName);
        verify(sequenceStrategy, times(1)).resolve(1, this.chain);
        verify(messageProcessorStrategy, times(1)).resolve(this.queue, sequence);
        verify(iobjectStrategy, times(1)).resolve();
        verify(processor, times(1)).process(message, context);
        verify(context, times(1)).setValue(eq(new FieldName("finalActions")), any(List.class));
        verify(context, times(1)).setValue(new FieldName("messageBusReplyTo"), replyToChainName);
        verify(context, times(1)).setValue(eq(new FieldName("responseStrategy")), same(mbResponseStrategy));
    }

    @Test (expected = MessageBusHandlerException.class)
    public void checkMessageBusHandlerExceptionOnErrorInHandleForReply()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handleForReply(null, null);
        fail();
    }

    @Test
    public void checkMessageHandleWithSpecificChainAndReply()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                messageProcessorStrategy
        );
        IResolveDependencyStrategy iobjectStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.iobject.IObject"),
                iobjectStrategy
        );
        IResolveDependencyStrategy chainIdStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "chain_id_from_map_name"),
                chainIdStrategy
        );
        IResolveDependencyStrategy chainStorageStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), IChainStorage.class.getCanonicalName()),
                chainStorageStrategy
        );
        IReceiverChain chain = mock(IReceiverChain.class);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(1, chain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.queue, sequence)).thenReturn(processor);
        IObject context = mock(IObject.class);
        when(iobjectStrategy.resolve()).thenReturn(context);
        IObject message = mock(IObject.class);
        IAction finalAction = mock(IAction.class);
        Object replyToChainName = mock(Object.class);
        Object chainName = mock(Object.class);

        Object replyToChainId = mock(Object.class);
        Object chainId = mock(Object.class);
        IChainStorage storage = mock(IChainStorage.class);
        IReceiverChain replyToChain = mock(IReceiverChain.class);

        when(chainIdStrategy.resolve(replyToChainName)).thenReturn(replyToChainId);
        when(chainIdStrategy.resolve(chainName)).thenReturn(chainId);
        when(chainStorageStrategy.resolve()).thenReturn(storage);
        when(storage.resolve(replyToChainId)).thenReturn(replyToChain);
        when(storage.resolve(chainId)).thenReturn(chain);

        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, finalAction);
        handler.handleForReply(message, chainName, replyToChainName);
        verify(sequenceStrategy, times(1)).resolve(1, chain);
        verify(messageProcessorStrategy, times(1)).resolve(this.queue, sequence);
        verify(iobjectStrategy, times(1)).resolve();
        verify(processor, times(1)).process(message, context);
        verify(context, times(1)).setValue(eq(new FieldName("finalActions")), any(List.class));
        verify(context, times(1)).setValue(new FieldName("messageBusReplyTo"), replyToChainName);
    }

    @Test (expected = MessageBusHandlerException.class)
    public void checkMessageBusHandlerExceptionOnErrorInHandleForReplyWithSpecificChain()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain, mock(IAction.class));
        handler.handleForReply(null, null, null);
        fail();
    }
}
