package info.smart_tools.smartactors.core.message_context;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_context.exceptions.MessageContextAccessException;

/**
 * Interface for object providing operations of access to current message context.
 */
public interface IMessageContextContainer {
    /**
     * Get current message context.
     *
     * @return current message context
     * @throws MessageContextAccessException if any error occurs
     */
    IObject getCurrentContext() throws MessageContextAccessException;

    /**
     * Set new current context.
     *
     * @param context    new message context
     * @throws MessageContextAccessException if any error occurs
     */
    void setCurrentContext(IObject context) throws MessageContextAccessException;
}
