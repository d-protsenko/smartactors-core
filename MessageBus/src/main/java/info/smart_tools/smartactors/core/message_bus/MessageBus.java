package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;

/**
 * Realization of MessageBus by ServiceLocator pattern
 */
public final class MessageBus {

    private static IMessageBusContainer container;

    /**
     * Initialize IOC by default implementation of {@link IContainer}
     */
    static {
        container = new MessageBusContainer();
    }

    /**
     * Default private constructor
     */
    private MessageBus() {
    }

    /**
     * Return specific instance of {@link IKey} for container ID
     * @return instance of {@link IKey}
     */
    public IKey getMessageBusKey() {
        return container.getMessageBusKey();
    }

    /**
     * Send message to the receiver
     * @param message the sending message
     * @throws SendingMessageException if sending of message has been failed
     */
    public static void send(final IObject message)
            throws SendingMessageException {
        container.send(message);
    }
}
