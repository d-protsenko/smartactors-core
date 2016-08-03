package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

/**
 * Realization of MessageBus by ServiceLocator pattern
 */
public final class MessageBus {

    private static IMessageBusContainer container;
    /**
     * Default private constructor
     */

    /**
     * Initialize IOC by default implementation of {@link IContainer}
     */

    static {
        container = new MessageBusContainer();
    }

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
     *
     * @param message
     */
    public static void send(final IObject message) {

    }
}
