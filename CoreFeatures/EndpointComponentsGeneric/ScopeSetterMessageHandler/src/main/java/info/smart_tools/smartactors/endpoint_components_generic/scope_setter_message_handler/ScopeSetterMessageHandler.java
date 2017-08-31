package info.smart_tools.smartactors.endpoint_components_generic.scope_setter_message_handler;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers.IBypassMessageHandler;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

/**
 * A {@link info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler message handler} that sets
 * a scope for the next handler in pipeline.
 *
 * @param <TSrc>
 * @param <TDst>
 * @param <TCtx>
 */
public class ScopeSetterMessageHandler<TSrc, TDst, TCtx> implements IBypassMessageHandler<TSrc, TDst, TCtx> {
    private final IScope scope;

    /**
     * The constructor.
     *
     * @param scope the scope to set for next handler
     */
    public ScopeSetterMessageHandler(final IScope scope) {
        this.scope = scope;
    }

    @Override
    public void handle(
            final IMessageHandlerCallback<TSrc, TDst, TCtx> next,
            final TSrc srcMessage, final TDst dstMessage, final TCtx ctx)
            throws MessageHandlerException {
        try {
            ScopeProvider.setCurrentScope(scope);
        } catch (ScopeProviderException e) {
            throw new MessageHandlerException(e);
        }

        next.handle(srcMessage, dstMessage, ctx);
    }
}
