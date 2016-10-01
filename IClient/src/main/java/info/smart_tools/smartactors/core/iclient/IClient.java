package info.smart_tools.smartactors.core.iclient;

import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import java.util.concurrent.CompletableFuture;

/**
 * General interface for clients.
 * @param <Request>
 */
public interface IClient<Request> extends IAsyncService<IClient<Request>> {

    /**
     * Method to send request
     * @param request request message
     * @return completable future of the sending request
     */
    CompletableFuture<Void> send(Request request);
}
