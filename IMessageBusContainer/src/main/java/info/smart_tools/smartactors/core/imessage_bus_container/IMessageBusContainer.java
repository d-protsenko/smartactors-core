package info.smart_tools.smartactors.core.imessage_bus_container;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface for MessageBus
 */
public interface IMessageBusContainer {

    /**
     * Return specific instance of {@link IKey} for container ID
     * @return instance of {@link IKey}
     */
    IKey getMessageBusKey();

    /**
     * Send message to the receiver
     * @param message the message for send
     * @throws SendingMessageException if sending message has been failed
     */
    void send(IObject message) throws SendingMessageException;
}
