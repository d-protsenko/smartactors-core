package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.endpoint.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

import java.util.concurrent.ExecutionException;

/**
 *
 */
public class WSEndpointHandler extends EndpointHandler<ChannelHandlerContext, WebSocketFrame> {
    private final IFieldName contextFN;
    private final IFieldName messageFN;
    private final IFieldName wsConnectionIdFN;
    private final IFieldName binaryDataFN;

    private static final AttributeKey<Object> CONNECTION_ID_ATTR_KEY = AttributeKey.valueOf("connectionId");

    /**
     * Constructor for HttpRequestHandler
     *
     * @param receiver           chain, that should receive message
     * @param environmentHandler handler for environment
     * @param scope              scope for HttpRequestHandler
     * @param name               name of the endpoint
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public WSEndpointHandler(final IReceiverChain receiver, final IEnvironmentHandler environmentHandler, final IScope scope, final String name)
            throws ResolutionException {
        super(receiver, environmentHandler, scope, name);

        contextFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
        messageFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
        wsConnectionIdFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "wsConnectionId");
        binaryDataFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "binaryData");
    }

    @Override
    protected IObject getEnvironment(final ChannelHandlerContext ctx, final WebSocketFrame webSocketFrame)
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

        IObject context = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));
        context.setValue(wsConnectionIdFN, ctx.channel().attr(CONNECTION_ID_ATTR_KEY).get());
        env.setValue(contextFN, context);

        IObject message;

        if (webSocketFrame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) webSocketFrame;
            message = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"), textWebSocketFrame.text());
        } else {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) webSocketFrame;
            ByteBuf byteBuf = binaryWebSocketFrame.content();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            message = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));
            message.setValue(binaryDataFN, bytes);
        }

        env.setValue(messageFN, message);

        return env;
    }

    @Override
    protected void sendExceptionalResponse(final ChannelHandlerContext ctx, final WebSocketFrame webSocketFrame, final IObject responseIObject)
            throws Exception {
        // TODO
        ctx.close();
    }

    @Override
    public void handle(final ChannelHandlerContext ctx, final WebSocketFrame webSocketFrame) throws ExecutionException {
        webSocketFrame.retain();
        try {
            super.handle(ctx, webSocketFrame);
        } catch (ExecutionException | RuntimeException e) {
            webSocketFrame.release();
            throw e;
        }
    }
}
