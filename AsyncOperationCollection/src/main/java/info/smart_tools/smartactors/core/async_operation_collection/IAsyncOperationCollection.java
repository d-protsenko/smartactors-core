package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Interface for asynchronous operation
 */
public interface IAsyncOperationCollection {

    /**
     * Get asynchronous operation
     * @param token operation unique token
     * @return operation object
     * @throws GetAsyncOperationException if error during read is occurred
     */
    IObject getAsyncOperation(final String token) throws GetAsyncOperationException;
}
