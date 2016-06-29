package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Interface for AsyncOperationCollection
 */
public interface IAsyncOperationCollection {

    /**
     * Get asynchronous operation
     * @param token operation unique token
     * @return operation object
     * @throws GetAsyncOperationException 123
     */
    IObject getAsyncOperation(final String token) throws GetAsyncOperationException;

    /**
     * Create async operation in db
     * @param data the async operation data
     * @param token guid token for operation
     * @throws CreateAsyncOperationException 123
     */
    void createAsyncOperation(final IObject data, final String token) throws CreateAsyncOperationException;
}
