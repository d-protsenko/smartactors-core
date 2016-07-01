package info.smart_tools.smartactors.actors.close_async_operation;

import info.smart_tools.smartactors.actors.close_async_operation.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.close_async_operation.wrapper.CloseAsyncOpMessage;
import info.smart_tools.smartactors.core.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Actor that close async operation
 */
public class CloseAsyncOperationActor {
    private IAsyncOperationCollection collection;

    /**
     * Constructor
     * @param params the params for constructor
     * @throws InvalidArgumentException
     */
    CloseAsyncOperationActor(final ActorParams params) throws InvalidArgumentException {
        try {
            collection = IOC.resolve(Keys.getOrAdd(IAsyncOperationCollection.class.toString()), params.getCollectionName());
        } catch (Exception e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Close and delete async operation from collection
     * @param message the message
     * @throws TaskExecutionException
     */
    void closeAsyncOp(final CloseAsyncOpMessage message) throws TaskExecutionException {
        try {
            //
            message.getOperationTokens().remove(message.getToken());
            collection.complete(collection.getAsyncOperation(message.getToken()));
        } catch (Exception e) {
            throw new TaskExecutionException("Failed to close async operation", e);
        }
    }
}
