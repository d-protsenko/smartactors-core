package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler;


import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;

/**
 * Interface for an object that handles a source message.
 *
 * <p>
 *  Message handling may include one or more actions of the following kinds:
 *
 *  <ul>
 *      <li>
 *          Environment setup for consequent handlers. E.g. execute next handler in another thread or setup some
 *          thread-local variables.
 *      </li>
 *      <li>
 *          Context transformation.
 *      </li>
 *      <li>
 *          Source message transformation. Including aggregation/split (in this case the next handler may be called more
 *          or less times than previous one).
 *      </li>
 *      <li>
 *          Creation of destination message.
 *      </li>
 *      <li>
 *          Destination message transformation. Created destination message may be transformed using some data taken
 *          from source message and/or connection context. Examples: parse headers of inbound HTTP message and store
 *          them in internal message environment or vise versa setting headers of outbound HTTP message from data stored
 *          in internal message.
 *      </li>
 *      <li>
 *          Sending destination message. Such action is most probably performed by the last handler of pipeline.
 *      </li>
 *  </ul>
 * </p>
 *
 * <p>
 *  Message handlers are joined into pipelines. Message handler may let the next handler of pipeline to continue message
 *  handling by calling {@link IMessageHandlerCallback callback} passed to it. Message handler may call that callback
 *  zero, one or more than one time for a call of handler method.
 * </p>
 *
 * <p>
 *  In case of inbound message the external inbound message (concrete type depends on endpoint implementation) is source
 *  message for the first {@link IMessageHandler message handler} and the internal message environment is destination
 *  message produced by some of intermediate message handlers.
 *  One of the pipeline handlers (probably the last one) may send the internal message.
 * </p>
 *
 * <p>
 *  In case of outbound message the internal message is a source message for the first message handler and the endpoint
 *  implementation depended representation of external message is the destination message produced by some of
 *  intermediate message handlers.
 *  One of the pipeline handlers (probably the last one) may send the external message in
 *  endpoint-implementation-specific way.
 * </p>
 *
 * @param <TSrc>     source message type
 * @param <TDst>     destination message type
 * @param <TCtx>     connection context type
 * @param <TNextDst> type of destination message passed to the next handler
 * @param <TNextSrc> type of source message passed to the next handler. Handler may transform/split/aggregate the
 *                  message so {@code TSrc} and {@code TNextSrc} may be different types
 * @param <TNextCtx> type of connection context passed to next handler. Handler may switch between application-specific
 *                  and endpoint-implementation-specific context types so {@code TCtx} and {@code TNextCtx} may be
 *                  different types
 */
public interface IMessageHandler<TSrc, TDst, TCtx, TNextSrc, TNextDst, TNextCtx> {
    /**
     * Handle the message.
     *
     * @param next       callback that should handle the destination message after this handler
     * @param srcMessage the source message
     * @param dstMessage the destination message, may be {@code null}
     * @param ctx        the connection context
     * @throws MessageHandlerException if any error occurs handling te message
     */
    void handle(IMessageHandlerCallback<TNextSrc, TNextDst, TNextCtx> next, TSrc srcMessage, TDst dstMessage, TCtx ctx)
            throws MessageHandlerException;
}
