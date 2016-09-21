package info.smart_tools.smartactors.core.http_request_handler;


import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.channel_handler_netty.ChannelHandlerNetty;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.iresponse_sender.exceptions.ResponseSendingException;
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

    private final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;

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
        IFieldName finalActionsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "finalActions");
        IFieldName httpResponseIsSentFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "httpResponseIsSent");
        IFieldName httpResponseStatusCodeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "httpResponseStatusCode");
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
        context.setValue(requestFieldName, request);
        context.setValue(httpResponseIsSentFieldName, false);
        IAction<IObject> httpFinalAction = new IAction<IObject>() {
            @Override
            public void execute(final IObject environment) throws ActionExecuteException, InvalidArgumentException {
                try {
                    IObject context = (IObject) environment.getValue(contextFieldName);
                    if (null != context) {
                        if ((boolean) context.getValue(httpResponseIsSentFieldName)) {
                            return;
                        }
                    }
                    IFieldName channelFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "channel");
                    IChannelHandler channelHandler = (IChannelHandler)
                            context.getValue(channelFieldName);

                    IResponse response = IOC.resolve(Keys.getOrAdd(IResponse.class.getCanonicalName()));
                    response.setContent("".getBytes());

                    IResponseSender sender = IOC.resolve(Keys.getOrAdd(IResponseSender.class.getCanonicalName()),
                            environment);
                    context.setValue(httpResponseStatusCodeFieldName, INTERNAL_SERVER_ERROR_STATUS_CODE);
                    sender.send(response, environment, channelHandler);
                } catch (ResolutionException | ReadValueException | ResponseSendingException | ChangeValueException e) {
                    throw new ActionExecuteException("Could not execute final http action.");
                }
            }
        };
        context.setValue(finalActionsFieldName, new ArrayList<IAction>() {{ add(httpFinalAction); }});
        //create environment
        environment.setValue(messageFieldName, message);
        environment.setValue(contextFieldName, context);
        return environment;
    }
}
