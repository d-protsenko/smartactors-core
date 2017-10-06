package info.smart_tools.smartactors.endpoint_components_generic.default_endpoint_pipeline_implementation;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.endpoint_interfaces.iendpoint_pipeline.IEndpointPipeline;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;

import java.util.List;
import java.util.ListIterator;

public class DefaultEndpointPipelineImplementation implements IEndpointPipeline {
    private final List<IMessageHandler> handlers;
    private final IFunction0<IMessageContext> contextFactory;
    private IMessageHandlerCallback<IMessageContext> callback;

    public DefaultEndpointPipelineImplementation(
            final List<IMessageHandler> handlers,
            final IFunction0<IMessageContext> contextFactory) {
        this.callback = ctx -> {
            throw new MessageHandlerException("End of pipeline reached.");
        };
        this.contextFactory = contextFactory;
        this.handlers = handlers;
        ListIterator li = handlers.listIterator(handlers.size());
        while(li.hasPrevious()) {
            IMessageHandler handler = (IMessageHandler) li.previous();
            this.callback = (ctx -> {
                handler.handle(callback, ctx);
            });
        }
    }

    @Override
    public Iterable<IMessageHandler> getHandlers() {
        return handlers;
    }

    @Override
    public IFunction0 getContextFactory() {
        return contextFactory;
    }

    @Override
    public IMessageHandlerCallback getInputCallback() {
        return callback;
    }
}
