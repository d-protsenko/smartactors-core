package info.smart_tools.smartactors.async_operations.close_async_operation.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Wrapper for CloseAsyncOperationActor parameters
 */
public interface ActorParams {
    /**
     *
     * @return the name of collection where async op's are stored
     * @throws ReadValueException Throw when can't correct read value
     */
    String getCollectionName() throws ReadValueException;
}
