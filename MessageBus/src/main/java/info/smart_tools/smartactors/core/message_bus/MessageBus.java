package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;

/**
 * Realization of MessageBus by ServiceLocator pattern
 */
public final class MessageBus {

    private static IMessageBusContainer container;

    /**
     * Initialize MessageBus by default implementation of {@link IMessageBusContainer}
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
    public static IKey getMessageBusKey() {
        return container.getMessageBusKey();
    }

    /**
     * Send message to the chain call receiver
     * @param message the sending message
     * @throws SendingMessageException if sending of message has been failed
     */
    public static void send(final IObject message)
            throws SendingMessageException {
        container.send(message);
    }

    /**
     * Send message to the specific chain
     * @param message the message for send
     * @param chainName the name of specific chain
     * @throws SendingMessageException if message sending has been failed
     */
    public static void send(final IObject message, final Object chainName)
            throws SendingMessageException {
        container.send(message, chainName);
    }

    /**
     * Send message to the chain call receiver  and send processed message to the specific chain
     * @param message the message for send
     * @param replyToChainName the name of specific chain to reply to
     * @throws SendingMessageException if message sending has been failed
     */
    public static void sendAndReply(final IObject message, final Object replyToChainName)
            throws SendingMessageException {
        container.sendAndReply(message, replyToChainName);
    }

    /**
     * Send message to the specific chain and reply processed message to other chain
     * @param message the message for send
     * @param chainName the name of specific chain to send to
     * @param replyToChainName the name of specific chain to reply to
     * @throws SendingMessageException if message sending has been failed
     */
    public static void sendAndReply(final IObject message, final Object chainName, final Object replyToChainName)
            throws SendingMessageException {
        container.sendAndReply(message, chainName, replyToChainName);
    }
}
