package info.smart_tools.smartactors.core.message_bus_handler;

import info.smart_tools.smartactors.core.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.core.imessage_bus_handler.exception.MessageBusHandlerException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MessageBusHandler}.
 */
public class MessageBusHandlerTest {

    private IStrategyContainer container = new StrategyContainer();
    private IQueue queue = mock(IQueue.class);
    private IReceiverChain chain = mock(IReceiverChain.class);

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    @Test
    public void checkCreation()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain);
        assertNotNull(handler);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullQueue() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(null, 1, this.chain);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnWrongStackDepth() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, -1, this.chain);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullReceiverChain() throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, null);
        fail();
    }

    @Test
    public void checkMessageHandle()
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
        IResolveDependencyStrategy iobjectStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                iobjectStrategy
        );
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(1, this.chain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.queue, sequence)).thenReturn(processor);
        IObject context = mock(IObject.class);
        when(iobjectStrategy.resolve()).thenReturn(context);
        IObject message = mock(IObject.class);

        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain);
        handler.handle(message);
        verify(sequenceStrategy, times(1)).resolve(1, this.chain);
        verify(messageProcessorStrategy, times(1)).resolve(this.queue, sequence);
        verify(iobjectStrategy, times(1)).resolve();
        verify(processor, times(1)).process(message, context);
    }

    @Test (expected = MessageBusHandlerException.class)
    public void checkMessageBusHandlerExceptionOnErrorInHandle()
            throws Exception {
        IMessageBusHandler handler = new MessageBusHandler(this.queue, 1, this.chain);
        handler.handle(null);
        fail();
    }
}
