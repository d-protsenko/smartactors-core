package info.smart_tools.smartactors.endpoint_components_generic.default_message_handler_pipeline;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;

public class DefaultStaticMessageHandlerPipelineBuilder<T extends IMessageContext> {
    private static final IMessageHandlerCallback<IMessageContext> DEFAULT_DEAD_END_CALLBACK
            = ctx -> {
        throw new MessageHandlerException("End of pipeline reached.");
    };

    private final IMessageHandlerCallback<T> callback;

    private DefaultStaticMessageHandlerPipelineBuilder(final IMessageHandlerCallback<T> callback) {
        this.callback = callback;
    }

    public static DefaultStaticMessageHandlerPipelineBuilder<IMessageContext>
            create() {
        return new DefaultStaticMessageHandlerPipelineBuilder<>(DEFAULT_DEAD_END_CALLBACK);
    }

    public <S extends IDefaultMessageContext> DefaultStaticMessageHandlerPipelineBuilder<S>
            add(final IMessageHandler<S, T> handler) {
        return new DefaultStaticMessageHandlerPipelineBuilder<>(ctx -> {
            handler.handle(callback, ctx);
        });
    }

    public IMessageHandlerCallback<T> finish() {
        return callback;
    }
}
