package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.http_server.HttpServer;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpEndpoint extends HttpServer {
    public HttpEndpoint(final int port, final IScope scope, final IEnvironmentHandler handler,
                        final IReceiverChain receiverChain, final IMessageMapper<byte[]> messageMapper,
                        final IDeserializeStrategy strategy) throws ResolutionException {
        super(port, new EndpointChannelInboundHandler<>(
                new HttpRequestHandler(scope, handler, receiverChain, messageMapper, strategy),
                FullHttpRequest.class
        ));
    }
}
