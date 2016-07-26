package info.smart_tools.smartactors.core.https_endpoint;


import info.smart_tools.smartactors.core.EndpointChannelInboundHandler;
import info.smart_tools.smartactors.core.http_request_handler.HttpRequestHandler;
import info.smart_tools.smartactors.core.https_server.HttpsServer;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.ssl_context_provider.SSLContextProvider;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Concrete HTTPS endpoint.
 * It setups the underlying server to handle requests in an endpoint way.
 */
public class HttpsEndpoint extends HttpsServer {
    /**
     * Constructor for endpoint
     *
     * @param port             port of the endpoint
     * @param maxContentLength max length of the content
     * @param scope            scope for endpoint
     * @param handler          handler for environment
     * @param receiverChain    chain, that should receive {@link info.smart_tools.smartactors.core.message_processor.MessageProcessor}
     * @param contextProvider  provider for ssl context
     */
    public HttpsEndpoint(final int port, final int maxContentLength, final IScope scope,
                         final IEnvironmentHandler handler, final IReceiverChain receiverChain,
                         final SSLContextProvider contextProvider
    ) {
        super(port, new EndpointChannelInboundHandler<>(
                        new HttpRequestHandler(scope, handler, receiverChain),
                        FullHttpRequest.class),
                maxContentLength,
                contextProvider);
    }
}
