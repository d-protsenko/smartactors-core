package info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Message for checking
 */
public interface CheckValidityMessage {
    /**
     * Returns session from message
     * @return Session
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    Session getSession() throws ReadValueException, ChangeValueException;

    /**
     * The identifier of asynchronous operation which came from the client
     * @return String Id
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    String getAsyncOperationId() throws ReadValueException, ChangeValueException;
}
