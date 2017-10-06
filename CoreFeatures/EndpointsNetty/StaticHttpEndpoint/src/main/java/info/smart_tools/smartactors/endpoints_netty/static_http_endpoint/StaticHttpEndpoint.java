package info.smart_tools.smartactors.endpoints_netty.static_http_endpoint;

import info.smart_tools.smartactors.endpoint_components_generic.asynchronous_unordered_message_handler.AsynchronousUnorderedMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.create_environment_message_handler.CreateEnvironmentMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_codecs.impl.json.JsonBlockDecoder;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_codecs.impl.json.JsonBlockEncoder;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_context_implementation.DefaultMessageContextImplementation;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_handler_pipeline.DefaultStaticMessageHandlerPipelineBuilder;
import info.smart_tools.smartactors.endpoint_components_generic.endpoint_response_strategy.EndpointResponseStrategy;
import info.smart_tools.smartactors.endpoint_components_generic.generic_exception_interceptor_message_handler.GenericExceptionInterceptorMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.response_strategy_set_message_handler.ResponseStrategySetMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.send_internal_message_handler.SendInternalMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.http_exceptional_action.HttpServerExceptionalAction;
import info.smart_tools.smartactors.endpoint_components_netty.http_query_string_parser.HttpQueryStringParser;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_channel_handler.InboundNettyChannelHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.ReleaseNettyMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.RetainNettyMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.message_extractors.InboundMessageExtractor;
import info.smart_tools.smartactors.endpoint_components_netty.outbound_netty_message_creation_handler.OutboundNettyMessageCreationHandler;
import info.smart_tools.smartactors.endpoint_components_netty.send_netty_message_message_handler.SendNettyMessageMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.wrap_inbound_netty_message_to_message_byte_array_message_handler.WrapInboundNettyMessageToMessageByteArrayMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_byte_array.IInboundMessageByteArray;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_byte_array.IOutboundMessageByteArray;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;


public class StaticHttpEndpoint {
    public StaticHttpEndpoint(
            final IReceiverChain mainChain,
            final int internalStackDepth,
            final IQueue<ITask> taskQueue
    ) throws ResolutionException {
        final IMessageHandlerCallback<IDefaultMessageContext<IObject, Void, Channel>> responsePipelineCallback =
                DefaultStaticMessageHandlerPipelineBuilder
                .create()
                .<IDefaultMessageContext<IObject, IOutboundMessageByteArray<FullHttpResponse>, Channel>>
                        add(new SendNettyMessageMessageHandler<>())
                .add(new JsonBlockEncoder<>())
                .add(new OutboundNettyMessageCreationHandler<>(
                                () -> new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)))
                .add(new GenericExceptionInterceptorMessageHandler<>(
                        new HttpServerExceptionalAction()))
                .finish();

        final IMessageHandlerCallback<IDefaultMessageContext<FullHttpRequest, Void, Channel>> requestPipelineCallback =
                DefaultStaticMessageHandlerPipelineBuilder
                .create()
                .<IDefaultMessageContext<IInboundMessageByteArray<FullHttpRequest>, IObject, Channel>>
                        add(new SendInternalMessageHandler<>(
                                internalStackDepth, taskQueue, mainChain))
                .add(new ResponseStrategySetMessageHandler<>(new EndpointResponseStrategy<>(
                        responsePipelineCallback, DefaultMessageContextImplementation::new
                )))
                .add(new HttpQueryStringParser<>())
                .add(new JsonBlockDecoder<>())
                .add(new CreateEnvironmentMessageHandler<>())
                .add(new WrapInboundNettyMessageToMessageByteArrayMessageHandler<>())
                .add(new GenericExceptionInterceptorMessageHandler<>(
                        new HttpServerExceptionalAction()))
                .add(new ReleaseNettyMessageHandler<>(new InboundMessageExtractor<>()))
                .add(new AsynchronousUnorderedMessageHandler<>(taskQueue))
                .add(new RetainNettyMessageHandler<>(new InboundMessageExtractor<>()))
                .add(new GenericExceptionInterceptorMessageHandler<>(
                        new HttpServerExceptionalAction()))
                .finish();

        //ChannelHandler httpInboundHandler = new InboundNettyChannelHandler<>(
        //        requestPipelineCallback, DefaultMessageContextImplementation::new, FullHttpRequest.class);


    }
}
