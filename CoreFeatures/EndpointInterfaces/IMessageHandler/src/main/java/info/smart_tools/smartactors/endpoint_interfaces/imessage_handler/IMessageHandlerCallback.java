package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;

/**
 * Interface for a callback passed to {@link IMessageHandlerCallback}.
 *
 * @param <TSrc> source message type
 * @param <TDst> destination message type
 * @param <TCtx> context type
 */
public interface IMessageHandlerCallback<TSrc, TDst, TCtx> {
    /**
     * Delegate message handling to the next handler.
     *
     * @param srcMessage the source message
     * @param dstMessage the destination message
     * @param ctx        the connection context
     * @throws MessageHandlerException if any error occurs
     */
    void handle(TSrc srcMessage, TDst dstMessage, TCtx ctx)
            throws MessageHandlerException;
}
