package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smat_tools.smartactors.core.iexchange.IExchange;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;


/**
 * Endpoint handler for HTTP requests.
 * TODO: deserialize message from different formats (e.g. xml/json/properties etc.) based on content-type.
 */
public class HttpRequestHandler extends EndpointHandler<ChannelHandlerContext, FullHttpRequest> {
    private final IMessageMapper<byte[]> messageMapper;
    private final IDeserializeStrategy deserializeStrategy;

    public HttpRequestHandler(
            IMessageReceiver receiver, IMessageMapper<byte[]> messageMapper, IDeserializeStrategy deserializeStrategy
    ) throws ResolutionException {
        super(receiver);
        this.messageMapper = messageMapper;
        this.deserializeStrategy = deserializeStrategy;
    }

    @Override
    protected IMessage getMessageProcessor(FullHttpRequest request) throws Exception {

        IMessage message = deserializeStrategy.deserialize(request);
        //EndpointFields.HEADERS_FIELD.inject(message, new HttpHeadersObject(request.headers()));

        return message;
    }

    @Override
    protected IExchange getExchange(IMessage message, ChannelHandlerContext ctx, FullHttpRequest request) {
        return null;//new HttpExchange(message, ctx, request, messageMapper);
    }

    @Override
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
