package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
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
            final IScope scope, final IEnvironmentHandler environmentHandler, final IReceiverChain receiver,
            final IMessageMapper<byte[]> messageMapper, final IDeserializeStrategy deserializeStrategy
    ) throws ResolutionException {
        super(receiver, environmentHandler, scope);
        this.messageMapper = messageMapper;
        this.deserializeStrategy = deserializeStrategy;
    }

    @Override
    protected IObject getEnvironment(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        IObject environment = deserializeStrategy.deserialize(request);
        FieldName context = new FieldName("context");
        environment.setValue(context, ctx);
        return environment;
    }

    @Override
    public void handleException(final ChannelHandlerContext ctx, final Throwable cause) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected IExchange getExchange(final IMessage message,
                                    final ChannelHandlerContext ctx,
                                    final FullHttpRequest fullHttpRequest) throws ResolutionException {
        return new HttpExchange(message, ctx, fullHttpRequest, messageMapper);
    }
}
