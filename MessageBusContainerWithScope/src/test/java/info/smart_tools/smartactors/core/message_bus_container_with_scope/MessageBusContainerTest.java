package info.smart_tools.smartactors.core.message_bus_container_with_scope;

import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link MessageBusContainer}
 */
public class MessageBusContainerTest {

    private IStrategyContainer container = new StrategyContainer();

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
    public void checkCreation() {
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        assertNotNull(messageBusContainer);
        assertNotNull(messageBusContainer.getMessageBusKey());
    }

    @Test
    public void checkSendingMessage()
            throws Exception {
        IMessageBusHandler handler = mock(IMessageBusHandler.class);
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        ScopeProvider.getCurrentScope().setValue(messageBusContainer.getMessageBusKey(), handler);
        IObject message = mock(IObject.class);
        doNothing().when(handler).handle(message);

        messageBusContainer.send(message);
        verify(handler, times(1)).handle(message);
    }

    @Test (expected = SendingMessageException.class)
    public void checkSendingMessageException()
            throws Exception {
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        messageBusContainer.send(null);
        fail();
    }

    @Test
    public void checkSendingMessageWithSpecificChain()
            throws Exception {
        IMessageBusHandler handler = mock(IMessageBusHandler.class);
        Object chainName = mock(Object.class);
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        ScopeProvider.getCurrentScope().setValue(messageBusContainer.getMessageBusKey(), handler);
        IObject message = mock(IObject.class);
        doNothing().when(handler).handle(message, chainName);

        messageBusContainer.send(message, chainName);
        verify(handler, times(1)).handle(message, chainName);
    }

    @Test (expected = SendingMessageException.class)
    public void checkSendingMessageExceptionOnSendWithSpecificChain()
            throws Exception {
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        messageBusContainer.send(null, null);
        fail();
    }

    @Test
    public void checkSendingMessageWithReply()
            throws Exception {
        IMessageBusHandler handler = mock(IMessageBusHandler.class);
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        ScopeProvider.getCurrentScope().setValue(messageBusContainer.getMessageBusKey(), handler);
        IObject message = mock(IObject.class);
        Object chainNameForReply = mock(Object.class);
        doNothing().when(handler).handleForReply(message, chainNameForReply);

        messageBusContainer.sendAndReply(message, chainNameForReply);
        verify(handler, times(1)).handleForReply(message, chainNameForReply);
    }

    @Test (expected = SendingMessageException.class)
    public void checkSendingMessageExceptionOnSendingWithReply()
            throws Exception {
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        messageBusContainer.sendAndReply(null, null);
        fail();
    }

    @Test
    public void checkSendingMessageWithSpecificChainAndReply()
            throws Exception {
        IMessageBusHandler handler = mock(IMessageBusHandler.class);
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        ScopeProvider.getCurrentScope().setValue(messageBusContainer.getMessageBusKey(), handler);
        IObject message = mock(IObject.class);
        Object chainName = mock(Object.class);
        Object chainNameForReply = mock(Object.class);
        doNothing().when(handler).handleForReply(message, chainName, chainNameForReply);

        messageBusContainer.sendAndReply(message, chainName, chainNameForReply);
        verify(handler, times(1)).handleForReply(message, chainName, chainNameForReply);
    }

    @Test (expected = SendingMessageException.class)
    public void checkSendingMessageExceptionOnSendingWithSpecificChainAndReply()
            throws Exception {
        IMessageBusContainer messageBusContainer = new MessageBusContainer();
        messageBusContainer.sendAndReply(null, null, null);
        fail();
    }
}
