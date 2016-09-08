package info.smart_tools.smartactors.core.message_bus_container_with_scope;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

/**
 * Implementation of {@link IMessageBusContainer}.
 */
public class MessageBusContainer implements IMessageBusContainer {

    /** Key for getting instance of {@link IMessageBusContainer} from current scope */
    private IKey messageBusContainerKey;

    /**
     * Default constructor
     */
    public MessageBusContainer() {
        try {
            this.messageBusContainerKey = new Key(java.util.UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new RuntimeException("Initialization of Message bus container has been failed.");
        }
    }

    @Override
    public IKey getMessageBusKey() {
        return this.messageBusContainerKey;
    }

    @Override
    public void send(final IObject message) throws SendingMessageException {
        try {
            IMessageBusHandler handler = (IMessageBusHandler) ScopeProvider
                    .getCurrentScope()
                    .getValue(messageBusContainerKey);
            handler.handle(message);
        } catch (Throwable e) {
            throw new SendingMessageException("Could not send message.");
        }
    }

    @Override
    public void send(final IObject message, final Object chainName)
            throws SendingMessageException {
        try {
            IMessageBusHandler handler = (IMessageBusHandler) ScopeProvider
                    .getCurrentScope()
                    .getValue(messageBusContainerKey);
            handler.handle(message, chainName);
        } catch (Throwable e) {
            throw new SendingMessageException("Could not send message.");
        }
    }
}
