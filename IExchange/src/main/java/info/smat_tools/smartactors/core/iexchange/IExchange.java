package info.smat_tools.smartactors.core.iexchange;

import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.concurrent.CompletableFuture;

/**
 * An interface for Message Exchange Pattern (MEP).
 * Common implementation will be able to send the given message asynchronously away from the given node.
 * It can be used for sending a response to request or for sending a message as a client.
 * Example of MEPs are: request-response / in-only / publish-subscribe etc.
 * TODO: should it accept a generic parameter for Future return value?
 * TODO: should it accept a generic parameter instead of {@link IMessage} so we can use it for all kind of clients?
 */
public interface IExchange {
    /**
     * Initiate a asynchronous transfer of the given message.
     *
     * @param message message to be sent
     * @return a future value which can be used to wait an operation completion.
     */
    CompletableFuture<Void> write(IObject message);
}
