package info.smart_tools.smartactors.core.endpoint_handler;

import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

import java.util.concurrent.ExecutionException;

/**
 * Base class with a common message handling logic from endpoints.
 * It will initiate message contexts, generate service fields etc...
 *
 * @param <TContext> type of context associated with endpoint data channel.
 *                   It is used to avoid unnecessary new instances creation in endpoint.
 * @param <TRequest> type of a request received by endpoint
 */
public abstract class EndpointHandler<TContext, TRequest> {
    private final IReceiverChain receiverChain;
    private final IEnvironmentHandler environmentHandler;
    private final IScope scope;
    private final String name;

    /**
     * Constructor for HttpRequestHandler
     *
     * @param scope              scope for HttpRequestHandler
     * @param environmentHandler handler for environment
     * @param receiver           chain, that should receive message
     * @param name               name of the endpoint
     */
    public EndpointHandler(final IReceiverChain receiver, final IEnvironmentHandler environmentHandler,
                           final IScope scope, final String name) {
        this.scope = scope;
        this.receiverChain = receiver;
        this.environmentHandler = environmentHandler;
        this.name = name;
    }

    /**
     * Parse a message from the given request.
     * Endpoint can receive message in different formats, so we can't make this process common for now.
     *
     * @param request request to the endpoint
     * @param ctx     context of the request
     * @return a deserialized message
     * @throws Exception if there is exception at environment getting
     */
    protected abstract IObject getEnvironment(TContext ctx, TRequest request) throws Exception;

    /**
     * Send response to client if there are some problems on handling request
     *
     * @param request         request to the endpoint
     * @param ctx             context of the request
     * @param responseIObject iobject, that will send to client
     * @throws Exception if there is exception at environment getting
     */
    protected abstract void sendExceptionalResponse(TContext ctx, TRequest request, IObject responseIObject)
            throws Exception;

    /**
     * Handle an endpoint request using the specified context.
     *
     * @param ctx     endpoint channel context
     * @param request request to the endpoint
     * @throws ExecutionException if request failed to handle
     */
    public void handle(final TContext ctx, final TRequest request) throws ExecutionException {
        try {
            ScopeProvider.setCurrentScope(scope);
            IObject environment = getEnvironment(ctx, request);
            environmentHandler.handle(environment, receiverChain, null);
        } catch (Exception e) {
            try {
                sendExceptionalResponse(ctx, request, IOC.resolve(Keys.getOrAdd("HttpInternalException")));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
