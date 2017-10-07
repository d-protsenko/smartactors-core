package info.smart_tools.smartactors.endpoint_components_generic.error_message_handler;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;

/**
 * {@link IMessageHandler Message handler} that always throws a exception with configured message.
 *
 * @param <TCtx>
 * @param <TNextCtx>
 */
public class ErrorMessageHandler<TCtx extends IMessageContext, TNextCtx extends IMessageContext>
        implements IMessageHandler<TCtx, TNextCtx> {
    private final String errorMessage;

    /**
     * The constructor.
     *
     * @param errorMessage text message for thrown exception
     */
    public ErrorMessageHandler(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public void handle(
        final IMessageHandlerCallback<TNextCtx> next,
        final TCtx context)
            throws MessageHandlerException {
        throw new MessageHandlerException(errorMessage);
    }
}
