package info.smart_tools.smartactors.core.imessage_bus_container;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.iobject.IObject;

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
     * Send message to the chain call receiver
     * @param message the message for send
     * @throws SendingMessageException if message sending has been failed
     */
    void send(IObject message) throws SendingMessageException;

    /**
     * Send message to the specific chain
     * @param message the message for send
     * @param chainName the name of specific chain
     * @throws SendingMessageException if message sending has been failed
     */
    void send(IObject message, Object chainName) throws SendingMessageException;
}
