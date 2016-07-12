package info.smart_tools.smartactors.core.endpoint_handler;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processor.MessageProcessor;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.receiver_chain.ImmutableReceiverChain;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smat_tools.smartactors.core.iexchange.IExchange;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Base class with a common message handling logic from endpoints.
 * It will initiate message contexts, generate service fields etc...
 * TODO: 'IsRequest' field processing - do we need it now? Can't we just set a no-op realization of {@link IExchange}?
 * TODO: ID generation - will the user associate an ID with the message, or it should be done by server?
 * TODO: do we need an interface for this handler?
 *
 * @param <TContext> type of context associated with endpoint data channel.
 *                   It is used to avoid unnecessary new instances creation in endpoint.
 * @param <TRequest> type of a request received by endpoint
 */
public abstract class EndpointHandler<TContext, TRequest> {
    private final IMessageReceiver receiver;

    public EndpointHandler(final IMessageReceiver receiver) throws ResolutionException {
        this.receiver = receiver;
    }

    /**
     * Parse a message from the given request.
     * Endpoint can receive message in different formats, so we can't make this process common for now.
     *
     * @param request request to the endpoint
     * @return a deserialized message
     * @throws Exception
     */
    protected abstract IObject getEnvironment(TContext ctx, TRequest request) throws Exception;

    /**
     * Handle exceptions during request processing.
     *
     * @param ctx   endpoint channel context
     * @param cause thrown exception
     */
    public abstract void handleException(TContext ctx, Throwable cause);


    /**
     * Get {@link IExchange} object used for communication with the message sender.
     *
     * @param message a received message
     * @param ctx     endpoint channel context
     * @param request request to the endpoint
     * @return a {@link IExchange} object for the given request
     */
    protected abstract IExchange getExchange(IMessage message, TContext ctx, TRequest request) throws ResolutionException;

    /**
     * Handle an endpoint request using the specified context.
     *
     * @param ctx     endpoint channel context
     * @param request request to the endpoint
     * @throws ExecutionException if request failed to handle
     */
    public void handle(final TContext ctx, final TRequest request) throws ExecutionException {
        try {
            IObject environment = getEnvironment(ctx, request);
            SocketAddress address = ((ChannelHandlerContext) ctx).channel().remoteAddress();
            if (address != null) {
                FieldName ipAddressField = new FieldName("ipAddress");
                environment.setValue(ipAddressField, ((InetSocketAddress) address).getAddress().getHostAddress());
            }
            /*
            TODO: add sending of environment to chain
             */
        } catch (Exception e) {
            throw new ExecutionException("Failed to handle request to endpoint", e);
        }
    }
}
