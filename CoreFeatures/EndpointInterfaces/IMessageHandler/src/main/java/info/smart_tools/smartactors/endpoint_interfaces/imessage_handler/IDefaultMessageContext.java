package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler;

public interface IDefaultMessageContext<TSrc, TDst, TCtx> extends IMessageContext {
    TSrc getSrcMessage();

    TDst getDstMessage();

    TCtx getConnectionContext();

    void setSrcMessage(TSrc message);

    void setDstMessage(TDst message);

    void setConnectionContext(TCtx context);
}
