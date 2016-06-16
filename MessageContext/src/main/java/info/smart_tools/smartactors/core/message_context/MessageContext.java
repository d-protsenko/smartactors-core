package info.smart_tools.smartactors.core.message_context;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_context.exceptions.MessageContextAccessException;

/**
 * Service locator that provides access to current message context.
 */
public final class MessageContext {
    /**
     *
     * Initialization possible only with using java reflection API
     * Example:
     * <pre>
     * {@code
     * Field field = MessageContext.class.getDeclaredField("container");
     * field.setAccessible(true);
     * field.set(null, new Object());
     * field.setAccessible(false);
     * }
     * </pre>
     */
    private static IMessageContextContainer container;

    private MessageContext() {
    }

    /**
     * Get current message context.
     *
     * @return current message context
     * @throws MessageContextAccessException if any error occurs
     */
    public static IObject get()
            throws MessageContextAccessException {
        return container.getCurrentContext();
    }

    /**
     * Set new current context.
     *
     * @param context new message context
     * @throws MessageContextAccessException if any error occurs
     */
    public static void set(final IObject context)
            throws MessageContextAccessException {
        container.setCurrentContext(context);
    }
}
