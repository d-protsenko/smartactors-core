package info.smart_tools.smartactors.actors.close_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for message
 */
public interface CloseAsyncOpMessage {
    /**
     * Getter for token
     * @return token of async operation
     * @throws ReadValueException
     */
    String getToken() throws ReadValueException;
}
