package info.smart_tools.smartactors.core.message_bus_container_with_scope;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.iobject.IObject;
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
    public void send(IObject message) throws SendingMessageException {

    }
}
