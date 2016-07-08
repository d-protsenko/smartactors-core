package info.smart_tools.smartactors.core.endpoint_handler;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processor.MessageProcessor;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
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
    private final IQueue<ITask> taskQueue;

    public EndpointHandler(final IQueue<ITask> taskQueue) throws ResolutionException {
        this.taskQueue = taskQueue;
    }

    /**
     * Parse a message from the given request.
     * Endpoint can receive message in different formats, so we can't make this process common for now.
     *
     * @param request request to the endpoint
     * @return a deserialized message
     * @throws Exception
     */
    protected abstract IMessage getMessage(TRequest request) throws Exception;


    /**
     * Parse a context from the given request.
     *
     * @param request request to the endpoint
     * @return a context object
     * @throws Exception
     */
    protected abstract IObject getContext(TRequest request) throws Exception;

    /**
     * Get {@link IExchange} object used for communication with the message sender.
     *
     * @param message a received message
     * @param ctx     endpoint channel context
     * @param request request to the endpoint
     * @return a {@link IExchange} object for the given request
     */

    protected abstract IExchange getExchange(IMessage message, TContext ctx, TRequest request);

    /**
     * Handle exceptions during request processing.
     *
     * @param ctx   endpoint channel context
     * @param cause thrown exception
     */
    public abstract void handleException(TContext ctx, Throwable cause);

    /**
     * Handle an endpoint request using the specified context.
     *
     * @param ctx     endpoint channel context
     * @param request request to the endpoint
     * @throws ExecutionException if request failed to handle
     */
    public void handle(final TContext ctx, final TRequest request) throws ExecutionException {
        try {
            IMessage message = getMessage(request);
            generateMessageFields(message);
            if (isRequest(message)) {
                IExchange exchange = getExchange(message, ctx, request);

                //SystemFields.EXCHANGE_FIELD.inject(message, exchange);
            }
            SocketAddress address = ((ChannelHandlerContext) ctx).channel().remoteAddress();
            if (address != null) {
                Field<String> ipAddressField = IOC.resolve(Keys.getOrAdd(Field.class.toString()), "ipAddress");
                ipAddressField.in(message, ((InetSocketAddress) address).getAddress().getHostAddress());
            }
            MessageProcessingSequence messageProcessingSequence = IOC.resolve(
                    Keys.getOrAdd(MessageProcessingSequence.class.toString()), 5, null/*get chain by name message.messageMapId*/
            );
            MessageProcessor messageProcessor = IOC.resolve(
                    Keys.getOrAdd(MessageProcessor.class.toString()), taskQueue, messageProcessingSequence
            );
            messageProcessor.process(message, getContext(request));
        } catch (Exception e) {
            throw new ExecutionException("Failed to handle request to endpoint", e);
        }
    }

    private void generateMessageFields(final IMessage message) throws ChangeValueException, InvalidArgumentException {
        Field<UUID> idField = null;
        try {
            idField = IOC.resolve(Keys.getOrAdd(Field.class.toString()), "ID");
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Can't create field", e);
        }
        idField.in(message, UUID.randomUUID());
    }

    private boolean isRequest(final IMessage message) {
        return true;
    }
}
