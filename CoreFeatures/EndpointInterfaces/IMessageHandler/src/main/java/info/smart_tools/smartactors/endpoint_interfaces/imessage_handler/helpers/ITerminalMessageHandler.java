package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;

/**
 * A {@link IMessageHandler message handler} that does not pass a message to the nest handler.
 *
 * @param <TSrc>
 * @param <TDst>
 * @param <TCtx>
 */
public interface ITerminalMessageHandler<TSrc, TDst, TCtx> extends IMessageHandler<TSrc, TDst, TCtx, Void, Void, Void> {
}
