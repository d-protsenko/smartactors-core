package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;

/**
 * A {@link IMessageHandler message handler} that does not change types of source message, destination message and
 * connection context.
 *
 * @param <TSrc> type of source message
 * @param <TDst> type of destination message
 * @param <TCtx> type of connection context
 */
public interface IBypassMessageHandler<TSrc, TDst, TCtx> extends IMessageHandler<TSrc, TDst, TCtx, TSrc, TDst, TCtx> {
}
