package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.http_server.HttpServer;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpEndpoint extends HttpServer {
    public HttpEndpoint(int port, IMessageReceiver receiver, IMessageMapper<byte[]> messageMapper, IDeserializeStrategy strategy) throws ResolutionException {
        super(port, new EndpointChannelInboundHandler<>(
                new HttpRequestHandler(receiver, messageMapper, strategy),
                FullHttpRequest.class
        ));
    }
}
