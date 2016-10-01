package info.smart_tools.smartactors.core.http_request_handler;


import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.channel_handler_netty.ChannelHandlerNetty;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.i_add_request_parameters_to_iobject.IAddRequestParametersToIObject;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.RequestHandlerDataException;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.RequestHandlerInternalException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.iresponse_sender.exceptions.ResponseSendingException;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Endpoint handler for HTTP requests.
 * It should parse request and send response if there are some problems on parsing.
 */
public class HttpRequestHandler extends EndpointHandler<ChannelHandlerContext, FullHttpRequest> {
    private final String name;

    private final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;

    /**
     * Constructor for HttpRequestHandler
     *
     * @param scope              scope for HttpRequestHandler
     * @param environmentHandler handler for environment
     * @param receiver           chain, that should receive message
     * @param name               name of the endpoint
     */
    public HttpRequestHandler(
            final IScope scope, final IEnvironmentHandler environmentHandler, final IReceiverChain receiver,
            final String name) {
        super(receiver, environmentHandler, scope, name);
        this.name = name;
    }

    @Override
    protected IObject getEnvironment(final ChannelHandlerContext ctx, final FullHttpRequest request)
            throws RequestHandlerDataException, RequestHandlerInternalException, ReadValueException {
        try {
            IDeserializeStrategy deserializeStrategy = IOC.resolve(
                    Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                    IOC.resolve(Keys.getOrAdd("http_request_key_for_deserialize"), request),
                    name
            );

            //resolving body of the request
            IObject message = null;
            try {
                message = deserializeStrategy.deserialize(request);
            } catch (DeserializationException e) {
                IObject exception = IOC.resolve(Keys.getOrAdd("HttpPostParametersToIObjectException"));
                ctx.writeAndFlush(formExceptionalResponse(exception));
                throw new RequestHandlerDataException(e);
            }

            //resolving uri and another request parameters of the request
            IAddRequestParametersToIObject requestParametersToIObject = IOC.resolve(
                    Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                    "HTTP_GET",
                    name
            );
            try {
                requestParametersToIObject.extract(message, request);
            } catch (Exception e) {
                IObject exceptionalResponse = IOC.resolve(Keys.getOrAdd("HttpRequestParametersToIObjectException"));

                throw new RequestHandlerDataException(e);
            }

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
            IFieldName endpointName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "endpointName");

            IChannelHandler channelHandler = IOC.resolve(Keys.getOrAdd(ChannelHandlerNetty.class.getCanonicalName()), ctx);
            //create context of the MP
            IObject context = IOC.resolve(Keys.getOrAdd("EmptyIObject"));
            context.setValue(channelFieldName, channelHandler);
            context.setValue(cookiesFieldName, new ArrayList<IObject>());
            context.setValue(headersFieldName, new ArrayList<IObject>());
            context.setValue(requestFieldName, request);
            context.setValue(endpointName, name);

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
            context.setValue(finalActionsFieldName, new ArrayList<IAction>() {{
                add(httpFinalAction);
            }});

            //create environment
            environment.setValue(messageFieldName, message);
            environment.setValue(contextFieldName, context);
            return environment;
        } catch (InvalidArgumentException | SerializeException | ChangeValueException | ResolutionException e) {
            try {
                IObject exception = IOC.resolve(Keys.getOrAdd("HttpInternalException"), e);
                ctx.writeAndFlush(formExceptionalResponse(exception));
                throw new RequestHandlerInternalException(e);
            } catch (SerializeException | ResolutionException | InvalidArgumentException e1) {
                throw new RequestHandlerInternalException("Failed to send response", e);
            }
        }
    }

    @Override
    protected void sendExceptionalResponse(final ChannelHandlerContext ctx, final FullHttpRequest request,
                                           final IObject responseIObject) throws SerializeException,
            ReadValueException, InvalidArgumentException, ResolutionException {
        ctx.writeAndFlush(formExceptionalResponse(responseIObject));
    }

    private FullHttpResponse formExceptionalResponse(final IObject iObjectResponse)
            throws SerializeException, ResolutionException, ReadValueException, InvalidArgumentException {
        IFieldName exceptionalStatusCode = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "statusCode");
        ByteBuf byteResponse = Unpooled.wrappedBuffer(((String) iObjectResponse.serialize()).getBytes(Charset.forName("UTF-8")));
        int length = ((String) iObjectResponse.serialize()).length();
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf((Integer) iObjectResponse.getValue(exceptionalStatusCode)),
                        byteResponse);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        return response;
    }

}