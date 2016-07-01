package info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

/**
 * Message for checking
 */
public interface CheckValidityMessage {

    /**
     * The identifier of asynchronous operation which came from the client
     * @return String Id
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    String getAsyncOperationId() throws ReadValueException, ChangeValueException;

    /**
     * Returns the list with all identifiers of asynchronous operations which are admissible for this session
     * @return List with identifiers
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    List<String> getIdentifiers() throws ChangeValueException, ReadValueException;
}
