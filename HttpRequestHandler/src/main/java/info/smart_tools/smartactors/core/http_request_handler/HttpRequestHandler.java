package info.smart_tools.smartactors.core.http_request_handler;


import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.channel_handler_netty.ChannelHandlerNetty;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;

/**
 * Endpoint handler for HTTP requests.
 */
public class HttpRequestHandler extends EndpointHandler<ChannelHandlerContext, FullHttpRequest> {

    /**
     * Constructor for HttpRequestHandler
     *
     * @param scope              scope for HttpRequestHandler
     * @param environmentHandler handler for environment
     * @param receiver           chain, that should receive message
     */
    public HttpRequestHandler(
            final IScope scope, final IEnvironmentHandler environmentHandler, final IReceiverChain receiver) {
        super(receiver, environmentHandler, scope);
    }

    @Override
    protected IObject getEnvironment(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        IDeserializeStrategy deserializeStrategy = IOC.resolve(Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()), request);
        IObject message = deserializeStrategy.deserialize(request);
        IObject environment = IOC.resolve(Keys.getOrAdd("EmptyIObject"));
        IFieldName messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
        IFieldName contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
        IFieldName requestFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "request");
        IFieldName channelFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "channel");
        IFieldName headersFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "headers");
        IFieldName cookiesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "cookies");

        IChannelHandler channelHandler = IOC.resolve(Keys.getOrAdd(ChannelHandlerNetty.class.getCanonicalName()), ctx);
        //create context of the MP
        IObject context = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        context.setValue(channelFieldName, channelHandler);
        context.setValue(cookiesFieldName, new ArrayList<IObject>());
        context.setValue(headersFieldName, new ArrayList<IObject>());
        //create environment
        environment.setValue(messageFieldName, message);
        environment.setValue(contextFieldName, context);
        environment.setValue(requestFieldName, request);
        return environment;
    }
}
