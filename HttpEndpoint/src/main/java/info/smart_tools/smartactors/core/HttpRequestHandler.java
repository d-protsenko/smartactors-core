package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
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
    protected IObject getEnvironment(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        IObject environment = deserializeStrategy.deserialize(request);

        FieldName context = new FieldName("context");
        environment.setValue(context, ctx);
        //EndpointFields.HEADERS_FIELD.inject(message, new HttpHeadersObject(request.headers()));

        return environment;
    }

    @Override
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected IExchange getExchange(IMessage message, ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws ResolutionException {
        return new HttpExchange(message, ctx, fullHttpRequest, messageMapper);
    }
}
